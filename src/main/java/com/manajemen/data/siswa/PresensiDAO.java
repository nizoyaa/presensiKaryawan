package com.manajemen.data.siswa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Akses database untuk tabel `presensi` dan aturan pencatatan hasil scan.
 *
 * Aturan: setiap karyawan hanya punya satu baris per tanggal.
 * Scan pertama pada hari itu = masuk, scan berikutnya = pulang.
 */
public class PresensiDAO {

    /**
     * Scan kedua baru dianggap "pulang" kalau sudah lewat jeda ini sejak masuk.
     * Tujuannya mencegah scan ganda yang tidak disengaja langsung tercatat sebagai pulang.
     */
    public static final int JEDA_MINIMAL_MENIT = 5;

    /** Keputusan untuk satu scan, ditentukan murni dari data presensi hari itu. */
    enum Aksi { MASUK, PULANG, SUDAH_LENGKAP, TERLALU_CEPAT }

    /** Angka ringkasan untuk kartu statistik. */
    public static class RingkasanHari {
        public int aktif;
        public int hadir;
        public int pulang;

        public int masihDiKantor() {
            return hadir - pulang;
        }

        public int belumHadir() {
            return Math.max(0, aktif - hadir);
        }
    }

    /** Semua data yang dibutuhkan layar scan untuk satu tanggal. */
    public static class DataHari {
        public final LocalDate tanggal;
        public List<Presensi> presensi = new ArrayList<>();
        public List<Karyawan> belumHadir = new ArrayList<>();
        public RingkasanHari ringkasan = new RingkasanHari();

        DataHari(LocalDate tanggal) {
            this.tanggal = tanggal;
        }
    }

    private final KaryawanDAO karyawanDao = new KaryawanDAO();

    private Connection koneksi() throws SQLException {
        return KoneksiDB.ambil();
    }

    // =====================================================
    // ATURAN PRESENSI
    // =====================================================

    /**
     * Menentukan apa yang harus dilakukan untuk scan pada jam tertentu.
     * Dipisah dari SQL supaya aturannya mudah dibaca dan diuji.
     *
     * @param hariIni presensi karyawan pada tanggal tersebut, atau null kalau belum ada
     */
    static Aksi tentukanAksi(Presensi hariIni, LocalTime jam) {
        if (hariIni == null) {
            return Aksi.MASUK;
        }
        if (hariIni.getJamPulang() != null) {
            return Aksi.SUDAH_LENGKAP;
        }
        long menitSejakMasuk = Duration.between(hariIni.getJamMasuk(), jam).toMinutes();
        return menitSejakMasuk < JEDA_MINIMAL_MENIT ? Aksi.TERLALU_CEPAT : Aksi.PULANG;
    }

    /**
     * Mencatat satu scan. Waktu diberikan pemanggil (bukan diambil di sini) supaya jam yang tercatat
     * adalah saat kartu di-scan, bukan saat proses database selesai.
     */
    public HasilScan catatScan(String kode, LocalDateTime waktu) throws SQLException {
        Karyawan k = karyawanDao.cariByKode(kode);
        if (k == null) {
            return HasilScan.tidakDikenal(kode);
        }
        if (!k.isAktif()) {
            return HasilScan.nonaktif(k);
        }

        LocalDate tanggal = waktu.toLocalDate();
        LocalTime jam = waktu.toLocalTime().withNano(0);

        Presensi p = cariPresensi(k.getId(), tanggal);
        if (p == null) {
            try {
                return HasilScan.masuk(k, catatMasuk(k, tanggal, jam));
            } catch (SQLException ex) {
                if (!duplikat(ex)) {
                    throw ex;
                }
                // Scan lain sudah mencatat masuk lebih dulu; lanjutkan sebagai scan kedua.
                p = cariPresensi(k.getId(), tanggal);
                if (p == null) {
                    throw ex;
                }
            }
        }

        switch (tentukanAksi(p, jam)) {
            case SUDAH_LENGKAP:
                return HasilScan.sudahLengkap(k, p);
            case TERLALU_CEPAT:
                return HasilScan.terlaluCepat(k, p, p.getJamMasuk().plusMinutes(JEDA_MINIMAL_MENIT));
            default:
                if (catatPulang(p.getId(), jam) == 0) {
                    // Sudah diisi oleh scan lain di antara pengecekan dan penyimpanan.
                    Presensi terbaru = cariPresensi(k.getId(), tanggal);
                    if (terbaru == null) {
                        throw new SQLException("Data presensi berubah saat diproses. Silakan scan ulang.");
                    }
                    return HasilScan.sudahLengkap(k, terbaru);
                }
                p.setJamPulang(jam);
                return HasilScan.pulang(k, p);
        }
    }

    // =====================================================
    // DATA UNTUK TAMPILAN
    // =====================================================

