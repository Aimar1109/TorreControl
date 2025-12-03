package gui;

import domain.Avion;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static threads.ControladorHangar.estaEnHangar;

public class MapPanel extends JPanel {

    //Coordenadas

    //Pista 1 Aterrizaje
    private static final Point[] PISTAATERRIZAJEABAJO = {new Point(29,509), new Point(29,459), new Point(738, 459), new Point(738,509)};
    private static final Point PISTAATERRIZAJEABAJOCENTROIZDA = new Point(30,485);
    private static final Point PISTAATERRIZAJEABAJOCENTRODCHA = new Point(738,485);

    //Pista 1 Despegue
    private static final Point[] PISTADESPEGUEARRIBA = {new Point(29,418), new Point(29,367), new Point(738, 367), new Point(738,418)};
    private static final Point PISTADESPEGUEARRIBACENTROIZDA = new Point(30,392);
    private static final Point PISTADESPEGUEARRIBACENTRODCHA = new Point(738,392);

    //Pista 2 Aterrizaje
    private static final Point[] PISTAATERRIZAJEDERECHA = {new Point(149,667), new Point(149,69), new Point(198, 69), new Point(198,667)};
    private static final Point PISTAATERRIZAJEDERECHACENTRONORTH = new Point(174,69);
    private static final Point PISTAATERRIZAJEDERECHACENTROSOUTH = new Point(174,669);

    //Pista 2 Despegue
    private static final Point[] PISTADESPEGUEIZQUIERDA = {new Point(59,667), new Point(59,69), new Point(108, 69), new Point(108,667)};
    private static final Point PISTADESPEGUEIZQUIERDACENTRONORTH = new Point(84,69);
    private static final Point PISTADESPEGUEIZQUIERDACENTROSOUTH = new Point(84,669);

    //Pìstas Auxiliares: Unión Pista 1
    private static final Point[] UNIONPISTAS1 = {new Point(690,509), new Point(690,370), new Point(738, 376), new Point(738,509)};
    private static final Point UNIONPISTAS1CENTRONORTH = new Point(713,370);
    private static final Point UNIONPISTAS1CENTROSOUTH = new Point(713,509);

    //Hangar: Area
    private static final Point[] AREAHANGAR = {new Point(410,655), new Point(410,540), new Point(607, 540), new Point(607,655)};

    //Hangar: Entrada Norte
    private static final Point[] ENTRADANORTE = {new Point(470, 655), new Point(548, 655)};
    private static final Point CENTROENTRADANORTE = new Point(509, 655);
    private static final Point[] PISTAENTRADANORTE = {new Point(470, 537), new Point(470, 510), new Point(548,510), new Point(548,537)};

    //Hangar: Entrada Oeste
    private static final Point[] ENTRADAOESTE = {new Point(409, 578), new Point(409, 620)};
    private static final Point CENTROENTRADAOESTE = new Point(409, 599);
    private static final Point[] PISTAENTRADAOESTE = {new Point(200, 620), new Point(200, 578), new Point(407,578), new Point(407,620)};

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
        int tamañoBaseX = 20;
        int tamañoBaseY = 30;

        //Escalado en función del tamaño del mapa
        int tamañoRealX = (int) Math.max(tamañoBaseX * escaladoX, 10);
        int tamañoRealY = (int) Math.max(tamañoBaseY * escaladoY, 15);

        //Transformación inicial
        AffineTransform transformacionInicial = g2d.getTransform();

        //Se traslada al centro del avión y rota
        g2d.translate(x, y);
        g2d.rotate(avion.getAngulo());

        int mitadX = tamañoRealX/2;
        int mitadY = tamañoRealY/2;

        //Si la imagen existe se dibuja
        if (avion.getImagen() != null) {
            Image imagenAvionCacheEscalada = obtenerImagenCache(avion.getImagen(), tamañoRealX, tamañoRealY);
            g2d.drawImage(imagenAvionCacheEscalada,-mitadX, -mitadY, this);
        }
        //Si no se consigue acceder a la imagen, se crea un avión(polígono) manualmente
        else {
            int[] xPoint = {0, -(int)(tamañoRealX * 0.4), (int)(tamañoRealX * 0.4)};
            int[] yPoint = {-(int)(tamañoRealY * 0.5), (int)(tamañoRealY * 0.5), (int)(tamañoRealY * 0.5)};

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
            } catch (IOException e) {
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
}