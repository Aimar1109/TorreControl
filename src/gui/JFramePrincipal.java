package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;
import domain.Vuelo;

public class JFramePrincipal extends JFrame {
	private static final long serialVersionUID = 1L;
	private ArrayList<Vuelo> vuelos;
	private ButtonGroup buttonGroup;
	
	public JFramePrincipal(ArrayList<Vuelo> vuelos) {
		
		// PANEL PRINCIPAL
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		// MENU DE ARRIBA
		JPanel menuPanel = new JPanel(new GridLayout(1, 4, 5, 5));
		
		JToggleButton boton1 = new JToggleButton("Salesman");
		JToggleButton boton2 = new JToggleButton("Vuelos");
		JToggleButton boton3 = new JToggleButton("Clima");
		JToggleButton boton4 = new JToggleButton("Principal");
		
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
		
		// PANEL CENTRAL VUELOS
		JPanelVuelos jfvuelos = new JPanelVuelos(vuelos);
		
		// AGREGAR JPanelSalesman con los vuelos
		JPanelSalesman panelSalesman = new JPanelSalesman(vuelos);
		
		// Seleccionar por defecto el botón Salesman
		boton1.setSelected(true);
		mainPanel.add(panelSalesman, BorderLayout.CENTER);
		
		boton1.addActionListener(e -> {
			Component center = ((BorderLayout) mainPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
			if (center != null) {
			    mainPanel.remove(center);
			}
			mainPanel.add(panelSalesman, BorderLayout.CENTER);
			mainPanel.revalidate();
			mainPanel.repaint();
		});
		
		boton2.addActionListener(e -> {
			Component center = ((BorderLayout) mainPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
			if (center != null) {
			    mainPanel.remove(center);
			}
			mainPanel.add(jfvuelos, BorderLayout.CENTER);
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
		boton.setFont(new Font("Arial", Font.BOLD, 12));
		boton.setFocusPainted(false);
		boton.setBorderPainted(true);
		boton.setBackground(new Color(240, 240, 240));
		boton.setForeground(Color.BLACK);
		boton.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
			BorderFactory.createEmptyBorder(8, 15, 8, 15)
		));

		// Cambiar apariencia cuando está seleccionado
		boton.addItemListener(e -> {
			if (boton.isSelected()) {
				boton.setBackground(new Color(70, 130, 180)); // Azul
				boton.setForeground(Color.WHITE);
			} else {
				boton.setBackground(new Color(240, 240, 240));
				boton.setForeground(Color.BLACK);
			}
		});
	}
}