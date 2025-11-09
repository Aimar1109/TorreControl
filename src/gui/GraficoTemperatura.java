package gui;

import domain.Clima;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

public class GraficoTemperatura extends JPanel{
	
	private static final long serialVersionUID = 1L;
	
	public interface OnHoverListener {
		void onPuntoHover(Clima climaHovered, int indiceOffset); // Se llama cuando el ratón se pone sobre un punto
		void onPuntoExit(); // Se llama cuando el ratón se quita del punto
	}
	
	// --- Datos del Gráfico ---
    private LinkedList<Clima> datosClima; // La lista de 6 pronósticos
    private int horaBase; // La hora actual para etiquetar el eje X correctamente
    
    private OnHoverListener hoverListener;
    private List<Point> puntosGraficados;
    private int ultimoIndiceHovered = -1;
    
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
        this.puntosGraficados= new ArrayList<>();        
        // --- Detector de movimiento del ratón ---
        addMouseMotionListener(new MouseMotionAdapter() {
        	@Override
            public void mouseMoved(MouseEvent e) {
                chequearHover(e.getPoint());
            }
        });
    }
    
    public void setHoverListener(OnHoverListener listener) {
    	this.hoverListener = listener;
    }
    
    public void setDatos(LinkedList<Clima> datosClima, int horaActual) {
        this.datosClima = datosClima;
        this.horaBase = horaActual;
        repaint(); // Vuelve a pintar el componente con los nuevos datos
    }
    
    private void chequearHover(Point mousePoint) {
        if (puntosGraficados.isEmpty() || hoverListener == null) return;

        int radioSensible = 15; // Distancia en píxeles para activar el hover
        int indiceEncontrado = -1;

        for (int i = 0; i < puntosGraficados.size(); i++) {
            if (mousePoint.distance(puntosGraficados.get(i)) <= radioSensible) {
                indiceEncontrado = i;
                break;
            }
        }

        // Si el estado ha cambiado, avisamos al listener
        if (indiceEncontrado != ultimoIndiceHovered) {
            if (indiceEncontrado != -1) {
                // Entró en un punto nuevo
                hoverListener.onPuntoHover(datosClima.get(indiceEncontrado), indiceEncontrado);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Cambiar cursor
            } else {
                // Salió de cualquier punto
                hoverListener.onPuntoExit();
                setCursor(Cursor.getDefaultCursor()); // Restaurar cursor
            }
            ultimoIndiceHovered = indiceEncontrado;
        }
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
        double espaciadoX = (double) anchoDibujo / (NUM_HORAS_A_MOSTRAR - 1);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(COLOR_LINEA_TEMPERATURA);
        g2d.setStroke(new BasicStroke(2));

        puntosGraficados.clear();

        int prevX = -1;
        int prevY = -1;
        
        for (int i = 0; i < NUM_HORAS_A_MOSTRAR; i++) {
            Clima clima = datosClima.get(i);
            double temp = clima.getTemperatura();
            int x = (int) (x0 + i * espaciadoX);
            // Aseguramos que 'temp' no se salga visualmente de los límites MIN/MAX
            double tempClamp = Math.max(MIN_TEMP, Math.min(MAX_TEMP, temp));
            int y = (int) (y0 + altoDibujo - ((tempClamp - MIN_TEMP) / (MAX_TEMP - MIN_TEMP) * altoDibujo));

            // Líneas verticales y etiquetas hora
            g2d.setColor(COLOR_GRID_LINEAS);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine(x, y0, x, y0 + altoDibujo);
            g2d.setColor(COLOR_TEXTO);
            String etiquetaHora = String.format("%02d:00", (horaBase + i) % 24);
            g2d.drawString(etiquetaHora, x - 15, y0 + altoDibujo + 20);

            // Línea de temperatura
            g2d.setColor(COLOR_LINEA_TEMPERATURA);
            g2d.setStroke(new BasicStroke(2));
            if (prevX != -1) {
                g2d.drawLine(prevX, prevY, x, y);
            }
            g2d.fillOval(x - 3, y - 3, 6, 6);

            puntosGraficados.add(new Point(x, y));
            prevX = x;
            prevY = y;
        }
    }

}
