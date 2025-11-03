package main;

import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import domain.Aereopuerto;
import domain.Aerolinea;
import domain.Avion;
import domain.Pista;
import domain.PuertaEmbarque;
import domain.Vuelo;
import gui.JFramePrincipal;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ArrayList<Vuelo> vuelos = new ArrayList<Vuelo>();
		
		Avion av = new Avion("a1");
		PuertaEmbarque pue = new PuertaEmbarque("1");
		Pista pis = new Pista("1");
		ArrayList<String> pasa = new ArrayList<String>();
		ArrayList<String> tripu = new ArrayList<String>();
		Aereopuerto ae = new Aereopuerto("BIO", "Aer Bio", "Bilbo");
		Aereopuerto ae1 = new Aereopuerto("MAD", "Aer Mad", "Madrid");
		Aerolinea al = new Aerolinea("IB", "Iberia");
		
		LocalDateTime ahora = LocalDateTime.now();
		
		for(int i = 0; i<100; i++) {
			Vuelo v1 = new Vuelo(1235, ae, ae1, al, pis, pue, false, ahora.plusHours(i), 1, av, false, pasa, tripu, 0);
			Vuelo v2 = new Vuelo(1643, ae1, ae, al, pis, pue, false, ahora.plusHours(i), 1, av, false, pasa, tripu, 0);
			vuelos.add(v1);
			vuelos.add(v2);
		}
		
	
		
		SwingUtilities.invokeLater(() -> new JFramePrincipal(vuelos));
	}

}
