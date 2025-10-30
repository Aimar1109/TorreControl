package gui;

import domain.Clima;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class GraficoTemperatura extends JPanel{
	
	private static final long serialVersionUID = 1L;
	
	// --- Datos del Gráfico ---
    private LinkedList<Clima> datosClima; // La lista de 6 pronósticos
    private int horaBase; // La hora actual para etiquetar el eje X correctamente
    
    // --- Márgenes y Escala ---
    private final int MARGEN = 30; // Margen alrededor del gráfico
    private final int PADDING_SUPERIOR_INFERIOR = 20; // Padding extra para que la línea no toque los bordes
    private final int NUM_HORAS_A_MOSTRAR = 6;
    
 // --- Escala Fija ---
    private final double MIN_TEMP = -5.0;
    private final double MAX_TEMP = 30.0;
    private final int PASO_TEMP = 5; // Dibujar una línea cada 5 grados
    
    // --- Colores del Gráfico ---
    private final Color COLOR_LINEA_TEMPERATURA = new Color(70, 130, 180); // Azul acero
    private final Color COLOR_FONDO_GRID = new Color(240, 240, 240); // Gris claro para el fondo de las líneas
    private final Color COLOR_GRID_LINEAS = new Color(200, 200, 200); // Gris para las líneas de la cuadrícula
    private final Color COLOR_TEXTO = Color.BLACK;
    
    public GraficoTemperatura() {
        // Establecemos un color de fondo por defecto
        setBackground(Color.WHITE);
        // Borde para diferenciarlo visualmente en el layout principal
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); 
    }
    
    public void setDatos(LinkedList<Clima> datosClima, int horaActual) {
        this.datosClima = datosClima;
        this.horaBase = horaActual;
        repaint(); // Vuelve a pintar el componente con los nuevos datos
    }
    
    @Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g); // Siempre llamar al super para asegurar que se pinte el fondo
    	
    	if (datosClima == null || datosClima.isEmpty()) {
    		// Si no hay datos, mostramos un mensaje simple
            g.setColor(COLOR_TEXTO);
            g.drawString("Cargando datos de temperatura...", getWidth() / 2 - 80, getHeight() / 2);
            return;
    	}
    	
    	Graphics2D g2d = (Graphics2D) g; // Usamos Graphics2D para más opciones (anti-aliasing)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Suaviza las líneas
        
        // --- Definir el área de dibujo real (quitando márgenes) ---
        int anchoDibujo = getWidth() - 2 * MARGEN;
        int altoDibujo = getHeight() - 2 * MARGEN - PADDING_SUPERIOR_INFERIOR; // Quitamos padding arriba y abajo
        int x0 = MARGEN;
        int y0 = MARGEN + PADDING_SUPERIOR_INFERIOR / 2; // Y superior del área de dibujo
        
        // --- Dibujar la cuadrícula de fondo y etiquetas del eje Y ---
        g2d.setColor(COLOR_FONDO_GRID);
        g2d.fillRect(x0, y0, anchoDibujo, altoDibujo); // Fondo del área de datos

        g2d.setColor(COLOR_GRID_LINEAS);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10)); // Fuente para las etiquetas
        
        for (double temp = MIN_TEMP; temp <= MAX_TEMP; temp += PASO_TEMP) {
            
            // Calculamos la posición Y para esta temperatura
            // (temp - MIN_TEMP) / (MAX_TEMP - MIN_TEMP) nos da un ratio de 0.0 a 1.0
            int y = (int) (y0 + altoDibujo - ((temp - MIN_TEMP) / (MAX_TEMP - MIN_TEMP) * altoDibujo));
            
            g2d.drawLine(x0, y, x0 + anchoDibujo, y); // Línea horizontal
            g2d.setColor(COLOR_TEXTO);
            g2d.drawString(String.format("%.0f°C", temp), MARGEN - 25, y + 5); // Etiqueta Y
            g2d.setColor(COLOR_GRID_LINEAS);
        }
        
        
        // --- Dibujar etiquetas del eje X (Horas) y las líneas verticales ---
        double espaciadoX = (double) anchoDibujo / (NUM_HORAS_A_MOSTRAR - 1); // Espacio entre puntos de datos

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        for (int i = 0; i < NUM_HORAS_A_MOSTRAR; i++) {
            int x = (int) (x0 + i * espaciadoX);
            g2d.drawLine(x, y0, x, y0 + altoDibujo); // Línea vertical
            
            // Etiqueta de la hora
            g2d.setColor(COLOR_TEXTO);
            String etiquetaHora = String.format("%02d:00", (horaBase + i) % 24); // T+1, T+2...
            g2d.drawString(etiquetaHora, x - 15, y0 + altoDibujo + 20); // Posición debajo del gráfico
            g2d.setColor(COLOR_GRID_LINEAS);
        }
        
        // --- Dibujar la línea de temperatura ---
        g2d.setColor(COLOR_LINEA_TEMPERATURA);
        g2d.setStroke(new BasicStroke(2)); // Grosor de la línea
        
        int prevX = -1;
        int prevY = -1;
        
        for (int i = 0; i < NUM_HORAS_A_MOSTRAR; i++) {
            Clima clima = datosClima.get(i);
            double temp = clima.getTemperatura();

            // Calcular las coordenadas X e Y para este punto
            int x = (int) (x0 + i * espaciadoX);
            int y = (int) (y0 + altoDibujo - ((temp - MIN_TEMP) / (MAX_TEMP - MIN_TEMP) * altoDibujo));

            if (prevX != -1) {
                g2d.drawLine(prevX, prevY, x, y); // Dibujar segmento de línea
            }
            
            // Dibujar un pequeño círculo en cada punto de dato
            g2d.fillOval(x - 3, y - 3, 6, 6); 

            prevX = x;
            prevY = y;
        }
    }

}
