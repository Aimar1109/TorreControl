package gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import domain.Vuelo;

//	JPanelSalesman - initComponents(),initTables(),attachListeners()

public class JPanelSalesman extends JPanel {

    private static final long serialVersionUID = 1L;
    
    private ArrayList<Vuelo> vuelos;
    
    // Componentes visuales
    private JTable tablaVuelos;
    private JTable tablaPasajerosTripulacion;
    private JTable tablaInfoVuelo;
    private JComboBox<String> comboOpciones;
    
    // Modelos
    private DefaultTableModel modeloVuelos;
    private DefaultTableModel modeloPasajeros;
    private DefaultTableModel modeloInfoVuelo;

    public JPanelSalesman(ArrayList<Vuelo> vuelos) {
        this.vuelos = vuelos;
        setLayout(new BorderLayout());
        
        // Construcción modular del panel
        initComponents();
        initTables();
        attachListeners();
        
        // refreshVuelos() se añadirá en un commit posterior cuando queramos rellenar modeloVuelos desde la lista 'vuelos'.
    }
    
    //	Crea y organiza los paneles y componentes visuales.


    private void initComponents() {
        // -----------------
        // Panel superior con título
        JPanel panelSuperior = new JPanel();
        JLabel lblTitulo = new JLabel("Panel Salesman");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        panelSuperior.add(lblTitulo);
        add(panelSuperior, BorderLayout.NORTH);
        
        // Panel central con margen
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // -----------------
        // Panel izquierdo: tabla de vuelos (placeholder por ahora)
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelCentral.setBorder(new EmptyBorder(10, 15, 10, 10));
        
        // Creación de tabla/scroll (modelo asignado en initTables)
        tablaVuelos = new JTable();
        JScrollPane scrollVuelos = new JScrollPane(tablaVuelos);
        panelIzquierdo.add(scrollVuelos, BorderLayout.CENTER);
        panelIzquierdo.setPreferredSize(new Dimension(375,0));
        panelCentral.add(panelIzquierdo, BorderLayout.WEST);
        
        // -----------------
        // Panel derecho dividido en dos filas iguales
        JPanel panelDerecho = new JPanel(new GridLayout(2, 1, 5, 5)); // 2 filas, 1 columna, 5px de separación
        panelDerecho.setBorder(new EmptyBorder(0,5,0,5));

        // Arriba: combo + tabla pasajeros/tripulación
        JPanel panelArriba = new JPanel(new BorderLayout());
        comboOpciones = new JComboBox<>(new String[] {"Pasajeros", "Tripulación"});
        panelArriba.add(comboOpciones, BorderLayout.NORTH);

        tablaPasajerosTripulacion = new JTable();
        JScrollPane scrollPasajeros = new JScrollPane(tablaPasajerosTripulacion);
        panelArriba.add(scrollPasajeros, BorderLayout.CENTER);

        // Abajo: tabla info vuelo
        tablaInfoVuelo = new JTable();
        JScrollPane scrollInfo = new JScrollPane(tablaInfoVuelo);

        // Añadimos los dos paneles al panel derecho
        panelDerecho.add(panelArriba);
        panelDerecho.add(scrollInfo);
        
        panelCentral.add(panelDerecho, BorderLayout.CENTER);
        
        add(panelCentral, BorderLayout.CENTER);
    }
    
    //	 Crea y asigna los DefaultTableModel a cada JTable. Solo los headers por ahora.
    private void initTables() {
        // -----------------
        // Tabla Vuelos
        String[] columnasVuelos = {"Código", "Origen", "Destino", "Duración", "Delayed"};
        modeloVuelos = new DefaultTableModel(columnasVuelos, 0);
        tablaVuelos.setModel(modeloVuelos);
        tablaVuelos.getTableHeader().setReorderingAllowed(false);

        // Tabla Pasajeros/Tripulación
        String[] columnasPasajeros = {"ID", "Nombre", "Asiento"};
        modeloPasajeros = new DefaultTableModel(columnasPasajeros, 0);
        tablaPasajerosTripulacion.setModel(modeloPasajeros);
        tablaPasajerosTripulacion.getTableHeader().setReorderingAllowed(false);

        // Tabla Info Vuelo
        String[] columnasInfo = {"Campo", "Valor"};
        modeloInfoVuelo = new DefaultTableModel(columnasInfo, 0);
        tablaInfoVuelo.setModel(modeloInfoVuelo);
        tablaInfoVuelo.getTableHeader().setReorderingAllowed(false);

        // -----------------
        // HEADER RENDERER
        TableCellRenderer headerRenderer = (table, value, isSelected, hasFocus, row, column) -> {
            JLabel lbl = new JLabel(value.toString(), JLabel.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 14));
            lbl.setOpaque(true);
            lbl.setBackground(new Color(200, 200, 200));
            lbl.setForeground(Color.BLACK);
            lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.DARK_GRAY));
            return lbl;
        };

        // CELL RENDERER
        TableCellRenderer cellRenderer = (table, value, isSelected, hasFocus, row, column) -> {
            JLabel lbl = new JLabel(value.toString());
            lbl.setOpaque(true);
            lbl.setFont(new Font("Arial", Font.PLAIN, 12));

            // Centrar ciertas columnas
            if (table == tablaVuelos && (column == 0 || column == 3 || column == 4)) {
                lbl.setHorizontalAlignment(JLabel.CENTER);
            } else {
                lbl.setHorizontalAlignment(JLabel.LEFT);
            }

            // Colores alternados
            if (!isSelected) {
                if (row % 2 == 0) lbl.setBackground(new Color(245, 255, 245)); // verde
                else lbl.setBackground(new Color(255, 255, 255)); // blanco

                // Highlight delayed
                if (table == tablaVuelos) {
                    try {
                        Object delayedObj = table.getModel().getValueAt(row, 4);
                        int delayed = Integer.parseInt(delayedObj.toString());
                        if (delayed > 0) lbl.setBackground(new Color(255, 230, 230)); // rojo
                    } catch (Exception ex) {}
                }
            } else {
                lbl.setBackground(table.getSelectionBackground());
                lbl.setForeground(table.getSelectionForeground());
            }

            return lbl;
        };

        // -----------------
        // Aplicar renderers
        tablaVuelos.getTableHeader().setDefaultRenderer(headerRenderer);
        tablaVuelos.setDefaultRenderer(Object.class, cellRenderer);

        tablaPasajerosTripulacion.getTableHeader().setDefaultRenderer(headerRenderer);
        tablaPasajerosTripulacion.setDefaultRenderer(Object.class, cellRenderer);

        tablaInfoVuelo.getTableHeader().setDefaultRenderer(headerRenderer);
        tablaInfoVuelo.setDefaultRenderer(Object.class, cellRenderer);

        // -----------------
        // FILAS DE PRUEBA
        modeloVuelos.addRow(new Object[]{1001, "Bilbao", "Madrid", "2h 15m", 0});
        modeloVuelos.addRow(new Object[]{1002, "Bilbao", "Barcelona", "1h 50m", 15});

        modeloPasajeros.addRow(new Object[]{1, "Juan Pérez", "12A"});
        modeloPasajeros.addRow(new Object[]{2, "Ana López", "12B"});

        modeloInfoVuelo.addRow(new Object[]{"Piloto", "Carlos Ruiz"});
        modeloInfoVuelo.addRow(new Object[]{"Puerta", "A12"});
    }

    
    //	Lugar para enganchar listeners.
    private void attachListeners() {
    	
    }
    
}

