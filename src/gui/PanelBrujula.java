package gui;

import domain.PaletaColor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class PanelBrujula extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private double velocidad;
	private double direccionGrados;
	
	public PanelBrujula() {
		
		this.velocidad = 0.0;
		this.direccionGrados = 0.0;
		
		setBackground(PaletaColor.get(PaletaColor.FONDO));
		setBorder(new LineBorder(PaletaColor.get(PaletaColor.PRIMARIO), 1, true));
		setPreferredSize(new Dimension(220, 220));
	}
	
	public void setDatos(double velocidad, double direccion) {
		this.velocidad = velocidad;
		this.direccionGrados = direccion;
		
		revalidate();
		repaint();
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		int w = getWidth();
		int h = getHeight();
		
		
		Insets insets = getInsets();
        int areaW = w - insets.left - insets.right;
        int areaH = h - insets.top - insets.bottom;
        
        int cx = insets.left + areaW / 2;
        int cy = insets.top + areaH / 2;
        int radio = Math.min(areaW, areaH) / 2 - 10;
        
        if (radio <= 0) return;
		
        // 1. Dibujar Esfera de la brújula
        g2d.setColor(Color.WHITE);
        g2d.fillOval(cx - radio, cy - radio, radio * 2, radio * 2);
        g2d.setColor(PaletaColor.get(PaletaColor.PRIMARIO));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(cx - radio, cy - radio, radio * 2, radio * 2);
		
        // 2. Dibujar Marcas Cardinales (N, S, E, O)
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        drawCenteredString(g2d, "N", cx, cy - radio + 15);
        drawCenteredString(g2d, "S", cx, cy + radio - 15);
        drawCenteredString(g2d, "E", cx + radio - 15, cy);
        drawCenteredString(g2d, "O", cx - radio + 15, cy);
		
        // 3. Dibujar Aguja Rotada
        AffineTransform old = g2d.getTransform();
        g2d.rotate(Math.toRadians(direccionGrados), cx, cy);
		
        // Flecha Norte (Roja)
        Path2D flechaNorte = new Path2D.Double();
        flechaNorte.moveTo(cx, cy - radio + 25);
        flechaNorte.lineTo(cx + 6, cy);          
        flechaNorte.lineTo(cx - 6, cy);          
        flechaNorte.closePath();
        g2d.setColor(new Color(220, 50, 50));
        g2d.fill(flechaNorte);
		
        // Flecha Sur (Gris)
        Path2D flechaSur = new Path2D.Double();
        flechaSur.moveTo(cx, cy + radio - 25);
        flechaSur.lineTo(cx + 6, cy);
        flechaSur.lineTo(cx - 6, cy);
        flechaSur.closePath();
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fill(flechaSur);
		
		g2d.setTransform(old);
		
		// 4. Pivote central y Texto de velocidad
        g2d.setColor(PaletaColor.get(PaletaColor.PRIMARIO));
        g2d.fillOval(cx - 5, cy - 5, 10, 10); // Puntito central
		
        // Recuadro para la velocidad
        String textoVel = String.format("%.0f km/h", velocidad);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int tw = fm.stringWidth(textoVel);
        int th = fm.getHeight();
		
     // Etiqueta flotante para la velocidad
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(cx - tw/2 - 6, cy + 20, tw + 12, th + 4, 10, 10);
        g2d.setColor(PaletaColor.get(PaletaColor.PRIMARIO)); // Texto en azul corporativo
        g2d.drawRoundRect(cx - tw/2 - 6, cy + 20, tw + 12, th + 4, 10, 10);
        
        drawCenteredString(g2d, textoVel, cx, cy + 20 + (th/2) + 2);
	}
	
	private void drawCenteredString(Graphics g, String text, int x, int y) {
		FontMetrics metrics = g.getFontMetrics(g.getFont());
		int xCentrado = x - (metrics.stringWidth(text) / 2);
		int yCentrado = y - (metrics.getHeight() / 2) + metrics.getAscent();
		g.drawString(text, xCentrado, yCentrado);
	}
}
