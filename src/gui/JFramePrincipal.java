package gui;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import domain.Vuelo;

public class JFramePrincipal extends JFrame {

	private static final long serialVersionUID = 1L;
	private ArrayList<Vuelo> vuelos;
	
	public JFramePrincipal(ArrayList<Vuelo> vuelos) {
		this.vuelos = vuelos;
		
		// PANEL PRINCIPAL
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		// MENU DE ARRIBA
		JPanel menuPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		menuPanel.add(new Button("1"));
		menuPanel.add(new Button("2"));
		menuPanel.add(new Button("3"));
		menuPanel.add(new Button("4"));
		
		mainPanel.add(menuPanel, BorderLayout.NORTH);
		
		// AGREGAR JPanelSalesman con los vuelos
		JPanelSalesman panelSalesman = new JPanelSalesman(vuelos);
		mainPanel.add(panelSalesman, BorderLayout.CENTER);
		
		// CONFIGURACION DE LA VENTANA
		this.add(mainPanel);
		
		this.setTitle("Torre de Control");		
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		this.setSize(1200, 800);
		this.setLocationRelativeTo(null);
		this.setVisible(true);	
	}
}