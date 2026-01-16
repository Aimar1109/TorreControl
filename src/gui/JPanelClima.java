package gui;

import domain.Clima;
import domain.IntensidadSol;
import domain.Clima.ClimaDespejado;
import domain.Clima.ClimaLluvioso;
import domain.Clima.ClimaNevado;
import domain.Clima.ClimaNublado;
import domain.PaletaColor;
import domain.ServicioMeteorologico; 
import domain.AnalisisDatos;       
import jdbc.GestorBD;              

import threads.ObservadorTiempo;
import threads.RelojGlobal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class JPanelClima extends JPanel implements ObservadorTiempo {

    private static final long serialVersionUID = 1L;
    
    private JLabel lblReloj;
    private JLabel lblHeaderTitulo;
    private GraficoTemperatura graficoTemperatura;
    private GraficoPrecipitacion graficoPrecipitacion;
    
    private JToggleButton btnTabTemperatura;
    private JToggleButton btnTabPrecipitacion;
    private JButton btnInforme; 
    private JButton btnTabla;
    
    private ButtonGroup grupoPestanas;
    private JPanel panelContenidoGraficos; 
    private CardLayout cardLayout;

    private JLabel lblTituloClima;
    private JLabel valTemp, valViento, valLluvia, valNieve, valNiebla, valNubes, valPresion;
    private PanelBrujula panelBrujula;
    private JLabel lblVelocidadViento;
    private JLabel lblEstadoAeropuerto;

    private int horaActualInt;
    private Random generadorAleatorio;
    private LinkedList<Clima> historiaDia;
    private String fuenteDatos = ""; 
    
    private final Font FONT_RELOJ = new Font("Consolas", Font.BOLD, 22);
    private final Font FONT_TITULO = new Font("Segoe UI", Font.BOLD, 24);
    private final Font FONT_SUBTITULO = new Font("Segoe UI", Font.BOLD, 16);
    private final Font FONT_LABEL_TABLA = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_VALOR_TABLA = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_BOTON = new Font("Segoe UI", Font.BOLD, 12);

    public JPanelClima() {
        this.horaActualInt = 0;
        this.generadorAleatorio = new Random(); 
        this.historiaDia = new LinkedList<>();
        
        generarDatosDiaCompleto();
        
        setLayout(new BorderLayout(0, 0));
        setBackground(PaletaColor.get(PaletaColor.FONDO));
        
        // --- HEADER ---
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
        lblEstadoAeropuerto.setBackground(new Color(34, 139, 34)); 
        lblEstadoAeropuerto.setOpaque(true);
        lblEstadoAeropuerto.setHorizontalAlignment(SwingConstants.CENTER);
        lblEstadoAeropuerto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
                new EmptyBorder(10, 5, 10, 5)
        ));
        lblEstadoAeropuerto.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panelIzquierdo.add(lblEstadoAeropuerto);
        panelIzquierdo.add(Box.createVerticalGlue());
        
        
        JPanel panelBotones = new JPanel(new GridLayout(1, 4, 5, 0));
        panelBotones.setOpaque(false);
        
        btnTabTemperatura = new JToggleButton("TEMPERATURA");
        btnTabPrecipitacion = new JToggleButton("PRECIPITACIÓN");
        
        estilizarBotonToggle(btnTabTemperatura);
        estilizarBotonToggle(btnTabPrecipitacion);
        
        btnInforme = new JButton("INFORME");
        estilizarBotonNormal(btnInforme, PaletaColor.SECUNDARIO);
        
        btnTabla = new JButton("VER DETALLE");
        estilizarBotonNormal(btnTabla, PaletaColor.SECUNDARIO);
        
        
        btnInforme.addActionListener(e -> {
            String reporte = AnalisisDatos.generarInformeTexto(historiaDia);
            JOptionPane.showMessageDialog(this, reporte, "Análisis Estadístico Diario", JOptionPane.INFORMATION_MESSAGE);
        });
        
        btnTabla.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            DialogoDetalleClima dialogo = new DialogoDetalleClima(parentFrame, historiaDia);
            dialogo.setVisible(true);
        });
        
        grupoPestanas = new ButtonGroup();
        grupoPestanas.add(btnTabTemperatura);
        grupoPestanas.add(btnTabPrecipitacion);
        
        btnTabTemperatura.setSelected(true);
        
        panelBotones.add(btnTabTemperatura);
        panelBotones.add(btnTabPrecipitacion);
        panelBotones.add(btnInforme);
        panelBotones.add(btnTabla);
        
        cardLayout = new CardLayout();
        panelContenidoGraficos = new JPanel(cardLayout);
        panelContenidoGraficos.setBackground(Color.WHITE);
        panelContenidoGraficos.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        graficoTemperatura = new GraficoTemperatura();
        graficoPrecipitacion = new GraficoPrecipitacion();
        
        panelContenidoGraficos.add(graficoTemperatura, "TEMP");
        panelContenidoGraficos.add(graficoPrecipitacion, "PRECIP");
        
        btnTabTemperatura.addActionListener(e -> cardLayout.show(panelContenidoGraficos, "TEMP"));
        btnTabPrecipitacion.addActionListener(e -> cardLayout.show(panelContenidoGraficos, "PRECIP"));
        
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setOpaque(false);
        panelDerecho.setBorder(new EmptyBorder(0, 10, 0, 0)); 
        
        JPanel panelGraficosWrapper = new JPanel(new BorderLayout());
        panelGraficosWrapper.setBackground(PaletaColor.get(PaletaColor.FONDO));
        
        TitledBorder borderGraficos = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PaletaColor.get(PaletaColor.PRIMARIO)), "Pronóstico");
        borderGraficos.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        borderGraficos.setTitleColor(PaletaColor.get(PaletaColor.PRIMARIO));
        
        panelGraficosWrapper.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 0, 0),
                borderGraficos
        ));
        
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
        
        // Interactividad Gráficos
        graficoTemperatura.setHoverListener(new GraficoTemperatura.OnHoverListener() {
            @Override
            public void onPuntoHover(Clima climaHovered, int hora) {
                actualizarDatosUI(climaHovered, hora);
            }
            @Override
            public void onPuntoExit() {
                actualizarDatosUI(historiaDia.get(horaActualInt), horaActualInt); 
            }
        });
        
        actualizarDatosUI(historiaDia.get(horaActualInt), horaActualInt);
        actualizarGraficos();
        RelojGlobal.getInstancia().addObservador(this);
        
        this.revalidate();
        this.repaint();
    }
    
    private void estilizarBotonToggle(JToggleButton boton) {
        boton.setFont(FONT_BOTON);
        boton.setPreferredSize(new Dimension(100, 35)); 
        boton.setFocusPainted(false);
        boton.setBorderPainted(false); 
        boton.setContentAreaFilled(true);
        boton.setOpaque(true);

        Color bgNormal = PaletaColor.get(PaletaColor.FILA_ALT);
        Color bgHover = new Color(230, 240, 250);
        
        Color fgNormal = Color.GRAY;
        Color fgSelected = PaletaColor.get(PaletaColor.PRIMARIO);

        boton.setBackground(bgNormal); 
        boton.setForeground(fgNormal);
        boton.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, new Color(220, 220, 220)));

        boton.addItemListener(e -> {
            if (boton.isSelected()) {
                boton.setBackground(Color.WHITE);
                boton.setForeground(fgSelected);
                boton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 1, 0, 1, new Color(220, 220, 220)),
                    BorderFactory.createMatteBorder(2, 0, 0, 0, fgSelected)
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
                if (!boton.isSelected()) boton.setBackground(bgNormal);
                boton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private void estilizarBotonNormal(JButton boton, PaletaColor colorEnum) {
        boton.setFont(FONT_BOTON);
        boton.setPreferredSize(new Dimension(100, 35)); 
        boton.setFocusPainted(false);
        boton.setBorderPainted(false); 
        boton.setContentAreaFilled(true);
        boton.setOpaque(true);

        Color bgNormal = PaletaColor.get(colorEnum); 
        Color bgHover = PaletaColor.get(PaletaColor.PRIMARIO);    
        Color fgTexto = PaletaColor.get(PaletaColor.BLANCO);      

        boton.setBackground(bgNormal); 
        boton.setForeground(fgTexto);
        boton.setBorder(BorderFactory.createLineBorder(bgHover, 1));

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(bgHover); 
                boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(bgNormal); 
                boton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }
    
    // Lógica de Datos
    private void generarDatosDiaCompleto() {
        historiaDia.clear();
        GestorBD gestor = new GestorBD();
        ServicioMeteorologico servicio = new ServicioMeteorologico();
        
        List<Clima> datosReales = servicio.obtenerPronosticoReal();
        
        if (datosReales != null && !datosReales.isEmpty()) {
            fuenteDatos = " (EN VIVO - OPEN METEO)";
            
            for (int h = 0; h < datosReales.size() && h < 24; h++) {
            	Clima c = datosReales.get(h);
                c.setHora(h);
                gestor.insertClima(h, c);
            }
            historiaDia.addAll(datosReales);
            
        } else {
            System.out.println("Fallo API. Buscando en Base de Datos local...");
            
            if (gestor.existeDatosClima()) {
                historiaDia = gestor.loadClimaDiario();
                fuenteDatos = " (HISTÓRICO - BASE DE DATOS)";
            } else {
                System.out.println("BD vacía. Generando simulación aleatoria.");
                fuenteDatos = " (SIMULACIÓN)";
                for (int h = 0; h < 24; h++) {
                	Clima c = generarClimaAleatorio(h);
                    c.setHora(h);
                    historiaDia.add(c);
                    gestor.insertClima(h, c);
                }
            }
        }
        
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
        
        if (lblVelocidadViento != null) {
            lblVelocidadViento.setText(String.format("%.1f km/h", c.getVelocidadViento()));
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
        
        if (c.isSenalPeligro()) {
            lblEstadoAeropuerto.setBackground(new Color(220, 53, 69));
            String causa = "PELIGRO";
            if (c.getVelocidadViento() > 80.0) causa = "VIENTO FUERTE";
            else if (c.getVisibilidadKm() < 1.0) causa = "VISIBILIDAD NULA";
            else if (c.getTechoNubesMetros() < 300) causa = "TECHO NUBES BAJO";
            else if (c instanceof ClimaLluvioso && ((ClimaLluvioso)c).isTormentaElectrica()) causa = "TORMENTA ELÉC.";
            else if (c instanceof ClimaNevado && ((ClimaNevado)c).getAcumulacionNieveCm() > 0.5) causa = "NIEVE EN PISTA";
            
            lblEstadoAeropuerto.setText("¡" + causa + "!");
        } else {
            lblEstadoAeropuerto.setBackground(new Color(34, 139, 34));
            lblEstadoAeropuerto.setText("AEROPUERTO OPERATIVO");
        }
    }
    
    private void actualizarGraficos() {
        if (graficoTemperatura != null) graficoTemperatura.setDatos(historiaDia, horaActualInt);
        if (graficoPrecipitacion != null) graficoPrecipitacion.setDatos(historiaDia, horaActualInt);
    }
    
    @Override
    public void actualizarTiempo(LocalDateTime nuevoTiempo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        lblReloj.setText(nuevoTiempo.format(formatter));
        
        int h = nuevoTiempo.getHour();
        if (h != this.horaActualInt) {
            avanzarHora(h);
        }
    }
    
    private void avanzarHora(int nuevaHora) {
        if (nuevaHora < this.horaActualInt) {
            generarDatosDiaCompleto();
        }
        this.horaActualInt = nuevaHora;
        actualizarDatosUI(historiaDia.get(horaActualInt), horaActualInt);
        actualizarGraficos();
    }
    
    private JLabel crearLabelValor() {
        JLabel l = new JLabel("-");
        l.setFont(FONT_VALOR_TABLA);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setBackground(Color.WHITE);
        l.setOpaque(true);
        return l;
    }
    
    private void agregarFilaEstilizada(JPanel panel, String titulo, JLabel labelValor) {
        JLabel lblTitulo = new JLabel(" " + titulo);
        lblTitulo.setFont(FONT_LABEL_TABLA);
        lblTitulo.setBackground(PaletaColor.get(PaletaColor.PRIMARIO)); 
        lblTitulo.setForeground(Color.WHITE); 
        lblTitulo.setOpaque(true);
        panel.add(lblTitulo);
        panel.add(labelValor);
    }
    
    private double round(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
    
    private Clima generarClimaAleatorio(int hora) {
        int tipoClima = generadorAleatorio.nextInt(4);
        boolean esDeNoche = (hora > 20 || hora < 6);
        double tempBase = 20.0; 
        if (esDeNoche) tempBase -= 5; else if (hora >= 12 && hora <= 16) tempBase += 5;
        double temp = tempBase - 5 + (10 * generadorAleatorio.nextDouble());
        double viento = (generadorAleatorio.nextDouble() < 0.15) ? 75 + generadorAleatorio.nextDouble()*30 : 5 + generadorAleatorio.nextDouble()*60;
        double dir = generadorAleatorio.nextDouble() * 360;
        
        Clima c = null;
        switch(tipoClima) {
            case 0: 
                c = new ClimaDespejado(round(temp), round(viento), 10 + generadorAleatorio.nextDouble()*20, 40 + generadorAleatorio.nextDouble()*40, 1013 + generadorAleatorio.nextDouble()*10, IntensidadSol.ALTA); 
                break;
            case 1: 
                double precip = 1 + generadorAleatorio.nextDouble() * 30;
                double visi = (precip > 20) ? 0.5 + generadorAleatorio.nextDouble() : 5.0 + generadorAleatorio.nextDouble()*5;
                int nubes = 200 + generadorAleatorio.nextInt(1500);
                boolean tormenta = generadorAleatorio.nextDouble() < 0.25;
                c = new ClimaLluvioso(round(temp), round(viento), round(visi), round(precip), nubes, 80, 80, 1000, tormenta);
                break;
            case 2: 
                int nubes2 = 250 + generadorAleatorio.nextInt(2500);
                double visi2 = (nubes2 < 400) ? 0.8 + generadorAleatorio.nextDouble()*3 : 10.0;
                c = new ClimaNublado(round(temp), round(viento), round(visi2), nubes2, 20, 60, 1010); 
                break;
            default: 
                double precip2 = 1 + generadorAleatorio.nextDouble() * 15;
                double visi3 = 0.3 + generadorAleatorio.nextDouble() * 4.0;
                int nubes3 = 200 + generadorAleatorio.nextInt(1000);
                double acum = 0.1 + generadorAleatorio.nextDouble() * 1.5; 
                c = new ClimaNevado(round(temp), round(viento), round(visi3), round(precip2), nubes3, 90, 80, 1000, round(acum));
                break;
        }
        if(c != null) c.setDireccionViento(dir);
        return c;
    }
}