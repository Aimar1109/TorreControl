package gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
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
import jdbc.GestorBD;
import domain.PaletaColor;
import domain.Pasajero;
import domain.Tripulante;
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
    
    private GestorBD gestorBD;

    public JPanelSalesman(GestorBD gestorBD) {
    	// te lo he cambiado para recibir coger los datos de la base de datos
    	this.gestorBD = gestorBD;
        this.vuelos = (ArrayList<Vuelo>) this.gestorBD.loadVuelos();
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

        // Dummy 
        JLabel lblDummy = new JLabel("00:00:00");
        lblDummy.setFont(new Font("Consolas", Font.BOLD, 18));
        lblDummy.setForeground(new Color(0, 0, 0, 0)); 
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
        scrollVuelos.setBorder(null); 
        scrollVuelos.getViewport().setBackground(Color.WHITE);
        panelIzquierdo.add(scrollVuelos, BorderLayout.CENTER);

        // B. Derecha: Pestañas + Tabla Dinámica / Seatmap
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setOpaque(false);
        panelDerecho.setBorder(new EmptyBorder(0, 10, 0, 0)); 

        // Botones (Pestañas)
        JPanel panelBotones = new JPanel(new GridLayout(1, 4, 0, 0)); 
        panelBotones.setOpaque(false);

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
        btnPasajeros.setSelected(true);

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

        panelDinamico.add(scrollDinamico, BorderLayout.CENTER);
        panelDerecho.add(panelDinamico, BorderLayout.CENTER);

        // Split Central
        JSplitPane splitHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdo, panelDerecho);
        splitHorizontal.setDividerLocation(580);
        splitHorizontal.setResizeWeight(0.5);
        estilizarSplitPane(splitHorizontal);
        splitHorizontal.setBackground(PaletaColor.get(PaletaColor.FONDO));

        panelCentral.add(splitHorizontal, BorderLayout.CENTER);

        // --- 3. PANEL INFERIOR ---
        panelInferior = new JPanel(new BorderLayout());
        panelInferior.setPreferredSize(new Dimension(0, 250));
        panelInferior.setBackground(Color.WHITE); 
        
        panelInferior.setBorder(new CompoundBorder(
                new EmptyBorder(5, 10, 5, 10),
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1)
        ));

        // --- CABECERA: Fondo Primario ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PaletaColor.get(PaletaColor.PRIMARIO)); 
        headerPanel.setPreferredSize(new Dimension(40,40));
        headerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        // Título Centrado y Blanco
        JLabel lblTimeline = new JLabel("SEGUIMIENTO EN TIEMPO REAL", JLabel.CENTER);
        lblTimeline.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTimeline.setForeground(PaletaColor.get(PaletaColor.BLANCO)); 
        headerPanel.add(lblTimeline, BorderLayout.CENTER);


        panelInferior.add(headerPanel, BorderLayout.NORTH);

        panelTimeline = new PanelTimeline(vuelos);
        panelInferior.add(panelTimeline, BorderLayout.CENTER);

        // Split Vertical Final
        JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelCentral, panelInferior);
        splitVertical.setDividerLocation(350);
        splitVertical.setResizeWeight(0.65);
        estilizarSplitPane(splitVertical);
        splitVertical.setBackground(PaletaColor.get(PaletaColor.FONDO));

        add(splitVertical, BorderLayout.CENTER);
        
    }

    // --- ESTILIZADO DE COMPONENTES ---

    private void estilizarBotonToggle(JToggleButton boton) {
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setPreferredSize(new Dimension(80, 35));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false); 
        boton.setContentAreaFilled(true);
        boton.setOpaque(true);

        Color bgNormal = PaletaColor.get(PaletaColor.FILA_ALT);
        Color bgHover = new Color(230, 240, 250);
        Color bgSelected = Color.WHITE;
        
        Color fgNormal = Color.GRAY;
        Color fgSelected = PaletaColor.get(PaletaColor.PRIMARIO);

        boton.setBackground(bgNormal); 
        boton.setForeground(fgNormal);

        boton.addItemListener(e -> {
            if (boton.isSelected()) {
                boton.setBackground(bgSelected);
                boton.setForeground(fgSelected);
            } else {
                boton.setBackground(bgNormal);
                boton.setForeground(fgNormal);
            }
        });

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!boton.isSelected()) { 
                    boton.setBackground(bgHover);
                    boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!boton.isSelected()) {
                    boton.setBackground(bgNormal);
                }
                boton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
        
        configurarEstiloTabla(tablaVuelos);
        
        estilizarScrollPane(scrollVuelos);
        
        
        
        tablaVuelos.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaVuelos.getColumnModel().getColumn(5).setPreferredWidth(80);

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
        tabla.setRowHeight(35);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(new Color(230, 230, 230));
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setResizingAllowed(false);
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
            
            modeloVuelos.addRow(new Object[]{
                icono, v.getCodigo(), v.getOrigen().getCiudad(), 
                v.getDestino().getCiudad(), duracion, v.getDelayed()
            });
        }
    }

    private ImageIcon obtenerIconoEstado(Vuelo vuelo) {
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

        // Hover Dinámica 
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
        modeloDinamico.setColumnCount(0);
        modeloDinamico.addColumn("ID");
        modeloDinamico.addColumn("Nombre");
        modeloDinamico.addColumn("Asiento");
        
        ArrayList<Pasajero> pax = vuelo.getPasajeros();
        
        for (int i = 0; i < pax.size(); i++) {
            Pasajero p = pax.get(i);
            if (p instanceof Pasajero) {
	            String id = String.format("PAX-%04d", i + 1); 
	            String nombre = p.getNombre();
	            String asiento = generarAsiento(i);
	            modeloDinamico.addRow(new Object[]{ id, nombre, asiento });
            }
        }
    }

    private void cargarTripulacion(Vuelo vuelo) {
        modeloDinamico.setColumnCount(0);
        modeloDinamico.addColumn("ID");
        modeloDinamico.addColumn("Nombre");
        modeloDinamico.addColumn("Rol");
        
        ArrayList<Tripulante> trip = vuelo.getTripulacion();
        
        String[] roles = {"Comandante", "Copiloto", "Sobrecargo", "Auxiliar de Vuelo"};

        for (int i = 0; i < trip.size(); i++) {
            Tripulante t = trip.get(i);
            String id = String.format("CREW-%03d", i + 1);
            String nombre = t.getNombre();
            String rol = (i < roles.length) ? roles[i] : "Auxiliar de Vuelo";
            modeloDinamico.addRow(new Object[]{ id, nombre, rol });
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
        ArrayList<Pasajero> listaPasajeros = vuelo.getPasajeros();
        final int COLUMNAS = 6;
        int filas = (int) Math.ceil((double) capacidad / COLUMNAS);
        int colsGrid = COLUMNAS + 2; // +1 Fila, +1 Pasillo
        Map<String, String> mapOcupacion = new HashMap<>();
        if (listaPasajeros != null) {
            for (int i = 0; i < listaPasajeros.size(); i++) {
                Pasajero p = listaPasajeros.get(i);
                if (p instanceof Pasajero) {
                    String codigoAsiento = generarAsiento(i); 
                    mapOcupacion.put(codigoAsiento, p.getNombre());
                }
            }
        }
        // --- 1. CABECERA  ---
        JPanel pnlCabecera = new JPanel(new BorderLayout());
        pnlCabecera.setBackground(Color.WHITE);
        pnlCabecera.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, PaletaColor.get(PaletaColor.OCUPADO)));

        // A) Indicador de Frente
        JLabel lblFrente = new JLabel("FRENTE DEL AVIÓN", JLabel.CENTER);
        lblFrente.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFrente.setForeground(new Color(150, 150, 150)); 
        lblFrente.setBorder(new EmptyBorder(15, 0, 5, 0)); 
        pnlCabecera.add(lblFrente, BorderLayout.NORTH);

        // B) Leyenda
        JPanel pnlLeyenda = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pnlLeyenda.setBackground(Color.WHITE);
        pnlLeyenda.add(crearItemLeyenda("LIBRE", PaletaColor.get(PaletaColor.LIBRE)));
        pnlLeyenda.add(crearItemLeyenda("OCUPADO", PaletaColor.get(PaletaColor.OCUPADO)));
        pnlCabecera.add(pnlLeyenda, BorderLayout.CENTER);

        // --- 2. GRID DE ASIENTOS  ---
        JPanel grid = new JPanel(new GridLayout(filas, colsGrid, 5, 5));
        grid.setBackground(PaletaColor.get(PaletaColor.BLANCO));
        grid.setBorder(new EmptyBorder(20, 20, 20, 20)); 
        
        int cont = 0;
        for (int f = 1; f <= filas; f++) {
            JLabel lblF = new JLabel(String.valueOf(f), JLabel.CENTER);
            lblF.setFont(new Font("Arial", Font.BOLD, 12));
            lblF.setForeground(PaletaColor.get(PaletaColor.TEXTO));
            grid.add(lblF);

            for (int c = 0; c < COLUMNAS; c++) {
                if (c == 3) grid.add(new JLabel("")); 
                
                if (cont < capacidad) {
                    String asientoActual = generarAsientoDirecto(f, c);
                    String nombrePasajero = mapOcupacion.get(asientoActual);
                    boolean ocupado = (nombrePasajero != null);
                    String tooltip;
                    if (ocupado) {
                        tooltip = "<html><b>Asiento " + asientoActual + "</b><br/>" + nombrePasajero + "</html>";
                    } else {
                        tooltip = "<html><b>Asiento " + asientoActual + "</b><br/><i>Disponible</i></html>";
                    }

                    grid.add(new SeatLabel(asientoActual, ocupado, tooltip));
                    cont++;
                } else {
                    grid.add(new JLabel(""));
                }
            }
            grid.add(new JLabel("")); 
        }

        // --- 3. SCROLL ---
        JScrollPane scroll = new JScrollPane(grid);
        estilizarScrollPane(scroll);

        // --- 4. AÑADIR AL PANEL PRINCIPAL ---
        panelSeatmap.add(pnlCabecera, BorderLayout.NORTH);
        panelSeatmap.add(scroll, BorderLayout.CENTER);
        
        panelSeatmap.revalidate();
        panelSeatmap.repaint();
    }

    private JLabel crearItemLeyenda(String txt, Color color) {
        JLabel lbl = new JLabel(txt);
        lbl.setIcon(new Icon() {
            public int getIconWidth() { return 12; }
            public int getIconHeight() { return 12; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.fillRect(x, y, 12, 12);
                g.setColor(PaletaColor.get(PaletaColor.TEXTO_SUAVE));
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
    // IAG
    private void estilizarScrollPane(JScrollPane scroll) {
        // Estilizar barra vertical
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0)); 
        scroll.getVerticalScrollBar().setUnitIncrement(16); 
        
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // Estilizar barra horizontal
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        
        JPanel cornerTopRight = new JPanel();
        cornerTopRight.setBackground(PaletaColor.get(PaletaColor.PRIMARIO));
        scroll.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, cornerTopRight);
        
        // Esquina blanca
        JPanel corner = new JPanel();
        corner.setBackground(Color.WHITE);
        scroll.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, corner);
        scroll.setBorder(null); 
    }

    // Clase interna para dibujar la barra personalizada
    // IAG
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
            
            g2.setPaint(isThumbRollover() ? PaletaColor.get(PaletaColor.SECUNDARIO) : new Color(190, 195, 200));
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8);
            g2.dispose();
        }

    }
    // IAG
    private void estilizarSplitPane(JSplitPane split) {
        split.setBorder(null); 
        split.setDividerSize(2); 
        split.setBackground(PaletaColor.get(PaletaColor.FONDO)); 

        split.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void setBorder(Border b) { }
                    
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(PaletaColor.get(PaletaColor.FONDO));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
            }
        });
    }
    
}


