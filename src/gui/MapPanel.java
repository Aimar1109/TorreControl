package gui;

import domain.Avion;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static threads.ControladorMovimiento.estaEnHangar;

public class MapPanel extends JPanel {

    //Tamaño lógico fijo para evitar problemas con el escalado de coordenadas al variar el tamaño de la ventana.
    private final int widthReal = 1000;
    private final int heightReal = 700;
    //Aviones visualizados en el mapa
    private List<Avion> avionesAeropuerto = new ArrayList<>();
    //Imagen del aeropuerto
    private Image mapaAeropuerto;
    //Imagenes de los aviones
    private List<Image> imagenesAviones = new ArrayList<>();
    //Caché de imagenes
    private HashMap<String, Image> cacheImagenes = new HashMap<>();
    private Image mapaCache = null;
    private int ultimoWidth = -1;
    private int ultimoHeight = -1;

    public MapPanel() {
        super(null, true);

        cargarImagenMapa();
        cargarImagenesAviones();
        setBackground(Color.BLACK);
        registrarImpresionClicks();
    }

    //Actualiza lista aviones
    public void setAviones (List<Avion> aviones) {
        this.avionesAeropuerto = aviones;

        if (aviones != null) {
            for (int i = 0; i < aviones.size(); i++) {
                Avion a = aviones.get(i);
            }
        }

        asignarImagenesAleatorias();
        repaint();
    }

    //Añade a lista aviones
    public void addAvion (Avion avion) {
        this.avionesAeropuerto.add(avion);
        asignarImagenAleatoria(avion);
        repaint();
    }

    //Asigna Imagen Aleatoria a un avión
    private void asignarImagenAleatoria(Avion avion) {
        if (imagenesAviones != null && !imagenesAviones.isEmpty()){
            Random r = new Random();
            int aleatorio = r.nextInt(imagenesAviones.size());
            avion.setImagen(imagenesAviones.get(aleatorio));
        }
    }

    //Asigna Imagen Aleatoria a todos los aviones
    private void asignarImagenesAleatorias() {
        if (imagenesAviones != null && !imagenesAviones.isEmpty()) {
            for (Avion avion : avionesAeropuerto) {
                if (avion.getImagen() == null) {
                    asignarImagenAleatoria(avion);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        //Se activa antialiasing para mayor calidad
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //Escalado para conseguir coordenadas relativas
        double escaladoX = (double)getWidth() / widthReal;
        double escaladoY = (double)getHeight() / heightReal;

        //Dibuja mapa del aeropuerto
        dibujarMapa(g2d);

        //Dibujo aviones únicamente si están en el mapa
        for (Avion avion: avionesAeropuerto) {
            //Si está dentro del hangar no se dibuja
            if (estaEnHangar(avion)) {
                continue;
            }

            boolean enMapa = estaMapa(avion);

            if (enMapa) {
                dibujarAvion(g2d, avion, escaladoX, escaladoY);
            }
        }
    }

    private void dibujarMapa(Graphics2D g2d) {
        int width = this.getWidth();
        int height = this.getHeight();

        //Si la ventana ha variado su tamaño
        if (mapaAeropuerto != null && (width != ultimoWidth || height != ultimoHeight || mapaCache == null)) {
            mapaCache = mapaAeropuerto.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ultimoHeight = height;
            ultimoWidth = width;
        }

        if (mapaCache != null) {
            g2d.drawImage(mapaCache, 0, 0, this);
        }
    }

    private void dibujarAvion(Graphics2D g2d, Avion avion, double escaladoX, double escaladoY) {
        //Coordenadas lógicas
        int x = (int) (avion.getX() * escaladoX);
        int y = (int) (avion.getY() * escaladoY);


        //Tamaño base del avion
        int tamanoBaseX = 20;
        int tamanoBaseY = 30;

        //Escalado en función del tamaño del mapa
        int tamanoRealX = (int) Math.max(tamanoBaseX * escaladoX, 10);
        int tamanoRealY = (int) Math.max(tamanoBaseY * escaladoY, 15);

        //Transformación inicial
        AffineTransform transformacionInicial = g2d.getTransform();

        //Se traslada al centro del avión y rota
        g2d.translate(x, y);
        g2d.rotate(avion.getAngulo());

        int mitadX = tamanoRealX/2;
        int mitadY = tamanoRealY/2;

        //Si la imagen existe se dibuja
        if (avion.getImagen() != null) {
            Image imagenAvionCacheEscalada = obtenerImagenCache(avion.getImagen(), tamanoRealX, tamanoRealY);
            g2d.drawImage(imagenAvionCacheEscalada,-mitadX, -mitadY, this);
        }
        //Si no se consigue acceder a la imagen, se crea un avión(polígono) manualmente
        else {
            int[] xPoint = {0, -(int)(tamanoRealX * 0.4), (int)(tamanoRealX * 0.4)};
            int[] yPoint = {-(int)(tamanoRealY * 0.5), (int)(tamanoRealY * 0.5), (int)(tamanoRealY * 0.5)};

            g2d.setColor(Color.CYAN);
            g2d.fillPolygon(xPoint, yPoint, 3);
            g2d.setColor(Color.WHITE);
            g2d.drawPolygon(xPoint, yPoint, 3);
        }

        g2d.setTransform(transformacionInicial);
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
                Image imagenAvion = ImageIO.read(new File("resources/img/" + nombreAvion));
                imagenesAviones.add(imagenAvion);
            } catch (IOException ignored) {
            }
        }
    }

    private void cargarImagenMapa() {
        try {
            mapaAeropuerto = ImageIO.read(new File("resources/img/aeropuerto.PNG"));
        } catch (IOException e) {
            mapaAeropuerto = null;
        }
    }

    private boolean estaMapa(Avion avion) {
        int x = avion.getX();
        int y = avion.getY();

        //Compruebo si está dentro del rango
        return x >= 0 && x <= widthReal && y >= 0 && y <= heightReal;
    }

    private Image obtenerImagenCache(Image imagenOriginal, int width, int height) {
        String clave = width + "x" + height + "@" + System.identityHashCode(imagenOriginal);

        //Si el mapa no contiene la clave se incluye con la imagen
        if (!cacheImagenes.containsKey(clave)) {
            Image imagenEscalada = imagenOriginal.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            cacheImagenes.put(clave, imagenEscalada);
        }

        return cacheImagenes.get(clave);
    }

    public void limpiarCache() {
        cacheImagenes.clear();
        mapaCache = null;
    }

    //Métodos temporales para descubrir coordenadas

    public void imprimirCoordenadasLogicas(int xPixel, int yPixel) {
        int xLogico = (int) Math.round((xPixel / (double) getWidth()) * widthReal);
        int yLogico = (int) Math.round((yPixel / (double) getHeight()) * heightReal);

        System.out.println(xLogico + ", " + yLogico);
    }

    private void registrarImpresionClicks() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                imprimirCoordenadasLogicas(e.getX(), e.getY());
            }
        });
    }
}