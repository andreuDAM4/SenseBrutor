package anglada.sensebrutor.vista;

import anglada.dimedianetpollingcomponent.DIMediaNetPollingComponent;
import anglada.dimedianetpollingcomponent.events.EventNousFitxers;
import anglada.dimedianetpollingcomponent.events.ListenerNousFitxers;
import anglada.dimedianetpollingcomponent.model.Media;
import anglada.sensebrutor.SenseBrutor;
import anglada.sensebrutor.model.MediaFileModel;
import anglada.sensebrutor.model.UnifiedMediaModel;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;

/**
 * Panel sempre visible que mostra els arxius de dins una carpeta on aniran les descargues.
 * Llista tots els arxius depenent d'els filtres seleccionats.
 * Permet cercar per nom amb temps real.
 * @author Andreu
 */
public class MediaFilePanel extends javax.swing.JPanel {

    private final SenseBrutor mainFrame;
    private final DIMediaNetPollingComponent diComponent;
    private final List<UnifiedMediaModel> allMedia;
    private final List<UnifiedMediaModel> filteredMedia;
    
    private MediaTableModel tableModel;
    private DefaultListModel<String> mimeListModel;
    private DefaultComboBoxModel<String> filterModel;
    
    public MediaFilePanel(SenseBrutor mainFrame) {
        this.mainFrame = mainFrame;
        this.diComponent = mainFrame.getDiMediaPolling();

        initComponents();
        allMedia = new ArrayList<>();
        filteredMedia = new ArrayList<>();
        jProgressBar.setVisible(false); 
        setupModels();
        setupListeners();
        // Afegim un listener al polling per actualitzar la taula automàticament
        diComponent.afegirListenerNousFitxers(new ListenerNousFitxers() {
            @Override
            public void hiHaNousFitxers(EventNousFitxers evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        carregarDades();
                    }
                });
            }
        });
        carregarDades();
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
        filterModel.addElement("Local");
        filterModel.addElement("Remoto");
        filterModel.addElement("Ambos");
        jComboBoxFilter.setModel(filterModel);
        // Columna 4 es "Estado"
        // Aplicar el renderer a TODAS las columnas para que toda la fila se pinte
        for (int i = 0; i < jTable.getColumnCount(); i++) {
            jTable.getColumnModel().getColumn(i).setCellRenderer(new EstadoRenderer());
        }
        jButtonPlay.setContentAreaFilled(true);
        jButtonPlay.setOpaque(true);
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
                int selectedRow = jTable.getSelectedRow();
                if (selectedRow == -1) {
                    jButtonPlay.setEnabled(false);
                    jButtonDelete.setEnabled(false);
                    return;
                }

                UnifiedMediaModel media = filteredMedia.get(selectedRow);
                //Actualitzacio de lu que apareix al boto segons l'estat
                switch (media.getEstado()) {
                    case LOCAL -> {
                        jButtonPlay.setText("Subir");
                        jButtonPlay.setEnabled(true);
                        jButtonDelete.setEnabled(true);
                        jButtonPlay.setBackground(new java.awt.Color(255, 165, 0)); 
                        jButtonPlay.setForeground(java.awt.Color.BLACK);
                    }
                    case REMOTO -> {
                        jButtonPlay.setText("Descargar");
                        jButtonPlay.setEnabled(true);
                        jButtonDelete.setEnabled(false);
                        jButtonPlay.setBackground(new java.awt.Color(173, 216, 230));
                        jButtonPlay.setForeground(java.awt.Color.BLACK);
                    }
                    case AMBOS -> {
                        jButtonPlay.setText("Reproducir");
                        jButtonPlay.setEnabled(true);
                        jButtonDelete.setEnabled(true);
                        jButtonPlay.setBackground(new java.awt.Color(204, 255, 204));
                        jButtonPlay.setForeground(java.awt.Color.BLACK);
                    }
                }
            }
        });
    }
    
    // Actualiza la llista de tipus MIME disponibles a la interfaç
    // Recorre tots els fitxers, obte tipus, ordena y elimina duplicats
    private void actualizarMimeList() {
        mimeListModel.clear();

        // Empram un conjunt per guardar tipus MIME sense repetir i ordenats
        Set<String> mimes = new TreeSet<>();

        // Recorrem tots els arxius de sa biblio
        for (UnifiedMediaModel media : allMedia) {
            String tipo = media.getMime(); // Agafam tipus mime de fitxer
            // Agregam els que no siguin nulls ni en blanc
            if (tipo != null && !tipo.isBlank()) {
                mimes.add(tipo);
            }
        }

        for (String mime : mimes) {
            mimeListModel.addElement(mime);
        }
    }
    
    //Carrega sa biblioteca de medis a l'hora de seleccionar una carpeta de sortida
    public void reloadIfConfigured() {
        String path = mainFrame.getPreferencesPanel().getDownloadPath();
        if (path != null && !path.isEmpty()) {
            File folder = new File(path);
            if (folder.exists() && folder.isDirectory()) {
                carregarDades();
            }
        }
    } 

    /**
    * Funció per mostrar els arxius a la taula.
    * Carrega tots els arxius de manera unificada, combinant arxius locals i remots.
    */
    private void carregarDades() {
        allMedia.clear();

        // Mapa temporal per unir arxius locals i remots usant el nom com a clau
        Map<String, UnifiedMediaModel> mapa = new HashMap<>();

        //CARREGAR ARXIUS LOCALS
        String rutaDescargas = mainFrame.getPreferencesPanel().getDownloadPath();
        if (rutaDescargas != null && !rutaDescargas.isEmpty()) {
            File carpeta = new File(rutaDescargas);
            File[] totsArxius = carpeta.listFiles(); // Obté tots els fitxers de la carpeta

            if (totsArxius != null) {
                for (File arxiu : totsArxius) {
                    // Comprovar que és un fitxer i no una carpeta
                    if (arxiu.isFile()) {
                        // Crear model del fitxer local
                        MediaFileModel modelLocal = new MediaFileModel(arxiu);

                        // Crear objecte unificat
                        UnifiedMediaModel mediaLocal = new UnifiedMediaModel();
                        mediaLocal.setNombre(modelLocal.getName());
                        mediaLocal.setSize(modelLocal.getSize());
                        mediaLocal.setMime(modelLocal.getMimeType());
                        mediaLocal.setDate(modelLocal.getDate());
                        mediaLocal.setLocalFile(arxiu);
                        mediaLocal.setEstado(UnifiedMediaModel.Estado.LOCAL);

                        // Guardar al mapa amb el nom com a clau
                        mapa.put(mediaLocal.getNombre(), mediaLocal);
                    }
                }
            }
        }

        //CARREGAR ARXIUS REMOTS
        try {
            List<Media> arxiusRemots = diComponent.obtenirTotsElsFitxers();

            for (Media arxiuRemot : arxiusRemots) {
                // Comprovar si ja existeix un fitxer local amb el mateix nom
                UnifiedMediaModel media = mapa.get(arxiuRemot.mediaFileName);

                if (media != null && media.getLocalFile() != null && media.getLocalFile().exists()) {
                    // Ja existeix fitxer local: actualitzar informació remota i estat
                    media.setRemoteMedia(arxiuRemot);
                    media.setEstado(UnifiedMediaModel.Estado.AMBOS);
                } else {
                    // No existeix fitxer local: crear objecte només amb informació remota
                    UnifiedMediaModel mediaRemot = new UnifiedMediaModel();
                    mediaRemot.setNombre(arxiuRemot.mediaFileName);
                    mediaRemot.setMime(arxiuRemot.mediaMimeType);
                    mediaRemot.setRemoteMedia(arxiuRemot);
                    mediaRemot.setEstado(UnifiedMediaModel.Estado.REMOTO);

                    // Guardar al mapa
                    mapa.put(mediaRemot.getNombre(), mediaRemot);
                }
            }
        } catch (Exception e) {
            // Pot passar si no hi ha carpeta local o fallada de xarxa; no crític
            System.err.println("Avíso de red (no crítico): " + e.getMessage());
        }

        //Afegir tots els arxius del mapa a la llista principal
        allMedia.addAll(mapa.values());

        // Actualitzar la llista de tipus MIME per a filtres
        actualizarMimeList();

        // Aplicar filtres actius si n’hi ha
        applyFilters();
    }

    /**
     * Funció per filtrar arxius segons la recerca, estat i tipus MIME seleccionats
     */
    private void applyFilters() {
        //Obtenir els filtres seleccionats per l'usuari
        String textBusqueda = jTextFieldBuscar.getText().toLowerCase();
        String filtroEstado = (String) jComboBoxFilter.getSelectedItem();
        String filtroMime = jListMimeTypes.getSelectedValue();

        //Netejar la llista de resultats filtrats
        filteredMedia.clear();

        //Recórrer tots els arxius i aplicar filtres
        for (UnifiedMediaModel media : allMedia) {
            // Filtrar per nom (recerca)
            if (!media.getNombre().toLowerCase().contains(textBusqueda)) continue;

            // Filtrar per estat (Tots, LOCAL, REMOT, AMBOS)
            if (!"Todos".equals(filtroEstado) && !media.getEstado().name().equalsIgnoreCase(filtroEstado)) continue;

            // Filtrar per tipus MIME
            if (filtroMime != null && !filtroMime.equals(media.getMime())) continue;

            // Si passa tots els filtres, afegir a la llista filtrada
            filteredMedia.add(media);
        }

        //Actualitzar la taula amb els resultats filtrats
        tableModel.updateData(filteredMedia);
    }

    /**
     * Funció per eliminar l'arxiu seleccionat de la taula i del disc
     */
    private void deleteSelectedFile() {
        int filaSeleccionada = jTable.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un archivo de la tabla para eliminar.");
            return;
        }

        UnifiedMediaModel media = filteredMedia.get(filaSeleccionada);

        // No existeix localment
        if (media.getLocalFile() == null || !media.getLocalFile().exists()) {
            JOptionPane.showMessageDialog(this,
                    "Este archivo no existe localmente.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar el archivo local '" + media.getNombre() + "'?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean eliminat = media.getLocalFile().delete();

            if (eliminat) {
                JOptionPane.showMessageDialog(this,
                        "Archivo eliminado correctamente.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se ha podido eliminar el archivo.");
            }

            // Tornar a carregar dades per actualitzar estats
            carregarDades();
        }
    }

    /**
     * Funció per descarregar l'arxiu seleccionat
     */
    private void downloadSelectedFile() {
        if (!validarCarpetaDescarga()) return;
        int filaSeleccionada = jTable.getSelectedRow();
        if (filaSeleccionada == -1) return;

        UnifiedMediaModel media = filteredMedia.get(filaSeleccionada);

        toggleLoading(true); // Solo pasamos el boolean

        new Thread(() -> {
            try {
                String rutaDescarga = mainFrame.getPreferencesPanel().getDownloadPath();
                File desti = new File(rutaDescarga, media.getNombre());

                diComponent.descarregar(media.getRemoteMedia().id, desti);

                SwingUtilities.invokeLater(() -> {
                    toggleLoading(false);
                    JOptionPane.showMessageDialog(this, "Archivo '" + media.getNombre() + "' descargado con éxito.");
                    carregarDades(); 
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    toggleLoading(false);
                    JOptionPane.showMessageDialog(this, "Error en la descarga: " + e.getMessage());
                });
            }
        }).start();
    }
    private void toggleLoading(boolean active) {
        SwingUtilities.invokeLater(() -> {
            jProgressBar.setVisible(active);
            jProgressBar.setIndeterminate(active);

            // Deshabilitamos controles para evitar clicks accidentales durante la operación
            jButtonPlay.setEnabled(!active);
            jButtonDelete.setEnabled(!active);
            jButtonRefresh.setEnabled(!active);
            jTable.setEnabled(!active);
        });
    }

    /**
     * Funció per pujar l'arxiu seleccionat al servidor
     */
    private void uploadSelectedFile() {
        int filaSeleccionada = jTable.getSelectedRow();
        if (filaSeleccionada == -1) return;

        UnifiedMediaModel media = filteredMedia.get(filaSeleccionada);
        File fitxerLocal = media.getLocalFile();

        if (fitxerLocal == null || !fitxerLocal.exists()) {
            JOptionPane.showMessageDialog(this, "El archivo local no existe.");
            return;
        }

        toggleLoading(true); // Activa la barra

        new Thread(() -> {
            try {
                diComponent.pujarFitxer(fitxerLocal, "/");

                SwingUtilities.invokeLater(() -> {
                    toggleLoading(false); // Apaga la barra
                    JOptionPane.showMessageDialog(this, "Archivo '" + media.getNombre() + "' subido correctamente.");
                    carregarDades();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    toggleLoading(false);
                    JOptionPane.showMessageDialog(this, "Error en la subida: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Funció per reproduir un arxiu seleccionat
     */
    private void playSelectedFile() {
        int filaSeleccionada = jTable.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un archivo de la tabla para reproducir.");
            return;
        }

        UnifiedMediaModel media = filteredMedia.get(filaSeleccionada);

        // Només es pot reproduir si existeix localment
        if (media.getLocalFile() == null || !media.getLocalFile().exists()) {
            JOptionPane.showMessageDialog(this,
                "Este archivo no existe localmente y no se puede reproducir.");
            return;
        }

        try {
            java.awt.Desktop.getDesktop().open(media.getLocalFile());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "No se ha podido abrir el archivo: " + ex.getMessage());
        }
    }

    /**
     * Classe interna per personalitzar la taula de fitxers
     */
    private class MediaTableModel extends AbstractTableModel {
        private List<UnifiedMediaModel> data = new ArrayList<>();
        private final String[] cols = { "Nombre", "Tamaño", "MIME", "Fecha", "Estado" };

        // Actualitzar dades de la taula
        public void updateData(List<UnifiedMediaModel> newData) {
            this.data = new ArrayList<>(newData);
            fireTableDataChanged(); // Notificar la taula que ha canviat
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }

        @Override
        public Object getValueAt(int fila, int columna) {
            UnifiedMediaModel media = data.get(fila);

            // Decidir què mostrar segons la columna
            switch (columna) {
                case 0:
                    // Nom
                    return media.getNombre();
                case 1:
                    // Tamany
                    if (media.getSize() > 0) return formatSize(media.getSize());
                    else return "";
                case 2:
                    // MIME
                    return media.getMime();
                case 3:
                    // Data
                    if (media.getDate() != null)
                        return media.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"));
                    else return "";
                case 4:
                    // Estat
                    return media.getEstado().name();
                default:
                    return null;
            }
        }
    }
    //Controla l'error de si no tenim a preferences panel la ruta de la carpeta descarregues
    private boolean validarCarpetaDescarga() {
        String rutaDescarga = mainFrame.getPreferencesPanel().getDownloadPath();

        if (rutaDescarga == null || rutaDescarga.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No se ha definido la carpeta de descarga en Preferencias.",
                "Carpeta no definida",
                JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        File carpeta = new File(rutaDescarga);
        if (!carpeta.exists()) {
            boolean creada = carpeta.mkdirs(); // intenta crear la carpeta
            if (!creada) {
                JOptionPane.showMessageDialog(
                    this,
                    "No se pudo crear la carpeta de descarga: " + rutaDescarga,
                    "Error de carpeta",
                    JOptionPane.ERROR_MESSAGE
                );
                return false;
            }
        } else if (!carpeta.isDirectory()) {
            JOptionPane.showMessageDialog(
                this,
                "La ruta de descarga no es un directorio válido: " + rutaDescarga,
                "Error de carpeta",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        return true;
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
        jButtonPlay = new javax.swing.JButton();
        jProgressBar = new javax.swing.JProgressBar();

        setName("MediaFilePanel"); // NOI18N
        setLayout(null);

        jLabelTitle.setFont(new java.awt.Font("Segoe UI", 1, 32)); // NOI18N
        jLabelTitle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/multimedia.png"))); // NOI18N
        jLabelTitle.setText("Biblioteca de Medios");
        add(jLabelTitle);
        jLabelTitle.setBounds(80, 0, 400, 40);

        jLabelBuscar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabelBuscar.setText("Buscar por nombre:");
        add(jLabelBuscar);
        jLabelBuscar.setBounds(20, 50, 110, 16);

        jTextFieldBuscar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jTextFieldBuscar.setToolTipText("Buscar por nombre...");
        add(jTextFieldBuscar);
        jTextFieldBuscar.setBounds(150, 50, 370, 22);

        jComboBoxFilter.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jComboBoxFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFilterActionPerformed(evt);
            }
        });
        add(jComboBoxFilter);
        jComboBoxFilter.setBounds(20, 80, 120, 20);

        jListMimeTypes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPaneMimeTypes.setViewportView(jListMimeTypes);

        add(jScrollPaneMimeTypes);
        jScrollPaneMimeTypes.setBounds(20, 110, 120, 220);

        jTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPaneTable.setViewportView(jTable);

        add(jScrollPaneTable);
        jScrollPaneTable.setBounds(150, 110, 370, 220);

        jButtonDelete.setBackground(new java.awt.Color(255, 204, 204));
        jButtonDelete.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButtonDelete.setText("Eliminar");
        jButtonDelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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
        jButtonRefresh.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRefreshActionPerformed(evt);
            }
        });
        add(jButtonRefresh);
        jButtonRefresh.setBounds(150, 80, 90, 23);

        jButtonPlay.setBackground(new java.awt.Color(204, 255, 204));
        jButtonPlay.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButtonPlay.setText("Reproducir");
        jButtonPlay.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonPlay.setEnabled(false);
        jButtonPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPlayActionPerformed(evt);
            }
        });
        add(jButtonPlay);
        jButtonPlay.setBounds(280, 80, 100, 23);
        add(jProgressBar);
        jProgressBar.setBounds(20, 340, 500, 20);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFilterActionPerformed
        applyFilters();
    }//GEN-LAST:event_jComboBoxFilterActionPerformed

    private void jButtonRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRefreshActionPerformed
        carregarDades();
    }//GEN-LAST:event_jButtonRefreshActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        deleteSelectedFile();
    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jButtonPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPlayActionPerformed
        int selectedRow = jTable.getSelectedRow();
        if (selectedRow == -1) return;

        UnifiedMediaModel media = filteredMedia.get(selectedRow);

        switch (media.getEstado()) {
            case LOCAL -> uploadSelectedFile();
            case REMOTO -> downloadSelectedFile();
            case AMBOS -> playSelectedFile();
        }
    }//GEN-LAST:event_jButtonPlayActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonPlay;
    private javax.swing.JButton jButtonRefresh;
    private javax.swing.JComboBox<String> jComboBoxFilter;
    private javax.swing.JLabel jLabelBuscar;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JList<String> jListMimeTypes;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JScrollPane jScrollPaneMimeTypes;
    private javax.swing.JScrollPane jScrollPaneTable;
    private javax.swing.JTable jTable;
    private javax.swing.JTextField jTextFieldBuscar;
    // End of variables declaration//GEN-END:variables
}
