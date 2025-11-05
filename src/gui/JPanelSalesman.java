package gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import java.util.ArrayList;
import java.util.HashMap; // Necesario para mapear asientos a Tooltips
import java.util.Map;     // Necesario para el mapa de Tooltips

import domain.Vuelo;

public class JPanelSalesman extends JPanel {

    private static final long serialVersionUID = 1L;

    
    private ArrayList<Vuelo> vuelos;
    
    // Componentes visuales
    private JTable tablaVuelos;
    private JTable tablaDinamica;
    private JToggleButton btnPasajeros;
    private JToggleButton btnTripulacion;
    private JToggleButton btnInfoVuelo;
    private JToggleButton btnSeatmap;
    private ButtonGroup buttonGroup;
    private JPanel panelDinamico;
    private JPanel panelSeatmap; // Panel que contendrá el mapa de asientos
    private JScrollPane scrollDinamico;
    private JPanel panelInferior;
    
    // Modelos
    private DefaultTableModel modeloVuelos;
    private DefaultTableModel modeloDinamico;
    
    // ScrollPanes
    private JScrollPane scrollVuelos;

    public JPanelSalesman(ArrayList<Vuelo> vuelos) {
        this.vuelos = vuelos;
        setLayout(new BorderLayout());
        
        initComponents();
        initTables();
        attachListeners();
        cargarVuelosReales();
    }
    
    private void initComponents() {
        // Panel superior
        JPanel panelSuperior = new JPanel();
        JLabel lblTitulo = new JLabel("Panel Salesman");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        panelSuperior.add(lblTitulo);
        add(panelSuperior, BorderLayout.NORTH);
        
        // Panel central
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(new EmptyBorder(5, 15, 5, 10));
        
        // -----------------
        // PANEL IZQUIERDO (Tabla de Vuelos)
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        tablaVuelos = new JTable();
        scrollVuelos = new JScrollPane(tablaVuelos);
        scrollVuelos.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollVuelos.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panelIzquierdo.add(scrollVuelos, BorderLayout.CENTER);
        
        // -----------------
        // PANEL DERECHO
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setBorder(new EmptyBorder(0, 5, 0, 5));

        // Panel con botones toggle
        JPanel panelBotones = new JPanel(new GridLayout(1, 4, 0, 0));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Crear botones toggle
        btnPasajeros = new JToggleButton("Pasajeros");
        btnTripulacion = new JToggleButton("Tripulación");
        btnInfoVuelo = new JToggleButton("Info Vuelo");
        btnSeatmap = new JToggleButton("Seatmap");
        
        // Estilo de los botones
        estilizarBotonToggle(btnPasajeros);
        estilizarBotonToggle(btnTripulacion);
        estilizarBotonToggle(btnInfoVuelo);
        estilizarBotonToggle(btnSeatmap);
        
        // Agrupar botones
        buttonGroup = new ButtonGroup();
        buttonGroup.add(btnPasajeros);
        buttonGroup.add(btnTripulacion);
        buttonGroup.add(btnInfoVuelo);
        buttonGroup.add(btnSeatmap);
        
        // Seleccionar Pasajeros por defecto
        btnPasajeros.setSelected(true);
        
        // Añadir botones al panel
        panelBotones.add(btnPasajeros);
        panelBotones.add(btnTripulacion);
        panelBotones.add(btnInfoVuelo);
        panelBotones.add(btnSeatmap);
        
        panelDerecho.add(panelBotones, BorderLayout.NORTH);

        // Panel dinámico que cambia entre tabla y seatmap
        panelDinamico = new JPanel(new BorderLayout());
        
        // Tabla dinámica (para pasajeros, tripulación, info)
        tablaDinamica = new JTable();
        scrollDinamico = new JScrollPane(tablaDinamica);
        scrollDinamico.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollDinamico.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Inicialización del panel Seatmap. El contenido se generará al seleccionar la pestaña.
        panelSeatmap = new JPanel(new BorderLayout());
        panelSeatmap.setBackground(new Color(245, 245, 245));
        panelSeatmap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Inicialmente mostrar tabla
        panelDinamico.add(scrollDinamico, BorderLayout.CENTER);
        
        panelDerecho.add(panelDinamico, BorderLayout.CENTER);
        
        // -----------------
        // JSplitPane HORIZONTAL para dividir izquierda/derecha
        JSplitPane splitPaneHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdo, panelDerecho);
        splitPaneHorizontal.setDividerLocation(575);
        splitPaneHorizontal.setResizeWeight(0.5);
        splitPaneHorizontal.setOneTouchExpandable(true);
        
