package gui;

import domain.Clima;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class GraficoTemperatura extends JPanel{
	
	private static final long serialVersionUID = 1L;
	
	public interface OnHoverListener {
		void onPuntoHover(Clima climaHovered, int hora); // Se llama cuando el ratón se pone sobre un punto
		void onPuntoExit(); // Se llama cuando el ratón se quita del punto
	}
	
    private LinkedList<Clima> datosDia;
    private int horaActual;
    	
    private OnHoverListener hoverListener;
    private List<Point> puntosGraficados;
    private int ultimoIndiceHovered = -1;

    private final int MARGEN_X = 40;
    private final int MARGEN_Y = 30; 	
    private final int NUM_HORAS_A_MOSTRAR = 24;
    
    private final double MIN_TEMP = -5.0;
    private final double MAX_TEMP = 35.0;
    
    private final Color COLOR_LINEA = new Color(0, 85, 165);
    private final Color COLOR_RELLENO = new Color(0, 85, 165, 30);
    private final Color COLOR_ACTUAL = new Color(220, 50, 50, 200);
    
    public GraficoTemperatura() {
        setBackground(Color.WHITE);
        puntosGraficados = new ArrayList<>();    
        
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                chequearHover(e.getPoint());
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                limpiarHover();
            }
        });
    }
    
    public void setHoverListener(OnHoverListener listener) {
    	this.hoverListener = listener;
    }
    
    public void setDatos(LinkedList<Clima> datosDia, int horaActual) {
        this.datosDia = datosDia;
        this.horaActual = horaActual;
        repaint();
    }
    	
    private void chequearHover(Point mousePoint) {
        if (puntosGraficados.isEmpty() || hoverListener == null) return;
        
        int masCercano = -1;
        double distMin = 20.0;
        
        for (int i = 0; i < puntosGraficados.size(); i++) {
            double dist = mousePoint.distance(puntosGraficados.get(i));
            if (dist < distMin) {
                distMin = dist;
                masCercano = i;
            }
        }
        
        if (masCercano != ultimoIndiceHovered) {
            if (masCercano != -1) {
                hoverListener.onPuntoHover(datosDia.get(masCercano), masCercano);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                hoverListener.onPuntoExit();
                setCursor(Cursor.getDefaultCursor());
            }
            ultimoIndiceHovered = masCercano;
        }
    }
    
    private void limpiarHover() {
        if (hoverListener != null) {
            hoverListener.onPuntoExit();
        }
        ultimoIndiceHovered = -1;
        setCursor(Cursor.getDefaultCursor());
    }
    
    @Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	
    	if (datosDia == null || datosDia.isEmpty()) { return; }
    	
    	Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int anchoUtil = w - 2 * MARGEN_X;
        int altoUtil = h - 2 * MARGEN_Y;
        
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(MARGEN_X, h - MARGEN_Y, w - MARGEN_X, h - MARGEN_Y);

        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        for (double t = MIN_TEMP; t <= MAX_TEMP; t += 10) {
            int y = getYParaTemp(t, altoUtil);
            g2d.setColor(new Color(240, 240, 240));
            g2d.drawLine(MARGEN_X, y, w - MARGEN_X, y);
            
            g2d.setColor(Color.GRAY);
            g2d.drawString((int)t + "°", MARGEN_X - 25, y + 4);
        }
        
        double pasoX = (double) anchoUtil / (NUM_HORAS_A_MOSTRAR - 1);
        puntosGraficados.clear();
        
        Polygon poly = new Polygon();
        poly.addPoint(MARGEN_X, h - MARGEN_Y);
        
        for (int i = 0; i < NUM_HORAS_A_MOSTRAR; i++) {
            if (i >= datosDia.size()) break;
            
            Clima c = datosDia.get(i);
            int x = MARGEN_X + (int)(i * pasoX);
            int y = getYParaTemp(c.getTemperatura(), altoUtil);
            
            puntosGraficados.add(new Point(x, y));
            poly.addPoint(x, y);

            if (i % 3 == 0) {
                g2d.setColor(Color.GRAY);
                g2d.drawString(String.format("%02d", i), x - 6, h - MARGEN_Y + 15);
            }
        }
        
        if (!puntosGraficados.isEmpty()) {
            poly.addPoint(puntosGraficados.get(puntosGraficados.size()-1).x, h - MARGEN_Y);
        }
        
        g2d.setColor(COLOR_RELLENO);
        g2d.fill(poly);
        
        g2d.setColor(COLOR_LINEA);
        g2d.setStroke(new BasicStroke(2f));
        if (puntosGraficados.size() > 1) {
            for (int i = 0; i < puntosGraficados.size() - 1; i++) {
                Point p1 = puntosGraficados.get(i);
                Point p2 = puntosGraficados.get(i+1);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
        
        if (horaActual >= 0 && horaActual < puntosGraficados.size()) {
            int xActual = puntosGraficados.get(horaActual).x;
            
            g2d.setColor(COLOR_ACTUAL);
            // Línea punteada roja
            Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5f}, 0f);
            g2d.setStroke(dashed);
            g2d.drawLine(xActual, MARGEN_Y, xActual, h - MARGEN_Y);

            Point pActual = puntosGraficados.get(horaActual);
            g2d.setStroke(new BasicStroke(1f));
            g2d.fillOval(pActual.x - 5, pActual.y - 5, 10, 10);
        }
    }
    
    private int getYParaTemp(double temp, int altoUtil) {
        // Mapear temp al rango Y
        double ratio = (temp - MIN_TEMP) / (MAX_TEMP - MIN_TEMP);
        return (getHeight() - MARGEN_Y) - (int)(ratio * altoUtil);
    }

}
