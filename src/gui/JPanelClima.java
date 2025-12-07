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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import javax.swing.border.TitledBorder;

import java.util.LinkedList;
	
public class JPanelClima extends JPanel implements ObservadorTiempo {

	private static final long serialVersionUID = 1L;
	
	// Componentes Gráficos
    private JLabel lblReloj;

    // Componentes del Panel Izquierdo
    private JLabel lblTituloClima;
    private JLabel valTemp, valViento, valLluvia, valNieve, valNiebla, valNubes;
    
    // Panel de la Brújula
    private PanelBrujula panelBrujula;
    
    // Pestañas para los gráficos
    private JTabbedPane tabbedPaneGraficos;
    private GraficoTemperatura graficoTemperatura;
    private GraficoPrecipitacion graficoPrecipitacion;
    
    // Lógica de Simulación
    private int horaActualInt;
    private Random generadorAleatorio;

    private Clima climaHoraActual;
    private LinkedList<Clima> pronosticoFuturo;
    
    // Colores
    private final Color COLOR_ACENTO = new Color(0, 85, 165); 
    private final Color COLOR_FONDO_DATOS = Color.WHITE;
	
	public JPanelClima() {
		this.horaActualInt = 0;
		this.generadorAleatorio = new Random();	
		this.pronosticoFuturo = new LinkedList<>();
		
		// Generamos datos inciales
        this.climaHoraActual = generarClimaAleatorio(0);
        for (int i = 1; i <= 5; i++) {
            this.pronosticoFuturo.add(generarClimaAleatorio(i));
        }
		
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
		JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lblReloj = new JLabel("00:00:00");
		lblReloj.setFont(new Font("Monospaced", Font.BOLD, 26));
		panelControles.add(lblReloj);
		add(panelControles, BorderLayout.NORTH);

		JPanel panelIzquierdo = new JPanel();
		panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
		panelIzquierdo.setPreferredSize(new Dimension(280, 0));
		
        JPanel panelTablaWrapper = new JPanel(new BorderLayout());
        panelTablaWrapper.setBackground(COLOR_FONDO_DATOS);
        panelTablaWrapper.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        lblTituloClima = new JLabel("Clima Actual");
        lblTituloClima.setFont(new Font("Arial", Font.BOLD, 16)); // Fuente más pequeña
        lblTituloClima.setHorizontalAlignment(SwingConstants.CENTER);
        lblTituloClima.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // La tabla de datos interna
        JPanel tablaDatos = new JPanel(new GridLayout(7, 2, 5, 5));
        tablaDatos.setBackground(Color.WHITE);
        tablaDatos.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Definimos las fuentes y alineaciones
        Font headerFont = new Font("Arial", Font.BOLD, 14);
        Font typeFont = new Font("Arial", Font.PLAIN, 14);
        Font valueFont = new Font("Monospaced", Font.PLAIN, 14);
        
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

        panelTablaWrapper.add(lblTituloClima, BorderLayout.NORTH);
        panelTablaWrapper.add(tablaDatos, BorderLayout.CENTER);
        
        
        
        panelBrujula = new PanelBrujula();
        panelBrujula.setMaximumSize(new Dimension(280, 250));
        
        panelIzquierdo.add(panelTablaWrapper);
        panelIzquierdo.add(Box.createVerticalStrut(20));
        panelIzquierdo.add(panelBrujula);
        panelIzquierdo.add(Box.createVerticalGlue());
        
        add(panelIzquierdo, BorderLayout.WEST);
        
        graficoTemperatura = new GraficoTemperatura();
        graficoPrecipitacion = new GraficoPrecipitacion();
        
        // Creamos el JTabbedPane
        tabbedPaneGraficos = new JTabbedPane();
        tabbedPaneGraficos.setFont(new Font("Arial", Font.PLAIN, 14));
        tabbedPaneGraficos.addTab("Temperatura", graficoTemperatura);
        tabbedPaneGraficos.addTab("Precipitación", graficoPrecipitacion);
        
        JPanel panelGraficosPronostico = new JPanel(new BorderLayout());
        TitledBorder border = BorderFactory.createTitledBorder("Pronóstico");
        border.setTitleFont(new Font("Arial", Font.BOLD, 14));
        border.setTitleColor(COLOR_ACENTO);
        panelGraficosPronostico.setBorder(border);
        panelGraficosPronostico.add(tabbedPaneGraficos, BorderLayout.CENTER);
        
        // Panel Contenedor para dar márgenes verticales 
        JPanel panelContenedorGraficos = new JPanel(new BorderLayout());
        panelContenedorGraficos.setBorder(BorderFactory.createEmptyBorder(0, 20, 80, 0));
        panelContenedorGraficos.add(panelGraficosPronostico, BorderLayout.CENTER);
        
        add(panelContenedorGraficos, BorderLayout.CENTER);
        
        graficoTemperatura.setHoverListener(new GraficoTemperatura.OnHoverListener() {
            @Override
            public void onPuntoHover(Clima climaHovered, int indiceOffset) {
                int horaHovered = (horaActualInt + indiceOffset) % 24;
                actualizarDatosUI(climaHovered, horaHovered);
            }
            @Override
            public void onPuntoExit() {
                actualizarDatosUI(climaHoraActual, horaActualInt);
            }
        });
        
        actualizarDatosUI(climaHoraActual, horaActualInt);
        actualizarGraficos();
        
        RelojGlobal.getInstancia().addObservador(this);
	}

