package main;

import javax.swing.SwingUtilities;

import domain.*;
import gui.JFramePrincipal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(() -> {
			// Crear vuelos y aviones
			List<Vuelo> vuelos = crearVuelosPrueba();
			List<Avion> aviones = crearAvionesPrueba();

			// Crear ventana principal pasando vuelos y aviones
			JFramePrincipal ventana = new JFramePrincipal((ArrayList<Vuelo>) vuelos, aviones);
		});


	}

	public static List<Vuelo> crearVuelosPrueba() {
		List<Vuelo> vuelos = new ArrayList<Vuelo>();

		// Crear aeropuertos
		Aereopuerto mad = new Aereopuerto("MAD", "Adolfo Suárez Madrid-Barajas", "Madrid");
		Aereopuerto bcn = new Aereopuerto("BCN", "Barcelona-El Prat", "Barcelona");
		Aereopuerto vlc = new Aereopuerto("VLC", "Valencia", "Valencia");
		Aereopuerto agp = new Aereopuerto("AGP", "Málaga-Costa del Sol", "Málaga");
		Aereopuerto svq = new Aereopuerto("SVQ", "Sevilla", "Sevilla");
		Aereopuerto lhr = new Aereopuerto("LHR", "London Heathrow", "Londres");
		Aereopuerto cdg = new Aereopuerto("CDG", "Paris Charles de Gaulle", "París");
		Aereopuerto jfk = new Aereopuerto("JFK", "John F. Kennedy", "Nueva York");

		// Crear pistas
		Pista pista1 = new Pista();
		Pista pista2 = new Pista();
		Pista pista3 = new Pista();

		// Crear puertas
		PuertaEmbarque puerta12 = new PuertaEmbarque();
		PuertaEmbarque puerta5 = new PuertaEmbarque();
		PuertaEmbarque puerta23 = new PuertaEmbarque();
		PuertaEmbarque puerta8 = new PuertaEmbarque();
		PuertaEmbarque puerta15 = new PuertaEmbarque();

		// Crear aviones
		Avion avion1 = new Avion("Boeing 737", "EC-001", 150);
		Avion avion2 = new Avion("Airbus A320", "EC-002", 180);
		Avion avion3 = new Avion("Boeing 787", "EC-003", 250);
		Avion avion4 = new Avion("Airbus A350", "EC-004", 300);

		// Listas de pasajeros y tripulación
		ArrayList<String> pasajeros150 = new ArrayList<>();
		for (int i = 1; i <= 150; i++) {
			pasajeros150.add("Pasajero" + i);
		}

		ArrayList<String> pasajeros80 = new ArrayList<>();
		for (int i = 1; i <= 80; i++) {
			pasajeros80.add("Pasajero" + i);
		}

		ArrayList<String> tripulacion = new ArrayList<>();
		tripulacion.add("Capitán García");
		tripulacion.add("Copiloto Martínez");
		tripulacion.add("Azafata López");
		tripulacion.add("Azafata Rodríguez");

		// VUELO 1: Madrid -> Barcelona (A tiempo)
		vuelos.add(new Vuelo(1001, mad, bcn, pista1, puerta12, true, 75, avion1, false,
				new ArrayList<>(pasajeros150), new ArrayList<>(tripulacion), 0));

		// VUELO 2: Barcelona -> Valencia (CON EMERGENCIA)
		vuelos.add(new Vuelo(1002, bcn, vlc, pista2, puerta5, true, 45, avion2, true,
				new ArrayList<>(pasajeros80), new ArrayList<>(tripulacion), 0));

		// VUELO 3: Madrid -> Málaga (Retrasado 30 min)
		vuelos.add(new Vuelo(1003, mad, agp, pista1, puerta23, true, 60, avion3, false,
				new ArrayList<>(pasajeros150), new ArrayList<>(tripulacion), 30));

		// VUELO 4: Sevilla -> Madrid (A tiempo)
		vuelos.add(new Vuelo(1004, svq, mad, pista3, puerta8, true, 55, avion4, false,
				new ArrayList<>(pasajeros80), new ArrayList<>(tripulacion), 0));

		// VUELO 5: Madrid -> Londres (Retrasado 15 min)
		vuelos.add(new Vuelo(1005, mad, lhr, pista2, puerta15, true, 150, avion1, false,
				new ArrayList<>(pasajeros150), new ArrayList<>(tripulacion), 15));

		// VUELO 6: París -> Madrid (A tiempo)
		vuelos.add(new Vuelo(1006, cdg, mad, pista1, puerta12, true, 120, avion2, false,
				new ArrayList<>(pasajeros150), new ArrayList<>(tripulacion), 0));

		// VUELO 7: Madrid -> Nueva York (CON EMERGENCIA)
		vuelos.add(new Vuelo(1007, mad, jfk, pista3, puerta23, true, 480, avion3, true,
				new ArrayList<>(pasajeros150), new ArrayList<>(tripulacion), 0));

		// VUELO 8: Barcelona -> Sevilla (Retrasado 45 min)
		vuelos.add(new Vuelo(1008, bcn, svq, pista2, puerta5, true, 90, avion4, false,
				new ArrayList<>(pasajeros80), new ArrayList<>(tripulacion), 45));

		// VUELO 9: Valencia -> Madrid (A tiempo)
		vuelos.add(new Vuelo(1009, vlc, mad, pista1, puerta8, true, 50, avion1, false,
				new ArrayList<>(pasajeros80), new ArrayList<>(tripulacion), 0));

		// VUELO 10: Málaga -> Barcelona (Retrasado 60 min)
		vuelos.add(new Vuelo(1010, agp, bcn, pista3, puerta15, true, 70, avion2, false,
				new ArrayList<>(pasajeros150), new ArrayList<>(tripulacion), 60));

		return vuelos;
	}


	public static List<Avion> crearAvionesPrueba() {
		List<Avion> aviones = new ArrayList<>();
		Random random = new Random();

		// Crear 5 aviones con posiciones aleatorias
		for (int i = 1; i <= 5; i++) {
			// Posiciones aleatorias dentro del rango del mapa (1000x700)
			int x = random.nextInt(900) + 50; // Entre 50 y 950
			int y = random.nextInt(600) + 50; // Entre 50 y 650
			// Ángulo aleatorio
			double angulo = random.nextDouble() * 2 * Math.PI;

			Avion avion = new Avion(
					"Boeing " + (700 + i*10),
					"EC-" + String.format("%03d", i),
					150 + random.nextInt(100),
					x,
					y,
					angulo
			);

			aviones.add(avion);
		}

		return aviones;
	}
}