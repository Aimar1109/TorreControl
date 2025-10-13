package domain;

import java.util.ArrayList;

public class Vuelo {
	
	// ATRIBUTOS
	private int codigo;
	private Aereopuerto origen;
	private Aereopuerto destino;
	private Pista pista;
	private PuertaEmbarque puerta;
	private boolean estado;
	private int duracion;
	private Avion avion;
	private boolean emergencia;
	private ArrayList<String> pasajeros;
	private ArrayList<String> tripulacion;
	private int delayed;
	
	// CONSTRUCTOR	
	public Vuelo(int codigo, Aereopuerto origen, Aereopuerto destino, Pista pista, PuertaEmbarque puerta, boolean estado,
			int duracion, Avion avion, boolean emergencia, ArrayList<String> pasajeros, ArrayList<String> tripulacion,
			int delayed) {
		super();
		this.codigo = codigo;
		this.origen = origen;
		this.destino = destino;
		this.pista = pista;
		this.puerta = puerta;
		this.estado = estado;
		this.duracion = duracion;
		this.avion = avion;
		this.emergencia = emergencia;
		this.pasajeros = pasajeros;
		this.tripulacion = tripulacion;
		this.delayed = delayed;
	}
	
	//GETTERS Y SETTERS
	public int getcodigo() {
		return codigo;
	}
	public void setcodigo(int codigo) {
		this.codigo = codigo;
	}
	public Aereopuerto getOrigen() {
		return origen;
	}
	public void setOrigen(Aereopuerto origen) {
		this.origen = origen;
	}
	public Aereopuerto getDestino() {
		return destino;
	}
	public void setDestino(Aereopuerto destino) {
		this.destino = destino;
	}
	public Pista getPista() {
		return pista;
	}
	public void setPista(Pista pista) {
		this.pista = pista;
	}
	public PuertaEmbarque getPuerta() {
		return puerta;
	}
	public void setPuerta(PuertaEmbarque puerta) {
		this.puerta = puerta;
	}
	public boolean isEstado() {
		return estado;
	}
	public void setEstado(boolean estado) {
		this.estado = estado;
	}
	public int getDuracion() {
		return duracion;
	}
	public void setDuracion(int duracion) {
		this.duracion = duracion;
	}
	public Avion getAvion() {
		return avion;
	}
	public void setAvion(Avion avion) {
		this.avion = avion;
	}
	public boolean isEmergencia() {
		return emergencia;
	}
	public void setEmergencia(boolean emergencia) {
		this.emergencia = emergencia;
	}
	public ArrayList<String> getPasajeros() {
		return pasajeros;
	}
	public void setPasajeros(ArrayList<String> pasajeros) {
		this.pasajeros = pasajeros;
	}
	public ArrayList<String> getTripulacion() {
		return tripulacion;
	}
	public void setTripulacion(ArrayList<String> tripulacion) {
		this.tripulacion = tripulacion;
	}
	public int getDelayed() {
		return delayed;
	}
	public void setDelayed(int delayed) {
		this.delayed = delayed;
	}
	
}