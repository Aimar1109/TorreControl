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
		
		// LLEGADAS
		JPanel mainLlegadas = new JPanel(new BorderLayout());
		
		// titulo llegadas
		JLabel tituLlegadas = new JLabel("Llegadas", SwingConstants.CENTER);
		tituLlegadas.setFont(new Font("Arial", Font.BOLD, 24));
        
        mainLlegadas.add(tituLlegadas, BorderLayout.NORTH);
        
        // tabla llegadas
        String[] columnasLlegadas = {"Vuelo", "Origen", "Hora", "Delayed"};
        
        DefaultTableModel modeloLlegadas = new DefaultTableModel(columnasLlegadas, 0);
        JTable tablaLlegadas = new JTable(modeloLlegadas);
        tablaLlegadas.setFillsViewportHeight(true);
        tablaLlegadas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        for(Vuelo v: llegadas) {
        	modeloLlegadas.addRow(new Object[] {
        			v.getCodigo(),
        			v.getOrigen().getCiudad(),
        			v.getFechaHoraProgramada().format(formatter),
        			v.getDelayed()
        	});
        }
		
        JScrollPane scrollLlegadas = new JScrollPane(tablaLlegadas);
        scrollLlegadas.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollLlegadas.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        mainLlegadas.add(scrollLlegadas, BorderLayout.CENTER);
		
        // SALIDAS
        JPanel mainSalidas = new JPanel(new BorderLayout());
     
        // titulo Salidas
        JLabel tituSalidas = new JLabel("Salidas", SwingConstants.CENTER);
        tituSalidas.setFont(new Font("Arial", Font.BOLD, 24));
        
        mainSalidas.add(tituSalidas, BorderLayout.NORTH);
           
        // tabla Salidas
        String[] columnasSalidas = {"Vuelo", "Destino", "Hora", "Delayed"};
         
        DefaultTableModel modeloSalidas = new DefaultTableModel(columnasSalidas, 0);
        JTable tablaSalidas = new JTable(modeloSalidas);
        tablaSalidas.setFillsViewportHeight(true);
		tablaSalidas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        for(Vuelo v: salidas) {
        	modeloSalidas.addRow(new Object[] {
        			v.getCodigo(),
        			v.getDestino().getCiudad(),
        			v.getFechaHoraProgramada().format(formatter),
        			v.getDelayed()
        	});
        }
     		
        JScrollPane scrollSalidas = new JScrollPane(tablaSalidas);
        scrollSalidas.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollSalidas.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        mainSalidas.add(scrollSalidas, BorderLayout.CENTER);
        
        Dimension dim = this.getSize();
        tablaLlegadas.setPreferredScrollableViewportSize(new Dimension(dim.width-50, dim.height));
        tablaSalidas.setPreferredScrollableViewportSize(new Dimension(dim.width-50, dim.height));
        
		// main
        panelCentral.add(mainLlegadas);
        panelCentral.add(mainSalidas);
        
        
        mainVuelos.add(panelCentral, BorderLayout.CENTER);
		add(mainVuelos);		
	}
}
