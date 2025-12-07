package gui;

import domain.Clima;
import domain.IntensidadSol;
import domain.Clima.ClimaDespejado;
import domain.Clima.ClimaLluvioso;
import domain.Clima.ClimaNevado;
import domain.Clima.ClimaNublado;
import threads.ObservadorTiempo;
import threads.RelojGlobal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import java.util.LinkedList;
	
public class JPanelClima extends JPanel implements ObservadorTiempo {

	private static final long serialVersionUID = 1L;
	
	// --- Componentes Gráficos ---
    private JLabel lblReloj;

    // --- Componentes del Panel Izquierdo (Formato Tabla) ---
    private JPanel panelHoraActual; 
    private JLabel lblTituloClima;
    private JLabel valTemp, valViento, valLluvia, valNieve, valNiebla, valNubes;
    
    private GraficoTemperatura graficoTemperatura;
    private GraficoPrecipitacion graficoPrecipitacion;
    
    // --- Lógica de Simulación ---
    private int horaActualInt;
    private Random generadorAleatorio;

    private Clima climaHoraActual;	// Almacena T (Hora Actual)
    private LinkedList<Clima> pronosticoFuturo; // Almacena T+1, T+2, T+3, T+4, T+5
    

    private final Color COLOR_ACENTO = new Color(0, 85, 165); 
    // Blanco para el fondo de las áreas de datos (para que resalten sobre el gris de la ventana)
    private final Color COLOR_FONDO_DATOS = Color.WHITE;
	
