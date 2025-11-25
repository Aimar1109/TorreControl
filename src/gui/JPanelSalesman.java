package gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import domain.Vuelo;
import domain.PaletaColor;
import threads.ObservadorTiempo;
import threads.RelojGlobal;
import threads.PanelTimeline; 

public class JPanelSalesman extends JPanel implements ObservadorTiempo {

    private static final long serialVersionUID = 1L;

    // Estado
    private int filaHoverDinamica = -1;
    private ArrayList<Vuelo> vuelos;

    // Componentes visuales
    private JLabel lblReloj;
    private JTable tablaVuelos;
    private JTable tablaDinamica;
    
    private JToggleButton btnPasajeros;
    private JToggleButton btnTripulacion;
    private JToggleButton btnInfoVuelo;
    private JToggleButton btnSeatmap;
    private ButtonGroup buttonGroup;
    
    private JPanel panelDinamico;
    private JPanel panelSeatmap;
    private JScrollPane scrollDinamico;
    private JScrollPane scrollVuelos;
    
    private JPanel panelInferior;
    private PanelTimeline panelTimeline;

    // Modelos
    private DefaultTableModel modeloVuelos;
    private DefaultTableModel modeloDinamico;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public JPanelSalesman(ArrayList<Vuelo> vuelos) {
        this.vuelos = vuelos;
        setLayout(new BorderLayout());
        setBackground(PaletaColor.get(PaletaColor.FONDO));

        initComponents();
        initTables();
        attachListeners();
        attachHoverListeners();
        cargarVuelosReales();

        // Iniciar el reloj
        RelojGlobal.getInstancia().addObservador(this);
        actualizarTiempo(RelojGlobal.getInstancia().getTiempoActual());
    }

