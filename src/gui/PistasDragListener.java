package gui;

import domain.ComparadorFechaVuelos;
import domain.Pista;
import domain.Vuelo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PistasDragListener implements MouseListener, MouseMotionListener {

    private JList<Vuelo> listaOrigen;
    private List<JList<Vuelo>> listasDestino;
    private JPanel ventanaPrincipal;

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

    //Constructor una única lista destino
    public PistasDragListener(JList<Vuelo> listaOrigen, JList<Vuelo> listaDestino, Pista pista1, Pista pista2) {
        this.listaOrigen = listaOrigen;
        this.listasDestino = new ArrayList<>();
        this.listasDestino.add(listaDestino);
        this.ventanaPrincipal = ventanaPrincipal;
        this.pista1 = pista1;
        this.pista2 = pista2;

        inicializarVentanaFlotante();
        inicializarTimer();
    }

    //Constructor multiples listas destino
    public PistasDragListener(JList<Vuelo> listaOrigen, List<JList<Vuelo>> listasDestino, Pista pista1, Pista pista2) {
        this.listaOrigen = listaOrigen;
        this.listasDestino = listasDestino;
        this.ventanaPrincipal = ventanaPrincipal;
        this.pista1 = pista1;
        this.pista2 = pista2;

        inicializarVentanaFlotante();
        inicializarTimer();
    }

    private void inicializarVentanaFlotante() {
        //Creación ventana flotante
        ventanaFlotante = new JWindow();
        ventanaFlotante.setAlwaysOnTop(true);

        //Creo un JPanel para organizar el contenido dentro de la ventana flotante
        JPanel contenidoLabel = new JPanel(new BorderLayout(0, 2));
        contenidoLabel.setBackground(new Color(70, 130, 180, 230));
        contenidoLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 100, 150), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

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
        //labelRuta.setName("labelRuta");

        contenidoLabel.add(labelCodigo, BorderLayout.NORTH);
        contenidoLabel.add(labelRuta, BorderLayout.CENTER);

        ventanaFlotante.add(contenidoLabel);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        JList<Vuelo> lista = (JList<Vuelo>) e.getSource();
        int indiceElemento = lista.locationToIndex(e.getPoint());

        //Se verifica si el indice es valido
        if (indiceElemento >= 0 && indiceElemento < lista.getModel().getSize()) {
            vueloArrastrado = lista.getModel().getElementAt(indiceElemento);
            indiceArrastrado = indiceElemento;

            //Cambia el cursor
            lista.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            //Selecciona el elemento
            lista.setSelectedIndex(indiceElemento);

            //Configuro la ventana flotante
            Point puntoActual = e.getLocationOnScreen();
            ventanaFlotante.setLocation(puntoActual);
            ventanaFlotante.setVisible(true);

            //Configura la ventana flotante
            configurarVentanaFlotante();

            //Iniciar timer de actualización
            timerActualizacion.start();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (vueloArrastrado != null) {
            timerActualizacion.stop();

            //Reestablecer configuraciones visuales originales
            ventanaFlotante.setVisible(false);
            JList<Vuelo> lista = (JList<Vuelo>) e.getSource();
            lista.setCursor(Cursor.getDefaultCursor());

            Point puntoSoltar = e.getLocationOnScreen();

            if (puntoSoltar != null) {
                for (JList<Vuelo> listaDestino : listasDestino) {
                    Point puntoRelativo = new Point(puntoSoltar);
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

            //Restauro valores iniciales
            vueloArrastrado = null;
            indiceArrastrado = -1;
            punto = null;
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
            Point puntoRelativo = new Point(posicion);
            SwingUtilities.convertPointFromScreen(puntoRelativo, lista);
            if (lista.contains(puntoRelativo)) {
                lista.setBorder(BorderFactory.createLineBorder(
                        new Color(70, 130, 180), 3
                ));
            } else {
                lista.setBorder(null);
            }
        }
    }

    private void borrarResaltados() {
        listaOrigen.setBorder(null);
        for (JList lista : listasDestino) {
            lista.setBorder(null);
        }
    }

    //IAG (herramienta: Claude)
    private void inicializarTimer() {
        // Timer que lee la posición global del mouse y actualiza la ventana
        timerActualizacion = new Timer(INTERVALO_ACTUALIZACION, e -> {
            if (vueloArrastrado != null && ventanaFlotante.isVisible()) {
                try {
                    // Obtener posición GLOBAL del mouse
                    PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                    if (pointerInfo != null) {
                        Point posicionMouse = pointerInfo.getLocation();

                        // Actualizar posición de ventana flotante
                        ventanaFlotante.setLocation(
                                posicionMouse.x,
                                posicionMouse.y
                        );

                        // Actualizar resaltado de listas
                        setResaltados(posicionMouse);
                    }
                } catch (Exception ex) {
                    // En caso de error, continuar
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
            Vuelo vueloAñadir = modeloVuelos.get(i);
            vuelos.add(vueloAñadir);
        }

        //Vacio el modelo y ordeno la lista auxiliar
        vuelos.sort(comparator);
        modeloVuelos.clear();

        //Añado de nuevo los vuelos
        for (Vuelo vuelo : vuelos) {
            modeloVuelos.addElement(vuelo);
        }
    }
}