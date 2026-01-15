package threads;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicScrollBarUI;

import domain.PaletaColor;
import domain.Vuelo;

public class PanelTimeline extends JPanel implements ObservadorTiempo {

    private static final long serialVersionUID = 1L;
    
    public static final String CODIGO_AEROPUERTO_LOCAL = "LEBB"; 
    
    private JPanel listPanel;
    private List<RadarTile> tiles;
    
    private ThreadAnimacion threadAnimacion;
    private volatile boolean ejecutando;
    private volatile LocalDateTime tiempoActual;

    public PanelTimeline(ArrayList<Vuelo> vuelos) {
        setLayout(new BorderLayout());
        setBackground(PaletaColor.get(PaletaColor.FONDO_OSCURO));

        // Ordenación inicial por prioridad	usando Comparator (Lambda)
        Collections.sort(vuelos, (v1, v2) -> {
            LocalDateTime now = RelojGlobal.getInstancia().getTiempoActual();
            return Integer.compare(getFlightScore(v1, now), getFlightScore(v2, now));
        });
        
        // BoxLayout vertical para apilar elementos dinámicamente
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(PaletaColor.get(PaletaColor.FONDO_OSCURO));
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tiles = new ArrayList<>();
        
        // Generación de tarjetas visuales (RadarTile)
        for (Vuelo v : vuelos) {
            RadarTile tile = new RadarTile(v);
            listPanel.add(tile);
            listPanel.add(Box.createRigidArea(new Dimension(0, 8))); 
            tiles.add(tile);
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        estilizarScrollPane(scroll);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(PaletaColor.get(PaletaColor.FONDO_OSCURO));
        add(scroll, BorderLayout.CENTER);
        
        // Inicialización del hilo de refresco
        this.ejecutando = true;
        this.tiempoActual = RelojGlobal.getInstancia().getTiempoActual();
        iniciarAnimacion();

        RelojGlobal.getInstancia().addObservador(this);
    }
    
    // Logica de priorización de vuelos
    private int getFlightScore(Vuelo v, LocalDateTime now) {
        LocalDateTime dep = v.getFechaHoraProgramada().plusMinutes(v.getDelayed());
        LocalDateTime arr = dep.plusMinutes((long)v.getDuracion());
        
        if (now.isAfter(dep) && now.isBefore(arr)) return 1; // En aire
        if (now.isBefore(dep) && ChronoUnit.MINUTES.between(now, dep) < 60) return 2; //Saliendo
        if (now.isBefore(dep)) return 3; //Futuro
        return 4; // Pasado
    }
    
    private void iniciarAnimacion() {
        this.threadAnimacion = new ThreadAnimacion();
        // Daemon: El hilo muere automáticamente si se cierra la aplicación
        this.threadAnimacion.setDaemon(true);
        this.threadAnimacion.start();
    }

    @Override
    public void actualizarTiempo(LocalDateTime nuevoTiempo) {
    	this.tiempoActual = nuevoTiempo;
    }


    public void detener() {
    	ejecutando = false; // Rompe el bucle del hilo
        if (threadAnimacion != null) {
            threadAnimacion.interrupt();
        }
        RelojGlobal.getInstancia().eliminarObservador(this);
    }
    
    // Hilo dedicado a la actualización de estado y repintado
    private class ThreadAnimacion extends Thread {
        @Override
        public void run() {
            while (ejecutando) {
                try {
                    if (!RelojGlobal.getInstancia().isPausado() && tiempoActual != null) {
                        // Actualización lógica
                        for (RadarTile tile : tiles) {
                            tile.actualizarEstado(tiempoActual);
                        }
                        // Actualización visual
                        SwingUtilities.invokeLater(() -> listPanel.repaint());
                    }
                    Thread.sleep(40); // ~25 FPS
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

 // IAG: Configuración visual del ScrollPane con estilización para modernidad aplicada
 // también en el panelsalesman
private void estilizarScrollPane(JScrollPane scroll) {
    scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
    scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

    scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
    scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
    
    JPanel corner = new JPanel();
    corner.setBackground(PaletaColor.get(PaletaColor.FONDO_OSCURO));
    scroll.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, corner);
    
    scroll.setBorder(null);
}

// IAG: Clase estética con mismo toque del panelsalesman
private static class ModernScrollBarUI extends BasicScrollBarUI {
    @Override
    protected void configureScrollBarColors() {
        this.thumbColor = new Color(80, 85, 90); 
        this.trackColor = PaletaColor.get(PaletaColor.FONDO_OSCURO);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }

    @Override
    protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }

    private JButton createZeroButton() {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(0, 0));
        return btn;
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setPaint(isThumbRollover() ? new Color(120, 125, 130) : thumbColor);
        g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8);
        g2.dispose();
    }
    
    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(trackColor);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    }
}

/**
 * RadarTile: Componente gráfico que representa un vuelo individual.
 * Utiliza Graphics2D para dibujar un HUD vectorial en lugar de componentes estándar.
 */
class RadarTile extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private Vuelo vuelo;
    private float progreso = 0f;
    private Color estadoColor = Color.GRAY;
    private String estadoTexto = "N/A";
    
