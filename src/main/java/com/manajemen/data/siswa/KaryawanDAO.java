package com.manajemen.data.siswa;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Semua akses database untuk tabel `karyawan` ada di sini,
 * supaya kode tampilan (panel/dialog) tidak perlu menulis SQL.
 */
public class KaryawanDAO {

    private static final String KOLOM =
        "id, kode_karyawan, nama, jenis_kelamin, jabatan, departemen, "
        + "no_hp, email, alamat, tanggal_masuk, status";

    private static final String PREFIX_KODE = "KRY-";

    /** Ringkasan untuk kartu statistik dan isi filter departemen. */
    public static class Ringkasan {
        public int total;
        public int aktif;
        public int nonaktif;
        public List<String> departemen = new ArrayList<>();
    }

    private Connection koneksi() throws SQLException {
        return KoneksiDB.ambil();
    }

    // =====================================================
    // BACA DATA
    // =====================================================

    /**
     * Mencari karyawan. Parameter yang null/kosong berarti tidak difilter.
     * Kata kunci dicocokkan ke kode, nama, jabatan, departemen, no. HP, dan email.
     */
    public List<Karyawan> cari(String kataKunci, String status, String departemen) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " + KOLOM + " FROM karyawan WHERE 1=1");
        List<String> param = new ArrayList<>();

        if (kataKunci != null && !kataKunci.trim().isEmpty()) {
            sql.append(" AND (kode_karyawan LIKE ? OR nama LIKE ? OR jabatan LIKE ?")
               .append(" OR departemen LIKE ? OR no_hp LIKE ? OR email LIKE ?)");
            String pola = "%" + escapeLike(kataKunci.trim()) + "%";
            for (int i = 0; i < 6; i++) {
                param.add(pola);
            }
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            param.add(status);
        }
        if (departemen != null && !departemen.isEmpty()) {
            sql.append(" AND departemen = ?");
            param.add(departemen);
        }
        sql.append(" ORDER BY nama ASC");

        List<Karyawan> hasil = new ArrayList<>();
        try (PreparedStatement ps = koneksi().prepareStatement(sql.toString())) {
            for (int i = 0; i < param.size(); i++) {
                ps.setString(i + 1, param.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(petakan(rs));
                }
            }
        }
        return hasil;
    }

    /** Mencari satu karyawan dari kode (misalnya hasil scan). Null kalau tidak ada. */
    public Karyawan cariByKode(String kode) throws SQLException {
        String sql = "SELECT " + KOLOM + " FROM karyawan WHERE kode_karyawan = ? LIMIT 1";
        try (PreparedStatement ps = koneksi().prepareStatement(sql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        }
    }

    public Ringkasan ringkasan() throws SQLException {
        Ringkasan r = new Ringkasan();
        Connection c = koneksi();

        String sqlHitung = "SELECT COUNT(*), "
            + "COALESCE(SUM(status = 'Aktif'), 0), "
            + "COALESCE(SUM(status = 'Nonaktif'), 0) FROM karyawan";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sqlHitung)) {
            if (rs.next()) {
                r.total = rs.getInt(1);
                r.aktif = rs.getInt(2);
                r.nonaktif = rs.getInt(3);
            }
        }

        String sqlDep = "SELECT DISTINCT departemen FROM karyawan ORDER BY departemen";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sqlDep)) {
            while (rs.next()) {
                r.departemen.add(rs.getString(1));
            }
        }
        return r;
    }

    /** Kode berikutnya, misalnya KRY-001, KRY-002, dst. */
    public String kodeBerikutnya() throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(kode_karyawan, "
            + (PREFIX_KODE.length() + 1) + ") AS UNSIGNED)), 0) + 1 "
            + "FROM karyawan WHERE kode_karyawan REGEXP '^KRY-[0-9]+$'";
        try (Statement st = koneksi().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            int nomor = rs.next() ? rs.getInt(1) : 1;
            return String.format("%s%03d", PREFIX_KODE, nomor);
        }
    }

    // =====================================================
    // UBAH DATA
    // =====================================================

    public void tambah(Karyawan k) throws SQLException {
        String sql = "INSERT INTO karyawan (kode_karyawan, nama, jenis_kelamin, jabatan, departemen, "
            + "no_hp, email, alamat, tanggal_masuk, status) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = koneksi().prepareStatement(sql)) {
            ps.setString(1, k.getKode());
            ps.setString(2, k.getNama());
            ps.setString(3, k.getJenisKelamin());
            ps.setString(4, k.getJabatan());
            ps.setString(5, k.getDepartemen());
            ps.setString(6, kosongJadiNull(k.getNoHp()));
            ps.setString(7, kosongJadiNull(k.getEmail()));
            ps.setString(8, kosongJadiNull(k.getAlamat()));
            ps.setDate(9, Date.valueOf(k.getTanggalMasuk()));
            ps.setString(10, k.getStatus());
            ps.executeUpdate();
        }
    }

    /** Kode karyawan sengaja tidak ikut diubah, karena dipakai sebagai identitas saat scan presensi. */
    public void ubah(Karyawan k) throws SQLException {
        String sql = "UPDATE karyawan SET nama = ?, jenis_kelamin = ?, jabatan = ?, departemen = ?, "
            + "no_hp = ?, email = ?, alamat = ?, tanggal_masuk = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = koneksi().prepareStatement(sql)) {
            ps.setString(1, k.getNama());
            ps.setString(2, k.getJenisKelamin());
            ps.setString(3, k.getJabatan());
            ps.setString(4, k.getDepartemen());
            ps.setString(5, kosongJadiNull(k.getNoHp()));
            ps.setString(6, kosongJadiNull(k.getEmail()));
            ps.setString(7, kosongJadiNull(k.getAlamat()));
            ps.setDate(8, Date.valueOf(k.getTanggalMasuk()));
            ps.setString(9, k.getStatus());
            ps.setInt(10, k.getId());
            ps.executeUpdate();
        }
    }

    public void ubahStatus(int id, String status) throws SQLException {
        try (PreparedStatement ps = koneksi().prepareStatement("UPDATE karyawan SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void hapus(int id) throws SQLException {
        try (PreparedStatement ps = koneksi().prepareStatement("DELETE FROM karyawan WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // =====================================================
    // UTILITAS
    // =====================================================

    /** Dipakai juga oleh PresensiDAO; membaca kolom berdasarkan nama. */
    static Karyawan petakan(ResultSet rs) throws SQLException {
        Karyawan k = new Karyawan();
        k.setId(rs.getInt("id"));
        k.setKode(rs.getString("kode_karyawan"));
        k.setNama(rs.getString("nama"));
        k.setJenisKelamin(rs.getString("jenis_kelamin"));
        k.setJabatan(rs.getString("jabatan"));
        k.setDepartemen(rs.getString("departemen"));
        k.setNoHp(rs.getString("no_hp"));
        k.setEmail(rs.getString("email"));
        k.setAlamat(rs.getString("alamat"));
        Date tgl = rs.getDate("tanggal_masuk");
        k.setTanggalMasuk(tgl == null ? null : tgl.toLocalDate());
        k.setStatus(rs.getString("status"));
        return k;
    }

    private static String kosongJadiNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    /** Supaya karakter % dan _ yang diketik pengguna dicari apa adanya, bukan sebagai wildcard. */
    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}