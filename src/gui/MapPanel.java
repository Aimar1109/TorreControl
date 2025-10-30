package gui;

import domain.Avion;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapPanel extends JPanel {

    //Tamaño lógico fijo para evitar problemas con el escalado de coordenadas al variar el tamaño de la ventana.
    private final int widthReal = 1000;
    private final int heightReal = 700;
    //Aviones visualizados en el mapa
    private List<Avion> avionesAeropuerto = new ArrayList<>();
    //Imagen del aeropuerto
    private Image mapaAeropuerto;

    public MapPanel() {
        super(null, true);
        this.avionesAeropuerto = avionesAeropuerto;

        cargarImagenMapa();
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        //Se activa antialiasing para mayor calidad
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //Escalado para conseguir coordenadas relativas
        double escaladoX = getWidth()/widthReal;
        double escaladoY = getHeight()/heightReal;

        //Dibuja mapa del aeropuerto
        g2d.drawImage(mapaAeropuerto, 0, 0, getWidth(), getHeight(), this);

        //Dibujo aviones únicamente si están en el mapa
    }

    private void cargarImagenMapa() {
        try {
            mapaAeropuerto = ImageIO.read(new File("/resources/img/aeropuerto.PNG"));
        } catch (IOException e) {
            //En caso de no poder cargarlo el fondo se queda negro
            System.err.println("No se ha podido cargar el mapa" + e.getMessage());
        }
    }
}
