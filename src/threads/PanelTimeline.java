package threads;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import domain.Vuelo;

// AHORA IMPLEMENTA TU INTERFAZ ObservadorTiempo
public class PanelTimeline extends JPanel implements ObservadorTiempo {

    private static final long serialVersionUID = 1L;
    
    private JPanel contenedorTarjetas;
    private List<VueloCard> tarjetas;

    public PanelTimeline(ArrayList<Vuelo> vuelos) {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245)); 

        // Ordenar vuelos
        Collections.sort(vuelos, new Comparator<Vuelo>() {
            @Override
            public int compare(Vuelo v1, Vuelo v2) {
                LocalDateTime salida1 = v1.getFechaHoraProgramada().plusMinutes(v1.getDelayed());
                LocalDateTime salida2 = v2.getFechaHoraProgramada().plusMinutes(v2.getDelayed());
                return salida1.compareTo(salida2);
            }
        });

        tarjetas = new ArrayList<>();
        contenedorTarjetas = new JPanel();
        contenedorTarjetas.setLayout(new BoxLayout(contenedorTarjetas, BoxLayout.Y_AXIS));
        contenedorTarjetas.setBackground(new Color(240, 242, 245));
        contenedorTarjetas.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Crear tarjetas
        int totalVuelos = vuelos.size();
        for (int i = 0; i < totalVuelos; i++) {
            VueloCard card = new VueloCard(vuelos.get(i), i == 0, i == totalVuelos - 1);
            contenedorTarjetas.add(card);
            contenedorTarjetas.add(Box.createRigidArea(new Dimension(0, 15))); 
            tarjetas.add(card);
        }

        if (totalVuelos > 0 && contenedorTarjetas.getComponentCount() > 0) {
            contenedorTarjetas.remove(contenedorTarjetas.getComponentCount() - 1);
        }

        JScrollPane scroll = new JScrollPane(contenedorTarjetas);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // --- INTEGRACIÓN CON RELOJ GLOBAL ---
        // Nos suscribimos como observadores. 
        // El reloj nos llamará automáticamente cuando cambie el tiempo.
        RelojGlobal.getInstancia().addObservador(this);
        
        // Actualización inicial inmediata para que no salga vacío al arrancar
        actualizarTiempo(RelojGlobal.getInstancia().getTiempoActual());
    }
    
    // --- IMPLEMENTACIÓN DE ObservadorTiempo ---
    @Override
    public void actualizarTiempo(LocalDateTime nuevoTiempo) {
        // Importante: El reloj corre en un hilo separado, pero Swing debe
        // actualizarse en el Event Dispatch Thread (EDT).
        SwingUtilities.invokeLater(() -> {
            for (VueloCard card : tarjetas) {
                card.actualizar(nuevoTiempo);
            }
        });
    }

    @Override
    public void cambioEstadoPausa(boolean pausa) {
        // Opcional: Podrías cambiar el color de fondo o mostrar un icono 
        // si el reloj está pausado.
    }

    // Método para limpiar la suscripción al cerrar el panel
    public void detener() {
        RelojGlobal.getInstancia().eliminarObservador(this);
    }
}

// -------------------------------------------------------------------------
// CLASE VISUAL: Nodo de la línea de tiempo (Línea vertical + Punto)
// -------------------------------------------------------------------------
class TimelineNodePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private boolean isFirst;
    private boolean isLast;

    public TimelineNodePanel(boolean isFirst, boolean isLast) {
        this.isFirst = isFirst;
        this.isLast = isLast;
        setOpaque(false);
        setPreferredSize(new Dimension(40, 70)); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        
        Color lineColor = new Color(200, 200, 200);
        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(2));

        if (!isFirst) {
            g2.drawLine(centerX, 0, centerX, centerY);
        }
        if (!isLast) {
            g2.drawLine(centerX, centerY, centerX, getHeight());
        }

        int nodeSize = 12;
        g2.setColor(Color.WHITE);
        g2.fillOval(centerX - nodeSize / 2, centerY - nodeSize / 2, nodeSize, nodeSize);
        
        g2.setColor(new Color(100, 100, 100));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(centerX - nodeSize / 2, centerY - nodeSize / 2, nodeSize, nodeSize);
    }
}