	private void actualizarDatosUI(Clima c, int hora) {
        if (c == null) return;
        
        // Actualizar Título
        lblTituloClima.setText(String.format("Clima (Hora %02d:00)", hora));
        
        // Actualizar Tabla
        valTemp.setText(String.format("%.1f", c.getTemperatura()));
        valTemp.setForeground(c.getTemperatura() > 30 ? Color.RED : (c.getTemperatura() < 0 ? Color.BLUE : Color.BLACK));
        
        valViento.setText(String.format("%.1f", c.getVelocidadViento()));
        valNiebla.setText(String.format("%.1f", c.getVisibilidadKm()));
        valNubes.setText(String.valueOf(c.getTechoNubesMetros()));
        
        // Lógica precipitación
        if (c instanceof ClimaNevado) {
            valLluvia.setText("0.0");
            valNieve.setText(String.format("%.1f", c.getPrecipitacion()));
        } else {
            valLluvia.setText(String.format("%.1f", c.getPrecipitacion()));
            valNieve.setText("0.0");
        }
        
        // Actualizar Brújula
        if (panelBrujula != null) {
            panelBrujula.setDatos(c.getVelocidadViento(), c.getDireccionViento());
        }
    }
	
	private void actualizarGraficos() {
        LinkedList<Clima> datos = new LinkedList<>(pronosticoFuturo);
        datos.addFirst(climaHoraActual);
        
        if (graficoTemperatura != null) graficoTemperatura.setDatos(datos, horaActualInt);
        if (graficoPrecipitacion != null) graficoPrecipitacion.setDatos(datos, horaActualInt);
    }
	
	public void actualizarTiempo(LocalDateTime nuevoTiempo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        lblReloj.setText(nuevoTiempo.format(formatter));
        
        if (nuevoTiempo.getHour() != this.horaActualInt) {
            this.horaActualInt = nuevoTiempo.getHour();
            avanzarHora();
        }
    }
	
	private void avanzarHora() {
        if (!pronosticoFuturo.isEmpty()) {
            this.climaHoraActual = pronosticoFuturo.poll();
        } else {
            this.climaHoraActual = generarClimaAleatorio(this.horaActualInt);
        }
        
        // Añadir nuevo pronóstico al final
        pronosticoFuturo.add(generarClimaAleatorio((this.horaActualInt + 5) % 24));
        
        actualizarDatosUI(climaHoraActual, horaActualInt);
        actualizarGraficos();
    }
	
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
	
	
    
    
    
    private double round(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
    
    
    // Este método genera un objeto Clima por cada hora
    private Clima generarClimaAleatorio(int hora) {
        int tipoClima = generadorAleatorio.nextInt(4);
        boolean esDeNoche = (hora > 20 || hora < 6);

        double temp = -3 + (35 * generadorAleatorio.nextDouble());
        // Aseguramos viento mínimo para que la brújula se mueva algo (entre 5 y 100 km/h)
        double viento = 5 + (95 * generadorAleatorio.nextDouble()); 
        double visi = 20 * generadorAleatorio.nextDouble();
        double humedad = 20 + (80 * generadorAleatorio.nextDouble());
        double presion = 980 + (50 * generadorAleatorio.nextDouble());
        double direccion = generadorAleatorio.nextDouble() * 360; // Dirección aleatoria

        if (esDeNoche) { temp -= 5.0; }

        Clima c = null;
        switch (tipoClima) {
            case 0: // Despejado
                IntensidadSol intensidad = IntensidadSol.BAJA;
                if (!esDeNoche) intensidad = IntensidadSol.ALTA; // Simplificado
                c = new ClimaDespejado(round(temp), round(viento), round(visi), round(humedad), round(presion), intensidad);
                break;
            case 1: // Lluvioso
                double precipitacion = 1 + (50 * generadorAleatorio.nextDouble());
                c = new ClimaLluvioso(round(temp), round(viento), round(visi), round(precipitacion), 1000, 80, round(humedad), round(presion), false);
                break;
            case 2: // Nublado
                c = new ClimaNublado(round(temp), round(viento), round(visi), 1000, 20, round(humedad), round(presion));
                break;
            case 3: // Nevado
            default:
                c = new ClimaNevado(round(temp), round(viento), round(visi), 5.0, 500, 90, round(humedad), round(presion), 2.0);
                break;
        }
        
        // Importante: setear la dirección generada
        if (c != null) c.setDireccionViento(direccion);
        return c;
    }
}