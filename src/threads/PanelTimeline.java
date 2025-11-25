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

import domain.PaletaColor;
import domain.Vuelo;

public class PanelTimeline extends JPanel implements ObservadorTiempo {

    private static final long serialVersionUID = 1L;
    
    // CONSTANTE: Código del aeropuerto local
    public static final String CODIGO_AEROPUERTO_LOCAL = "LEBB"; 
    
    private JPanel listPanel;
    private List<RadarTile> tiles;

    // NOTA: Se han eliminado las variables estáticas de color locales.
    // Ahora se usa directamente PaletaColor.get(...)

    public PanelTimeline(ArrayList<Vuelo> vuelos) {
        setLayout(new BorderLayout());
        setBackground(PaletaColor.get(PaletaColor.FONDO_OSCURO));

        // Ordenar: Prioridad a los activos
        Collections.sort(vuelos, (v1, v2) -> {
            LocalDateTime now = RelojGlobal.getInstancia().getTiempoActual();
            int score1 = getFlightScore(v1, now);
            int score2 = getFlightScore(v2, now);
            return Integer.compare(score1, score2);
        });

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(PaletaColor.get(PaletaColor.FONDO_OSCURO));
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tiles = new ArrayList<>();
        
        for (Vuelo v : vuelos) {
            RadarTile tile = new RadarTile(v);
            listPanel.add(tile);
            listPanel.add(Box.createRigidArea(new Dimension(0, 8))); 
            tiles.add(tile);
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(PaletaColor.get(PaletaColor.FONDO_OSCURO));
        add(scroll, BorderLayout.CENTER);

        RelojGlobal.getInstancia().addObservador(this);
    }
    
    private int getFlightScore(Vuelo v, LocalDateTime now) {
        LocalDateTime dep = v.getFechaHoraProgramada().plusMinutes(v.getDelayed());
        LocalDateTime arr = dep.plusMinutes((long)v.getDuracion());
        
        if (now.isAfter(dep) && now.isBefore(arr)) return 1; 
        if (now.isBefore(dep) && ChronoUnit.MINUTES.between(now, dep) < 60) return 2; 
        if (now.isBefore(dep)) return 3; 
        return 4; 
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
 * Tarjeta estilo HUD.
 */
class RadarTile extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private Vuelo vuelo;
    private float progreso = 0f;
    private Color estadoColor = Color.GRAY;
    private String estadoTexto = "N/A";
    
    private float pulseAlpha = 0f;
    private boolean isFlying = false;
    
    // Datos lógicos
    private boolean esSalida; 

    // Fuentes
    private static final Font FONT_CODE = new Font("Consolas", Font.BOLD, 22);
    private static final Font FONT_ROUTE = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_DETAILS = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_STATUS = new Font("Consolas", Font.BOLD, 14);
    private static final Font FONT_AIRLINE = new Font("Segoe UI", Font.BOLD, 11);

    public RadarTile(Vuelo vuelo) {
        this.vuelo = vuelo;
        
        String origen = vuelo.getOrigen().getCodigo();
        this.esSalida = origen.equals(PanelTimeline.CODIGO_AEROPUERTO_LOCAL);
        
        setOpaque(false);
        setMinimumSize(new Dimension(400, 95));
        setPreferredSize(new Dimension(600, 95));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
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
            estadoColor = PaletaColor.get(PaletaColor.TEXTO_SUAVE); 
            estadoTexto = "FINALIZADO";
        } else if (now.isAfter(salida)) {
            isFlying = true;
            progreso = (float)elapsed / (float)Math.max(totalMin, 1);
            // Uso de PaletaColor para estados
            estadoColor = vuelo.getDelayed() > 0 ? PaletaColor.get(PaletaColor.DELAYED) : PaletaColor.get(PaletaColor.EXITO);
            estadoTexto = "EN VUELO";
        } else {
            progreso = 0f;
            
            if (vuelo.getDelayed() > 0) {
                estadoColor = PaletaColor.get(PaletaColor.DELAYED); 
                estadoTexto = "RETRASADO";             
            } else if (toGo < 60) {
                estadoColor = PaletaColor.get(PaletaColor.ACENTO); 
                estadoTexto = "EMBARCANDO"; 
            } else {
                estadoColor = PaletaColor.get(PaletaColor.SECUNDARIO); 
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

        // --- 1. FONDO CON PALETA ---
        GradientPaint bgGrad = new GradientPaint(
            0, 0, PaletaColor.get(PaletaColor.TILE_INICIO), 
            0, h, PaletaColor.get(PaletaColor.TILE_FIN)
        );
        g2.setPaint(bgGrad);
        g2.fillRoundRect(0, 0, w, h, 10, 10);
        
        // Borde indicador lateral (Color de estado calculado dinámicamente)
        g2.setColor(estadoColor);
        g2.fillRoundRect(0, 0, 6, h, 10, 10); 
        
        // Borde del Tile
        g2.setColor(PaletaColor.get(PaletaColor.TILE_BORDE));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w-1, h-1, 10, 10);

        // === 2. ICONO DIRECCIÓN ===
        int iconX = 25;
        int iconY = h/2;
        dibujarIconoDireccion(g2, iconX, iconY, esSalida, estadoColor);

        // === 3. CÓDIGO Y AEROLÍNEA ===
        int textLeftX = iconX + 45;
        
        // Aerolínea
        g2.setFont(FONT_AIRLINE);
        g2.setColor(PaletaColor.get(PaletaColor.ACENTO)); // Usamos ACENTO (Naranja)
        String aerolinea = vuelo.getAereolinea().getNombre().toUpperCase();
        g2.drawString(aerolinea, textLeftX, 25);

        // Código Vuelo
        g2.setFont(FONT_CODE);
        g2.setColor(PaletaColor.get(PaletaColor.BLANCO));
        g2.drawString(vuelo.getCodigo(), textLeftX, 48);
        
        // Etiqueta Salida/Llegada
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(PaletaColor.get(PaletaColor.BLANCO));
        String tagDir = esSalida ? "SALIDA" : "LLEGADA";
        g2.drawString(tagDir, textLeftX, 65);

        // === 4. RUTA ===
        int routeX = textLeftX + 120;
        int routeY = h/2 - 5;
        
        g2.setFont(FONT_ROUTE);
        g2.setColor(PaletaColor.get(PaletaColor.BLANCO));
        
        String origenCode = vuelo.getOrigen().getCodigo();
        String destinoCode = vuelo.getDestino().getCodigo();
        
        // Dibujar Origen
        g2.drawString(origenCode, routeX, routeY);
        int wOrigen = g2.getFontMetrics().stringWidth(origenCode);
        
        // Flecha
        int arrowX = routeX + wOrigen + 10;
        dibujarFlecha(g2, arrowX, routeY - 5, 30, PaletaColor.get(PaletaColor.TEXTO_SUAVE));
        
        // Dibujar Destino
        g2.setColor(PaletaColor.get(PaletaColor.BLANCO)); 
        g2.drawString(destinoCode, arrowX + 40, routeY);
        
        // Nombres ciudades
        g2.setFont(FONT_DETAILS);
        g2.setColor(PaletaColor.get(PaletaColor.TEXTO_SUAVE));
        String rutaFull = vuelo.getOrigen().getCiudad() + " - " + vuelo.getDestino().getCiudad();
        if (rutaFull.length() > 30) rutaFull = rutaFull.substring(0, 27) + "...";
        g2.drawString(rutaFull, routeX, routeY + 20);

        // === 5. DETALLES TÉCNICOS ===
        int detailsX = w / 2 + 40; 
        
        drawDetail(g2, "Avión", vuelo.getAvion().getModelo(), detailsX, routeY - 5);
        drawDetail(g2, "Pasajeros", vuelo.getPasajeros().size() + " / " + vuelo.getAvion().getCapacidad(), detailsX, routeY + 20);
        
        int detailsX2 = detailsX + 120;
        String puerta = (vuelo.getPuerta() != null) ? vuelo.getPuerta().getCodigo() : "--";
        drawDetail(g2, "Puerta", puerta, detailsX2, routeY - 5);
        drawDetail(g2, "Matrícula", vuelo.getAvion().getMatricula(), detailsX2, routeY + 20);

        // === 6. ESTADO Y HORARIOS (LADO DERECHO) ===
        int rightMargin = w - 20;
        
        // Estado Texto
        g2.setFont(FONT_STATUS);
        g2.setColor(estadoColor);
        String statusStr = estadoTexto;
        int sw = g2.getFontMetrics().stringWidth(statusStr);
        g2.drawString(statusStr, rightMargin - sw, 30);
        
        // --- GESTIÓN DE DOBLE HORA ---
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        
        LocalDateTime scheduledTime = vuelo.getFechaHoraProgramada();
        LocalDateTime realTime = scheduledTime.plusMinutes(vuelo.getDelayed());
        
        if (!esSalida) {
             scheduledTime = scheduledTime.plusMinutes((long)vuelo.getDuracion());
             realTime = realTime.plusMinutes((long)vuelo.getDuracion());
        }

        g2.setFont(FONT_DETAILS);
        String labelTime = esSalida ? "Salida:" : "Llegada:";
        
        if (vuelo.getDelayed() > 0) {
            // CASO RETRASO
            String origStr = scheduledTime.format(fmt);
            String newStr = realTime.format(fmt);
            
            g2.setColor(PaletaColor.get(PaletaColor.DELAYED));
            int wNew = g2.getFontMetrics().stringWidth(newStr);
            g2.drawString(newStr, rightMargin - wNew, 55);
            
            g2.setColor(PaletaColor.get(PaletaColor.TEXTO_SUAVE));
            int wOrig = g2.getFontMetrics().stringWidth(origStr);
            int xOrig = rightMargin - wNew - 15 - wOrig;
            g2.drawString(origStr, xOrig, 55);
            
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(xOrig, 55 - 4, xOrig + wOrig, 55 - 4);
            
            g2.drawString(labelTime, xOrig - 45, 55);
            
        } else {
            // CASO NORMAL
            g2.setColor(PaletaColor.get(PaletaColor.BLANCO));
            String timeStr = labelTime + " " + scheduledTime.format(fmt);
            int tw = g2.getFontMetrics().stringWidth(timeStr);
            g2.drawString(timeStr, rightMargin - tw, 55);
        }

        // Barra progreso inferior
        if (progreso > 0 && progreso < 1) {
            int barHeight = 3;
            int barY = h - barHeight;
            
            // Fondo barra
            g2.setColor(PaletaColor.get(PaletaColor.NEGRO_SUAVE));
            g2.fillRect(0, barY, w, barHeight); 
            
            // Progreso
            g2.setColor(estadoColor);
            g2.fillRect(0, barY, (int)(w * progreso), barHeight);
            
            // Brillo punta
            g2.setColor(PaletaColor.get(PaletaColor.BLANCO));
            g2.fillOval((int)(w * progreso) - 2, barY - 2, 5, 5);
        }
    }
    
    private void drawDetail(Graphics2D g2, String label, String value, int x, int y) {
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10)); 
        g2.setColor(PaletaColor.get(PaletaColor.TEXTO_SUAVE));
        g2.drawString(label.toUpperCase(), x, y - 2);
        
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g2.setColor(PaletaColor.get(PaletaColor.BLANCO));
        g2.drawString(value, x, y + 11);
    }
    
