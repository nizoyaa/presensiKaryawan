package com.manajemen.data.siswa;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

/**
 * Halaman menu "Data Karyawan": tabel, pencarian, filter, tambah/ubah/hapus, dan ekspor CSV.
 * Data diambil dari database lewat {@link KaryawanDAO} di thread terpisah supaya jendela tidak macet.
 */
public class PanelDataKaryawan extends JPanel {

    private static final String[] KOLOM = {
        "No", "Kode", "Nama", "Jenis Kelamin", "Jabatan", "Departemen", "No. HP", "Tanggal Masuk", "Status"
    };
    private static final int KOLOM_TANGGAL = 7;
    private static final int KOLOM_STATUS = 8;
    private static final String SEMUA = "Semua";
    private static final Color BARIS_GANJIL = new Color(248, 250, 252);
    private static final DateTimeFormatter FORMAT_TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final KaryawanDAO dao = new KaryawanDAO();

    // ----- tabel -----
    private final DefaultTableModel modelTabel = new DefaultTableModel(KOLOM, 0) {
        @Override
        public boolean isCellEditable(int baris, int kolom) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int kolom) {
            return kolom == KOLOM_TANGGAL ? LocalDate.class : Object.class;
        }
    };
    private final JTable tabel = new JTable(modelTabel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelTabel);
    private final JPanel areaTabel = new JPanel(new CardLayout());
    private final JLabel lblKosong = new JLabel("", SwingConstants.CENTER);

    // ----- pencarian & filter -----
    private final Tema.FieldHint tfCari = new Tema.FieldHint("Cari nama, kode, jabatan, atau departemen...");
    private final JComboBox<String> cbStatus = new JComboBox<>(new String[] {SEMUA, Karyawan.STATUS_AKTIF, Karyawan.STATUS_NONAKTIF});
    private final JComboBox<String> cbDepartemen = new JComboBox<>(new String[] {SEMUA});
    private final Timer timerCari = new Timer(300, e -> muatData());

    // ----- tombol -----
    private final Tema.Tombol btnTambah = new Tema.Tombol("+  Tambah Karyawan", Tema.UTAMA, true);
    private final Tema.Tombol btnEkspor = new Tema.Tombol("Ekspor CSV", Tema.UTAMA, false);
    private final Tema.Tombol btnRefresh = new Tema.Tombol("Muat Ulang", Tema.TEKS_PUDAR, false);
    private final Tema.Tombol btnReset = new Tema.Tombol("Reset Filter", Tema.TEKS_PUDAR, false);
    private final Tema.Tombol btnDetail = new Tema.Tombol("Detail", Tema.TEKS_PUDAR, false);
    private final Tema.Tombol btnUbah = new Tema.Tombol("Ubah", Tema.UTAMA, false);
    private final Tema.Tombol btnStatus = new Tema.Tombol("Nonaktifkan", Tema.PERINGATAN, false);
    private final Tema.Tombol btnHapus = new Tema.Tombol("Hapus", Tema.BAHAYA, false);

    // ----- statistik & info -----
    private final JLabel lblTotal = new JLabel("-");
    private final JLabel lblAktif = new JLabel("-");
    private final JLabel lblNonaktif = new JLabel("-");
    private final JLabel lblInfo = new JLabel(" ");
    private final JLabel lblPesan = new JLabel(" ");
    private final Timer timerPesan = new Timer(4500, e -> lblPesan.setText(" "));

    // ----- menu klik kanan -----
    private final JMenuItem menuDetail = new JMenuItem("Lihat detail");
    private final JMenuItem menuUbah = new JMenuItem("Ubah data");
    private final JMenuItem menuStatus = new JMenuItem("Nonaktifkan");
    private final JMenuItem menuHapus = new JMenuItem("Hapus");

    // ----- state -----
    private List<Karyawan> data = new ArrayList<>();
    private List<String> daftarDepartemen = new ArrayList<>();
    private int totalSemua = -1;
    private int nomorPermintaan = 0;
    private boolean mengisiFilter = false;
    private boolean galatMuatSudahTampil = false;

    public PanelDataKaryawan() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Tema.LATAR);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        timerCari.setRepeats(false);
        timerPesan.setRepeats(false);

        siapkanTabel();

        JPanel atas = new JPanel(new BorderLayout(0, 16));
        atas.setOpaque(false);
        atas.add(buatHeader(), BorderLayout.NORTH);
        atas.add(buatKartuStatistik(), BorderLayout.CENTER);
        atas.add(buatToolbar(), BorderLayout.SOUTH);

        add(atas, BorderLayout.NORTH);
        add(buatAreaTabel(), BorderLayout.CENTER);
        add(buatFooter(), BorderLayout.SOUTH);

        pasangAksi();
        perbaruiTombolAksi();
        muatSemua();
    }

    // =====================================================
    // MEMBANGUN TAMPILAN
    // =====================================================

    private JPanel buatHeader() {
        JLabel judul = new JLabel("Data Karyawan");
        judul.setFont(Tema.font(Font.BOLD, 26));
        judul.setForeground(Tema.TEKS);

        JLabel sub = new JLabel("Kelola karyawan yang melakukan presensi. Kode karyawan dipakai saat scan di meja admin.");
        sub.setFont(Tema.font(Font.PLAIN, 14));
        sub.setForeground(Tema.TEKS_PUDAR);

        JPanel kiri = new JPanel(new GridLayout(2, 1, 0, 2));
        kiri.setOpaque(false);
        kiri.add(judul);
        kiri.add(sub);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(btnRefresh);
        tombol.add(btnEkspor);
        tombol.add(btnTambah);

        JPanel kanan = new JPanel(new GridBagLayout());
        kanan.setOpaque(false);
        kanan.add(tombol);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(kiri, BorderLayout.WEST);
        p.add(kanan, BorderLayout.EAST);
        return p;
    }

    private JPanel buatKartuStatistik() {
        JPanel p = new JPanel(new GridLayout(1, 3, 16, 0));
        p.setOpaque(false);
        p.add(buatKartu("Total Karyawan", lblTotal, Tema.UTAMA));
        p.add(buatKartu("Karyawan Aktif", lblAktif, Tema.SUKSES));
        p.add(buatKartu("Karyawan Nonaktif", lblNonaktif, Tema.TEKS_PUDAR));
        return p;
    }

    private JPanel buatKartu(String judul, JLabel nilai, Color aksen) {
        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(Tema.font(Font.PLAIN, 13));
        lblJudul.setForeground(Tema.TEKS_PUDAR);

        nilai.setFont(Tema.font(Font.BOLD, 28));
        nilai.setForeground(aksen);

        JPanel kartu = new JPanel(new BorderLayout(0, 2));
        kartu.setBackground(Tema.KARTU);
        kartu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Tema.GARIS),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, aksen),
                BorderFactory.createEmptyBorder(12, 16, 12, 16))));
        kartu.add(lblJudul, BorderLayout.NORTH);
        kartu.add(nilai, BorderLayout.CENTER);
        return kartu;
    }

    private JPanel buatToolbar() {
        tfCari.setPreferredSize(new Dimension(400, 40));

        for (JComboBox<String> cb : Arrays.asList(cbStatus, cbDepartemen)) {
            cb.setFont(Tema.font(Font.PLAIN, 14));
            cb.setPreferredSize(new Dimension(150, 38));
        }

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(Tema.KARTU);
        p.setBorder(Tema.borderKartu(8, 10));
        p.add(tfCari);
        p.add(labelFilter("Status"));
        p.add(cbStatus);
        p.add(labelFilter("Departemen"));
        p.add(cbDepartemen);
        p.add(btnReset);
        return p;
    }

    private JLabel labelFilter(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(Tema.font(Font.BOLD, 12));
        l.setForeground(Tema.TEKS_PUDAR);
        return l;
    }

    private JPanel buatAreaTabel() {
        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        lblKosong.setFont(Tema.font(Font.PLAIN, 15));
        lblKosong.setForeground(Tema.TEKS_PUDAR);

        areaTabel.setBackground(Color.WHITE);
        areaTabel.add(scroll, "tabel");
        areaTabel.add(lblKosong, "kosong");

        JLabel petunjuk = new JLabel("Klik dua kali pada baris untuk mengubah data, klik kanan untuk menu cepat.");
        petunjuk.setFont(Tema.font(Font.PLAIN, 12));
        petunjuk.setForeground(Tema.TEKS_PUDAR);

        JPanel aksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        aksi.setOpaque(false);
        aksi.add(btnDetail);
        aksi.add(btnUbah);
        aksi.add(btnStatus);
        aksi.add(btnHapus);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Tema.KARTU);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.GARIS),
            BorderFactory.createEmptyBorder(10, 14, 10, 6)));
        bar.add(petunjuk, BorderLayout.WEST);
        bar.add(aksi, BorderLayout.EAST);

        JPanel kartu = new JPanel(new BorderLayout());
        kartu.setBackground(Tema.KARTU);
        kartu.setBorder(BorderFactory.createLineBorder(Tema.GARIS));
        kartu.add(areaTabel, BorderLayout.CENTER);
        kartu.add(bar, BorderLayout.SOUTH);
        return kartu;
    }

    private JPanel buatFooter() {
        lblInfo.setFont(Tema.font(Font.PLAIN, 13));
        lblInfo.setForeground(Tema.TEKS_PUDAR);
        lblPesan.setFont(Tema.font(Font.BOLD, 13));
        lblPesan.setForeground(Tema.SUKSES);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(lblInfo, BorderLayout.WEST);
        p.add(lblPesan, BorderLayout.EAST);
        return p;
    }

    private void siapkanTabel() {
        tabel.setRowHeight(40);
        tabel.setShowGrid(false);
        tabel.setIntercellSpacing(new Dimension(0, 0));
        tabel.setFillsViewportHeight(true);
        tabel.setFont(Tema.font(Font.PLAIN, 14));
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.setSelectionBackground(new Color(219, 234, 254));
        tabel.setSelectionForeground(Tema.TEKS);
        tabel.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        tabel.setRowSorter(sorter);
        sorter.setSortable(0, false);
        sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(2, SortOrder.ASCENDING)));

        int[] lebar = {50, 90, 220, 110, 150, 140, 130, 120, 100};
        for (int i = 0; i < lebar.length; i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }

        // isi sel
        tabel.setDefaultRenderer(Object.class, new RendererDasar());
        tabel.setDefaultRenderer(LocalDate.class, new RendererDasar() {
            @Override
            protected void ubahTampilan(Object nilai, int baris) {
                setText(nilai instanceof LocalDate ? ((LocalDate) nilai).format(FORMAT_TANGGAL) : "-");
            }
        });
        tabel.getColumnModel().getColumn(0).setCellRenderer(new RendererDasar() {
            @Override
            protected void ubahTampilan(Object nilai, int baris) {
                setText(String.valueOf(baris + 1));
                setForeground(Tema.TEKS_PUDAR);
            }
        });
        tabel.getColumnModel().getColumn(KOLOM_STATUS).setCellRenderer(new RendererDasar() {
            @Override
            protected void ubahTampilan(Object nilai, int baris) {
                boolean aktif = Karyawan.STATUS_AKTIF.equals(nilai);
                setText("\u25CF  " + nilai);
                setForeground(aktif ? Tema.SUKSES : Tema.TEKS_PUDAR);
                setFont(Tema.font(Font.BOLD, 13));
            }
        });

        // judul kolom
        JTableHeader header = tabel.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 42));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object nilai, boolean dipilih,
                    boolean fokus, int baris, int kolom) {
                super.getTableCellRendererComponent(t, nilai, false, false, baris, kolom);
                String teks = String.valueOf(nilai);
                List<? extends RowSorter.SortKey> kunci = sorter.getSortKeys();
                if (!kunci.isEmpty() && kunci.get(0).getColumn() == t.convertColumnIndexToModel(kolom)
                        && kunci.get(0).getSortOrder() != SortOrder.UNSORTED) {
                    teks += kunci.get(0).getSortOrder() == SortOrder.ASCENDING ? "   \u25B2" : "   \u25BC";
                }
                setText(teks);
                setOpaque(true);
                setBackground(Tema.SIDEBAR);
                setForeground(Color.WHITE);
                setFont(Tema.font(Font.BOLD, 13));
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return this;
            }
        });
    }

    /** Renderer dasar: padding, warna selang-seling, dan warna baris terpilih. */
    private class RendererDasar extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object nilai, boolean dipilih,
                boolean fokus, int baris, int kolom) {
            super.getTableCellRendererComponent(t, nilai, dipilih, false, baris, kolom);
            setOpaque(true);
            setFont(t.getFont());
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setForeground(Tema.TEKS);
            setBackground(dipilih ? t.getSelectionBackground() : (baris % 2 == 0 ? Color.WHITE : BARIS_GANJIL));
            if (nilai == null || String.valueOf(nilai).trim().isEmpty()) {
                setText("-");
            }
            ubahTampilan(nilai, baris);
            return this;
        }

        protected void ubahTampilan(Object nilai, int baris) {
        }
    }

    // =====================================================
    // PEMASANGAN AKSI
    // =====================================================

    private void pasangAksi() {
        btnTambah.addActionListener(e -> tambahKaryawan());
        btnRefresh.addActionListener(e -> muatSemua());
        btnEkspor.addActionListener(e -> eksporCsv());
        btnReset.addActionListener(e -> resetFilter());
        btnDetail.addActionListener(e -> lihatDetail());
        btnUbah.addActionListener(e -> ubahKaryawan());
        btnStatus.addActionListener(e -> gantiStatus());
        btnHapus.addActionListener(e -> hapusKaryawan());

        menuDetail.addActionListener(e -> lihatDetail());
        menuUbah.addActionListener(e -> ubahKaryawan());
        menuStatus.addActionListener(e -> gantiStatus());
        menuHapus.addActionListener(e -> hapusKaryawan());
        JPopupMenu menu = new JPopupMenu();
        menu.add(menuDetail);
        menu.add(menuUbah);
        menu.add(menuStatus);
        menu.addSeparator();
        menu.add(menuHapus);

        PendengarFilter filter = new PendengarFilter();
        cbStatus.addActionListener(filter);
        cbDepartemen.addActionListener(filter);

        tfCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                timerCari.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                timerCari.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                timerCari.restart();
            }
        });

        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                perbaruiTombolAksi();
            }
        });

        tabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                tampilkanMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                tampilkanMenu(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)
                        && tabel.rowAtPoint(e.getPoint()) >= 0) {
                    ubahKaryawan();
                }
            }

            private void tampilkanMenu(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                int baris = tabel.rowAtPoint(e.getPoint());
                if (baris < 0) {
                    return;
                }
                tabel.setRowSelectionInterval(baris, baris);
                Karyawan k = karyawanTerpilih();
                menuStatus.setText(k != null && k.isAktif() ? "Nonaktifkan" : "Aktifkan");
                menu.show(tabel, e.getX(), e.getY());
            }
        });

        // Ctrl+F untuk langsung ke kolom pencarian
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "fokusCari");
        getActionMap().put("fokusCari", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tfCari.requestFocusInWindow();
                tfCari.selectAll();
            }
        });
    }

    /** Memuat ulang data saat filter diganti, kecuali kalau daftar filter sedang diisi ulang oleh program. */
    private class PendengarFilter implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!mengisiFilter) {
                muatData();
            }
        }
    }

    private void perbaruiTombolAksi() {
        Karyawan k = karyawanTerpilih();
        boolean ada = k != null;
        btnDetail.setEnabled(ada);
        btnUbah.setEnabled(ada);
        btnStatus.setEnabled(ada);
        btnHapus.setEnabled(ada);
        btnStatus.setText(ada && !k.isAktif() ? "Aktifkan" : "Nonaktifkan");
    }

    // =====================================================
    // MEMUAT DATA
    // =====================================================

    private void muatSemua() {
        muatData();
        muatRingkasan();
    }

    private void muatData() {
        final String kata = tfCari.getText();
        final String status = pilihanFilter(cbStatus);
        final String departemen = pilihanFilter(cbDepartemen);
        final int nomor = ++nomorPermintaan;
        lblInfo.setText("Memuat data...");

        new SwingWorker<List<Karyawan>, Void>() {
            @Override
            protected List<Karyawan> doInBackground() throws Exception {
                return dao.cari(kata, status, departemen);
            }

            @Override
            protected void done() {
                if (nomor != nomorPermintaan) {
                    return; // sudah ada permintaan yang lebih baru
                }
                try {
                    isiTabel(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    lblInfo.setText("Gagal memuat data karyawan.");
                    if (!galatMuatSudahTampil) {
                        galatMuatSudahTampil = true;
                        tampilkanGalat("Gagal memuat data karyawan", ex.getCause());
                    }
                }
            }
        }.execute();
    }

    private void muatRingkasan() {
        new SwingWorker<KaryawanDAO.Ringkasan, Void>() {
            @Override
            protected KaryawanDAO.Ringkasan doInBackground() throws Exception {
                return dao.ringkasan();
            }

            @Override
            protected void done() {
                try {
                    terapkanRingkasan(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    // kegagalan koneksi sudah dilaporkan oleh muatData()
                }
            }
        }.execute();
    }

    private void terapkanRingkasan(KaryawanDAO.Ringkasan r) {
        lblTotal.setText(String.valueOf(r.total));
        lblAktif.setText(String.valueOf(r.aktif));
        lblNonaktif.setText(String.valueOf(r.nonaktif));
        totalSemua = r.total;
        daftarDepartemen = r.departemen;

        String sebelumnya = pilihanFilter(cbDepartemen);
        boolean tidakAdaLagi = sebelumnya != null && !r.departemen.contains(sebelumnya);

        mengisiFilter = true;
        try {
            cbDepartemen.removeAllItems();
            cbDepartemen.addItem(SEMUA);
            for (String d : r.departemen) {
                cbDepartemen.addItem(d);
            }
            cbDepartemen.setSelectedItem(tidakAdaLagi || sebelumnya == null ? SEMUA : sebelumnya);
        } finally {
            mengisiFilter = false;
        }
        if (tidakAdaLagi) {
            muatData(); // departemen yang dipilih sudah tidak punya anggota
        }
        perbaruiInfo();
    }

    private void isiTabel(List<Karyawan> daftar) {
        galatMuatSudahTampil = false;

        Karyawan dipilih = karyawanTerpilih();
        int idDipilih = dipilih == null ? -1 : dipilih.getId();

        data = daftar;
        modelTabel.setRowCount(0);
        for (Karyawan k : daftar) {
            modelTabel.addRow(new Object[] {
                0, k.getKode(), k.getNama(), k.getJenisKelamin(), k.getJabatan(),
                k.getDepartemen(), k.getNoHp(), k.getTanggalMasuk(), k.getStatus()
            });
        }

        for (int i = 0; i < daftar.size(); i++) {
            if (daftar.get(i).getId() == idDipilih) {
                int baris = tabel.convertRowIndexToView(i);
                tabel.setRowSelectionInterval(baris, baris);
                break;
            }
        }

        if (daftar.isEmpty()) {
            lblKosong.setText(filterAktif()
                ? "Tidak ada karyawan yang cocok dengan pencarian atau filter."
                : "Belum ada data karyawan. Klik tombol 'Tambah Karyawan' untuk memulai.");
        }
        ((CardLayout) areaTabel.getLayout()).show(areaTabel, daftar.isEmpty() ? "kosong" : "tabel");

        perbaruiTombolAksi();
        perbaruiInfo();
    }

    private void perbaruiInfo() {
        String teks = "Menampilkan " + data.size() + " karyawan";
        if (filterAktif() && totalSemua >= 0) {
            teks += " dari " + totalSemua;
        }
        lblInfo.setText(teks);
    }

    private boolean filterAktif() {
        return !tfCari.getText().trim().isEmpty()
            || cbStatus.getSelectedIndex() > 0
            || cbDepartemen.getSelectedIndex() > 0;
    }

    private String pilihanFilter(JComboBox<String> cb) {
        Object o = cb.getSelectedItem();
        return (o == null || SEMUA.equals(o)) ? null : o.toString();
    }

    private void resetFilter() {
        mengisiFilter = true;
        try {
            cbStatus.setSelectedIndex(0);
            cbDepartemen.setSelectedIndex(0);
        } finally {
            mengisiFilter = false;
        }
        timerCari.stop();
        tfCari.setText("");
        muatData();
    }

    private Karyawan karyawanTerpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            return null;
        }
        int indeks = tabel.convertRowIndexToModel(baris);
        return indeks < data.size() ? data.get(indeks) : null;
    }

    // =====================================================
    // AKSI: TAMBAH, UBAH, STATUS, HAPUS, DETAIL
    // =====================================================

    private Window jendela() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private void tambahKaryawan() {
        String kode;
        try {
            kode = dao.kodeBerikutnya();
        } catch (SQLException ex) {
            tampilkanGalat("Gagal menyiapkan kode karyawan", ex);
            return;
        }
        Karyawan baru = new FormKaryawanDialog(jendela(), null, kode, daftarDepartemen).tampilkan();
        if (baru == null) {
            return;
        }
        try {
            dao.tambah(baru);
            tampilkanPesan("Karyawan " + baru.getNama() + " (" + baru.getKode() + ") berhasil ditambahkan.");
            muatSemua();
        } catch (SQLException ex) {
            tampilkanGalat("Gagal menyimpan karyawan", ex);
        }
    }

    private void ubahKaryawan() {
        Karyawan k = karyawanTerpilih();
        if (k == null) {
            return;
        }
        Karyawan diubah = new FormKaryawanDialog(jendela(), k, null, daftarDepartemen).tampilkan();
        if (diubah == null) {
            return;
        }
        try {
            dao.ubah(diubah);
            tampilkanPesan("Data " + diubah.getNama() + " berhasil diperbarui.");
            muatSemua();
        } catch (SQLException ex) {
            tampilkanGalat("Gagal memperbarui data karyawan", ex);
        }
    }

    private void gantiStatus() {
        Karyawan k = karyawanTerpilih();
        if (k == null) {
            return;
        }
        boolean menonaktifkan = k.isAktif();
        String statusBaru = menonaktifkan ? Karyawan.STATUS_NONAKTIF : Karyawan.STATUS_AKTIF;
        String pesan = menonaktifkan
            ? "Nonaktifkan " + k.getNama() + "?\nKaryawan nonaktif tidak akan dihitung saat presensi, "
                + "tetapi datanya tetap tersimpan."
            : "Aktifkan kembali " + k.getNama() + "?";

        int pilihan = JOptionPane.showConfirmDialog(this, pesan,
            menonaktifkan ? "Nonaktifkan karyawan" : "Aktifkan karyawan",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (pilihan != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.ubahStatus(k.getId(), statusBaru);
            tampilkanPesan(k.getNama() + " sekarang berstatus " + statusBaru + ".");
            muatSemua();
        } catch (SQLException ex) {
            tampilkanGalat("Gagal mengubah status karyawan", ex);
        }
    }

    private void hapusKaryawan() {
        Karyawan k = karyawanTerpilih();
        if (k == null) {
            return;
        }
        Object[] opsi = {"Hapus Permanen", "Batal"};
        int pilihan = JOptionPane.showOptionDialog(this,
            "Hapus " + k.getNama() + " (" + k.getKode() + ") secara permanen?\n\n"
                + "Data yang dihapus tidak bisa dikembalikan. Jika karyawan hanya berhenti bekerja,\n"
                + "lebih aman memakai 'Nonaktifkan' agar riwayat presensinya tetap ada.",
            "Hapus karyawan", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
            null, opsi, opsi[1]);
        if (pilihan != 0) {
            return;
        }
        try {
            dao.hapus(k.getId());
            tampilkanPesan(k.getNama() + " telah dihapus.");
            muatSemua();
        } catch (SQLException ex) {
            tampilkanGalat("Gagal menghapus karyawan", ex);
        }
    }

    private void lihatDetail() {
        Karyawan k = karyawanTerpilih();
        if (k == null) {
            return;
        }
        String html = "<html><body style='width:340px'>"
            + "<h2 style='margin:0 0 8px 0'>" + esc(k.getNama()) + "</h2>"
            + "<table cellpadding='3'>"
            + barisDetail("Kode", k.getKode())
            + barisDetail("Status", k.getStatus())
            + barisDetail("Jenis kelamin", k.getJenisKelamin())
            + barisDetail("Jabatan", k.getJabatan())
            + barisDetail("Departemen", k.getDepartemen())
            + barisDetail("Tanggal masuk", k.getTanggalMasuk() == null ? null : k.getTanggalMasuk().format(FORMAT_TANGGAL))
            + barisDetail("No. HP", k.getNoHp())
            + barisDetail("Email", k.getEmail())
            + barisDetail("Alamat", k.getAlamat())
            + "</table></body></html>";
        JOptionPane.showMessageDialog(this, html, "Detail karyawan", JOptionPane.PLAIN_MESSAGE);
    }

    private String barisDetail(String label, String nilai) {
        String isi = (nilai == null || nilai.trim().isEmpty()) ? "-" : esc(nilai);
        return "<tr><td valign='top'><b>" + label + "</b></td><td valign='top'>: " + isi + "</td></tr>";
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // =====================================================
    // EKSPOR CSV
    // =====================================================

    /** Mengekspor baris yang sedang tampil (sesuai pencarian, filter, dan urutan tabel). */
    private void eksporCsv() {
        if (tabel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk diekspor.", "Ekspor CSV",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser pemilih = new JFileChooser();
        pemilih.setDialogTitle("Simpan data karyawan");
        pemilih.setFileFilter(new FileNameExtensionFilter("File CSV (*.csv)", "csv"));
        pemilih.setSelectedFile(new File("data-karyawan-"
            + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv"));
        if (pemilih.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File berkas = pemilih.getSelectedFile();
        if (!berkas.getName().toLowerCase().endsWith(".csv")) {
            berkas = new File(berkas.getParentFile(), berkas.getName() + ".csv");
        }
        if (berkas.exists()) {
            int timpa = JOptionPane.showConfirmDialog(this,
                "File " + berkas.getName() + " sudah ada. Timpa?", "Ekspor CSV",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (timpa != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try (BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(berkas), StandardCharsets.UTF_8))) {
            w.write('\uFEFF'); // penanda UTF-8 agar Excel membaca huruf dengan benar
            w.write("Kode,Nama,Jenis Kelamin,Jabatan,Departemen,No. HP,Email,Alamat,Tanggal Masuk,Status\r\n");
            for (int v = 0; v < tabel.getRowCount(); v++) {
                Karyawan k = data.get(tabel.convertRowIndexToModel(v));
                String hp = k.getNoHp() == null ? "" : "=\"" + k.getNoHp() + "\""; // menjaga angka 0 di depan di Excel
                w.write(String.join(",",
                    csv(k.getKode()), csv(k.getNama()), csv(k.getJenisKelamin()), csv(k.getJabatan()),
                    csv(k.getDepartemen()), csv(hp), csv(k.getEmail()), csv(k.getAlamat()),
                    csv(k.getTanggalMasuk() == null ? "" : k.getTanggalMasuk().format(FORMAT_TANGGAL)),
                    csv(k.getStatus())));
                w.write("\r\n");
            }
            tampilkanPesan(tabel.getRowCount() + " karyawan diekspor ke " + berkas.getName() + ".");
        } catch (IOException ex) {
            tampilkanGalat("Gagal menyimpan file CSV", ex);
        }
    }

    private static String csv(String s) {
        return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\"";
    }

    // =====================================================
    // PESAN
    // =====================================================

    private void tampilkanPesan(String pesan) {
        lblPesan.setText(pesan);
        timerPesan.restart();
    }

    private void tampilkanGalat(String judul, Throwable ex) {
        String detail = ex == null || ex.getMessage() == null ? "Terjadi kesalahan tak dikenal." : ex.getMessage();
        String saran = "";

        if (ex instanceof SQLException) {
            String state = ((SQLException) ex).getSQLState();
            if (state != null && state.startsWith("23")) {
                saran = "\n\nKode karyawan sudah dipakai karyawan lain. Coba simpan ulang.";
            } else if (detail.contains("doesn't exist")) {
                saran = "\n\nTabel 'karyawan' belum dibuat. Jalankan file karyawan.sql di phpMyAdmin "
                    + "pada database db_presensikaryawan.";
            } else if (detail.contains("Communications link failure") || detail.contains("Connection refused")) {
                saran = "\n\nPastikan MySQL (XAMPP) sedang berjalan.";
            }
        }
        JOptionPane.showMessageDialog(this, judul + ":\n" + detail + saran, "Terjadi masalah",
            JOptionPane.ERROR_MESSAGE);
    }
}
