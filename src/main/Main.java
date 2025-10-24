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
		SwingUtilities.invokeLater(() -> new JFramePrincipal((ArrayList<Vuelo>) crearVuelosPrueba()));


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

		// Crear pistas (simplificado - puedes mejorar esto)
		Pista pista1 = new Pista();
		Pista pista2 = new Pista();
		Pista pista3 = new Pista();

		// Crear puertas (simplificado - puedes mejorar esto)
		PuertaEmbarque puerta12 = new PuertaEmbarque();
		PuertaEmbarque puerta5 = new PuertaEmbarque();
		PuertaEmbarque puerta23 = new PuertaEmbarque();
		PuertaEmbarque puerta8 = new PuertaEmbarque();
		PuertaEmbarque puerta15 = new PuertaEmbarque();

		// Crear aviones (simplificado)
		Avion avion1 = new Avion();
		Avion avion2 = new Avion();
		Avion avion3 = new Avion();
		Avion avion4 = new Avion();

		// Listas de pasajeros y tripulación (simplificado)
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
		vuelos.add(new Vuelo(
				1001,                    // código
				mad,                     // origen
				bcn,                     // destino
				pista1,                  // pista
				puerta12,                // puerta
				true,                    // estado (activo)
				75,                      // duración en minutos
				avion1,                  // avión
				false,                   // sin emergencia
				new ArrayList<>(pasajeros150), // pasajeros
				new ArrayList<>(tripulacion),  // tripulación
				0                        // sin retraso
		));

		// VUELO 2: Barcelona -> Valencia (CON EMERGENCIA)
		vuelos.add(new Vuelo(
				1002,
				bcn,
				vlc,
				pista2,
				puerta5,
				true,
				45,
				avion2,
				true,                    // ¡EMERGENCIA!
				new ArrayList<>(pasajeros80),
				new ArrayList<>(tripulacion),
				0
		));

		// VUELO 3: Madrid -> Málaga (Retrasado 30 min)
		vuelos.add(new Vuelo(
				1003,
				mad,
				agp,
				pista1,
				puerta23,
				true,
				60,
				avion3,
				false,
				new ArrayList<>(pasajeros150),
				new ArrayList<>(tripulacion),
				30                       // 30 minutos de retraso
		));

		// VUELO 4: Sevilla -> Madrid (A tiempo)
		vuelos.add(new Vuelo(
				1004,
				svq,
				mad,
				pista3,
				puerta8,
				true,
				55,
				avion4,
				false,
				new ArrayList<>(pasajeros80),
				new ArrayList<>(tripulacion),
				0
		));

		// VUELO 5: Madrid -> Londres (Retrasado 15 min)
		vuelos.add(new Vuelo(
				1005,
				mad,
				lhr,
				pista2,
				puerta15,
				true,
				150,
				avion1,
				false,
				new ArrayList<>(pasajeros150),
				new ArrayList<>(tripulacion),
				15
		));

		// VUELO 6: París -> Madrid (A tiempo)
		vuelos.add(new Vuelo(
				1006,
				cdg,
				mad,
				pista1,
				puerta12,
				true,
				120,
				avion2,
				false,
				new ArrayList<>(pasajeros150),
				new ArrayList<>(tripulacion),
				0
		));

		// VUELO 7: Madrid -> Nueva York (CON EMERGENCIA)
		vuelos.add(new Vuelo(
				1007,
				mad,
				jfk,
				pista3,
				puerta23,
				true,
				480,
				avion3,
				true,                    // ¡EMERGENCIA!
				new ArrayList<>(pasajeros150),
				new ArrayList<>(tripulacion),
				0
		));

		// VUELO 8: Barcelona -> Sevilla (Retrasado 45 min)
		vuelos.add(new Vuelo(
				1008,
				bcn,
				svq,
				pista2,
				puerta5,
				true,
				90,
				avion4,
				false,
				new ArrayList<>(pasajeros80),
				new ArrayList<>(tripulacion),
				45
		));

		// VUELO 9: Valencia -> Madrid (A tiempo)
		vuelos.add(new Vuelo(
				1009,
				vlc,
				mad,
				pista1,
				puerta8,
				true,
				50,
				avion1,
				false,
				new ArrayList<>(pasajeros80),
				new ArrayList<>(tripulacion),
				0
		));

		// VUELO 10: Málaga -> Barcelona (Retrasado 60 min)
		vuelos.add(new Vuelo(
				1010,
				agp,
				bcn,
				pista3,
				puerta15,
				true,
				70,
				avion2,
				false,
				new ArrayList<>(pasajeros150),
				new ArrayList<>(tripulacion),
				60
		));

		return vuelos;
	}
}
