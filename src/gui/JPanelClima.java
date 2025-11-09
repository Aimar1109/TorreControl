package gui;

import domain.Clima;
import domain.IntensidadSol;
import domain.Clima.ClimaDespejado;
import domain.Clima.ClimaLluvioso;
import domain.Clima.ClimaNevado;
import domain.Clima.ClimaNublado;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.Timer;
import java.util.LinkedList;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

public class JPanelClima extends JPanel {

	private static final long serialVersionUID = 1L;
	
	// --- Componentes Gráficos ---
    private JLabel lblReloj;
    private JButton btnPausarReanudar;
    private JButton btnReiniciar;
    private JButton btnAvanzarHoraRapido;
    private GraficoTemperatura graficoTemperatura;
    private GraficoPrecipitacion graficoPrecipitacion;
    
    // --- Componentes del Panel Izquierdo (Formato Tabla) ---
    private JPanel panelHoraActual; 
    private JLabel lblTituloClima;
    private JLabel valTemp, valViento, valLluvia, valNieve, valNiebla, valNubes;
    
    // --- Lógica de Simulación ---
    private int horaActual;
    private Random generadorAleatorio;
    private int segundosTranscurridosEnLaHora;
    private Clima climaHoraActual;	// Almacena T (Hora Actual)
    private LinkedList<Clima> pronosticoFuturo; // Almacena T+1, T+2, T+3, T+4, T+5
    
	
	// Lógica del Reloj Interno
	private Timer relojInterno;
	private static final int SEGUNDOS_REALES_POR_HORA_SIMULADA = 60;	// 1 hora del programa = 60 segundos reales
	private static final int TICK_DEL_RELOJ_MS = 1000; // 1000ms = 1 segundo
	
	// Color de fondo (como el amarillo de la referencia) ---
    private final Color PANEL_BACKGROUND_COLOR = new Color(210,210,210);
	
