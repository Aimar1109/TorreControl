package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

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
		JPanel panelSuperior = new JPanel(new BorderLayout());
		
		// Titulo VUELOS
		JLabel titu = new JLabel("VUELOS", SwingConstants.CENTER);
        titu.setFont(new Font("Arial", Font.BOLD, 24));
        
        panelSuperior.add(titu, BorderLayout.CENTER);
        
        // Reloj - A la izquierda
        int widthLados = 100;
        JLabel relojLabel = new JLabel("12:34 AM");
        relojLabel.setPreferredSize(new Dimension(widthLados, 0));
        relojLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        relojLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0)); // margen a la izquierda
        
        panelSuperior.add(relojLabel, BorderLayout.WEST);
        
        // Derecha vacio para vuelos centrado
        JLabel vacioD = new JLabel("");
        vacioD.setPreferredSize(new Dimension(widthLados, 0));
        vacioD.setFont(new Font("Arial", Font.PLAIN, 16));
        vacioD.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); // margen a la izquierda
        
        panelSuperior.add(vacioD, BorderLayout.EAST);
        
        // Reloj - timer

		// Ajustar tamaño preferido
		int anchoVentana = mainVuelos.getWidth();
		panelSuperior.setPreferredSize(new Dimension(anchoVentana - 50, 40));
		
		mainVuelos.add(panelSuperior, BorderLayout.NORTH);
		
		// Panel Central
		JPanel panelCentral = new JPanel(new GridLayout(1, 2, 5, 5));
		panelCentral.setBorder(new EmptyBorder(10, 30, 30, 30));
		
		// crear tablas
		JPanel mainLlegadas = creadorTablaVuelos("LLEGADAS", llegadas, true);
		JPanel mainSalidas = creadorTablaVuelos("SALIDAS", salidas, false);
        
		// MAIN
        panelCentral.add(mainLlegadas);
        panelCentral.add(mainSalidas);
        
        
        mainVuelos.add(panelCentral, BorderLayout.CENTER);
		add(mainVuelos);		
	}
	
	private JPanel creadorTablaVuelos(String titulo, ArrayList<Vuelo> vuelos, boolean esLlegada) {
		// Funcion para crear tabla de Vuelos tanto llegadas como salidas
		
		// Formater para que solo aparezca la hora
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		
		// cellRenderer para los Titulos
		TableCellRenderer headerRenderer = (table, value, isSelected, hasFocus, row, column) -> {
			JLabel result = new JLabel(value.toString());			
			result.setHorizontalAlignment(JLabel.CENTER);
			
			switch (value.toString()) {
				case "ORIGEN":
				case "DESTINO":
					result.setHorizontalAlignment(JLabel.LEFT);
			}
			
			result.setBackground(table.getBackground());
			result.setForeground(table.getForeground());
			
			result.setOpaque(true);
			
			result.setFont(new Font("Arial", Font.BOLD, 14));
			
			return result;
		};
		
		// cellRenderer para las Tablas
		TableCellRenderer cellRenderer = (table, value, isSelected, hasFocus, row, column) -> {
			JLabel result = new JLabel(value.toString());
			
			if (column == 0 || column == 2 || column == 3) {
				result.setHorizontalAlignment(JLabel.CENTER);
			}
			
			result.setFont(new Font("Arial", Font.PLAIN, 12));
			
			return result;
		};	
		
		
		//Panel
		JPanel mainPanel = new JPanel(new BorderLayout());
	     
        // Titulo 
        JLabel tituT = new JLabel(titulo, SwingConstants.CENTER);
        tituT.setFont(new Font("Arial", Font.BOLD, 24));
        
        mainPanel.add(tituT, BorderLayout.NORTH);
           
        // Tabla
        String ae;
        if (esLlegada) {
        	ae = "ORIGEN";
        } else {
        	ae = "DESTINO";
        }
        String[] columnas = {"VUELO", ae, "HORA", "RETRASO"};
         
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);
        JTable tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
		
        
        for(Vuelo v: vuelos) {
        	String ciudad = esLlegada ? v.getOrigen().getCiudad() : v.getDestino().getCiudad();
        	modelo.addRow(new Object[] {
        			v.getCodigo(),
        			ciudad,
        			v.getFechaHoraProgramada().format(formatter),
        			v.getDelayed()
        	});
        }
        
        for (int i=0; i<tabla.getColumnCount(); i++) {
        	tabla.getColumnModel().getColumn(i).setMinWidth(80);
        }
        
        // Tamaño minimo de las columnas
        int anchoMinimoTotal = 80 + 80 + 80 + 80; // = 290px
        tabla.setPreferredScrollableViewportSize(new Dimension(anchoMinimoTotal, 0));
     	
        // Crear scroll si es necesario
        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTabla.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Ponerle un component listener a la tabla para que salga el scroll cuando la tabla no entre
        scrollTabla.addComponentListener(new ComponentAdapter() {
        	
        	@Override
        	public void componentResized(ComponentEvent e) {
        		int anchoDisponible = scrollTabla.getViewport().getWidth();
        		if (anchoDisponible >= anchoMinimoTotal) {
                    // Hay suficiente espacio: las columnas se expanden
                    tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                } else {
                    // No hay espacio: mantener tamaños y mostrar scroll
                    tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                }
        	}
        });
        
        // Añadiendo la tabla con scroll al main Panel
        mainPanel.add(scrollTabla, BorderLayout.CENTER);
        
        // Haciendo que las tablas se ajusten al tamaño de la ventana
        Dimension dim = this.getSize();
        tabla.setPreferredScrollableViewportSize(new Dimension(dim.width-50, dim.height));
        
        // No permitir que se ajusten los tamaños ni orden de las columnas
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setResizingAllowed(false);
        
        // Añadiendo los renderer a las tablas
        tabla.getTableHeader().setDefaultRenderer(headerRenderer);
        tabla.setDefaultRenderer(Object.class, cellRenderer);
        
        // Configurando tamaños
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 30)); // 30 píxeles de alto
        
        tabla.setRowHeight(25);
        
        tabla.getColumnModel().getColumn(0).setPreferredWidth(80);  // VUELO
        tabla.getColumnModel().getColumn(1).setPreferredWidth(150); // ORIGEN/DESTINO
        tabla.getColumnModel().getColumn(2).setPreferredWidth(80);  // HORA
        tabla.getColumnModel().getColumn(3).setPreferredWidth(100); // RETRASO
        
		return mainPanel;
	}
}
