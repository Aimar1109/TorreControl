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
import domain.ServicioMeteorologico;
import jdbc.GestorBD;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.util.LinkedList;
import java.util.List;
	
public class JPanelClima extends JPanel implements ObservadorTiempo {

	private static final long serialVersionUID = 1L;
	
	// Componentes Gráficos
    private JLabel lblReloj;
    private JLabel lblHeaderTitulo;
    private GraficoTemperatura graficoTemperatura;
    private GraficoPrecipitacion graficoPrecipitacion;
    
    private JToggleButton btnTabTemperatura;
    private JToggleButton btnTabPrecipitacion;
    private ButtonGroup grupoPestanas;
    private JPanel panelContenidoGraficos; // Panel con CardLayout
    private CardLayout cardLayout;

    // Componentes del Panel Izquierdo
    private JLabel lblTituloClima;
    private JLabel valTemp, valViento, valLluvia, valNieve, valNiebla, valNubes, valPresion;
    private PanelBrujula panelBrujula;
    private JLabel lblVelocidadViento;

    // Lógica de Simulación
    private int horaActualInt;
    private Random generadorAleatorio;
    private LinkedList<Clima> historiaDia;
    private JLabel lblEstadoAeropuerto;
    
    // FUENTES (Estandarizadas)
    private final Font FONT_RELOJ = new Font("Consolas", Font.BOLD, 22);
    private final Font FONT_TITULO = new Font("Segoe UI", Font.BOLD, 24);
    private final Font FONT_SUBTITULO = new Font("Segoe UI", Font.BOLD, 16);
    private final Font FONT_LABEL_TABLA = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_VALOR_TABLA = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_BOTON = new Font("Segoe UI", Font.BOLD, 12);
    
