package gui;

import domain.Clima;
import domain.IntensidadSol;
import domain.Clima.ClimaDespejado;
import domain.Clima.ClimaLluvioso;
import domain.Clima.ClimaNevado;
import domain.Clima.ClimaNublado;
import threads.ObservadorTiempo;
import threads.RelojGlobal;
import domain.PaletaColor;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import java.util.LinkedList;
	
public class JPanelClima extends JPanel implements ObservadorTiempo {

	private static final long serialVersionUID = 1L;
	
	// Componentes Gráficos
    private JLabel lblReloj;
    private JLabel lblHeaderTitulo;
    private JTabbedPane tabbedPaneGraficos;
    private GraficoTemperatura graficoTemperatura;
    private GraficoPrecipitacion graficoPrecipitacion;

    // Componentes del Panel Izquierdo
    private JLabel lblTituloClima;
    private JLabel valTemp, valViento, valLluvia, valNieve, valNiebla, valNubes;
    private PanelBrujula panelBrujula;

    // Lógica de Simulación
    private int horaActualInt;
    private Random generadorAleatorio;
    private Clima climaHoraActual;
    private LinkedList<Clima> pronosticoFuturo;
    
    private JLabel lblEstadoAeropuerto;
    private JPanel panelEstado;

	
	public JPanelClima() {
		this.horaActualInt = 0;
		this.generadorAleatorio = new Random();	
		this.pronosticoFuturo = new LinkedList<>();
		
		// Generamos datos inciales
        this.climaHoraActual = generarClimaAleatorio(0);
        for (int i = 1; i <= 5; i++) {
            this.pronosticoFuturo.add(generarClimaAleatorio(i));
        }
		
        setLayout(new BorderLayout(0, 0));
        setBackground(PaletaColor.get(PaletaColor.FONDO));
        
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(PaletaColor.get(PaletaColor.PRIMARIO));
        panelHeader.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        lblReloj = new JLabel("00:00:00");
        lblReloj.setFont(new Font("Consolas", Font.BOLD, 18));
        lblReloj.setForeground(Color.WHITE);
        lblReloj.setHorizontalAlignment(SwingConstants.LEFT);
        panelHeader.add(lblReloj, BorderLayout.WEST);
        
        lblHeaderTitulo = new JLabel("MONITORIZACIÓN METEOROLÓGICA", JLabel.CENTER);
        lblHeaderTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeaderTitulo.setForeground(Color.WHITE);
        panelHeader.add(lblHeaderTitulo, BorderLayout.CENTER);
        
        
        JLabel lblDummy = new JLabel("00:00:00");
        lblDummy.setFont(new Font("Consolas", Font.BOLD, 18));
        lblDummy.setForeground(new Color(0, 0, 0, 0)); 
        panelHeader.add(lblDummy, BorderLayout.EAST);
        
        add(panelHeader, BorderLayout.NORTH);
        
        JPanel panelContenido = new JPanel(new BorderLayout(15, 15));
        panelContenido.setBackground(PaletaColor.get(PaletaColor.FONDO));
        panelContenido.setBorder(new EmptyBorder(15, 15, 15, 15));

		JPanel panelIzquierdo = new JPanel();
		panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
		panelIzquierdo.setBackground(PaletaColor.get(PaletaColor.FONDO));
		panelIzquierdo.setPreferredSize(new Dimension(280, 0));
		
        JPanel panelTablaContainer = new JPanel(new BorderLayout());
        panelTablaContainer.setBackground(Color.WHITE);
        panelTablaContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PaletaColor.get(PaletaColor.PRIMARIO), 1),
                new EmptyBorder(5, 5, 5, 5)
        ));
        
        lblTituloClima = new JLabel(String.format("DATOS HORA %02d:00", horaActualInt));
        lblTituloClima.setFont(new Font("Arial", Font.BOLD, 16));
        lblTituloClima.setForeground(PaletaColor.get(PaletaColor.PRIMARIO));
        lblTituloClima.setHorizontalAlignment(SwingConstants.CENTER);
        lblTituloClima.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        
        // La tabla de datos interna
        JPanel tablaDatos = new JPanel(new GridLayout(7, 2, 1, 1));
        tablaDatos.setBackground(PaletaColor.get(PaletaColor.FONDO));
        tablaDatos.setBorder(BorderFactory.createLineBorder(PaletaColor.get(PaletaColor.FONDO)));
        
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

        panelTablaContainer.add(lblTituloClima, BorderLayout.NORTH);
        panelTablaContainer.add(tablaDatos, BorderLayout.CENTER);
        
        
        panelBrujula = new PanelBrujula();
        panelBrujula.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panelIzquierdo.add(panelTablaContainer);
        panelIzquierdo.add(Box.createVerticalStrut(20));
        
        JPanel panelBrujulaWrapper = new JPanel(new BorderLayout());
        panelBrujulaWrapper.setBackground(PaletaColor.get(PaletaColor.FONDO));
        JLabel lblBrujula = new JLabel("DIRECCIÓN DEL VIENTO");
        lblBrujula.setFont(new Font("Arial", Font.BOLD, 14));
        lblBrujula.setForeground(Color.DARK_GRAY);
        lblBrujula.setHorizontalAlignment(SwingConstants.CENTER);
        lblBrujula.setBorder(new EmptyBorder(0,0,5,0));
        
        panelBrujulaWrapper.add(lblBrujula, BorderLayout.NORTH);
        panelBrujulaWrapper.add(panelBrujula, BorderLayout.CENTER);
        panelBrujulaWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelIzquierdo.add(panelBrujulaWrapper);
        panelIzquierdo.add(Box.createVerticalGlue());
        
        panelEstado = new JPanel(new BorderLayout());
        panelEstado.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelEstado.setBackground(new Color(60, 179, 113));
        
        lblEstadoAeropuerto = new JLabel("OPERATIVO");
        lblEstadoAeropuerto.setFont(new Font("Arial", Font.BOLD, 18));
        lblEstadoAeropuerto.setForeground(Color.WHITE);
        lblEstadoAeropuerto.setHorizontalAlignment(SwingConstants.CENTER);
        
        panelEstado.add(lblEstadoAeropuerto, BorderLayout.CENTER);
        
        panelIzquierdo.add(Box.createVerticalStrut(20));
        panelIzquierdo.add(panelEstado);
        
        
        graficoTemperatura = new GraficoTemperatura();
        graficoPrecipitacion = new GraficoPrecipitacion();
        
        // Creamos el JTabbedPane
        tabbedPaneGraficos = new JTabbedPane();
        tabbedPaneGraficos.setFont(new Font("Arial", Font.PLAIN, 12));
        tabbedPaneGraficos.addTab("Temperatura", graficoTemperatura);
        tabbedPaneGraficos.addTab("Precipitación", graficoPrecipitacion);
        
        JPanel panelGraficosWrapper = new JPanel(new BorderLayout());
        panelGraficosWrapper.setBackground(PaletaColor.get(PaletaColor.FONDO));
        
        TitledBorder borderGraficos = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PaletaColor.get(PaletaColor.PRIMARIO)), "Pronóstico");
        borderGraficos.setTitleFont(new Font("Arial", Font.BOLD, 14));
        borderGraficos.setTitleColor(PaletaColor.get(PaletaColor.PRIMARIO));
        
        panelGraficosWrapper.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 0, 0), // Margen externo
                borderGraficos
        ));
        
        JPanel panelInternoGraficos = new JPanel(new BorderLayout());
        panelInternoGraficos.setBackground(Color.WHITE);
        panelInternoGraficos.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelInternoGraficos.add(tabbedPaneGraficos, BorderLayout.CENTER);
        
        panelGraficosWrapper.add(panelInternoGraficos, BorderLayout.CENTER);
        
        panelContenido.add(panelIzquierdo, BorderLayout.WEST);
        panelContenido.add(panelGraficosWrapper, BorderLayout.CENTER);
        
        add(panelContenido, BorderLayout.CENTER);
        
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
        
        if (c.isSenalPeligro()) {
            // Condiciones peligrosas (Viento fuerte, tormenta, etc.)
            panelEstado.setBackground(new Color(220, 53, 69)); // Rojo Alerta
            lblEstadoAeropuerto.setText("¡PELIGRO: PISTA CERRADA!");
            
            // Opcional: Indicar la causa
            if (c.getVelocidadViento() > 80) lblEstadoAeropuerto.setText("¡ALERTA: VIENTO PELIGROSO!");
            if (c.getVisibilidadKm() < 1.0) lblEstadoAeropuerto.setText("¡VISIBILIDAD NULA!");
            
        } else {
            // Condiciones normales
            panelEstado.setBackground(new Color(40, 167, 69)); // Verde Operativo
            lblEstadoAeropuerto.setText("AEROPUERTO OPERATIVO");
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
            label.setBackground(PaletaColor.get(PaletaColor.PRIMARIO));
            label.setForeground(Color.WHITE);
        } else {
            // DATO NORMAL: Fondo blanco, texto negro
            label.setBackground(PaletaColor.get(PaletaColor.FONDO));
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