// -------------------------------------------------------------------------
// CLASE VISUAL: Tarjeta de Vuelo
// -------------------------------------------------------------------------
class VueloCard extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private Vuelo vuelo;
    private BarraProgresoVuelo barraProgreso;
    private JLabel lblEstado;
    private JLabel lblHoras;
    
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public VueloCard(Vuelo vuelo, boolean isFirst, boolean isLast) {
        this.vuelo = vuelo;
        
        setLayout(new BorderLayout(0, 0)); 
        setBackground(Color.WHITE);
        
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(5, 5, 5, 15)
        ));
        
        setPreferredSize(new Dimension(0, 85)); 
        setMaximumSize(new Dimension(9999, 85)); 

        TimelineNodePanel nodePanel = new TimelineNodePanel(isFirst, isLast);
        add(nodePanel, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 5));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

        JPanel headerInfo = new JPanel(new BorderLayout());
        headerInfo.setOpaque(false);
        
        JLabel lblCodigo = new JLabel(vuelo.getCodigo());
        lblCodigo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCodigo.setForeground(new Color(44, 62, 80));
        
        JLabel lblRuta = new JLabel("  " + vuelo.getOrigen().getCodigo() + " ➝ " + vuelo.getDestino().getCodigo());
        lblRuta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRuta.setForeground(Color.GRAY);
        
        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.X_AXIS));
        titleContainer.setOpaque(false);
        titleContainer.add(lblCodigo);
        titleContainer.add(lblRuta);
        
        JPanel statusContainer = new JPanel(new GridLayout(2, 1));
        statusContainer.setOpaque(false);
        
        lblEstado = new JLabel("Pendiente", JLabel.RIGHT);
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        LocalDateTime salida = vuelo.getFechaHoraProgramada().plusMinutes(vuelo.getDelayed());
        LocalDateTime llegada = salida.plusMinutes((long)vuelo.getDuracion());
        lblHoras = new JLabel(salida.format(TIME_FMT) + " - " + llegada.format(TIME_FMT), JLabel.RIGHT);
        lblHoras.setFont(new Font("Consolas", Font.PLAIN, 12));
        lblHoras.setForeground(Color.DARK_GRAY);
        
        statusContainer.add(lblEstado);
        statusContainer.add(lblHoras);

        headerInfo.add(titleContainer, BorderLayout.WEST);
        headerInfo.add(statusContainer, BorderLayout.EAST);
        
        barraProgreso = new BarraProgresoVuelo(vuelo);
        barraProgreso.setPreferredSize(new Dimension(0, 30));

        centerPanel.add(headerInfo, BorderLayout.NORTH);
        centerPanel.add(barraProgreso, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void actualizar(LocalDateTime ahora) {
        barraProgreso.setTiempoActual(ahora);
        
        LocalDateTime salida = vuelo.getFechaHoraProgramada().plusMinutes(vuelo.getDelayed());
        LocalDateTime llegada = salida.plusMinutes((long)vuelo.getDuracion());

        if (ahora.isAfter(llegada)) {
            lblEstado.setText("ATERRIZADO");
            lblEstado.setForeground(new Color(127, 140, 141)); 
        } else if (ahora.isAfter(salida)) {
            lblEstado.setText("EN VUELO");
            lblEstado.setForeground(new Color(39, 174, 96)); 
        } else {
            if (vuelo.getDelayed() > 0) {
                lblEstado.setText("RETRASADO " + vuelo.getDelayed() + "m");
                lblEstado.setForeground(new Color(192, 57, 43)); 
            } else {
                lblEstado.setText("PROGRAMADO");
                lblEstado.setForeground(new Color(41, 128, 185)); 
            }
        }
        
        String textoHora = salida.format(TIME_FMT) + " - " + llegada.format(TIME_FMT);
        if (salida.getDayOfYear() != ahora.getDayOfYear()) {
            int diffDias = salida.getDayOfYear() - ahora.getDayOfYear();
            if (diffDias > 0) textoHora += " (+" + diffDias + "d)";
        }
        lblHoras.setText(textoHora);
    }
}

// -------------------------------------------------------------------------
// CLASE VISUAL: Barra de Progreso Personalizada
// -------------------------------------------------------------------------
class BarraProgresoVuelo extends JPanel {
    private static final long serialVersionUID = 1L;
    private Vuelo vuelo;
    private float porcentaje = 0f;
    
    private long minutosParaSalida = 0;
    private boolean haSalido = false;
    private boolean haLlegado = false;

