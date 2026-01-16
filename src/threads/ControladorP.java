package threads;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import domain.Avion;
import domain.Pista;
import domain.PuertaEmbarque;
import domain.Vuelo;
import jdbc.GestorBD;

public class ControladorP {
	
	private PuertaEmbarque[] puertas;
	private Boolean[] pistas;
	private GestorBD gestorBD;
	
	//private Actualizador act;
	
	
	public ControladorP(GestorBD gestorBD) {
		this.puertas = new PuertaEmbarque[9];
		for (PuertaEmbarque p: gestorBD.loadPuertasEmbarque()) {
			this.puertas[p.getNumero()-1] = p;
		}
		this.pistas = new Boolean[2];
		this.gestorBD = gestorBD;
		
		//this.act = new Actualizador();
		//this.act.start();
		
		
	}
	
	public void updatePuerta(PuertaEmbarque p) {
		
		this.puertas[p.getNumero()-1] = p;
		gestorBD.updatePuerta(p);
	}
	
	
	public void updatePista(Pista p) {
		
		this.pistas[Integer.parseInt(p.getNumero())-1] = p.isDisponible();
	
	}
	
	public void asignarPuertas() {
		
		LocalDateTime hora = RelojGlobal.getInstancia().getTiempoActual();
		
		gestorBD.estadosVuelo();
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
						
					} else if (av.isEmergencia()) {
						sv.setDelayed(sv.getDelayed()+1);
						gestorBD.updatePistaPuertaVuelo(sv);
						
					} else {
						
						
						av.setDelayed(av.getDelayed()+1);
						gestorBD.updatePistaPuertaVuelo(av);
						
					}
				}
				
				// llegadas
				if (x<tvuelos.get(1).size()-1) {
					Vuelo av1 = tvuelos.get(1).get(x+1);
					LocalDateTime avL1 = av1.getFechaHoraProgramada().plusMinutes((long) (av1.getDelayed()+av.getDuracion()));
					if(avL.isAfter(avL1.minusMinutes(1)) && avL.isBefore(avL1.plusMinutes(1))) {
						// prioridad emergencia
						if (av.isEmergencia()) {
							av1.setDelayed(av1.getDelayed()+2);
							gestorBD.updatePistaPuertaVuelo(av1);
						} else if (av1.isEmergencia()) {
							av.setDelayed(av.getDelayed()+2);
							gestorBD.updatePistaPuertaVuelo(av);
						} else {
							// se retrasa el ultimo que llege
							if (avL.isBefore(avL1)) {
								av1.setDelayed(av1.getDelayed()+1);
								gestorBD.updatePistaPuertaVuelo(av1);
							} else {
								av.setDelayed(av.getDelayed()+1);
								gestorBD.updatePistaPuertaVuelo(av);
							}
						}
					}
				}
			}
			
			// salidas
			if (i<tvuelos.get(0).size()-1) {
				Vuelo sv1 = tvuelos.get(0).get(i+1);
				
				if (sv.getFechaHoraProgramada().plusMinutes(sv.getDelayed()).isAfter(sv1.getFechaHoraProgramada().plusMinutes(sv1.getDelayed()).minusMinutes(1)) 
						&& sv.getFechaHoraProgramada().plusMinutes(sv.getDelayed()).isBefore(sv1.getFechaHoraProgramada().plusMinutes(sv1.getDelayed()).plusMinutes(1))) {
					// Como las salidas estan en orden a no ser que el segundo vuelo sea una emergencia siempre se retrasa este
					if (sv1.isEmergencia()) {
						sv.setDelayed(sv.getDelayed()+3);
						gestorBD.updatePistaPuertaVuelo(sv1);
					} else {
						sv1.setDelayed(sv1.getDelayed()+1);
						gestorBD.updatePistaPuertaVuelo(sv1);
					}
				}
			}
		}
		
		// Comprobar puertas
		for(PuertaEmbarque p: this.puertas) {
			if (p.getSalida()!= null) {
				if (p.getSalida().getFechaHoraProgramada().plusMinutes(p.getSalida().getDelayed()).isBefore(hora)) {
					p.setSalida(null);
				}
			}
			if (p.getLlegada()!=null) {
				LocalDateTime sH = p.getLlegada().getFechaHoraProgramada().plusMinutes((long) (p.getLlegada().getDelayed()+p.getLlegada().getDuracion()));
				if (sH.isBefore(hora)) {
					p.setLlegada(null);
				}
			}
		}
		// Asignar llegadas		
		for (Vuelo v: tvuelos.get(1)) {
			// Checkear si el vuelo ya tiene una puerta asignada correctamente
			Boolean yaesta = false;
			for (int i=0; i<puertas.length; i++) {
				if (puertas[i].getLlegada()!=null && puertas[i].getLlegada().equals(v)) {
					yaesta = true;
					break;
				}
			}
			if (yaesta) {
				continue;
			}
			
			if ( v.getPuerta() != null) {
				
				for (int i=0; i<puertas.length; i++) {
					if(puertas[i].getLlegada()==null) {
						// Si la puerta tiene un avion ya que su salida es mas tarde de que este vuelo llege no puede ir a esa puerta
						if (puertas[i].getSalida() != null) {
						    LocalDateTime horaLlegada = v.getFechaHoraProgramada().plusMinutes((long)(v.getDelayed() + v.getDuracion()));
						    LocalDateTime horaSalida = puertas[i].getSalida().getFechaHoraProgramada().plusMinutes(puertas[i].getSalida().getDelayed());
						    
						    // Si el avión que llega no tiene tiempo suficiente antes de la salida programada
						    if (horaLlegada.plusMinutes(2).isAfter(horaSalida)) { // margen de 30 min
						        continue;
						    }
						}
						v.setPuerta(puertas[i]);
						puertas[i].setLlegada(v);
						gestorBD.updatePuerta(puertas[i]);
						gestorBD.updatePuertaVuelo(v);
						break;
					}
				}
			}	
		}
		
		// Asignar salidas intentado enlazar con llegadas
		for(Vuelo v: tvuelos.get(0)) {
			
			Boolean yaesta = false;
			for (int i=0; i<puertas.length; i++) {
				if (puertas[i].getSalida()!=null && puertas[i].getSalida().equals(v)) {
					yaesta = true;
					break;
				}
			}
			if (yaesta) {
				continue;
			}
			
			if (v.getPuerta()!=null) {
				for (int i=0; i<puertas.length; i++) {
					
					if (puertas[i].getSalida()==null && puertas[i].getLlegada()!=null) {
						

						// Actualizar avion para hacer que sea el mismo avion que a aterrizado y la trasicion sea limpia
						if (puertas[i].getLlegada().getAvion()!= null) {
							Avion av = puertas[i].getLlegada().getAvion();
							v.setAvion(av);
							gestorBD.updateAvionVuelo(v);

							av.setMarchaAtras(false);
							av.setDestinoMarchaAtras(null);
							av.setEnHangar(false);
							av.setEstadoAvion(domain.EstadoAvion.RODANDO_A_PISTA);
							av.setRuta(new ArrayList<>());
							av.setSpeed(0);
							av.resetPointIndex();
						}

						v.setPuerta(puertas[i]);
						puertas[i].setSalida(v);
						gestorBD.updatePuerta(puertas[i]);
						gestorBD.updatePuertaVuelo(v);
						break;
					}
					
				}
			}
		}
		
	}
	
	/*
	private class Actualizador extends Thread {
		@Override
		public void run() {
			
			while(!this.isInterrupted()) {
				
				try {
					asignarPuertas();
					Thread.sleep(10_000);
				} catch (InterruptedException e) {
					this.interrupt();
				}
				
			}
		}
	}
	*/
}
