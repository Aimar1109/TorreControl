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
		gestorBD.updateEstadoPuerta(p);
	}
	
	
	public void updatePista(Pista p) {
		
		this.pistas[Integer.parseInt(p.getNumero())-1] = p.isDisponible();
	
	}
	
	public void asignarPuertas() {
		
		LocalDateTime hora = RelojGlobal.getInstancia().getTiempoActual();
		
		List<List<Vuelo>> tvuelos = gestorBD.getVueloControlP(hora, 9);
		
		// Margen de 1 minuto entre vuelos para evitar colisiones
				
		for(int i=0; i<tvuelos.get(0).size(); i++) {
			Vuelo sv = tvuelos.get(0).get(i);
			System.out.println(sv+" - "+sv.getFechaHoraProgramada() + " - "+sv.getDelayed());
			for(int x=0; x<tvuelos.get(1).size(); x++) {
				Vuelo av = tvuelos.get(1).get(x);
				LocalDateTime avL = av.getFechaHoraProgramada().plusMinutes((long) (av.getDelayed()+av.getDuracion()));
				System.out.println(av.toString()+" - "+avL.toString());
				if ( sv.getFechaHoraProgramada().plusMinutes(sv.getDelayed()).isAfter(avL.minusMinutes(1)) &&
						sv.getFechaHoraProgramada().plusMinutes(sv.getDelayed()).isBefore(avL.plusMinutes(1)) ) {
					if (sv.isEmergencia()) {
						
						av.setDelayed(av.getDelayed()+1);
						gestorBD.updatePistaPuertaVuelo(av);
						System.out.println(av.toString()+"update");
						
					} else if (av.isEmergencia()) {
						sv.setDelayed(sv.getDelayed()+1);
						gestorBD.updatePistaPuertaVuelo(sv);
						System.out.println(sv.toString()+"update");
						
					} else {
						av.setDelayed(av.getDelayed()+1);
						gestorBD.updatePistaPuertaVuelo(av);
						System.out.println(av.toString()+"update");
						
					}
				}
			}
			System.out.println("");
		}
		
		List<PuertaEmbarque> puertasBD = gestorBD.loadPuertasEmbarque();
		
		for (Vuelo v: tvuelos.get(1)) {
			//System.out.println(v);
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
