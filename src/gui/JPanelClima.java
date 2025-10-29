package gui;

import domain.Clima;
import domain.IntensidadSol;
import domain.Clima.ClimaDespejado;
import domain.Clima.ClimaLluvioso;
import domain.Clima.ClimaNevado;
import domain.Clima.ClimaNublado;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.Timer;
import java.util.LinkedList;

public class JPanelClima extends JPanel {

	private static final long serialVersionUID = 1L;
	
	// --- Componentes Gráficos ---
    private JLabel lblReloj;
    private JButton btnPausarReanudar;
    private JButton btnReiniciar;
    private JPanel panelTablaClima; // Panel central con GridLayout
    private JButton btnAvanzarHoraRapido;
    
    // --- Componentes del Panel Izquierdo (Formato Tabla) ---
    private JPanel panelHoraActual; 
    private JLabel lblTituloClima;
    private JLabel valTemp, valViento, valLluvia, valNieve, valNiebla, valNubes;
    
    // --- Lógica de Simulación ---
    private int horaActual;
    private Random generadorAleatorio;
    private int segundosTranscurridosEnLaHora;
    
    private Clima climaHoraActual;	// Almacena T (Hora Actual)
    private LinkedList<Clima> pronosticoFuturo; // Almacena T+1, T+2, T+3, T+4, T+5, T+6
    
	
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
        // Generamos los 6 pronósticos futuros (Hora 1 a 6)
        for (int i = 1; i <= 6; i++) {
            this.pronosticoFuturo.add(generarClimaAleatorio(i));
        }
		
		// 1. Configurar el layout principal (directamente sobre el panel)
        setLayout(new BorderLayout(10, 10));
		
        // 2. Aplicar el borde/margen (directamente sobre el panel)
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 3. Crear el panel de controles (Norte)
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
        
        // --- 2. Panel Izquierdo (Tabla "Clima Actual") ---
        
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
        tablaDatos.add(crearCeldaTabla("Tipo", SwingConstants.CENTER, headerFont));
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
        tablaDatos.add(crearCeldaTabla("Nieve [mm/h]", SwingConstants.LEFT, typeFont));
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
        
        // 3. Panel Derecho (Pronóstico 6 Horas)
        panelTablaClima = new JPanel(new GridLayout(1, 6, 10, 10));
        panelTablaClima.setBorder(BorderFactory.createTitledBorder("Pronóstico Próximas 6 Horas"));
        
        // 4. Panel de Contenido Principal (Izquierda + Derecha)
        JPanel panelWrapperIzquierdo = new JPanel(new BorderLayout());
        panelWrapperIzquierdo.add(panelHoraActual, BorderLayout.NORTH); // Lo anclamos al NORTE
        JPanel mainContentPanel = new JPanel(new BorderLayout(10, 10));
        mainContentPanel.add(panelWrapperIzquierdo, BorderLayout.WEST); // Añadimos el wrapper
        mainContentPanel.add(panelTablaClima, BorderLayout.CENTER); // El pronóstico ocupa el resto
        add(mainContentPanel, BorderLayout.CENTER);
        
        
     
        // 6. Añadir Listeners a los botones
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
        actualizarTablaClima();
        
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
	
	
	
