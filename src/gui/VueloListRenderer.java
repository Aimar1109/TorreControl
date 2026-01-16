package gui;

import domain.PaletaColor;
import domain.Vuelo;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VueloListRenderer extends JPanel implements ListCellRenderer<Vuelo> {
    
	
	private static final long serialVersionUID = 1L;
	
	private JLabel cod;
    private JLabel or_des;
    private JLabel info;
    private JLabel llegada;

    public VueloListRenderer() {
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        setOpaque(true);

        //Label Estado
        cod = new JLabel();
        cod.setFont(new Font("Arial", Font.PLAIN, 13));
        cod.setOpaque(false);
        cod.setPreferredSize(new Dimension(50, 50));
        cod.setHorizontalAlignment(SwingConstants.LEFT);
        add(cod, BorderLayout.WEST);

        //Panel Central
        JPanel panelCentral = new JPanel(new GridLayout(2, 1, 0, 2));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        panelCentral.setOpaque(false);

        //Label origen y destino
        or_des = new JLabel();
        or_des.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        or_des.setOpaque(false);
        or_des.setVerticalAlignment(SwingConstants.CENTER);
        panelCentral.add(or_des);

        //Label info
        info = new JLabel();
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info.setOpaque(false);
        info.setVerticalAlignment(SwingConstants.CENTER);
        panelCentral.add(info);

        add(panelCentral, BorderLayout.CENTER);

        //Label Llegada
        llegada = new JLabel();
        llegada.setOpaque(false);
        llegada.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        llegada.setHorizontalAlignment(SwingConstants.RIGHT);
        add(llegada, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Vuelo> list, Vuelo value, int index, boolean isSelected, boolean cellHasFocus) {

        if (value != null) {
            //Ruta: origen-destino
            String origen = value.getOrigen().getNombre();
            String destino = value.getDestino().getNombre();
            or_des.setText(origen + " → " + destino);

            //Codigo
            cod.setText(value.getCodigo());

            //Información
            String ciudadOrigen = value.getOrigen().getCiudad();
            String ciudadDestino = value.getDestino().getCiudad();
            info.setText(ciudadOrigen + " → " + ciudadDestino);

            //Llegada
             
            LocalDateTime real = value.getFechaHoraProgramada().plusMinutes((long) value.getDelayed());
            
            if (ciudadDestino.equals("Bilbao")) {
            	
            	real = value.getFechaHoraProgramada().plusMinutes((long) (value.getDelayed()+value.getDuracion()));
            	
            }
            String textoLabel = real.format(DateTimeFormatter.ofPattern("HH:mm"));
            llegada.setText(textoLabel);
        }

        //Colores
        cod.setForeground(PaletaColor.get(PaletaColor.TEXTO));
        or_des.setForeground(PaletaColor.get(PaletaColor.TEXTO));
        info.setForeground(PaletaColor.get(PaletaColor.TEXTO));
        llegada.setForeground(PaletaColor.get(PaletaColor.TEXTO));

        Color zebra;
        if (index % 2 == 0) {
            zebra = PaletaColor.get(PaletaColor.BLANCO);
        } else {
            zebra = PaletaColor.get(PaletaColor.FILA_ALT);
        }
        setBackground(zebra);
        cod.setFont(cod.getFont().deriveFont(Font.PLAIN));
        or_des.setFont(cod.getFont().deriveFont(Font.PLAIN));
        info.setFont(cod.getFont().deriveFont(Font.PLAIN));
        llegada.setFont(cod.getFont().deriveFont(Font.PLAIN));

        if (!isSelected) {
            if (value != null && value.getDelayed() > 0) {
                llegada.setForeground(PaletaColor.get(PaletaColor.DELAYED));
            }
        }

        return this;
    }
}