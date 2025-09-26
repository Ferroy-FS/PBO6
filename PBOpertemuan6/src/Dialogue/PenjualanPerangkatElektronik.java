/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Dialogue;

/**
 *
 * @author LEGION
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class PenjualanPerangkatElektronik extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PenjualanPerangkatElektronik.class.getName());
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/PBO_Praktikum_5";
    private static final String USER = "postgres";
    private static final String PASS = "0000";
    private Connection conn;

    public PenjualanPerangkatElektronik() {
        initComponents();
        connectToDatabase();
        loadDataToTable();
        setupTableSelectionListener();
    }

    private void connectToDatabase() {
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Koneksi database berhasil!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal terhubung ke database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDataToTable() {
        try {
            String query = "SELECT nomor_seri, jenis_perangkat, merek_perangkat, nama_perangkat, model_perangkat FROM penjualan_perangkat_elektronik";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }

            model.setRowCount(0);

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }

            jTable1.setModel(model);

            jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i = 0; i < columnCount; i++) {
                jTable1.getColumnModel().getColumn(i).setPreferredWidth(150);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTableSelectionListener() {
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = jTable1.getSelectedRow();
                if (row >= 0) {
                }
            }
        });
    }

    private void refreshTable() {
        loadDataToTable();
    }

    private void searchData() {
        String keyword = JOptionPane.showInputDialog(this, "Masukkan kata kunci pencarian:");

        if (keyword == null) {
            return;
        }
        keyword = keyword.trim();

        try {
            if (keyword.isEmpty()) {
                loadDataToTable();
                return;
            }

            String query = "SELECT nomor_seri, jenis_perangkat, merek_perangkat, nama_perangkat, model_perangkat "
                    + "FROM penjualan_perangkat_elektronik WHERE "
                    + "nomor_seri ILIKE ? OR "
                    + "jenis_perangkat ILIKE ? OR "
                    + "merek_perangkat ILIKE ? OR "
                    + "nama_perangkat ILIKE ? OR "
                    + "model_perangkat ILIKE ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            String searchPattern = "%" + keyword + "%";

            for (int i = 1; i <= 5; i++) {
                pstmt.setString(i, searchPattern);
            }

            ResultSet rs = pstmt.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            DefaultTableModel model = new DefaultTableModel();

            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }

            jTable1.setModel(model);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mencari data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showInsertDialog() {
        Dialog dialog = new Dialog("INSERT", "", "", "", "", "");
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            refreshTable();
        }
    }

    private void showUpdateDialog() {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data yang akan diupdate terlebih dahulu!");
            return;
        }

        String nomorSeri = jTable1.getValueAt(selectedRow, 0).toString();
        String jenis = jTable1.getValueAt(selectedRow, 1).toString();
        String merek = jTable1.getValueAt(selectedRow, 2).toString();
        String nama = jTable1.getValueAt(selectedRow, 3).toString();
        String model = jTable1.getValueAt(selectedRow, 4).toString();

        Dialog dialog = new Dialog("UPDATE", nomorSeri, jenis, merek, nama, model);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            refreshTable();
        }
    }

    private void showDeleteDialog() {
        int selectedRow = jTable1.getSelectedRow();
        String nomorSeri = "";

        if (selectedRow != -1) {
            nomorSeri = jTable1.getValueAt(selectedRow, 0).toString();
        } else {
            nomorSeri = JOptionPane.showInputDialog(this, "Masukkan nomor seri yang akan dihapus:");
            if (nomorSeri == null || nomorSeri.trim().isEmpty()) {
                return;
            }
        }

        Dialog dialog = new Dialog("DELETE", nomorSeri.trim());
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            refreshTable();
        }
    }

    private boolean insertData(String nomorSeri, String jenis, String merek, String nama, String model) {
        try {
            if (nomorSeri.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nomor seri harus diisi!");
                return false;
            }

            if (isNomorSeriExists(nomorSeri)) {
                JOptionPane.showMessageDialog(this, "Nomor seri sudah ada dalam database!");
                return false;
            }

            String query = "INSERT INTO penjualan_perangkat_elektronik "
                    + "(nomor_seri, jenis_perangkat, merek_perangkat, nama_perangkat, model_perangkat, warna, tahun_rilis, harga, stok) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, nomorSeri);
            pstmt.setString(2, jenis);
            pstmt.setString(3, merek);
            pstmt.setString(4, nama);
            pstmt.setString(5, model);
            pstmt.setString(6, "Default");
            pstmt.setInt(7, 2023);
            pstmt.setLong(8, 0);
            pstmt.setInt(9, 0);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan!");
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan data!");
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menambahkan data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateData(String nomorSeri, String jenis, String merek, String nama, String model) {
        try {
            if (nomorSeri.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nomor seri harus diisi!");
                return false;
            }

            String query = "UPDATE penjualan_perangkat_elektronik SET "
                    + "jenis_perangkat = ?, merek_perangkat = ?, nama_perangkat = ?, model_perangkat = ? "
                    + "WHERE nomor_seri = ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, jenis);
            pstmt.setString(2, merek);
            pstmt.setString(3, nama);
            pstmt.setString(4, model);
            pstmt.setString(5, nomorSeri);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil diupdate!");
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Data tidak ditemukan!");
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengupdate data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean deleteData(String nomorSeri) {
        try {
            String query = "DELETE FROM penjualan_perangkat_elektronik WHERE nomor_seri = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, nomorSeri);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Data tidak ditemukan!");
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean isNomorSeriExists(String nomorSeri) {
        try {
            String query = "SELECT COUNT(*) FROM penjualan_perangkat_elektronik WHERE nomor_seri = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, nomorSeri);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void clearForm() {
        refreshTable();
        JOptionPane.showMessageDialog(this, "Tabel telah di-refresh!");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnRefresh = new javax.swing.JButton();
        btnInsert = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnSearch = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(51, 204, 255));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "title 5"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        btnRefresh.setText("Refresh");
        btnRefresh.setMaximumSize(new java.awt.Dimension(70, 25));
        btnRefresh.setMinimumSize(new java.awt.Dimension(70, 25));
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        btnInsert.setText("Insert");
        btnInsert.setMaximumSize(new java.awt.Dimension(70, 25));
        btnInsert.setMinimumSize(new java.awt.Dimension(70, 25));
        btnInsert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInsertActionPerformed(evt);
            }
        });

        btnUpdate.setText("Update");
        btnUpdate.setMaximumSize(new java.awt.Dimension(70, 25));
        btnUpdate.setMinimumSize(new java.awt.Dimension(70, 25));
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete");
        btnDelete.setMaximumSize(new java.awt.Dimension(70, 25));
        btnDelete.setMinimumSize(new java.awt.Dimension(70, 25));
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 20)); // NOI18N
        jLabel1.setText("DATA PENJUALAN PERANGKAT");

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(188, 188, 188)
                        .addComponent(btnInsert, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSearch)
                        .addGap(18, 18, 18)
                        .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 215, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(261, 261, 261))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnDelete, btnInsert, btnUpdate});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnInsert, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnDelete, btnInsert, btnRefresh, btnUpdate});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        showDeleteDialog();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        // TODO add your handling code here:
        showUpdateDialog();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnInsertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInsertActionPerformed
        // TODO add your handling code here:
        showInsertDialog();
    }//GEN-LAST:event_btnInsertActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        clearForm();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:
        searchData();
    }//GEN-LAST:event_btnSearchActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
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
        java.awt.EventQueue.invokeLater(() -> {
            new PenjualanPerangkatElektronik().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnInsert;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
