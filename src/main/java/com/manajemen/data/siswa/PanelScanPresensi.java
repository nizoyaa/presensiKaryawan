package com.manajemen.data.siswa;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

/**
 * Layar utama untuk admin: scan kartu karyawan untuk mencatat presensi masuk dan pulang,
 * lengkap dengan hasil scan terakhir, ringkasan hari ini, dan daftar siapa yang belum hadir.
 *
 * Scanner USB bekerja seperti keyboard, jadi yang diperlukan hanya satu kolom teks yang selalu
 * aktif (fokus). Panel ini menjaga fokus itu supaya scan tidak "nyasar".
 */
public class PanelScanPresensi extends JPanel {

    private static final Locale INDONESIA = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter FORMAT_TANGGAL = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", INDONESIA);
    private static final DateTimeFormatter FORMAT_JAM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMAT_JAM_DETIK = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String PEMISAH = "  \u2022  ";
    private static final int MAKS_KODE_TAMPIL = 30;
    private static final int KOLOM_DURASI = 6;
    private static final int KOLOM_STATUS = 7;

    private final PresensiDAO dao = new PresensiDAO();

    // ----- jam & tanggal -----
    private final JLabel lblJam = new JLabel(" ", SwingConstants.RIGHT);
    private final JLabel lblTanggal = new JLabel(" ", SwingConstants.RIGHT);
    private final Timer timerJam = new Timer(1000, e -> tikJam());
    private LocalDate tanggalAktif = LocalDate.now();

    // ----- input scan -----
    private final Tema.FieldHint tfScan = new Tema.FieldHint("Scan kartu atau ketik kode...");
    private final Tema.Tombol btnCatat = new Tema.Tombol("Catat", Tema.UTAMA, true);
    private final Tema.Tombol btnRefresh = new Tema.Tombol("Muat Ulang", Tema.TEKS_PUDAR, false);
    private final JLabel lblSiaga = new JLabel(" ");

    // ----- hasil scan terakhir -----
    private final JPanel kartuHasil = new JPanel(new BorderLayout(0, 8));
    private final JLabel lblJenis = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblWaktuScan = new JLabel(" ", SwingConstants.RIGHT);
    private final JLabel lblNama = new JLabel(" ");
    private final JLabel lblDetail = new JLabel(" ");
    private final JLabel lblPesan = new JLabel(" ");
    private final Timer timerIdle = new Timer(12000, e -> tampilkanIdle());

    // ----- statistik -----
    private final JLabel lblAktif = new JLabel("-");
    private final JLabel lblHadir = new JLabel("-");
    private final JLabel lblDiKantor = new JLabel("-");
    private final JLabel lblPulang = new JLabel("-");
    private final JLabel lblBelumHadir = new JLabel("-");

    // ----- daftar -----
    private final KartuTabel daftarPresensi = new KartuTabel("Aktivitas Presensi Hari Ini",
        new String[] {"Kode", "Nama", "Jabatan", "Departemen", "Masuk", "Pulang", "Durasi", "Status"},
        new int[] {80, 170, 130, 110, 64, 64, 115, 150});
    private final KartuTabel daftarBelumHadir = new KartuTabel("Belum Hadir",
        new String[] {"Kode", "Nama", "Departemen"},
        new int[] {90, 200, 130});

    private final JLabel lblInfo = new JLabel(" ");
    private int nomorPermintaan = 0;