	private void actualizarPanelHoraActual() {
        if (climaHoraActual == null) return;

        // Actualizamos el título del panel
        lblTituloClima.setText(String.format("Clima Actual (Hora %02d:00)", (horaActual % 24)));
        
        // Actualizamos los valores (solo el texto)
        valTemp.setText(String.format("%.1f", climaHoraActual.getTemperatura()));
        valViento.setText(String.format("%.1f", climaHoraActual.getVelocidadViento()));
        valNiebla.setText(String.format("%.1f", climaHoraActual.getVisibilidadKm()));
        
        valNubes.setText(climaHoraActual.getTechoNubesMetros() >= 10000 ? 
            "N/A" : String.format("%d", climaHoraActual.getTechoNubesMetros()));

        // Lógica para Lluvia/Nieve
        double precipitacion = climaHoraActual.getPrecipitacion();
        
        if (climaHoraActual instanceof ClimaNevado) {
            valLluvia.setText("0.0");
            valNieve.setText(String.format("%.1f", precipitacion));
        } else if (precipitacion > 0) { // Lluvioso
            valLluvia.setText(String.format("%.1f", precipitacion));
            valNieve.setText("0.0");
        } else { // Despejado o Nublado
            valLluvia.setText("0.0");
            valNieve.setText("0.0");
        }
        
        // (La alineación ya está definida en la creación de los JLabels)
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
        this.horaActual = (this.horaActual + 1) % 24; // Avanza la hora (ej. 0 a 1)
        
        // 1. El pronóstico T+1 se convierte en el clima actual T
        this.climaHoraActual = pronosticoFuturo.poll(); // Saca el primer item de la cola

        // 2. Generamos un nuevo pronóstico para T+6
        int horaNuevoPronostico = (this.horaActual + 6) % 24; // La nueva 6ª hora
        Clima nuevoPronostico = generarClimaAleatorio(horaNuevoPronostico);
        
        // 3. Añadimos el nuevo pronóstico al final de la cola
        pronosticoFuturo.add(nuevoPronostico);
        
        // 4. Actualizamos las dos tablas en la UI
        actualizarPanelHoraActual(); // Muestra el nuevo clima actual
        actualizarTablaClima(); // Muestra la nueva cola de 6 pronósticos
    }
    
    private void avanzarHoraRapido() {
        avanzarHora(); // Ejecuta la lógica del turno
        
        segundosTranscurridosEnLaHora = 0; // Resetea el contador de segundos
        actualizarLabelReloj(); // Actualiza el label visual a HH:00
        relojInterno.restart(); // Reinicia el timer para que el próximo tick sea en 1 seg
        
        btnPausarReanudar.setText("Pausar");
    }
    
    /**
     * Reinicia la hora a 0 y actualiza la interfaz.
     */
    private void reiniciarSimulacion() {
        this.horaActual = 0;
        this.segundosTranscurridosEnLaHora = 0;
        
        // Vuelve a llenar la cola
        pronosticoFuturo.clear();
        this.climaHoraActual = generarClimaAleatorio(0);
        for (int i = 1; i <= 6; i++) {
            pronosticoFuturo.add(generarClimaAleatorio(i));
        }
        
        // Actualiza toda la UI
        actualizarLabelReloj();
        actualizarPanelHoraActual();
        actualizarTablaClima();
        
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
	
    /**
     * Esta es la función CLAVE.
     * Borra la tabla y la vuelve a generar con las 6 horas correspondientes.
     */
    private void actualizarTablaClima() {
        panelTablaClima.removeAll(); // Borra las 6 columnas viejas
        
        int i = 1; // Empezamos en T+1
        for (Clima climaPronosticado : pronosticoFuturo) {
            // Calculamos la hora que representará esta columna
            int horaEnPanel = (this.horaActual + i) % 24;
            
            // Creamos la columna con el clima de la cola
            JPanel panelColumna = crearPanelColumnaResumen(climaPronosticado, horaEnPanel); 
            panelTablaClima.add(panelColumna); // Añadimos la nueva columna
            i++;
        }
        
        panelTablaClima.revalidate(); // Re-dibuja el panel de pronóstico
        panelTablaClima.repaint();
    }
    
    /**
     * Crea un panel individual (una "columna") para un clima específico.
     * @param clima El objeto Clima con los datos.
     * @param hora La hora que representa este panel.
     * @return Un JPanel formateado.
     */
    private JPanel crearPanelColumnaResumen(Clima clima, int hora) {
        JPanel panelColumna = new JPanel(new BorderLayout(5, 5));
        String horaFormateada = String.format("%02d:00", hora % 24);
        
        panelColumna.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                " Hora: " + horaFormateada + " ",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));

