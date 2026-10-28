package com.manajemen.data.siswa;

import com.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Satu pintu untuk mengambil koneksi database, dipakai bersama oleh semua DAO.
 * Koneksi dipakai ulang dan hanya dibuat lagi kalau sudah mati (misalnya MySQL di-restart),
 * jadi DAO tidak perlu menutupnya setiap selesai query.
 *
 * Kalau nama method di DatabaseConnection milik Anda berbeda dari getConnection(),
 * cukup ubah satu baris di method ambil() di bawah ini.
 */
final class KoneksiDB {

    private static Connection koneksi;

    private KoneksiDB() {
    }

    static synchronized Connection ambil() throws SQLException {
        if (koneksi == null || koneksi.isClosed() || !koneksi.isValid(2)) {
            koneksi = DatabaseConnection.getConnection();
            if (koneksi == null) {
                throw new SQLException("Koneksi ke database gagal (koneksi bernilai null).");
            }
        }
        return koneksi;
    }
}
