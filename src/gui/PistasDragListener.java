package gui;

import domain.Vuelo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

public class PistasDragListener implements MouseListener, MouseMotionListener {

    private JList<Vuelo> listaOrigen;
    private List<JList<Vuelo>> listasDestino;
    private JPanel ventanaPrincipal;

    private Vuelo vueloArrastrado = null;
    private int indiceArrastrado = -1;
    private Point punto = null;

    //Ventada Flotante
    private JWindow ventanaFlotante;
    private JLabel labelFlotante;

    //Constructor una única lista destino
    public PistasDragListener(JList<Vuelo> listaOrigen, JList<Vuelo> listaDestino) {
        this.listaOrigen = listaOrigen;
        this.listasDestino = new ArrayList<>();
        this.listasDestino.add(listaDestino);
        this.ventanaPrincipal = ventanaPrincipal;

        inicializarVentanaFlotante();
    }

    //Constructor multiples listas destino
    public PistasDragListener(JList<Vuelo> listaOrigen, List<JList<Vuelo>> listasDestino) {
        this.listaOrigen = listaOrigen;
        this.listasDestino = listasDestino;
        this.ventanaPrincipal = ventanaPrincipal;

        inicializarVentanaFlotante();
    }

    private void inicializarVentanaFlotante() {
        //Creación ventana flotante
    	
        ventanaFlotante = new JWindow();
        labelFlotante = new JLabel();
        labelFlotante.setBackground(Color.WHITE);
        labelFlotante.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        labelFlotante.setFont(new Font("Arial", Font.BOLD, 13));
        labelFlotante.setOpaque(true);
        ventanaFlotante.add(labelFlotante);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        JList<Vuelo> lista = (JList<Vuelo>) e.getSource();
        int indiceElemento = lista.locationToIndex(e.getPoint());

        //Se verifica si el indice es valido
        if (indiceElemento >= 0 && indiceElemento < lista.getModel().getSize()) {
            vueloArrastrado = lista.getModel().getElementAt(indiceElemento);
            indiceArrastrado = indiceElemento;
            lista.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            punto = e.getPoint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (vueloArrastrado != null && punto != null) {
            //Ubicación de la ventana flotante
            Point ubicacionVentanaFlotante = e.getLocationOnScreen();
            ventanaFlotante.setLocation(ubicacionVentanaFlotante.x + 5, ubicacionVentanaFlotante.y + 5);

            //Configuración de la ventana flotante
            labelFlotante.setText("" + vueloArrastrado.getCodigo());
            ventanaFlotante.pack();

            if (!ventanaFlotante.isVisible()) {
                ventanaFlotante.setVisible(true);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (vueloArrastrado != null) {
            //Reestablecer configuraciones visuales originales
            ventanaFlotante.setVisible(false);
            JList<Vuelo> lista = (JList<Vuelo>) e.getSource();
            lista.setCursor(Cursor.getDefaultCursor());

            Point puntoSoltar = e.getLocationOnScreen();

            for(JList<Vuelo> listaDestino : listasDestino) {
                Point puntoRelativo = new Point(puntoSoltar);
                SwingUtilities.convertPointFromScreen(puntoRelativo, listaDestino);

                //Si esta en la lista destino se actua
                if (listaDestino.contains(puntoRelativo)) {
                    DefaultListModel<Vuelo> modeloOrigen = (DefaultListModel<Vuelo>) this.listaOrigen.getModel();
                    DefaultListModel<Vuelo> modeloDestino = (DefaultListModel<Vuelo>) listaDestino.getModel();

                    //Añado y elimino el vuelo de cada lista correspondiente
                    modeloOrigen.removeElement(vueloArrastrado);
                    modeloDestino.addElement(vueloArrastrado);

                    break;
                }
            }

            //Restauro valores iniciales
            vueloArrastrado = null;
            indiceArrastrado = -1;
            punto = null;
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
}