        // Panel de Resumen (Big 5)
        JPanel panelDatosResumen = new JPanel(new GridLayout(5, 2, 3, 3));
        panelDatosResumen.setOpaque(false);
        panelDatosResumen.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panelDatosResumen.add(new JLabel("Temp:"));
        panelDatosResumen.add(new JLabel(String.format("%.1f °C", clima.getTemperatura())));
        panelDatosResumen.add(new JLabel("Viento:"));
        panelDatosResumen.add(new JLabel(String.format("%.1f km/h", clima.getVelocidadViento())));
        panelDatosResumen.add(new JLabel("Visib:"));
        panelDatosResumen.add(new JLabel(String.format("%.1f km", clima.getVisibilidadKm())));
        panelDatosResumen.add(new JLabel("Techo:"));
        panelDatosResumen.add(new JLabel(clima.getTechoNubesMetros() >= 10000 ? "DESPEJADO" : clima.getTechoNubesMetros() + " m"));
        panelDatosResumen.add(new JLabel("Precip:"));
        panelDatosResumen.add(new JLabel(String.format("%.1f mm/h", clima.getPrecipitacion())));
        
        panelColumna.add(panelDatosResumen, BorderLayout.CENTER);

        // Lógica del color de peligro
        if (clima.isSenalPeligro()) {
            panelColumna.setBackground(new Color(255, 220, 220));
            panelDatosResumen.setOpaque(true);
            panelDatosResumen.setBackground(new Color(255, 220, 220));
        } else {
             panelColumna.setBackground(Color.WHITE);
        }

        return panelColumna;
    }
    
    private double round(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
    
    private Clima generarClimaAleatorio(int hora) {
        int tipoClima = generadorAleatorio.nextInt(4);
        boolean esDeNoche = (hora > 20 || hora < 6);

        // --- 1. Generar valores base (que pueden ser sobreescritos) ---
        double temp = -5 + (35 * generadorAleatorio.nextDouble());
        double viento = 120 * generadorAleatorio.nextDouble();
        double visi = 20 * generadorAleatorio.nextDouble(); // Visibilidad base (alta)
        double humedad = 20 + (80 * generadorAleatorio.nextDouble());
        double presion = 980 + (50 * generadorAleatorio.nextDouble());

        if (esDeNoche) { temp -= 5.0; } // Más frío de noche

        // --- 2. Aplicar lógica de clima específica ---
        switch (tipoClima) {
            
            case 0: // Despejado
            {
                // LÓGICA: precipitación 0, nubes "infinitas"
                double precipitacion = 0.0;
                int techoNubes = 10000; // Valor simbólico para "Despejado"
                
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
                // LÓGICA: precipitación > 0, nubes BAJAS
                double precipitacion = 1 + (50 * generadorAleatorio.nextDouble()); // Mínimo 1.0
                int techoNubes = 100 + generadorAleatorio.nextInt(1000); // Nubes bajas (100-1100m)
                boolean tormenta = generadorAleatorio.nextBoolean();
                
                // Si llueve mucho, baja la visibilidad
                if (precipitacion > 25) {
                    visi = visi / 3;
                } else if (precipitacion > 10) {
                    visi = visi / 2;
                }
                
                return new ClimaLluvioso(round(temp), round(viento), round(visi), 
                                      round(precipitacion), techoNubes, round(humedad), 
                                      round(presion), tormenta);
            }

            case 2: // Nublado
            {
                // LÓGICA: precipitación 0, nubes variables (pero no 0 ni 10000)
                double precipitacion = 0.0;
                int techoNubes = 150 + generadorAleatorio.nextInt(2000); // Nubes (150-2150m)
                
                // Si las nubes están muy bajas (niebla alta), baja la visibilidad
                if (techoNubes < 300) {
                    visi = visi / 2;
                }

                return new ClimaNublado(round(temp), round(viento), round(visi), 
                                    techoNubes, round(humedad), round(presion));
            }

            case 3: // Nevado
            default:
            {
                // LÓGICA: precipitación > 0, nubes MUY BAJAS, temp <= 0
                double precipitacion = 1 + (20 * generadorAleatorio.nextDouble()); // Nieve (mínimo 1.0)
                int techoNubes = 100 + generadorAleatorio.nextInt(800); // Nubes muy bajas (100-900m)
                double acumulacion = 1 + (10 * generadorAleatorio.nextDouble());
                
                // Forzar temperatura bajo cero
                if (temp > 0) {
                    temp = - (temp / 5);
                }
                
                // La nieve siempre reduce mucho la visibilidad
                visi = visi / 4; 
                
                return new ClimaNevado(round(temp), round(viento), round(visi), 
                                     round(precipitacion), techoNubes, round(humedad), 
                                     round(presion), round(acumulacion));
            }
        }
    }
}