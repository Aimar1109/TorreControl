package main;

import java.time.LocalDateTime;
import java.util.*;

import javax.swing.SwingUtilities;

import domain.Aeropuerto;
import domain.Aerolinea;
import domain.Avion;
import domain.Pista;
import domain.PuertaEmbarque;
import domain.Vuelo;
import gui.JFramePrincipal;
import jdbc.GestorBD;
import jdbc.GestorBDInitializer;
import domain.*;
import threads.RelojGlobal;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        GestorBD gestorBD = new GestorBD();
        GestorBDInitializer gestorBDInitializer = new GestorBDInitializer();

        gestorBDInitializer.crearTablas();

        // Generar vuelos de ejemplo
        ArrayList<Aerolinea> aers = generadorAerolinea(gestorBD);
        generarVuelosAleatorios(50, gestorBD);
        List<Avion> avionesPrueba = crearAvionesPrueba();

        //Configuración RelojGlobal
        RelojGlobal relojGlobal = RelojGlobal.getInstancia();
        relojGlobal.iniciar();


        // Lanzar interfaz con los vuelos
        SwingUtilities.invokeLater(() -> new JFramePrincipal(gestorBD, avionesPrueba));
    }

    private static void generarVuelosAleatorios(int cantidad, GestorBD gestorBD) {
        Random random = new Random();

        // Ciudades disponibles (incluye BIO en la lista si quieres, pero lo gestionamos a parte)
        String[] nombresAeropuertos = {"John F. Kennedy International Airport", "Los Angeles International Airport", "Heathrow Airport", //IAG
                "Charles de Gaulle Airport", "Tokyo Haneda Airport", "Dubai International Airport", "Frankfurt Airport", //IAG
                "Sydney Kingsford Smith Airport", "Toronto Pearson International Airport", "Adolfo Suárez Madrid-Barajas Airport"}; //IAG
        String[] ciudades = {"New York", "Los Angeles", "London", "Paris", "Tokyo", "Dubai", "Frankfurt", "Sydney", "Toronto", "Madrid"}; //IAG
        String[] codigosAeropuertos = {"KJFK", "KLAX", "EGLL", "LFPG", "RJTT", "OMDB", "EDDF", "YSSY", "CYYZ", "LEMD"}; //IAG
        String[] nombres = {"Juan", "María", "Carlos", "Ana", "Pedro", "Laura", "José", "Carmen", "Miguel", "Isabel"};
        String[] apellidos = {"García", "Rodríguez", "González", "Fernández", "López", "Martínez", "Sánchez", "Pérez", "Ruiz", "Díaz"};
        String[] modelos = {"Boeing 737", "Airbus A320", "Boeing 787", "Airbus A350"};

        for(int i=0; i<nombresAeropuertos.length; i++) {
            String codigo = codigosAeropuertos[i];
            String nombre = nombresAeropuertos[i];
            String ciudad = ciudades[i];
            Aeropuerto aeropuerto = new Aeropuerto(codigo, nombre, ciudad);
            gestorBD.insertAeropuerto(aeropuerto);
        }

        ArrayList<Aeropuerto> aeropuertos = (ArrayList<Aeropuerto>) gestorBD.loadAeropuertos();

        Aeropuerto bilbao = new Aeropuerto("LEBB", "Bilbao Airport", "Bilbao");
        gestorBD.insertAeropuerto(bilbao);

        for (int i=1; i<10; i++) {
            PuertaEmbarque npe = new PuertaEmbarque(false);
            gestorBD.insertPuerta(npe);
        }

        ArrayList<PuertaEmbarque> puertas = (ArrayList<PuertaEmbarque>) gestorBD.loadPuertasEmbarque();


        // Queremos aproximadamente la mitad llegadas a BIO y la mitad salidas desde BIO
        int targetArrivalsToBIO = cantidad / 2;
        int targetDeparturesFromBIO = cantidad - targetArrivalsToBIO;
        int remainingArrivals = targetArrivalsToBIO;
        int remainingDepartures = targetDeparturesFromBIO;

        for (int i = 0; i < cantidad; i++) {
            int codigo = 1000 + i;

            boolean makeArrivalToBIO;
            // Decidir de forma aleatoria pero garantizando equilibrio
            if (remainingArrivals == 0) {
                makeArrivalToBIO = false;
            } else if (remainingDepartures == 0) {
                makeArrivalToBIO = true;
            } else {
                // 50/50 pero con sesgo para no agotar una categoría antes
                makeArrivalToBIO = random.nextBoolean();
            }

            // Datos comunes
            boolean estado = random.nextBoolean();
            float duracion = 60 + random.nextInt(240);
            String modelo = modelos[random.nextInt(modelos.length)];
            String matricula = "EC-" + (char)('A' + random.nextInt(26)) +
                    (char)('A' + random.nextInt(26)) +
                    (char)('A' + random.nextInt(26));
            while (gestorBD.getAvionByMatricula(matricula)!=null) {
                matricula = "EC-" + (char)('A' + random.nextInt(26)) +
                        (char)('A' + random.nextInt(26)) +
                        (char)('A' + random.nextInt(26));
            }
            int capacidad = 150 + random.nextInt(200);
            Avion avion = new Avion(modelo, matricula, capacidad);
            gestorBD.insertAvion(avion);

            boolean emergencia = random.nextInt(10) == 0;

            // Pasajeros
            int numPasajeros = 50 + random.nextInt(Math.max(1, Math.min(capacidad, 150)));
            ArrayList<Pasajero> pasajeros = new ArrayList<>();
            for (int j = 0; j < numPasajeros; j++) {
                pasajeros.add(new Pasajero(nombres[random.nextInt(nombres.length)] + " " +
                        apellidos[random.nextInt(apellidos.length)]));
            }

            // Tripulación
            ArrayList<Tripulante> tripulacion = new ArrayList<>();
            int numTripulacion = 4 + random.nextInt(4);
            for (int j = 0; j < numTripulacion; j++) {
                tripulacion.add(new Tripulante(nombres[random.nextInt(nombres.length)] + " " +
                        apellidos[random.nextInt(apellidos.length)]));
            }

            // Retraso en minutos (70% sin retraso, 30% con retraso)
            int delayed = random.nextInt(10) < 7 ? 0 : random.nextInt(120);

            // Construcción de origen/destino según el tipo
            Aeropuerto origen;
            Aeropuerto destino;
            Pista pista;

            LocalDateTime ahora = LocalDateTime.now();

            if (makeArrivalToBIO) {
                // Vuelo que LLEGA a BIO: origen = otra ciudad, destino = BIO

                origen = aeropuertos.get(random.nextInt(aeropuertos.size()));
                destino = bilbao;

                // Pista y puerta: asignadas en Bilbao (destino)
                pista = new Pista("Pista BIO " + (i % 3 + 1), false);
                gestorBD.insertPista(pista);

                remainingArrivals--;
            } else {
                // Vuelo que SALE desde BIO: origen = BIO, destino = otra ciudad
                origen = bilbao;
                destino = aeropuertos.get(random.nextInt(aeropuertos.size()));

                // Pista y puerta: asignadas en Bilbao (origen)
                pista = new Pista("Pista BIO " + (i % 3 + 1), false);
                gestorBD.insertPista(pista);

                remainingDepartures--;
            }

            Vuelo vuelo = new Vuelo( codigo,  origen,  destino,  gestorBD.loadAerolineas().get(0),  pista,
                    puertas.get(random.nextInt(puertas.size())),  estado,  ahora.plusHours(i),  duracion,  avion,
                    emergencia,  pasajeros,  tripulacion,  delayed);
            gestorBD.insertVuelo(vuelo);
        }
    }

    public static ArrayList<Aerolinea> generadorAerolinea(GestorBD gestorBD) {
        String[] nombresAerolineas = { //IAG
                "Iberia",
                "Vueling",
                "Air Europa",
                "Ryanair",
                "Lufthansa",
                "Air France",
                "British Airways",
                "KLM",
                "Emirates",
                "Qatar Airways"
        };

        String[] codigosAerolineas = { //IAG
                "IB",   // Iberia
                "VY",   // Vueling
                "UX",   // Air Europa
                "FR",   // Ryanair
                "LH",   // Lufthansa
                "AF",   // Air France
                "BA",   // British Airways
                "KL",   // KLM
                "EK",   // Emirates
                "QR"    // Qatar Airways
        };

        ArrayList<Aerolinea> aer = new ArrayList<Aerolinea>();

        for(int i=0; i<nombresAerolineas.length; i++) {
            Aerolinea a = new Aerolinea(codigosAerolineas[i], nombresAerolineas[i]);
            aer.add(a);
            gestorBD.insertAerolinea(a);
        }
        return aer;
    }

    public static List<Avion> crearAvionesPrueba() {
        List<Avion> aviones = new ArrayList<>();
        Random random = new Random();

        for (int i = 1; i <= 5; i++) {
            int x = random.nextInt(900) + 50;
            int y = random.nextInt(600) + 50;
            double angulo = random.nextDouble() * 2 * Math.PI;

            Avion avion = new Avion("" + i, "" + i, i, x, y, angulo);

            aviones.add(avion);
        }

        return aviones;
    }
}