    private void initComponents() {
        // --- 1. HEADER (PANEL SUPERIOR) ---
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(PaletaColor.get(PaletaColor.PRIMARIO));
        panelSuperior.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Reloj (Izquierda)
        lblReloj = new JLabel("00:00:00");
        lblReloj.setFont(new Font("Consolas", Font.BOLD, 18));
        lblReloj.setForeground(Color.WHITE);
        panelSuperior.add(lblReloj, BorderLayout.WEST);

        // Título (Centro)
        JLabel lblTitulo = new JLabel("CONTROL DE VUELOS", JLabel.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        panelSuperior.add(lblTitulo, BorderLayout.CENTER);

        // Dummy (Derecha - para equilibrar el centrado)
        JLabel lblDummy = new JLabel("00:00:00");
        lblDummy.setFont(new Font("Consolas", Font.BOLD, 18));
        lblDummy.setForeground(new Color(0, 0, 0, 0)); // Invisible
        panelSuperior.add(lblDummy, BorderLayout.EAST);

        add(panelSuperior, BorderLayout.NORTH);

        // --- 2. PANEL CENTRAL (SPLIT PANE) ---
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(PaletaColor.get(PaletaColor.FONDO));
        panelCentral.setBorder(new EmptyBorder(10, 10, 5, 10));

        // A. Izquierda: Tabla Vuelos
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setBackground(Color.WHITE);
        panelIzquierdo.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        tablaVuelos = new JTable();
        scrollVuelos = new JScrollPane(tablaVuelos);
        scrollVuelos.setBorder(null); // Limpieza visual
        scrollVuelos.getViewport().setBackground(Color.WHITE);
        panelIzquierdo.add(scrollVuelos, BorderLayout.CENTER);

        // B. Derecha: Pestañas + Tabla Dinámica / Seatmap
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setOpaque(false);
        panelDerecho.setBorder(new EmptyBorder(0, 10, 0, 0)); // Margen izquierdo

        // Botones (Pestañas)
        JPanel panelBotones = new JPanel(new GridLayout(1, 4, 5, 0)); // 5px gap horizontal
        panelBotones.setOpaque(false);
        panelBotones.setBorder(new EmptyBorder(0, 0, 5, 0));

        btnPasajeros = new JToggleButton("Pasajeros");
        btnTripulacion = new JToggleButton("Tripulación");
        btnInfoVuelo = new JToggleButton("Info");
        btnSeatmap = new JToggleButton("Seatmap");

        estilizarBotonToggle(btnPasajeros);
        estilizarBotonToggle(btnTripulacion);
        estilizarBotonToggle(btnInfoVuelo);
        estilizarBotonToggle(btnSeatmap);

        buttonGroup = new ButtonGroup();
        buttonGroup.add(btnPasajeros);
        buttonGroup.add(btnTripulacion);
        buttonGroup.add(btnInfoVuelo);
        buttonGroup.add(btnSeatmap);
        btnPasajeros.setSelected(true); // Default

        panelBotones.add(btnPasajeros);
        panelBotones.add(btnTripulacion);
        panelBotones.add(btnInfoVuelo);
        panelBotones.add(btnSeatmap);
        
        panelDerecho.add(panelBotones, BorderLayout.NORTH);

        // Contenedor cambiante
        panelDinamico = new JPanel(new BorderLayout());
        panelDinamico.setBackground(Color.WHITE);
        panelDinamico.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        tablaDinamica = new JTable();
        scrollDinamico = new JScrollPane(tablaDinamica);
        scrollDinamico.setBorder(null);
        scrollDinamico.getViewport().setBackground(Color.WHITE);

        panelSeatmap = new JPanel(new BorderLayout());
        panelSeatmap.setBackground(Color.WHITE);
        panelSeatmap.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Por defecto mostramos la tabla
        panelDinamico.add(scrollDinamico, BorderLayout.CENTER);
        panelDerecho.add(panelDinamico, BorderLayout.CENTER);

        // Split Central
        JSplitPane splitHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdo, panelDerecho);
        splitHorizontal.setDividerLocation(600);
        splitHorizontal.setResizeWeight(0.5);
        splitHorizontal.setDividerSize(6);
        splitHorizontal.setBorder(null);
        splitHorizontal.setBackground(PaletaColor.get(PaletaColor.FONDO));

        panelCentral.add(splitHorizontal, BorderLayout.CENTER);

     // --- 3. PANEL INFERIOR (TIMELINE + LEYENDA MODIFICADO) ---
        panelInferior = new JPanel(new BorderLayout());
        panelInferior.setPreferredSize(new Dimension(0, 250));
        panelInferior.setBackground(Color.WHITE); 
        
        panelInferior.setBorder(new CompoundBorder(
                new EmptyBorder(5, 10, 5, 10),
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1)
        ));

        // --- CABECERA: Fondo Primario ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        // AQUI: Cambio a color Primario
        headerPanel.setBackground(PaletaColor.get(PaletaColor.PRIMARIO)); 
        headerPanel.setPreferredSize(new Dimension(40,40));
        headerPanel.setBorder(new EmptyBorder(5, 10, 5, 10)); // Un poco de padding interno

        // Título Centrado y Blanco
        JLabel lblTimeline = new JLabel("SEGUIMIENTO EN TIEMPO REAL", JLabel.CENTER);
        lblTimeline.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTimeline.setForeground(Color.WHITE); // Texto blanco
        headerPanel.add(lblTimeline, BorderLayout.CENTER);

        // Añadir cabecera al Norte del panel inferior
        panelInferior.add(headerPanel, BorderLayout.NORTH);

        // Instancia del PanelTimeline
        panelTimeline = new PanelTimeline(vuelos);
        panelInferior.add(panelTimeline, BorderLayout.CENTER);

