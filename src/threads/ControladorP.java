package threads;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import domain.Pista;
import domain.PuertaEmbarque;
import domain.Vuelo;
import jdbc.GestorBD;

import threads.RelojGlobal;

public class ControladorP {
	
	private Boolean[] puertas;
	private Boolean[] pistas;
	private GestorBD gestorBD;
	
	
	
	public ControladorP(GestorBD gestorBD) {
		this.puertas = new Boolean[9];
		this.pistas = new Boolean[2];
		this.gestorBD = gestorBD;
		
	}
	
	public void updatePuerta(PuertaEmbarque p) {
		
		this.puertas[p.getNumero()-1] = p.isOcupada();
		
	}
	
	
	public void updatePista(Pista p) {
		
		this.pistas[Integer.parseInt(p.getNumero())-1] = p.isDisponible();
	
	}
	
	public void asignarPuertas() {
		
		LocalDateTime hora = RelojGlobal.getInstancia().getTiempoActual();
		
		List<Vuelo> vuelos = gestorBD.getVueloHoraNumero(hora, 9);
		
		List<PuertaEmbarque> puertasBD = gestorBD.loadPuertasEmbarque();
		
		for (Vuelo v: vuelos) {
			
			if ( v.getPuerta() == null) {
				for (int i=0; i<puertas.length; i++) {
					if (!puertas[i]) {
						v.setPuerta(puertasBD.get(i));
						puertasBD.get(i).setOcupada(true);
						updatePuerta(puertasBD.get(i));
					}
				}
			}

		}
		
	}
}
