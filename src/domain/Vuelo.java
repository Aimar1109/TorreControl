package domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class Vuelo {
	
	
	// ATRIBUTOS
	final private String codigo;
	private Integer numero;
	private Aeropuerto origen;
	private Aeropuerto destino;
	private Aerolinea aereolinea;
	private Pista pista;
	private PuertaEmbarque puerta;
	private boolean estado;
	private LocalDateTime fechaHoraProgramada;
	private float duracion;
	private Avion avion;
	private boolean emergencia;
	private ArrayList<Pasajero> pasajeros;
	private ArrayList<Tripulante> tripulacion;
	private int delayed;
		
	// CONSTRUCTOR
	public Vuelo(Integer numero, Aeropuerto origen, Aeropuerto destino, Aerolinea aereolinea,
			PuertaEmbarque puerta, LocalDateTime fechaHoraProgramada, float duracion, Avion avion) {
		super();
		if (numero.toString().length() != 4) {
			throw new IllegalArgumentException("El numero de vuelo tiene que tener 4 digitos");
		} else {
			this.numero = numero;
		}
		this.codigo = aereolinea.getCodigo() + numero.toString();
		this.origen = origen;
		this.destino = destino;
		this.aereolinea = aereolinea;
		this.pista = null;
		this.puerta = puerta;
		this.estado = false;
		this.fechaHoraProgramada = fechaHoraProgramada;
		this.duracion = duracion;
		this.avion = avion;
		this.emergencia = false;
		this.pasajeros = new ArrayList<Pasajero>();
		this.tripulacion = new ArrayList<Tripulante>();
		this.delayed = 0;
		
	}
	
	public Vuelo(Integer numero, Aeropuerto origen, Aeropuerto destino, Aerolinea aereolinea, Pista pista,
			PuertaEmbarque puerta, Boolean estado, LocalDateTime fechaHoraProgramada, Float duracion, 
			Avion avion, Boolean emergencia, Integer delayed) {
		super();
		if (numero.toString().length() != 4) {
			throw new IllegalArgumentException("El numero de vuelo tiene que tener 4 digitos");
		} else {
			this.numero = numero;
		}
		this.codigo = aereolinea.getCodigo() + numero.toString();
		this.origen = origen;
		this.destino = destino;
		this.aereolinea = aereolinea;
		this.pista = pista;
		this.puerta = puerta;
		this.estado = estado;
		this.fechaHoraProgramada = fechaHoraProgramada;
		this.duracion = duracion;
		this.avion = avion;
		this.emergencia = emergencia;
		this.pasajeros = new ArrayList<Pasajero>();
		this.tripulacion = new ArrayList<Tripulante>();
		this.delayed = delayed;
		
	}
	
	public Vuelo(Integer numero, Aeropuerto origen, Aeropuerto destino, Aerolinea aereolinea, Pista pista,
			PuertaEmbarque puerta, boolean estado, LocalDateTime fechaHoraProgramada, float duracion, Avion avion,
			boolean emergencia, ArrayList<Pasajero> pasajeros, ArrayList<Tripulante> tripulacion, int delayed) {
		super();
		if (numero.toString().length() != 4) {
			throw new IllegalArgumentException("El numero de vuelo tiene que tener 4 digitos");
		} else {
			this.numero = numero;
		}
		this.codigo = aereolinea.getCodigo() + numero.toString();
		this.origen = origen;
		this.destino = destino;
		this.aereolinea = aereolinea;
		this.pista = pista;
		this.puerta = puerta;
		this.estado = estado;
		this.fechaHoraProgramada = fechaHoraProgramada;
		this.duracion = duracion;
		this.avion = avion;
		this.emergencia = emergencia;
		this.pasajeros = pasajeros;
		this.tripulacion = tripulacion;
		this.delayed = delayed;
		
	}
	
	//GETTERS Y SETTERS
	public String getCodigo() {
		return codigo;
	}
	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		if (numero.toString().length() != 4) {
			throw new IllegalArgumentException("El numero de vuelo tiene que tener 4 digitos");
		} else {
			this.numero = numero;
		}
	}

	public Aeropuerto getOrigen() {
		return origen;
	}

	public void setOrigen(Aeropuerto origen) {
		this.origen = origen;
	}

	public Aeropuerto getDestino() {
		return destino;
	}

	public void setDestino(Aeropuerto destino) {
		this.destino = destino;
	}

	public Aerolinea getAereolinea() {
		return aereolinea;
	}

	public void setAereolinea(Aerolinea aereolinea) {
		this.aereolinea = aereolinea;
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

	public LocalDateTime getFechaHoraProgramada() {
		return fechaHoraProgramada;
	}

	public void setFechaHoraProgramada(LocalDateTime fechaHoraProgramada) {
		this.fechaHoraProgramada = fechaHoraProgramada;
	}

	public float getDuracion() {
		return duracion;
	}

	public void setDuracion(float duracion) {
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

	public ArrayList<Pasajero> getPasajeros() {
		return pasajeros;
	}

	public void setPasajeros(ArrayList<Pasajero> pasajeros) {
		this.pasajeros = pasajeros;
	}
	
	public void addPasajero(Pasajero pasajero) {
		if (this.avion.getCapacidad()>this.pasajeros.size()) {
			this.pasajeros.add(pasajero);
		} else {
			throw new IllegalArgumentException("El avion esta lleno");
		}
	}

	public ArrayList<Tripulante> getTripulacion() {
		return tripulacion;
	}

	public void setTripulacion(ArrayList<Tripulante> tripulacion) {
		this.tripulacion = tripulacion;
	}
	
	public void addTripulante(Tripulante tripulante) {
		this.tripulacion.add(tripulante);
	}

	public int getDelayed() {
		return delayed;
	}

	public void setDelayed(int delayed) {
		this.delayed = delayed;
	}
	
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vuelo that = (Vuelo) o;
        return codigo.equals(that.codigo); // La igualdad se basa solo en el código
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }
    
    @Override
    public String toString() {
        return codigo;
    }
}
