package anglada.sensebrutor.vista;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Color;
import java.awt.Font;

public class EstadoRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        // Llamamos al super para obtener el componente base
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        Object estadoObj = table.getValueAt(row, 4);
        String estado = (estadoObj != null) ? estadoObj.toString() : "";

        // 2. Definimos los colores según el estado
        if (!isSelected) {
            switch (estado) {
                case "LOCAL" -> {
                    c.setBackground(new Color(235, 245, 255)); // Azul suave
                    c.setForeground(new Color(0, 51, 153));   // Azul oscuro
                }
                case "REMOTO" -> {
                    c.setBackground(new Color(255, 245, 225)); // Crema/Naranja muy suave
                    c.setForeground(new Color(204, 102, 0));   // Naranja oscuro (legible)
                }
                case "AMBOS" -> {
                    c.setBackground(new Color(230, 255, 230)); // Verde suave
                    c.setForeground(new Color(0, 102, 0));     // Verde oscuro
                }
                default -> {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }
            }
        } else {
            // Si la fila está seleccionada, mantenemos el color azul estándar de selección
            c.setBackground(table.getSelectionBackground());
            c.setForeground(table.getSelectionForeground());
        }

        // 3. Opcional: Centrar solo la columna del estado, las demás a la izquierda
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