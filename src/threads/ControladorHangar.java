package threads;

import domain.Avion;
import domain.Vuelo;
import gui.MapPanel;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class ControladorHangar implements ObservadorTiempo{

    private MapPanel mapPanel;
    private ArrayList<Vuelo> vuelos;
    private Map<String, Avion> avionesEnCurso;
    private Set<Vuelo> vuelosProcesados;
    private threadAnimacion threadAnimacion;
    private boolean ejecutando;

    //Coordenadas

    //Pista 1 Aterrizaje
    private static final Point PISTAATERRIZAJEABAJOCENTROIZDA = new Point(30,485);
    private static final Point PISTAATERRIZAJEABAJOCENTRODCHA = new Point(738,485);

    //Pista 1 Despegue
    private static final Point PISTADESPEGUEARRIBACENTROIZDA = new Point(30,392);
    private static final Point PISTADESPEGUEARRIBACENTRODCHA = new Point(738,392);

    //Pista 2 Aterrizaje
    private static final Point PISTAATERRIZAJEDERECHACENTRONORTH = new Point(174,69);
    private static final Point PISTAATERRIZAJEDERECHACENTROSOUTH = new Point(174,669);

    //Pista 2 Despegue
    private static final Point PISTADESPEGUEIZQUIERDACENTRONORTH = new Point(84,69);
    private static final Point PISTADESPEGUEIZQUIERDACENTROSOUTH = new Point(84,669);

    //Hangar: Area
    private static final int HANGARMINX = 410;
    private static final int HANGARMAXX = 607;
    private static final int HANGARMINY = 540;
    private static final int HANGARMAXY = 655;

    //Hangar: Entrada Norte
    private static final Point CENTROENTRADANORTE = new Point(509, 655);

    //Hangar: Entrada Oeste
    private static final Point CENTROENTRADAOESTE = new Point(409, 599);

    //Union pistas 1
    private static final Point UNIONPISTAS1CENTRONORTH = new Point(713,370);
    private static final Point UNIONPISTAS1CENTROSOUTH = new Point(713,509);

    public ControladorHangar(MapPanel mapPanel, ArrayList<Vuelo> vuelos) {
        this.mapPanel = mapPanel;
        this.vuelos = vuelos;
        this.avionesEnCurso = new HashMap<>();
        this.vuelosProcesados = new HashSet<>();
        this.ejecutando = true;

        RelojGlobal.getInstancia().addObservador(this);

        iniciarAnimacion();
    }

    @Override
    public void actualizarTiempo(LocalDateTime nuevoTiempo) {
        verificarSiIniciar(nuevoTiempo);
    }

    @Override
    public void cambioEstadoPausa(boolean pausa) {

    }

    private void verificarSiIniciar(LocalDateTime momentoActual) {
        for (Vuelo v : vuelos) {
            if (!v.getDestino().getCiudad().equals("Bilbao")) {
                continue;
            }

            if (vuelosProcesados.contains(v)) {
                continue;
            }

            long delay = v.getDelayed();
            LocalDateTime horaLLegada = v.getFechaHoraProgramada().plusMinutes(delay);
            LocalDateTime horaAparece = horaLLegada.minusMinutes(2);

            if (momentoActual.isAfter(horaAparece) || momentoActual.equals(horaAparece)) {
                iniciarVuelo(v);
                vuelosProcesados.add(v);
            }

        }
    }

    private void iniciarVuelo(Vuelo vuelo)  {
        Avion avion = vuelo.getAvion();

        boolean pistaHorizontal = avionesEnCurso.values().size() % 2 == 0;

        if (pistaHorizontal) {
            setAterrizajeHorizontal(avion, vuelo);
        } else {
            setAterrizajeVertical(avion, vuelo);
        }

        //Agrego el avión al mapa
        avionesEnCurso.put(avion.getMatricula(), avion);

        //Actualizo la lista de aviones del MapPanel
        ArrayList<Avion> avionesMapPanel = new ArrayList<>(avionesEnCurso.values());
        mapPanel.setAviones(avionesMapPanel);
    }

    private void setAterrizajeHorizontal(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        avion.setX(1050);
        int alturaPista = (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY();
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Recorre prácticamente toda la pista
        ruta.add(PISTAATERRIZAJEABAJOCENTRODCHA);
        Point finalAterrizaje = new Point((int) (PISTAATERRIZAJEABAJOCENTROIZDA.getX() + 5), (int) PISTAATERRIZAJEABAJOCENTROIZDA.getY());
        ruta.add(finalAterrizaje);

        //Vuelve y entra por la entrada norte
        Point interseccionEntradaNortePista1 = new Point((int) CENTROENTRADANORTE.getX(), (int) PISTAATERRIZAJEABAJOCENTROIZDA.getY());
        ruta.add(interseccionEntradaNortePista1);
        ruta.add(CENTROENTRADANORTE);
        Point posicionHangar = calcularPosicionHangar();
        ruta.add(posicionHangar);
        avion.setEstacionamientoHangar(posicionHangar);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeVertical(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        int anchoPista = (int) PISTAATERRIZAJEDERECHACENTRONORTH.getX();
        avion.setX(anchoPista);
        int alturaPista = (int) 0;
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Recorre prácticamente toda la pista
        ruta.add(PISTAATERRIZAJEDERECHACENTRONORTH);
        Point finalAterrizaje = new Point((int) (PISTAATERRIZAJEDERECHACENTRONORTH.getX()), (int) PISTAATERRIZAJEABAJOCENTROIZDA.getY() - 50);
        ruta.add(finalAterrizaje);

        //Vuelve y entra por la entrada norte
        Point interseccionEntradaOestePista2 = new Point((int) PISTAATERRIZAJEDERECHACENTRONORTH.getX(), (int) CENTROENTRADAOESTE.getY());
        ruta.add(interseccionEntradaOestePista2);
        ruta.add(CENTROENTRADAOESTE);
        Point posicionHangar = calcularPosicionHangar();
        ruta.add(posicionHangar);
        avion.setEstacionamientoHangar(posicionHangar);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private Point calcularPosicionHangar() {
        Random r = new Random();

        //Doy ciertos margenes para que en los movimentos sean más realistas
        int xMax = HANGARMAXX - 5;
        int xMin = HANGARMINX + 5;
        int yMax = HANGARMAXY - 5;
        int yMin = HANGARMINY + 5;

        int x = r.nextInt(xMin, xMax);
        int y = r.nextInt(yMin, yMax);
        Point posicion = new Point(x, y);

        return posicion;
    }

    private void iniciarAnimacion() {
        this.threadAnimacion = new threadAnimacion();
        this.threadAnimacion.setDaemon(true);
        this.threadAnimacion.start();
    }

    private class threadAnimacion extends Thread {
        @Override
        public void run() {
            while (ejecutando) {
                try {
                    //Si el reloj no esta pausado se actualiza la posicion de los aviones
                    if (!RelojGlobal.getInstancia().isPausado()) {
                        synchronized (avionesEnCurso) {
                            for (Avion a : avionesEnCurso.values()) {
                                actualizarAvion(a);
                            }
                        }
                        mapPanel.repaint();
                    }

                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private void detener() {
        ejecutando = false;
        if (this.threadAnimacion != null) {
            this.threadAnimacion.interrupt();
        }
        RelojGlobal.getInstancia().eliminarObservador(this);
    }

    private void actualizarAvion(Avion avion) {
        //Si esta estacionado no hace nada
        if (avion.isEnHangar()) {
            return;
        }

        avion.actualizarPosicion();

        //Se verifica si ha llegado
        if (avion.enDestino()) {
            boolean haySiguientePunto = avion.siguientePunto();

            if (!haySiguientePunto) {
                avion.setEnHangar(true);
                avion.setSpeed(0);
            } else {
                if (estaEnHangar(avion)) {
                    //Si esta estacionando va más lento
                    avion.setSpeed(0.8);
                }
            }
        }
    }

    //Verfica si está dentro del área del hangar
    private boolean estaEnHangar(Avion avion) {
        int x = avion.getX();
        int y = avion.getY();

        boolean devolver = false;
        if (x >= HANGARMINX && x <= HANGARMAXX && y >= HANGARMINY && y <= HANGARMAXY) {
            devolver = true;
        }
        return devolver;
    }

}