        panelCentral.add(splitPaneHorizontal, BorderLayout.CENTER);
        
        // -----------------
        // PANEL INFERIOR: Progress Bar / Threads / Timeline de vuelos
        panelInferior = new JPanel(new BorderLayout());
        panelInferior.setPreferredSize(new Dimension(0, 250));
        panelInferior.setBackground(new Color(245, 245, 245));
        panelInferior.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel lblTimeline = new JLabel("Timeline de Despegues y Aterrizajes", JLabel.CENTER);
        lblTimeline.setFont(new Font("Arial", Font.BOLD, 14));
        panelInferior.add(lblTimeline, BorderLayout.NORTH);
        
        // Panel para los cuadrados animados
        JPanel panelAnimacion = new JPanel();
        panelAnimacion.setBackground(Color.WHITE);
        panelAnimacion.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        JLabel lblPlaceholder = new JLabel("Espacio para animación de vuelos (threads/timer)", JLabel.CENTER);
        lblPlaceholder.setForeground(Color.GRAY);
        lblPlaceholder.setFont(new Font("Arial", Font.ITALIC, 12));
        panelAnimacion.add(lblPlaceholder);
        
        panelInferior.add(panelAnimacion, BorderLayout.CENTER);
        
        // -----------------
        // JSplitPane VERTICAL para hacer resizable el panel inferior
        JSplitPane splitPaneVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelCentral, panelInferior);
        splitPaneVertical.setDividerLocation(450);
        splitPaneVertical.setResizeWeight(0.65);
        splitPaneVertical.setOneTouchExpandable(true);
        
        add(splitPaneVertical, BorderLayout.CENTER);
    }
    
    // Estiliza los botones toggle para que parezcan pestañas modernas
    private void estilizarBotonToggle(JToggleButton boton) {
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setBorderPainted(true);
        boton.setBackground(new Color(240, 240, 240));
        boton.setForeground(Color.BLACK);
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        // Cambiar apariencia cuando está seleccionado
        boton.addItemListener(e -> {
            if (boton.isSelected()) {
                boton.setBackground(new Color(70, 130, 180)); // Azul
                boton.setForeground(Color.WHITE);
            } else {
                boton.setBackground(new Color(240, 240, 240));
                boton.setForeground(Color.BLACK);
            }
        });
    }
    
    private void initTables() {
        // -----------------
        // TABLA VUELOS
        String[] columnasVuelos = {"Estado", "Código", "Origen", "Destino", "Duración", "Delayed"};
        modeloVuelos = new DefaultTableModel(columnasVuelos, 0);
        tablaVuelos.setModel(modeloVuelos);
        
        tablaVuelos.setBorder(null);
        scrollVuelos.setBorder(null);
        scrollVuelos.getViewport().setBackground(Color.WHITE);
        
        tablaVuelos.getTableHeader().setReorderingAllowed(false);
        tablaVuelos.getTableHeader().setResizingAllowed(false);
        
        tablaVuelos.getColumnModel().getColumn(0).setPreferredWidth(60);  // Estado (icono)
        tablaVuelos.getColumnModel().getColumn(1).setPreferredWidth(100);
        tablaVuelos.getColumnModel().getColumn(2).setPreferredWidth(120);
        tablaVuelos.getColumnModel().getColumn(3).setPreferredWidth(120);
        tablaVuelos.getColumnModel().getColumn(4).setPreferredWidth(100);
        tablaVuelos.getColumnModel().getColumn(5).setPreferredWidth(70);
        
        int anchoMinimoVuelos = 580;
        tablaVuelos.setPreferredScrollableViewportSize(new Dimension(anchoMinimoVuelos, 0));
        
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
        // TABLA DINÁMICA (inicialmente vacía)
        modeloDinamico = new DefaultTableModel();
        tablaDinamica.setModel(modeloDinamico);
        tablaDinamica.getTableHeader().setReorderingAllowed(false);
        tablaDinamica.getTableHeader().setResizingAllowed(false);
        
        int anchoMinimoDinamico = 500;
        tablaDinamica.setPreferredScrollableViewportSize(new Dimension(anchoMinimoDinamico, 0));
        
        scrollDinamico.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int anchoDisponible = scrollDinamico.getViewport().getWidth();
                if (anchoDisponible >= anchoMinimoDinamico) {
                    tablaDinamica.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                } else {
                    tablaDinamica.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                }
            }
        });

        // -----------------
        // RENDERERS
        TableCellRenderer headerRenderer = (table, value, isSelected, hasFocus, row, column) -> {
            JLabel lbl = new JLabel(value.toString(), JLabel.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 14));
            lbl.setOpaque(true);
            lbl.setBackground(new Color(200, 200, 200));
            lbl.setForeground(Color.BLACK);
            lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
            return lbl;
        };

        TableCellRenderer cellRenderer = (table, value, isSelected, hasFocus, row, column) -> {
            JLabel lbl = new JLabel();
            lbl.setOpaque(true);
            lbl.setFont(new Font("Arial", Font.PLAIN, 12));

            // Icono en la columna 0
            if (table == tablaVuelos && column == 0 && value instanceof ImageIcon) {
                lbl.setIcon((ImageIcon) value);
                lbl.setHorizontalAlignment(JLabel.CENTER);
            } else {
                lbl.setText(value.toString());
                if (table == tablaVuelos && (column == 1 || column == 4 || column == 5)) {
                    lbl.setHorizontalAlignment(JLabel.CENTER);
                } else if (table == tablaDinamica && modeloDinamico.getColumnCount() == 3 && (column == 0 || column == 2)) {
                    lbl.setHorizontalAlignment(JLabel.CENTER);
                } else {
                    lbl.setHorizontalAlignment(JLabel.LEFT);
                }
            }

            // Si es la tabla de vuelos, convertimos el índice de vista a modelo (por filtros/orden)
            boolean isVuelosTable = table == tablaVuelos;
            int modelRow = -1;
            if (isVuelosTable && row >= 0) {
                modelRow = table.convertRowIndexToModel(row);
            }

            // Si está seleccionado, prioridad al color de selección
            if (isSelected) {
                lbl.setBackground(new Color(70, 130, 180));
                lbl.setForeground(Color.WHITE);
                return lbl;
            }


            // Si es la tabla de vuelos y hay datos válidos, comprobamos retraso y tipo (salida/llegada)
            if (isVuelosTable && modelRow >= 0 && modelRow < vuelos.size()) {
                boolean delayed = false;
                try {
                    Object delayedObj = tablaVuelos.getModel().getValueAt(modelRow, 5); // columna 5 = Delayed
                    if (delayedObj != null) {
                    	delayed = Integer.parseInt(delayedObj.toString()) > 0;
                    }
                } catch (Exception ex) {
                    delayed = false;
                }

                // Si hay retraso, resaltamos SOLO la celda "Delayed" (columna 5) en rojo suave
                if (delayed && column == 5) {
                    lbl.setBackground(new Color(255, 200, 200)); // rojo claro
                    lbl.setForeground(Color.BLACK);
                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                    return lbl;
                }

                // Si no hay retraso, pintamos según salida/llegada (usando constantes si las tienes)
                boolean esSalida = false;
                try {
                    esSalida = vuelos.get(modelRow).getOrigen().getCiudad().equalsIgnoreCase("Bilbao");
                } catch (Exception ex) {
                    esSalida = false;
                }
                lbl.setBackground(esSalida ? new Color(186, 214, 242) : new Color(198, 234, 214));
                lbl.setForeground(Color.BLACK);
                return lbl;
            }

            // Caso general (otras tablas o modelos fuera de rango): fondo alternado
            if (table == tablaDinamica) {
                if (row % 2 == 0) lbl.setBackground(new Color(245, 255, 245));
                else lbl.setBackground(Color.WHITE);
            } else {
                lbl.setBackground(Color.WHITE);
            }
            lbl.setForeground(Color.BLACK);
            return lbl;
        };


        tablaVuelos.getTableHeader().setDefaultRenderer(headerRenderer);
        tablaVuelos.setDefaultRenderer(Object.class, cellRenderer);
        tablaVuelos.getTableHeader().setPreferredSize(new Dimension(0, 30));
        tablaVuelos.setRowHeight(25);

        tablaDinamica.getTableHeader().setDefaultRenderer(headerRenderer);
        tablaDinamica.setDefaultRenderer(Object.class, cellRenderer);
        tablaDinamica.getTableHeader().setPreferredSize(new Dimension(0, 30));
        tablaDinamica.setRowHeight(25);
    }

    private void attachListeners() {
        // Listener para selección de vuelo
        tablaVuelos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaSeleccionada = tablaVuelos.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    Vuelo vueloSeleccionado = vuelos.get(filaSeleccionada);
                    
                    // Lógica para auto-seleccionar la pestaña Seatmap al hacer clic en un vuelo, 
                    // proporcionando una vista de ocupación inmediata.
                    if (!btnSeatmap.isSelected()) {
                        btnSeatmap.setSelected(true);
                    }
                    
                    actualizarPanelDinamico(vueloSeleccionado);
                } else {
                    // Si se deselecciona, limpiar el panel dinámico para no mostrar datos antiguos
                    panelDinamico.removeAll(); 
                    panelDinamico.revalidate();
                    panelDinamico.repaint();
                }
            }
        });
        
        
        
        // Listeners para los botones toggle
        btnPasajeros.addActionListener(e -> {
            int filaSeleccionada = tablaVuelos.getSelectedRow();
            if (filaSeleccionada >= 0) {
                Vuelo vueloSeleccionado = vuelos.get(filaSeleccionada);
                actualizarPanelDinamico(vueloSeleccionado);
            }
        });
        
        btnTripulacion.addActionListener(e -> {
            int filaSeleccionada = tablaVuelos.getSelectedRow();
            if (filaSeleccionada >= 0) {
                Vuelo vueloSeleccionado = vuelos.get(filaSeleccionada);
                actualizarPanelDinamico(vueloSeleccionado);
            }
        });
        
        btnInfoVuelo.addActionListener(e -> {
            int filaSeleccionada = tablaVuelos.getSelectedRow();
            if (filaSeleccionada >= 0) {
                Vuelo vueloSeleccionado = vuelos.get(filaSeleccionada);
                actualizarPanelDinamico(vueloSeleccionado);
            }
        });
        
        // El listener del Seatmap solo asegura la actualización del panel si se hace clic manualmente
        btnSeatmap.addActionListener(e -> {
            int filaSeleccionada = tablaVuelos.getSelectedRow();
            if (filaSeleccionada >= 0) {
                Vuelo vueloSeleccionado = vuelos.get(filaSeleccionada);
                actualizarPanelDinamico(vueloSeleccionado);
            }
        });
    }
    
    private void cargarVuelosReales() {
        // Limpia la tabla de vuelos y carga los datos de la lista 'vuelos'
        modeloVuelos.setRowCount(0);
        
        for (Vuelo vuelo : vuelos) {
            String origen = vuelo.getOrigen().getCiudad();
            String destino = vuelo.getDestino().getCiudad();
            String duracion = formatearDuracion(vuelo.getDuracion());
            
            // Determinar el icono según el estado del vuelo
            ImageIcon icono = obtenerIconoEstado(vuelo);
            
            modeloVuelos.addRow(new Object[]{
                icono,
                vuelo.getCodigo(),
                origen,
                destino,
                duracion,
                vuelo.getDelayed()
            });
        }
    }
    
    // Obtiene el icono según el estado del vuelo
    private ImageIcon obtenerIconoEstado(Vuelo vuelo) {

        ImageIcon icono;

        if (vuelo.isEmergencia()) {
            icono = new ImageIcon("resources\\emergencia.png");
        } else if (vuelo.getDelayed() > 0) {
            icono = new ImageIcon("resources\\retrasado.png");
        } else {
            icono = new ImageIcon("resources\\atiempo.png");
        }

        Image imagenEscalada = icono.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        return new ImageIcon(imagenEscalada);
    }

    
    private String formatearDuracion(float f) {
        int horas = (int) (f / 60);
        int mins = (int) (f % 60);
        return horas + "h " + mins + "m";
    }
    
    // Actualiza el panel dinámico según el botón seleccionado
    private void actualizarPanelDinamico(Vuelo vuelo) {
        panelDinamico.removeAll();
        
        if (btnSeatmap.isSelected()) {
            // Mostrar seatmap
            cargarSeatmap(vuelo); 
            panelDinamico.add(panelSeatmap, BorderLayout.CENTER);
        } else {
            // Mostrar tabla (Pasajeros, Tripulación, Info)
            modeloDinamico.setRowCount(0);
            modeloDinamico.setColumnCount(0);
            
            if (btnPasajeros.isSelected()) {
                cargarPasajeros(vuelo);
            } else if (btnTripulacion.isSelected()) {
                cargarTripulacion(vuelo);
            } else if (btnInfoVuelo.isSelected()) {
                cargarInfoVuelo(vuelo);
            }
            
            panelDinamico.add(scrollDinamico, BorderLayout.CENTER);
        }
        
        panelDinamico.revalidate();
        panelDinamico.repaint();
    }
    
    // **********************************************
    // LÓGICA DEL SEATMAP CON TOOLTIP SIMULADO
    // **********************************************
    
    private void cargarSeatmap(Vuelo vuelo) {
        // Limpiar el panelSeatmap existente
        panelSeatmap.removeAll();
        
        // Obtener datos del vuelo
        int capacidad = vuelo.getAvion().getCapacidad(); 
        ArrayList<String> listaPasajeros = vuelo.getPasajeros(); // Lista de nombres (String)
        
        // Configuración de la cabina (asumiendo 3-3, 6 asientos por fila)
        final int ASIENTOS_POR_FILA = 6; 
        final int NUM_COLUMNAS_GRID = ASIENTOS_POR_FILA + 2; // Incluye etiquetas de fila y pasillo
        int numFilas = (int) Math.ceil((double) capacidad / ASIENTOS_POR_FILA);

        // --- Mapeo de Datos (SIMULACIÓN DE TOOLTIP) ---
        // Asocia el código de asiento con el Tooltip HTML de los datos simulados del pasajero
        Map<String, String> asientosTooltip = new HashMap<>(); 
        
        for (int i = 0; i < listaPasajeros.size(); i++) {
            String nombrePasajero = listaPasajeros.get(i);
            
            // SIMULACIÓN DE DATOS FALTANTES:
            String idSimulado = String.format("DOC-%04d", i + 1); 
            String claseSimulada = (i % 3 == 0 || i % 7 == 0) ? "Business" : "Turista"; 
            
            String codigoAsientoAsignado = generarAsiento(i); 

            // Construcción del Tooltip en formato HTML para mostrar múltiples líneas
            String tooltipHTML = "<html><p style='padding: 5px;'>"
                + "<b>Pasajero:</b> " + nombrePasajero + "<br>" 
                + "<b>ID (Simulado):</b> " + idSimulado + "<br>"
                + "<b>Clase:</b> <span style='color:" + (claseSimulada.equals("Business") ? "blue" : "black") + "'>" + claseSimulada + "</span>" 
                + "</p></html>";
                
            asientosTooltip.put(codigoAsientoAsignado, tooltipHTML); 
        }
        // FIN: MAPEO DE DATOS

        // --- Panel Principal y Leyenda ---
        
        JPanel panelLeyenda = new JPanel(new BorderLayout());
        JLabel lblTitulo = new JLabel("Seatmap Vuelo: " + vuelo.getCodigo(), JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        
        // Leyenda de colores
        JPanel panelColores = new JPanel(new GridLayout(1, 2, 10, 0));
        panelColores.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel lblLibre = new JLabel("LIBRE", JLabel.CENTER);
        lblLibre.setOpaque(true);
        lblLibre.setBackground(SeatLabel.COLOR_LIBRE);
        lblLibre.setForeground(Color.BLACK);
        lblLibre.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JLabel lblOcupado = new JLabel("OCUPADO", JLabel.CENTER);
        lblOcupado.setOpaque(true);
        lblOcupado.setBackground(SeatLabel.COLOR_OCUPADO.darker());
        lblOcupado.setForeground(Color.WHITE);
        lblOcupado.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        panelColores.add(lblLibre);
        panelColores.add(lblOcupado);
        
        panelLeyenda.add(lblTitulo, BorderLayout.NORTH);
        panelLeyenda.add(panelColores, BorderLayout.CENTER);


        // --- Construcción de Asientos ---
        
        JPanel panelAsientos = new JPanel(new GridLayout(numFilas, NUM_COLUMNAS_GRID, 5, 5)); 
        panelAsientos.setBackground(new Color(245, 245, 245));
        panelAsientos.setBorder(new EmptyBorder(10, 10, 10, 10));

        
        int asientosGenerados = 0;

        for (int fila = 1; fila <= numFilas; fila++) {
            
            // Etiqueta de la fila
            JLabel lblFila = new JLabel(String.valueOf(fila), JLabel.CENTER);
            lblFila.setFont(new Font("Arial", Font.BOLD, 14));
            panelAsientos.add(lblFila); 
            
            for (int col = 0; col < ASIENTOS_POR_FILA; col++) {
                
                // Simulación de pasillo central (columna entre C y D)
                if (col == 3) {
                    JLabel pasillo = new JLabel("-", JLabel.CENTER); 
                    pasillo.setFont(new Font("Arial", Font.ITALIC, 10));
                    pasillo.setForeground(new Color(150, 150, 150));
                    panelAsientos.add(pasillo); 
                }
                
                // Solo crear asiento si es parte de la capacidad real del avión
                if (asientosGenerados < capacidad) {
                    // Genera el código de asiento (e.g., 1A, 2B)
                    String codigoAsiento = generarAsientoDirecto(fila, col); 
                    
                    // Comprueba la ocupación y obtiene el tooltip
                    boolean ocupado = asientosTooltip.containsKey(codigoAsiento); 
                    String tooltipData = asientosTooltip.get(codigoAsiento); 
                    
                    // Crea la etiqueta estática (no-interactiva)
                    SeatLabel seatLabel = new SeatLabel(codigoAsiento, ocupado, tooltipData); 
                    
                    panelAsientos.add(seatLabel);
                    asientosGenerados++;
                } else {
                    // Rellena el espacio vacío si la capacidad se agotó antes de esta posición
                    panelAsientos.add(new JLabel("")); 
                }
            }
            
            // Columna extra para mantener el formato de la cabina
            JLabel lblPasilloFin = new JLabel("");
            panelAsientos.add(lblPasilloFin); 
        }

        // Envolver el panel de asientos en un JScrollPane
        JScrollPane scrollAsientos = new JScrollPane(panelAsientos);
        scrollAsientos.setBorder(null);
        scrollAsientos.getVerticalScrollBar().setUnitIncrement(16);
        
        // Construir el panelSeatmap final
        panelSeatmap.removeAll();
        panelSeatmap.setLayout(new BorderLayout());
        panelSeatmap.add(panelLeyenda, BorderLayout.NORTH);
        panelSeatmap.add(scrollAsientos, BorderLayout.CENTER);
        
        // Forzar revalidación y repintado para asegurar que la vista se actualiza
        panelSeatmap.revalidate();
        panelSeatmap.repaint();
    }
    
    // Método auxiliar para generar el código de asiento basado en el índice de la lista de pasajeros
    private String generarAsiento(int index) {
        char[] letras = {'A', 'B', 'C', 'D', 'E', 'F'};
        int fila = (index / 6) + 1;
        char columna = letras[index % 6];
        return fila + "" + columna;
    }
    
    // Método auxiliar para generar el código de asiento basado en Fila y Columna
    private String generarAsientoDirecto(int fila, int colIndex) {
        char[] letras = {'A', 'B', 'C', 'D', 'E', 'F'};
        char columna = letras[colIndex];
        return fila + "" + columna;
    }

    private void cargarPasajeros(Vuelo vuelo) {
        // Carga la lista de pasajeros en la tabla dinámica
        modeloDinamico.addColumn("ID");
        modeloDinamico.addColumn("Nombre");
        modeloDinamico.addColumn("Asiento");
        
        tablaDinamica.getColumnModel().getColumn(0).setPreferredWidth(120);
        tablaDinamica.getColumnModel().getColumn(1).setPreferredWidth(250);
        tablaDinamica.getColumnModel().getColumn(2).setPreferredWidth(180);
        
        ArrayList<String> pasajeros = vuelo.getPasajeros();
        for (int i = 0; i < pasajeros.size(); i++) {
            String asiento = generarAsiento(i);
            // SIMULACIÓN DE ID FALTANTE para la tabla:
            String idSimulado = String.format("DOC-%04d", i + 1);
            modeloDinamico.addRow(new Object[]{idSimulado, pasajeros.get(i), asiento});
        }
    }
    
    private void cargarTripulacion(Vuelo vuelo) {
        // Carga la lista de tripulación en la tabla dinámica
        modeloDinamico.addColumn("ID");
        modeloDinamico.addColumn("Nombre");
        modeloDinamico.addColumn("Rol");
        
        tablaDinamica.getColumnModel().getColumn(0).setPreferredWidth(120);
        tablaDinamica.getColumnModel().getColumn(1).setPreferredWidth(250);
        tablaDinamica.getColumnModel().getColumn(2).setPreferredWidth(180);
        
        ArrayList<String> tripulacion = vuelo.getTripulacion();
        String[] roles = {"Piloto", "Copiloto", "Jefe Cabina", "Auxiliar", "Auxiliar", "Auxiliar", "Auxiliar", "Auxiliar"};
        for (int i = 0; i < tripulacion.size(); i++) {
            String rol = i < roles.length ? roles[i] : "Auxiliar";
            // SIMULACIÓN DE ID FALTANTE para la tabla:
            String idSimulado = String.format("EMP-%03d", i + 1);
            modeloDinamico.addRow(new Object[]{idSimulado, tripulacion.get(i), rol});
        }
    }
    
    private void cargarInfoVuelo(Vuelo vuelo) {
        // Carga información detallada del vuelo en la tabla dinámica
        modeloDinamico.addColumn("Campo");
        modeloDinamico.addColumn("Valor");
        
        tablaDinamica.getColumnModel().getColumn(0).setPreferredWidth(240);
        tablaDinamica.getColumnModel().getColumn(1).setPreferredWidth(440);
        
        modeloDinamico.addRow(new Object[]{"Código", vuelo.getCodigo()});
        modeloDinamico.addRow(new Object[]{"Origen", vuelo.getOrigen().getCiudad()});
        modeloDinamico.addRow(new Object[]{"Aeropuerto Origen", vuelo.getOrigen().getNombre()});
        modeloDinamico.addRow(new Object[]{"Código Origen", vuelo.getOrigen().getCodigo()});
        modeloDinamico.addRow(new Object[]{"Destino", vuelo.getDestino().getCiudad()});
        modeloDinamico.addRow(new Object[]{"Aeropuerto Destino", vuelo.getDestino().getNombre()});
        modeloDinamico.addRow(new Object[]{"Código Destino", vuelo.getDestino().getCodigo()});
        modeloDinamico.addRow(new Object[]{"Duración", formatearDuracion(vuelo.getDuracion())});
        modeloDinamico.addRow(new Object[]{"Retraso", vuelo.getDelayed() > 0 ? vuelo.getDelayed() + " min" : "Sin retraso"});
        modeloDinamico.addRow(new Object[]{"Estado", vuelo.isEstado() ? "Activo" : "Cancelado"});
        modeloDinamico.addRow(new Object[]{"Emergencia", vuelo.isEmergencia() ? "SÍ" : "NO"});
        modeloDinamico.addRow(new Object[]{"Modelo Avión", vuelo.getAvion().getModelo()});
        modeloDinamico.addRow(new Object[]{"Matrícula", vuelo.getAvion().getMatricula()});
        modeloDinamico.addRow(new Object[]{"Capacidad Avión", vuelo.getAvion().getCapacidad() + " pasajeros"});
        modeloDinamico.addRow(new Object[]{"Total Pasajeros", vuelo.getPasajeros().size()});
        modeloDinamico.addRow(new Object[]{"Total Tripulación", vuelo.getTripulacion().size()});
    }
}