        // Split Vertical Final
        JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelCentral, panelInferior);
        splitVertical.setDividerLocation(350);
        splitVertical.setResizeWeight(0.65);
        splitVertical.setDividerSize(6);
        splitVertical.setBorder(null);
        splitVertical.setBackground(PaletaColor.get(PaletaColor.FONDO));

        add(splitVertical, BorderLayout.CENTER);
        
    }

    // --- ESTILIZADO DE COMPONENTES ---

    private void estilizarBotonToggle(JToggleButton boton) {
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setPreferredSize(new Dimension(80, 35));
        boton.setFocusPainted(false);
        boton.setBorderPainted(true);
        boton.setContentAreaFilled(true);
        boton.setOpaque(true);
        
        // Estado base 
        boton.setBackground(PaletaColor.get(PaletaColor.FONDO));
        boton.setForeground(PaletaColor.get(PaletaColor.TEXTO));
        boton.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        // Listener para cambio de color activo/inactivo
        boton.addItemListener(e -> {
            if (boton.isSelected()) {
                boton.setBackground(PaletaColor.get(PaletaColor.HOVER)); // Azul activo
                boton.setForeground(Color.WHITE);
            } else {
                boton.setBackground(PaletaColor.get(PaletaColor.FONDO));
                boton.setForeground(PaletaColor.get(PaletaColor.TEXTO));
            }
        });
    }
    

    private void initTables() {
        // Modelo Vuelos
        String[] columnasVuelos = {"Estado", "Código", "Origen", "Destino", "Duración", "Retraso"};
        modeloVuelos = new DefaultTableModel(columnasVuelos, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaVuelos.setModel(modeloVuelos);
        
        // Estilo moderno reutilizable
        configurarEstiloTabla(tablaVuelos);
        
        estilizarScrollPane(scrollVuelos);

        // Ajustes de ancho específicos
        tablaVuelos.getColumnModel().getColumn(0).setPreferredWidth(40); // Icono
        tablaVuelos.getColumnModel().getColumn(5).setPreferredWidth(80); // Status

        // Modelo Dinámico
        modeloDinamico = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaDinamica.setModel(modeloDinamico);
        configurarEstiloTabla(tablaDinamica);
        estilizarScrollPane(scrollDinamico);
    }

    private void configurarEstiloTabla(JTable tabla) {
        tabla.setRowHeight(35); // Más altura (Padding)
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(new Color(230, 230, 230));
        tabla.setIntercellSpacing(new Dimension(0, 0));

        // Header
        JTableHeader header = tabla.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBackground(PaletaColor.get(PaletaColor.PRIMARIO));
                lbl.setForeground(PaletaColor.get(PaletaColor.BLANCO));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setBorder(new EmptyBorder(8, 5, 8, 5));
                return lbl;
            }
        });
        header.setPreferredSize(new Dimension(0, 40));

        // Celdas
        tabla.setDefaultRenderer(Object.class, new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel();
                lbl.setOpaque(true);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));

                // Contenido
                if (value instanceof ImageIcon) {
                    lbl.setIcon((ImageIcon) value);
                    lbl.setHorizontalAlignment(JLabel.CENTER);
                } else {
                    lbl.setText(value != null ? value.toString() : "");
                    // Centrar ciertas columnas en tablaVuelos
                    if (table == tablaVuelos && (column == 1 || column == 4 || column == 5)) {
                        lbl.setHorizontalAlignment(JLabel.CENTER);
                    } else {
                        lbl.setHorizontalAlignment(JLabel.LEFT);
                    }
                }

                // Selección y Hover
                if (isSelected || (table == tablaDinamica && row == filaHoverDinamica)) {
                    lbl.setBackground(PaletaColor.get(PaletaColor.HOVER));
                    lbl.setForeground(PaletaColor.get(PaletaColor.PRIMARIO));
                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                } else {
                    // Zebra
                    lbl.setBackground((row % 2 == 0) ? Color.WHITE : PaletaColor.get(PaletaColor.FILA_ALT));
                    lbl.setForeground(Color.DARK_GRAY);
                }

                // Color para "Delayed"
                if (table == tablaVuelos && column == 5) {
                    try {
                        // En la lógica de carga, la columna 5 es int (minutos)
                        // pero aquí podría llegar como String
                        String txt = value != null ? value.toString() : "0";
                        if (!txt.equals("0")) {
                            lbl.setForeground(PaletaColor.get(PaletaColor.DELAYED));
                            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                        } else {
                            lbl.setForeground(PaletaColor.get(PaletaColor.EXITO));
                        }
                    } catch (Exception e) {}
                }
                return lbl;
            }
        });
    }

    // --- CARGA DE DATOS ---

    private void cargarVuelosReales() {
        modeloVuelos.setRowCount(0);
        for (Vuelo v : vuelos) {
            String duracion = formatearDuracion(v.getDuracion());
            ImageIcon icono = obtenerIconoEstado(v);
            
            // Nota: Aquí pasamos v.getDelayed() (int), el renderer se encarga del color
            modeloVuelos.addRow(new Object[]{
                icono, v.getCodigo(), v.getOrigen().getCiudad(), 
                v.getDestino().getCiudad(), duracion, v.getDelayed()
            });
        }
    }

    private ImageIcon obtenerIconoEstado(Vuelo vuelo) {
        // Ajusta las rutas según tu proyecto
        String path = "resources/atiempo.png";
        if (vuelo.isEmergencia()) path = "resources/emergencia.png";
        else if (vuelo.getDelayed() > 0) path = "resources/retrasado.png";

        ImageIcon icono = new ImageIcon(path);
        if (icono.getImageLoadStatus() == MediaTracker.COMPLETE) {
            Image img = icono.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return new ImageIcon();
    }

    private String formatearDuracion(float f) {
        int h = (int) (f / 60);
        int m = (int) (f % 60);
        return h + "h " + m + "m";
    }

    // --- INTERACCIÓN Y DINÁMICA ---

    private void attachListeners() {
        // Click en tabla vuelos
        tablaVuelos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tablaVuelos.getSelectedRow();
                if (row >= 0) {
                    int modelRow = tablaVuelos.convertRowIndexToModel(row);
                    actualizarPanelDinamico(vuelos.get(modelRow));
                }
            }
        });

        // Click en botones
        ActionListener al = e -> {
            int row = tablaVuelos.getSelectedRow();
            if (row >= 0) {
                int modelRow = tablaVuelos.convertRowIndexToModel(row);
                actualizarPanelDinamico(vuelos.get(modelRow));
            }
        };
        btnPasajeros.addActionListener(al);
        btnTripulacion.addActionListener(al);
        btnInfoVuelo.addActionListener(al);
        btnSeatmap.addActionListener(al);
    }

    private void attachHoverListeners() {
        // Hover Vuelos
        tablaVuelos.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = tablaVuelos.rowAtPoint(e.getPoint());
                if (row != tablaVuelos.getSelectedRow() && row >= 0) {
                    tablaVuelos.setRowSelectionInterval(row, row);
                }
            }
        });

        // Hover Dinámica (solo resalta fila)
        tablaDinamica.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = tablaDinamica.rowAtPoint(e.getPoint());
                if (row != filaHoverDinamica) {
                    filaHoverDinamica = row;
                    tablaDinamica.repaint();
                }
            }
        });
        tablaDinamica.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (filaHoverDinamica != -1) {
                    filaHoverDinamica = -1;
                    tablaDinamica.repaint();
                }
            }
        });
    }

    private void actualizarPanelDinamico(Vuelo vuelo) {
        panelDinamico.removeAll();
        filaHoverDinamica = -1;

        if (btnSeatmap.isSelected()) {
            cargarSeatmap(vuelo);
            panelDinamico.add(panelSeatmap, BorderLayout.CENTER);
        } else {
            modeloDinamico.setRowCount(0);
            modeloDinamico.setColumnCount(0);

            if (btnPasajeros.isSelected()) cargarPasajeros(vuelo);
            else if (btnTripulacion.isSelected()) cargarTripulacion(vuelo);
            else if (btnInfoVuelo.isSelected()) cargarInfoVuelo(vuelo);

            panelDinamico.add(scrollDinamico, BorderLayout.CENTER);
        }
        panelDinamico.revalidate();
        panelDinamico.repaint();
    }

    // --- CARGA DE DATOS ESPECÍFICOS ---

    private void cargarPasajeros(Vuelo vuelo) {
        modeloDinamico.addColumn("ID");
        modeloDinamico.addColumn("Nombre");
        modeloDinamico.addColumn("Asiento");
        
        ArrayList<String> pax = vuelo.getPasajeros();
        for (int i = 0; i < pax.size(); i++) {
            modeloDinamico.addRow(new Object[]{
                String.format("DOC-%04d", i + 1), pax.get(i), generarAsiento(i)
            });
        }
    }

    private void cargarTripulacion(Vuelo vuelo) {
        modeloDinamico.addColumn("ID");
        modeloDinamico.addColumn("Nombre");
        modeloDinamico.addColumn("Rol");
        
        ArrayList<String> trip = vuelo.getTripulacion();
        String[] roles = {"Piloto", "Copiloto", "Jefe Cabina", "Auxiliar"};
        for (int i = 0; i < trip.size(); i++) {
            modeloDinamico.addRow(new Object[]{
                String.format("EMP-%03d", i + 1), trip.get(i), (i < roles.length ? roles[i] : "Auxiliar")
            });
        }
    }

    private void cargarInfoVuelo(Vuelo vuelo) {
        modeloDinamico.addColumn("Dato");
        modeloDinamico.addColumn("Valor");
        modeloDinamico.addRow(new Object[]{"Código", vuelo.getCodigo()});
        modeloDinamico.addRow(new Object[]{"Origen", vuelo.getOrigen().getCiudad() + " (" + vuelo.getOrigen().getCodigo() + ")"});
        modeloDinamico.addRow(new Object[]{"Destino", vuelo.getDestino().getCiudad() + " (" + vuelo.getDestino().getCodigo() + ")"});
        modeloDinamico.addRow(new Object[]{"Avión", vuelo.getAvion().getModelo()});
        modeloDinamico.addRow(new Object[]{"Matrícula", vuelo.getAvion().getMatricula()});
        modeloDinamico.addRow(new Object[]{"Capacidad", vuelo.getAvion().getCapacidad()});
    }

    private void cargarSeatmap(Vuelo vuelo) {
        panelSeatmap.removeAll();
        int capacidad = vuelo.getAvion().getCapacidad();
        ArrayList<String> listaPasajeros = vuelo.getPasajeros();
        
        final int COLUMNAS = 6;
        int filas = (int) Math.ceil((double) capacidad / COLUMNAS);
        int colsGrid = COLUMNAS + 2; // + Fila y Pasillo

        Map<String, String> tooltips = new HashMap<>();
        for (int i = 0; i < listaPasajeros.size(); i++) {
            tooltips.put(generarAsiento(i), "Pasajero: " + listaPasajeros.get(i));
        }

        // Leyenda
        JPanel pnlLeyenda = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        pnlLeyenda.setBackground(Color.WHITE);
        pnlLeyenda.add(crearItemLeyenda("LIBRE", SeatLabel.COLOR_LIBRE));
        pnlLeyenda.add(crearItemLeyenda("OCUPADO", SeatLabel.COLOR_OCUPADO));

        // Grid Asientos
        JPanel grid = new JPanel(new GridLayout(filas, colsGrid, 5, 5));
        grid.setBackground(Color.WHITE);
        
        int cont = 0;
        for (int f = 1; f <= filas; f++) {
            JLabel lblF = new JLabel(String.valueOf(f), JLabel.CENTER);
            lblF.setFont(new Font("Arial", Font.BOLD, 12));
            grid.add(lblF);

            for (int c = 0; c < COLUMNAS; c++) {
                if (c == 3) grid.add(new JLabel("")); // Pasillo
                
                if (cont < capacidad) {
                    String asiento = generarAsientoDirecto(f, c);
                    boolean ocupado = tooltips.containsKey(asiento);
                    grid.add(new SeatLabel(asiento, ocupado, tooltips.get(asiento)));
                    cont++;
                } else {
                    grid.add(new JLabel(""));
                }
            }
            grid.add(new JLabel(""));
        }

        JScrollPane scroll = new JScrollPane(grid);
        estilizarScrollPane(scroll);

        panelSeatmap.add(pnlLeyenda, BorderLayout.NORTH);
        panelSeatmap.add(scroll, BorderLayout.CENTER);
        panelSeatmap.revalidate();
        panelSeatmap.repaint();
    }

    private JLabel crearItemLeyenda(String txt, Color col) {
        JLabel lbl = new JLabel(txt);
        lbl.setIcon(new Icon() {
            public int getIconWidth() { return 12; }
            public int getIconHeight() { return 12; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(col);
                g.fillRect(x, y, 12, 12);
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x, y, 12, 12);
            }
        });
        return lbl;
    }

    private String generarAsiento(int i) {
        char[] l = {'A','B','C','D','E','F'};
        return ((i/6)+1) + "" + l[i%6];
    }
    
    private String generarAsientoDirecto(int f, int c) {
        char[] l = {'A','B','C','D','E','F'};
        return f + "" + l[c];
    }

    // --- OBSERVADOR TIEMPO ---

    @Override
    public void actualizarTiempo(LocalDateTime nuevoTiempo) {
        SwingUtilities.invokeLater(() -> {
            if (lblReloj != null) lblReloj.setText(nuevoTiempo.format(formatter));
        });
    }

    @Override
    public void cambioEstadoPausa(boolean pausa) {
        SwingUtilities.invokeLater(() -> {
            if (lblReloj != null) {
                if (pausa) {
                    lblReloj.setForeground(PaletaColor.get(PaletaColor.DELAYED));
                    lblReloj.setText(lblReloj.getText() + " (PAUSA)");
                } else {
                    lblReloj.setForeground(Color.WHITE);
                }
            }
        });
    }
 // --- MÉTODOS DE ESTILO PARA EL SCROLLBAR  ---

    private void estilizarScrollPane(JScrollPane scroll) {
        // Estilizar barra vertical
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0)); // Ancho fino
        scroll.getVerticalScrollBar().setUnitIncrement(16); // Velocidad scroll

        // Estilizar barra horizontal
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        
        // Esquina blanca
        JPanel corner = new JPanel();
        corner.setBackground(Color.WHITE);
        scroll.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, corner);
        scroll.setBorder(null); 
    }

    // Clase interna para dibujar la barra personalizada
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(180, 180, 180); 
            this.trackColor = Color.WHITE;              
        }

        @Override
        protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }

        @Override
        protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Color: Gris normal o Azul (SECUNDARIO) si pasas el ratón
            g2.setPaint(isThumbRollover() ? PaletaColor.get(PaletaColor.SECUNDARIO) : new Color(190, 195, 200));
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            // Fondo transparente/blanco, no pintamos nada para que se vea limpio
        }
    }
}


// --- CLASE AUXILIAR SEATLABEL (Estilo Moderno) ---
class SeatLabel extends JLabel {
    private static final long serialVersionUID = 1L;
    public static final Color COLOR_LIBRE = new Color(236, 240, 241);
    public static final Color COLOR_OCUPADO = new Color(52, 152, 219);

    public SeatLabel(String text, boolean occupied, String tooltip) {
        super(text, JLabel.CENTER);
        setOpaque(true);
        setFont(new Font("Segoe UI", Font.BOLD, 11));
        setPreferredSize(new Dimension(45, 30));
        // Borde redondeado "fake" usando borde suave
        setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        
        if (occupied) {
            setBackground(COLOR_OCUPADO);
            setForeground(Color.WHITE);
            setToolTipText(tooltip);
        } else {
            setBackground(COLOR_LIBRE);
            setForeground(Color.DARK_GRAY);
        }
    }
}

