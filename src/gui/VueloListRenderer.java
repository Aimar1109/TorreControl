package gui;

import domain.Vuelo;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class VueloListRenderer extends JPanel implements ListCellRenderer<Vuelo> {
    private JLabel cod;
    private JLabel or_des;
    private JLabel info;
    private JLabel estado;

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

        //Label Información
        info = new JLabel();
        info.setFont(new Font("Arial", Font.BOLD, 13));
        info.setOpaque(false);
        info.setVerticalAlignment(SwingConstants.CENTER);
        panelCentral.add(info);

        add(panelCentral, BorderLayout.CENTER);

        //Label Estado
        estado = new JLabel();
        estado.setOpaque(false);
        estado.setFont(new Font("Arial", Font.BOLD, 13));
        add(estado, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Vuelo> list, Vuelo value, int index, boolean isSelected, boolean cellHasFocus) {

        if (value != null) {
            //Ruta: origen-destino
            String origen = value.getOrigen().getNombre();
            String destino = value.getDestino().getNombre();
            or_des.setText(origen + " → " + destino);

            //Codigo
            cod.setText("" + value.getcodigo());

            //Información
            String ciudadOrigen = value.getOrigen().getCiudad();
            String ciudadDestino = value.getOrigen().getCiudad();
            info.setText(ciudadOrigen + " → " + ciudadDestino);

            //Estado
            if (value.isEmergencia()) {
                estado.setText("⚠ EMERGENCIA");
                estado.setForeground(new Color(238, 75, 43));
            } else if (value.getDelayed() > 0) {
                estado.setText("⏱ " + value.getDelayed() + " min");
                estado.setForeground(new Color(225, 150, 0));
            } else {
                estado.setText("✔ A tiempo");
                estado.setForeground(new Color(147, 197, 114));
            }
        }

        //Colores
        if (isSelected) {
            setBackground(new Color(220,220,220));
            cod.setForeground(Color.BLACK);
            or_des.setForeground(Color.BLACK);
            info.setForeground(Color.BLACK);
        } else {
            setBackground(Color.WHITE);
            cod.setForeground(Color.BLACK);
            or_des.setForeground(Color.BLACK);
            info.setForeground(Color.BLACK);
        }

        return this;
    }
}