    /** Mengambil presensi, daftar yang belum hadir, dan ringkasan untuk satu tanggal. */
    public DataHari muatHari(LocalDate tanggal) throws SQLException {
        DataHari data = new DataHari(tanggal);
        Connection c = koneksi();
        java.sql.Date tgl = java.sql.Date.valueOf(tanggal);

        // Yang terbaru (scan paling akhir) tampil paling atas.
        String sqlPresensi = "SELECT p.id, p.karyawan_id, p.tanggal, p.jam_masuk, p.jam_pulang, "
            + "k.kode_karyawan, k.nama, k.jabatan, k.departemen "
            + "FROM presensi p JOIN karyawan k ON k.id = p.karyawan_id "
            + "WHERE p.tanggal = ? "
            + "ORDER BY COALESCE(p.jam_pulang, p.jam_masuk) DESC, p.id DESC";
        try (PreparedStatement ps = c.prepareStatement(sqlPresensi)) {
            ps.setDate(1, tgl);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Presensi p = petakanPresensi(rs);
                    p.setKode(rs.getString("kode_karyawan"));
                    p.setNama(rs.getString("nama"));
                    p.setJabatan(rs.getString("jabatan"));
                    p.setDepartemen(rs.getString("departemen"));
                    data.presensi.add(p);
                }
            }
        }

        String sqlBelum = "SELECT k.* FROM karyawan k WHERE k.status = 'Aktif' "
            + "AND NOT EXISTS (SELECT 1 FROM presensi p WHERE p.karyawan_id = k.id AND p.tanggal = ?) "
            + "ORDER BY k.nama";
        try (PreparedStatement ps = c.prepareStatement(sqlBelum)) {
            ps.setDate(1, tgl);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.belumHadir.add(KaryawanDAO.petakan(rs));
                }
            }
        }

        // Hanya karyawan aktif yang dihitung, supaya angkanya konsisten dengan daftar "belum hadir".
        String sqlRingkasan = "SELECT (SELECT COUNT(*) FROM karyawan WHERE status = 'Aktif'), "
            + "COUNT(p.id), COALESCE(SUM(p.jam_pulang IS NOT NULL), 0) "
            + "FROM presensi p JOIN karyawan k ON k.id = p.karyawan_id AND k.status = 'Aktif' "
            + "WHERE p.tanggal = ?";
        try (PreparedStatement ps = c.prepareStatement(sqlRingkasan)) {
            ps.setDate(1, tgl);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data.ringkasan.aktif = rs.getInt(1);
                    data.ringkasan.hadir = rs.getInt(2);
                    data.ringkasan.pulang = rs.getInt(3);
                }
            }
        }
        return data;
    }

    // =====================================================
    // QUERY DASAR
    // =====================================================

    private Presensi cariPresensi(int karyawanId, LocalDate tanggal) throws SQLException {
        String sql = "SELECT id, karyawan_id, tanggal, jam_masuk, jam_pulang "
            + "FROM presensi WHERE karyawan_id = ? AND tanggal = ?";
        try (PreparedStatement ps = koneksi().prepareStatement(sql)) {
            ps.setInt(1, karyawanId);
            ps.setDate(2, java.sql.Date.valueOf(tanggal));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakanPresensi(rs) : null;
            }
        }
    }

    private Presensi catatMasuk(Karyawan k, LocalDate tanggal, LocalTime jam) throws SQLException {
        String sql = "INSERT INTO presensi (karyawan_id, tanggal, jam_masuk) VALUES (?, ?, ?)";
        try (PreparedStatement ps = koneksi().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, k.getId());
            ps.setDate(2, java.sql.Date.valueOf(tanggal));
            ps.setTime(3, Time.valueOf(jam));
            ps.executeUpdate();

            Presensi p = new Presensi();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                if (kunci.next()) {
                    p.setId(kunci.getInt(1));
                }
            }
            p.setKaryawanId(k.getId());
            p.setTanggal(tanggal);
            p.setJamMasuk(jam);
            p.setKode(k.getKode());
            p.setNama(k.getNama());
            p.setJabatan(k.getJabatan());
            p.setDepartemen(k.getDepartemen());
            return p;
        }
    }

    /** Mengembalikan jumlah baris yang berubah; 0 berarti pulang sudah terisi oleh scan lain. */
    private int catatPulang(int presensiId, LocalTime jam) throws SQLException {
        String sql = "UPDATE presensi SET jam_pulang = ? WHERE id = ? AND jam_pulang IS NULL";
        try (PreparedStatement ps = koneksi().prepareStatement(sql)) {
            ps.setTime(1, Time.valueOf(jam));
            ps.setInt(2, presensiId);
            return ps.executeUpdate();
        }
    }

    private static Presensi petakanPresensi(ResultSet rs) throws SQLException {
        Presensi p = new Presensi();
        p.setId(rs.getInt("id"));
        p.setKaryawanId(rs.getInt("karyawan_id"));
        p.setTanggal(rs.getDate("tanggal").toLocalDate());
        p.setJamMasuk(rs.getTime("jam_masuk").toLocalTime());
        Time pulang = rs.getTime("jam_pulang");
        p.setJamPulang(pulang == null ? null : pulang.toLocalTime());
        return p;
    }

    /** Pelanggaran kunci unik (karyawan + tanggal sudah ada). */
    private static boolean duplikat(SQLException ex) {
        return ex.getErrorCode() == 1062 || "23000".equals(ex.getSQLState());
    }
}
