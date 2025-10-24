package gui;
import domain.Vuelo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class JFramePrincipal extends JFrame {

	private static final long serialVersionUID = 1L;
	
	public JFramePrincipal(ArrayList<Vuelo> vuelos) {
		
		//Panel Principal
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		//Configuración Común
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5,5);
		
		//Menu Pantallas
		JPanel menuPanel = new JPanel(new GridLayout(1, 4, 5, 5));
		menuPanel.add(new Button("1"));
		menuPanel.add(new Button("2"));
		menuPanel.add(new Button("3"));
		menuPanel.add(new Button("4"));
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		mainPanel.add(menuPanel, gbc);

		//Panel Mapa
		MapPanel mapa = new MapPanel();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.7;
		gbc.weighty = 0.8;
		mainPanel.add(mapa, gbc);

		//Pistas
		JPanel panelPistas = new JPanel(new GridLayout(1, 2, 5, 0));
		JPanel pista1 = crearPanelLista("Aterrizajes Pista 1", new ArrayList<>());
		JPanel pista2 = crearPanelLista("Aterrizajes Pista 2", new ArrayList<>());
		panelPistas.add(pista1);
		panelPistas.add(pista2);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.6;
		gbc.weighty = 0.2;
		mainPanel.add(panelPistas, gbc);


		//Vuelos Cercanos
		JPanel panelVuelos = crearPanelLista("Vuelos Cercanos", vuelos);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 2;
		gbc.weightx = 0.3;
		gbc.weighty = 1;
		mainPanel.add(panelVuelos, gbc);


		//Configuración de la ventana
		this.add(mainPanel);
		
		this.setTitle("Torre de Control");		
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		this.setSize(800, 600);
		this.setLocationRelativeTo(null);
		this.setVisible(true);	
	}

	private JPanel crearPanelLista(String titulo, List<Vuelo> vuelos) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(titulo));

		Vuelo[] arrayVuelos = vuelos.toArray(new Vuelo[0]);

		JList lista = new JList<>(arrayVuelos);
		lista.setCellRenderer(new VueloListRenderer());
		lista.setFixedCellHeight(60);
		JScrollPane scrollAviones = new JScrollPane(lista);
		panel.add(scrollAviones, BorderLayout.CENTER);

		return panel;
	}
}