package com.manajemen.data.siswa;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Dasboard extends JFrame {

    private static final Logger logger = Logger.getLogger(Dasboard.class.getName());

    // ===== KONSTANTA =====
    private static final int LEBAR_SIDEBAR_BESAR = 350;
    private static final int LEBAR_SIDEBAR_KECIL = 60;
    private static final int TINGGI_TOMBOL = 55;
    private static final int UKURAN_ICON = 24;
    private static final Color WARNA_SIDEBAR = new Color(25, 35, 55);
    private static final String LOKASI_ICON = "/com/images/icon/";

    // Daftar menu: {teks menu, nama file ikon}
    private static final String[][] DAFTAR_MENU = {
        {"Dashboard",        "1.png"},
        {"Data Karyawan",    "2.png"},
        {"Jadwal Presensi",  "3.png"},
        {"Riwayat Presensi", "4.png"},
        {"Laporan",          "5.png"},
        {"Pengaturan",       "6.png"}
    };

    // ===== KOMPONEN =====
    private final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final JPanel panelKonten = new JPanel(new CardLayout());
    private final JButton tombolToggle = new JButton("<");
    private final List<JButton> tombolMenu = new ArrayList<>();

    // Halaman dibuat saat menu pertama kali dibuka (bukan saat aplikasi mulai),
    // supaya halaman yang memuat database tidak langsung mengakses DB di awal.
    private final Map<String, Supplier<JComponent>> pabrikHalaman = new HashMap<>();
    private final Set<String> halamanTerbuat = new HashSet<>();

    private boolean sidebarKecil = false;

    public Dasboard() {
        setTitle("Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        pabrikHalaman.put("Dashboard", PanelScanPresensi::new);
        pabrikHalaman.put("Data Karyawan", PanelDataKaryawan::new);

        splitPane.setLeftComponent(buatSidebar());
        splitPane.setRightComponent(panelKonten);
        splitPane.setEnabled(false); // divider dikunci, lebar diatur lewat tombol toggle
        splitPane.setDividerLocation(LEBAR_SIDEBAR_BESAR);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        tombolToggle.addActionListener(e -> aturSidebar(!sidebarKecil));

        tampilkanHalaman(DAFTAR_MENU[0][0]);
        setExtendedState(MAXIMIZED_BOTH);
    }

    // =====================================================
    // SIDEBAR
    // =====================================================

    private JPanel buatSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(WARNA_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(LEBAR_SIDEBAR_BESAR, 0));
        sidebar.setMinimumSize(new Dimension(LEBAR_SIDEBAR_KECIL, 0));

        sidebar.add(buatPanelAtas(), BorderLayout.NORTH);
        sidebar.add(buatPanelMenu(), BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel buatPanelAtas() {
        tombolToggle.setFocusPainted(false);

        JPanel panelAtas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelAtas.setBackground(WARNA_SIDEBAR);
        panelAtas.add(tombolToggle);
        return panelAtas;
    }

    private JPanel buatPanelMenu() {
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(new BoxLayout(panelMenu, BoxLayout.Y_AXIS));
        panelMenu.setBackground(WARNA_SIDEBAR);
        panelMenu.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));

        for (int i = 0; i < DAFTAR_MENU.length; i++) {
            String teks = DAFTAR_MENU[i][0];
            String fileIcon = DAFTAR_MENU[i][1];

            JButton tombol = buatTombolMenu(teks, muatIcon(LOKASI_ICON + fileIcon));
            tombol.addActionListener(e -> tampilkanHalaman(teks));
            tombolMenu.add(tombol);

            if (i > 0) {
                panelMenu.add(Box.createVerticalStrut(5));
            }
            panelMenu.add(tombol);
        }
        return panelMenu;
    }

    private JButton buatTombolMenu(String teks, ImageIcon icon) {
        JButton tombol = new JButton(teks, icon);
        tombol.setToolTipText(teks);
        tombol.setFocusPainted(false);
        tombol.setBorderPainted(false);
        tombol.setContentAreaFilled(false);
        tombol.setForeground(Color.WHITE);
        tombol.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tombol.setHorizontalAlignment(SwingConstants.LEFT);
        tombol.setIconTextGap(15);
        tombol.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tombol.setMaximumSize(new Dimension(Integer.MAX_VALUE, TINGGI_TOMBOL));
        tombol.setPreferredSize(new Dimension(LEBAR_SIDEBAR_BESAR, TINGGI_TOMBOL));
        return tombol;
    }

    /**
     * Membesarkan atau mengecilkan sidebar.
     * Saat kecil: hanya ikon yang tampil (nama menu tersedia sebagai tooltip).
     */
    private void aturSidebar(boolean kecil) {
        sidebarKecil = kecil;
        splitPane.setDividerLocation(kecil ? LEBAR_SIDEBAR_KECIL : LEBAR_SIDEBAR_BESAR);
        tombolToggle.setText(kecil ? ">" : "<");

        for (int i = 0; i < tombolMenu.size(); i++) {
            JButton tombol = tombolMenu.get(i);
            tombol.setText(kecil ? "" : DAFTAR_MENU[i][0]);
            tombol.setHorizontalAlignment(kecil ? SwingConstants.CENTER : SwingConstants.LEFT);
        }
    }

    // =====================================================
    // KONTEN
    // =====================================================

    /**
     * Halaman sementara berisi judul saja.
     * Dipakai untuk menu yang halamannya belum dibuat. Daftarkan halaman asli di pabrikHalaman.
     */
    private JPanel buatHalaman(String judul) {
        JLabel labelJudul = new JLabel(judul, SwingConstants.CENTER);
        labelJudul.setFont(new Font("Segoe UI", Font.BOLD, 30));

        JPanel halaman = new JPanel(new BorderLayout());
        halaman.add(labelJudul, BorderLayout.CENTER);
        return halaman;
    }

    private void tampilkanHalaman(String nama) {
        if (halamanTerbuat.add(nama)) {
            Supplier<JComponent> pabrik = pabrikHalaman.getOrDefault(nama, () -> buatHalaman(nama));
            panelKonten.add(pabrik.get(), nama);
        }
        ((CardLayout) panelKonten.getLayout()).show(panelKonten, nama);
    }

    // =====================================================
    // UTILITAS
    // =====================================================

    /** Memuat ikon dari classpath lalu mengecilkannya. Return null jika file tidak ketemu. */
    private ImageIcon muatIcon(String lokasi) {
        URL url = getClass().getResource(lokasi);
        if (url == null) {
            logger.log(Level.WARNING, "Ikon tidak ditemukan: {0}", lokasi);
            return null;
        }
        Image gambar = new ImageIcon(url).getImage()
                .getScaledInstance(UKURAN_ICON, UKURAN_ICON, Image.SCALE_SMOOTH);
        return new ImageIcon(gambar);
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        EventQueue.invokeLater(() -> new Dasboard().setVisible(true));
    }
}