    private float pulseAlpha = 0f;
    private boolean isFlying = false;
    private boolean isHover = false;
    private boolean isEmergency = false;
    
    private boolean esSalida; 

    private static final Font FONT_CODE = new Font("Consolas", Font.BOLD, 22);
    private static final Font FONT_ROUTE = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_DETAILS = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_STATUS = new Font("Consolas", Font.BOLD, 14);
    private static final Font FONT_AIRLINE = new Font("Segoe UI", Font.BOLD, 11);

    public RadarTile(Vuelo vuelo) {
        this.vuelo = vuelo;
        this.isEmergency = vuelo.isEmergencia();
        
        String origen = vuelo.getOrigen().getCodigo();
        this.esSalida = origen.equals(PanelTimeline.CODIGO_AEROPUERTO_LOCAL);
        
        setOpaque(false);
        setMinimumSize(new Dimension(400, 95));
        setPreferredSize(new Dimension(600, 95));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        
        // MouseAdapter para interactividad
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHover = true;
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                repaint(); 
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHover = false;
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                repaint();
            }
        });
    }
    // Este metodo calcula el estado en el que se encuentra cada vuelo
    public void actualizarEstado(LocalDateTime now) {
        this.isEmergency = vuelo.isEmergencia();

        LocalDateTime salida = vuelo.getFechaHoraProgramada().plusMinutes(vuelo.getDelayed());
        LocalDateTime llegada = salida.plusMinutes((long)vuelo.getDuracion());
        
        // Ventanas de tiempo extra
        long MINUTOS_DESEMBARQUE = 15; 
        long MINUTOS_ATERRIZAJE = 15;
        
        LocalDateTime finDesembarque = llegada.plusMinutes(MINUTOS_DESEMBARQUE);
        LocalDateTime inicioAterrizaje = llegada.minusMinutes(MINUTOS_ATERRIZAJE);

        // IAG
        long totalMin = ChronoUnit.MINUTES.between(salida, llegada);
        long elapsed = ChronoUnit.MINUTES.between(salida, now);
        long toGo = ChronoUnit.MINUTES.between(now, salida);

        isFlying = false;

        // LÓGICA DE ESTADOS
        if (now.isAfter(finDesembarque)) {
            progreso = 1f;
            estadoColor = PaletaColor.get(PaletaColor.TEXTO_SUAVE); 
            estadoTexto = "FINALIZADO";
        
        } else if (now.isAfter(llegada)) {
            progreso = 1f;
            
            if (esSalida) {
                estadoColor = PaletaColor.get(PaletaColor.TEXTO_SUAVE);
                estadoTexto = "EN DESTINO";
            } else {
                // Si es llegada a Bilbao mostramos desembarque
                estadoColor = new Color(255, 140, 0); 
                estadoTexto = "DESEMBARCANDO";
                isFlying = true; 
            }

        } else if (now.isAfter(salida)) {
            isFlying = true;
            progreso = (float)elapsed / (float)Math.max(totalMin, 1);
            
            // LÓGICA DE EMERGENCIA EN VUELO
            if (isEmergency) {
                estadoColor = new Color(255, 50, 50); 
                estadoTexto = "¡EMERGENCIA!";
            } else {
                // Comprobar si está aterrizando (solo llegadas)
                if (!esSalida && now.isAfter(inicioAterrizaje)) {
                    estadoColor = new Color(34, 139, 34); 
                    estadoTexto = "ATERRIZANDO";
                } else {
                    estadoColor = vuelo.getDelayed() > 0 ? PaletaColor.get(PaletaColor.DELAYED) : PaletaColor.get(PaletaColor.EXITO);
                    estadoTexto = "EN VUELO";
                }
            }

        } else {
            progreso = 0f;
            
            if (vuelo.getDelayed() > 0) {
                estadoColor = PaletaColor.get(PaletaColor.DELAYED); 
                estadoTexto = "RETRASADO";             
            } else if (toGo < 45) {
                estadoColor = PaletaColor.get(PaletaColor.ACENTO); 
                if (esSalida) {
                    estadoTexto = "EMBARCANDO"; 
                } else {
                    estadoTexto = "EN ORIGEN"; 
                }
            } else {
                estadoColor = PaletaColor.get(PaletaColor.SECUNDARIO); 
                estadoTexto = "PROGRAMADO";
            }
            
            if (isEmergency) {
                estadoColor = new Color(255, 50, 50);
                estadoTexto = "¡EMERGENCIA!";
            }
        }
        
        // Animación de pulso (Más rápida si es emergencia o aterrizando)
        // IAG
        if (isFlying || isEmergency) { 
            long millis = System.currentTimeMillis();
            boolean aterrizando = "ATERRIZANDO".equals(estadoTexto);
            double speed = isEmergency ? 100.0 : (aterrizando ? 180.0 : 250.0); 
            
            pulseAlpha = (float) (0.3f + 0.2f * Math.sin(millis / speed)); 
        } else {
            pulseAlpha = 0f;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // IAG: Activamos Antialiasing para gráficos vectoriales suaves
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Si hay emergencia, cambiamos el tono base a rojizo
        Color cInicio = isEmergency ? new Color(60, 20, 20) : PaletaColor.get(PaletaColor.TILE_INICIO);
        Color cFin    = isEmergency ? new Color(30, 10, 10) : PaletaColor.get(PaletaColor.TILE_FIN);

        GradientPaint bgGrad = new GradientPaint(0, 0, cInicio, 0, h, cFin);
        g2.setPaint(bgGrad);
        g2.fillRoundRect(0, 0, w, h, 10, 10);
        
        // Efecto hover
        if (isHover) {
            g2.setColor(new Color(176, 196, 222, 15)); 
            g2.fillRoundRect(0, 0, w, h, 10, 10);
            
            g2.setColor(new Color(176, 196, 222, 60));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w-1, h-1, 10, 10);
        } else {
            if (isEmergency) {
            	// Parpadeo del borde en emergencia usando Alpha
                int alpha = (int)(100 + (pulseAlpha * 300)); 
                if (alpha > 255) alpha = 255; if (alpha < 0) alpha = 0;
                g2.setColor(new Color(255, 0, 0, alpha));
                g2.setStroke(new BasicStroke(2f)); 
            } else {
                g2.setColor(PaletaColor.get(PaletaColor.TILE_BORDE));
                g2.setStroke(new BasicStroke(1f));
            }
            g2.drawRoundRect(0, 0, w-1, h-1, 10, 10);
        }
        
        // Indicador lateral
        g2.setColor(estadoColor);
        g2.fillRoundRect(0, 0, 6, h, 10, 10);

        // Dibujo de datos
        int iconX = 25;
        int iconY = h/2;
        dibujarIconoDireccion(g2, iconX, iconY, esSalida, isEmergency ? Color.RED : estadoColor);

        // Codigo y aerolínea
        int textLeftX = iconX + 45;
        
        g2.setFont(FONT_AIRLINE);
        g2.setColor(estadoColor); 
        String aerolinea = vuelo.getAereolinea().getNombre().toUpperCase();
        g2.drawString(aerolinea, textLeftX, 25);

        g2.setFont(FONT_CODE);
        g2.setColor(PaletaColor.get(PaletaColor.BLANCO));
        g2.drawString(vuelo.getCodigo(), textLeftX, 48);
        
        // Tipo de operación
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(PaletaColor.get(PaletaColor.BLANCO));
        String tagDir = esSalida ? "SALIDA" : "LLEGADA";
        g2.drawString(tagDir, textLeftX, 65);

        // Ruta visual
        int routeX = textLeftX + 120;
        int routeY = h/2 - 5;
        
        g2.setFont(FONT_ROUTE);
        g2.setColor(PaletaColor.get(PaletaColor.BLANCO));
        
        String origenCode = vuelo.getOrigen().getCodigo();
        String destinoCode = vuelo.getDestino().getCodigo();
        
        g2.drawString(origenCode, routeX, routeY);
        int wOrigen = g2.getFontMetrics().stringWidth(origenCode);
        
        int arrowX = routeX + wOrigen + 10;
        dibujarFlecha(g2, arrowX, routeY - 5, 30, PaletaColor.get(PaletaColor.TEXTO_SUAVE));
        
        g2.setColor(PaletaColor.get(PaletaColor.BLANCO)); 
        g2.drawString(destinoCode, arrowX + 40, routeY);
        
        // Ciudad Origen - Destino
        g2.setFont(FONT_DETAILS);
        g2.setColor(PaletaColor.get(PaletaColor.TEXTO_SUAVE));
        String rutaFull = vuelo.getOrigen().getCiudad() + " - " + vuelo.getDestino().getCiudad();
        // Acortar texto si es muy largo
        if (rutaFull.length() > 30) rutaFull = rutaFull.substring(0, 27) + "...";
        g2.drawString(rutaFull, routeX, routeY + 20);

        // Detalles técnicos
        int detailsX = w / 2 + 40; 
        drawDetail(g2, "Avión", vuelo.getAvion().getModelo(), detailsX, routeY - 5);
        drawDetail(g2, "Pasajeros", vuelo.getPasajeros().size() + " / " + vuelo.getAvion().getCapacidad(), detailsX, routeY + 20);
        
        int detailsX2 = detailsX + 120;
        String puerta = (vuelo.getPuerta() != null) ? vuelo.getPuerta().getCodigo() : "--";
        drawDetail(g2, "Puerta", puerta, detailsX2, routeY - 5);
        drawDetail(g2, "Matrícula", vuelo.getAvion().getMatricula(), detailsX2, routeY + 20);

        // Estado y hora (Columna derecha)
        int rightMargin = w - 20;
        
        g2.setFont(FONT_STATUS);
        g2.setColor(estadoColor);
        String statusStr = estadoTexto;
        
        int sw = g2.getFontMetrics().stringWidth(statusStr);
        g2.drawString(statusStr, rightMargin - sw, 30);
        
        // Formateo de horas y retrasos
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime scheduledTime = vuelo.getFechaHoraProgramada();
        LocalDateTime realTime = scheduledTime.plusMinutes(vuelo.getDelayed());
        
        if (!esSalida) {
             scheduledTime = scheduledTime.plusMinutes((long)vuelo.getDuracion());
             realTime = realTime.plusMinutes((long)vuelo.getDuracion());
        }

        g2.setFont(FONT_DETAILS);
        String labelTime = esSalida ? "Salida:" : "Llegada:";
        
        // Si hay retraso O emergencia, mostramos hora en rojo
        if (vuelo.getDelayed() > 0 || isEmergency) {
            String origStr = scheduledTime.format(fmt);
            String newStr = realTime.format(fmt);
            
            // Hora nueva en rojo
            g2.setColor(isEmergency ? Color.RED : PaletaColor.get(PaletaColor.DELAYED));
            int wNew = g2.getFontMetrics().stringWidth(newStr);
            g2.drawString(newStr, rightMargin - wNew, 55);
            // Hora original tachada
            g2.setColor(PaletaColor.get(PaletaColor.TEXTO_SUAVE));
            int wOrig = g2.getFontMetrics().stringWidth(origStr);
            int xOrig = rightMargin - wNew - 15 - wOrig;
            g2.drawString(origStr, xOrig, 55);
            
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(xOrig, 55 - 4, xOrig + wOrig, 55 - 4);
            
            g2.drawString(labelTime, xOrig - 45, 55);
        } else {
            g2.setColor(PaletaColor.get(PaletaColor.BLANCO));
            String timeStr = labelTime + " " + scheduledTime.format(fmt);
            int tw = g2.getFontMetrics().stringWidth(timeStr);
            g2.drawString(timeStr, rightMargin - tw, 55);
        }

        // Barra progreso inferior
        if (progreso > 0 && progreso < 1) {
            int barHeight = 3;
            int barY = h - barHeight;
            
            g2.setColor(PaletaColor.get(PaletaColor.NEGRO_SUAVE));
            g2.fillRect(0, barY, w, barHeight); 
            
            g2.setColor(estadoColor);
            g2.fillRect(0, barY, (int)(w * progreso), barHeight);
            
            g2.setColor(PaletaColor.get(PaletaColor.BLANCO));
            g2.fillOval((int)(w * progreso) - 2, barY - 2, 5, 5);
        }
    }
    
    // Métodos auxiliares de dibujo
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
    	// IAG: Cálculo vectorial manual para icono de avión (dibujo de líneas)
    	int size = 34;
        int half = size / 2;
        
        g2.setColor(PaletaColor.get(PaletaColor.NEGRO_SUAVE));
        g2.fillOval(x, y - half, size, size);
        g2.setColor(color.darker());
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x, y - half, size, size);
        
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.5f));
        
        int cx = x + half;
        int cy = y;
        
        if (esSalida) {
            g2.drawLine(cx - 6, cy + 6, cx + 6, cy - 6);
            g2.drawLine(cx + 6, cy - 6, cx + 1, cy - 6);
            g2.drawLine(cx + 6, cy - 6, cx + 6, cy - 1);
        } else {
            g2.drawLine(cx - 6, cy - 6, cx + 6, cy + 6);
            g2.drawLine(cx + 6, cy + 6, cx + 1, cy + 6); 
            g2.drawLine(cx + 6, cy + 6, cx + 6, cy + 1); 
        }
        // Efecto pulso (solo si está en vuelo o emergencia)
        if (isFlying || isEmergency) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulseAlpha));
            g2.setColor(color);
            g2.fillOval(x + 5, y - half + 5, size - 10, size - 10);
            g2.setComposite(AlphaComposite.SrcOver);
        }
    }
}}