package gui;

import domain.Clima;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.MouseEvent;

public class GraficoPrecipitacion extends JPanel{

	private static final long serialVersionUID = 1L;
	
	// --- Datos del Gráfico ---
    private LinkedList<Clima> datosDia;
    private int horaActual;
    
    // --- Márgenes ---
    private final int MARGEN_X = 40;
    private final int MARGEN_Y = 30;
    private final int NUM_HORAS_A_MOSTRAR = 24;
    
    // --- Colores del Gráfico ---
    private final Color COLOR_BARRA = new Color(135, 206, 250);
    private final Color COLOR_BARRA_ACTUAL = new Color(0, 85, 165); // Azul más fuerte

    public GraficoPrecipitacion() {
        setBackground(Color.WHITE);
    }
    
    public void setDatos(LinkedList<Clima> datosDia, int horaActual) {
        this.datosDia = datosDia;
        this.horaActual = horaActual;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (datosDia == null || datosDia.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int anchoUtil = w - 2 * MARGEN_X;
        int altoUtil = h - 2 * MARGEN_Y;

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(MARGEN_X, h - MARGEN_Y, w - MARGEN_X, h - MARGEN_Y);

        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        for (int p = 0; p <= 100; p += 25) {
            int y = (h - MARGEN_Y) - (int)((p / 100.0) * altoUtil);
            g2d.setColor(new Color(240, 240, 240));
            g2d.drawLine(MARGEN_X, y, w - MARGEN_X, y);
            g2d.setColor(Color.GRAY);
            g2d.drawString(p + "%", MARGEN_X - 30, y + 4);
        }
        
        double anchoBarraEspacio = (double) anchoUtil / NUM_HORAS_A_MOSTRAR;
        int anchoBarra = Math.max(2, (int)(anchoBarraEspacio * 0.7));
        int gap = (int)((anchoBarraEspacio - anchoBarra) / 2);
        
        for (int i = 0; i < NUM_HORAS_A_MOSTRAR; i++) {
            if (i >= datosDia.size()) break;
            
            Clima c = datosDia.get(i);
            int prob = c.getProbabilidadPrecipitacion();
            
            int alturaBarra = (int)((prob / 100.0) * altoUtil);
            int x = MARGEN_X + (int)(i * anchoBarraEspacio) + gap;
            int y = (h - MARGEN_Y) - alturaBarra;
            
            // Si es la hora actual, la pintamos de un color diferente
            if (i == horaActual) {
                g2d.setColor(COLOR_BARRA_ACTUAL);
            } else {
                g2d.setColor(COLOR_BARRA);
            }
            
            g2d.fillRect(x, y, anchoBarra, alturaBarra);
            
            // Etiquetas hora (cada 3 horas)
            if (i % 3 == 0) {
                g2d.setColor(Color.GRAY);
                // Centramos el texto en el espacio de la barra
                int xTexto = MARGEN_X + (int)(i * anchoBarraEspacio);
                g2d.drawString(String.format("%02d", i), xTexto + (int)(anchoBarraEspacio/4), h - MARGEN_Y + 15);
            }
        }
    }
}
