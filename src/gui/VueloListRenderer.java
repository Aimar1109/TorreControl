package gui;

import domain.Vuelo;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VueloListRenderer extends JPanel implements ListCellRenderer<Vuelo> {
    private JLabel cod;
    private JLabel or_des;
    private JLabel info;
    private JLabel llegada;

    public VueloListRenderer() {
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        setOpaque(true);

        //Label Código
        cod = new JLabel();
        cod.setFont(new Font("Arial", Font.BOLD, 13));
        cod.setOpaque(false);
        cod.setPreferredSize(new Dimension(50, 50));
        add(cod, BorderLayout.WEST);

        //Panel Central
        JPanel panelCentral = new JPanel(new GridLayout(2, 1, 0, 2));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        panelCentral.setOpaque(false);

        //Label origen y destino
        or_des = new JLabel();
        or_des.setFont(new Font("Arial", Font.BOLD, 13));
        or_des.setOpaque(false);
        or_des.setVerticalAlignment(SwingConstants.CENTER);
        panelCentral.add(or_des);

        //Label info
        info = new JLabel();
        info.setFont(new Font("Arial", Font.BOLD, 13));
        info.setOpaque(false);
        info.setVerticalAlignment(SwingConstants.CENTER);
        panelCentral.add(info);

        add(panelCentral, BorderLayout.CENTER);

        //Label Estado
        llegada = new JLabel();
        llegada.setOpaque(false);
        llegada.setFont(new Font("Arial", Font.BOLD, 13));
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
            cod.setText("" + value.getCodigo());

            //Información
            String ciudadOrigen = value.getOrigen().getCiudad();
            String ciudadDestino = value.getDestino().getCiudad();
            info.setText(ciudadOrigen + " → " + ciudadDestino);

            //Llegada
            LocalDateTime intermedio = value.getFechaHoraProgramada();
            LocalDateTime real = intermedio.plusMinutes(value.getDelayed());
            String textoLabel = real.format(DateTimeFormatter.ofPattern("HH:mm"));
            llegada.setText(textoLabel);
        }

        //Colores
        cod.setForeground(Color.BLACK);
        or_des.setForeground(Color.BLACK);
        info.setForeground(Color.BLACK);
        llegada.setForeground(Color.BLACK);

        if (isSelected) {
            setBackground(new Color(173, 216, 230));
        }
        else {
            if (value.getDelayed() > 0) {
                setBackground(new Color(255, 248, 228));
            } else if (value.isEmergencia()) {
                setBackground(new Color(255, 240, 240));
            } else {
                setBackground(new Color(240, 248, 255));
            }
        }

        return this;
    }
}