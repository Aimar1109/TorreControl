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

public class JPanelClima extends JPanel {

	private static final long serialVersionUID = 1L;
	
	// --- Componentes Gráficos ---
    private JLabel lblReloj;
    private JButton btnPausarReanudar;
    private JButton btnReiniciar;
    private JPanel panelTablaClima; // Panel central con GridLayout
    private JButton btnAvanzarHoraRapido;
    
    private int horaActual;
    private Random generadorAleatorio;
    private int segundosTranscurridosEnLaHora;
	
	// Lógica del Reloj Interno
	private Timer relojInterno;
	
	private static final int SEGUNDOS_REALES_POR_HORA_SIMULADA = 60;	// 1 hora del programa = 60 segundos reales
	
	private static final int TICK_DEL_RELOJ_MS = 1000; // 1000ms = 1 segundo
	
	public JPanelClima() {
		this.horaActual = 0;
		this.generadorAleatorio = new Random();
		this.segundosTranscurridosEnLaHora = 0;
		
		
		// 1. Configurar el layout principal (directamente sobre el panel)
        setLayout(new BorderLayout(10, 10));
		
        // 2. Aplicar el borde/margen (directamente sobre el panel)
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 3. Crear el panel de controles (Norte)
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        lblReloj = new JLabel("Hora actual: 0");
        lblReloj.setFont(new Font("Arial", Font.BOLD, 16));
        
        btnPausarReanudar = new JButton("Pausar");
        btnReiniciar = new JButton("Reiniciar Simulación");
        btnAvanzarHoraRapido = new JButton("Avanzar 1 Hora");
        
        panelControles.add(lblReloj);
        panelControles.add(btnPausarReanudar);
        panelControles.add(btnReiniciar);
        panelControles.add(btnAvanzarHoraRapido);
        
        // 4. Crear el panel de la "tabla" de climas (Centro)
        // Usamos GridLayout de 1 fila y 6 columnas, con 10px de separación
        panelTablaClima = new JPanel(new GridLayout(1, 6, 10, 10));
        panelTablaClima.setBorder(BorderFactory.createTitledBorder("Pronóstico Próximas 6 Horas"));
        
        // 5. Añadir los paneles (directamente sobre el panel)
        add(panelControles, BorderLayout.NORTH);
        add(panelTablaClima, BorderLayout.CENTER);
        
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
        this.horaActual++;
        actualizarTablaClima();
    }
    
    /**
     * ¡NUEVO! Método para el botón de "Avanzar 1 Hora".
     */
    private void avanzarHoraRapido() {
        avanzarHora(); // Avanza la lógica de la hora y actualiza la tabla
        segundosTranscurridosEnLaHora = 0; // Resetea el contador de segundos
        actualizarLabelReloj(); // Actualiza el label visual a HH:00
        relojInterno.restart(); // Reinicia el timer para que el próximo tick automático sea en 1 seg
        
        // Asegurarse de que el botón de pausa esté en el estado correcto
        if (!relojInterno.isRunning()) {
            // Si el reloj estaba pausado, lo reanudamos
            toggleReloj();
        } else {
             btnPausarReanudar.setText("Pausar");
        }
    }
    
    /**
     * Reinicia la hora a 0 y actualiza la interfaz.
     */
    private void reiniciarSimulacion() {
        this.horaActual = 0;
        this.segundosTranscurridosEnLaHora = 0;		// Resetea los segundos a 0
        
        actualizarLabelReloj();
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
        // 1. Borrar todos los componentes anteriores del panel
        panelTablaClima.removeAll();

        // 2. Generar y añadir las 6 "columnas" (paneles) nuevas
        for (int i = 0; i < 6; i++) {
            int horaMostrada = this.horaActual + i;
            
            // Generamos un clima aleatorio para esa hora
            Clima climaDeLaHora = generarClimaAleatorio();
            climaDeLaHora.actualizarSenalPeligro(); // Importante calcular el peligro

            // Creamos un panel-columna para ese clima
            JPanel panelColumna = crearPanelColumna(climaDeLaHora, horaMostrada);
            
            // Añadimos la nueva columna al GridLayout
            panelTablaClima.add(panelColumna);
        }

        // 3. Refrescar el panel para que muestre los cambios
        panelTablaClima.revalidate();
        panelTablaClima.repaint();
    }
    
