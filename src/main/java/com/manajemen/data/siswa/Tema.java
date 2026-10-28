package com.manajemen.data.siswa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Warna, font, dan komponen kecil yang dipakai bersama oleh halaman-halaman aplikasi.
 */
final class Tema {

    private Tema() {
    }

    static final Color LATAR = new Color(243, 245, 249);
    static final Color KARTU = Color.WHITE;
    static final Color GARIS = new Color(222, 226, 233);
    static final Color TEKS = new Color(30, 41, 59);
    static final Color TEKS_PUDAR = new Color(100, 116, 139);
    static final Color SIDEBAR = new Color(25, 35, 55);
    static final Color UTAMA = new Color(37, 99, 235);
    static final Color SUKSES = new Color(22, 163, 74);
    static final Color PERINGATAN = new Color(217, 119, 6);
    static final Color BAHAYA = new Color(220, 38, 38);

    static Font font(int gaya, int ukuran) {
        return new Font("Segoe UI", gaya, ukuran);
    }

    static Border borderKartu(int vertikal, int horizontal) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GARIS),
            BorderFactory.createEmptyBorder(vertikal, horizontal, vertikal, horizontal)
        );
    }

    static Border borderInput() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GARIS),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)
        );
    }

    /** Mencampur warna dengan putih. porsi 1 = warna asli, 0 = putih. Berguna untuk latar yang lembut. */
    static Color campur(Color warna, float porsi) {
        int r = Math.round(255 - (255 - warna.getRed()) * porsi);
        int g = Math.round(255 - (255 - warna.getGreen()) * porsi);
        int b = Math.round(255 - (255 - warna.getBlue()) * porsi);
        return new Color(r, g, b);
    }

    /** Kartu angka ringkasan (judul kecil + angka besar) dengan garis warna di sisi kiri. */
    static JPanel kartuStatistik(String judul, JLabel nilai, Color aksen) {
        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(font(Font.PLAIN, 13));
        lblJudul.setForeground(TEKS_PUDAR);

        nilai.setFont(font(Font.BOLD, 28));
        nilai.setForeground(aksen);

        JPanel kartu = new JPanel(new BorderLayout(0, 2));
        kartu.setBackground(KARTU);
        kartu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GARIS),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, aksen),
                BorderFactory.createEmptyBorder(12, 16, 12, 16))));
        kartu.add(lblJudul, BorderLayout.NORTH);
        kartu.add(nilai, BorderLayout.CENTER);
        return kartu;
    }

    /**
     * Gaya tabel yang seragam: baris lega, tanpa garis kisi, warna selang-seling,
     * dan judul kolom gelap (dengan panah urutan kalau tabelnya punya sorter).
     */
    static void gayaiTabel(JTable tabel) {
        tabel.setRowHeight(40);
        tabel.setShowGrid(false);
        tabel.setIntercellSpacing(new Dimension(0, 0));
        tabel.setFillsViewportHeight(true);
        tabel.setFont(font(Font.PLAIN, 14));
        tabel.setSelectionBackground(new Color(219, 234, 254));
        tabel.setSelectionForeground(TEKS);
        tabel.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabel.setDefaultRenderer(Object.class, new RendererTabel());

        JTableHeader header = tabel.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 42));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object nilai, boolean dipilih,
                    boolean fokus, int baris, int kolom) {
                super.getTableCellRendererComponent(t, nilai, false, false, baris, kolom);
                String teks = String.valueOf(nilai);
                RowSorter<?> sorter = t.getRowSorter();
                if (sorter != null) {
                    List<? extends RowSorter.SortKey> kunci = sorter.getSortKeys();
                    if (!kunci.isEmpty() && kunci.get(0).getColumn() == t.convertColumnIndexToModel(kolom)
                            && kunci.get(0).getSortOrder() != SortOrder.UNSORTED) {
                        teks += kunci.get(0).getSortOrder() == SortOrder.ASCENDING ? "   \u25B2" : "   \u25BC";
                    }
                }
                setText(teks);
                setOpaque(true);
                setBackground(SIDEBAR);
                setForeground(Color.WHITE);
                setFont(font(Font.BOLD, 13));
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return this;
            }
        });
    }

    /**
     * Renderer sel dasar: padding, warna selang-seling, dan warna baris terpilih.
     * Untuk tampilan khusus per kolom, buat turunan dan isi ubahTampilan().
     */
    static class RendererTabel extends DefaultTableCellRenderer {

        private static final Color BARIS_GANJIL = new Color(248, 250, 252);

        @Override
        public Component getTableCellRendererComponent(JTable t, Object nilai, boolean dipilih,
                boolean fokus, int baris, int kolom) {
            super.getTableCellRendererComponent(t, nilai, dipilih, false, baris, kolom);
            setOpaque(true);
            setFont(t.getFont());
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setForeground(TEKS);
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

    /**
     * Tombol dengan sudut membulat yang digambar sendiri.
     * Dibuat manual karena look and feel Nimbus mengabaikan setBackground pada JButton.
     * isi = true: tombol berwarna penuh; isi = false: tombol berbingkai (outline).
     */
    static class Tombol extends JButton {

        private final Color warna;
        private final boolean isi;

        Tombol(String teks, Color warna, boolean isi) {
            super(teks);
            this.warna = warna;
            this.isi = isi;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(font(Font.BOLD, 13));
            setForeground(isi ? Color.WHITE : warna);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(8, 16, 8, 16));
        }

        @Override
        public void setEnabled(boolean aktif) {
            super.setEnabled(aktif);
            if (warna != null) {
                setForeground(aktif ? (isi ? Color.WHITE : warna) : TEKS_PUDAR);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + 8, Math.max(d.height, 38));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ButtonModel m = getModel();
            int w = getWidth() - 1;
            int h = getHeight() - 1;

            if (!isEnabled()) {
                g2.setColor(isi ? GARIS : Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, 10, 10);
                g2.setColor(GARIS);
                g2.drawRoundRect(0, 0, w, h, 10, 10);
            } else if (isi) {
                Color c = m.isPressed() ? warna.darker().darker()
                        : (m.isRollover() ? warna.darker() : warna);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, w, h, 10, 10);
            } else {
                float alfa = m.isPressed() ? 0.25f : (m.isRollover() ? 0.12f : 0f);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, 10, 10);
                if (alfa > 0f) {
                    g2.setColor(new Color(warna.getRed(), warna.getGreen(), warna.getBlue(), (int) (255 * alfa)));
                    g2.fillRoundRect(0, 0, w, h, 10, 10);
                }
                g2.setColor(warna);
                g2.drawRoundRect(0, 0, w, h, 10, 10);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Kolom teks yang menampilkan petunjuk (placeholder) saat masih kosong.
     */
    static class FieldHint extends JTextField {

        private final String petunjuk;

        FieldHint(String petunjuk) {
            this.petunjuk = petunjuk;
            setFont(font(Font.PLAIN, 14));
            setBorder(borderInput());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(TEKS_PUDAR);
                g2.setFont(getFont());
                Insets in = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int y = in.top + (getHeight() - in.top - in.bottom - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(petunjuk, in.left, y);
                g2.dispose();
            }
        }
    }
}