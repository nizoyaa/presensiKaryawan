package com.manajemen.data.siswa;

public class Dasboard extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Dasboard.class.getName());

    public Dasboard() {



        initComponents();

        getContentPane().setLayout( 
            new java.awt.BorderLayout() 
        );

        javax.swing.JPanel panelSidebar =
        new javax.swing.JPanel();

        panelSidebar.setBackground(
            new java.awt.Color(25, 35, 55)
        );

        panelSidebar.setPreferredSize(
            new java.awt.Dimension(350, 0)
        );
         
        panelSidebar.setLayout(new java.awt.BorderLayout());

        javax.swing.JButton tombolKecil =
            new javax.swing.JButton("<");

        tombolKecil.setFocusPainted(false);

        javax.swing.JPanel panelAtas =
            new javax.swing.JPanel(
                new java.awt.FlowLayout(
                    java.awt.FlowLayout.RIGHT,
                    0,
                    0
                )
            );

        panelAtas.setBackground(new java.awt.Color(25, 35, 55));
        panelAtas.add(tombolKecil);
        panelSidebar.add(
            panelAtas, 
            java.awt.BorderLayout.NORTH
        );

        javax.swing.JPanel panelMenu =
        new javax.swing.JPanel();

        panelMenu.setLayout(
            new javax.swing.BoxLayout(
                panelMenu,
                javax.swing.BoxLayout.Y_AXIS
            )
        );

        panelMenu.setBackground(
            new java.awt.Color(25, 35, 55)
        );

        panelMenu.setBorder(
            javax.swing.BorderFactory.createEmptyBorder(
                50,
                0,
                0,
                0
            )
        );
        
        // ICON
        javax.swing.ImageIcon iconDashboard =
            resizeIcon(
                "/com/images/icon/1.png",
                24,
                24
            );

        javax.swing.ImageIcon iconKaryawan =
            resizeIcon(
                "/com/images/icon/2.png",
                24,
                24
            );

        javax.swing.ImageIcon iconJadwal =
            resizeIcon(
                "/com/images/icon/3.png",
                24,
                24
            );

        javax.swing.ImageIcon iconRiwayat =
            resizeIcon(
                "/com/images/icon/4.png",
                24,
                24
            );

        javax.swing.ImageIcon iconLaporan =
            resizeIcon(
                "/com/images/icon/5.png",
                24,
                24
            );

        javax.swing.ImageIcon iconPengaturan =
            resizeIcon(
                "/com/images/icon/6.png",
                24,
                24
            );

        // TOMBOL MENU
        javax.swing.JButton btnDashboard =
            new javax.swing.JButton("Dashboard", iconDashboard);

        javax.swing.JButton btnKaryawan =
            new javax.swing.JButton("Data Karyawan", iconKaryawan);

        javax.swing.JButton btnJadwal =
            new javax.swing.JButton("Jadwal Presensi", iconJadwal);

        javax.swing.JButton btnRiwayat =
            new javax.swing.JButton("Riwayat Presensi", iconRiwayat);

        javax.swing.JButton btnLaporan =
            new javax.swing.JButton("Laporan", iconLaporan);

        javax.swing.JButton btnPengaturan =
            new javax.swing.JButton("Pengaturan", iconPengaturan);

        // ATUR TAMPILAN TOMBOL
        javax.swing.JButton[] tombolMenu = {
            btnDashboard,
            btnKaryawan,
            btnJadwal,
            btnRiwayat,
            btnLaporan,
            btnPengaturan
        };

        for (javax.swing.JButton tombol : tombolMenu) {
            tombol.setFocusPainted(false);
            tombol.setBorderPainted(false);
            tombol.setContentAreaFilled(false);
            tombol.setForeground(java.awt.Color.WHITE);
            tombol.setHorizontalAlignment(
                javax.swing.SwingConstants.LEFT
            );
            tombol.setIconTextGap(15);
            tombol.setFont(
                new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 16)
            );

            tombol.setAlignmentX(
                javax.swing.JComponent.LEFT_ALIGNMENT
            );

            tombol.setMaximumSize(
                new java.awt.Dimension(
                    Integer.MAX_VALUE,
                    55
                )
            );

            tombol.setPreferredSize(
                new java.awt.Dimension(
                    350,
                    55
                )
            );
            
        }

        // MASUKKAN KE PANEL MENU -> DONE
        panelMenu.add(btnDashboard);
        panelMenu.add(javax.swing.Box.createVerticalStrut(5));
        panelMenu.add(btnKaryawan);
        panelMenu.add(javax.swing.Box.createVerticalStrut(5));
        panelMenu.add(btnJadwal);
        panelMenu.add(javax.swing.Box.createVerticalStrut(5));
        panelMenu.add(btnRiwayat);
        panelMenu.add(javax.swing.Box.createVerticalStrut(5));
        panelMenu.add(btnLaporan);
        panelMenu.add(javax.swing.Box.createVerticalStrut(5));
        panelMenu.add(btnPengaturan);
