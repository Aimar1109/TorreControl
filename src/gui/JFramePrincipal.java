package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;

import domain.Aerolinea;
import domain.Aeropuerto;
import domain.Avion;
import domain.PaletaColor;
import domain.PuertaEmbarque;
import domain.Vuelo;
import jdbc.GestorBD;

public class JFramePrincipal extends JFrame {
    private static final long serialVersionUID = 1L;

    private ArrayList<Vuelo> vuelos;
    private ButtonGroup buttonGroup;

    private GestorBD gestorBD;

    public JFramePrincipal(GestorBD gestorBD) {

        this.gestorBD = gestorBD;
        // PANEL PRINCIPAL
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        Image icon = new ImageIcon("resources/img/torre-de-control.png").getImage();
        setIconImage(icon);

        // MENU DE ARRIBA
        JPanel menuPanel = new JPanel(new GridLayout(1, 4, 5, 5));

        JToggleButton boton1 = new JToggleButton("Principal");
        JToggleButton boton2 = new JToggleButton("Salesman");
        JToggleButton boton3 = new JToggleButton("Vuelos");
        JToggleButton boton4 = new JToggleButton("Clima");

        // Estilizar botones
        estilizarBotonToggle(boton1);
        estilizarBotonToggle(boton2);
        estilizarBotonToggle(boton3);
        estilizarBotonToggle(boton4);

        // Agrupar botones
        buttonGroup = new ButtonGroup();
        buttonGroup.add(boton1);
        buttonGroup.add(boton2);
        buttonGroup.add(boton3);
        buttonGroup.add(boton4);

        menuPanel.add(boton1);
        menuPanel.add(boton2);
        menuPanel.add(boton3);
        menuPanel.add(boton4);

        mainPanel.add(menuPanel, BorderLayout.NORTH);

        ArrayList<Vuelo> vuelos = (ArrayList<Vuelo>) gestorBD.loadVuelos();

        // PANEL CENTRAL VUELOS
        JPanelVuelos jpvuelos = new JPanelVuelos(gestorBD, vuelos);

        // AGREGAR JPanelSalesman con los vuelos
        JPanelSalesman panelSalesman = new JPanelSalesman(gestorBD, vuelos);

        //
        JPanelPrincipal jpPrincipal = new JPanelPrincipal(gestorBD, vuelos);


        JPanelClima panelClima = new JPanelClima();


        // Seleccionar por defecto el botón Salesman
        boton1.setSelected(true);
        mainPanel.add(jpPrincipal, BorderLayout.CENTER);


        boton1.addActionListener(e -> {
            Component center = ((BorderLayout) mainPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (center != null) {
                mainPanel.remove(center);
            }
            mainPanel.add(jpPrincipal, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        });

        boton2.addActionListener(e -> {
            Component center = ((BorderLayout) mainPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (center != null) {
                mainPanel.remove(center);
            }
            mainPanel.add(panelSalesman, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        });

        boton3.addActionListener(e -> {
            Component center = ((BorderLayout) mainPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (center != null) {
                mainPanel.remove(center);
            }
            mainPanel.add(jpvuelos, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        });

        boton4.addActionListener(e -> {
            Component center = ((BorderLayout) mainPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (center != null) {
                mainPanel.remove(center);
            }
            mainPanel.add(panelClima, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        });

        // CONFIGURACION DE LA VENTANA
        this.add(mainPanel);


        this.setTitle("Torre de Control");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1200, 800);

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void estilizarBotonToggle(JToggleButton boton) {
        boton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        boton.setPreferredSize(new Dimension(80, 35));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(true);
        boton.setOpaque(true);

        Color bgNormal = PaletaColor.get(PaletaColor.FILA_ALT);
        Color bgHover = new Color(230, 240, 250);
        Color bgSelected = Color.WHITE;

        Color fgNormal = Color.GRAY;
        Color fgSelected = PaletaColor.get(PaletaColor.PRIMARIO);

        boton.setBackground(bgNormal);
        boton.setForeground(fgNormal);

        boton.addItemListener(e -> {
            if (boton.isSelected()) {
                boton.setBackground(bgSelected);
                boton.setForeground(fgSelected);
            } else {
                boton.setBackground(bgNormal);
                boton.setForeground(fgNormal);
            }
        });

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!boton.isSelected()) {
                    boton.setBackground(bgHover);
                    boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!boton.isSelected()) {
                    boton.setBackground(bgNormal);
                }
                boton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }
}