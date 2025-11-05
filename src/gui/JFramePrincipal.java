package gui;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import domain.Vuelo;

public class JFramePrincipal extends JFrame {

	private static final long serialVersionUID = 1L;
	private ArrayList<Vuelo> vuelos;
	
	public JFramePrincipal(ArrayList<Vuelo> vuelos) {
		
		// PANEL PRINCIPAL
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		// MENU DE ARRIBA
		JPanel menuPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		
		JButton boton1 = new JButton("Salesman");
		JButton boton2 = new JButton("Vuelos");
		menuPanel.add(boton1);
		menuPanel.add(boton2);
		menuPanel.add(new Button("3"));
		menuPanel.add(new Button("4"));
		
		mainPanel.add(menuPanel, BorderLayout.NORTH);
		

		// PANEL CENTRAL VUELOS
		JPanelVuelos jfvuelos = new JPanelVuelos(vuelos);
	//	mainPanel.add(jfvuelos, BorderLayout.CENTER);
		
		// AGREGAR JPanelSalesman con los vuelos
		JPanelSalesman panelSalesman = new JPanelSalesman(vuelos);
		
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
}