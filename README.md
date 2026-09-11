# 🏢 Presensi Karyawan

Aplikasi desktop untuk **manajemen pegawai dan presensi karyawan** yang dikembangkan menggunakan Java dan MySQL.

Aplikasi ini dirancang dengan konsep **admin-based attendance system**, di mana admin mengelola data pegawai dan membantu proses presensi karyawan menggunakan **QR Code**.

> 🚧 **Status:** Dalam Pengembangan

<img width="882" height="421" alt="image" src="https://github.com/user-attachments/assets/3af53aa4-f69e-463f-a992-273613a43714" />


---

## 📌 Tentang Project

**Presensi Karyawan** merupakan aplikasi desktop yang ditujukan untuk membantu perusahaan dalam mengelola data pegawai dan mencatat kehadiran karyawan secara lebih terstruktur.

Konsep utama aplikasi:

```text
Admin
  │
  ├── Login
  │
  ├── Management Pegawai
  │
  ├── Generate QR Code
  │
  ├── Presensi Karyawan
  │
  └── Rekap Presensi
```

Karyawan tidak perlu melakukan login ke dalam aplikasi. Proses presensi dilakukan melalui admin dengan melakukan **scan QR Code milik karyawan**.

---

## ✨ Fitur

### 🔐 Authentication
- Login admin
- Autentikasi menggunakan database MySQL
- Password disimpan menggunakan **BCrypt hashing**
- Validasi status akun

### 👥 Management Pegawai
- Tambah data pegawai
- Edit data pegawai
- Hapus data pegawai
- Melihat data pegawai
- Generate QR Code untuk setiap pegawai

### 🕐 Presensi
- Scan QR Code pegawai
- Pencatatan waktu masuk
- Pencatatan waktu keluar
- Riwayat presensi

### 📊 Management & Reporting
- Rekap presensi
- Pencarian data presensi
- Laporan kehadiran

> Beberapa fitur masih dalam tahap pengembangan.

---

## 🛠️ Teknologi

| Teknologi | Penggunaan |
|---|---|
| Java | Bahasa pemrograman |
| Java Swing | Desktop GUI |
| Maven | Dependency & project management |
| MySQL | Database |
| JDBC | Koneksi Java dengan MySQL |
| BCrypt | Password hashing |
| XAMPP | Local development environment |
| VS Code | Code editor |

---

## 🗄️ Database

Database menggunakan **MySQL**.

Struktur database akan dikembangkan secara bertahap sesuai dengan fitur aplikasi.

Rancangan utama:

```text
db_presensi
│
├── users
│   ├── id_user
│   ├── username
│   ├── password
│   ├── nama
│   ├── role
│   └── status
│
├── pegawai
│
└── presensi
```

Password pengguna **tidak disimpan dalam bentuk plaintext**, melainkan menggunakan BCrypt hashing.

---

## 🔐 Authentication Flow

Proses login admin:

```text
Username + Password
        │
        ▼
      Java
        │
        ▼
      MySQL
        │
        ▼
  Cari Username
        │
        ▼
 Ambil Password Hash
        │
        ▼
   BCrypt Check
      /     \
   Cocok   Tidak
     │       │
     ▼       ▼
 Dashboard  Login Gagal
```

---

## 📂 Struktur Project

```text
presensiKaryawan/
│
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── mycompany/
│                   └── mavenproject1/
│                       ├── config/
│                       │   └── DatabaseConnection.java
│                       │
│                       ├── Login.java
│                       ├── Dasboard.java
│                       └── CreateAdmin.java
│
├── pom.xml
├── database/
│   └── db_presensi.sql
│
└── README.md
```

---

## 🚀 Development Progress

### Milestone 01 — Authentication

- [x] Membuat project Java Maven
- [x] Membuat form login
- [x] Membuat database MySQL
- [x] Membuat tabel `users`
- [x] Menghubungkan Java dengan MySQL
- [x] Implementasi BCrypt
- [x] Membuat akun admin
- [x] Login menggunakan database
- [x] Validasi username dan password
- [x] Validasi status akun

### Milestone 02 — Management Pegawai

- [ ] Membuat tabel `pegawai`
- [ ] Form data pegawai
- [ ] Tambah pegawai
- [ ] Edit pegawai
- [ ] Hapus pegawai
- [ ] Pencarian pegawai
- [ ] Generate QR Code

### Milestone 03 — Presensi

- [ ] Scan QR Code
- [ ] Presensi masuk
- [ ] Presensi keluar
- [ ] Validasi presensi
- [ ] Riwayat presensi

### Milestone 04 — Reporting

- [ ] Rekap presensi
- [ ] Filter berdasarkan tanggal
- [ ] Filter berdasarkan pegawai
- [ ] Laporan presensi
- [ ] Export laporan

---

## 💻 Cara Menjalankan Project

### 1. Clone Repository

```bash
git clone https://github.com/nizoyaa/presensiKaryawan.git
```

### 2. Jalankan MySQL

Pastikan **Apache** dan **MySQL** pada XAMPP sudah aktif.

### 3. Buat Database

Buat database:

```text
db_presensi
```

Kemudian import file SQL yang tersedia pada folder:

```text
database/db_presensi.sql
```

### 4. Konfigurasi Database

Sesuaikan konfigurasi database pada:

```text
DatabaseConnection.java
```

Contoh konfigurasi lokal:

```java
private static final String URL =
        "jdbc:mysql://localhost:3306/db_presensi";

private static final String USER = "root";

private static final String PASSWORD = "";
```

### 5. Jalankan Project

Buka project menggunakan VS Code dan jalankan project Maven.

---

## 🔒 Security Note

Project ini menggunakan **BCrypt** untuk menyimpan password dalam bentuk hash.

Password asli tidak disimpan langsung di database.

> Jangan menyimpan password database, API key, atau credential pribadi ke dalam repository publik.

---

## 🗺️ Roadmap

Pengembangan aplikasi dilakukan secara bertahap:

```text
Authentication
      ↓
Dashboard
      ↓
Management Pegawai
      ↓
QR Code
      ↓
Presensi
      ↓
Rekap Presensi
      ↓
Reporting
```

---

## 👨‍💻 Developer

**Nizoyaa**

GitHub: [@nizoyaa](https://github.com/nizoyaa)

---

## 📄 License

Project ini dibuat untuk keperluan pembelajaran dan pengembangan portfolio.
