package main;

import javax.swing.SwingUtilities;

import gui.JFramePrincipal;
import domain.*;
import java.util.ArrayList;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        // Generar vuelos de ejemplo
        ArrayList<Vuelo> vuelosEjemplo = generarVuelosAleatorios(16);

        // Lanzar interfaz con los vuelos
        SwingUtilities.invokeLater(() -> new JFramePrincipal(vuelosEjemplo));
    }

    private static ArrayList<Vuelo> generarVuelosAleatorios(int cantidad) {
        ArrayList<Vuelo> vuelos = new ArrayList<>();
        Random random = new Random();

        // Ciudades disponibles (incluye BIO en la lista si quieres, pero lo gestionamos a parte)
        String[] otrasCiudades = {"Madrid", "Barcelona", "Valencia", "Sevilla", "Málaga", "Palma", "Lisboa", "París", "Londres"};
        String[] nombres = {"Juan", "María", "Carlos", "Ana", "Pedro", "Laura", "José", "Carmen", "Miguel", "Isabel"};
        String[] apellidos = {"García", "Rodríguez", "González", "Fernández", "López", "Martínez", "Sánchez", "Pérez", "Ruiz", "Díaz"};
        String[] modelos = {"Boeing 737", "Airbus A320", "Boeing 787", "Airbus A350"};

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
            int duracion = 60 + random.nextInt(240);
            String modelo = modelos[random.nextInt(modelos.length)];
            String matricula = "EC-" + (char)('A' + random.nextInt(26)) +
                                       (char)('A' + random.nextInt(26)) +
                                       (char)('A' + random.nextInt(26));
            int capacidad = 150 + random.nextInt(200);
            Avion avion = new Avion(modelo, matricula, capacidad);
            boolean emergencia = random.nextInt(10) == 0;

            // Pasajeros
            int numPasajeros = 50 + random.nextInt(Math.max(1, Math.min(capacidad, 150)));
            ArrayList<String> pasajeros = new ArrayList<>();
            for (int j = 0; j < numPasajeros; j++) {
                pasajeros.add(nombres[random.nextInt(nombres.length)] + " " +
                               apellidos[random.nextInt(apellidos.length)]);
            }

            // Tripulación
            ArrayList<String> tripulacion = new ArrayList<>();
            int numTripulacion = 4 + random.nextInt(4);
            for (int j = 0; j < numTripulacion; j++) {
                tripulacion.add(nombres[random.nextInt(nombres.length)] + " " +
                                apellidos[random.nextInt(apellidos.length)]);
            }

            // Retraso en minutos (70% sin retraso, 30% con retraso)
            int delayed = random.nextInt(10) < 7 ? 0 : random.nextInt(120);

            // Construcción de origen/destino según el tipo
            Aereopuerto origen;
            Aereopuerto destino;
            Pista pista;
            PuertaEmbarque puerta;

            if (makeArrivalToBIO) {
                // Vuelo que LLEGA a BIO: origen = otra ciudad, destino = BIO
                String ciudadOrigen = otrasCiudades[random.nextInt(otrasCiudades.length)];
                origen = new Aereopuerto("AER" + random.nextInt(100), "Aeropuerto " + ciudadOrigen, ciudadOrigen);
                destino = new Aereopuerto("BIO", "Aeropuerto de Bilbao", "Bilbao");

                // Pista y puerta: asignadas en Bilbao (destino)
                pista = new Pista("Pista BIO " + (i % 3 + 1), false);
                puerta = new PuertaEmbarque("Puerta BIO " + (i % 10 + 1), false);

                remainingArrivals--;
            } else {
                // Vuelo que SALE desde BIO: origen = BIO, destino = otra ciudad
                origen = new Aereopuerto("BIO", "Aeropuerto de Bilbao", "Bilbao");
                String ciudadDestino = otrasCiudades[random.nextInt(otrasCiudades.length)];
                destino = new Aereopuerto("AER" + random.nextInt(100), "Aeropuerto " + ciudadDestino, ciudadDestino);

                // Pista y puerta: asignadas en Bilbao (origen)
                pista = new Pista("Pista BIO " + (i % 3 + 1), false);
                puerta = new PuertaEmbarque("Puerta BIO " + (i % 10 + 1), false);

                remainingDepartures--;
            }

            Vuelo vuelo = new Vuelo(codigo, origen, destino, pista, puerta, estado,
                                    duracion, avion, emergencia, pasajeros, tripulacion, delayed);
            vuelos.add(vuelo);
        }

        return vuelos;
    }

}
