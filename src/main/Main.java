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
    	VueloGenerador vg = new VueloGenerador();
    	AeropuertoGenerador ag = new AeropuertoGenerador();
    	AvionGenerador av = new AvionGenerador();
    	PuertaGenerador pe = new PuertaGenerador();
    	ArrayList<Aerolinea> aers = generadorAerolinea();
        generarVuelosAleatorios(50, ag, aers, av, pe, vg);
        Set<Aeropuerto> aeroEjemplo = ag.devolverA();
        Set<Avion> avEjemplo = av.devolverA();
        List<Avion> avionesPrueba = crearAvionesPrueba();
        Set<PuertaEmbarque> puertasEjemplo = pe.devolverP();
        

        // Lanzar interfaz con los vuelos
        SwingUtilities.invokeLater(() -> new JFramePrincipal(vg, new ArrayList<Aeropuerto>(aeroEjemplo), avionesPrueba,
        													 aers, new ArrayList<Avion>(avEjemplo), new ArrayList<PuertaEmbarque>(puertasEjemplo)));
    }

    private static void generarVuelosAleatorios(int cantidad, AeropuertoGenerador ag, ArrayList<Aerolinea> aer,
    														AvionGenerador av, PuertaGenerador pe, VueloGenerador vg) {
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
        	ag.añadirA(aeropuerto);
        }
        
        ArrayList<Aeropuerto> aeropuertos = new ArrayList<Aeropuerto>(ag.devolverA());
        
        Aeropuerto bilbao = new Aeropuerto("LEBB", "Bilbao Airport", "Bilbao");
        
        for (int i=1; i<10; i++) {
        	pe.añadirP(new PuertaEmbarque(false));
        }
        
        ArrayList<PuertaEmbarque> puertas = new ArrayList<PuertaEmbarque>(pe.devolverP());
        

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
            while (Avion.existeMatricula(matricula)) {
            		matricula = "EC-" + (char)('A' + random.nextInt(26)) +
                    (char)('A' + random.nextInt(26)) +
                    (char)('A' + random.nextInt(26));
            }
            int capacidad = 150 + random.nextInt(200);
            Avion avion = new Avion(modelo, matricula, capacidad);
            av.añadirA(avion);
            
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

            LocalDateTime ahora = LocalDateTime.now();

            if (makeArrivalToBIO) {
                // Vuelo que LLEGA a BIO: origen = otra ciudad, destino = BIO
                
                origen = aeropuertos.get(random.nextInt(aeropuertos.size()));
                destino = bilbao;

                // Pista y puerta: asignadas en Bilbao (destino)
                pista = new Pista("Pista BIO " + (i % 3 + 1), false);
                
                remainingArrivals--;
            } else {
                // Vuelo que SALE desde BIO: origen = BIO, destino = otra ciudad
                origen = bilbao;
                destino = aeropuertos.get(random.nextInt(aeropuertos.size()));

                // Pista y puerta: asignadas en Bilbao (origen)
                pista = new Pista("Pista BIO " + (i % 3 + 1), false);

                remainingDepartures--;
            }

            Vuelo vuelo = new Vuelo( codigo,  origen,  destino,  aer.get(0),  pista,
        			 puertas.get(random.nextInt(puertas.size())),  estado,  ahora.plusHours(i),  duracion,  avion,
        			 emergencia,  pasajeros,  tripulacion,  delayed);
            vg.añadirA(vuelo);
            
        }
    }
    
    public static class VueloGenerador {
    	private Set<Vuelo> vuelos;
    	
    	public VueloGenerador() {
    		this.vuelos = new HashSet<Vuelo>();
    	}
    	public void añadirA(Vuelo v) {
    		this.vuelos.add(v);
    	}
    	public Set<Vuelo> devolverA(){
    		return this.vuelos;
    	} 
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
    
    public static class AvionGenerador {
    	private Set<Avion> aviones;
    	
    	public AvionGenerador() {
    		this.aviones = new HashSet<Avion>();
    	}
    	public void añadirA(Avion a) {
    		this.aviones.add(a);
    	}
    	public Set<Avion> devolverA(){
    		return this.aviones;
    	}
    }
    
    public static class PuertaGenerador {
    	private Set<PuertaEmbarque> puertas;
    	
    	public PuertaGenerador() {
    		this.puertas = new HashSet<PuertaEmbarque>();
    	}
    	public void añadirP(PuertaEmbarque p) {
    		this.puertas.add(p);
    	}
    	public Set<PuertaEmbarque> devolverP(){
    		return this.puertas;
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