// batas

        panelSidebar.add(
            panelMenu,
            java.awt.BorderLayout.CENTER
        );

        javax.swing.JPanel panelKonten =
            new javax.swing.JPanel();
        
        panelKonten.setLayout(
            new java.awt.BorderLayout()
        );

        btnDashboard.addActionListener(e -> {
            tampilkanHalaman(
                panelKonten, "Dashboard"
            );
        });

        btnKaryawan.addActionListener(e -> {
            tampilkanHalaman(
                panelKonten, "Data Karyawan"
            );
        });

        btnJadwal.addActionListener(e -> {
            tampilkanHalaman(
                panelKonten, "Jadwal Presensi"
            );
        });

        btnRiwayat.addActionListener(e -> {
            tampilkanHalaman(
                panelKonten, "Riwayat Presensi"
            );
        });

        btnLaporan.addActionListener(e -> {
            tampilkanHalaman(
                panelKonten, "Laporan"
            );
        });

        btnPengaturan.addActionListener(e -> {
            tampilkanHalaman(
                panelKonten, "Pengaturan"
            );
        });

        javax.swing.JSplitPane splitPane =
            new javax.swing.JSplitPane(
                javax.swing.JSplitPane.HORIZONTAL_SPLIT,
                panelSidebar,
                panelKonten
            );

        getContentPane().add(
            splitPane,
            java.awt.BorderLayout.CENTER
        );

    final boolean[] sidebarKecil = {false};

    tombolKecil.addActionListener(e -> {

        if (sidebarKecil[0]) {

            // =========================
            // SIDEBAR DIBESARKAN
            // =========================

            splitPane.setDividerLocation(350);

            btnDashboard.setText("Dashboard");
            btnKaryawan.setText("Data Karyawan");
            btnJadwal.setText("Jadwal Presensi");
            btnRiwayat.setText("Riwayat Presensi");
            btnLaporan.setText("Laporan");
            btnPengaturan.setText("Pengaturan");

            for (javax.swing.JButton tombol : tombolMenu) {

                tombol.setHorizontalAlignment(
                    javax.swing.SwingConstants.LEFT
                );

            }

            tombolKecil.setText("<");

            sidebarKecil[0] = false;

        } else {

            // =========================
            // SIDEBAR DIKECILKAN
            // =========================

            splitPane.setDividerLocation(60);

            btnDashboard.setText("");
            btnKaryawan.setText("");
            btnJadwal.setText("");
            btnRiwayat.setText("");
            btnLaporan.setText("");
            btnPengaturan.setText("");

            for (javax.swing.JButton tombol : tombolMenu) {

                tombol.setHorizontalAlignment(
                    javax.swing.SwingConstants.CENTER
                );

            }

            tombolKecil.setText(">");

            sidebarKecil[0] = true;
        }
    });

        setExtendedState(
            javax.swing.JFrame.MAXIMIZED_BOTH
        );
    }

    private javax.swing.ImageIcon resizeIcon(
        String lokasi,
        int lebar,
        int tinggi
    ) 
    {
        java.awt.Image gambar =
            new javax.swing.ImageIcon(
                getClass().getResource(lokasi)
            ).getImage();

        java.awt.Image gambarKecil =
            gambar.getScaledInstance(
                lebar,
                tinggi,
                java.awt.Image.SCALE_SMOOTH
            );

        return new javax.swing.ImageIcon(gambarKecil);
    }

    private void tampilkanHalaman(
            javax.swing.JPanel panelKonten,
            String judul
    ) {

        panelKonten.removeAll();

        javax.swing.JLabel labelJudul =
            new javax.swing.JLabel(judul);

        labelJudul.setFont(
            new java.awt.Font(
                "Segoe UI",
                java.awt.Font.BOLD,
                30
            )
        );

        labelJudul.setHorizontalAlignment(
            javax.swing.SwingConstants.CENTER
        );

        panelKonten.add(
            labelJudul,
            java.awt.BorderLayout.CENTER
        );

        panelKonten.revalidate();
        panelKonten.repaint();
    }
    
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("DASHBOARD");

        jButton1.setText("jButton1");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(165, 165, 165)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(609, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap(381, Short.MAX_VALUE))
        );

        //pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Dasboard().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
