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
		
		// Listas de siguientes vuelos y vuelos activos por llegar
		List<List<Vuelo>> tvuelos = gestorBD.getVueloControlP(hora, 9);
		
		// Separacion de llegadas y salidas
		ArrayList<Integer> is = new ArrayList<Integer>(); 
		
		for(int i=0; i<tvuelos.get(0).size(); i++) {
			if (tvuelos.get(0).get(i).getDestino().getCodigo().equals("LEBB")) {
				is.add(i);
			}
		}
		for (int i=0; i<is.size(); i++) {		
			tvuelos.get(1).add(tvuelos.get(0).get(is.get(i)));
		}
		
		List<Vuelo> nsa = new ArrayList<Vuelo>();
		for (int i=0; i<tvuelos.get(0).size(); i++) {
			if(!is.contains(i)) {
				nsa.add(tvuelos.get(0).get(i));
			}
		}
		
		tvuelos.set(0, nsa);
		
		// Margen de 1 minuto entre vuelos para evitar colisiones
				
		for(int i=0; i<tvuelos.get(0).size(); i++) {
			Vuelo sv = tvuelos.get(0).get(i);
			
			for(int x=0; x<tvuelos.get(1).size(); x++) {
				Vuelo av = tvuelos.get(1).get(x);
				LocalDateTime avL = av.getFechaHoraProgramada().plusMinutes((long) (av.getDelayed()+av.getDuracion()));

				if ( sv.getFechaHoraProgramada().plusMinutes(sv.getDelayed()).isAfter(avL.minusMinutes(1)) &&
						sv.getFechaHoraProgramada().plusMinutes(sv.getDelayed()).isBefore(avL.plusMinutes(1)) ) {
					if (sv.isEmergencia()) {
						
						av.setDelayed(av.getDelayed()+1);
						gestorBD.updatePistaPuertaVuelo(av);
						//System.out.println(av.toString()+" updated");
						
					} else if (av.isEmergencia()) {
						sv.setDelayed(sv.getDelayed()+1);
						gestorBD.updatePistaPuertaVuelo(sv);
						//System.out.println(sv.toString()+" updated");
						
					} else {
						av.setDelayed(av.getDelayed()+1);
						gestorBD.updatePistaPuertaVuelo(av);
						//System.out.println(av.toString()+" updated");
						
					}
				}
				
				// llegadas
				if (x<tvuelos.get(1).size()-1) {
					Vuelo av1 = tvuelos.get(1).get(x+1);
					LocalDateTime avL1 = av1.getFechaHoraProgramada().plusMinutes((long) (av1.getDelayed()+av.getDuracion()));
					if(avL.isAfter(avL1.minusMinutes(1)) && avL.isBefore(avL1.plusMinutes(1))) {
						// si el primero es emergencia se retrasa el otro en cualquier otro case se retrasa el primero
						if (av.isEmergencia()) {
							av1.setDelayed(av1.getDelayed()+1);
							gestorBD.updatePistaPuertaVuelo(av1);
						} else {
							av.setDelayed(av.getDelayed()+1);
							gestorBD.updatePistaPuertaVuelo(av);
						}
					}
				}
			}
			
			// salidas
			if (i<tvuelos.get(0).size()-1) {
				Vuelo sv1 = tvuelos.get(0).get(i+1);
				
				if (sv.getFechaHoraProgramada().isAfter(sv1.getFechaHoraProgramada().minusMinutes(1)) 
						&& sv.getFechaHoraProgramada().isBefore(sv1.getFechaHoraProgramada().plusMinutes(1))) {
					// lo mismo que con las salidas
					if (sv.isEmergencia()) {
						sv1.setDelayed(sv1.getDelayed()+1);
						gestorBD.updatePistaPuertaVuelo(sv1);
					} else {
						sv.setDelayed(sv.getDelayed()+1);
						gestorBD.updatePistaPuertaVuelo(sv1);
					}
				}
			}
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