	public JPanelClima() {
		this.horaActualInt = 0;
		this.generadorAleatorio = new Random();	
		this.pronosticoFuturo = new LinkedList<>();
		
		// Generamos el clima actual (Hora 0)
        this.climaHoraActual = generarClimaAleatorio(0);
        // Generamos los 5 pronósticos futuros (Hora 1 a 5)
        for (int i = 1; i <= 5; i++) {
            this.pronosticoFuturo.add(generarClimaAleatorio(i));
        }
		
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
		JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lblReloj = new JLabel("00:00:00");
		lblReloj.setFont(new Font("Monospaced", Font.BOLD, 24));
		panelControles.add(lblReloj);
		
		add(panelControles, BorderLayout.NORTH);
		
		// El panel "wrapper" principal
        panelHoraActual = new JPanel(new BorderLayout(0, 0)); // 0px h-gap, 5px v-gap
        panelHoraActual.setBackground(COLOR_FONDO_DATOS); // Fondo gris claro
        
        lblTituloClima = new JLabel("Clima Actual");
        lblTituloClima.setFont(new Font("Arial", Font.BOLD, 12)); // Fuente más pequeña
        lblTituloClima.setHorizontalAlignment(SwingConstants.CENTER);
        lblTituloClima.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        // La tabla de datos interna
        JPanel tablaDatos = new JPanel(new GridLayout(7, 2, 0, 0)); // 7 filas, 2 col, 2px GAPS
        tablaDatos.setBackground(Color.LIGHT_GRAY); // El gap será gris claro
        tablaDatos.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Definimos las fuentes y alineaciones
        Font headerFont = new Font("Arial", Font.BOLD, 12);
        Font typeFont = new Font("Arial", Font.PLAIN, 12);
        Font valueFont = new Font("Monospaced", Font.PLAIN, 12);
        
        // Fila 1: Cabeceras (Centradas)
        tablaDatos.add(crearCeldaTabla("Propiedad", SwingConstants.CENTER, headerFont, true));
        tablaDatos.add(crearCeldaTabla("Valor", SwingConstants.CENTER, headerFont, true));

        // Fila 2: Temperatura (Izquierda, Derecha)
        tablaDatos.add(crearCeldaTabla("Temperatura [°C]", SwingConstants.LEFT, typeFont, false));
        valTemp = crearCeldaTabla("---", SwingConstants.RIGHT, valueFont, false);
        tablaDatos.add(valTemp);

        // Fila 3: Viento (Izquierda, Derecha)
        tablaDatos.add(crearCeldaTabla("Viento [km/h]", SwingConstants.LEFT, typeFont, false));
        valViento = crearCeldaTabla("---", SwingConstants.RIGHT, valueFont, false);
        tablaDatos.add(valViento);

        // Fila 4: Lluvia (Izquierda, Derecha)
        tablaDatos.add(crearCeldaTabla("Lluvia [mm/h]", SwingConstants.LEFT, typeFont, false));
        valLluvia = crearCeldaTabla("---", SwingConstants.RIGHT, valueFont, false);
        tablaDatos.add(valLluvia);

        // Fila 5: Nieve (Izquierda, Derecha)
        tablaDatos.add(crearCeldaTabla("Nieve [cm/h]", SwingConstants.LEFT, typeFont, false));
        valNieve = crearCeldaTabla("---", SwingConstants.RIGHT, valueFont, false);
        tablaDatos.add(valNieve);

        // Fila 6: Visibilidad (Izquierda, Derecha)
        tablaDatos.add(crearCeldaTabla("Visibilidad [km]", SwingConstants.LEFT, typeFont, false));
        valNiebla = crearCeldaTabla("---", SwingConstants.RIGHT, valueFont, false);
        tablaDatos.add(valNiebla);

        // Fila 7: Nubes (Izquierda, Derecha)
        tablaDatos.add(crearCeldaTabla("Nubes [m]", SwingConstants.LEFT, typeFont, false));
        valNubes = crearCeldaTabla("---", SwingConstants.RIGHT, valueFont, false);
        tablaDatos.add(valNubes);

        // Montamos el panel izquierdo (Título + Tabla)
        panelHoraActual.add(lblTituloClima, BorderLayout.NORTH);
        panelHoraActual.add(tablaDatos, BorderLayout.CENTER);
        
        JPanel panelGraficosPronostico = new JPanel(new GridBagLayout());
        TitledBorder borderGraficos = BorderFactory.createTitledBorder("Pronóstico Próximas 6 Horas");
        borderGraficos.setTitleColor(COLOR_ACENTO);
        borderGraficos.setTitleFont(new Font("Arial", Font.BOLD, 12));
        panelGraficosPronostico.setBorder(borderGraficos);
        
        graficoTemperatura = new GraficoTemperatura();
        graficoPrecipitacion = new GraficoPrecipitacion();
        
        GridBagConstraints cTemp = new GridBagConstraints();
        cTemp.gridx = 0; cTemp.gridy = 0; cTemp.weightx = 1.0; cTemp.weighty = 0.6; cTemp.fill = GridBagConstraints.BOTH;
        panelGraficosPronostico.add(graficoTemperatura, cTemp);
        
        GridBagConstraints cPrecip = new GridBagConstraints();
        cPrecip.gridx = 0; cPrecip.gridy = 1; cPrecip.weightx = 1.0; cPrecip.weighty = 0.4; cPrecip.fill = GridBagConstraints.BOTH;
        panelGraficosPronostico.add(graficoPrecipitacion, cPrecip);
        
        JPanel panelWrapperIzquierdo = new JPanel(new BorderLayout());
        panelWrapperIzquierdo.add(panelHoraActual, BorderLayout.NORTH);
        
        JPanel mainContentPanel = new JPanel(new BorderLayout(10, 10));
        mainContentPanel.add(panelWrapperIzquierdo, BorderLayout.WEST);
        mainContentPanel.add(panelGraficosPronostico, BorderLayout.CENTER); // ¡Añadimos el panel de gráficos aquí!
        add(mainContentPanel, BorderLayout.CENTER);
        
        graficoTemperatura.setHoverListener(new GraficoTemperatura.OnHoverListener() {
            @Override
            public void onPuntoHover(Clima climaHovered, int indiceOffset) {
                int horaHovered = (horaActualInt + indiceOffset) % 24;
                actualizarPanelHoraActual(climaHovered, horaHovered);
            }
            @Override
            public void onPuntoExit() {
                actualizarPanelHoraActual();
            }
        });
        
        actualizarPanelHoraActual();
        actualizarGraficosPronostico();
        
        RelojGlobal.getInstancia().addObservador(this);
	}
	