    /**
     * Crea un panel individual (una "columna") para un clima específico.
     * @param clima El objeto Clima con los datos.
     * @param hora La hora que representa este panel.
     * @return Un JPanel formateado.
     */
    private JPanel crearPanelColumna(Clima clima, int hora) {
        JPanel panelColumna = new JPanel(new BorderLayout(5, 5));
        
        // Formateamos la hora para que se vea bien (ej: 08:00)
        String horaFormateada = String.format("%02d:00", hora % 24);
        
        // Usamos TitledBorder para poner la hora como título de la columna
        panelColumna.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                " Hora: " + horaFormateada + " ",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));

        // Usamos un GridLayout de 5 filas y 2 columnas (para "Etiqueta" y "Valor")
        JPanel panelDatosResumen = new JPanel(new GridLayout(5, 2, 3, 3));
        panelDatosResumen.setOpaque(false); // Transparente para heredar el color
        panelDatosResumen.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // 1. Temperatura
        panelDatosResumen.add(new JLabel("Temp:"));
        panelDatosResumen.add(new JLabel(String.format("%.1f °C", clima.getTemperatura())));
        
        // 2. Viento
        panelDatosResumen.add(new JLabel("Viento:"));
        panelDatosResumen.add(new JLabel(String.format("%.1f km/h", clima.getVelocidadViento())));
        
        // 3. Visibilidad
        panelDatosResumen.add(new JLabel("Visib:"));
        panelDatosResumen.add(new JLabel(String.format("%.1f km", clima.getVisibilidadKm())));
        
        // 4. Techo de Nubes
        panelDatosResumen.add(new JLabel("Techo:"));
        panelDatosResumen.add(new JLabel(clima.getTechoNubesMetros() >= 10000 ? "DESPEJADO" : clima.getTechoNubesMetros() + " m"));
        
        // 5. Precipitación
        panelDatosResumen.add(new JLabel("Precip:"));
        panelDatosResumen.add(new JLabel(String.format("%.1f mm/h", clima.getPrecipitacion())));
        
        // Añadimos el panel de datos al centro de la columna
        panelColumna.add(panelDatosResumen, BorderLayout.CENTER);
        
        // La lógica del color de peligro sigue igual
        if (clima.isSenalPeligro()) {
            panelColumna.setBackground(new Color(255, 220, 220));
            panelDatosResumen.setOpaque(true);
            panelDatosResumen.setBackground(new Color(255, 220, 220));
        } else {
             panelColumna.setBackground(Color.WHITE);
        }

        return panelColumna;
    }
    
    private Clima generarClimaAleatorio() {
        int tipoClima = generadorAleatorio.nextInt(4); // 0, 1, 2, o 3

        // Datos base aleatorios (ahora incluye humedad y presión)
        double temp = -5 + (35 * generadorAleatorio.nextDouble());
        double viento = 100 * generadorAleatorio.nextDouble();
        double visi = 20 * generadorAleatorio.nextDouble();
        double humedad = 20 + (80 * generadorAleatorio.nextDouble()); // Humedad 20-100%
        double presion = 980 + (50 * generadorAleatorio.nextDouble()); // Presión 980-1030 hPa
        
        // Redondeo
        temp = Math.round(temp * 10.0) / 10.0;
        viento = Math.round(viento * 10.0) / 10.0;
        visi = Math.round(visi * 10.0) / 10.0;
        humedad = Math.round(humedad * 10.0) / 10.0;
        presion = Math.round(presion * 10.0) / 10.0;

        switch (tipoClima) {
        	case 0: // Despejado
        		IntensidadSol[] intensidades = IntensidadSol.values();
        		IntensidadSol intensidad = intensidades[generadorAleatorio.nextInt(intensidades.length)];
        		return new ClimaDespejado(temp, viento, visi, humedad, tipoClima, presion, presion, intensidad);

        case 1: // Lluvioso
            boolean tormenta = generadorAleatorio.nextBoolean();
            double precipitacion = 50 * generadorAleatorio.nextDouble();
            precipitacion = Math.round(precipitacion * 10.0) / 10.0;
            int techoLluvia = 100 + generadorAleatorio.nextInt(1000); // Techo bajo
            return new ClimaLluvioso(temp, viento, visi, precipitacion, techoLluvia, humedad, presion, tormenta);

        case 2: // Nublado
            int techoNubes = 150 + generadorAleatorio.nextInt(2000); // Techo variable
            return new ClimaNublado(temp, viento, visi, techoNubes, humedad, presion);

        case 3: // Nevado
        	default:
        		double precipitacionNieve = 20 * generadorAleatorio.nextDouble(); // Nieve
        		precipitacionNieve = Math.round(precipitacionNieve * 10.0) / 10.0;
        		int techoNieve = 100 + generadorAleatorio.nextInt(800); // Techo muy bajo
        		double acumulacion = 10 * generadorAleatorio.nextDouble();
        		acumulacion = Math.round(acumulacion * 10.0) / 10.0;
            
        		// Forzamos la temperatura a bajo cero
        		if (temp > 0) temp = -temp / 5; 
        		temp = Math.round(temp * 10.0) / 10.0;
            
        		return new ClimaNevado(temp, viento, visi, precipitacionNieve, techoNieve, humedad, presion, acumulacion);
        }
    }
}