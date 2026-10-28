package com.manajemen.data.siswa;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Satu baris presensi: seorang karyawan pada satu tanggal.
 * Kolom kode, nama, jabatan, dan departemen hanya terisi untuk keperluan tampilan (hasil join).
 */
public class Presensi {

    private int id;
    private int karyawanId;
    private LocalDate tanggal;
    private LocalTime jamMasuk;
    private LocalTime jamPulang;

    private String kode;
    private String nama;
    private String jabatan;
    private String departemen;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getKaryawanId() { return karyawanId; }
    public void setKaryawanId(int karyawanId) { this.karyawanId = karyawanId; }

    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }

    public LocalTime getJamMasuk() { return jamMasuk; }
    public void setJamMasuk(LocalTime jamMasuk) { this.jamMasuk = jamMasuk; }

    public LocalTime getJamPulang() { return jamPulang; }
    public void setJamPulang(LocalTime jamPulang) { this.jamPulang = jamPulang; }

    public String getKode() { return kode; }
    public void setKode(String kode) { this.kode = kode; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getJabatan() { return jabatan; }
    public void setJabatan(String jabatan) { this.jabatan = jabatan; }

    public String getDepartemen() { return departemen; }
    public void setDepartemen(String departemen) { this.departemen = departemen; }

    public boolean isSudahPulang() {
        return jamPulang != null;
    }

    /** Lama bekerja, atau null kalau belum pulang. */
    public Duration getDurasi() {
        return (jamMasuk == null || jamPulang == null) ? null : Duration.between(jamMasuk, jamPulang);
    }

    /** Contoh hasil: "8 jam 15 menit", "45 menit", "2 jam". */
    public static String formatDurasi(Duration durasi) {
        if (durasi == null || durasi.isNegative()) {
            return "-";
        }
        long jam = durasi.toHours();
        long menit = durasi.toMinutes() % 60;
        if (jam == 0) {
            return menit + " menit";
        }
        return menit == 0 ? jam + " jam" : jam + " jam " + menit + " menit";
    }
}
