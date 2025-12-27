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

public class ControladorMovimiento implements ObservadorTiempo{

    private MapPanel mapPanel;
    private ArrayList<Vuelo> vuelos;
    private Map<String, Avion> avionesEnCurso;
    private Set<Vuelo> vuelosProcesados;
    private Set<Vuelo> vuelosSalidaProcesados;
    private threadAnimacion threadAnimacion;
    private boolean ejecutando;
    private boolean siguientePistaAterrizajeHorizontal = true;
    private boolean siguientePistaDespeguejeHorizontal = true;

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

    //Puertas de embarque
    private static final Point P1 = new Point(312, 209);
    private static final Point P2 = new Point(364, 209);
    private static final Point P3 = new Point(444, 272);
    private static final Point P4 = new Point(493, 272);
    private static final Point P5 = new Point(543, 272);
    private static final Point P6 = new Point(593, 272);
    private static final Point P7 = new Point(643, 272);
    private static final Point P8 = new Point(713, 210);
    private static final Point P9 = new Point(765, 210);

    //Auxiliares terminal
    private static final Point ENTRADATERMINAL = new Point(713,331);
    private static final int ALTURAAPARCAR = 303;

    public ControladorMovimiento(MapPanel mapPanel, ArrayList<Vuelo> vuelos) {
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
        verificarVuelosSalida(nuevoTiempo);
    }

    @Override
    public void cambioEstadoPausa(boolean pausa) {

    }

    private void verificarVuelosSalida(LocalDateTime momentoActual) {
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

            //Si es mas tarde de la hora en la que debe aparecer o es la hora a la que debe aparecer aparece
            if (momentoActual.isAfter(horaSalida) || momentoActual.equals(horaSalida)) {
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
        avion.setEstacionamientoHangar(posicionHangar);
        avion.setSpeed(0);
        avion.setEstadoAvion(EstadoAvion.ESTACIONADO_HANGAR);
        avion.setEnHangar(true);
    }

    private void iniciarVueloSalida(Vuelo vuelo) {
        Avion avion = vuelo.getAvion();

        boolean pistaHorizontal;
        pistaHorizontal = siguientePistaDespeguejeHorizontal;
        siguientePistaDespeguejeHorizontal = !siguientePistaDespeguejeHorizontal;

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
        Point interseccionEntradaNortePista1 = new Point((int) CENTROENTRADANORTE.getX(), (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY());
        ruta.add(interseccionEntradaNortePista1);

        //Llega a la interseccion entre la pista de aterrizaje 1 y la pista unión
        Point interseccionUnionPistas1PistaAterrizaje = new Point((int) UNIONPISTAS1CENTROSOUTH.getX(), (int) PISTAATERRIZAJEABAJOCENTROIZDA.getY());
        ruta.add(interseccionUnionPistas1PistaAterrizaje);

        //Sube hasta la intersección entre la pista de despegue 1 y la pista unión
        Point interseccionUnionPistas1PistaDespegue = new Point((int) UNIONPISTAS1CENTROSOUTH.getX(), (int) PISTADESPEGUEARRIBACENTROIZDA.getY());
        ruta.add(interseccionUnionPistas1PistaDespegue);

        //Recorre la pista hasta salir
        ruta.add(PISTADESPEGUEARRIBACENTROIZDA);
        Point puntoSalida = new Point(-1, (int) PISTADESPEGUEARRIBACENTRODCHA.getY());
        ruta.add(puntoSalida);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
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

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
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
            pistaHorizontal = false;

            if (nPista.equals("1")) {
                pistaHorizontal = true;
            } else {
                pistaHorizontal = false;
            }

        } else {
            pistaHorizontal = siguientePistaAterrizajeHorizontal;
            if (pistaHorizontal) {
                vuelo.setPista(new Pista("1", false));
            } else {
                vuelo.setPista(new Pista("2", false));
            }
            siguientePistaAterrizajeHorizontal = !siguientePistaAterrizajeHorizontal;
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
                ArrayList<Point> ruta = avion.getRutaActual();
                int i = avion.getPointIndex();
                if (ruta != null && i >= 0 && i < ruta.size()) {
                    Point destino = ruta.get(i);

                    if (estaVolando(destino)) {
                        avion.setEstadoAvion(EstadoAvion.DESPEGANDO);
                        avion.setSpeed(4);
                    } else if (estaEnPista(destino)) {
                        avion.setEstadoAvion(EstadoAvion.RODANDO_A_PISTA);
                        avion.setSpeed(3);
                    } else if (estaEnAreaHangarPunto(destino)) {
                        avion.setEstadoAvion(EstadoAvion.RODANDO_A_HANGAR);
                        avion.setSpeed(0.8);
                    } else {
                        avion.setSpeed(1.3);
                    }
                }
            }
        }
    }

    //Verfica si está dentro del área del hangar
    public static boolean estaEnHangar(Avion avion) {
        int x = avion.getX();
        int y = avion.getY();
        boolean devolver = false;

        //Si se encuentra dentro de los limites del hangar se devuelver true
        if (x >= HANGARMINX && x <= HANGARMAXX && y >= HANGARMINY && y <= HANGARMAXY) {
            devolver = true;
        }
        return devolver;
    }

    //Verifica si está en pista
    private boolean estaEnPista(Point p) {
        int x = p.x;
        int y = p.y;

        //Cada condicion corresponde a una pista
        boolean cond1 = (y >= 440 && y <= 530 && x >= 19 && x <= 750);
        boolean cond2 = (y >= 350 && y <= 430 && x >= 19 && x <= 750);
        boolean cond3 = (y >= 50 && y <= 680 && x >= 135 && x <= 210);
        boolean cond4 = (y >= 50 && y <= 680 && x >= 47 && x <= 120);

        //Si está en alguna de las pistas devuelve true
        if (cond1 || cond2 || cond3 || cond4) {
            return true;
        } else {
            return false;
        }
    }

    private boolean estaVolando(Point p) {
        int x = p.x;
        int y = p.y;

        /*Si esta llegando o saliendo de alguna de las pistas
        se tiene que cumplir alguna de estas condiciones*/
        boolean cond1 = y < 50;
        boolean cond2 = x > 750;
        boolean cond3 = x < 19;

        if (cond1 || cond2 || cond3) {
            return true;
        } else {
            return false;
        }
    }

    private boolean estaEnAreaHangarPunto(Point p) {
        int x = p.x;
        int y = p.y;
        boolean devolver = false;

        if (x >= HANGARMINX && x <= HANGARMAXX && y >= HANGARMINY && y <= HANGARMAXY) {
            devolver = true;
        }
        return devolver;
    }
}