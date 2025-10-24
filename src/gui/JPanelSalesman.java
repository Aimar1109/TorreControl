package gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

import domain.Vuelo;

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
    
    // ScrollPanes para gestionar el redimensionamiento
    private JScrollPane scrollVuelos;
    private JScrollPane scrollPasajeros;
    private JScrollPane scrollInfo;

    public JPanelSalesman(ArrayList<Vuelo> vuelos) {
        this.vuelos = vuelos;
        setLayout(new BorderLayout());
        
        // Construcción modular del panel
        initComponents();
        initTables();
        attachListeners();
        cargarVuelosReales();
    }
    
    // Crea y organiza los paneles y componentes visuales.
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
        panelCentral.setBorder(new EmptyBorder(5, 15, 10, 10));
        
        // -----------------
        // Panel izquierdo: tabla de vuelos 
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        
        // Creación de tabla (modelo asignado en initTables)
        tablaVuelos = new JTable();
        
        // Configurar scroll con políticas
        scrollVuelos = new JScrollPane(tablaVuelos);
        scrollVuelos.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollVuelos.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        panelIzquierdo.add(scrollVuelos, BorderLayout.CENTER);
        
        // -----------------
        // Panel derecho dividido en dos filas iguales
        JPanel panelDerecho = new JPanel(new GridLayout(2, 1, 5, 5)); // 2 filas, 1 columna, 5px de separación
        panelDerecho.setBorder(new EmptyBorder(0, 5, 0, 5));

        // Arriba: combo + tabla pasajeros/tripulación
        JPanel panelArriba = new JPanel(new BorderLayout());
        comboOpciones = new JComboBox<>(new String[] {"Pasajeros", "Tripulación"});
        comboOpciones.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value.toString());
            lbl.setHorizontalAlignment(JLabel.CENTER);
            if (isSelected) {
                lbl.setBackground(list.getSelectionBackground());
                lbl.setForeground(list.getSelectionForeground());
            } else {
                lbl.setBackground(list.getBackground());
                lbl.setForeground(list.getForeground());
            }
            lbl.setOpaque(true);
            return lbl;
        });
        panelArriba.add(comboOpciones, BorderLayout.NORTH);

        tablaPasajerosTripulacion = new JTable();
        
        // Configurar scroll con políticas
        scrollPasajeros = new JScrollPane(tablaPasajerosTripulacion);
        scrollPasajeros.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPasajeros.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        panelArriba.add(scrollPasajeros, BorderLayout.CENTER);

        // Abajo: tabla info vuelo
        tablaInfoVuelo = new JTable();
        
        // Configurar scroll con políticas
        scrollInfo = new JScrollPane(tablaInfoVuelo);
        scrollInfo.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollInfo.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Añadimos los dos paneles al panel derecho
        panelDerecho.add(panelArriba);
        panelDerecho.add(scrollInfo);
        
        // -----------------
        // JSplitPane para dividir izquierda/derecha de forma redimensionable
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdo, panelDerecho);
        splitPane.setDividerLocation(400); // Posición inicial del divisor (400px para la izquierda)
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true); // Botones para expandir/colapsar rápidamente
        
        panelCentral.add(splitPane, BorderLayout.CENTER);
        
        add(panelCentral, BorderLayout.CENTER);
    }
    
    // Inicializa las tablas con sus modelos y renderers
    private void initTables() {
        // -----------------
        // TABLA VUELOS
        String[] columnasVuelos = {"Código", "Origen", "Destino", "Duración", "Delayed"};
        modeloVuelos = new DefaultTableModel(columnasVuelos, 0);
        tablaVuelos.setModel(modeloVuelos);
        tablaVuelos.getTableHeader().setReorderingAllowed(false);
        tablaVuelos.getTableHeader().setResizingAllowed(false);
        
        // Tamaños de columnas tabla vuelos
        tablaVuelos.getColumnModel().getColumn(0).setPreferredWidth(80);   // Código
        tablaVuelos.getColumnModel().getColumn(1).setPreferredWidth(80);  // Origen
        tablaVuelos.getColumnModel().getColumn(2).setPreferredWidth(80);  // Destino
        tablaVuelos.getColumnModel().getColumn(3).setPreferredWidth(70);  // Duración
        tablaVuelos.getColumnModel().getColumn(4).setPreferredWidth(70);   // Delayed
        
        // Tamaño mínimo total de columnas tabla vuelos
        int anchoMinimoVuelos = 390; // = 400px
        tablaVuelos.setPreferredScrollableViewportSize(new Dimension(anchoMinimoVuelos, 0));
        
        // Listener para redimensionamiento tabla vuelos
        scrollVuelos.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int anchoDisponible = scrollVuelos.getViewport().getWidth();
                if (anchoDisponible >= anchoMinimoVuelos) {
                    tablaVuelos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                } else {
                    tablaVuelos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                }
            }
        });
        
        // -----------------
        // TABLA PASAJEROS/TRIPULACIÓN
        String[] columnasPasajeros = {"ID", "Nombre", "Asiento"};
        modeloPasajeros = new DefaultTableModel(columnasPasajeros, 0);
        tablaPasajerosTripulacion.setModel(modeloPasajeros);
        tablaPasajerosTripulacion.getTableHeader().setReorderingAllowed(false);
        tablaPasajerosTripulacion.getTableHeader().setResizingAllowed(false);
        
        // Tamaños de columnas tabla pasajeros
        tablaPasajerosTripulacion.getColumnModel().getColumn(0).setPreferredWidth(60);   // ID
        tablaPasajerosTripulacion.getColumnModel().getColumn(1).setPreferredWidth(180);  // Nombre
        tablaPasajerosTripulacion.getColumnModel().getColumn(2).setPreferredWidth(90);   // Asiento
        
        // Tamaño mínimo total de columnas tabla pasajeros
        int anchoMinimoPasajeros = 330; // = 330px
        tablaPasajerosTripulacion.setPreferredScrollableViewportSize(new Dimension(anchoMinimoPasajeros, 0));
        
        // Listener para redimensionamiento tabla pasajeros
        scrollPasajeros.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int anchoDisponible = scrollPasajeros.getViewport().getWidth();
                if (anchoDisponible >= anchoMinimoPasajeros) {
                    tablaPasajerosTripulacion.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                } else {
                    tablaPasajerosTripulacion.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                }
            }
        });

        // -----------------
        // TABLA INFO VUELO
        String[] columnasInfo = {"Campo", "Valor"};
        modeloInfoVuelo = new DefaultTableModel(columnasInfo, 0);
        tablaInfoVuelo.setModel(modeloInfoVuelo);
        tablaInfoVuelo.getTableHeader().setReorderingAllowed(false);
        tablaInfoVuelo.getTableHeader().setResizingAllowed(false);
        
        // Tamaños de columnas tabla info
        tablaInfoVuelo.getColumnModel().getColumn(0).setPreferredWidth(120);  // Campo
        tablaInfoVuelo.getColumnModel().getColumn(1).setPreferredWidth(220);  // Valor
        
        // Tamaño mínimo total de columnas tabla info
        int anchoMinimoInfo = 330; // = 370px
        tablaInfoVuelo.setPreferredScrollableViewportSize(new Dimension(anchoMinimoInfo, 0));
        
        // Listener para redimensionamiento tabla info
        scrollInfo.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int anchoDisponible = scrollInfo.getViewport().getWidth();
                if (anchoDisponible >= anchoMinimoInfo) {
                    tablaInfoVuelo.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                } else {
                    tablaInfoVuelo.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                }
            }
        });

        // -----------------
        // HEADER RENDERER (común para todas las tablas)
        TableCellRenderer headerRenderer = (table, value, isSelected, hasFocus, row, column) -> {
            JLabel lbl = new JLabel(value.toString(), JLabel.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 14));
            lbl.setOpaque(true);
            lbl.setBackground(new Color(200, 200, 200));
            lbl.setForeground(Color.BLACK);
            lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.DARK_GRAY));
            return lbl;
        };

        // CELL RENDERER (común para todas las tablas)
        TableCellRenderer cellRenderer = (table, value, isSelected, hasFocus, row, column) -> {
            JLabel lbl = new JLabel(value.toString());
            lbl.setOpaque(true);
            lbl.setFont(new Font("Arial", Font.PLAIN, 12));

            // Centrar ciertas columnas
            if (table == tablaVuelos && (column == 0 || column == 3 || column == 4)) {
                lbl.setHorizontalAlignment(JLabel.CENTER);
            } else if (table == tablaPasajerosTripulacion && (column == 0 || column == 2)) {
                lbl.setHorizontalAlignment(JLabel.CENTER);
            } else {
                lbl.setHorizontalAlignment(JLabel.LEFT);
            }

            // Colores alternados
            if (!isSelected) {
                if (row % 2 == 0) lbl.setBackground(new Color(245, 255, 245)); // verde claro
                else lbl.setBackground(new Color(255, 255, 255)); // blanco

                // Highlight delayed en tabla vuelos
                if (table == tablaVuelos) {
                    try {
                        Object delayedObj = table.getModel().getValueAt(row, 4);
                        int delayed = Integer.parseInt(delayedObj.toString());
                        if (delayed > 0) lbl.setBackground(new Color(255, 230, 230)); // rojo claro
                    } catch (Exception ex) {}
                }
            } else {
                lbl.setBackground(table.getSelectionBackground());
                lbl.setForeground(table.getSelectionForeground());
            }

            return lbl;
        };

        // -----------------
        // Aplicar renderers a todas las tablas
        tablaVuelos.getTableHeader().setDefaultRenderer(headerRenderer);
        tablaVuelos.setDefaultRenderer(Object.class, cellRenderer);
        tablaVuelos.getTableHeader().setPreferredSize(new Dimension(0, 30));
        tablaVuelos.setRowHeight(25);

        tablaPasajerosTripulacion.getTableHeader().setDefaultRenderer(headerRenderer);
        tablaPasajerosTripulacion.setDefaultRenderer(Object.class, cellRenderer);
        tablaPasajerosTripulacion.getTableHeader().setPreferredSize(new Dimension(0, 30));
        tablaPasajerosTripulacion.setRowHeight(25);

        tablaInfoVuelo.getTableHeader().setDefaultRenderer(headerRenderer);
        tablaInfoVuelo.setDefaultRenderer(Object.class, cellRenderer);
        tablaInfoVuelo.getTableHeader().setPreferredSize(new Dimension(0, 30));
        tablaInfoVuelo.setRowHeight(25);
    }

    // Lugar para enganchar listeners
    private void attachListeners() {
        // Listener para selección de vuelo
        tablaVuelos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaSeleccionada = tablaVuelos.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    Vuelo vueloSeleccionado = vuelos.get(filaSeleccionada);
                    cargarInfoVuelo(vueloSeleccionado);
                    cargarPasajerosTripulacion(vueloSeleccionado);
                }
            }
        });
        
        // Listener para cambiar entre Pasajeros y Tripulación
        comboOpciones.addActionListener(e -> {
            int filaSeleccionada = tablaVuelos.getSelectedRow();
            if (filaSeleccionada >= 0) {
                Vuelo vueloSeleccionado = vuelos.get(filaSeleccionada);
                cargarPasajerosTripulacion(vueloSeleccionado);
            }
        });
    }
    
    // Carga los vuelos reales en la tabla
    private void cargarVuelosReales() {
        // Limpiar tabla
        modeloVuelos.setRowCount(0);
        
        // Cargar vuelos reales
        for (Vuelo vuelo : vuelos) {
            String origen = vuelo.getOrigen().getCiudad();
            String destino = vuelo.getDestino().getCiudad();
            String duracion = formatearDuracion(vuelo.getDuracion());
            
            modeloVuelos.addRow(new Object[]{
                vuelo.getcodigo(),
                origen,
                destino,
                duracion,
                vuelo.getDelayed()
            });
        }
    }
    
    // Formatea la duración de minutos a formato "Xh Ym"
    private String formatearDuracion(int minutos) {
        int horas = minutos / 60;
        int mins = minutos % 60;
        return horas + "h " + mins + "m";
    }
    
    // Carga la información detallada del vuelo seleccionado
    private void cargarInfoVuelo(Vuelo vuelo) {
        modeloInfoVuelo.setRowCount(0);
        
        modeloInfoVuelo.addRow(new Object[]{"Código", vuelo.getcodigo()});
        modeloInfoVuelo.addRow(new Object[]{"Origen", vuelo.getOrigen().getCiudad()});
        modeloInfoVuelo.addRow(new Object[]{"Aeropuerto Origen", vuelo.getOrigen().getNombre()});
        modeloInfoVuelo.addRow(new Object[]{"Código Origen", vuelo.getOrigen().getCodigo()});
        modeloInfoVuelo.addRow(new Object[]{"Destino", vuelo.getDestino().getCiudad()});
        modeloInfoVuelo.addRow(new Object[]{"Aeropuerto Destino", vuelo.getDestino().getNombre()});
        modeloInfoVuelo.addRow(new Object[]{"Código Destino", vuelo.getDestino().getCodigo()});
        modeloInfoVuelo.addRow(new Object[]{"Duración", formatearDuracion(vuelo.getDuracion())});
        modeloInfoVuelo.addRow(new Object[]{"Retraso", vuelo.getDelayed() > 0 ? vuelo.getDelayed() + " min" : "Sin retraso"});
        modeloInfoVuelo.addRow(new Object[]{"Estado", vuelo.isEstado() ? "Activo" : "Cancelado"});
        modeloInfoVuelo.addRow(new Object[]{"Emergencia", vuelo.isEmergencia() ? "SÍ" : "NO"});
        modeloInfoVuelo.addRow(new Object[]{"Modelo Avión", vuelo.getAvion().getModelo()});
        modeloInfoVuelo.addRow(new Object[]{"Matrícula", vuelo.getAvion().getMatricula()});
        modeloInfoVuelo.addRow(new Object[]{"Capacidad Avión", vuelo.getAvion().getCapacidad() + " pasajeros"});
        modeloInfoVuelo.addRow(new Object[]{"Total Pasajeros", vuelo.getPasajeros().size()});
        modeloInfoVuelo.addRow(new Object[]{"Total Tripulación", vuelo.getTripulacion().size()});
    }
    
    // Carga los pasajeros o tripulación según el combo seleccionado
    private void cargarPasajerosTripulacion(Vuelo vuelo) {
        modeloPasajeros.setRowCount(0);
        
        String seleccion = (String) comboOpciones.getSelectedItem();
        
        if ("Pasajeros".equals(seleccion)) {
            ArrayList<String> pasajeros = vuelo.getPasajeros();
            for (int i = 0; i < pasajeros.size(); i++) {
                String asiento = generarAsiento(i);
                modeloPasajeros.addRow(new Object[]{i + 1, pasajeros.get(i), asiento});
            }
        } else {
            ArrayList<String> tripulacion = vuelo.getTripulacion();
            String[] roles = {"Piloto", "Copiloto", "Jefe Cabina", "Auxiliar", "Auxiliar", "Auxiliar", "Auxiliar", "Auxiliar"};
            for (int i = 0; i < tripulacion.size(); i++) {
                String rol = i < roles.length ? roles[i] : "Auxiliar";
                modeloPasajeros.addRow(new Object[]{i + 1, tripulacion.get(i), rol});
            }
        }
    }
    
    // Genera un asiento aleatorio (ej: 12A, 15C)
    private String generarAsiento(int index) {
        char[] letras = {'A', 'B', 'C', 'D', 'E', 'F'};
        int fila = (index / 6) + 1;
        char columna = letras[index % 6];
        return fila + "" + columna;
    }
 }