package domain;

import java.util.StringJoiner;

public abstract class Clima {
	
	// Atributos
	protected double temperatura;				// En °C
	protected double velocidadViento;			// En km/h
	protected boolean senalPeligro;
	protected double visibilidadKm;				// En km
	
	protected static final double VISIBILIDAD_PELIGROSA = 1.0;
	protected static final double VELOCIDAD_VIENTO_PELIGROSA = 60.0;
	
	// Constructor
	public Clima(double temperatura, double velocidadViento, double visibilidadKm) {
		super();
		this.temperatura = temperatura;
		this.setVelocidadViento(velocidadViento);
		this.senalPeligro = false;
		this.visibilidadKm = visibilidadKm;
	}
	
	public abstract void actualizarSenalPeligro();
	public abstract String getDescripcionParaPanel();

	// Getters y Setters
	public double getTemperatura() {
		return temperatura;
	}

	public void setTemperatura(int temperatura) {
		this.temperatura = temperatura;
	}

	public double getVelocidadViento() {
		return velocidadViento;
	}

	public void setVelocidadViento(double velocidadViento) {
		this.velocidadViento = velocidadViento;
	}

	public boolean isSenalPeligro() {
		return senalPeligro;
	}

	public void setSenalPeligro(boolean senalPeligro) {
		this.senalPeligro = senalPeligro;
	}

	public double getVisibilidadKm() {
		return visibilidadKm;
	}

	public void setVisibilidadKm(double visibilidadKm) {
		this.visibilidadKm = visibilidadKm;
	}
	
	public static class ClimaDespejado extends Clima {
		
		private IntensidadSol intensidad;
		
		public ClimaDespejado(double temperaturaCelsius, double velocidadVientoKmh, double visibilidadKm, IntensidadSol intensidad) {
	        super(temperaturaCelsius, velocidadVientoKmh, visibilidadKm); 
	        this.intensidad = intensidad;
	    }
		
		public void actualizarSenalPeligro() {
	        // Peligro si el viento es muy fuerte o la visibilidad es inesperadamente baja.
	        if (this.velocidadViento > VELOCIDAD_VIENTO_PELIGROSA || this.visibilidadKm < VISIBILIDAD_PELIGROSA) {
	            this.senalPeligro = true;
	        } else {
	            this.senalPeligro = false;
	        }
		}
		
		public String getDescripcionParaPanel() {
	        StringJoiner sj = new StringJoiner("\n");
	        sj.add("--- CLIMA DESPEJADO ---");
	        sj.add("Temperatura: " + this.temperatura + " °C");
	        sj.add("Viento: " + this.velocidadViento + " km/h");
	        sj.add("Visibilidad: " + this.visibilidadKm + " km");
	        sj.add("Intensidad Sol: " + this.intensidad);
	        return sj.toString();
	    }
		
		public IntensidadSol getIntensidad() {
			return intensidad;
		}
		
	}
	
	public static class ClimaLluvioso extends Clima {
		 
		private boolean tormentaElectrica;
	    private double cantidadPrecipitacion;		// En milímetros == Litros por metro cuadrado
	    
	    public ClimaLluvioso(double temperaturaCelsius, double velocidadVientoKmh, double visibilidadKm, boolean tormentaElectrica, double cantidadPrecipitacion) {
	        super(temperaturaCelsius, velocidadVientoKmh, visibilidadKm);
	        this.tormentaElectrica = tormentaElectrica;
	    }
	    
	    public void actualizarSenalPeligro() {
	        // Peligro si hay tormenta, viento fuerte o baja visibilidad por lluvia intensa
	        if (this.tormentaElectrica || this.velocidadViento > VELOCIDAD_VIENTO_PELIGROSA || this.visibilidadKm < VISIBILIDAD_PELIGROSA) {
	            this.senalPeligro = true;
	        } else {
	            this.senalPeligro = false;
	        }
	    }
	    
	    public String getDescripcionParaPanel() {
	        StringJoiner sj = new StringJoiner("\n");
	        sj.add("--- CLIMA LLUVIOSO ---");
	        sj.add("Temperatura: " + this.temperatura + " °C");
	        sj.add("Viento: " + this.velocidadViento + " km/h");
	        sj.add("Visibilidad: " + this.visibilidadKm + " km");
	        sj.add("Precipitación (L/m²): " + this.cantidadPrecipitacion + " mm");
	        sj.add("Tormenta Eléctrica: " + (this.tormentaElectrica ? "SÍ" : "NO"));
	        return sj.toString();
	    }
	    
	    public boolean isTormentaElectrica() {
	        return tormentaElectrica;
	    }

		public double getCantidadPrecipitacion() {
			return cantidadPrecipitacion;
		}
	}
	
	public static class ClimaNublado extends Clima {
		
		private int techoNubesAltura;
		private static final int TECHO_NUBES_ALTURA_PELIGROSA = 300;
		
		public ClimaNublado(double temperaturaCelsius, double velocidadVientoKmh, double visibilidadKm, int techoNubesAltura) {
	        super(temperaturaCelsius, velocidadVientoKmh, visibilidadKm);
	        this.techoNubesAltura = techoNubesAltura;
	    }

		@Override
		public void actualizarSenalPeligro() {
	        // Peligro si las nubes están muy bajas, la visibilidad es mala (niebla) O el viento es fuerte
	        if (this.techoNubesAltura < TECHO_NUBES_ALTURA_PELIGROSA || this.visibilidadKm < VISIBILIDAD_PELIGROSA || this.velocidadViento > VELOCIDAD_VIENTO_PELIGROSA) {
	            this.senalPeligro = true;
	        } else {
	            this.senalPeligro = false;
	        }
	    }
		
		public String getDescripcionParaPanel() {
	        StringJoiner sj = new StringJoiner("\n");
	        sj.add("--- CLIMA NUBLADO ---");
	        sj.add("Temperatura: " + this.temperatura + " °C");
	        sj.add("Viento: " + this.velocidadViento + " km/h");
	        sj.add("Visibilidad: " + this.visibilidadKm + " km");
	        sj.add("Techo de Nubes: " + this.techoNubesAltura + " m");
	        return sj.toString();
	    }
		
		public int getTechoNubesMetros() {
	        return techoNubesAltura;
	    }
	}
	
	public static class ClimaNevado extends Clima {
		
		private double acumulacionNieve;		// En cm
		
		public ClimaNevado(double temperaturaCelsius, double velocidadVientoKmh, double visibilidadKm, double acumulacionNieve) {
	        super(temperaturaCelsius, velocidadVientoKmh, visibilidadKm);
	        this.acumulacionNieve = acumulacionNieve;
	    }
		
		public void actualizarSenalPeligro() {
	        // Peligro si hay acumulación, la visibilidad es baja (ventisca) O viento fuerte
	        if (this.acumulacionNieve > 0.5 || this.visibilidadKm < VISIBILIDAD_PELIGROSA || this.velocidadViento > VELOCIDAD_VIENTO_PELIGROSA
	        		) {
	            this.senalPeligro = true;
	        } else {
	            this.senalPeligro = false;
	        }
	    }
		
		public String getDescripcionParaPanel() {
	        StringJoiner sj = new StringJoiner("\n");
	        sj.add("--- CLIMA NEVADO ---");
	        sj.add("Temperatura: " + this.temperatura + " °C");
	        sj.add("Viento: " + this.velocidadViento + " km/h");
	        sj.add("Visibilidad: " + this.visibilidadKm + " km");
	        sj.add("Acumulación Nieve: " + this.acumulacionNieve + " cm");
	        return sj.toString();
	    }
	}
}