    public PanelScanPresensi() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Tema.LATAR);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        timerIdle.setRepeats(false);
        btnCatat.setFocusable(false);   // klik tombol tidak boleh memindahkan fokus dari kolom scan
        btnRefresh.setFocusable(false);

        JPanel tengah = new JPanel(new BorderLayout(0, 16));
        tengah.setOpaque(false);
        tengah.add(buatKartuStatistik(), BorderLayout.NORTH);
        tengah.add(buatKartuScan(), BorderLayout.CENTER);

        JPanel atas = new JPanel(new BorderLayout(0, 16));
        atas.setOpaque(false);
        atas.add(buatHeader(), BorderLayout.NORTH);
        atas.add(tengah, BorderLayout.CENTER);

        add(atas, BorderLayout.NORTH);
        add(buatPapanDaftar(), BorderLayout.CENTER);
        add(buatFooter(), BorderLayout.SOUTH);

        siapkanDaftar();
        pasangAksi();
        tampilkanIdle();
        perbaruiSiaga();
        tikJam();
        timerJam.start();
        muatData();
    }

    // =====================================================
    // MEMBANGUN TAMPILAN
    // =====================================================

    private JPanel buatHeader() {
        JLabel judul = new JLabel("Dashboard Presensi");
        judul.setFont(Tema.font(Font.BOLD, 26));
        judul.setForeground(Tema.TEKS);

        JLabel sub = new JLabel("Scan kartu karyawan di sini untuk mencatat presensi masuk dan pulang.");
        sub.setFont(Tema.font(Font.PLAIN, 14));
        sub.setForeground(Tema.TEKS_PUDAR);

        JPanel kiri = new JPanel(new GridLayout(2, 1, 0, 2));
        kiri.setOpaque(false);
        kiri.add(judul);
        kiri.add(sub);

        lblJam.setFont(Tema.font(Font.BOLD, 30));
        lblJam.setForeground(Tema.TEKS);
        lblTanggal.setFont(Tema.font(Font.PLAIN, 14));
        lblTanggal.setForeground(Tema.TEKS_PUDAR);

        JPanel jam = new JPanel(new GridLayout(2, 1, 0, 0));
        jam.setOpaque(false);
        jam.add(lblJam);
        jam.add(lblTanggal);

        JPanel kananIsi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        kananIsi.setOpaque(false);
        kananIsi.add(btnRefresh);
        kananIsi.add(jam);

        JPanel kanan = new JPanel(new GridBagLayout());
        kanan.setOpaque(false);
        kanan.add(kananIsi);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(kiri, BorderLayout.WEST);
        p.add(kanan, BorderLayout.EAST);
        return p;
    }

    private JPanel buatKartuStatistik() {
        JPanel p = new JPanel(new GridLayout(1, 5, 16, 0));
        p.setOpaque(false);
        p.add(Tema.kartuStatistik("Karyawan Aktif", lblAktif, Tema.TEKS));
        p.add(Tema.kartuStatistik("Sudah Hadir", lblHadir, Tema.SUKSES));
        p.add(Tema.kartuStatistik("Masih di Kantor", lblDiKantor, Tema.UTAMA));
        p.add(Tema.kartuStatistik("Sudah Pulang", lblPulang, Tema.TEKS_PUDAR));
        p.add(Tema.kartuStatistik("Belum Hadir", lblBelumHadir, Tema.PERINGATAN));
        return p;
    }

    private JPanel buatKartuScan() {
        siapkanKartuHasil();

        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setOpaque(false);
        p.add(buatInputScan(), BorderLayout.WEST);
        p.add(kartuHasil, BorderLayout.CENTER);
        return p;
    }

    private JPanel buatInputScan() {
        JLabel judul = new JLabel("SCAN KARTU KARYAWAN");
        judul.setFont(Tema.font(Font.BOLD, 12));
        judul.setForeground(Tema.TEKS_PUDAR);

        tfScan.setFont(Tema.font(Font.PLAIN, 22));
        tfScan.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Tema.UTAMA, 2),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        tfScan.setPreferredSize(new Dimension(100, 54));

        JPanel baris = new JPanel(new BorderLayout(10, 0));
        baris.setOpaque(false);
        baris.add(tfScan, BorderLayout.CENTER);
        baris.add(btnCatat, BorderLayout.EAST);

        lblSiaga.setFont(Tema.font(Font.BOLD, 13));
        lblSiaga.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        JLabel petunjuk = new JLabel("Scanner USB bekerja seperti keyboard: arahkan saja ke kartu.");
        petunjuk.setFont(Tema.font(Font.PLAIN, 12));
        petunjuk.setForeground(Tema.TEKS_PUDAR);

        JPanel kartu = new JPanel(new GridBagLayout());
        kartu.setBackground(Tema.KARTU);
        kartu.setBorder(Tema.borderKartu(16, 18));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 8, 0);
        kartu.add(judul, c);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 10, 0);
        kartu.add(baris, c);
        c.gridy = 2;
        c.insets = new Insets(0, 0, 6, 0);
        kartu.add(lblSiaga, c);
        c.gridy = 3;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 0);
        kartu.add(petunjuk, c);

        // lebar dikunci, tinggi mengikuti kartu hasil di sebelahnya
        kartu.setPreferredSize(new Dimension(520, kartu.getPreferredSize().height));
        return kartu;
    }

    private void siapkanKartuHasil() {
        lblJenis.setOpaque(true);
        lblJenis.setForeground(Color.WHITE);
        lblJenis.setFont(Tema.font(Font.BOLD, 12));
        lblJenis.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

        lblWaktuScan.setFont(Tema.font(Font.BOLD, 16));
        lblWaktuScan.setForeground(Tema.TEKS_PUDAR);
        lblNama.setFont(Tema.font(Font.BOLD, 26));
        lblNama.setForeground(Tema.TEKS);
        lblDetail.setFont(Tema.font(Font.PLAIN, 14));
        lblDetail.setForeground(Tema.TEKS_PUDAR);
        lblPesan.setFont(Tema.font(Font.PLAIN, 15));
        lblPesan.setForeground(Tema.TEKS);

        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chip.setOpaque(false);
        chip.add(lblJenis);

        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(chip, BorderLayout.WEST);
        atas.add(lblWaktuScan, BorderLayout.EAST);

        JPanel isi = new JPanel(new GridLayout(3, 1, 0, 2));
        isi.setOpaque(false);
        isi.add(lblNama);
        isi.add(lblDetail);
        isi.add(lblPesan);

        kartuHasil.add(atas, BorderLayout.NORTH);
        kartuHasil.add(isi, BorderLayout.CENTER);
    }

    private JPanel buatPapanDaftar() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;

        c.gridx = 0;
        c.weightx = 0.66;
        c.insets = new Insets(0, 0, 0, 8);
        p.add(daftarPresensi, c);

        c.gridx = 1;
        c.weightx = 0.34;
        c.insets = new Insets(0, 8, 0, 0);
        p.add(daftarBelumHadir, c);
        return p;
    }

    private JPanel buatFooter() {
        lblInfo.setFont(Tema.font(Font.PLAIN, 13));
        lblInfo.setForeground(Tema.TEKS_PUDAR);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(lblInfo, BorderLayout.WEST);
        return p;
    }

    private void siapkanDaftar() {
        daftarPresensi.tabel.getColumnModel().getColumn(KOLOM_DURASI).setCellRenderer(new Tema.RendererTabel() {
            @Override
            protected void ubahTampilan(Object nilai, int baris) {
                setForeground(Tema.TEKS_PUDAR);
            }
        });
        daftarPresensi.tabel.getColumnModel().getColumn(KOLOM_STATUS).setCellRenderer(new Tema.RendererTabel() {
            @Override
            protected void ubahTampilan(Object nilai, int baris) {
                boolean selesai = "Sudah pulang".equals(nilai);
                setText("\u25CF  " + nilai);
                setForeground(selesai ? Tema.SUKSES : Tema.UTAMA);
                setFont(Tema.font(Font.BOLD, 13));
            }
        });
    }

    // =====================================================
    // AKSI & FOKUS
    // =====================================================

    private void pasangAksi() {
        tfScan.addActionListener(e -> prosesScan());
        btnCatat.addActionListener(e -> prosesScan());
        btnRefresh.addActionListener(e -> {
            tanggalAktif = LocalDate.now();
            muatData();
            tfScan.requestFocusInWindow();
        });

        tfScan.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                perbaruiSiaga();
            }

            @Override
            public void focusLost(FocusEvent e) {
                perbaruiSiaga();
            }
        });

        MouseAdapter rebutFokus = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                tfScan.requestFocusInWindow();
            }
        };
        addMouseListener(rebutFokus);
        lblSiaga.addMouseListener(rebutFokus);
        kartuHasil.addMouseListener(rebutFokus);

        // Setiap kali halaman ini ditampilkan (misalnya kembali dari menu lain): segarkan data dan aktifkan scanner.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                tanggalAktif = LocalDate.now();
                muatData();
                SwingUtilities.invokeLater(() -> tfScan.requestFocusInWindow());
            }
        });
    }

    private void tikJam() {
        LocalDateTime kini = LocalDateTime.now();
        lblJam.setText(kini.format(FORMAT_JAM_DETIK));
        lblTanggal.setText(kini.format(FORMAT_TANGGAL));

        if (!kini.toLocalDate().equals(tanggalAktif)) {
            tanggalAktif = kini.toLocalDate(); // lewat tengah malam: mulai hari baru
            muatData();
        }
        pastikanFokus();
    }

    /**
     * Mengembalikan fokus ke kolom scan, tapi hanya kalau halaman ini sedang tampil dan jendela aktif.
     * Dengan begitu dialog atau menu lain tidak terganggu.
     */
    private void pastikanFokus() {
        if (!isShowing() || tfScan.hasFocus()) {
            return;
        }
        Window jendela = SwingUtilities.getWindowAncestor(this);
        if (jendela != null && jendela.isActive()) {
            tfScan.requestFocusInWindow();
        }
    }

    private void perbaruiSiaga() {
        boolean siap = tfScan.hasFocus();
        lblSiaga.setText(siap ? "\u25CF  Scanner siap digunakan" : "\u25CF  Klik di sini agar scanner aktif");
        lblSiaga.setForeground(siap ? Tema.SUKSES : Tema.PERINGATAN);
    }

    // =====================================================
    // PROSES SCAN
    // =====================================================

    private void prosesScan() {
        final String kode = bersihkan(tfScan.getText());
        tfScan.setText("");
        if (kode.isEmpty()) {
            return; // beberapa scanner mengirim Enter tambahan
        }
        tfScan.requestFocusInWindow();

        final LocalDateTime waktu = LocalDateTime.now(); // jam saat kartu di-scan
        new SwingWorker<HasilScan, Void>() {
            @Override
            protected HasilScan doInBackground() throws Exception {
                return dao.catatScan(kode, waktu);
            }

            @Override
            protected void done() {
                try {
                    HasilScan hasil = get();
                    tampilkanHasil(hasil, waktu);
                    if (hasil.isDicatat()) {
                        muatData();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    tampilkanGagal(kode, ex.getCause());
                }
            }
        }.execute();
    }

    /** Membuang karakter kontrol yang kadang ikut terkirim scanner, lalu merapikan spasi. */
    private static String bersihkan(String teks) {
        return teks == null ? "" : teks.replaceAll("\\p{Cntrl}", "").trim();
    }

    // =====================================================
    // SPANDUK HASIL
    // =====================================================

    private void tampilkanHasil(HasilScan h, LocalDateTime waktu) {
        Karyawan k = h.getKaryawan();
        Presensi p = h.getPresensi();
        LocalTime w = waktu.toLocalTime();

        switch (h.getJenis()) {
            case MASUK:
                tampilkanBanner(Tema.SUKSES, "PRESENSI MASUK", k.getNama(), detail(k),
                    "Masuk tercatat pukul " + jam(p.getJamMasuk()) + ". Selamat bekerja!", w);
                break;
            case PULANG:
                tampilkanBanner(Tema.UTAMA, "PRESENSI PULANG", k.getNama(), detail(k),
                    "Pulang tercatat pukul " + jam(p.getJamPulang()) + ". Lama kerja "
                        + Presensi.formatDurasi(p.getDurasi()) + ". Hati-hati di jalan!", w);
                break;
            case SUDAH_LENGKAP:
                tampilkanBanner(Tema.TEKS_PUDAR, "SUDAH LENGKAP", k.getNama(), detail(k),
                    "Presensi hari ini sudah lengkap: masuk " + jam(p.getJamMasuk())
                        + ", pulang " + jam(p.getJamPulang()) + ".", w);
                break;
            case TERLALU_CEPAT:
                tampilkanBanner(Tema.PERINGATAN, "SUDAH MASUK", k.getNama(), detail(k),
                    "Masuk sudah tercatat pukul " + jam(p.getJamMasuk())
                        + ". Scan pulang baru bisa setelah pukul " + jam(h.getBolehPulang()) + ".", w);
                break;
            case NONAKTIF:
                tampilkanBanner(Tema.BAHAYA, "NONAKTIF", k.getNama(), detail(k),
                    "Karyawan berstatus nonaktif, presensi tidak dicatat.", w);
                break;
            default:
                tampilkanBanner(Tema.BAHAYA, "TIDAK DIKENAL", "Kode tidak dikenal",
                    "Kode dipindai: " + potong(h.getKode()),
                    "Kode ini tidak terdaftar di Data Karyawan.", w);
                break;
        }

        if (!h.isDicatat()) {
            Toolkit.getDefaultToolkit().beep();
        }
        timerIdle.restart();
    }

    /** Gagal menyimpan ke database: harus sangat jelas karena presensinya tidak tercatat. */
    private void tampilkanGagal(String kode, Throwable ex) {
        tampilkanBanner(Tema.BAHAYA, "GAGAL MENYIMPAN", "Presensi TIDAK tercatat",
            "Kode dipindai: " + potong(kode),
            "Scan ulang kartunya. " + Galat.ringkas(ex), LocalTime.now());
        Toolkit.getDefaultToolkit().beep();
        timerIdle.stop(); // biarkan tetap tampil sampai ada scan berikutnya
    }

    private void tampilkanIdle() {
        tampilkanBanner(Tema.TEKS_PUDAR, "SIAP", "Menunggu scan...",
            "Arahkan kartu karyawan ke scanner.", " ", null);
    }

    private void tampilkanBanner(Color aksen, String jenis, String nama, String detail, String pesan, LocalTime waktu) {
        kartuHasil.setBackground(Tema.campur(aksen, 0.08f));
        kartuHasil.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Tema.campur(aksen, 0.35f)),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 8, 0, 0, aksen),
                BorderFactory.createEmptyBorder(14, 18, 14, 18))));
        lblJenis.setBackground(aksen);
        lblJenis.setText(jenis);
        lblWaktuScan.setText(waktu == null ? " " : waktu.format(FORMAT_JAM_DETIK));
        lblNama.setText(nama);
        lblDetail.setText(detail);
        lblPesan.setText(pesan);
    }

    private static String detail(Karyawan k) {
        return k.getKode() + PEMISAH + k.getJabatan() + PEMISAH + k.getDepartemen();
    }

    private static String jam(LocalTime waktu) {
        return waktu == null ? "-" : waktu.format(FORMAT_JAM);
    }

    private static String potong(String kode) {
        if (kode == null) {
            return "";
        }
        return kode.length() > MAKS_KODE_TAMPIL ? kode.substring(0, MAKS_KODE_TAMPIL) + "..." : kode;
    }

    // =====================================================
    // MEMUAT DATA
    // =====================================================

    private void muatData() {
        final LocalDate tanggal = tanggalAktif;
        final int nomor = ++nomorPermintaan;

        new SwingWorker<PresensiDAO.DataHari, Void>() {
            @Override
            protected PresensiDAO.DataHari doInBackground() throws Exception {
                return dao.muatHari(tanggal);
            }

            @Override
            protected void done() {
                if (nomor != nomorPermintaan) {
                    return; // sudah ada permintaan yang lebih baru
                }
                try {
                    terapkanData(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    lblInfo.setForeground(Tema.BAHAYA);
                    lblInfo.setText("Gagal memuat data: " + Galat.ringkas(ex.getCause()));
                }
            }
        }.execute();
    }

    private void terapkanData(PresensiDAO.DataHari d) {
        PresensiDAO.RingkasanHari r = d.ringkasan;
        lblAktif.setText(String.valueOf(r.aktif));
        lblHadir.setText(String.valueOf(r.hadir));
        lblDiKantor.setText(String.valueOf(r.masihDiKantor()));
        lblPulang.setText(String.valueOf(r.pulang));
        lblBelumHadir.setText(String.valueOf(r.belumHadir()));

        List<Object[]> barisPresensi = new ArrayList<>();
        for (Presensi p : d.presensi) {
            boolean selesai = p.isSudahPulang();
            barisPresensi.add(new Object[] {
                p.getKode(), p.getNama(), p.getJabatan(), p.getDepartemen(),
                jam(p.getJamMasuk()), jam(p.getJamPulang()),
                selesai ? Presensi.formatDurasi(p.getDurasi()) : "-",
                selesai ? "Sudah pulang" : "Di kantor"
            });
        }
        daftarPresensi.isi(barisPresensi, "Belum ada karyawan yang presensi hari ini.");

        List<Object[]> barisBelum = new ArrayList<>();
        for (Karyawan k : d.belumHadir) {
            barisBelum.add(new Object[] {k.getKode(), k.getNama(), k.getDepartemen()});
        }
        daftarBelumHadir.isi(barisBelum, r.aktif == 0
            ? "Belum ada karyawan aktif. Tambahkan lewat menu Data Karyawan."
            : "Semua karyawan aktif sudah hadir.");

        lblInfo.setForeground(Tema.TEKS_PUDAR);
        lblInfo.setText("Data tanggal " + d.tanggal.format(DateTimeFormatter.ofPattern("d MMMM yyyy", INDONESIA))
            + ", diperbarui pukul " + LocalTime.now().format(FORMAT_JAM_DETIK) + ".");
    }

    // =====================================================
    // KARTU DAFTAR
    // =====================================================

    /** Kartu berisi judul, jumlah baris, dan tabel baca-saja (atau pesan kosong). */
    private static class KartuTabel extends JPanel {

        final DefaultTableModel model;
        final JTable tabel;
        private final JLabel lblJumlah = new JLabel("0", SwingConstants.CENTER);
        private final JLabel lblKosong = new JLabel("", SwingConstants.CENTER);
        private final CardLayout susunan = new CardLayout();
        private final JPanel area = new JPanel(susunan);

        KartuTabel(String judul, String[] kolom, int[] lebar) {
            super(new BorderLayout());

            model = new DefaultTableModel(kolom, 0) {
                @Override
                public boolean isCellEditable(int baris, int kolom) {
                    return false;
                }
            };
            tabel = new JTable(model);
            Tema.gayaiTabel(tabel);
            tabel.setRowSelectionAllowed(false);
            tabel.setFocusable(false);          // jangan merebut fokus dari kolom scan
            tabel.setPreferredScrollableViewportSize(new Dimension(300, 160));
            for (int i = 0; i < lebar.length; i++) {
                tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
            }

            JLabel lblJudul = new JLabel(judul);
            lblJudul.setFont(Tema.font(Font.BOLD, 15));
            lblJudul.setForeground(Tema.TEKS);

            lblJumlah.setOpaque(true);
            lblJumlah.setBackground(Tema.campur(Tema.UTAMA, 0.15f));
            lblJumlah.setForeground(Tema.UTAMA);
            lblJumlah.setFont(Tema.font(Font.BOLD, 13));
            lblJumlah.setBorder(BorderFactory.createEmptyBorder(3, 11, 3, 11));

            JPanel penengah = new JPanel(new GridBagLayout());
            penengah.setOpaque(false);
            penengah.add(lblJumlah);

            JPanel kepala = new JPanel(new BorderLayout());
            kepala.setBackground(Tema.KARTU);
            kepala.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.GARIS),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
            kepala.add(lblJudul, BorderLayout.WEST);
            kepala.add(penengah, BorderLayout.EAST);

            JScrollPane gulir = new JScrollPane(tabel);
            gulir.setBorder(BorderFactory.createEmptyBorder());
            gulir.getViewport().setBackground(Color.WHITE);

            lblKosong.setFont(Tema.font(Font.PLAIN, 15));
            lblKosong.setForeground(Tema.TEKS_PUDAR);

            area.setBackground(Color.WHITE);
            area.add(gulir, "tabel");
            area.add(lblKosong, "kosong");
            susunan.show(area, "kosong");

            setBackground(Tema.KARTU);
            setBorder(BorderFactory.createLineBorder(Tema.GARIS));
            add(kepala, BorderLayout.NORTH);
            add(area, BorderLayout.CENTER);
        }

        void isi(List<Object[]> baris, String pesanKosong) {
            model.setRowCount(0);
            for (Object[] b : baris) {
                model.addRow(b);
            }
            lblJumlah.setText(String.valueOf(baris.size()));
            lblKosong.setText(pesanKosong);
            susunan.show(area, baris.isEmpty() ? "kosong" : "tabel");
        }
    }
}