	public JPanelClima() {
		this.horaActual = 0;
		this.generadorAleatorio = new Random();
		this.segundosTranscurridosEnLaHora = 0;
		
		this.pronosticoFuturo = new LinkedList<>();
		// Generamos el clima actual (Hora 0)
        this.climaHoraActual = generarClimaAleatorio(0);
        // Generamos los 5 pronósticos futuros (Hora 1 a 5)
        for (int i = 1; i <= 5; i++) {
            this.pronosticoFuturo.add(generarClimaAleatorio(i));
        }
		
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Crear el panel de controles (Norte)
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblReloj = new JLabel("00:00");
        lblReloj.setFont(new Font("Monospaced", Font.BOLD, 16));
        btnPausarReanudar = new JButton("Pausar");
        btnReiniciar = new JButton("Reiniciar Simulación");
        btnAvanzarHoraRapido = new JButton("Avanzar 1 Hora");
        panelControles.add(lblReloj);
        panelControles.add(btnPausarReanudar);
        panelControles.add(btnReiniciar);
        panelControles.add(btnAvanzarHoraRapido);
        
        add(panelControles, BorderLayout.NORTH);
        
        // Panel Izquierdo (Tabla "Clima Actual")
        
        // El panel "wrapper" principal
        panelHoraActual = new JPanel(new BorderLayout(0, 5)); // 0px h-gap, 5px v-gap
        panelHoraActual.setBackground(PANEL_BACKGROUND_COLOR); // Fondo gris claro
        // (Quitamos el TitledBorder)

        // El Título (Centrado, más pequeño)
        lblTituloClima = new JLabel(String.format("Clima Actual (Hora %02d:00)", horaActual));
        lblTituloClima.setFont(new Font("Arial", Font.BOLD, 12)); // Fuente más pequeña
        lblTituloClima.setHorizontalAlignment(SwingConstants.CENTER);
        lblTituloClima.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0)); // Padding superior
        
        // La tabla de datos interna
        JPanel tablaDatos = new JPanel(new GridLayout(7, 2, 2, 2)); // 7 filas, 2 col, 2px GAPS
        tablaDatos.setBackground(PANEL_BACKGROUND_COLOR); // El gap será gris claro
        tablaDatos.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding exterior

        // Definimos las fuentes y alineaciones
        Font headerFont = new Font("Arial", Font.BOLD, 12);
        Font typeFont = new Font("Arial", Font.PLAIN, 12);
        Font valueFont = new Font("Monospaced", Font.PLAIN, 12);
        
        // Fila 1: Cabeceras (Centradas)
        tablaDatos.add(crearCeldaTabla("Propiedad", SwingConstants.CENTER, headerFont));
        tablaDatos.add(crearCeldaTabla("Valor", SwingConstants.CENTER, headerFont));

        // Fila 2: Temperatura (Izquierda, Derecha)
        tablaDatos.add(crearCeldaTabla("Temperatura [°C]", SwingConstants.LEFT, typeFont));
        valTemp = crearCeldaTabla("---", SwingConstants.RIGHT, valueFont);
        tablaDatos.add(valTemp);

        // Fila 3: Viento (Izquierda, Derecha)
        tablaDatos.add(crearCeldaTabla("Viento [km/h]", SwingConstants.LEFT, typeFont));
        valViento = crearCeldaTabla("---", SwingConstants.RIGHT, valueFont);
        tablaDatos.add(valViento);

        // Fila 4: Lluvia (Izquierda, Derecha)
        tablaDatos.add(crearCeldaTabla("Lluvia [mm/h]", SwingConstants.LEFT, typeFont));
        valLluvia = crearCeldaTabla("---", SwingConstants.RIGHT, valueFont);
        tablaDatos.add(valLluvia);

        // Fila 5: Nieve (Izquierda, Derecha)
        tablaDatos.add(crearCeldaTabla("Nieve [cm/h]", SwingConstants.LEFT, typeFont));
        valNieve = crearCeldaTabla("---", SwingConstants.RIGHT, valueFont);
        tablaDatos.add(valNieve);

        // Fila 6: Visibilidad (Izquierda, Derecha)
        tablaDatos.add(crearCeldaTabla("Visibilidad [km]", SwingConstants.LEFT, typeFont));
        valNiebla = crearCeldaTabla("---", SwingConstants.RIGHT, valueFont);
        tablaDatos.add(valNiebla);

        // Fila 7: Nubes (Izquierda, Derecha)
        tablaDatos.add(crearCeldaTabla("Nubes [m]", SwingConstants.LEFT, typeFont));
        valNubes = crearCeldaTabla("---", SwingConstants.RIGHT, valueFont);
        tablaDatos.add(valNubes);

        // Montamos el panel izquierdo (Título + Tabla)
        panelHoraActual.add(lblTituloClima, BorderLayout.NORTH);
        panelHoraActual.add(tablaDatos, BorderLayout.CENTER);
        
        // Creamos el panel que contendrá nuestros gráficos. 
        JPanel panelGraficosPronostico = new JPanel(new GridBagLayout());
        panelGraficosPronostico.setBorder(BorderFactory.createTitledBorder("Pronóstico Próximas 6 Horas"));
        graficoTemperatura = new GraficoTemperatura();
        graficoPrecipitacion = new GraficoPrecipitacion();

        GridBagConstraints cTemp = new GridBagConstraints();
        cTemp.gridx = 0; cTemp.gridy = 0; cTemp.weightx = 1.0; cTemp.weighty = 0.6; cTemp.fill = GridBagConstraints.BOTH;
        panelGraficosPronostico.add(graficoTemperatura, cTemp);

        GridBagConstraints cPrecip = new GridBagConstraints();
        cPrecip.gridx = 0; cPrecip.gridy = 1; cPrecip.weightx = 1.0; cPrecip.weighty = 0.4; cPrecip.fill = GridBagConstraints.BOTH;
        panelGraficosPronostico.add(graficoPrecipitacion, cPrecip);
        
        // Panel de Contenido Principal (Izquierda + Derecha)
        JPanel panelWrapperIzquierdo = new JPanel(new BorderLayout());
        panelWrapperIzquierdo.add(panelHoraActual, BorderLayout.NORTH);
        
        JPanel mainContentPanel = new JPanel(new BorderLayout(10, 10));
        mainContentPanel.add(panelWrapperIzquierdo, BorderLayout.WEST);
        mainContentPanel.add(panelGraficosPronostico, BorderLayout.CENTER); // ¡Añadimos el panel de gráficos aquí!
        add(mainContentPanel, BorderLayout.CENTER);
        
        graficoTemperatura.setHoverListener(new GraficoTemperatura.OnHoverListener() {
            @Override
            public void onPuntoHover(Clima climaHovered, int indiceOffset) {
                int horaHovered = (horaActual + indiceOffset) % 24;
                actualizarPanelHoraActual(climaHovered, horaHovered);
            }
            @Override
            public void onPuntoExit() {
                actualizarPanelHoraActual();
            }
        });
        
        
        
        
     
        // Añadir Listeners a los botones
        btnPausarReanudar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleReloj();
            }
        });

        btnReiniciar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reiniciarSimulacion();
            }
        });
        
        btnAvanzarHoraRapido.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                avanzarHoraRapido();
            }
        });

        // 7. Cargar los datos iniciales
        actualizarPanelHoraActual();
        actualizarGraficosPronostico();
        
        // 8. Configurar y arrancar el Timer
        relojInterno = new Timer(TICK_DEL_RELOJ_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	// Este código se ejecuta CADA SEGUNDO
                
            	// 1. Avanzamos el contador de segundos
                segundosTranscurridosEnLaHora++;
                // 2. Comprobamos si ha pasado una hora simulada (60 seg)
                if (segundosTranscurridosEnLaHora >= SEGUNDOS_REALES_POR_HORA_SIMULADA) {
                    segundosTranscurridosEnLaHora = 0; // Resetea el contador de segundos
                    avanzarHora(); // Llama al método que actualiza la TABLA
                }
                actualizarLabelReloj();
            }
        });
        relojInterno.start();
	}
	
	// --- MÉTODOS ---
	
	private JLabel crearCeldaTabla(String texto, int alignment, Font font) {
        JLabel label = new JLabel(texto);
        label.setFont(font);
        label.setHorizontalAlignment(alignment);
        
        // Celdas blancas y opacas
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        
        // Borde vacío (padding interno)
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        return label;
    }
	
	private void actualizarPanelHoraActual(Clima climaForzado, int horaForzada) {
        Clima climaAMostrar = (climaForzado != null) ? climaForzado : this.climaHoraActual;
        int horaAMostrar = (climaForzado != null) ? horaForzada : this.horaActual;
        if (climaAMostrar == null) return;

        lblTituloClima.setText(String.format("Clima (Hora %02d:00)", (horaAMostrar % 24)));
        valTemp.setText(String.format("%.1f", climaAMostrar.getTemperatura()));
        valViento.setText(String.format("%.1f", climaAMostrar.getVelocidadViento()));
        valNiebla.setText(String.format("%.1f", climaAMostrar.getVisibilidadKm()));
        valNubes.setText(climaAMostrar.getTechoNubesMetros() >= 10000 ? "N/A" : String.format("%d", climaAMostrar.getTechoNubesMetros()));
        double precipitacion = climaAMostrar.getPrecipitacion();
        if (climaAMostrar instanceof ClimaNevado) {
            valLluvia.setText("0.0");
            valNieve.setText(String.format("%.1f", precipitacion));
        } else if (precipitacion > 0) {
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
	
	/**
     * Pausa o reanuda el reloj interno (Timer).
     */
    private void toggleReloj() {
        if (relojInterno.isRunning()) {
            relojInterno.stop();
            btnPausarReanudar.setText("Reanudar");
        } else {
            relojInterno.start();
            btnPausarReanudar.setText("Pausar");
        }
    }
	
	/**
     * Incrementa la hora y actualiza la interfaz.
     */
    private void avanzarHora() {
        this.horaActual = (this.horaActual + 1) % 24;
        
        // 1. T+1 se convierte en T
        this.climaHoraActual = pronosticoFuturo.poll(); // Saca el T+1 de la cola

        // 2. Generamos un nuevo pronóstico para T+5
        int horaNuevoPronostico = (this.horaActual + 5) % 24; // La nueva 5ª hora
        Clima nuevoPronostico = generarClimaAleatorio(horaNuevoPronostico);
        
        // 3. Añadimos T+5 al final de la cola
        pronosticoFuturo.add(nuevoPronostico);
        
        // 4. Actualizamos UI
        actualizarPanelHoraActual();
        actualizarGraficosPronostico(); // Pasa la nueva lista (T a T+5)
    }
    
    private void avanzarHoraRapido() {
        avanzarHora();
        segundosTranscurridosEnLaHora = 0;
        actualizarLabelReloj();
        relojInterno.restart();
        btnPausarReanudar.setText("Pausar");
    }
    
    /**
     * Reinicia la hora a 0 y actualiza la interfaz.
     */
    private void reiniciarSimulacion() {
        this.horaActual = 0;
        this.segundosTranscurridosEnLaHora = 0;
        
        // Vuelve a llenar T y T+1 a T+5
        pronosticoFuturo.clear();
        this.climaHoraActual = generarClimaAleatorio(0);
        for (int i = 1; i <= 5; i++) { // Bucle de 1 a 5
            pronosticoFuturo.add(generarClimaAleatorio(i));
        }
        
        // Actualiza UI
        actualizarLabelReloj();
        actualizarPanelHoraActual();
        actualizarGraficosPronostico(); // Pasa la nueva lista (T a T+5)
        
        relojInterno.restart();
        btnPausarReanudar.setText("Pausar");
    }
    
    /**
     * Formatea y actualiza el JLabel del reloj.
     */
    private void actualizarLabelReloj() {
        if (horaActual >= 24) {
            horaActual = 0;
        }
        
        String horaStr = String.format("%02d", horaActual);
        String segStr = String.format("%02d", segundosTranscurridosEnLaHora);
        
        lblReloj.setText(horaStr + ":" + segStr);
    }
    
    private void actualizarGraficosPronostico() {
        // 1. Crear la lista temporal (T a T+5)
        LinkedList<Clima> datosParaGrafico = new LinkedList<>(pronosticoFuturo);
        datosParaGrafico.addFirst(climaHoraActual);
            
        if (graficoTemperatura != null) {
            graficoTemperatura.setDatos(datosParaGrafico, horaActual);
        }
        
        // 2. ¡NUEVO! Pasar los mismos datos al gráfico de precipitación
        if (graficoPrecipitacion != null) {
            graficoPrecipitacion.setDatos(datosParaGrafico, horaActual);
        }
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