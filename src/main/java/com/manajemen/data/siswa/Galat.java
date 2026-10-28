package com.manajemen.data.siswa;

import java.sql.SQLException;

/**
 * Mengubah error teknis (terutama SQLException) menjadi pesan yang bisa dipahami admin.
 */
final class Galat {

    private static final int MYSQL_DUPLIKAT = 1062;
    private static final int MYSQL_TERPAKAI_TABEL_LAIN = 1451;

    private Galat() {
    }

    /** Pesan teknis apa adanya dari exception. */
    static String detail(Throwable ex) {
        return (ex == null || ex.getMessage() == null || ex.getMessage().trim().isEmpty())
            ? "Terjadi kesalahan yang tidak diketahui."
            : ex.getMessage().trim();
    }

    /** Saran perbaikan sesuai jenis error. Kosong kalau tidak ada saran khusus. */
    static String saran(Throwable ex) {
        String detail = detail(ex);
        if (ex instanceof SQLException) {
            SQLException sql = (SQLException) ex;
            if (sql.getErrorCode() == MYSQL_TERPAKAI_TABEL_LAIN) {
                return "Data ini sudah dipakai di tabel lain (misalnya riwayat presensi) sehingga tidak bisa "
                    + "dihapus. Gunakan 'Nonaktifkan' agar riwayatnya tetap aman.";
            }
            if (sql.getErrorCode() == MYSQL_DUPLIKAT) {
                return "Data dengan kode yang sama sudah ada. Coba ulangi.";
            }
        }
        if (detail.contains("doesn't exist")) {
            return "Tabel belum dibuat. Jalankan karyawan.sql lalu presensi.sql di phpMyAdmin "
                + "pada database db_presensikaryawan.";
        }
        if (detail.contains("Communications link failure") || detail.contains("Connection refused")) {
            return "Pastikan MySQL (XAMPP) sedang berjalan.";
        }
        return "";
    }

    /** Detail + saran, untuk kotak dialog. */
    static String pesan(Throwable ex) {
        String saran = saran(ex);
        return saran.isEmpty() ? detail(ex) : detail(ex) + "\n\n" + saran;
    }

    /** Satu kalimat pendek, untuk ditampilkan di spanduk atau baris status. */
    static String ringkas(Throwable ex) {
        String saran = saran(ex);
        String teks = saran.isEmpty() ? detail(ex) : saran;
        return teks.length() > 110 ? teks.substring(0, 107) + "..." : teks;
    }
}
