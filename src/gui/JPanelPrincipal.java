package gui;

import domain.Avion;
import domain.Vuelo;

import java.util.ArrayList;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class JPanelPrincipal extends JPanel {
	//Referencias
	private JList<Vuelo> listaVuelosCercanos;
	private JList<Vuelo> listaVuelosPista1;
	private JList<Vuelo> listaVuelosPista2;
	private MapPanel mapa;

	//Y sus respectivos modelos
	private DefaultListModel<Vuelo> modeloVuelosCercanos;
	private DefaultListModel<Vuelo> modeloVuelosPista1;
	private DefaultListModel<Vuelo> modeloVuelosPista2;


	public JPanelPrincipal(ArrayList<Vuelo> vuelos) {
		this(vuelos, new ArrayList<>());
	}

	public JPanelPrincipal(ArrayList<Vuelo> vuelos, List<Avion> aviones) {

		//Panel Principal
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		//Configuración Común
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5,5);

		//Vuelos Cercanos
		JPanel panelVuelos = crearPanelListaOrigen("Vuelos Cercanos", vuelos);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 2;
		gbc.weightx = 0.4;
		gbc.weighty = 1;
		add(panelVuelos, gbc);

		//Panel Mapa
		mapa = new MapPanel();
		mapa.setPreferredSize(new Dimension(1000, 700));
		mapa.setAviones(aviones);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.6;
		gbc.weighty = 0.7;
		add(mapa, gbc);

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
		add(panelPistas, gbc);

		//Configuración de la ventana
		configuraciónDraglistaVuelosCercanos();
		configuraciónDraglistaVuelos1();
		configuraciónDraglistaVuelos2();
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

	private void configuraciónDraglistaVuelosCercanos() {
		List<JList<Vuelo>> destinos = new ArrayList<>();
		destinos.add(listaVuelosPista1);
		destinos.add(listaVuelosPista2);

		PistasDragListener listener = new PistasDragListener(listaVuelosCercanos, destinos);

		listaVuelosCercanos.addMouseListener(listener);
		listaVuelosCercanos.addMouseMotionListener(listener);
	}

	private void configuraciónDraglistaVuelos1() {
		List<JList<Vuelo>> destinos = new ArrayList<>();
		destinos.add(listaVuelosCercanos);
		destinos.add(listaVuelosPista2);

		PistasDragListener listener = new PistasDragListener(listaVuelosPista1, destinos);

		listaVuelosPista1.addMouseListener(listener);
		listaVuelosPista1.addMouseMotionListener(listener);
	}

	private void configuraciónDraglistaVuelos2() {
		List<JList<Vuelo>> destinos = new ArrayList<>();
		destinos.add(listaVuelosPista1);
		destinos.add(listaVuelosCercanos);

		PistasDragListener listener = new PistasDragListener(listaVuelosPista2, destinos);

		listaVuelosPista2.addMouseListener(listener);
		listaVuelosPista2.addMouseMotionListener(listener);
	}

	public MapPanel getMapa() {
		return mapa;
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