/**
 * Clase auxiliar para el estilo del asiento. Implementa un JLabel estático
 * para cumplir con la perspectiva informativa y no comercial.
 */
class SeatLabel extends JLabel {
    private static final long serialVersionUID = 1L;
    
    // Colores estandarizados
    public static final Color COLOR_LIBRE = new Color(200, 200, 200); 
    public static final Color COLOR_OCUPADO = new Color(70, 130, 180); 

    /**
     * @param text El código del asiento (e.g., "1A").
     * @param isOccupied El estado de ocupación del asiento.
     * @param tooltip El texto (en formato HTML) a mostrar al pasar el ratón. Null si está libre.
     */
    public SeatLabel(String text, boolean isOccupied, String tooltip) {
        super(text, JLabel.CENTER); 
        setFont(new Font("Arial", Font.BOLD, 10));
        setPreferredSize(new Dimension(50, 30));
        setOpaque(true);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        updateStyle(isOccupied);

        // Configurar el tooltip solo si el asiento está ocupado para mostrar detalles del pasajero
        if (isOccupied && tooltip != null && !tooltip.isEmpty()) {
            setToolTipText(tooltip);
        }
    }

    // Aplica el color estático del asiento basado en su estado de ocupación
    private void updateStyle(boolean isOccupied) { 
        if (isOccupied) {
            // Asiento Ocupado (Azul oscuro)
            setBackground(COLOR_OCUPADO.darker());
            setForeground(Color.WHITE);
        } else {
            // Asiento Libre (Gris claro)
            setBackground(COLOR_LIBRE);
            setForeground(Color.BLACK);
        }
    }
}