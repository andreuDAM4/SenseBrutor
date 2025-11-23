package anglada.sensebrutor.vista;

import anglada.sensebrutor.SenseBrutor;
import anglada.sensebrutor.model.MediaFileModel;
import anglada.sensebrutor.model.MediaFileModel;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;;

/**
 * Panel sempre visible que mostra els arxius de dins una carpeta on aniran les descargues.
 * Llista tots els arxius depenent d'els filtres seleccionats.
 * Permet cercar per nom amb temps real.
 * @author Andreu
 */
public class MediaFilePanel extends javax.swing.JPanel {

    private final SenseBrutor mainFrame;
    private final List<MediaFileModel> allFiles;
    private List<MediaFileModel> filteredFiles;
    private MediaTableModel tableModel;
    private DefaultListModel<String> mimeListModel;
    private DefaultComboBoxModel<String> filterModel;
    
    public MediaFilePanel(SenseBrutor mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
        allFiles = new ArrayList<>();
        filteredFiles = new ArrayList<>();
    
        setupModels();
        setupListeners();
    }
    private void setupModels() {
         // Asignar AbstractTableModel
        tableModel = new MediaTableModel();
        jTable.setModel(tableModel);
        jTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Nombre
        jTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Tamaño
        jTable.getColumnModel().getColumn(2).setPreferredWidth(100); // MIME
        jTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Fecha

        // Model dinamic
        mimeListModel = new DefaultListModel<>();
        jListMimeTypes.setModel(mimeListModel);

        // Categories fixes
        filterModel = new DefaultComboBoxModel<>();
        filterModel.addElement("Todos");
        filterModel.addElement("Video");
        filterModel.addElement("Audio");
        jComboBoxFilter.setModel(filterModel);
    }
    
    
    private void setupListeners() {
        // Cerca en temps real
        jTextFieldBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        jListMimeTypes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                applyFilters();
            }
        });

        // Habilitar boto eliminar
        jTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                jButtonDelete.setEnabled(jTable.getSelectedRow() != -1);
                jButtonPlay.setEnabled(jTable.getSelectedRow() != -1);
            }
        });

    }
    
    //Carrega sa biblioteca de medis a l'hora de seleccionar una carpeta de sortida
    public void reloadIfConfigured() {
        String path = mainFrame.getPreferencesPanel().getDownloadPath();
        if (path != null && !path.isEmpty()) {
            File folder = new File(path);
            if (folder.exists() && folder.isDirectory()) {
                loadMediaFiles();
            }
        }
    }
    
    //Funcio per mostrar es arxius a sa taula
    private void loadMediaFiles() {
        String path = mainFrame.getPreferencesPanel().getDownloadPath();
        if (path == null || path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Configura la carpeta de descargas.");
            return;
        }

        File folder = new File(path);
        File[] files = folder.listFiles(File::isFile);
        if (files == null) return;

        allFiles.clear();
        Set<String> mimeSet = new HashSet<>();
        
        
        for (File f : files) {
            MediaFileModel mf = new MediaFileModel(f);
            allFiles.add(mf);
            if (!mf.getMimeType().equals("unknown")) {
                mimeSet.add(mf.getMimeType());
            }
        }

        // Actualitzar JList amb unics
        mimeListModel.clear();
        mimeSet.stream().sorted().forEach(mimeListModel::addElement);

        applyFilters();
    }

    // Filtrar una vegada seleccionat es tipus des comobox
    private void applyFilters() {
        String search = jTextFieldBuscar.getText().toLowerCase();
        String filter = (String) jComboBoxFilter.getSelectedItem();
        String selectedMime = jListMimeTypes.getSelectedValue();

        filteredFiles = allFiles.stream()
            .filter(mf -> mf.getName().toLowerCase().contains(search))
            .filter(mf -> filter.equals("Todos") ||
                (filter.equals("Video") && mf.getMimeType().startsWith("video/")) ||
                (filter.equals("Audio") && mf.getMimeType().startsWith("audio/")))
            .filter(mf -> selectedMime == null || selectedMime.equals(mf.getMimeType()))
            .collect(Collectors.toList());

        tableModel.updateData(filteredFiles);
    }

    //Funcio que elimina s'arxiu seleccionat
    private void deleteSelectedFile() {
        int selectedRow = jTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un archivo de la tabla para eliminar.");
            return;
        }

        MediaFileModel mf = filteredFiles.get(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Eliminar '" + mf.getName() + "'?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            File file = new File(mf.getPath());
            if (file.delete()) {
                JOptionPane.showMessageDialog(this, "Archivo eliminado correctamente.");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el archivo.");
            }
            loadMediaFiles();
        }
    }
    // Funcio extra per reproduir un arxiu seleccionat
    private void playSelectedFile() {
        int selectedRow = jTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un archivo de la tabla para reproducir.");
            return;
        }

        MediaFileModel mf = filteredFiles.get(selectedRow);
        File file = new File(mf.getPath());

        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "El archivo no existe o fue movido: " + mf.getPath());
            return;
        }

        try {
            java.awt.Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir el archivo: " + ex.getMessage());
        }
    }

    //Es crea una clase interna per a poder  personalitzar a 100% sa taula
    private class MediaTableModel extends AbstractTableModel {
        private List<MediaFileModel> data = new ArrayList<>();
        private final String[] cols = {"Nombre", "Tamaño", "MIME", "Fecha"};

        public void updateData(List<MediaFileModel> newData) {
            this.data = new ArrayList<>(newData);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            MediaFileModel mf = data.get(row);
            return switch (col) {
                case 0 -> mf.getName();
                case 1 -> formatSize(mf.getSize());
                case 2 -> mf.getMimeType();
                case 3 -> mf.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"));
                default -> null;
            };
        }
    }
    
    //Funcio que serveix per mostrar es pes de s'arxiu amb un format segons sa mida
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelTitle = new javax.swing.JLabel();
        jLabelBuscar = new javax.swing.JLabel();
        jTextFieldBuscar = new javax.swing.JTextField();
        jComboBoxFilter = new javax.swing.JComboBox<>();
        jScrollPaneMimeTypes = new javax.swing.JScrollPane();
        jListMimeTypes = new javax.swing.JList<>();
        jScrollPaneTable = new javax.swing.JScrollPane();
        jTable = new javax.swing.JTable();
        jButtonDelete = new javax.swing.JButton();
        jButtonRefresh = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButtonPlay = new javax.swing.JButton();

        setName("MediaFilePanel"); // NOI18N
        setLayout(null);

        jLabelTitle.setFont(new java.awt.Font("Consolas", 1, 24)); // NOI18N
        jLabelTitle.setText("Biblioteca de Medios");
        add(jLabelTitle);
        jLabelTitle.setBounds(140, 10, 280, 40);

        jLabelBuscar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabelBuscar.setText("Buscar por nombre:");
        add(jLabelBuscar);
        jLabelBuscar.setBounds(30, 50, 110, 16);

        jTextFieldBuscar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jTextFieldBuscar.setToolTipText("Buscar por nombre...");
        add(jTextFieldBuscar);
        jTextFieldBuscar.setBounds(150, 50, 370, 22);

        jComboBoxFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFilterActionPerformed(evt);
            }
        });
        add(jComboBoxFilter);
        jComboBoxFilter.setBounds(150, 80, 100, 20);

        jScrollPaneMimeTypes.setViewportView(jListMimeTypes);

        add(jScrollPaneMimeTypes);
        jScrollPaneMimeTypes.setBounds(30, 110, 110, 160);

        jTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPaneTable.setViewportView(jTable);

        add(jScrollPaneTable);
        jScrollPaneTable.setBounds(140, 110, 380, 160);

        jButtonDelete.setBackground(new java.awt.Color(255, 204, 204));
        jButtonDelete.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButtonDelete.setText("Eliminar");
        jButtonDelete.setEnabled(false);
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });
        add(jButtonDelete);
        jButtonDelete.setBounds(440, 80, 80, 23);

        jButtonRefresh.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButtonRefresh.setText("Actualizar");
        jButtonRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRefreshActionPerformed(evt);
            }
        });
        add(jButtonRefresh);
        jButtonRefresh.setBounds(250, 80, 90, 23);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setText("Tipo de archivo:");
        add(jLabel1);
        jLabel1.setBounds(30, 80, 90, 16);

        jButtonPlay.setBackground(new java.awt.Color(204, 255, 204));
        jButtonPlay.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButtonPlay.setText("Reproducir");
        jButtonPlay.setEnabled(false);
        jButtonPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPlayActionPerformed(evt);
            }
        });
        add(jButtonPlay);
        jButtonPlay.setBounds(340, 80, 100, 23);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFilterActionPerformed
        applyFilters();
    }//GEN-LAST:event_jComboBoxFilterActionPerformed

    private void jButtonRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRefreshActionPerformed
        loadMediaFiles();
    }//GEN-LAST:event_jButtonRefreshActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        deleteSelectedFile();
    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jButtonPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPlayActionPerformed
        playSelectedFile();
    }//GEN-LAST:event_jButtonPlayActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonPlay;
    private javax.swing.JButton jButtonRefresh;
    private javax.swing.JComboBox<String> jComboBoxFilter;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelBuscar;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JList<String> jListMimeTypes;
    private javax.swing.JScrollPane jScrollPaneMimeTypes;
    private javax.swing.JScrollPane jScrollPaneTable;
    private javax.swing.JTable jTable;
    private javax.swing.JTextField jTextFieldBuscar;
    // End of variables declaration//GEN-END:variables
}
