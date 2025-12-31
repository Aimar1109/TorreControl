package gui;

import domain.*;
import jdbc.GestorBD;
import threads.ControladorMovimiento;
import threads.ObservadorTiempo;
import threads.RelojGlobal;

import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class JPanelPrincipal extends JPanel implements ObservadorTiempo {

    private static final long serialVersionUID = 1L;

    //Referencias
    private JList<Vuelo> listaVuelosCercanos;
    private JList<Vuelo> listaVuelosPista1;
    private JList<Vuelo> listaVuelosPista2;
    private MapPanel mapa;
    private JLabel labelReloj;

    //Y sus respectivos modelos
    private DefaultListModel<Vuelo> modeloVuelosCercanos;
    private DefaultListModel<Vuelo> modeloVuelosPista1;
    private DefaultListModel<Vuelo> modeloVuelosPista2;

    //Pistas
    private Pista pista1 = new Pista("1", true);
    private Pista pista2 = new Pista("2", true);

    //Animacion despegue, aterrizaje y estacionamiento
    private ControladorMovimiento controladorHangar;

    //Comparator vuelos
    private ComparadorFechaVuelos comparadorVuelos;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public JPanelPrincipal(GestorBD gestorBD, ArrayList<Vuelo> vuelos) {
        this(gestorBD, vuelos, new ArrayList<>());
    }

    public JPanelPrincipal(GestorBD gestorBD, ArrayList<Vuelo> vuelos, List<Avion> aviones) {

        this.comparadorVuelos = new ComparadorFechaVuelos();

        //Panel Principal
        setLayout(new BorderLayout());
        setBackground(PaletaColor.get(PaletaColor.FONDO));

        //Panel superior
        JPanel panelSuperior = crearPanelSuperior();
        add(panelSuperior, BorderLayout.NORTH);

        //El resto irá dentro de un gridBagLayout
        JPanel panelIntermedio = new JPanel(new GridBagLayout());
        panelIntermedio.setBackground(PaletaColor.get(PaletaColor.FONDO));

        Insets insets = new Insets(5, 5, 5,5);

        //Vuelos Cercanos
        JPanel panelVuelos = crearPanelListaOrigen("Vuelos Cercanos", vuelos);

        GridBagConstraints gbcIzda = new GridBagConstraints();
        gbcIzda.gridx = 0;
        gbcIzda.gridy = 1;
        gbcIzda.gridwidth = 1;
        gbcIzda.gridheight = 2;
        gbcIzda.weightx = 0.4;
        gbcIzda.weighty = 1;
        gbcIzda.insets = insets;
        gbcIzda.fill = GridBagConstraints.BOTH;
        panelIntermedio.add(panelVuelos, gbcIzda);

        //Panel Mapa
        mapa = new MapPanel();
        mapa.setPreferredSize(new Dimension(1000, 700));
        mapa.setAviones(aviones);

        GridBagConstraints gbcPrincipal = new GridBagConstraints();

        gbcPrincipal.gridx = 1;
        gbcPrincipal.gridy = 1;
        gbcPrincipal.gridwidth = 1;
        gbcPrincipal.gridheight = 1;
        gbcPrincipal.weightx = 0.6;
        gbcPrincipal.weighty = 0.7;
        gbcPrincipal.insets = insets;
        gbcPrincipal.fill = GridBagConstraints.BOTH;
        panelIntermedio.add(mapa, gbcPrincipal);

        //Pistas
        JPanel panelPistas = new JPanel(new GridLayout(1, 2, 5, 0));
        JPanel pista1 = crearPanelListaPanel1("Aterrizajes Pista 1");
        JPanel pista2 = crearPanelListaPanel2("Aterrizajes Pista 2");
        panelPistas.add(pista1);
        panelPistas.add(pista2);

        GridBagConstraints gbcInferior = new GridBagConstraints();
        gbcInferior.gridx = 1;
        gbcInferior.gridy = 2;
        gbcInferior.gridwidth = 1;
        gbcInferior.gridheight = 1;
        gbcInferior.weightx = 0.75;
        gbcInferior.weighty = 0.3;
        gbcInferior.insets = insets;
        gbcInferior.fill = GridBagConstraints.BOTH;
        panelIntermedio.add(panelPistas, gbcInferior);

        add(panelIntermedio, BorderLayout.CENTER);

        //Configuración de la ventana
        configuracionDraglistaVuelosCercanos();
        configuracionDraglistaVuelos1();
        configuracionDraglistaVuelos2();
        asignarPorTeclado();
        efectoHover(listaVuelosCercanos);
        efectoHover(listaVuelosPista1);
        efectoHover(listaVuelosPista2);

        this.controladorHangar = new ControladorMovimiento(mapa, vuelos);

        //Configurar instancia RelojPrincial
        RelojGlobal instanciaReloj = RelojGlobal.getInstancia();
        instanciaReloj.addObservador(this);

        //Ordena las listas
        ordenarLista(modeloVuelosCercanos);
        ordenarLista(modeloVuelosPista1);
        ordenarLista(modeloVuelosPista2);
    }

    private JPanel crearPanelSuperior() {
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(PaletaColor.get(PaletaColor.PRIMARIO));
        panelSuperior.setBorder(new EmptyBorder(15, 20, 15, 20));

        //Reloj
        labelReloj = new JLabel();
        labelReloj.setPreferredSize(new Dimension(120, 0));
        labelReloj.setFont(new Font("Consolas", Font.BOLD, 18));
        labelReloj.setForeground(PaletaColor.get(PaletaColor.BLANCO));
        panelSuperior.add(labelReloj, BorderLayout.WEST);

        //Título
        JLabel titulo = new JLabel("PANEL PRINCIPAL", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(PaletaColor.get(PaletaColor.BLANCO));
        panelSuperior.add(titulo, BorderLayout.CENTER);

        JLabel panelVacio = new JLabel("");
        panelVacio.setPreferredSize(new Dimension(120, 0));
        panelVacio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        panelSuperior.add(panelVacio, BorderLayout.EAST);

        return panelSuperior;
    }

    private JPanel crearHeader(String titulo) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PaletaColor.get(PaletaColor.PRIMARIO));
        header.setPreferredSize(new Dimension(0, 40));

        JLabel labelTitulo = new JLabel();
        labelTitulo.setText(titulo);
        labelTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        labelTitulo.setForeground(PaletaColor.get(PaletaColor.BLANCO));
        labelTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.add(labelTitulo, BorderLayout.CENTER);

        return header;
    }

    private JPanel crearPanelListaOrigen(String titulo, List<Vuelo> vuelos) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel header = crearHeader(titulo);
        panel.add(header, BorderLayout.NORTH);

        modeloVuelosCercanos = new DefaultListModel<>();
        for (Vuelo vuelo : vuelos) {
            modeloVuelosCercanos.addElement(vuelo);
        }

        listaVuelosCercanos = new JList<>(modeloVuelosCercanos);
        listaVuelosCercanos.setName("Vuelos Cercanos");
        listaVuelosCercanos.setCellRenderer(new VueloListRenderer());
        listaVuelosCercanos.setFixedCellHeight(60);

        JScrollPane scrollAviones = new JScrollPane(listaVuelosCercanos);
        estilizarScrollPane(scrollAviones);
        panel.add(scrollAviones, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelListaPanel1(String titulo) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel header = crearHeader(titulo);
        panel.add(header, BorderLayout.NORTH);

        modeloVuelosPista1 = new DefaultListModel<>();
        listaVuelosPista1 = new JList<>(modeloVuelosPista1);
        listaVuelosPista1.setName("Pista 1");
        listaVuelosPista1.setCellRenderer(new VueloListRenderer());
        listaVuelosPista1.setFixedCellHeight(60);

        JScrollPane scrollAviones = new JScrollPane(listaVuelosPista1);
        estilizarScrollPane(scrollAviones);
        panel.add(scrollAviones, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelListaPanel2(String titulo) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel header = crearHeader(titulo);
        panel.add(header, BorderLayout.NORTH);

        modeloVuelosPista2 = new DefaultListModel<>();
        listaVuelosPista2 = new JList<>(modeloVuelosPista2);
        listaVuelosPista2.setName("Pista 2");
        listaVuelosPista2.setCellRenderer(new VueloListRenderer());
        listaVuelosPista2.setFixedCellHeight(60);

        JScrollPane scrollAviones = new JScrollPane(listaVuelosPista2);
        estilizarScrollPane(scrollAviones);
        panel.add(scrollAviones, BorderLayout.CENTER);

        return panel;
    }

    private void configuracionDraglistaVuelosCercanos() {
        List<JList<Vuelo>> destinos = new ArrayList<>();
        destinos.add(listaVuelosPista1);
        destinos.add(listaVuelosPista2);

        PistasDragListener listener = new PistasDragListener(listaVuelosCercanos, destinos, pista1, pista2);

        listaVuelosCercanos.addMouseListener(listener);
        listaVuelosCercanos.addMouseMotionListener(listener);
    }

    private void configuracionDraglistaVuelos1() {
        List<JList<Vuelo>> destinos = new ArrayList<>();
        destinos.add(listaVuelosCercanos);
        destinos.add(listaVuelosPista2);

        PistasDragListener listener = new PistasDragListener(listaVuelosPista1, destinos, pista1, pista2);

        listaVuelosPista1.addMouseListener(listener);
        listaVuelosPista1.addMouseMotionListener(listener);
    }

    private void configuracionDraglistaVuelos2() {
        List<JList<Vuelo>> destinos = new ArrayList<>();
        destinos.add(listaVuelosPista1);
        destinos.add(listaVuelosCercanos);

        PistasDragListener listener = new PistasDragListener(listaVuelosPista2, destinos, pista1, pista2);

        listaVuelosPista2.addMouseListener(listener);
        listaVuelosPista2.addMouseMotionListener(listener);
    }

    public MapPanel getMapa() {
        return mapa;
    }

    private void asignarPorTeclado() {
        this.setFocusable(true);

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_P) {
                    abrirDialogoAsinacion();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
            }

            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
            }
        });

        //IAG (herramienta: Claude)
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    private void abrirDialogoAsinacion() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);


        DialogoAsignarPista dialogo = new DialogoAsignarPista(parent, modeloVuelosCercanos, modeloVuelosPista1, modeloVuelosPista2, pista1, pista2);
        dialogo.setVisible(true);

        this.revalidate();
        this.repaint();
    }

    private void efectoHover(JList<Vuelo> lista) {
        lista.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point punto = e.getPoint();
                int index = lista.locationToIndex(punto);
                if (index != -1) {
                    lista.setSelectedIndex(index);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                //No necesario
            }
        });

        lista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                lista.clearSelection();
            }
        });
    }

    @Override
    public void actualizarTiempo(LocalDateTime nuevoTiempo) {
        SwingUtilities.invokeLater(() -> {
            limpiarVuelos(modeloVuelosCercanos, nuevoTiempo);
            limpiarVuelos(modeloVuelosPista1, nuevoTiempo);
            limpiarVuelos(modeloVuelosPista2, nuevoTiempo);
        });

        SwingUtilities.invokeLater(() -> {
            if (labelReloj != null) labelReloj.setText(nuevoTiempo.format(formatter));
        });
    }

    @Override
    public void cambioEstadoPausa(boolean pausa) {
        SwingUtilities.invokeLater(() -> {
            if (labelReloj != null) {
                if (pausa) {
                    labelReloj.setForeground(PaletaColor.get(PaletaColor.DELAYED));
                    labelReloj.setText(labelReloj.getText() + " (PAUSA)");
                } else {
                    labelReloj.setForeground(Color.WHITE);
                }
            }
        });
    }

    private void ordenarLista(DefaultListModel<Vuelo> modelo) {
        if (modelo.isEmpty()) {
            return;
        }

        //Añado todos los vuelos a la lista
        ArrayList<Vuelo> vuelos = new ArrayList<>();
        for (int i = 0; i < modelo.size(); i++) {
            vuelos.add(modelo.get(i));
        }

        //Ordeno la lista
        Collections.sort(vuelos, comparadorVuelos);

        //Vuelvo insertar los vuelos al modelo vaciado
        modelo.clear();
        for (Vuelo v : vuelos) {
            modelo.addElement(v);
        }
    }

    private void limpiarVuelos(DefaultListModel<Vuelo> modelo, LocalDateTime momentoActual) {
        ArrayList<Vuelo> vuelosPasados = new ArrayList<>();

        for (int i = 0; i < modelo.size(); i++) {
            Vuelo vuelo = modelo.get(i);

            int delay = vuelo.getDelayed();
            LocalDateTime momentoLlegada = vuelo.getFechaHoraProgramada().plusMinutes(delay);
            if (momentoActual.isAfter(momentoLlegada)) {
                vuelosPasados.add(vuelo);
            }
        }

        for (Vuelo v: vuelosPasados) {
            modelo.removeElement(v);
        }
    }

    // IAG: Configuración visual del ScrollPane con estilización para modernidad aplicada
    //Codigo reutilizado de JPanelSalesman
    private void estilizarScrollPane(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));

        JPanel cornerTopRight = new JPanel();
        cornerTopRight.setBackground(PaletaColor.get(PaletaColor.PRIMARIO));
        scroll.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, cornerTopRight);

        JPanel corner = new JPanel();
        corner.setBackground(Color.WHITE);
        scroll.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, corner);
        scroll.setBorder(null);
    }

    // IAG: Clase estética (Codigo reutilizado de JPanelSalesman)
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
}