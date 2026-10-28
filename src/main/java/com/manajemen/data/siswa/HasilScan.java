package com.manajemen.data.siswa;

import java.time.LocalTime;

/**
 * Hasil dari satu kali scan: apa yang terjadi dan data pendukungnya,
 * supaya tampilan bisa menyusun pesan tanpa perlu tahu aturan presensinya.
 */
public class HasilScan {

    public enum Jenis {
        /** Presensi masuk baru dicatat. */
        MASUK,
        /** Presensi pulang baru dicatat. */
        PULANG,
        /** Masuk dan pulang hari ini sudah tercatat; scan diabaikan. */
        SUDAH_LENGKAP,
        /** Baru saja masuk; scan kedua terlalu dekat sehingga tidak dicatat sebagai pulang. */
        TERLALU_CEPAT,
        /** Kode tidak ada di Data Karyawan. */
        TIDAK_DIKENAL,
        /** Karyawan ada tetapi berstatus nonaktif. */
        NONAKTIF
    }

    private final Jenis jenis;
    private final String kode;
    private final Karyawan karyawan;
    private final Presensi presensi;
    private final LocalTime bolehPulang;

    private HasilScan(Jenis jenis, String kode, Karyawan karyawan, Presensi presensi, LocalTime bolehPulang) {
        this.jenis = jenis;
        this.kode = kode;
        this.karyawan = karyawan;
        this.presensi = presensi;
        this.bolehPulang = bolehPulang;
    }

    public static HasilScan masuk(Karyawan k, Presensi p) {
        return new HasilScan(Jenis.MASUK, k.getKode(), k, p, null);
    }

    public static HasilScan pulang(Karyawan k, Presensi p) {
        return new HasilScan(Jenis.PULANG, k.getKode(), k, p, null);
    }

    public static HasilScan sudahLengkap(Karyawan k, Presensi p) {
        return new HasilScan(Jenis.SUDAH_LENGKAP, k.getKode(), k, p, null);
    }

    public static HasilScan terlaluCepat(Karyawan k, Presensi p, LocalTime bolehPulang) {
        return new HasilScan(Jenis.TERLALU_CEPAT, k.getKode(), k, p, bolehPulang);
    }

    public static HasilScan tidakDikenal(String kode) {
        return new HasilScan(Jenis.TIDAK_DIKENAL, kode, null, null, null);
    }

    public static HasilScan nonaktif(Karyawan k) {
        return new HasilScan(Jenis.NONAKTIF, k.getKode(), k, null, null);
    }

    public Jenis getJenis() { return jenis; }

    /** Kode yang dipindai (untuk TIDAK_DIKENAL berisi teks yang masuk dari scanner). */
    public String getKode() { return kode; }

    public Karyawan getKaryawan() { return karyawan; }

    public Presensi getPresensi() { return presensi; }

    /** Jam paling awal scan pulang diterima; hanya terisi untuk TERLALU_CEPAT. */
    public LocalTime getBolehPulang() { return bolehPulang; }

    /** True kalau scan ini menghasilkan catatan baru di database. */
    public boolean isDicatat() {
        return jenis == Jenis.MASUK || jenis == Jenis.PULANG;
    }
}
