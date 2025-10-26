package gui;
import domain.Vuelo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class JFramePrincipal extends JFrame {

	private static final long serialVersionUID = 1L;

	//Referencias
	private JList<Vuelo> listaVuelosCercanos;
	private JList<Vuelo> listaVuelosPista1;
	private JList<Vuelo> listaVuelosPista2;

	//Y sus respectivos modelos
	private DefaultListModel<Vuelo> modeloVuelosCercanos;
	private DefaultListModel<Vuelo> modeloVuelosPista1;
	private DefaultListModel<Vuelo> modeloVuelosPista2;


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

		//Vuelos Cercanos
		JPanel panelVuelos = crearPanelListaOrigen("Vuelos Cercanos", vuelos);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 2;
		gbc.weightx = 0.4;
		gbc.weighty = 1;
		mainPanel.add(panelVuelos, gbc);

		//Panel Mapa
		MapPanel mapa = new MapPanel();
		mapa.setPreferredSize(new Dimension(1000, 700));

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.6;
		gbc.weighty = 0.7;
		mainPanel.add(mapa, gbc);

		//Pistas
		JPanel panelPistas = new JPanel(new GridLayout(1, 2, 5, 0));
		JPanel pista1 = crearPanelListaPanel1("Aterrizajes Pista 1");
		JPanel pista2 = crearPanelListaPanel2("Aterrizajes Pista 2");
		panelPistas.add(pista1);
		panelPistas.add(pista2);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.75;
		gbc.weighty = 0.3;
		mainPanel.add(panelPistas, gbc);

		//Configuración de la ventana
		configuraciónDrag();

		this.add(mainPanel);

		this.setTitle("Torre de Control");
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		this.setSize(800, 600);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	private JPanel crearPanelListaOrigen(String titulo, List<Vuelo> vuelos) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(titulo));

		modeloVuelosCercanos = new DefaultListModel<>();
		for (Vuelo vuelo : vuelos) {
			modeloVuelosCercanos.addElement(vuelo);
		}

		listaVuelosCercanos = new JList<>(modeloVuelosCercanos);
		listaVuelosCercanos.setCellRenderer(new VueloListRenderer());
		listaVuelosCercanos.setFixedCellHeight(60);

		JScrollPane scrollAviones = new JScrollPane(listaVuelosCercanos);
		panel.add(scrollAviones, BorderLayout.CENTER);

		return panel;
	}

	private JPanel crearPanelListaPanel1(String titulo) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(titulo));

		modeloVuelosPista1 = new DefaultListModel<>();
		listaVuelosPista1 = new JList<>(modeloVuelosPista1);
		listaVuelosPista1.setCellRenderer(new VueloListRenderer());
		listaVuelosPista1.setFixedCellHeight(60);

		JScrollPane scrollAviones = new JScrollPane(listaVuelosPista1);
		panel.add(scrollAviones, BorderLayout.CENTER);

		return panel;
	}

	private JPanel crearPanelListaPanel2(String titulo) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(titulo));

		modeloVuelosPista2 = new DefaultListModel<>();
		listaVuelosPista2 = new JList<>(modeloVuelosPista2);
		listaVuelosPista2.setCellRenderer(new VueloListRenderer());
		listaVuelosPista2.setFixedCellHeight(60);

		JScrollPane scrollAviones = new JScrollPane(listaVuelosPista2);
		panel.add(scrollAviones, BorderLayout.CENTER);

		return panel;
	}

	private void configuraciónDrag() {
		List<JList<Vuelo>> destinos = new ArrayList<>();
		destinos.add(listaVuelosPista1);
		destinos.add(listaVuelosPista2);

		PistasDragListener listener = new PistasDragListener(listaVuelosCercanos, destinos, this);

		listaVuelosCercanos.addMouseListener(listener);
		listaVuelosCercanos.addMouseMotionListener(listener);
	}
}