package anglada.sensebrutor.vista;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
/**
 * Renderer personalitzat per mostrar l'estat d'un element dins una taula.
 * 
 * Assigna colors de fons i de text segons el valor de la columna d'estat:
 * LOCAL, REMOTO o AMBOS.
 * 
 * També centra el text i el posa en negreta per la columna d'estat.
 * 
 * @author Andreu
 * @version 1.0
 */
public class EstadoRenderer extends DefaultTableCellRenderer {
    /**
     * Retorna el component visual per a una cel·la de la taula,
     * aplicant estils segons l'estat del registre.
     * 
     * @param table taula on es mostra la cel·la
     * @param value valor de la cel·la actual
     * @param isSelected indica si la fila està seleccionada
     * @param hasFocus indica si la cel·la té el focus
     * @param row fila de la cel·la
     * @param column columna de la cel·la
     * @return component configurat amb el format corresponent
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        // Cridam al mètode pare per obtenir el component base
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        Object estadoObj = table.getValueAt(row, 4);
        String estado = (estadoObj != null) ? estadoObj.toString() : "";

        // Definim els colors segons l'estat (si no està seleccionat)
        if (!isSelected) {
            switch (estado) {
                case "LOCAL" -> {
                    c.setBackground(new Color(235, 245, 255)); // Blau suau
                    c.setForeground(new Color(0, 51, 153));   // Blau fosc
                }
                case "REMOTO" -> {
                    c.setBackground(new Color(255, 245, 225)); // Crema / taronja suau
                    c.setForeground(new Color(204, 102, 0));   // Taronja fosc
                }
                case "AMBOS" -> {
                    c.setBackground(new Color(230, 255, 230)); // Verd suau
                    c.setForeground(new Color(0, 102, 0));     // Verd fosc
                }
                default -> {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }
            }
        } else {
            // Si la fila està seleccionada, mantenim els colors per defecte
            c.setBackground(table.getSelectionBackground());
            c.setForeground(table.getSelectionForeground());
        }

        // Centram la columna d'estat i la destacam en negreta
        if (column == 4) {
            setHorizontalAlignment(SwingConstants.CENTER);
            c.setFont(c.getFont().deriveFont(Font.BOLD));
        } else {
            setHorizontalAlignment(SwingConstants.LEFT);
            c.setFont(c.getFont().deriveFont(Font.PLAIN));
        }

        return c;
    }
}