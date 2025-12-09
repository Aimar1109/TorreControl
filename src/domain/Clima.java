package domain;

import java.util.StringJoiner;

public abstract class Clima {
	
	// Atributos
	protected double temperatura;				// En °C
	protected double velocidadViento;			// En km/h
	protected double visibilidadKm;				// En km
	protected double precipitacion;				// En mm/h
	protected int techoNubesMetros;				// Es similar relativamente a cantidad de nubes
	protected double humedad; 					// Porcentaje (0-100)
	protected double presionHPa; 				// Presión atmosférica
	protected int probabilidadPrecipitacion;
	protected double direccionViento;			// En grados (0-360)
	
	protected boolean senalPeligro;
	
	protected static final double VISIBILIDAD_PELIGROSA = 1.0;
	protected static final double VELOCIDAD_VIENTO_PELIGROSA = 80.0;
	protected static final int TECHO_NUBES_PELIGROSO_METROS = 300;
	
	// Constructor
	public Clima(double temperatura, double velocidadViento, double visibilidadKm, double precipitacion, int techoNubesMetros, int probabilidadPrecipitacion, double humedad, double presionHPa) {
		super();
		this.temperatura = temperatura;
		this.velocidadViento = velocidadViento;
		this.visibilidadKm = visibilidadKm;
		this.precipitacion = precipitacion;
		this.techoNubesMetros = techoNubesMetros;
		this.humedad = humedad;
		this.presionHPa = presionHPa;
		this.senalPeligro = false;
		this.probabilidadPrecipitacion = probabilidadPrecipitacion;
		this.direccionViento = 0.0;
		
		this.actualizarSenalPeligro();
	}
	
	public void actualizarSenalPeligro() {
		if (this.velocidadViento > VELOCIDAD_VIENTO_PELIGROSA || 
	            this.visibilidadKm < VISIBILIDAD_PELIGROSA || 
	            this.techoNubesMetros < TECHO_NUBES_PELIGROSO_METROS) {
	            this.senalPeligro = true;
	            } else {
	            this.senalPeligro = false;
	        }
	}
	
	public abstract String getDescripcionParaPanel();

	// Getters
	public double getTemperatura() { return temperatura; }
	public double getVelocidadViento() { return velocidadViento; }
	public boolean isSenalPeligro() { return senalPeligro; }
	public double getVisibilidadKm() { return visibilidadKm; }
	public double getPrecipitacion() { return precipitacion; }
	public int getTechoNubesMetros() { return techoNubesMetros; }
	public double getHumedad() { return humedad; }
	public double getPresionHPa() { return presionHPa; }
	public int getProbabilidadPrecipitacion() { return probabilidadPrecipitacion; }
	public double getDireccionViento() { return direccionViento; }
	public void setDireccionViento(double direccionViento) { this.direccionViento = direccionViento; }
	
	public static class ClimaDespejado extends Clima {
		
		private IntensidadSol intensidad;
		
		public ClimaDespejado(double temperatura, double velocidadViento, double visibilidadKm, 
				double humedad, double presionHPa, IntensidadSol intensidad) {
			super(temperatura, velocidadViento, visibilidadKm, 0.0, 10000, 0, humedad, presionHPa);
			this.intensidad = intensidad;
		}

		@Override
	    public String getDescripcionParaPanel() {
	        StringJoiner sj = new StringJoiner("\n");
	        sj.add("Temperatura: " + temperatura + " °C");
	        sj.add("Viento: " + velocidadViento + " km/h");
	        sj.add("Visibilidad: " + visibilidadKm + " km");
	        sj.add("Techo de Nubes: DESPEJADO");
	        sj.add("Precipitación: 0.0 mm/h");
	        sj.add("Humedad: " + humedad + " %");
	        sj.add("Presión: " + presionHPa + " hPa");
	        sj.add("---");
	        sj.add("Intensidad Sol: " + this.intensidad);
	        sj.add("Prob. Precip: " + this.probabilidadPrecipitacion + " %");
	        return sj.toString();
	    }

		public IntensidadSol getIntensidad() {
			return intensidad;
		}

	}
	
	public static class ClimaLluvioso extends Clima {
		
		private boolean tormentaElectrica;
		
		public ClimaLluvioso(double temperaturaCelsius, double velocidadVientoKmh, double visibilidadKm,
                double precipitacionMmH, int techoNubesMetros, int probabilidadPrecipitacion, 
                double humedadRelativa, double presionHPa, boolean tormentaElectrica) {
				// Pasa todos los datos base a la madre
				super(temperaturaCelsius, velocidadVientoKmh, visibilidadKm, precipitacionMmH, techoNubesMetros, probabilidadPrecipitacion, humedadRelativa, presionHPa);
					this.tormentaElectrica = tormentaElectrica;
		}
		
