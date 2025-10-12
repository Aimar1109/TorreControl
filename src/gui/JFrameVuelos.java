package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import domain.Vuelo;

public class JFrameVuelos extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Vuelo> vuelos;
	
	public JFrameVuelos(ArrayList<Vuelo> vuelos) {
		
		// Datos necesarios
		this.vuelos = vuelos;
		
		// Creacion del main panel
		JPanel mainVuelos = new JPanel(new BorderLayout());
		
		
		// Panel Superior
		JPanel panelSuperior = new JPanel();
		panelSuperior.setBackground(new Color(240, 240, 240));
		panelSuperior.setOpaque(true);
		JLabel titu = new JLabel("Vuelos", SwingConstants.CENTER);
        titu.setFont(new Font("Arial", Font.BOLD, 24));
		panelSuperior.add(titu);
		
		mainVuelos.add(panelSuperior, BorderLayout.NORTH);
		
		// Panel Central
		JPanel panelCentral = new JPanel(new GridLayout(1, 2, 5, 5));
		
		// Prueba manual
		String[] columnas = {"ID", "Vuelo", "Origen", "Destino", "Estado"};
        Object[][] datos = {
            {1, "IB123", "Madrid", "Bilbao", "En vuelo"},
            {2, "VY456", "Barcelona", "Sevilla", "Aterrizado"},
            {3, "UX789", "Bilbao", "Londres", "Retrasado"}
        };

        // Crear la tabla
        JTable tabla = new JTable(datos, columnas);
        JTable tabla1 = new JTable(datos, columnas);

        // Añadirla dentro de un JScrollPane para permitir scroll
        JScrollPane scroll = new JScrollPane(tabla);
        JScrollPane scroll1 = new JScrollPane(tabla1);

        // Añadir el scroll al panel
        panelCentral.add(scroll);
        panelCentral.add(scroll1);
        
        mainVuelos.add(panelCentral, BorderLayout.CENTER);
		
		
		add(mainVuelos);		
	}
}
