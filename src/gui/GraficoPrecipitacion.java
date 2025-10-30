package gui;

import domain.Clima;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class GraficoPrecipitacion extends JPanel{

	private static final long serialVersionUID = 1L;
	
	// --- Datos del Gráfico ---
    private LinkedList<Clima> datosClima;
    private int horaBase;
    
 // --- Márgenes ---
    private final int MARGEN = 30; // Margen alrededor del gráfico
    private final int NUM_HORAS_A_MOSTRAR = 6;
    private final int ANCHO_BARRA_PX = 30; // Ancho de cada barra de precipitación
    
    // --- Colores del Gráfico ---
    private final Color COLOR_BARRA_PRECIP = new Color(135, 206, 250); // Azul cielo
    private final Color COLOR_FONDO_GRID = new Color(240, 240, 240);
    private final Color COLOR_GRID_LINEAS = new Color(200, 200, 200);
    private final Color COLOR_TEXTO = Color.BLACK;

    public GraficoPrecipitacion() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); 
    }
    
    public void setDatos(LinkedList<Clima> datosClima, int horaActual) {
        this.datosClima = datosClima;
        this.horaBase = horaActual;
        repaint(); // Vuelve a pintar el componente con los nuevos datos
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (datosClima == null || datosClima.size() < NUM_HORAS_A_MOSTRAR) {
            g.setColor(COLOR_TEXTO);
            g.drawString("Cargando datos de precipitación...", getWidth() / 2 - 80, getHeight() / 2);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- 1. Definir el área de dibujo real (quitando márgenes) ---
        int anchoDibujo = getWidth() - 2 * MARGEN;
        int altoDibujo = getHeight() - 2 * MARGEN;
        int x0 = MARGEN;
        int y0 = MARGEN; // Y superior del área de dibujo

        // --- 2. Dibujar la cuadrícula de fondo y etiquetas del eje Y (0%, 50%, 100%) ---
        g2d.setColor(COLOR_FONDO_GRID);
        g2d.fillRect(x0, y0, anchoDibujo, altoDibujo); // Fondo del área de datos

        g2d.setColor(COLOR_GRID_LINEAS);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        // Dibujar líneas para 0%, 50% y 100%
        for (int i = 0; i <= 2; i++) {
            double porcentaje = 0 + (100 * i / 2.0); // 0, 50, 100
            int y = y0 + altoDibujo - (int) ((porcentaje / 100.0) * altoDibujo);
            
            g2d.drawLine(x0, y, x0 + anchoDibujo, y); // Línea horizontal
            g2d.setColor(COLOR_TEXTO);
            g2d.drawString(String.format("%.0f%%", porcentaje), MARGEN - 25, y + 5); // Etiqueta Y
            g2d.setColor(COLOR_GRID_LINEAS);
        }

        // --- 3. Dibujar etiquetas del eje X (Horas) y las Barras ---
        double espaciadoX = (double) anchoDibujo / NUM_HORAS_A_MOSTRAR; // Espacio entre centros de barras

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(COLOR_BARRA_PRECIP);

        for (int i = 0; i < NUM_HORAS_A_MOSTRAR; i++) {
            Clima clima = datosClima.get(i);
            int probPrecipitacion = clima.getProbabilidadPrecipitacion(); // 0-100

            // Calcular las coordenadas X e Y para esta barra
            int xCentro = (int) (x0 + (i * espaciadoX) + (espaciadoX / 2)); // Centro de la barra
            int xBarra = xCentro - (ANCHO_BARRA_PX / 2); // Esquina izquierda de la barra
            
            int alturaBarra = (int) ((probPrecipitacion / 100.0) * altoDibujo);
            int yBarra = y0 + altoDibujo - alturaBarra; // La 'Y' superior de la barra
            
            // Dibujar la barra
            g2d.fillRect(xBarra, yBarra, ANCHO_BARRA_PX, alturaBarra);

            // Etiqueta de la hora
            g2d.setColor(COLOR_TEXTO);
            String etiquetaHora = String.format("%02d:00", (horaBase + i) % 24); // T+0, T+1...
            g2d.drawString(etiquetaHora, xCentro - 15, y0 + altoDibujo + 20); // Posición debajo del gráfico
            g2d.setColor(COLOR_BARRA_PRECIP);
        }
    }
}