		@Override
	    public void actualizarSenalPeligro() {
	        super.actualizarSenalPeligro(); // Revisa las condiciones base (viento, visi, techo)
	        if (this.tormentaElectrica) { // Añade la condición de tormenta
	            this.senalPeligro = true;
	        }
	    }
		
		@Override
	    public String getDescripcionParaPanel() {
	        StringJoiner sj = new StringJoiner("\n");
	        sj.add("Temperatura: " + temperatura + " °C");
	        sj.add("Viento: " + velocidadViento + " km/h");
	        sj.add("Visibilidad: " + visibilidadKm + " km");
	        sj.add("Techo de Nubes: " + techoNubesMetros + " m");
	        sj.add("Precipitación: " + precipitacion + " mm/h");
	        sj.add("Humedad: " + humedad + " %");
	        sj.add("Presión: " + presionHPa + " hPa");
	        sj.add("---");
	        sj.add("Tormenta Eléctrica: " + (this.tormentaElectrica ? "SÍ" : "NO")); // Dato específico
	        sj.add("Prob. Precip: " + this.probabilidadPrecipitacion + " %");
	        return sj.toString();
	    }

		public boolean isTormentaElectrica() {
			return tormentaElectrica;
		}
	}
	
	public static class ClimaNublado extends Clima {
		
		public ClimaNublado(double temperaturaCelsius, double velocidadVientoKmh, double visibilidadKm,
                int techoNubesMetros, int probabilidadPrecipitacion, double humedadRelativa, double presionHPa) {
				super(temperaturaCelsius, velocidadVientoKmh, visibilidadKm, 0.0, techoNubesMetros, probabilidadPrecipitacion, humedadRelativa, presionHPa);
		}
		
		@Override
	    public String getDescripcionParaPanel() {
	        StringJoiner sj = new StringJoiner("\n");
	        sj.add("Temperatura: " + temperatura + " °C");
	        sj.add("Viento: " + velocidadViento + " km/h");
	        sj.add("Visibilidad: " + visibilidadKm + " km");
	        sj.add("Techo de Nubes: " + techoNubesMetros + " m");
	        sj.add("Precipitación: 0.0 mm/h");
	        sj.add("Humedad: " + humedad + " %");
	        sj.add("Presión: " + presionHPa + " hPa");
	        sj.add("Prob. Precip: " + this.probabilidadPrecipitacion + " %");
	        // No tiene datos específicos extra
	        return sj.toString();
	    }
	}
	
	public static class ClimaNevado extends Clima {
		
		private double acumulacionNieveCm; 
		
		public ClimaNevado(double temperaturaCelsius, double velocidadVientoKmh, double visibilidadKm,
                double precipitacionMmH, int techoNubesMetros, int probabilidadPrecipitacion, 
                double humedadRelativa, double presionHPa, double acumulacionNieveCm) {
 
				super(temperaturaCelsius, velocidadVientoKmh, visibilidadKm, precipitacionMmH, techoNubesMetros, probabilidadPrecipitacion, humedadRelativa, presionHPa);
				this.acumulacionNieveCm = acumulacionNieveCm;
		}
		
		@Override
	    public void actualizarSenalPeligro() {
	        super.actualizarSenalPeligro();
	        if (this.acumulacionNieveCm > 0.5) { // Nieve en pista
	            this.senalPeligro = true;
	        }
	    }
		
		@Override
	    public String getDescripcionParaPanel() {
	        StringJoiner sj = new StringJoiner("\n");
	        sj.add("Temperatura: " + temperatura + " °C");
	        sj.add("Viento: " + velocidadViento + " km/h");
	        sj.add("Visibilidad: " + visibilidadKm + " km");
	        sj.add("Techo de Nubes: " + techoNubesMetros + " m");
	        sj.add("Precipitación (nieve): " + precipitacion + " cm/h");
	        sj.add("Humedad: " + humedad + " %");
	        sj.add("Presión: " + presionHPa + " hPa");
	        sj.add("---");
	        sj.add("Acumulación en Pista: " + this.acumulacionNieveCm + " cm"); // Dato específico
	        sj.add("Prob. Precip: " + this.probabilidadPrecipitacion + " %");
	        return sj.toString();
	    }

		public double getAcumulacionNieveCm() {
			return acumulacionNieveCm;
		}
	}
}