	public void actualizarTiempo(LocalDateTime nuevoTiempo) {
		
		// 1. Actualizamos el reloj visual
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		lblReloj.setText(nuevoTiempo.format(formatter));
		
		// 2. Comprobamos si ha cambiado la hora
        int horaDelReloj = nuevoTiempo.getHour();
        
        if (horaDelReloj != this.horaActualInt) {
        	
        	// Calculamos cuántas horas han pasado (por si el reloj salta varias horas de golpe)
            // Nota: Esto es una simplificación. Si quieres manejar saltos de día, necesitarás más lógica.
            // Por ahora asumimos avance de 1 en 1 o ajuste simple.
        	
        	this.horaActualInt = horaDelReloj;
        	avanzarHora();
        }
	}
	
	private void avanzarHora() {
		
		// 1. Sacamos el clima actual de la cola y lo ponemos como "actual"
		if (!pronosticoFuturo.isEmpty()) {
			this.climaHoraActual = pronosticoFuturo.poll();
		} else {
			this.climaHoraActual = generarClimaAleatorio(this.horaActualInt);
		}
		
		// 2. Generamos un nuevo pronóstico para el final de la cola (Hora + 5)
		int horaNuevoPronostico = (this.horaActualInt + 5) % 24;
		Clima nuevoPronostico = generarClimaAleatorio(horaNuevoPronostico);
		pronosticoFuturo.add(nuevoPronostico);
		
		// 3. Actualizamos la interfaz
		actualizarPanelHoraActual();
		actualizarGraficosPronostico();
		
		// IMPORTANTE: Aquí repintamos el panel para asegurar que los cambios se ven
		revalidate();
		repaint();
	}
	
	// --- MÉTODOS ---
	
	private JLabel crearCeldaTabla(String texto, int alignment, Font font, boolean isHeader) {
        JLabel label = new JLabel(texto);
        label.setFont(font);
        label.setHorizontalAlignment(alignment);
        label.setOpaque(true);
        
        if (isHeader) {
            // ENCABEZADO: Fondo azul, texto blanco
            label.setBackground(COLOR_ACENTO);
            label.setForeground(Color.WHITE);
        } else {
            // DATO NORMAL: Fondo blanco, texto negro
            label.setBackground(COLOR_FONDO_DATOS);
            label.setForeground(Color.BLACK);
        }
        
        label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        return label;
    }
	
	private void actualizarPanelHoraActual(Clima climaForzado, int horaForzada) {
        Clima climaAMostrar = (climaForzado != null) ? climaForzado : this.climaHoraActual;
        int horaAMostrar = (climaForzado != null) ? horaForzada : this.horaActualInt;

        if (climaAMostrar == null) return;

        lblTituloClima.setText(String.format("Clima (Hora %02d:00)", (horaAMostrar % 24)));
        
        // --- 1. TEMPERATURA (CON ALERTA DE COLOR) ---
        double temp = climaAMostrar.getTemperatura();
        valTemp.setText(String.format("%.1f", temp));
        
        if (temp > 30) {
            valTemp.setForeground(Color.RED);   // Alerta de Calor
        } else if (temp < 0) {
            valTemp.setForeground(Color.BLUE);  // Alerta de Frío
        } else {
            valTemp.setForeground(Color.BLACK); // Normal
        }

        // --- Resto de valores (Sin cambios de color) ---
        valViento.setText(String.format("%.1f", climaAMostrar.getVelocidadViento()));
        // Aseguramos que siempre esté negro por si acaso
        valViento.setForeground(Color.BLACK); 

        valNiebla.setText(String.format("%.1f", climaAMostrar.getVisibilidadKm()));
        valNiebla.setForeground(Color.BLACK);
        
        valNubes.setText(climaAMostrar.getTechoNubesMetros() >= 10000 ? 
            "N/A" : String.format("%d", climaAMostrar.getTechoNubesMetros()));

        double precipitacion = climaAMostrar.getPrecipitacion();
        if (climaAMostrar instanceof ClimaNevado) {
            valLluvia.setText("0.0");
            valNieve.setText(String.format("%.1f", precipitacion));
        } else if (precipitacion > 0) { // Lluvioso
            valLluvia.setText(String.format("%.1f", precipitacion));
            valNieve.setText("0.0");
        } else {
            valLluvia.setText("0.0");
            valNieve.setText("0.0");
        }
    }
	