    public BarraProgresoVuelo(Vuelo vuelo) {
        this.vuelo = vuelo;
        setOpaque(false);
    }

    public void setTiempoActual(LocalDateTime ahora) {
        LocalDateTime salida = vuelo.getFechaHoraProgramada().plusMinutes(vuelo.getDelayed());
        LocalDateTime llegada = salida.plusMinutes((long)vuelo.getDuracion());

        minutosParaSalida = ChronoUnit.MINUTES.between(ahora, salida);
        
        if (ahora.isAfter(llegada)) {
            haLlegado = true;
            haSalido = true;
            porcentaje = 1f;
        } else if (ahora.isAfter(salida)) {
            haLlegado = false;
            haSalido = true;
            long duracionTotal = ChronoUnit.MINUTES.between(salida, llegada);
            long tiempoVolado = ChronoUnit.MINUTES.between(salida, ahora);
            porcentaje = (float) tiempoVolado / (float) (duracionTotal > 0 ? duracionTotal : 1);
        } else {
            haLlegado = false;
            haSalido = false;
            porcentaje = 0f;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        
        int alturaBarra = 18; 
        int radioEsquina = 18;
        
        int yBarra = (h - alturaBarra) / 2;
        int margenLateral = 5;
        int anchoUtil = w - (margenLateral * 2);

        g2.setColor(new Color(236, 240, 241));
        g2.fillRoundRect(margenLateral, yBarra, anchoUtil, alturaBarra, radioEsquina, radioEsquina);

        if (haSalido && !haLlegado) {
            Color colorVuelo = (vuelo.getDelayed() > 0) ? new Color(231, 76, 60) : new Color(52, 152, 219);
            
            int wProgreso = (int) (anchoUtil * porcentaje);
            g2.setColor(colorVuelo);
            g2.fillRoundRect(margenLateral, yBarra, wProgreso, alturaBarra, radioEsquina, radioEsquina);
            
            int xAvion = margenLateral + wProgreso;
            dibujarAvion(g2, xAvion, h / 2, colorVuelo);
            
            if (wProgreso > 40) {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                String textoPct = (int)(porcentaje * 100) + "%";
                g2.drawString(textoPct, margenLateral + 8, yBarra + 13);
            }
        }
        else if (!haSalido) {
            String textoEstado;
            Color colorTexto;
            
            if (minutosParaSalida < 60 && minutosParaSalida > 0) {
                g2.setColor(new Color(243, 156, 18)); 
                float urgencia = 1.0f - (Math.max(0.0f, (float)minutosParaSalida / 60.0f));
                int wUrgencia = (int) (anchoUtil * urgencia);
                g2.fillRoundRect(margenLateral, yBarra, wUrgencia, alturaBarra, radioEsquina, radioEsquina);
                
                textoEstado = "EMBARCANDO | -" + minutosParaSalida + "m";
                colorTexto = Color.DARK_GRAY;
            } else {
                long horas = minutosParaSalida / 60;
                long mins = minutosParaSalida % 60;
                textoEstado = (minutosParaSalida < 0) ? "RETRASADO" : "Salida en " + horas + "h " + mins + "m";
                colorTexto = Color.GRAY;
            }
            
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(colorTexto);
            int textWidth = g2.getFontMetrics().stringWidth(textoEstado);
            g2.drawString(textoEstado, (w - textWidth) / 2, yBarra + 13);
        }
        else {
            g2.setColor(new Color(149, 165, 166)); 
            g2.fillRoundRect(margenLateral, yBarra, anchoUtil, alturaBarra, radioEsquina, radioEsquina);
            
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            String fin = "FINALIZADO";
            int textWidth = g2.getFontMetrics().stringWidth(fin);
            g2.drawString(fin, (w - textWidth) / 2, yBarra + 13);
        }
    }
    
    private void dibujarAvion(Graphics2D g2, int x, int y, Color color) {
        var t = g2.getTransform();
        g2.translate(x, y);
        
        int size = 18; 
        
        g2.setColor(Color.WHITE); 
        g2.fillOval(-size / 2, -size / 2, size, size);
        
        g2.setColor(color); 
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(-size / 2, -size / 2, size, size);

        g2.setColor(color.darker()); 
        int[] xp = {-4, 6, -4};
        int[] yp = {-5, 0, 5};
        g2.fillPolygon(xp, yp, 3);
        
        g2.setTransform(t);
    }
}