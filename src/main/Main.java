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
import domain.*;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        // Generar vuelos de ejemplo
    	AeropuertoGenerador ag = new AeropuertoGenerador();
    	ArrayList<Aerolinea> aers = generadorAerolinea();
        ArrayList<Vuelo> vuelosEjemplo = generarVuelosAleatorios(50, ag, aers);
        Set<Aeropuerto> aeroEjemplo = ag.devolverA();
        List<Avion> avionesPrueba = crearAvionesPrueba();
        

        // Lanzar interfaz con los vuelos
        SwingUtilities.invokeLater(() -> new JFramePrincipal(vuelosEjemplo, new ArrayList<Aeropuerto>(aeroEjemplo), avionesPrueba, aers));
    }

    private static ArrayList<Vuelo> generarVuelosAleatorios(int cantidad, AeropuertoGenerador ag, ArrayList<Aerolinea> aer) {
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
            float duracion = 60 + random.nextInt(240);
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
            Aeropuerto origen;
            Aeropuerto destino;
            Pista pista;
            
            PuertaEmbarque puerta;
            LocalDateTime ahora = LocalDateTime.now();

            if (makeArrivalToBIO) {
                // Vuelo que LLEGA a BIO: origen = otra ciudad, destino = BIO
                String ciudadOrigen = otrasCiudades[random.nextInt(otrasCiudades.length)];
                origen = new Aeropuerto("AER" + random.nextInt(100), "Aeropuerto " + ciudadOrigen, ciudadOrigen);
                destino = new Aeropuerto("BIO", "Aeropuerto de Bilbao", "Bilbao");

                // Pista y puerta: asignadas en Bilbao (destino)
                pista = new Pista("Pista BIO " + (i % 3 + 1), false);
                puerta = new PuertaEmbarque("Puerta BIO " + (i % 10 + 1), false);
                
                ag.añadirA(origen);
                remainingArrivals--;
            } else {
                // Vuelo que SALE desde BIO: origen = BIO, destino = otra ciudad
                origen = new Aeropuerto("BIO", "Aeropuerto de Bilbao", "Bilbao");
                String ciudadDestino = otrasCiudades[random.nextInt(otrasCiudades.length)];
                destino = new Aeropuerto("AER" + random.nextInt(100), "Aeropuerto " + ciudadDestino, ciudadDestino);

                // Pista y puerta: asignadas en Bilbao (origen)
                pista = new Pista("Pista BIO " + (i % 3 + 1), false);
                puerta = new PuertaEmbarque("Puerta BIO " + (i % 10 + 1), false);
                
                ag.añadirA(destino);
                remainingDepartures--;
            }

            Vuelo vuelo = new Vuelo( codigo,  origen,  destino,  aer.get(0),  pista,
        			 puerta,  estado,  ahora.plusHours(i),  duracion,  avion,
        			 emergencia,  pasajeros,  tripulacion,  delayed);
            vuelos.add(vuelo);
        }

        return vuelos;
    }
    
    public static class AeropuertoGenerador {
    	private Set<Aeropuerto> aeropuertos;
    	
    	public AeropuertoGenerador() {
    		this.aeropuertos = new HashSet<Aeropuerto>();
    	}
    	public void añadirA(Aeropuerto a) {
    		this.aeropuertos.add(a);
    	}
    	public Set<Aeropuerto> devolverA(){
    		return this.aeropuertos;
    	}
    }
    
    public static ArrayList<Aerolinea> generadorAerolinea() {
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