	private void actualizarPanelHoraActual() {
        actualizarPanelHoraActual(null, -1);
    }

    
    
    private void actualizarGraficosPronostico() {
        // 1. Crear la lista temporal (T a T+5)
        LinkedList<Clima> datos = new LinkedList<>(pronosticoFuturo);
        datos.addFirst(climaHoraActual);
        graficoTemperatura.setDatos(datos, horaActualInt);
        graficoPrecipitacion.setDatos(datos, horaActualInt);
    }
    
    private double round(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
    
    
    // Este método genera un objeto Clima por cada hora
    private Clima generarClimaAleatorio(int hora) {
        int tipoClima = generadorAleatorio.nextInt(4);
        boolean esDeNoche = (hora > 20 || hora < 6);

        // --- 1. Generar valores base ---
        double temp = -3 + (35 * generadorAleatorio.nextDouble());
        double viento = 120 * generadorAleatorio.nextDouble();
        double visi = 20 * generadorAleatorio.nextDouble();
        double humedad = 20 + (80 * generadorAleatorio.nextDouble());
        double presion = 980 + (50 * generadorAleatorio.nextDouble());

        if (esDeNoche) { temp -= 5.0; }

        // --- 2. Aplicar lógica de clima específica ---
        switch (tipoClima) {
            
            case 0: // Despejado
            {
                // probPrecipitacion (0) se hardcodea en el constructor de ClimaDespejado
                IntensidadSol intensidad = IntensidadSol.BAJA;
                if (!esDeNoche) {
                    IntensidadSol[] intensidades = {IntensidadSol.BAJA, IntensidadSol.MEDIA, IntensidadSol.ALTA};
                    intensidad = intensidades[generadorAleatorio.nextInt(intensidades.length)];
                }
                
                return new ClimaDespejado(round(temp), round(viento), round(visi), 
                                        round(humedad), round(presion), intensidad);
            }

            case 1: // Lluvioso
            {
                double precipitacion = 1 + (50 * generadorAleatorio.nextDouble());
                int techoNubes = 100 + generadorAleatorio.nextInt(1000);
                int probPrecipitacion = 70 + generadorAleatorio.nextInt(31); // 70-100%
                boolean tormenta = generadorAleatorio.nextBoolean();
                
                if (precipitacion > 25) visi = visi / 3;
                else if (precipitacion > 10) visi = visi / 2;
                
                return new ClimaLluvioso(round(temp), round(viento), round(visi), 
                                      round(precipitacion), techoNubes, probPrecipitacion, 
                                      round(humedad), round(presion), tormenta);
            }

            case 2: // Nublado
            {
                int techoNubes = 150 + generadorAleatorio.nextInt(2000);
                int probPrecipitacion = 10 + generadorAleatorio.nextInt(31); // 10-40%
                
                if (techoNubes < 300) visi = visi / 2;

                return new ClimaNublado(round(temp), round(viento), round(visi), 
                                    techoNubes, probPrecipitacion, 
                                    round(humedad), round(presion));
            }

            case 3: // Nevado
            default:
            {
                double precipitacion = 1 + (20 * generadorAleatorio.nextDouble());
                int techoNubes = 100 + generadorAleatorio.nextInt(800);
                int probPrecipitacion = 80 + generadorAleatorio.nextInt(21); // 80-100%
                double acumulacion = 1 + (10 * generadorAleatorio.nextDouble());
                
                if (temp > 0) temp = - (temp / 5);
                
                visi = visi / 4; 
                
                return new ClimaNevado(round(temp), round(viento), round(visi), 
                                     round(precipitacion), techoNubes, probPrecipitacion,
                                     round(humedad), round(presion), round(acumulacion));
            }
        }
    }
}