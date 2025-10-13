package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import domain.Vuelo;

public class JFrameVuelos extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Vuelo> vuelos;
	
	public JFrameVuelos(ArrayList<Vuelo> vuelos) {
		
		setLayout(new BorderLayout());
		
		// Datos necesarios
		this.vuelos = vuelos;
		
		ArrayList<Vuelo> llegadas = new ArrayList<Vuelo>();
		ArrayList<Vuelo> salidas = new ArrayList<Vuelo>();
		for (Vuelo v: this.vuelos) {
			if (v.getOrigen().getCiudad().equals("Bilbo")) {
				salidas.add(v);
			} else {
				llegadas.add(v);
			}
		}
		
		// Creacion del main panel
		JPanel mainVuelos = new JPanel(new BorderLayout());
		
		
		// Panel Superior
		JPanel panelSuperior = new JPanel();
		panelSuperior.setBackground(new Color(245, 245, 220));
		panelSuperior.setOpaque(true);
		JLabel titu = new JLabel("Vuelos", SwingConstants.CENTER);
        titu.setFont(new Font("Arial", Font.BOLD, 24));
		panelSuperior.add(titu);
		
		mainVuelos.add(panelSuperior, BorderLayout.NORTH);
		
		// Panel Central
		JPanel panelCentral = new JPanel(new GridLayout(1, 2, 5, 5));
		panelCentral.setBorder(new EmptyBorder(10, 30, 30, 30));
		
		JPanel mainLlegadas = creadorTablaVuelos("LLEGADAS", llegadas, true);
		JPanel mainSalidas = creadorTablaVuelos("SALIDAS", salidas, false);
        
		// MAIN
        panelCentral.add(mainLlegadas);
        panelCentral.add(mainSalidas);
        
        
        mainVuelos.add(panelCentral, BorderLayout.CENTER);
		add(mainVuelos);		
	}
	
	private JPanel creadorTablaVuelos(String titulo, ArrayList<Vuelo> vuelos, boolean esLlegada) {
		
		//Panel
		JPanel mainPanel = new JPanel(new BorderLayout());
	     
        // Titulo 
        JLabel tituT = new JLabel(titulo, SwingConstants.CENTER);
        tituT.setFont(new Font("Arial", Font.BOLD, 24));
        
        mainPanel.add(tituT, BorderLayout.NORTH);
           
        // Tabla
        String ae;
        if (esLlegada) {
        	ae = "Origen";
        } else {
        	ae = "Destino";
        }
        String[] columnas = {"Vuelo", ae, "Hora", "Delayed"};
         
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);
        JTable tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
		tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        for(Vuelo v: vuelos) {
        	modelo.addRow(new Object[] {
        			v.getCodigo(),
        			v.getDestino().getCiudad(),
        			v.getFechaHoraProgramada().format(formatter),
        			v.getDelayed()
        	});
        }
     		
        JScrollPane scrollSalidas = new JScrollPane(tabla);
        scrollSalidas.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollSalidas.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        mainPanel.add(scrollSalidas, BorderLayout.CENTER);
        
        Dimension dim = this.getSize();
        tabla.setPreferredScrollableViewportSize(new Dimension(dim.width-50, dim.height));
        
		return mainPanel;
	}
}