    private String fuenteDatos = "";

	
	public JPanelClima() {
		this.horaActualInt = 0;
		this.generadorAleatorio = new Random();	
		this.historiaDia = new LinkedList<>();
		
		generarDatosDiaCompleto();
		
        setLayout(new BorderLayout(0, 0));
        setBackground(PaletaColor.get(PaletaColor.FONDO));
        
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(PaletaColor.get(PaletaColor.PRIMARIO));
        panelHeader.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        lblReloj = new JLabel("00:00:00");
        lblReloj.setPreferredSize(new Dimension(120, 0));
        lblReloj.setFont(new Font("Consolas", Font.BOLD, 18));
        lblReloj.setForeground(PaletaColor.get(PaletaColor.BLANCO));        
        panelHeader.add(lblReloj, BorderLayout.WEST);
        
        lblHeaderTitulo = new JLabel("METEOROLOGÍA" + fuenteDatos, JLabel.CENTER);
        lblHeaderTitulo.setFont(FONT_TITULO);
        lblHeaderTitulo.setForeground(Color.WHITE);
        panelHeader.add(lblHeaderTitulo, BorderLayout.CENTER);
        
        
        JLabel lblDummy = new JLabel("00:00:00");
        lblDummy.setFont(FONT_RELOJ);
        lblDummy.setForeground(new Color(0, 0, 0, 0)); 
        panelHeader.add(lblDummy, BorderLayout.EAST);
        
        add(panelHeader, BorderLayout.NORTH);
        
        JPanel panelContenido = new JPanel(new BorderLayout(15, 15));
        panelContenido.setBackground(PaletaColor.get(PaletaColor.FONDO));
        panelContenido.setBorder(new EmptyBorder(15, 15, 15, 15));

		JPanel panelIzquierdo = new JPanel();
		panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
		panelIzquierdo.setBackground(PaletaColor.get(PaletaColor.FONDO));
		panelIzquierdo.setPreferredSize(new Dimension(320, 0));
		
        JPanel panelTablaContainer = new JPanel(new BorderLayout());
        panelTablaContainer.setBackground(Color.WHITE);
        panelTablaContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PaletaColor.get(PaletaColor.PRIMARIO), 1),
                new EmptyBorder(5, 5, 5, 5)
        ));
        
        lblTituloClima = new JLabel(String.format("DATOS HORA %02d:00", horaActualInt));
        lblTituloClima.setFont(FONT_SUBTITULO);
        lblTituloClima.setForeground(PaletaColor.get(PaletaColor.PRIMARIO));
        lblTituloClima.setHorizontalAlignment(SwingConstants.CENTER);
        lblTituloClima.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        
        // La tabla de datos interna
        JPanel tablaDatos = new JPanel(new GridLayout(7, 2, 1, 1));
        tablaDatos.setBackground(PaletaColor.get(PaletaColor.FONDO));
        tablaDatos.setBorder(BorderFactory.createLineBorder(PaletaColor.get(PaletaColor.FONDO)));
        
        valTemp = crearLabelValor();
        valViento = crearLabelValor();
        valLluvia = crearLabelValor();
        valNieve = crearLabelValor();
        valNiebla = crearLabelValor();
        valNubes = crearLabelValor();
        valPresion = crearLabelValor();

        agregarFilaEstilizada(tablaDatos, "Temperatura (°C)", valTemp);
        agregarFilaEstilizada(tablaDatos, "Viento (km/h)", valViento);
        agregarFilaEstilizada(tablaDatos, "Precipitación (mm)", valLluvia);
        agregarFilaEstilizada(tablaDatos, "Nieve (cm)", valNieve);
        agregarFilaEstilizada(tablaDatos, "Visibilidad (km)", valNiebla);
        agregarFilaEstilizada(tablaDatos, "Nubes (m)", valNubes);
        agregarFilaEstilizada(tablaDatos, "Presión (hPa)", valPresion);
        
        panelTablaContainer.add(lblTituloClima, BorderLayout.NORTH);
        panelTablaContainer.add(tablaDatos, BorderLayout.CENTER);
        
        panelIzquierdo.add(panelTablaContainer);
        panelIzquierdo.add(Box.createVerticalStrut(20));
        
        
        panelBrujula = new PanelBrujula();
        panelBrujula.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        
        JPanel panelBrujulaWrapper = new JPanel(new BorderLayout());
        panelBrujulaWrapper.setBackground(PaletaColor.get(PaletaColor.FONDO));
        
        JLabel lblTituloBrujula = new JLabel("DIRECCIÓN Y VIENTO");
        lblTituloBrujula.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloBrujula.setForeground(Color.DARK_GRAY);
        lblTituloBrujula.setHorizontalAlignment(SwingConstants.CENTER);
        lblTituloBrujula.setBorder(new EmptyBorder(0,0,5,0));
        
        lblVelocidadViento = new JLabel("0 km/h");
        lblVelocidadViento.setFont(new Font("Consolas", Font.BOLD, 20));
        lblVelocidadViento.setForeground(PaletaColor.get(PaletaColor.PRIMARIO));
        lblVelocidadViento.setHorizontalAlignment(SwingConstants.CENTER);
        lblVelocidadViento.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        panelBrujulaWrapper.add(lblTituloBrujula, BorderLayout.NORTH);
        panelBrujulaWrapper.add(panelBrujula, BorderLayout.CENTER);
        panelBrujulaWrapper.add(lblVelocidadViento, BorderLayout.SOUTH);

        panelIzquierdo.add(panelBrujulaWrapper);
        panelIzquierdo.add(Box.createVerticalStrut(20));
             
        lblEstadoAeropuerto = new JLabel("AEROPUERTO OPERATIVO");
        lblEstadoAeropuerto.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblEstadoAeropuerto.setForeground(Color.WHITE);
        lblEstadoAeropuerto.setBackground(new Color(34, 139, 34)); // Verde Forest
        lblEstadoAeropuerto.setOpaque(true); // Necesario para ver el color de fondo
        lblEstadoAeropuerto.setHorizontalAlignment(SwingConstants.CENTER);
        lblEstadoAeropuerto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
                new EmptyBorder(10, 5, 10, 5) // Padding interno
        ));
        lblEstadoAeropuerto.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panelIzquierdo.add(lblEstadoAeropuerto);
        panelIzquierdo.add(Box.createVerticalGlue());
        
        
        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 0, 0));
        panelBotones.setOpaque(false);
        
        btnTabTemperatura = new JToggleButton("TEMPERATURA");
        btnTabPrecipitacion = new JToggleButton("PRECIPITACIÓN");
        
        estilizarBotonToggle(btnTabTemperatura);
        estilizarBotonToggle(btnTabPrecipitacion);
        
        grupoPestanas = new ButtonGroup();
        grupoPestanas.add(btnTabTemperatura);
        grupoPestanas.add(btnTabPrecipitacion);
        
        btnTabTemperatura.setSelected(true);	// Por defecto temp seleccionada
        
        panelBotones.add(btnTabTemperatura);
        panelBotones.add(btnTabPrecipitacion);
        
        cardLayout = new CardLayout();
        panelContenidoGraficos = new JPanel(cardLayout);
        panelContenidoGraficos.setBackground(Color.WHITE);
        // Borde fino gris para cerrar el contenido
        panelContenidoGraficos.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        graficoTemperatura = new GraficoTemperatura();
        graficoPrecipitacion = new GraficoPrecipitacion();
        
        panelContenidoGraficos.add(graficoTemperatura, "TEMP");
        panelContenidoGraficos.add(graficoPrecipitacion, "PRECIP");
        
        // Listeners para cambiar de panel
        btnTabTemperatura.addActionListener(e -> cardLayout.show(panelContenidoGraficos, "TEMP"));
        btnTabPrecipitacion.addActionListener(e -> cardLayout.show(panelContenidoGraficos, "PRECIP"));
        
        // Usamos un panel transparente para apilar botones + gráficos
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setOpaque(false);
        panelDerecho.setBorder(new EmptyBorder(0, 10, 0, 0)); // Separación con la izq
        
        JPanel panelGraficosWrapper = new JPanel(new BorderLayout());
        panelGraficosWrapper.setBackground(PaletaColor.get(PaletaColor.FONDO));
        
        TitledBorder borderGraficos = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PaletaColor.get(PaletaColor.PRIMARIO)), "Pronóstico");
        borderGraficos.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        borderGraficos.setTitleColor(PaletaColor.get(PaletaColor.PRIMARIO));
        
        panelGraficosWrapper.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 0, 0), // Margen externo
                borderGraficos
        ));
        
        // Panel interno que lleva los botones y el cardlayout
        JPanel panelInterno = new JPanel(new BorderLayout());
        panelInterno.setOpaque(false);
        panelInterno.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        panelInterno.add(panelBotones, BorderLayout.NORTH);
        panelInterno.add(panelContenidoGraficos, BorderLayout.CENTER);
        
        panelGraficosWrapper.add(panelInterno, BorderLayout.CENTER);
        
        panelDerecho.add(panelGraficosWrapper, BorderLayout.CENTER);
        
        panelContenido.add(panelIzquierdo, BorderLayout.WEST);
        panelContenido.add(panelDerecho, BorderLayout.CENTER);
        
        add(panelContenido, BorderLayout.CENTER);
        
        graficoTemperatura.setHoverListener(new GraficoTemperatura.OnHoverListener() {
            @Override
            public void onPuntoHover(Clima climaHovered, int hora) {
                // Actualiza la tabla lateral temporalmente al pasar el ratón
                actualizarDatosUI(climaHovered, hora);
            }
            @Override
            public void onPuntoExit() {
                // Al sacar el ratón, VOLVEMOS a la hora actual
                actualizarDatosUI(historiaDia.get(horaActualInt), horaActualInt); 
            }
        });
        	
        actualizarDatosUI(historiaDia.get(horaActualInt), horaActualInt);
        actualizarGraficos();
        RelojGlobal.getInstancia().addObservador(this);

        // --- AÑADE ESTO AL FINAL ---
        this.revalidate();
        this.repaint();
        
        // DEBUG: Ver si realmente hay datos en memoria
        System.out.println("DEBUG CLIMA: Dato hora 0 -> Temp: " + historiaDia.get(0).getTemperatura());
	}
	
	private void estilizarBotonToggle(JToggleButton boton) {
        boton.setFont(FONT_BOTON);
        boton.setPreferredSize(new Dimension(150, 35)); // Un poco más anchos para que quepa el texto
        boton.setFocusPainted(false);
        boton.setBorderPainted(false); 
        boton.setContentAreaFilled(true);
        boton.setOpaque(true);

        Color bgNormal = PaletaColor.get(PaletaColor.FILA_ALT);
        Color bgHover = new Color(230, 240, 250);
        Color bgSelected = Color.WHITE; // El seleccionado se funde con el panel blanco de abajo
        
        Color fgNormal = Color.GRAY;
        Color fgSelected = PaletaColor.get(PaletaColor.PRIMARIO);

        boton.setBackground(bgNormal); 
        boton.setForeground(fgNormal);
        
        // Borde inferior para simular conexión o separación
        boton.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, new Color(220, 220, 220)));

        boton.addItemListener(e -> {
            if (boton.isSelected()) {
                boton.setBackground(bgSelected);
                boton.setForeground(fgSelected);
                // Si está seleccionado, parece que está "delante"
                boton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 1, 0, 1, new Color(220, 220, 220)),
                    BorderFactory.createMatteBorder(2, 0, 0, 0, fgSelected) // Línea de color arriba
                ));
            } else {
                boton.setBackground(bgNormal);
                boton.setForeground(fgNormal);
                boton.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
            }
        });

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!boton.isSelected()) { 
                    boton.setBackground(bgHover);
                    boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!boton.isSelected()) {
                    boton.setBackground(bgNormal);
                }
                boton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }
	
	private void generarDatosDiaCompleto() {
        historiaDia.clear();
        GestorBD gestor = new GestorBD();
        ServicioMeteorologico servicio = new ServicioMeteorologico();
        
        System.out.println("Intentando obtener datos meteorológicos reales...");
        
        // Esta línea daba error si faltaba el import de java.util.List
        List<Clima> datosReales = servicio.obtenerPronosticoReal();
        
        if (datosReales != null && !datosReales.isEmpty()) {
            System.out.println("¡Datos reales obtenidos! Actualizando Base de Datos...");
            fuenteDatos = " (EN VIVO - OPEN METEO)";
            
            // Borramos (simulado) datos viejos sobrescribiendo
            for (int h = 0; h < datosReales.size() && h < 24; h++) {
                gestor.insertClima(h, datosReales.get(h));
            }
            // LinkedList soporta addAll de una List normal
            historiaDia.addAll(datosReales);
            
        } else {
            System.out.println("No se pudo conectar a la API (o error). Buscando en Base de Datos local...");
            
            if (gestor.existeDatosClima()) {
                // Esto devuelve LinkedList, así que asignamos directo
                historiaDia = gestor.loadClimaDiario();
                fuenteDatos = " (HISTÓRICO - BASE DE DATOS)";
            } else {
                System.out.println("BD vacía. Generando simulación aleatoria de respaldo.");
                fuenteDatos = " (SIMULACIÓN)";
                for (int h = 0; h < 24; h++) {
                    Clima c = generarClimaAleatorio(h);
                    historiaDia.add(c);
                    gestor.insertClima(h, c);
                }
            }
        }
        
        // Relleno de seguridad por si la API devolvió menos de 24 horas
        while (historiaDia.size() < 24) {
            historiaDia.add(generarClimaAleatorio(historiaDia.size()));
        }
    }

	private void actualizarDatosUI(Clima c, int hora) {
        if (c == null) return;
        lblTituloClima.setText(String.format("DATOS HORA %02d:00", hora));
        
        valTemp.setText(String.format("%.1f", c.getTemperatura()));
        if (c.getTemperatura() > 30) valTemp.setForeground(Color.RED);
        else if (c.getTemperatura() < 0) valTemp.setForeground(Color.BLUE);
        else valTemp.setForeground(Color.BLACK);
        
        valViento.setText(String.format("%.1f", c.getVelocidadViento()));
        valNiebla.setText(String.format("%.1f", c.getVisibilidadKm()));
        valNubes.setText(String.valueOf(c.getTechoNubesMetros()));
        valPresion.setText(String.format("%.1f", c.getPresionHPa()));
        
        if (c instanceof ClimaNevado) {
            valLluvia.setText("0.0");
            valNieve.setText(String.format("%.1f", c.getPrecipitacion()));
        } else {
            valLluvia.setText(String.format("%.1f", c.getPrecipitacion()));
            valNieve.setText("0.0");
        }
        
        // Actualizar dirección en la brújula
        if (lblVelocidadViento != null) {
            lblVelocidadViento.setText(String.format("%.1f km/h", c.getVelocidadViento()));
            
            // Si hay peligro por viento (>80), ponemos el texto rojo
            if (c.getVelocidadViento() > 80.0) {
                lblVelocidadViento.setForeground(Color.RED);
                lblVelocidadViento.setText(String.format("%.1f ¡PELIGRO!", c.getVelocidadViento()));
            } else {
                lblVelocidadViento.setForeground(PaletaColor.get(PaletaColor.PRIMARIO));
            }
        }
        
        if (panelBrujula != null) {
            panelBrujula.setDireccion(c.getDireccionViento());
        }
        
        // Comprobar señal de peligro general (Visibilidad, Nubes, Viento, Tormenta)
        if (c.isSenalPeligro()) {
            lblEstadoAeropuerto.setBackground(new Color(220, 53, 69)); // Rojo Alerta
            
            // Determinamos la causa brevemente
            String causa = "PELIGRO";
            if (c.getVelocidadViento() > 80.0) causa = "VIENTO FUERTE";
            else if (c.getVisibilidadKm() < 1.0) causa = "VISIBILIDAD NULA";
            else if (c.getTechoNubesMetros() < 300) causa = "TECHO NUBES BAJO";
            else if (c instanceof ClimaLluvioso && ((ClimaLluvioso)c).isTormentaElectrica()) causa = "TORMENTA ELÉC.";
            else if (c instanceof ClimaNevado && ((ClimaNevado)c).getAcumulacionNieveCm() > 0.5) causa = "NIEVE EN PISTA";
            
            lblEstadoAeropuerto.setText("¡" + causa + "!");
        } else {
            lblEstadoAeropuerto.setBackground(new Color(34, 139, 34)); // Verde Forest
            lblEstadoAeropuerto.setText("AEROPUERTO OPERATIVO");
        }
	}
	private void actualizarGraficos() {
        // Pasamos la lista COMPLETA y FIJA de 24h + la hora actual para la línea roja
        if (graficoTemperatura != null) graficoTemperatura.setDatos(historiaDia, horaActualInt);
        if (graficoPrecipitacion != null) graficoPrecipitacion.setDatos(historiaDia, horaActualInt);
    }
	
	@Override
	public void actualizarTiempo(LocalDateTime nuevoTiempo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        lblReloj.setText(nuevoTiempo.format(formatter));
        
        int h = nuevoTiempo.getHour();
        // Solo llamamos a avanzarHora si la hora ha cambiado
        if (h != this.horaActualInt) {
            avanzarHora(h);
        }
    }
	
	private void avanzarHora(int nuevaHora) {
        if (nuevaHora < this.horaActualInt) {
            generarDatosDiaCompleto();
        }
        
        this.horaActualInt = nuevaHora;
        
        // Actualizamos UI con los datos que YA tenemos generados para esta hora
        actualizarDatosUI(historiaDia.get(horaActualInt), horaActualInt);
        actualizarGraficos();
    }
	
	private JLabel crearLabelValor() {
        JLabel l = new JLabel("-");
        l.setFont(FONT_VALOR_TABLA); // Segoe UI Plain 14
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setBackground(Color.WHITE);
        l.setOpaque(true);
        return l;
    }
    
    private void agregarFilaEstilizada(JPanel panel, String titulo, JLabel labelValor) {
        JLabel lblTitulo = new JLabel(" " + titulo);
        lblTitulo.setFont(FONT_LABEL_TABLA); // Segoe UI Bold 13
        lblTitulo.setBackground(PaletaColor.get(PaletaColor.PRIMARIO)); 
        lblTitulo.setForeground(Color.WHITE); 
        lblTitulo.setOpaque(true);
        panel.add(lblTitulo);
        panel.add(labelValor);
    }
	
    
    
    
    private double round(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
    
    
    // Este método genera un objeto Clima por cada hora
    private Clima generarClimaAleatorio(int hora) {
        int tipoClima = generadorAleatorio.nextInt(4);
        boolean esDeNoche = (hora > 20 || hora < 6);
        double tempBase = 20.0; 
        if (esDeNoche) tempBase -= 5; else if (hora >= 12 && hora <= 16) tempBase += 5;
        double temp = tempBase - 5 + (10 * generadorAleatorio.nextDouble());
        
        // Viento variable (A veces peligroso)
        double viento = (generadorAleatorio.nextDouble() < 0.15) ? 75 + generadorAleatorio.nextDouble()*30 : 5 + generadorAleatorio.nextDouble()*60;
        double dir = generadorAleatorio.nextDouble() * 360;
        
        Clima c = null;
        switch(tipoClima) {
            case 0: // Despejado
                c = new ClimaDespejado(round(temp), round(viento), 
                        10 + generadorAleatorio.nextDouble()*20, // Visibilidad variable
                        40 + generadorAleatorio.nextDouble()*40, // Humedad variable
                        1013 + generadorAleatorio.nextDouble()*10, IntensidadSol.ALTA); 
                break;
            case 1: // Lluvioso
            {
                double precip = 1 + generadorAleatorio.nextDouble() * 30;
                double visi = (precip > 20) ? 0.5 + generadorAleatorio.nextDouble() : 5.0 + generadorAleatorio.nextDouble()*5;
                int nubes = 200 + generadorAleatorio.nextInt(1500); // Nubes pueden ser bajas
                boolean tormenta = generadorAleatorio.nextDouble() < 0.25;
                c = new ClimaLluvioso(round(temp), round(viento), round(visi), round(precip), nubes, 80, 80, 1000, tormenta);
                break;
            }
            case 2: // Nublado
            {
                int nubes = 250 + generadorAleatorio.nextInt(2500); // Rango muy amplio
                double visi = (nubes < 400) ? 0.8 + generadorAleatorio.nextDouble()*3 : 10.0;
                c = new ClimaNublado(round(temp), round(viento), round(visi), nubes, 20, 60, 1010); 
                break;
            }
            default: // Nevado
            {
                double precip = 1 + generadorAleatorio.nextDouble() * 15;
                double visi = 0.3 + generadorAleatorio.nextDouble() * 4.0;
                int nubes = 200 + generadorAleatorio.nextInt(1000);
                double acum = 0.1 + generadorAleatorio.nextDouble() * 1.5; 
                c = new ClimaNevado(round(temp), round(viento), round(visi), round(precip), nubes, 90, 80, 1000, round(acum));
                break;
            }
        }
        if(c != null) c.setDireccionViento(dir);
        return c;
    }
}