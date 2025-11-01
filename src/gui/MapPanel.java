package gui;

import domain.Avion;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapPanel extends JPanel {

    //Tamaño lógico fijo para evitar problemas con el escalado de coordenadas al variar el tamaño de la ventana.
    private final int widthReal = 1000;
    private final int heightReal = 700;
    //Aviones visualizados en el mapa
    private List<Avion> avionesAeropuerto = new ArrayList<>();
    //Imagen del aeropuerto
    private Image mapaAeropuerto;
    //Imagenes de los aviones
    private List<Image> imagenesAviones;
    //Caché de imagenes
    private HashMap<String, Image> cacheImagenes = new HashMap<>();
    private Image mapaCache = null;
    private int ultimoWidth = -1;
    private int ultimoHeight = -1;

    public MapPanel() {
        super(null, true);
        this.avionesAeropuerto = avionesAeropuerto;

        cargarImagenMapa();
        cargarImagenesAviones();
        setBackground(Color.BLACK);
    }

    //Actualiza lista aviones
    public void setAviones (List<Avion> aviones) {
        this.avionesAeropuerto = aviones;
        repaint();
    }

    //Añade a lista aviones
    public void addAviones (Avion avion) {
        this.avionesAeropuerto.add(avion);
        repaint();
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
        dibujarMapa(g2d);

        //Dibujo aviones únicamente si están en el mapa
        for (Avion avion: avionesAeropuerto) {
            if (estaMapa(avion)) {
                dibujarAvion(g2d, avion, escaladoX, escaladoY);
            }
        }
    }

    private void dibujarMapa(Graphics2D g2d) {
        int width = this.getWidth();
        int height = this.getHeight();

        //Si la ventana ha variado su tamaño
        if (width != ultimoWidth || height != ultimoHeight || mapaCache == null) {
            mapaCache = mapaAeropuerto.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ultimoHeight = height;
            ultimoWidth = width;
        }

        g2d.drawImage(mapaCache, 0, 0, this);
    }

    private void dibujarAvion(Graphics2D g2d, Avion avion, double escaladoX, double escaladoY) {
        //Coordenadas lógicas
        int x = (int) (avion.getX() * escaladoX);
        int y = (int) (avion.getY() * escaladoY);

        //Tamaño base del avion
        int tamañoBaseX = 20;
        int tamañoBaseY = 30;

        //Escalado en función del tamaño del mapa
        int tamañoRealX = (int) Math.max(tamañoBaseX * escaladoX, 10);
        int tamañoRealY = (int) Math.max(tamañoBaseY * escaladoY, 15);

        //Transformación inicial
        AffineTransform transformaciónInicial = g2d.getTransform();

        //Se traslada al centro del avión y rota
        g2d.translate(x, y);
        g2d.rotate(avion.getAngulo());

        int mitadX = tamañoRealX/2;
        int mitadY = tamañoRealY/2;

        //Si la imagen existe se dibuja
        if (avion.getImagen() != null) {
            Image imagenAvionCacheEscalada = obtenerImagenCache(avion.getImagen(), tamañoRealX, tamañoRealY);
            g2d.drawImage(avion.getImagen(),-mitadX, -mitadY, this);
        }
        //Si no se consigue acceder a la imagen, se crea un avión(poligono) manualmente
        else {
            int[] xPoint = {0, -(int)(tamañoRealX * 0.4), (int)(tamañoRealX * 0.4)};
            int[] yPoint = {-(int)(tamañoRealY * 0.5), (int)(tamañoRealY * 0.5), (int)(tamañoRealY * 0.5)};

            g2d.fillPolygon(xPoint, yPoint, 3);
            g2d.setColor(Color.WHITE);
            g2d.drawPolygon(xPoint, yPoint, 3);
        }

        g2d.setTransform(transformaciónInicial);
    }

    private void cargarImagenesAviones() {
        imagenesAviones = new ArrayList<>();
        String[] nombresDistintosAviones = {
                "avion1.PNG",
                "avion2.png",
                "avion3.PNG",
                "avion4.PNG",
                "avion5.PNG",
                "avion6.PNG",
                "avion7.PNG",
        };

        for (String nombreAvion : nombresDistintosAviones) {
            try {
                Image imagenAvion = ImageIO.read(new File("/resources/img/" + nombreAvion));
                imagenesAviones.add(imagenAvion);
            } catch (IOException e) {
                //En caso de no poder cargarlo la imagen
                System.err.println("No se ha podido cargar el avión" + e.getMessage());
            }
        }
    }

    private void cargarImagenMapa() {
        try {
            mapaAeropuerto = ImageIO.read(new File("/resources/img/aeropuerto.PNG"));
        } catch (IOException e) {
            //En caso de no poder cargarlo el fondo se queda negro
            System.err.println("No se ha podido cargar el mapa" + e.getMessage());
        }
    }

    private boolean estaMapa(Avion avion) {
        int x = avion.getX();
        int y = avion.getY();

        //Compruebo si
        boolean devolver = x >= 0 && x <= widthReal && y >= 0 && y <= heightReal;
        return devolver;
    }

    private Image obtenerImagenCache(Image imagenOriginal, int width, int height) {
        String clave = width + "x" + height + System.identityHashCode(imagenOriginal);

        //Si el mapa no contiene la clave se incluye con la imagen
        if (!cacheImagenes.containsKey(clave)) {
            Image imagenEscalada = imagenOriginal.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            cacheImagenes.put(clave, imagenEscalada);
        }

        return cacheImagenes.get(clave);
    }

    private void limpiarCache() {
        cacheImagenes.clear();
        mapaCache = null;
    }
}