package gui;

import domain.Avion;

import javax.swing.*;
import java.awt.*;
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
        //this.avionesAeropuerto = avionesAeropuerto;
        //this.mapaAeropuerto = mapaAeropuerto;
        setBackground(Color.BLACK);
    }
}