    private void dibujarFlecha(Graphics2D g2, int x, int y, int width, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(x, y, x + width, y);
        int arrowHeadSize = 4;
        g2.drawLine(x + width - arrowHeadSize, y - arrowHeadSize, x + width, y);
        g2.drawLine(x + width - arrowHeadSize, y + arrowHeadSize, x + width, y);
        g2.fillOval(x - 2, y - 2, 4, 4);
    }
    
    private void dibujarIconoDireccion(Graphics2D g2, int x, int y, boolean esSalida, Color color) {
        int size = 34;
        int half = size / 2;
        
        // Fondo Círculo usando NEGRO_SUAVE de la paleta
        g2.setColor(PaletaColor.get(PaletaColor.NEGRO_SUAVE));
        g2.fillOval(x, y - half, size, size);
        g2.setColor(color.darker());
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x, y - half, size, size);
        
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.5f));
        
        int cx = x + half;
        int cy = y;
        int d = 6;
        
        if (esSalida) {
            g2.drawLine(cx - d, cy + d, cx + d, cy - d);
            g2.drawLine(cx + d, cy - d, cx + d - 5, cy - d);
            g2.drawLine(cx + d, cy - d, cx + d, cy - d + 5);
        } else {
            g2.drawLine(cx - d, cy - d, cx + d, cy + d);
            g2.drawLine(cx + d, cy + d, cx + d - 5, cy + d); 
            g2.drawLine(cx + d, cy + d, cx + d, cy + d - 5); 
        }
        
        if (isFlying) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulseAlpha));
            g2.setColor(color);
            g2.fillOval(x + 5, y - half + 5, size - 10, size - 10);
            g2.setComposite(AlphaComposite.SrcOver);
        }
    }
}