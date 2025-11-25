package threads;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import domain.PaletaColor; // Importamos tu Enum
import domain.Vuelo;

public class PanelTimeline extends JPanel implements ObservadorTiempo {

    private static final long serialVersionUID = 1L;
    
    private JPanel listPanel;
    private List<RadarTile> tiles;

    // --- COLORES ADAPTADOS A TU PALETA ---
    // Mantenemos fondo oscuro para el efecto HUD, pero armonizado
    public static final Color BG_DARK = PaletaColor.get(PaletaColor.PRIMARIO);
    public static final Color TILE_BG_START = new Color(50, 55, 60);
    public static final Color TILE_BG_END = new Color(40, 45, 50);
    
    // Colores funcionales mapeados a tu Enum PaletaColor
    public static final Color COLOR_BLUE = PaletaColor.get(PaletaColor.SECUNDARIO); // Azul Oscuro/Brillante
    public static final Color COLOR_GREEN = PaletaColor.get(PaletaColor.EXITO);     // Verde Éxito
    public static final Color COLOR_RED = PaletaColor.get(PaletaColor.DELAYED);     // Rojo Retraso
    public static final Color COLOR_AMBER = PaletaColor.get(PaletaColor.ACENTO);    // Naranja Acento
    
    public static final Color TEXT_DIM = PaletaColor.get(PaletaColor.TEXTO_SUAVE); // Gris medio

    public PanelTimeline(ArrayList<Vuelo> vuelos) {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        // Ordenar: Primero los que están volando, luego programados, luego aterrizados
        Collections.sort(vuelos, (v1, v2) -> {
            LocalDateTime now = RelojGlobal.getInstancia().getTiempoActual();
            int score1 = getFlightScore(v1, now);
            int score2 = getFlightScore(v2, now);
            return Integer.compare(score1, score2);
        });

        // Usamos BoxLayout en eje Y para que las filas ocupen todo el ancho
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(BG_DARK);
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tiles = new ArrayList<>();
        
        for (Vuelo v : vuelos) {
            RadarTile tile = new RadarTile(v);
            listPanel.add(tile);
            listPanel.add(Box.createRigidArea(new Dimension(0, 8))); // Espacio entre barras
            tiles.add(tile);
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(BG_DARK);
        add(scroll, BorderLayout.CENTER);

        RelojGlobal.getInstancia().addObservador(this);
    }
    
    private int getFlightScore(Vuelo v, LocalDateTime now) {
        LocalDateTime dep = v.getFechaHoraProgramada().plusMinutes(v.getDelayed());
        LocalDateTime arr = dep.plusMinutes((long)v.getDuracion());
        
        if (now.isAfter(dep) && now.isBefore(arr)) return 1; // Volando
        if (now.isBefore(dep) && ChronoUnit.MINUTES.between(now, dep) < 60) return 2; // Embarcando
        if (now.isBefore(dep)) return 3; // Futuro
        return 4; // Aterrizado
    }

    @Override
    public void actualizarTiempo(LocalDateTime nuevoTiempo) {
        SwingUtilities.invokeLater(() -> {
            for (RadarTile tile : tiles) {
                tile.actualizarEstado(nuevoTiempo);
            }
            listPanel.repaint();
        });
    }

    @Override
    public void cambioEstadoPausa(boolean pausa) {
        repaint();
    }
    
    public void detener() {
        RelojGlobal.getInstancia().eliminarObservador(this);
    }
}

/**
 * Tarjeta estilo HUD Expandible (Responsive).
 * Se dibuja como una barra horizontal completa.
 */
class RadarTile extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private Vuelo vuelo;
    private float progreso = 0f;
    private Color estadoColor = Color.GRAY;
    private String estadoTexto = "N/A";
    
    private float pulseAlpha = 0f;
    private boolean isFlying = false;

    // Fuentes cacheadas
    private static final Font FONT_CODE = new Font("Consolas", Font.BOLD, 22);
    private static final Font FONT_ROUTE = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_DETAILS = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_STATUS = new Font("Consolas", Font.BOLD, 14);

    public RadarTile(Vuelo vuelo) {
        this.vuelo = vuelo;
        setOpaque(false);
        // Altura fija, Ancho expandible
        setMinimumSize(new Dimension(400, 85));
        setPreferredSize(new Dimension(600, 85));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
    }

