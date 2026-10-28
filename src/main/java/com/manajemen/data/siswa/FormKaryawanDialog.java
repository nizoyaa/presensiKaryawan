package com.manajemen.data.siswa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;

/**
 * Form untuk menambah atau mengubah satu karyawan.
 * Dialog hanya mengumpulkan dan memvalidasi isian; penyimpanan ke database dilakukan pemanggil.
 */
public class FormKaryawanDialog extends JDialog {

    private static final String[] DEPARTEMEN_UMUM = {
        "Administrasi", "Keuangan", "Gudang", "Operasional", "HRD", "IT"
    };
    private static final Pattern POLA_EMAIL = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern POLA_HP = Pattern.compile("^\\+?[0-9]{8,15}$");

    private final Karyawan awal;
    private Karyawan hasil;

    private final JTextField tfKode = new JTextField();
    private final JTextField tfNama = new JTextField();
    private final JTextField tfJabatan = new JTextField();
    private final JTextField tfHp = new JTextField();
    private final JTextField tfEmail = new JTextField();
    private final JTextArea taAlamat = new JTextArea(3, 20);
    private final JComboBox<String> cbJenisKelamin = new JComboBox<>(new String[] {"Laki-laki", "Perempuan"});
    private final JComboBox<String> cbStatus =
        new JComboBox<>(new String[] {Karyawan.STATUS_AKTIF, Karyawan.STATUS_NONAKTIF});
    private final JComboBox<String> cbDepartemen;
    private final JSpinner spTanggal =
        new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));

    /**
     * @param awal       data yang diubah, atau null kalau menambah karyawan baru
     * @param kodeBaru   kode otomatis untuk karyawan baru (diabaikan saat mengubah)
     * @param departemen daftar departemen yang sudah ada, untuk pilihan cepat
     */
    public FormKaryawanDialog(Window pemilik, Karyawan awal, String kodeBaru, List<String> departemen) {
        super(pemilik,
              awal == null ? "Tambah Karyawan" : "Ubah Data Karyawan",
              Dialog.ModalityType.APPLICATION_MODAL);
        this.awal = awal;

        Set<String> pilihan = new LinkedHashSet<>();
        if (departemen != null) {
            pilihan.addAll(departemen);
        }
        pilihan.addAll(Arrays.asList(DEPARTEMEN_UMUM));
        cbDepartemen = new JComboBox<>(pilihan.toArray(new String[0]));
        cbDepartemen.setEditable(true);

        siapkanKomponen(kodeBaru);

        JPanel isi = new JPanel(new BorderLayout());
        isi.setBackground(Tema.KARTU);
        isi.add(buatHeader(), BorderLayout.NORTH);
        isi.add(buatForm(), BorderLayout.CENTER);
        isi.add(buatTombol(), BorderLayout.SOUTH);
        setContentPane(isi);

        setResizable(false);
        pack();
        setLocationRelativeTo(pemilik);

        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    /** Menampilkan dialog dan menunggu sampai ditutup. Mengembalikan null jika dibatalkan. */
    public Karyawan tampilkan() {
        setVisible(true);
        return hasil;
    }

    // =====================================================
    // TAMPILAN
    // =====================================================

    private void siapkanKomponen(String kodeBaru) {
        for (JTextField tf : new JTextField[] {tfKode, tfNama, tfJabatan, tfHp, tfEmail}) {
            tf.setFont(Tema.font(Font.PLAIN, 14));
            tf.setBorder(Tema.borderInput());
        }
        tfKode.setEditable(false);
        tfKode.setBackground(new Color(241, 245, 249));
        tfKode.setForeground(Tema.TEKS_PUDAR);

        taAlamat.setFont(Tema.font(Font.PLAIN, 14));
        taAlamat.setLineWrap(true);
        taAlamat.setWrapStyleWord(true);
        taAlamat.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        for (JComboBox<String> cb : Arrays.asList(cbJenisKelamin, cbStatus, cbDepartemen)) {
            cb.setFont(Tema.font(Font.PLAIN, 14));
            cb.setPreferredSize(new Dimension(200, 38));
        }
        spTanggal.setEditor(new JSpinner.DateEditor(spTanggal, "dd-MM-yyyy"));
        spTanggal.setFont(Tema.font(Font.PLAIN, 14));
        spTanggal.setPreferredSize(new Dimension(200, 38));

        if (awal == null) {
            tfKode.setText(kodeBaru);
            cbDepartemen.setSelectedIndex(-1);
            cbStatus.setSelectedItem(Karyawan.STATUS_AKTIF);
        } else {
            tfKode.setText(awal.getKode());
            tfNama.setText(awal.getNama());
            tfJabatan.setText(awal.getJabatan());
            tfHp.setText(awal.getNoHp() == null ? "" : awal.getNoHp());
            tfEmail.setText(awal.getEmail() == null ? "" : awal.getEmail());
            taAlamat.setText(awal.getAlamat() == null ? "" : awal.getAlamat());
            cbJenisKelamin.setSelectedItem(awal.getJenisKelamin());
            cbDepartemen.setSelectedItem(awal.getDepartemen());
            cbStatus.setSelectedItem(awal.getStatus());
            if (awal.getTanggalMasuk() != null) {
                spTanggal.setValue(Date.from(awal.getTanggalMasuk().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
        }
    }

    private JPanel buatHeader() {
        JLabel judul = new JLabel(getTitle());
        judul.setFont(Tema.font(Font.BOLD, 20));
        judul.setForeground(Tema.TEKS);

        JLabel sub = new JLabel(awal == null
            ? "Kode dibuat otomatis dan dipakai sebagai identitas scan presensi."
            : "Kode tidak dapat diubah karena dipakai untuk scan presensi.");
        sub.setFont(Tema.font(Font.PLAIN, 13));
        sub.setForeground(Tema.TEKS_PUDAR);

        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Tema.KARTU);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.GARIS),
            BorderFactory.createEmptyBorder(18, 24, 14, 24)));
        p.add(judul, BorderLayout.NORTH);
        p.add(sub, BorderLayout.CENTER);
        return p;
    }

    private JPanel buatForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Tema.KARTU);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));

        JScrollPane scrollAlamat = new JScrollPane(taAlamat);
        scrollAlamat.setBorder(BorderFactory.createLineBorder(Tema.GARIS));

        tambahBlok(form, 0, 0, 1, "Kode Karyawan (otomatis)", tfKode);
        tambahBlok(form, 1, 0, 1, "Tanggal Masuk *", spTanggal);
        tambahBlok(form, 0, 1, 2, "Nama Lengkap *", tfNama);
        tambahBlok(form, 0, 2, 1, "Jenis Kelamin *", cbJenisKelamin);
        tambahBlok(form, 1, 2, 1, "Status *", cbStatus);
        tambahBlok(form, 0, 3, 1, "Jabatan *", tfJabatan);
        tambahBlok(form, 1, 3, 1, "Departemen *", cbDepartemen);
        tambahBlok(form, 0, 4, 1, "No. HP", tfHp);
        tambahBlok(form, 1, 4, 1, "Email", tfEmail);
        tambahBlok(form, 0, 5, 2, "Alamat", scrollAlamat);

        form.setPreferredSize(new Dimension(620, form.getPreferredSize().height));
        return form;
    }

    private void tambahBlok(JPanel form, int kolom, int baris, int lebarKolom, String label, JComponent isi) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(Tema.font(Font.BOLD, 12));
        lbl.setForeground(Tema.TEKS_PUDAR);

        JPanel blok = new JPanel(new BorderLayout(0, 5));
        blok.setOpaque(false);
        blok.add(lbl, BorderLayout.NORTH);
        blok.add(isi, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = kolom;
        c.gridy = baris;
        c.gridwidth = lebarKolom;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, kolom == 0 ? 0 : 8, 12, lebarKolom == 2 || kolom == 1 ? 0 : 8);
        form.add(blok, c);
    }

    private JPanel buatTombol() {
        Tema.Tombol batal = new Tema.Tombol("Batal", Tema.TEKS_PUDAR, false);
        Tema.Tombol simpan = new Tema.Tombol(awal == null ? "Simpan Karyawan" : "Simpan Perubahan", Tema.UTAMA, true);

        batal.addActionListener(e -> dispose());
        simpan.addActionListener(this::simpan);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setBackground(Tema.KARTU);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.GARIS),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        p.add(batal);
        p.add(simpan);
        getRootPane().setDefaultButton(simpan);
        return p;
    }

    // =====================================================
    // VALIDASI & SIMPAN
    // =====================================================

    private void simpan(ActionEvent e) {
        String nama = tfNama.getText().trim();
        if (nama.length() < 3) {
            peringatan("Nama lengkap wajib diisi (minimal 3 karakter).", tfNama);
            return;
        }
        if (nama.length() > 100) {
            peringatan("Nama lengkap terlalu panjang (maksimal 100 karakter).", tfNama);
            return;
        }

        String jabatan = tfJabatan.getText().trim();
        if (jabatan.isEmpty()) {
            peringatan("Jabatan wajib diisi.", tfJabatan);
            return;
        }
        if (jabatan.length() > 60) {
            peringatan("Jabatan terlalu panjang (maksimal 60 karakter).", tfJabatan);
            return;
        }

        String departemen = teksDepartemen();
        if (departemen.isEmpty()) {
            peringatan("Departemen wajib diisi. Pilih dari daftar atau ketik departemen baru.", cbDepartemen);
            return;
        }
        if (departemen.length() > 60) {
            peringatan("Nama departemen terlalu panjang (maksimal 60 karakter).", cbDepartemen);
            return;
        }

        String hp = tfHp.getText().replaceAll("[\\s-]", "");
        if (!hp.isEmpty() && !POLA_HP.matcher(hp).matches()) {
            peringatan("No. HP tidak valid. Gunakan angka saja (8-15 digit), boleh diawali tanda +.", tfHp);
            return;
        }

        String email = tfEmail.getText().trim();
        if (!email.isEmpty() && !POLA_EMAIL.matcher(email).matches()) {
            peringatan("Format email tidak valid. Contoh: nama@perusahaan.com", tfEmail);
            return;
        }

        Karyawan k = new Karyawan();
        if (awal != null) {
            k.setId(awal.getId());
        }
        k.setKode(tfKode.getText().trim());
        k.setNama(nama);
        k.setJenisKelamin((String) cbJenisKelamin.getSelectedItem());
        k.setJabatan(jabatan);
        k.setDepartemen(departemen);
        k.setNoHp(hp);
        k.setEmail(email);
        k.setAlamat(taAlamat.getText().trim());
        k.setTanggalMasuk(((Date) spTanggal.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        k.setStatus((String) cbStatus.getSelectedItem());

        hasil = k;
        dispose();
    }

    private String teksDepartemen() {
        Object o = cbDepartemen.getEditor().getItem();
        return o == null ? "" : o.toString().trim();
    }

    private void peringatan(String pesan, JComponent fokus) {
        JOptionPane.showMessageDialog(this, pesan, "Data belum valid", JOptionPane.WARNING_MESSAGE);
        fokus.requestFocusInWindow();
    }
}
