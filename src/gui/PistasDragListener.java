package gui;

import domain.ComparadorFechaVuelos;
import domain.Pista;
import domain.Vuelo;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class PistasDragListener implements MouseListener, MouseMotionListener, AWTEventListener {

    private JList<Vuelo> listaOrigen;
    private List<JList<Vuelo>> listasDestino;
    //private JPanel ventanaPrincipal;

    private Vuelo vueloArrastrado = null;
    private int indiceArrastrado = -1;
    private Point punto = null;

    Timer timerActualizacion;
    private final int INTERVALO_ACTUALIZACION = 8;

    //Ventada Flotante
    private JWindow ventanaFlotante;
    private JLabel labelCodigo;
    private JLabel labelRuta;

    //Pistas
    private Pista pista1;
    private Pista pista2;

    private Map<JScrollPane, Border> bordersOriginales = new HashMap<>();

    //Constructor una única lista destino
    public PistasDragListener(JList<Vuelo> listaOrigen, JList<Vuelo> listaDestino, Pista pista1, Pista pista2) {
        this.listaOrigen = listaOrigen;
        this.listasDestino = new ArrayList<>();
        this.listasDestino.add(listaDestino);
        this.pista1 = pista1;
        this.pista2 = pista2;

        inicializarVentanaFlotante();
        inicializarTimer();

        //IAG (ChatGPT)
        Toolkit.getDefaultToolkit().addAWTEventListener((AWTEventListener) this,
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    //Constructor multiples listas destino
    public PistasDragListener(JList<Vuelo> listaOrigen, List<JList<Vuelo>> listasDestino, Pista pista1, Pista pista2) {
        this.listaOrigen = listaOrigen;
        this.listasDestino = listasDestino;
        this.pista1 = pista1;
        this.pista2 = pista2;

        inicializarVentanaFlotante();
        inicializarTimer();

        //IAG (ChatGPT)
        Toolkit.getDefaultToolkit().addAWTEventListener((AWTEventListener) this,
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    private void inicializarVentanaFlotante() {
        //Creación ventana flotante
        ventanaFlotante = new JWindow();
        ventanaFlotante.setAlwaysOnTop(true);
        ventanaFlotante.setFocusableWindowState(false);
        ventanaFlotante.setAutoRequestFocus(false);
        ventanaFlotante.setType(Window.Type.POPUP);

        //Creo un JPanel para organizar el contenido dentro de la ventana flotante
        JPanel contenidoLabel = new JPanel(new BorderLayout(0, 2));
        contenidoLabel.setBackground(new Color(70, 130, 180, 230));
        contenidoLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 100, 150), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        //Se ignoran para no afectar al drag
        contenidoLabel.setEnabled(false);
        contenidoLabel.addMouseListener(new MouseAdapter() {
        });

        //Label del codigo (contenido principal)
        labelCodigo = new JLabel();
        labelCodigo.setForeground(Color.WHITE);
        labelCodigo.setFont(new Font("Arial", Font.BOLD, 13));
        labelCodigo.setOpaque(false);
        labelCodigo.setHorizontalAlignment(JLabel.CENTER);

        //Label de la ruta (contenido secundario)
        labelRuta = new JLabel();
        labelRuta.setForeground(Color.WHITE);
        labelRuta.setFont(new Font("Arial", Font.BOLD, 13));
        labelRuta.setOpaque(false);
        labelRuta.setHorizontalAlignment(JLabel.CENTER);

        contenidoLabel.add(labelCodigo, BorderLayout.NORTH);
        contenidoLabel.add(labelRuta, BorderLayout.CENTER);

        ventanaFlotante.setBackground(new Color(0,0,0,0));
        ventanaFlotante.add(contenidoLabel);
        ventanaFlotante.pack();
    }

    //Solo los vuelos que aterricen serán arrastrables
    private boolean esArrastrable(Vuelo vuelo) {
        if (vuelo != null && vuelo.getDestino().getCiudad().equals("Bilbao")) {
            return true;
        }
        return false;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        @SuppressWarnings("unchecked")
        JList<Vuelo> lista = (JList<Vuelo>) e.getSource();
        int indiceElemento = lista.locationToIndex(e.getPoint());

        //Se verifica si el indice es valido
        if (indiceElemento >= 0 && indiceElemento < lista.getModel().getSize()) {
            vueloArrastrado = lista.getModel().getElementAt(indiceElemento);
            indiceArrastrado = indiceElemento;

            if(!esArrastrable(vueloArrastrado)) {
                lista.clearSelection();
                vueloArrastrado = null;
                indiceArrastrado = -1;
                return;
            }

            //Cambia el cursor
            lista.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            //Selecciona el elemento
            lista.setSelectedIndex(indiceElemento);

            //Configura la ventana flotante
            configurarVentanaFlotante();

            Point puntoActual = e.getLocationOnScreen();
            ventanaFlotante.setLocation(puntoActual);
            ventanaFlotante.setVisible(true);

            //Iniciar timer de actualización
            timerActualizacion.start();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        //IAG: (ChatGPT)
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo != null) setResaltados(pointerInfo.getLocation());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Point punto = e.getLocationOnScreen();
        @SuppressWarnings("unchecked")
        JList<Vuelo> listaOrigen = (JList<Vuelo>) e.getSource();
        drop(punto, listaOrigen);
    }

    //IAG: (ChatGPT) Metodo generado con IA
    @SuppressWarnings("unchecked")
    @Override
    public void eventDispatched(AWTEvent event) {
        if (!(event instanceof MouseEvent)) return;
        MouseEvent me = (MouseEvent) event;
        if (me.getID() == MouseEvent.MOUSE_RELEASED && vueloArrastrado != null) {
            Component src = me.getComponent();
            // Si el release vino del JWindow o fuera de listas, igual finalizamos
            drop(me.getLocationOnScreen(), (src instanceof JList) ? (JList<Vuelo>) src : listaOrigen);
        }
    }

    private void drop(Point punto, JList<Vuelo> listaOrigen) {
        try {
            timerActualizacion.stop();
            ventanaFlotante.setVisible(false);

            if (listaOrigen != null) {
                listaOrigen.setCursor(Cursor.getDefaultCursor());
            }

            if (punto != null && vueloArrastrado != null && esArrastrable(vueloArrastrado)) {
                for (JList<Vuelo> listaDestino : listasDestino) {
                    Point puntoRelativo = new Point(punto);
                    SwingUtilities.convertPointFromScreen(puntoRelativo, listaDestino);

                    //Si esta en la lista destino se actua
                    if (listaDestino.contains(puntoRelativo)) {
                        //Asigno la pista al vuelo
                        if (listaDestino.getName().equals("Pista 1")) {
                            vueloArrastrado.setPista(pista1);
                        } else if (listaDestino.getName().equals("Pista 2")) {
                            vueloArrastrado.setPista(pista2);
                        } else {
                            vueloArrastrado.setPista(null);
                        }

                        DefaultListModel<Vuelo> modeloOrigen = (DefaultListModel<Vuelo>) this.listaOrigen.getModel();
                        DefaultListModel<Vuelo> modeloDestino = (DefaultListModel<Vuelo>) listaDestino.getModel();

                        //Añado y elimino el vuelo de cada lista correspondiente
                        modeloOrigen.removeElement(vueloArrastrado);
                        modeloDestino.addElement(vueloArrastrado);

                        //Ordeno los vuelos en orden de llegada
                        Comparator<Vuelo> comparator = new ComparadorFechaVuelos();
                        ordenarVuelos(modeloDestino, comparator);

                        break;
                    }
                }
            }

        } finally {
            vueloArrastrado = null;
            indiceArrastrado = -1;
            borrarResaltados();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    private void configurarVentanaFlotante() {
        String origen = vueloArrastrado.getOrigen().getCiudad();
        String destino = vueloArrastrado.getDestino().getCiudad();
        String textoTratecto = origen + " -> " + destino;

        //Establezco el texto de ambos labels
        labelCodigo.setText(vueloArrastrado.getCodigo());
        labelRuta.setText(textoTratecto);

        ventanaFlotante.pack();
    }

    private void setResaltados(Point posicion) {
        for (JList<Vuelo> lista : listasDestino) {
            JScrollPane scrollPane = getScrollPane(lista);

            Point puntoRelativo = new Point(posicion);
            SwingUtilities.convertPointFromScreen(puntoRelativo, lista);
            if (lista.contains(puntoRelativo)) {
                scrollPane.setBorder(BorderFactory.createLineBorder(
                        new Color(70, 130, 180), 5
                ));
            } else {
                scrollPane.setBorder(null);
            }
        }
    }

    private void borrarResaltados() {
        for (JList<?> lista : listasDestino) {
            JScrollPane scrollPane = getScrollPane(lista);
            if (scrollPane != null) {
                scrollPane.setBorder(null);
            }
        }
    }

    //IAG: (Chat-GPT)
    private JScrollPane getScrollPane(JComponent comp) {
        return (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, comp);
    }

    //IAG (herramienta: Claude)
    //Codigo modificado
    private void inicializarTimer() {
        // Timer que lee la posición global del mouse y actualiza la ventana
        timerActualizacion = new Timer(INTERVALO_ACTUALIZACION, e -> {
            if (vueloArrastrado != null && ventanaFlotante.isVisible()) {
                if (vueloArrastrado != null && ventanaFlotante.isVisible()) {
                    PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                    if (pointerInfo != null) {
                        Point posicionMouse = pointerInfo.getLocation();
                        ventanaFlotante.setLocation(posicionMouse.x, posicionMouse.y);
                        setResaltados(posicionMouse);
                    }
                }
            }
        });
        timerActualizacion.setRepeats(true);
        timerActualizacion.setCoalesce(true);
    }

    private void ordenarVuelos(DefaultListModel<Vuelo> modeloVuelos, Comparator<Vuelo> comparator) {
        ArrayList<Vuelo> vuelos = new ArrayList<>();

        //Añado todos los vuelos del modeloVuelos a un nuevo arraylist
        for (int i = 0; i < modeloVuelos.size(); i++) {
            Vuelo vueloAnadir = modeloVuelos.get(i);
            vuelos.add(vueloAnadir);
        }

        //Vacio el modelo y ordeno la lista auxiliar
        vuelos.sort(comparator);
        modeloVuelos.clear();

        //Añado de nuevo los vuelos
        for (Vuelo vuelo : vuelos) {
            modeloVuelos.addElement(vuelo);
        }
    }

    public int getIndiceArrastrado() {
        return indiceArrastrado;
    }

    public Point getPunto() {
        return punto;
    }

    public Map<JScrollPane, Border> getBordersOriginales() {
        return bordersOriginales;
    }
}