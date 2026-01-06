package threads;

import domain.*;
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

            //Si es más tarde de la hora en la que debe aparecer o es la hora a la que debe aparecer aparece
            if (momentoActual.isAfter(horaSalida) || momentoActual.equals(horaSalida)) {
                Avion avion = v.getAvion();

                //Verifico si el avión está ya en el aeropuerto
                if (!avionesEnCurso.containsKey(avion.getMatricula())) {
                    //Si no está en el aeropuerto lo colocamos manualmente en el hangar
                    colocarManualmenteSalida(avion, v);
                    avionesEnCurso.put(avion.getMatricula(), avion);

                    mapPanel.addAvion(avion);
                }
                iniciarVueloSalida(v);
                vuelosSalidaProcesados.add(v);
            }

        }
    }

    private void colocarManualmenteSalida(Avion avion, Vuelo vuelo) {
        PuertaEmbarque puertaEmbarque = vuelo.getPuerta();
        if (puertaEmbarque != null) {
            Point p = getPuntoPuertaEmbarque(puertaEmbarque);
            avion.setX((int) p.getX());
            avion.setY((int) p.getY());
            avion.setEstacionamientoHangar(null);
            avion.setSpeed(0);
            avion.setEstadoAvion(EstadoAvion.ESTACIONADO_PUERTA);
            avion.setEnHangar(false);
            puertaEmbarque.setOcupada(true);
        } else {
            colocarManualmenteHangar(avion);
        }
    }

    private void colocarManualmenteHangar(Avion avion) {
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

        PuertaEmbarque puertaEmbarque = vuelo.getPuerta();

        //Se asigna la pista
        if (pistaHorizontal) {
            if (puertaEmbarque != null) {
                switch (puertaEmbarque.getNumero()) {
                    case 1 -> setDespegueHorizontalP1(avion, vuelo);
                    case 2 -> setDespegueHorizontalP2(avion, vuelo);
                    case 3 -> setDespegueHorizontalP3(avion, vuelo);
                    case 4 -> setDespegueHorizontalP4(avion, vuelo);
                    case 5 -> setDespegueHorizontalP5(avion, vuelo);
                    case 6 -> setDespegueHorizontalP6(avion, vuelo);
                    case 7 -> setDespegueHorizontalP7(avion, vuelo);
                    case 8 -> setDespegueHorizontalP8(avion, vuelo);
                    case 9 -> setDespegueHorizontalP9(avion, vuelo);
                    default -> setDespegueHorizontalP1(avion, vuelo);
                }
            } else {
                setDespegueHorizontalHangar(avion, vuelo);
            }
        } else {
            if (puertaEmbarque != null) {
                switch (puertaEmbarque.getNumero()) {
                    case 1 -> setDespegueVerticalP1(avion, vuelo);
                    case 2 -> setDespegueVerticalP2(avion, vuelo);
                    case 3 -> setDespegueVerticalP3(avion, vuelo);
                    case 4 -> setDespegueVerticalP4(avion, vuelo);
                    case 5 -> setDespegueVerticalP5(avion, vuelo);
                    case 6 -> setDespegueVerticalP6(avion, vuelo);
                    case 7 -> setDespegueVerticalP7(avion, vuelo);
                    case 8 -> setDespegueVerticalP8(avion, vuelo);
                    case 9 -> setDespegueVerticalP9(avion, vuelo);
                    default -> setDespegueVerticalP1(avion, vuelo);
                }
            } else {
                setDespegueVerticalHangar(avion, vuelo);
            }
        }
    }

    private void setDespegueHorizontalHangar(Avion avion, Vuelo vuelo) {
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

    //Función que establece la ruta global que todos los despegues horizontales desde las puertas de embarque siguen
    private void despegueHorizontalPuertaGlobal(ArrayList<Point> ruta) {
        //Sale de la terminal
        Point puntoInicioAparcamiento = new Point((int) ENTRADATERMINAL.getX(), ALTURAAPARCAR);
        ruta.add(puntoInicioAparcamiento);
        ruta.add(ENTRADATERMINAL);
        ruta.add(UNIONPISTAS1CENTRONORTH);

        //Baja hasta la entre la pista de despegue 1
        Point interseccionUnionPistas1PistaDespegue = new Point((int) UNIONPISTAS1CENTROSOUTH.getX(), (int) PISTADESPEGUEARRIBACENTROIZDA.getY());
        ruta.add(interseccionUnionPistas1PistaDespegue);

        //Recorre la pista hasta salir
        ruta.add(PISTADESPEGUEARRIBACENTROIZDA);
        Point puntoSalida = new Point(-1, (int) PISTADESPEGUEARRIBACENTRODCHA.getY());
        ruta.add(puntoSalida);
    }

    private void setDespegueHorizontalP1(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P1);
        Point puntoParaleloAPuerta = new Point((int) P1.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Se añaden a la ruta los puntos globales que todos los despegues horizontales desde la terminal siguen
        despegueHorizontalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueHorizontalP2(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P2);
        Point puntoParaleloAPuerta = new Point((int) P2.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Se añaden a la ruta los puntos globales que todos los despegues horizontales desde la terminal siguen
        despegueHorizontalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueHorizontalP3(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P3);
        Point puntoParaleloAPuerta = new Point((int) P3.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Se añaden a la ruta los puntos globales que todos los despegues horizontales desde la terminal siguen
        despegueHorizontalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueHorizontalP4(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P4);
        Point puntoParaleloAPuerta = new Point((int) P4.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Se añaden a la ruta los puntos globales que todos los despegues horizontales desde la terminal siguen
        despegueHorizontalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueHorizontalP5(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P5);
        Point puntoParaleloAPuerta = new Point((int) P5.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Se añaden a la ruta los puntos globales que todos los despegues horizontales desde la terminal siguen
        despegueHorizontalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueHorizontalP6(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P6);
        Point puntoParaleloAPuerta = new Point((int) P6.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Se añaden a la ruta los puntos globales que todos los despegues horizontales desde la terminal siguen
        despegueHorizontalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueHorizontalP7(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P7);
        Point puntoParaleloAPuerta = new Point((int) P7.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Se añaden a la ruta los puntos globales que todos los despegues horizontales desde la terminal siguen
        despegueHorizontalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueHorizontalP8(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P8);
        Point puntoParaleloAPuerta = new Point((int) P8.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Se añaden a la ruta los puntos globales que todos los despegues horizontales desde la terminal siguen
        despegueHorizontalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atrás en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueHorizontalP9(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P9);
        Point puntoParaleloAPuerta = new Point((int) P9.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Se añaden a la ruta los puntos globales que todos los despegues horizontales desde la terminal siguen
        despegueHorizontalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueVerticalHangar(Avion avion, Vuelo vuelo) {
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
        Point puntoInicioDespegue = new Point((int) PISTADESPEGUEIZQUIERDACENTROSOUTH.getX(), (int) (PISTADESPEGUEIZQUIERDACENTROSOUTH.getY() - 20));
        ruta.add(puntoInicioDespegue);

        //Recorre la pista hasta salir
        ruta.add(PISTADESPEGUEIZQUIERDACENTRONORTH);
        Point puntoSalida = new Point((int) PISTADESPEGUEIZQUIERDACENTRONORTH.getX(), -1);
        ruta.add(puntoSalida);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void despegueVerticalPuertaGlobal(ArrayList<Point> ruta) {
        //Sale de la terminal
        Point puntoInicioAparcamiento = new Point((int) ENTRADATERMINAL.getX(), ALTURAAPARCAR);
        ruta.add(puntoInicioAparcamiento);
        ruta.add(ENTRADATERMINAL);
        ruta.add(UNIONPISTAS1CENTRONORTH);

        //Baja hasta la entre la pista de aterrizaje 1
        Point interseccionUnionPistas1PistaAterrizaje = new Point((int) UNIONPISTAS1CENTROSOUTH.getX(), (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY());
        ruta.add(interseccionUnionPistas1PistaAterrizaje);

        //Llega a la interseccion entr la pista de aterrizaje 1 y la pista de aterrizaje 2
        Point interseccionPistaAterrizaje1PistaAterrizaje2 = new Point((int) PISTAATERRIZAJEDERECHACENTRONORTH.getX(), (int) PISTAATERRIZAJEABAJOCENTROIZDA.getY());
        ruta.add(interseccionPistaAterrizaje1PistaAterrizaje2);

        //Va a la intersección entre la pista de aterrizaje 1 y la pista de salida 2
        Point interseccionPistaAterrizaje1PistaSalidaje2 = new Point((int) PISTADESPEGUEIZQUIERDACENTRONORTH.getX(), (int) PISTAATERRIZAJEABAJOCENTROIZDA.getY());
        ruta.add(interseccionPistaAterrizaje1PistaSalidaje2);

        //Baja hasta el inicio de la pista de despegue 2
        Point puntoInicioDespegue = new Point((int) PISTADESPEGUEIZQUIERDACENTROSOUTH.getX(), (int) (PISTADESPEGUEIZQUIERDACENTROSOUTH.getY() - 20));
        ruta.add(puntoInicioDespegue);

        //Recorre la pista hasta salir
        ruta.add(PISTADESPEGUEIZQUIERDACENTRONORTH);
        Point puntoSalida = new Point((int) PISTADESPEGUEIZQUIERDACENTRONORTH.getX(), -1);
        ruta.add(puntoSalida);
    }

    private void setDespegueVerticalP1(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P1);
        Point puntoParaleloAPuerta = new Point((int) P1.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Añade los puntos globales que todos los despegues verticales desde la puerta de embarque siguen
        despegueVerticalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueVerticalP2(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P2);
        Point puntoParaleloAPuerta = new Point((int) P2.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Añade los puntos globales que todos los despegues verticales desde la puerta de embarque siguen
        despegueVerticalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueVerticalP3(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P3);
        Point puntoParaleloAPuerta = new Point((int) P3.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Añade los puntos globales que todos los despegues verticales desde la puerta de embarque siguen
        despegueVerticalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueVerticalP4(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P4);
        Point puntoParaleloAPuerta = new Point((int) P4.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Añade los puntos globales que todos los despegues verticales desde la puerta de embarque siguen
        despegueVerticalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueVerticalP5(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P5);
        Point puntoParaleloAPuerta = new Point((int) P5.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Añade los puntos globales que todos los despegues verticales desde la puerta de embarque siguen
        despegueVerticalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueVerticalP6(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P6);
        Point puntoParaleloAPuerta = new Point((int) P6.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Añade los puntos globales que todos los despegues verticales desde la puerta de embarque siguen
        despegueVerticalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueVerticalP7(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P7);
        Point puntoParaleloAPuerta = new Point((int) P7.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Añade los puntos globales que todos los despegues verticales desde la puerta de embarque siguen
        despegueVerticalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueVerticalP8(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P8);
        Point puntoParaleloAPuerta = new Point((int) P8.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Añade los puntos globales que todos los despegues verticales desde la puerta de embarque siguen
        despegueVerticalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

        avion.setEnHangar(false);
        avion.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
    }

    private void setDespegueVerticalP9(Avion avion, Vuelo vuelo) {
        ArrayList<Point> ruta = new ArrayList<>();

        //Recorrido inicial particular a cada puerta de embarque
        ruta.add(P9);
        Point puntoParaleloAPuerta = new Point((int) P9.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);

        //Añade los puntos globales que todos los despegues verticales desde la puerta de embarque siguen
        despegueVerticalPuertaGlobal(ruta);

        avion.setRuta(ruta);
        avion.setSpeed(2);

        //Se activa la marcha atras en el recorrido de la puerta de embarque hasta el punto de inicio de aparcamiento
        avion.setMarchaAtras(true);
        avion.setDestinoMarchaAtras(puntoParaleloAPuerta);

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
                iniciarVueloLlegada(v);
                vuelosProcesados.add(v);
            }

        }
    }

    private void iniciarVueloLlegada(Vuelo vuelo)  {
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

        //Puerta de embarque
        PuertaEmbarque puertaEmbarque = vuelo.getPuerta();

        //Se asigna la pista
        if (pistaHorizontal) {
            if (puertaEmbarque == null) {
                setAterrizajeHorizontalHangar(avion, vuelo);
            }
            switch (puertaEmbarque.getNumero()) {
                case 1 -> setAterrizajeHorizontalP1(avion, vuelo);
                case 2 -> setAterrizajeHorizontalP2(avion, vuelo);
                case 3 -> setAterrizajeHorizontalP3(avion, vuelo);
                case 4 -> setAterrizajeHorizontalP4(avion, vuelo);
                case 5 -> setAterrizajeHorizontalP5(avion, vuelo);
                case 6 -> setAterrizajeHorizontalP6(avion, vuelo);
                case 7 -> setAterrizajeHorizontalP7(avion, vuelo);
                case 8 -> setAterrizajeHorizontalP8(avion, vuelo);
                case 9 -> setAterrizajeHorizontalP9(avion, vuelo);
                default -> setAterrizajeHorizontalHangar(avion, vuelo);
            }
        } else {
            if (puertaEmbarque == null) {
                setAterrizajeVerticalHangar(avion, vuelo);
            }
            switch (puertaEmbarque.getNumero()) {
                case 1 -> setAterrizajeVerticalP1(avion, vuelo);
                case 2 -> setAterrizajeVerticalP2(avion, vuelo);
                case 3 -> setAterrizajeVerticalP3(avion, vuelo);
                case 4 -> setAterrizajeVerticalP4(avion, vuelo);
                case 5 -> setAterrizajeVerticalP5(avion, vuelo);
                case 6 -> setAterrizajeVerticalP6(avion, vuelo);
                case 7 -> setAterrizajeVerticalP7(avion, vuelo);
                case 8 -> setAterrizajeVerticalP8(avion, vuelo);
                case 9 -> setAterrizajeVerticalP9(avion, vuelo);
                default -> setAterrizajeVerticalHangar(avion, vuelo);
            }
        }

        //Agrego el avión al mapa
        avionesEnCurso.put(avion.getMatricula(), avion);

        //Actualizo la lista de aviones del MapPanel
        ArrayList<Avion> avionesMapPanel = new ArrayList<>(avionesEnCurso.values());
        mapPanel.setAviones(avionesMapPanel);
    }

    private void setAterrizajeHorizontalHangar(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        avion.setX(1050);
        int alturaPista = (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY();
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Creo un punto inicial fuera de pista para que la velocidad mientras vuela sea la adecuada
        Point inicial = new Point(1000, (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY());
        ruta.add(inicial);

        //Recorre prácticamente toda la pista
        ruta.add(PISTAATERRIZAJEABAJOCENTRODCHA);
        Point finalAterrizaje = new Point((int) (PISTAATERRIZAJEABAJOCENTROIZDA.getX() + 20), (int) PISTAATERRIZAJEABAJOCENTROIZDA.getY());
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

    //Función que establece la ruta global que todos los aterrizajes horizontales a las puertas de embarque siguen
    private void aterrizajeHorizontalPuertaGlobal(ArrayList<Point> ruta) {
        //Creo un punto inicial fuera de pista para que la velocidad mientras vuela sea la adecuada
        Point inicial = new Point(1000, (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY());
        ruta.add(inicial);

        //Recorre prácticamente toda la pista
        ruta.add(PISTAATERRIZAJEABAJOCENTRODCHA);
        Point finalAterrizaje = new Point((int) (PISTAATERRIZAJEABAJOCENTROIZDA.getX() + 20), (int) PISTAATERRIZAJEABAJOCENTROIZDA.getY());
        ruta.add(finalAterrizaje);

        //Vuelve y sube a la terminal
        Point unionPistasAlturaAdecuada = new Point((int) UNIONPISTAS1CENTROSOUTH.getX(), (int) PISTAATERRIZAJEABAJOCENTROIZDA.getY());
        ruta.add(unionPistasAlturaAdecuada);
        ruta.add(UNIONPISTAS1CENTRONORTH);
        ruta.add(ENTRADATERMINAL);
        Point puntoInicioAparcamiento = new Point((int) ENTRADATERMINAL.getX(), ALTURAAPARCAR);
        ruta.add(puntoInicioAparcamiento);
    }

    private void setAterrizajeHorizontalP1(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        avion.setX(1050);
        int alturaPista = (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY();
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Establezco el inicio global de la ruta
        aterrizajeHorizontalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P1.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P1);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeHorizontalP2(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        avion.setX(1050);
        int alturaPista = (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY();
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Establezco el inicio global de la ruta
        aterrizajeHorizontalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P2.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P2);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeHorizontalP3(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        avion.setX(1050);
        int alturaPista = (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY();
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Establezco el inicio global de la ruta
        aterrizajeHorizontalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P3.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P3);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeHorizontalP4(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        avion.setX(1050);
        int alturaPista = (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY();
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Establezco el inicio global de la ruta
        aterrizajeHorizontalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P4.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P4);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeHorizontalP5(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        avion.setX(1050);
        int alturaPista = (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY();
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Establezco el inicio global de la ruta
        aterrizajeHorizontalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P5.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P5);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeHorizontalP6(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        avion.setX(1050);
        int alturaPista = (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY();
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Establezco el inicio global de la ruta
        aterrizajeHorizontalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P6.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P6);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeHorizontalP7(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        avion.setX(1050);
        int alturaPista = (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY();
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Establezco el inicio global de la ruta
        aterrizajeHorizontalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P7.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P7);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeHorizontalP8(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        avion.setX(1050);
        int alturaPista = (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY();
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Establezco el inicio global de la ruta
        aterrizajeHorizontalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P8.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P8);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeHorizontalP9(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        avion.setX(1050);
        int alturaPista = (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY();
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Establezco el inicio global de la ruta
        aterrizajeHorizontalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P9.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P9);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeVerticalHangar(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        int anchoPista = (int) PISTAATERRIZAJEDERECHACENTRONORTH.getX();
        avion.setX(anchoPista);
        int alturaPista = (int) 0;
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Creo un punto inicial fuera de pista para que la velocidad mientras vuela sea la adecuada
        Point inicial = new Point((int) PISTAATERRIZAJEDERECHACENTRONORTH.getX(), 0);
        ruta.add(inicial);

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

    //Función que establece la ruta global que todos los aterrizajes verticales a las puertas de embarque siguen
    private void aterrizajeVerticalPuertaGlobal(ArrayList<Point> ruta) {
        //Creo un punto inicial fuera de pista para que la velocidad mientras vuela sea la adecuada
        Point inicial = new Point((int) PISTAATERRIZAJEDERECHACENTRONORTH.getX(), 0);
        ruta.add(inicial);

        //Recorre prácticamente toda la pista
        ruta.add(PISTAATERRIZAJEDERECHACENTRONORTH);
        Point finalAterrizaje = new Point((int) (PISTAATERRIZAJEDERECHACENTRONORTH.getX()), (int) PISTAATERRIZAJEDERECHACENTROSOUTH.getY() - 10);
        ruta.add(finalAterrizaje);
        Point interseccionPistasAterrizaje = new Point((int) PISTAATERRIZAJEDERECHACENTRONORTH.getX(), (int) PISTADESPEGUEARRIBACENTRODCHA.getY());
        ruta.add(interseccionPistasAterrizaje);
        Point unionPistasAlturaAdecuada = new Point((int) UNIONPISTAS1CENTROSOUTH.getX(), (int) PISTADESPEGUEARRIBACENTRODCHA.getY());
        ruta.add(unionPistasAlturaAdecuada);
        ruta.add(UNIONPISTAS1CENTRONORTH);
        ruta.add(ENTRADATERMINAL);
        Point puntoInicioAparcamiento = new Point((int) ENTRADATERMINAL.getX(), ALTURAAPARCAR);
        ruta.add(puntoInicioAparcamiento);
    }

    private void setAterrizajeVerticalP1(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        int anchoPista = (int) PISTAATERRIZAJEDERECHACENTRONORTH.getX();
        avion.setX(anchoPista);
        int alturaPista = (int) 0;
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Llamo a aterrizajeVerticalPuertaGlobal que establece la ruta comun
        aterrizajeVerticalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P1.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P1);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeVerticalP2(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        int anchoPista = (int) PISTAATERRIZAJEDERECHACENTRONORTH.getX();
        avion.setX(anchoPista);
        int alturaPista = (int) 0;
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Llamo a aterrizajeVerticalPuertaGlobal que establece la ruta comun
        aterrizajeVerticalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P2.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P2);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeVerticalP3(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        int anchoPista = (int) PISTAATERRIZAJEDERECHACENTRONORTH.getX();
        avion.setX(anchoPista);
        int alturaPista = (int) 0;
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Llamo a aterrizajeVerticalPuertaGlobal que establece la ruta comun
        aterrizajeVerticalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P3.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P3);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeVerticalP4(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        int anchoPista = (int) PISTAATERRIZAJEDERECHACENTRONORTH.getX();
        avion.setX(anchoPista);
        int alturaPista = (int) 0;
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Llamo a aterrizajeVerticalPuertaGlobal que establece la ruta comun
        aterrizajeVerticalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P4.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P4);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeVerticalP5(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        int anchoPista = (int) PISTAATERRIZAJEDERECHACENTRONORTH.getX();
        avion.setX(anchoPista);
        int alturaPista = (int) 0;
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Llamo a aterrizajeVerticalPuertaGlobal que establece la ruta comun
        aterrizajeVerticalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P5.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P5);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeVerticalP6(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        int anchoPista = (int) PISTAATERRIZAJEDERECHACENTRONORTH.getX();
        avion.setX(anchoPista);
        int alturaPista = (int) 0;
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Llamo a aterrizajeVerticalPuertaGlobal que establece la ruta comun
        aterrizajeVerticalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P6.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P6);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeVerticalP7(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        int anchoPista = (int) PISTAATERRIZAJEDERECHACENTRONORTH.getX();
        avion.setX(anchoPista);
        int alturaPista = (int) 0;
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Llamo a aterrizajeVerticalPuertaGlobal que establece la ruta comun
        aterrizajeVerticalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P7.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P7);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeVerticalP8(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        int anchoPista = (int) PISTAATERRIZAJEDERECHACENTRONORTH.getX();
        avion.setX(anchoPista);
        int alturaPista = (int) 0;
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Llamo a aterrizajeVerticalPuertaGlobal que establece la ruta comun
        aterrizajeVerticalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P8.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P8);

        avion.setRuta(ruta);
        avion.setSpeed(2);
    }

    private void setAterrizajeVerticalP9(Avion avion, Vuelo vuelo) {
        //El avión entra por el este
        int anchoPista = (int) PISTAATERRIZAJEDERECHACENTRONORTH.getX();
        avion.setX(anchoPista);
        int alturaPista = (int) 0;
        avion.setY(alturaPista);

        ArrayList<Point> ruta = new ArrayList<>();

        //Llamo a aterrizajeVerticalPuertaGlobal que establece la ruta comun
        aterrizajeVerticalPuertaGlobal(ruta);

        Point puntoParaleloAPuerta = new Point((int) P9.getX(), ALTURAAPARCAR);
        ruta.add(puntoParaleloAPuerta);
        ruta.add(P9);

        avion.setRuta(ruta);
        avion.setSpeed(2);
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

        iniciarVelocidades(avion);
        avion.actualizarPosicion();

        //Se verifica si ha llegado
        if (avion.enDestino()) {
            boolean haySiguientePunto = avion.siguientePunto();

            if (!haySiguientePunto) {
                avion.setEnHangar(true);
                avion.setSpeed(0);
            } else {
                asignarVelocidadSegmento(avion);
            }
        }
    }


    private void iniciarVelocidades(Avion avion) {
        ArrayList<Point> ruta = avion.getRutaActual();
        int tramos = ruta.size() - 1;
        if (tramos <= 0) {
            avion.setSpeed(1.3);
            return;
        }

        ArrayList<Double> rutaVelociades = avion.getVelocidadesRuta();
        if (rutaVelociades == null) {
            rutaVelociades = new ArrayList<>();
        }
        if (rutaVelociades.size() != tramos) {
            ArrayList<Double> nueva = new ArrayList<>(tramos);
            for (int i = 0; i < tramos; i++) {
                if (i < rutaVelociades.size()) {
                    nueva.add(rutaVelociades.get(i));
                } else {
                    nueva.add(1.3);
                }
            }
            for (int i = 0; i < nueva.size(); i++) {
                avion.setVelocidadSegmento(i, nueva.get(i));
            }
        }

        establecerVelocidadesRutaRecursivo(avion, 0);
        asignarVelocidadSegmento(avion);
    }

    private void establecerVelocidadesRutaRecursivo(Avion avion, int i) {
        ArrayList<Point> ruta = avion.getRutaActual();
        int tramos = ruta.size() - 1;
        if (tramos <= 0) {
            return;
        }

        //Caso base
        if (i >= tramos) {
            return;
        }

        Point a = ruta.get(i);
        Point b = ruta.get(i + 1);
        TipoSegmento tipoSegmento = getTipoSegmento(a, b);
        double velocidad = getVelocidadSegmento(tipoSegmento);
        avion.setVelocidadSegmento(i, velocidad);

        //Caso recursivo
        establecerVelocidadesRutaRecursivo(avion, i + 1);
    }

    private void asignarVelocidadSegmento(Avion avion) {
        int pointIndex = avion.getPointIndex() - 1;
        if (pointIndex < 0) {
            pointIndex = 0;
        }

        ArrayList<Double> rutaVelocidades = avion.getVelocidadesRuta();
        if (rutaVelocidades == null || rutaVelocidades.isEmpty()) {
            avion.setSpeed(1.3);
            return;
        }

        int tramos = rutaVelocidades.size();
        int indice = Math.min(pointIndex, tramos - 1);

        if (rutaVelocidades.get(indice) == null) {
            avion.setSpeed(1.3);
            return;
        }

        avion.setSpeed(rutaVelocidades.get(indice));
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

    //Verfica si está dentro del mapa
    public static boolean estaEnMapa(Point p) {
        boolean devolver = false;

        //Si se encuentra dentro de los limites del hangar se devuelver true
        if (p.x >= 0 && p.x <= 1000 && p.y >= 0 && p.y <= 700) {
            devolver = true;
        }
        return devolver;
    }

    private TipoSegmento getTipoSegmento(Point a, Point b) {
        //Si uno de los dos puntos es fuera del mapa significa que sale o entra al aeropuerto
        if (!estaEnMapa(a) || !estaEnMapa(b) || estaVolando(a) || estaVolando(b)) {
            return TipoSegmento.VUELO;
        }

        //Si ambos puntos están en pista significa que recorre la pista
        if (estaEnPista(a) && estaEnPista(b)) {
            return TipoSegmento.PISTA;
        }

        //Si ambos puntos están en el hangar significa que está en el hangar
        if (estaEnAreaHangarPunto(a) && estaEnAreaHangarPunto(b)) {
            return TipoSegmento.HANGAR;
        }

        return TipoSegmento.TERMINAL;
    };

    //IAG: (ChatGPT)
    private boolean cercaDePistaHorizontal(Point a, Point b) {
        int yPista = (int) PISTAATERRIZAJEABAJOCENTRODCHA.getY();
        double distancia = Math.abs(a.y - yPista) + Math.abs(b.y - yPista);
        return distancia <= 8;
    }

    //IAG: (ChatGPT)
    private boolean cercaDePistaVertical(Point a, Point b) {
        int xPista = (int) PISTAATERRIZAJEDERECHACENTRONORTH.getX();
        double distancia = Math.abs(a.x - xPista) + Math.abs(b.x - xPista);
        return distancia <= 8;
    }

    private double getVelocidadSegmento(TipoSegmento segmento) {
        double devolver;
        switch (segmento) {
            case VUELO -> devolver = 4.0;
            case PISTA -> devolver = 2.5;
            case TERMINAL, HANGAR -> devolver = 0.8;
            default -> devolver = 1.3;
        }
        return devolver;
    }

    private Point getPuntoPuertaEmbarque(PuertaEmbarque puertaEmbarque) {
        Point devolver;
        switch (puertaEmbarque.getNumero()) {
            case 2 -> devolver = P2;
            case 3 -> devolver = P3;
            case 4 -> devolver = P4;
            case 5 -> devolver = P5;
            case 6 -> devolver = P6;
            case 7 -> devolver = P7;
            case 8 -> devolver = P8;
            case 9 -> devolver = P9;
            default -> devolver = P1;
        }
        return devolver;
    }
}