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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class PanelBrujula extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private BufferedImage imagenFondo;
	private double direccionGrados;
	private String mensajeError = "";
	
    private final Color COLOR_NORTE = new Color(255, 40, 40);
    private final Color COLOR_SUR = new Color(220, 220, 220);
    private final Color COLOR_BORDE_AGUJA = new Color(255, 255, 255, 150);
    private final Color COLOR_PIVOTE = new Color(40, 40, 40);
    private final Color COLOR_PIVOTE_BRILLO = new Color(200, 200, 200);
	
	public PanelBrujula() {
	
		this.direccionGrados = 0.0;
		
		setBackground(PaletaColor.get(PaletaColor.FONDO));
		setPreferredSize(new Dimension(240, 240));
		
		cargarImagen();
	}
	
	private void cargarImagen() {
        String nombreArchivo = "brujula_fondo.png";
        
        try {
            URL imgUrl = getClass().getResource("/img/" + nombreArchivo);
            
            if (imgUrl == null) {
                File archivo = new File("resources/img/" + nombreArchivo);
                if (archivo.exists()) {
                    imagenFondo = ImageIO.read(archivo);
                } else {
                    File archivo2 = new File("img/" + nombreArchivo);
                    if (archivo2.exists()) {
                        imagenFondo = ImageIO.read(archivo2);
                    } else {
                        mensajeError = "No se encuentra: " + archivo.getAbsolutePath();
                        System.err.println("ERROR: No se encuentra la imagen en ninguna ruta.");
                    }
                }
            } else {
                imagenFondo = ImageIO.read(imgUrl);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            mensajeError = "Excepción IO: " + e.getMessage();
        }
    }
	
	public void setDireccion(double direccion) {
        this.direccionGrados = direccion;
        revalidate();
        repaint();
    }
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
        int w = getWidth();
        int h = getHeight();
        int lado = Math.min(w, h);
        int cx = w / 2;
        int cy = h / 2;
        
        int margen = 5;
        int diametro = lado - 2 * margen;
        int radio = diametro / 2;
        
        if (radio <= 0) return;
		
        if (imagenFondo != null) {
            g2d.drawImage(imagenFondo, cx - radio, cy - radio, diametro, diametro, this);
        } else {
            // Fallback...
            g2d.setColor(new Color(220, 220, 220)); g2d.fillOval(cx - radio, cy - radio, diametro, diametro);
            g2d.setColor(Color.RED); g2d.drawString("IMG ERROR", cx - 30, cy);
        }
		
        // Dibujar Aguja Rotada
        AffineTransform old = g2d.getTransform();
        g2d.rotate(Math.toRadians(direccionGrados), cx, cy);
		
        Path2D flechaNorte = new Path2D.Double();
        flechaNorte.moveTo(cx, cy - radio + 12);
        flechaNorte.lineTo(cx + 7, cy);
        flechaNorte.lineTo(cx - 7, cy);
        flechaNorte.closePath();
		
        Path2D flechaSur = new Path2D.Double();
        flechaSur.moveTo(cx, cy + radio - 12);
        flechaSur.lineTo(cx + 7, cy);
        flechaSur.lineTo(cx - 7, cy);
        flechaSur.closePath();

        g2d.setColor(COLOR_NORTE); g2d.fill(flechaNorte);
        g2d.setColor(COLOR_SUR);   g2d.fill(flechaSur);
        
        g2d.setColor(COLOR_BORDE_AGUJA);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.draw(flechaNorte);
        g2d.draw(flechaSur);

        g2d.setTransform(old);
        
        g2d.setColor(COLOR_PIVOTE); g2d.fillOval(cx - 6, cy - 6, 12, 12); 
        g2d.setColor(COLOR_PIVOTE_BRILLO); g2d.fillOval(cx - 2, cy - 2, 4, 4);
	}
}