    public void actualizarEstado(LocalDateTime now) {
        LocalDateTime salida = vuelo.getFechaHoraProgramada().plusMinutes(vuelo.getDelayed());
        LocalDateTime llegada = salida.plusMinutes((long)vuelo.getDuracion());
        
        long totalMin = ChronoUnit.MINUTES.between(salida, llegada);
        long elapsed = ChronoUnit.MINUTES.between(salida, now);
        long toGo = ChronoUnit.MINUTES.between(now, salida);

        isFlying = false;

        if (now.isAfter(llegada)) {
            progreso = 1f;
            estadoColor = PaletaColor.get(PaletaColor.TEXTO_SUAVE); // Gris apagado
            estadoTexto = "FINALIZADO";
        } else if (now.isAfter(salida)) {
            isFlying = true;
            progreso = (float)elapsed / (float)Math.max(totalMin, 1);
            // Si tiene retraso, usamos ROJO, si no, VERDE (EXITO)
            estadoColor = vuelo.getDelayed() > 0 ? PanelTimeline.COLOR_RED : PanelTimeline.COLOR_GREEN;
            estadoTexto = "EN VUELO";
        } else {
            progreso = 0f;
            
            // 1. Comprobar PRIMERO si hay retraso
            if (vuelo.getDelayed() > 0) {
                estadoColor = PanelTimeline.COLOR_RED; // Rojo (DELAYED)
                estadoTexto = "RETRASADO";            
            } 
            // 2. Si no, comprobar si está embarcando (< 60 min)
            else if (toGo < 60) {
                estadoColor = PanelTimeline.COLOR_AMBER; // Naranja (ACENTO)
                estadoTexto = "EMBARCANDO T-" + toGo + "m";
            } 
            // 3. Si no, está programado normal
            else {
                estadoColor = PanelTimeline.COLOR_BLUE; // Azul (SECUNDARIO)
                estadoTexto = "PROGRAMADO";
            }
        }
        
        if (isFlying) {
            long millis = System.currentTimeMillis();
            pulseAlpha = (float) (0.3f + 0.2f * Math.sin(millis / 250.0)); 
        } else {
            pulseAlpha = 0f;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. FONDO CON GRADIENTE (Estilo Barra Metálica Oscura)
        GradientPaint bgGrad = new GradientPaint(0, 0, PanelTimeline.TILE_BG_START, 0, h, PanelTimeline.TILE_BG_END);
        g2.setPaint(bgGrad);
        g2.fillRoundRect(0, 0, w, h, 10, 10);
        
        // Borde izquierdo coloreado según estado (Indicador visual rápido)
        g2.setColor(estadoColor);
        g2.fillRoundRect(0, 0, 6, h, 10, 10); // "Ribbon" izquierdo
        
        // Borde sutil general
        g2.setColor(new Color(60, 65, 70));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w-1, h-1, 10, 10);

        // === SECCIÓN 1: RADAR & CÓDIGO (Izquierda) ===
        int radarX = 25;
        int radarY = h/2;
        int radarR = 24;

        // Círculo Radar
        g2.setColor(new Color(20, 20, 20));
        g2.fillOval(radarX - radarR, radarY - radarR, radarR*2, radarR*2);
        g2.setColor(estadoColor.darker());
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(radarX - radarR, radarY - radarR, radarR*2, radarR*2);

        // Arco de progreso radial
        if (progreso > 0 && progreso < 1) {
            g2.setColor(estadoColor);
            g2.setStroke(new BasicStroke(3f));
            g2.draw(new Arc2D.Float(radarX - radarR + 4, radarY - radarR + 4, (radarR-4)*2, (radarR-4)*2, 90, -360 * progreso, Arc2D.OPEN));
        }
        
        // Efecto Pulse
        if (isFlying) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulseAlpha));
            g2.setColor(estadoColor);
            g2.fillOval(radarX - 8, radarY - 8, 16, 16);
            g2.setComposite(AlphaComposite.SrcOver);
        } else if (estadoTexto.startsWith("EMBARCANDO")) {
            g2.setColor(PanelTimeline.COLOR_AMBER);
            g2.fillOval(radarX - 5, radarY - 5, 10, 10);
        }

        // Código de Vuelo
        g2.setFont(FONT_CODE);
        g2.setColor(PaletaColor.get(PaletaColor.BLANCO));
        g2.drawString(vuelo.getCodigo(), radarX + 40, radarY + 8);

        // === SECCIÓN 2: RUTA (Centro-Izquierda) ===
        int routeX = radarX + 130;
        g2.setFont(FONT_ROUTE);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString(vuelo.getOrigen().getCodigo() + "  ➝  " + vuelo.getDestino().getCodigo(), routeX, radarY - 10);
        
        // Ciudad Origen - Destino (Pequeño)
        g2.setFont(FONT_DETAILS);
        g2.setColor(PanelTimeline.TEXT_DIM);
        String rutaFull = vuelo.getOrigen().getCiudad() + " - " + vuelo.getDestino().getCiudad();
        // Recortar texto si es muy largo
        if (rutaFull.length() > 30) rutaFull = rutaFull.substring(0, 27) + "...";
        g2.drawString(rutaFull, routeX, radarY + 10);

        // === SECCIÓN 3: DETALLES TÉCNICOS (Centro expandible) ===
        // Calculamos posición relativa al ancho para que se expanda
        int detailsX = w / 2 + 20; 
        
        // Iconos simulados con texto
        drawDetail(g2, "AVIÓN", vuelo.getAvion().getModelo(), detailsX, radarY - 12);
        drawDetail(g2, "PAX", vuelo.getPasajeros().size() + "/" + vuelo.getAvion().getCapacidad(), detailsX, radarY + 12);
        
        int detailsX2 = detailsX + 120;
        String puerta = (vuelo.getPuerta() != null) ? vuelo.getPuerta().getCodigo() : "TBD";
        drawDetail(g2, "GATE", puerta, detailsX2, radarY - 12);
        drawDetail(g2, "MATR", vuelo.getAvion().getMatricula(), detailsX2, radarY + 12);

        // === SECCIÓN 4: ESTADO Y TIEMPO (Derecha) ===
        int statusX = w - 140;
        
        g2.setFont(FONT_STATUS);
        g2.setColor(estadoColor);
        // Alinear a la derecha
        String statusStr = estadoTexto;
        if (vuelo.getDelayed() > 0 && !statusStr.equals("FINALIZADO")) statusStr += " (+" + vuelo.getDelayed() + "m)";
        
        int sw = g2.getFontMetrics().stringWidth(statusStr);
        g2.drawString(statusStr, w - 20 - sw, radarY - 5);
        
        // Hora
        g2.setFont(FONT_DETAILS);
        g2.setColor(Color.WHITE);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime timeRef = vuelo.getFechaHoraProgramada().plusMinutes(vuelo.getDelayed());
        String timeLabel = (progreso >= 1) ? "LLEGADA" : "SALIDA";
        if (progreso > 0 && progreso < 1) {
            timeRef = timeRef.plusMinutes((long)vuelo.getDuracion());
            timeLabel = "ETA";
        }
        
        String timeStr = timeLabel + " " + timeRef.format(fmt);
        int tw = g2.getFontMetrics().stringWidth(timeStr);
        g2.drawString(timeStr, w - 20 - tw, radarY + 15);

        // === BARRA DE PROGRESO INFERIOR (Estilo Laser) ===
        if (progreso > 0 && progreso < 1) {
            int barHeight = 2;
            int barY = h - barHeight;
            g2.setColor(new Color(20, 20, 20));
            g2.fillRect(0, barY, w, barHeight); // Background track
            
            g2.setColor(estadoColor);
            g2.fillRect(0, barY, (int)(w * progreso), barHeight);
            
            // Brillo en la punta de la barra
            g2.setColor(Color.WHITE);
            g2.fillOval((int)(w * progreso) - 2, barY - 1, 4, 4);
        }
    }
    
    private void drawDetail(Graphics2D g2, String label, String value, int x, int y) {
        g2.setFont(new Font("Arial", Font.BOLD, 9));
        g2.setColor(PanelTimeline.TEXT_DIM);
        g2.drawString(label, x, y - 2);
        
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.setColor(PaletaColor.get(PaletaColor.BLANCO));
        g2.drawString(value, x, y + 9);
    }
}