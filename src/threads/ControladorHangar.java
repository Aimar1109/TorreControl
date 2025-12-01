package threads;

import domain.Avion;
import domain.EstadoAvion;
import domain.Pista;
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
    private Set<Vuelo> vuelosSalidaProcesados;
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
        this.vuelosSalidaProcesados = new HashSet<>();
        this.ejecutando = true;

        RelojGlobal.getInstancia().addObservador(this);

        iniciarAnimacion();
    }

    @Override
    public void actualizarTiempo(LocalDateTime nuevoTiempo) {
        verificarVuelosLlegada(nuevoTiempo);
    }

    @Override
    public void cambioEstadoPausa(boolean pausa) {

    }

    private void verificarVuelosSalid(LocalDateTime momentoActual) {
        for (Vuelo v : vuelos) {
            //Si la llegada no es Bilbao salta en el bucle
            if (!v.getOrigen().getCiudad().equals("Bilbao")) {
                continue;
            }

            //Si ya se ha procesado salta en el bucle
            if (vuelosSalidaProcesados.contains(v)) {
                continue;
            }

            long delay = v.getDelayed();
            LocalDateTime horaSalida = v.getFechaHoraProgramada().plusMinutes(delay);
            //El avion aparece 2 minutos antes de la llegada
            LocalDateTime horaAparece = horaSalida.minusMinutes(2);

            //Si es mas tarde de la hora en la que debe aparecer o es la hora a la que debe aparecer aparece
            if (momentoActual.isAfter(horaAparece) || momentoActual.equals(horaAparece)) {
                Avion avion = v.getAvion();

                //Verifico si el avión está ya en el aeropuerto
                if (!avionesEnCurso.containsKey(avion.getMatricula())) {
                    //Si no está en el aeropuerto lo colocamos manualmente en el hangar
                    colocarManualmente(avion);
                    avionesEnCurso.put(avion.getMatricula(), avion);

                    mapPanel.addAvion(avion);
                }
                iniciarVueloSalida(v);
                vuelosSalidaProcesados.add(v);
            }

        }
    }

    private void colocarManualmente(Avion avion) {
        //Se coloca el avion aleatoriamente en el hangar
        Point posicionHangar = calcularPosicionHangar();
        avion.setX((int) posicionHangar.getX());
        avion.setY((int) posicionHangar.getY());
        avion.setEnHangar(true);
        avion.setEstacionamientoHangar(posicionHangar);
        avion.setSpeed(0);
        avion.setEstadoAvion(EstadoAvion.ESTACIONADO_HANGAR);
    }

    private void iniciarVueloSalida(Vuelo vuelo) {
        Avion avion = vuelo.getAvion();

        boolean pistaHorizontal;
        pistaHorizontal = avionesEnCurso.values().size() % 2 == 0;

        //Se asigna la pista
        if (pistaHorizontal) {
            setDespegueHorizontal(avion, vuelo);
        } else {
            setDespegueVertical(avion, vuelo);
        }
    }

    private void setDespegueHorizontal(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //El primer punto es la salida norte
        ruta.add(CENTROENTRADANORTE);

        //Llega a la pista de aterrizaje 1
        Point interseccionEntradaNortePista1 = new Point((int) CENTROENTRADANORTE.getX(), (int) PISTAATERRIZAJEABAJOCENTROIZDA.getY());
        ruta.add(interseccionEntradaNortePista1);

        //Llega a la interseccion entr la pista de aterrizaje 1 y la pista unión
        ruta.add(UNIONPISTAS1CENTROSOUTH);

        //Sube hasta la intersección entre la pista de despegue 1 y la pista unión
        ruta.add(PISTADESPEGUEARRIBACENTRODCHA);

        //Recorre la pista hasta salir
        ruta.add(PISTADESPEGUEARRIBACENTROIZDA);
        Point puntoSalida = new Point(-1, (int) PISTADESPEGUEARRIBACENTRODCHA.getY());
        ruta.add(puntoSalida);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setDespegueVertical(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //El primer punto es la salida norte
        ruta.add(CENTROENTRADAOESTE);

        //Llega a la pista de aterrizaje 1
        Point interseccionEntradaOestePista2 = new Point((int) PISTAATERRIZAJEDERECHACENTRONORTH.getX(), (int) CENTROENTRADAOESTE.getY());
        ruta.add(interseccionEntradaOestePista2);

        //Llega a la interseccion entr la pista de aterrizaje 1 y la pista de aterrizaje 2
        Point interseccionPistaAterrizaje1PistaAterrizaje2 = new Point((int) PISTAATERRIZAJEDERECHACENTRONORTH.getX(), (int) PISTAATERRIZAJEABAJOCENTROIZDA.getY());
        ruta.add(interseccionPistaAterrizaje1PistaAterrizaje2);

        //Va a la intersección entre la pista de aterrizaje 1 y la pista de salida 2
        Point interseccionPistaAterrizaje1PistaSalidaje2 = new Point((int) PISTADESPEGUEIZQUIERDACENTRONORTH.getX(), (int) PISTAATERRIZAJEABAJOCENTROIZDA.getY());
        ruta.add(interseccionPistaAterrizaje1PistaSalidaje2);

        //Baja hasta el inicio de la pista de despegue 2
        ruta.add(PISTADESPEGUEIZQUIERDACENTROSOUTH);

        //Recorre la pista hasta salir
        ruta.add(PISTADESPEGUEIZQUIERDACENTRONORTH);
        Point puntoSalida = new Point((int) PISTADESPEGUEIZQUIERDACENTRONORTH.getX(), -1);
        ruta.add(puntoSalida);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void verificarVuelosLlegada(LocalDateTime momentoActual) {
        for (Vuelo v : vuelos) {
            //Si la llegada no es Bilbao salta en el bucle
            if (!v.getDestino().getCiudad().equals("Bilbao")) {
                continue;
            }

            //Si ya se ha procesado salta en el bucle
            if (vuelosProcesados.contains(v)) {
                continue;
            }

            long delay = v.getDelayed();
            LocalDateTime horaLLegada = v.getFechaHoraProgramada().plusMinutes(delay);
            //El avion aparece 2 minutos antes de la llegada
            LocalDateTime horaAparece = horaLLegada.minusMinutes(2);

            //Si es mas tarde de la hora en la que debe aparecer o es la hora a la que debe aparecer aparece
            if (momentoActual.isAfter(horaAparece) || momentoActual.equals(horaAparece)) {
                iniciarVuelo(v);
                vuelosProcesados.add(v);
            }

        }
    }

    private void iniciarVuelo(Vuelo vuelo)  {
        Avion avion = vuelo.getAvion();

        boolean pistaHorizontal;
        Pista pistaAsignada = vuelo.getPista();

        //Si la pista está asignada(mediante las listas del panel principal) no se asigna en función del anterior vuelo
        if (pistaAsignada != null) {
            String nPista = pistaAsignada.getNumero();

            if (nPista.equals("1")) {
                pistaHorizontal = true;
            } else {
                pistaHorizontal = false;
            }

        } else {
            pistaHorizontal = avionesEnCurso.values().size() % 2 == 0;
        }

        //Se asigna la pista
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
        Point finalAterrizaje = new Point((int) (PISTAATERRIZAJEDERECHACENTRONORTH.getX()), (int) PISTAATERRIZAJEDERECHACENTROSOUTH.getY() - 10);
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

        //Si se encuentra dentro de los limites del hangar se devuelver true
        if (x >= HANGARMINX && x <= HANGARMAXX && y >= HANGARMINY && y <= HANGARMAXY) {
            devolver = true;
        }
        return devolver;
    }

}