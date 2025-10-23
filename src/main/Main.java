package main;

import javax.swing.SwingUtilities;

import domain.*;
import gui.JFramePrincipal;

import java.util.ArrayList;
import java.util.Random;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(() -> new JFramePrincipal(generarVuelosAleatorios(20)));


	}

	private static ArrayList<Vuelo> generarVuelosAleatorios(int cantidad) {
		ArrayList<Vuelo> vuelos = new ArrayList<>();
		Random random = new Random();

		String[] ciudades = {"Madrid", "Barcelona", "Bilbao", "Valencia", "Sevilla", "Málaga", "Palma", "Lisboa", "París", "Londres"};
		String[] nombres = {"Juan", "María", "Carlos", "Ana", "Pedro", "Laura", "José", "Carmen", "Miguel", "Isabel"};
		String[] apellidos = {"García", "Rodríguez", "González", "Fernández", "López", "Martínez", "Sánchez", "Pérez", "Ruiz", "Díaz"};
		String[] modelos = {"Boeing 737", "Airbus A320", "Boeing 787", "Airbus A350"};

		for (int i = 0; i < cantidad; i++) {
			int codigo = 1000 + i;

			// Generar aeropuertos de origen y destino diferentes
			String ciudadOrigen = ciudades[random.nextInt(ciudades.length)];
			String ciudadDestino;
			do {
				ciudadDestino = ciudades[random.nextInt(ciudades.length)];
			} while (ciudadDestino.equals(ciudadOrigen));

			Aereopuerto origen = new Aereopuerto(
					"AER" + random.nextInt(100),
					"Aeropuerto " + ciudadOrigen,
					ciudadOrigen
			);

			Aereopuerto destino = new Aereopuerto(
					"AER" + random.nextInt(100),
					"Aeropuerto " + ciudadDestino,
					ciudadDestino
			);

			// Generar pista y puerta (clases vacías, pasamos null o creamos instancias simples)
			Pista pista = new Pista();
			PuertaEmbarque puerta = new PuertaEmbarque();

			// Estado del vuelo
			boolean estado = random.nextBoolean();

			// Duración en minutos
			int duracion = 60 + random.nextInt(240);

			// Avión con datos reales
			String modelo = modelos[random.nextInt(modelos.length)];
			String matricula = "EC-" + (char)('A' + random.nextInt(26)) +
					(char)('A' + random.nextInt(26)) +
					(char)('A' + random.nextInt(26));
			int capacidad = 150 + random.nextInt(200);
			Avion avion = new Avion();

			// Emergencia
			boolean emergencia = random.nextInt(10) == 0;

			// Generar pasajeros
			int numPasajeros = 50 + random.nextInt(150);
			ArrayList<String> pasajeros = new ArrayList<>();
			for (int j = 0; j < numPasajeros; j++) {
				pasajeros.add(nombres[random.nextInt(nombres.length)] + " " +
						apellidos[random.nextInt(apellidos.length)]);
			}

			// Generar tripulación
			ArrayList<String> tripulacion = new ArrayList<>();
			int numTripulacion = 4 + random.nextInt(4);
			for (int j = 0; j < numTripulacion; j++) {
				tripulacion.add(nombres[random.nextInt(nombres.length)] + " " +
						apellidos[random.nextInt(apellidos.length)]);
			}

			// Retraso en minutos (70% sin retraso, 30% con retraso)
			int delayed = random.nextInt(10) < 7 ? 0 : random.nextInt(120);

			Vuelo vuelo = new Vuelo(codigo, origen, destino, pista, puerta, estado,
					duracion, avion, emergencia, pasajeros, tripulacion, delayed);
			vuelos.add(vuelo);
		}

		return vuelos;
	}
}
