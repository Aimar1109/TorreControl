package gui;

import domain.AnalisisDatos;
import domain.Clima;
import domain.PaletaColor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class DialogoDetalleClima extends JDialog {

    private static final long serialVersionUID = 1L;
    private JTable tablaDatos;
    private ModeloTablaClima modeloTabla;
    private List<Clima> datosOriginales;

    public DialogoDetalleClima(JFrame parent, List<Clima> datos) {
        super(parent, "Detalle Horario y Análisis", true);
        this.datosOriginales = datos;
        
        setSize(850, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        JPanel panelHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelHeader.setBackground(PaletaColor.get(PaletaColor.PRIMARIO));
        JLabel lblTitulo = new JLabel("REGISTRO METEOROLÓGICO DETALLADO (24H)");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelHeader.add(lblTitulo);
        add(panelHeader, BorderLayout.NORTH);

        modeloTabla = new ModeloTablaClima(datos);
        tablaDatos = new JTable(modeloTabla);
        
        tablaDatos.setRowHeight(30);
        tablaDatos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JTableHeader header = tablaDatos.getTableHeader();
        header.setBackground(PaletaColor.get(PaletaColor.PRIMARIO));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setOpaque(true);
        
        tablaDatos.setSelectionBackground(PaletaColor.get(PaletaColor.SECUNDARIO));
        tablaDatos.setSelectionForeground(Color.WHITE);
        tablaDatos.setGridColor(new Color(230, 230, 230));
        
        tablaDatos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                
                if (isSelected) {
                    c.setForeground(Color.WHITE);
                } else {
                    c.setForeground(PaletaColor.get(PaletaColor.TEXTO));
                }

                if (column == 7) {
                    String val = (String) value;
                    if ("SÍ".equals(val)) {
                        c.setForeground(isSelected ? Color.WHITE : new Color(220, 53, 69)); // Rojo
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                    }
                }
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : PaletaColor.get(PaletaColor.FILA_ALT));
                }
                
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaDatos);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panelBotones.setBackground(Color.WHITE);

        JButton btnReset = crearBoton("Ordenar por Hora", PaletaColor.SECUNDARIO);
        JButton btnOrdenar = crearBoton("Ordenar por Temperatura", PaletaColor.ACENTO);
        JButton btnCerrar = crearBoton("Cerrar", PaletaColor.TEXTO_SUAVE);

        btnReset.addActionListener(e -> {
            modeloTabla.actualizarDatos(datosOriginales);
            tablaDatos.scrollRectToVisible(new Rectangle(tablaDatos.getCellRect(0, 0, true)));
        });

        btnOrdenar.addActionListener(e -> {
            List<Clima> ordenados = AnalisisDatos.ordenarPorTemperatura(datosOriginales);
            modeloTabla.actualizarDatos(ordenados);
            JOptionPane.showMessageDialog(this, "Datos ordenados usando QuickSort Recursivo.");
        });

        btnCerrar.addActionListener(e -> dispose());

        panelBotones.add(btnReset);
        panelBotones.add(btnOrdenar);
        panelBotones.add(btnCerrar);
        
        add(panelBotones, BorderLayout.SOUTH);
    }
    
    private JButton crearBoton(String texto, PaletaColor colorEnum) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(PaletaColor.get(colorEnum));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PaletaColor.get(colorEnum).darker(), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class ModeloTablaClima extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private List<Clima> datos;
        private final String[] columnas = {
            "Hora", "Temp (°C)", "Viento (km/h)", "Lluvia (mm)", 
            "Visibilidad", "Humedad %", "Presión", "Peligro"
        };

        public ModeloTablaClima(List<Clima> datos) {
            this.datos = datos;
        }

        public void actualizarDatos(List<Clima> nuevosDatos) {
            this.datos = nuevosDatos;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return datos.size(); }

        @Override
        public int getColumnCount() { return columnas.length; }
        
        @Override
        public String getColumnName(int column) { return columnas[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Clima c = datos.get(rowIndex);
            switch (columnIndex) {
                case 0: return String.format("%02d:00", c.getHora()); 
                case 1: return String.format("%.1f", c.getTemperatura());
                case 2: return String.format("%.1f", c.getVelocidadViento());
                case 3: return String.format("%.1f", c.getPrecipitacion());
                case 4: return String.format("%.1f", c.getVisibilidadKm());
                case 5: return String.format("%.0f", c.getHumedad());
                case 6: return String.format("%.0f", c.getPresionHPa());
                case 7: return c.isSenalPeligro() ? "SÍ" : "NO";
                default: return "-";
            }
        }
    }
}