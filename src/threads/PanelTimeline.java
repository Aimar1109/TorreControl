package threads;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import domain.Vuelo;

public class PanelTimeline extends JPanel {

    private static final long serialVersionUID = 1L;
    
    private JPanel contenedorTarjetas;
    private List<VueloCard> tarjetas;
    
    // Variables del hilo
    private HiloRefresco hilo;
    private volatile boolean ejecutando = true;

    public PanelTimeline(ArrayList<Vuelo> vuelos) {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));

        // Ordenar vuelos por hora de salida real (Programada + Retraso)
        Collections.sort(vuelos, new Comparator<Vuelo>() {
            @Override
            public int compare(Vuelo v1, Vuelo v2) {
                LocalDateTime salida1 = v1.getFechaHoraProgramada().plusMinutes(v1.getDelayed());
                LocalDateTime salida2 = v2.getFechaHoraProgramada().plusMinutes(v2.getDelayed());
                return salida1.compareTo(salida2);
            }
        });

        tarjetas = new ArrayList<>();
        contenedorTarjetas = new JPanel();
        contenedorTarjetas.setLayout(new BoxLayout(contenedorTarjetas, BoxLayout.Y_AXIS));
        contenedorTarjetas.setBackground(new Color(240, 242, 245));
        contenedorTarjetas.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Crear tarjetas
        for (Vuelo v : vuelos) {
            VueloCard card = new VueloCard(v);
            contenedorTarjetas.add(card);
            contenedorTarjetas.add(Box.createRigidArea(new Dimension(0, 8))); 
            tarjetas.add(card);
        }

        // Configurar Scroll
        JScrollPane scroll = new JScrollPane(contenedorTarjetas);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Iniciar Hilo
        hilo = new HiloRefresco();
        hilo.start();
    }
    
    public void detener() {
        ejecutando = false;
        if (hilo != null) {
            hilo.interrupt();
        }
    }

    // --- CLASE INTERNA DEL HILO ---
    private class HiloRefresco extends Thread {
        @Override
        public void run() {
            while (ejecutando) {
                try {
                    LocalDateTime ahora = RelojGlobal.getInstancia().getTiempoActual();

                    SwingUtilities.invokeLater(() -> {
                        for (VueloCard card : tarjetas) {
                            card.actualizar(ahora);
                        }
                    });

                    Thread.sleep(500); 

                } catch (InterruptedException e) {
                    ejecutando = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

// --- TARJETA DE VUELO INDIVIDUAL ---
class VueloCard extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private Vuelo vuelo;
    private BarraProgresoVuelo barraProgreso;
    private JLabel lblEstado;
    private JLabel lblHoras;
    
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public VueloCard(Vuelo vuelo) {
        this.vuelo = vuelo;
        
        setLayout(new BorderLayout(15, 0)); 
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        setPreferredSize(new Dimension(0, 70)); 
        setMaximumSize(new Dimension(9999, 70)); 

        // Info Izquierda
        JPanel panelInfo = new JPanel(new GridLayout(2, 1));
        panelInfo.setOpaque(false);
        
        JLabel lblCodigo = new JLabel(vuelo.getCodigo());
        lblCodigo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCodigo.setForeground(new Color(50, 50, 50));
        
        JLabel lblRuta = new JLabel(vuelo.getOrigen().getCodigo() + " ➝ " + vuelo.getDestino().getCodigo());
        lblRuta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRuta.setForeground(Color.GRAY);
        
        panelInfo.add(lblCodigo);
        panelInfo.add(lblRuta);
        
        // Barra Centro
        barraProgreso = new BarraProgresoVuelo(vuelo);
        
        // Estado Derecha
        JPanel panelEstado = new JPanel(new GridLayout(2, 1));
        panelEstado.setOpaque(false);
        
        lblEstado = new JLabel("Pendiente", JLabel.RIGHT);
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        LocalDateTime salida = vuelo.getFechaHoraProgramada().plusMinutes(vuelo.getDelayed());
        LocalDateTime llegada = salida.plusMinutes((long)vuelo.getDuracion());
        
        lblHoras = new JLabel(salida.format(TIME_FMT) + " - " + llegada.format(TIME_FMT), JLabel.RIGHT);
        lblHoras.setFont(new Font("Consolas", Font.PLAIN, 12));
        lblHoras.setForeground(Color.DARK_GRAY);
        
        panelEstado.add(lblEstado);
        panelEstado.add(lblHoras);

        add(panelInfo, BorderLayout.WEST);
        add(barraProgreso, BorderLayout.CENTER);
        add(panelEstado, BorderLayout.EAST);
    }

    public void actualizar(LocalDateTime ahora) {
        barraProgreso.setTiempoActual(ahora);
        
        LocalDateTime salida = vuelo.getFechaHoraProgramada().plusMinutes(vuelo.getDelayed());
        LocalDateTime llegada = salida.plusMinutes((long)vuelo.getDuracion());

        // Actualizar textos y colores de estado
        if (ahora.isAfter(llegada)) {
            lblEstado.setText("ATERRIZADO");
            lblEstado.setForeground(new Color(149, 165, 166)); 
        } else if (ahora.isAfter(salida)) {
            lblEstado.setText("EN VUELO");
            lblEstado.setForeground(new Color(46, 204, 113)); 
        } else {
            if (vuelo.getDelayed() > 0) {
                lblEstado.setText("RETRASADO " + vuelo.getDelayed() + "m");
                lblEstado.setForeground(new Color(231, 76, 60)); 
            } else {
                lblEstado.setText("PROGRAMADO");
                lblEstado.setForeground(new Color(52, 152, 219)); 
            }
        }
        
        // Ajuste visual si el vuelo es al día siguiente
        String textoHora = salida.format(TIME_FMT) + " - " + llegada.format(TIME_FMT);
        if (salida.getDayOfYear() != ahora.getDayOfYear()) {
            int diffDias = salida.getDayOfYear() - ahora.getDayOfYear();
            if (diffDias > 0) textoHora += " (+" + diffDias + "d)";
        }
        lblHoras.setText(textoHora);
    }
}

//--- BARRA DE ESTADO INTELIGENTE (CUENTA ATRÁS + VUELO) ---
class BarraProgresoVuelo extends JPanel {
 private static final long serialVersionUID = 1L;
 private Vuelo vuelo;
 private float porcentaje = 0f;
 
 // Variables para lógica visual
 private long minutosParaSalida = 0;
 private boolean haSalido = false;
 private boolean haLlegado = false;

 public BarraProgresoVuelo(Vuelo vuelo) {
     this.vuelo = vuelo;
     setOpaque(false);
 }

 public void setTiempoActual(LocalDateTime ahora) {
     LocalDateTime salida = vuelo.getFechaHoraProgramada().plusMinutes(vuelo.getDelayed());
     LocalDateTime llegada = salida.plusMinutes((long)vuelo.getDuracion());

     // Calculamos distancias temporales
     minutosParaSalida = ChronoUnit.MINUTES.between(ahora, salida);
     
     if (ahora.isAfter(llegada)) {
         haLlegado = true;
         haSalido = true;
         porcentaje = 1f;
     } else if (ahora.isAfter(salida)) {
         haLlegado = false;
         haSalido = true;
         // Cálculo de porcentaje de vuelo
         long duracionTotal = ChronoUnit.MINUTES.between(salida, llegada);
         long tiempoVolado = ChronoUnit.MINUTES.between(salida, ahora);
         porcentaje = (float) tiempoVolado / (float) (duracionTotal > 0 ? duracionTotal : 1);
     } else {
         // AÚN NO HA SALIDO
         haLlegado = false;
         haSalido = false;
         porcentaje = 0f;
     }
     repaint();
 }

 @Override
 protected void paintComponent(Graphics g) {
     super.paintComponent(g);
     Graphics2D g2 = (Graphics2D) g;
     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

     int w = getWidth();
     int h = getHeight();
     int alturaBarra = 14; // Un poco más gruesa para que quepa texto dentro
     int yBarra = (h - alturaBarra) / 2;
     int margenLateral = 5;
     int anchoUtil = w - (margenLateral * 2);

     // 1. DIBUJAR FONDO BASE
     g2.setColor(new Color(235, 235, 235));
     g2.fillRoundRect(margenLateral, yBarra, anchoUtil, alturaBarra, 8, 8);

     // ---------------------------------------------------------
     // CASO A: EL AVIÓN ESTÁ VOLANDO (Barra de Progreso + Avión)
     // ---------------------------------------------------------
     if (haSalido && !haLlegado) {
         // Color dinámico (Rojo si retrasado, Azul si en hora)
         Color colorVuelo = (vuelo.getDelayed() > 0) ? new Color(231, 76, 60) : new Color(52, 152, 219);
         
         // Barra de relleno
         int wProgreso = (int) (anchoUtil * porcentaje);
         g2.setColor(colorVuelo);
         g2.fillRoundRect(margenLateral, yBarra, wProgreso, alturaBarra, 8, 8);
         
         // Avión
         int xAvion = margenLateral + wProgreso;
         dibujarAvion(g2, xAvion, h / 2, colorVuelo);
         
         // Texto Porcentaje (encima de la barra si hay espacio)
         if (wProgreso > 30) {
             g2.setColor(Color.WHITE);
             g2.setFont(new Font("Arial", Font.BOLD, 9));
             String textoPct = (int)(porcentaje * 100) + "%";
             g2.drawString(textoPct, margenLateral + 5, yBarra + 11);
         }
     }
     
     // ---------------------------------------------------------
     // CASO B: AÚN NO HA SALIDO (Cuenta atrás / Embarque)
     // ---------------------------------------------------------
     else if (!haSalido) {
         String textoEstado;
         Color colorTexto;
         
         if (minutosParaSalida < 60 && minutosParaSalida > 0) {
             // FASE EMBARQUE (Menos de 1 hora): Barra Amarilla/Naranja parcial
             g2.setColor(new Color(241, 196, 15)); // Amarillo
             // Llenamos la barra inversamente (cuanto menos queda, más llena)
             float urgencia = 1.0f - ((float)minutosParaSalida / 60.0f);
             int wUrgencia = (int) (anchoUtil * urgencia);
             g2.fillRoundRect(margenLateral, yBarra, wUrgencia, alturaBarra, 8, 8);
             
             textoEstado = "EMBARCANDO (" + minutosParaSalida + "m)";
             colorTexto = Color.DARK_GRAY;
         } else {
             // FASE ESPERA (Más de 1 hora): Solo texto
             long horas = minutosParaSalida / 60;
             long mins = minutosParaSalida % 60;
             textoEstado = "Salida en " + horas + "h " + mins + "m";
             colorTexto = Color.GRAY;
         }
         
         // Dibujar el texto centrado
         g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
         g2.setColor(colorTexto);
         int textWidth = g2.getFontMetrics().stringWidth(textoEstado);
         g2.drawString(textoEstado, (w - textWidth) / 2, yBarra + 11);
     }

     // ---------------------------------------------------------
     // CASO C: YA LLEGÓ (Fin)
     // ---------------------------------------------------------
     else {
         g2.setColor(new Color(149, 165, 166)); // Gris
         g2.fillRoundRect(margenLateral, yBarra, anchoUtil, alturaBarra, 8, 8);
         
         g2.setColor(Color.WHITE);
         g2.setFont(new Font("Arial", Font.BOLD, 10));
         String fin = "LLEGADA CONFIRMADA";
         int textWidth = g2.getFontMetrics().stringWidth(fin);
         g2.drawString(fin, (w - textWidth) / 2, yBarra + 11);
     }
 }

 private void dibujarAvion(Graphics2D g2, int x, int y, Color color) {
     var t = g2.getTransform();
     g2.translate(x, y);
     g2.setColor(color.darker());
     int[] xp = {-5, 6, -5};
     int[] yp = {-5, 0, 5};
     g2.fillPolygon(xp, yp, 3);
     g2.setTransform(t);
 }
}