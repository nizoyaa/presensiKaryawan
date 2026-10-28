package com.manajemen.data.siswa;

import java.time.LocalDate;

/**
 * Model data satu karyawan (satu baris pada tabel `karyawan`).
 */
public class Karyawan {

    public static final String STATUS_AKTIF = "Aktif";
    public static final String STATUS_NONAKTIF = "Nonaktif";

    private int id;
    private String kode;
    private String nama;
    private String jenisKelamin;
    private String jabatan;
    private String departemen;
    private String noHp;
    private String email;
    private String alamat;
    private LocalDate tanggalMasuk;
    private String status = STATUS_AKTIF;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKode() { return kode; }
    public void setKode(String kode) { this.kode = kode; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getJenisKelamin() { return jenisKelamin; }
    public void setJenisKelamin(String jenisKelamin) { this.jenisKelamin = jenisKelamin; }

    public String getJabatan() { return jabatan; }
    public void setJabatan(String jabatan) { this.jabatan = jabatan; }

    public String getDepartemen() { return departemen; }
    public void setDepartemen(String departemen) { this.departemen = departemen; }

    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public LocalDate getTanggalMasuk() { return tanggalMasuk; }
    public void setTanggalMasuk(LocalDate tanggalMasuk) { this.tanggalMasuk = tanggalMasuk; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isAktif() { return STATUS_AKTIF.equals(status); }
}