//--- CLASE AUXILIAR SEATLABEL ---

class SeatLabel extends JLabel {
 private static final long serialVersionUID = 1L;
 
 private boolean occupied;
 private boolean isHover = false;

 public SeatLabel(String text, boolean occupied, String tooltip) {
     super(text, JLabel.CENTER);
     this.occupied = occupied;
     
     setFont(new Font("Segoe UI", Font.BOLD, 11));
     setPreferredSize(new Dimension(45, 35)); 
     setForeground(occupied ? PaletaColor.get(PaletaColor.TEXTO_SUAVE) : PaletaColor.get(PaletaColor.BLANCO));
     
     setToolTipText(tooltip);

     addMouseListener(new MouseAdapter() {
         @Override
         public void mouseEntered(MouseEvent e) {
             isHover = true;
             setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
             repaint();
         }

         @Override
         public void mouseExited(MouseEvent e) {
             isHover = false;
             setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
             repaint();
         }
     });
 }
 
 // IAG
 @Override
 protected void paintComponent(Graphics g) {
     Graphics2D g2 = (Graphics2D) g.create();
     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

     int w = getWidth();
     int h = getHeight();
     
     if (occupied) {
         g2.setColor(PaletaColor.get(PaletaColor.OCUPADO));
     } else {
         g2.setColor(PaletaColor.get(PaletaColor.LIBRE));
     }
     g2.fillRoundRect(2, 2, w-4, h-4, 12, 12);
     
     if (isHover) {
         g2.setColor(new Color(52, 152, 219)); 
         g2.setStroke(new BasicStroke(2f)); 
         g2.drawRoundRect(2, 2, w-4, h-4, 12, 12);
         
         if(occupied) {
             g2.setColor(new Color(255,255,255,30));
             g2.fillRoundRect(2, 2, w-4, h-4, 12, 12);
         }
     } else {
         g2.setColor(new Color(200, 200, 200));
         g2.setStroke(new BasicStroke(1f));
         g2.drawRoundRect(2, 2, w-4, h-4, 12, 12);
     }

     g2.dispose();

     super.paintComponent(g);
 }
}