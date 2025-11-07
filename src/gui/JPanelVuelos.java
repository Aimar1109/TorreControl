package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableModel;

import com.toedter.calendar.JDateChooser;

import domain.Aerolinea;
import domain.Aeropuerto;
import domain.Avion;
import domain.PuertaEmbarque;
import domain.Vuelo;
import main.Main.VueloGenerador;

public class JPanelVuelos extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Vuelo> vuelos;
	private JDialogNVuelo dialogNVuelo;
	private JPanel panelVuelos = this;
	
	public JPanelVuelos(VueloGenerador vg, ArrayList<Aeropuerto> aeropuertos, ArrayList<Aerolinea> aers, ArrayList<Avion> avs,
						ArrayList<PuertaEmbarque> puertas) {
		
		setLayout(new BorderLayout());
		
		// Datos necesarios
		this.vuelos = new ArrayList<Vuelo>(vg.devolverA());
		
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
		JPanel mainLlegadas = creadorTablaVuelos("LLEGADAS", vuelos, true, aeropuertos, aers, avs, puertas, vg);
		JPanel mainSalidas = creadorTablaVuelos("SALIDAS", vuelos, false, aeropuertos, aers, avs, puertas, vg);
        
		// MAIN
        panelCentral.add(mainLlegadas);
        panelCentral.add(mainSalidas);
        
        
        mainVuelos.add(panelCentral, BorderLayout.CENTER);
		add(mainVuelos);		
	}
	
	private JPanel creadorTablaVuelos(String titulo, ArrayList<Vuelo> vuelos, boolean esLlegada, ArrayList<Aeropuerto> aeropuertos,
									  ArrayList<Aerolinea> aers, ArrayList<Avion> avs, ArrayList<PuertaEmbarque> puertas, VueloGenerador vg) {
		// Funcion para crear tabla de Vuelos tanto llegadas como salidas
		
		
		// Formater para que solo aparezca la hora
		DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("dd:MM:yyyy");
		DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm");
		
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
		
		
		//Panel de la tabla
		JPanel mainPanel = new JPanel(new BorderLayout());
	     
        // Panel superior de la tabla
		JPanel tablaPSuperior = new JPanel(new BorderLayout());
		
		// JPanel para titulos y imagenes parte de arriba
		JPanel panelSdeTPS = new JPanel(new BorderLayout());
		
		// Titulo Parte Izquierda
        JLabel tituT = new JLabel(titulo, SwingConstants.LEFT);
        tituT.setFont(new Font("Arial", Font.BOLD, 24));
        tituT.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        panelSdeTPS.add(tituT, BorderLayout.WEST);
        
        // PANEL DE FILTROS (inicialmente oculto)
        JPanel panelFiltros = new JPanel(new GridLayout(1, 4, 10, 10));
        panelFiltros.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelFiltros.setVisible(false);
        
        // Campos filtro
        
        // Filtro vuelo
        JTextField txtFiltroVuelo = new JTextField();
        txtFiltroVuelo.setBorder(BorderFactory.createTitledBorder("VUELO"));
        panelFiltros.add(txtFiltroVuelo);
        
        // Filtro para Destino/Origen
        JTextField txtFiltroDO = new JTextField(); // Luego sera JCombobox
        String tituloFDO = esLlegada ? "ORIGEN": "DESTINO";
        txtFiltroDO.setBorder(BorderFactory.createTitledBorder(tituloFDO));
        panelFiltros.add(txtFiltroDO);
        
        // Filtro Fecha
        JPanel panelFecha = new JPanel(new BorderLayout());
        // JDateChooser para fecha
        JDateChooser dateChooserFiltro = new JDateChooser();
        dateChooserFiltro.setDateFormatString("dd/MM/yyyy"); // formato de fecha
        panelFecha.add(dateChooserFiltro, BorderLayout.CENTER);
        // Boton para borrar
        ImageIcon xIcon = new ImageIcon("resources\\img\\x.png");
        Image xImg = xIcon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
        JLabel xLabel = new JLabel(new ImageIcon(xImg));
        panelFecha.add(xLabel, BorderLayout.EAST);
        panelFiltros.add(panelFecha);
        
        // Filtro Hora
        JPanel panelHora = new JPanel(new BorderLayout());
        // CheckBox para hora
        JCheckBox chkFiltroHora = new JCheckBox();
        chkFiltroHora.setFont(new Font("Arial", Font.BOLD, 10));
        panelHora.add(chkFiltroHora, BorderLayout.WEST);
        // Spinner para la hora
        JSpinner spinnerFiltroHora = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorFiltroHora = new JSpinner.DateEditor(spinnerFiltroHora, "HH:mm");
        spinnerFiltroHora.setEditor(editorFiltroHora);
        spinnerFiltroHora.setEnabled(false);
        
        chkFiltroHora.addActionListener(e -> {
        	spinnerFiltroHora.setEnabled(chkFiltroHora.isSelected());
        });
        
        panelHora.add(spinnerFiltroHora, BorderLayout.CENTER);
        
        panelFiltros.add(panelHora);
        
        // Tabla
        String ae = esLlegada ? "ORIGEN": "DESTINO";
        String[] columnas = {"VUELO", ae, "FECHA", "HORA", "RETRASO"};
         
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
        	@Override
            public Class<?> getColumnClass(int columnIndex) { //IAG
                // Importante para que el sorter sepa qué tipo de dato es
                if (columnIndex == 2) return LocalDate.class;
                if (columnIndex == 3) return LocalTime.class;
                return String.class;
            }
        };
        
        JTable tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
		
        
        for(Vuelo v: vuelos) {
        	if (esLlegada && v.getOrigen().getCiudad().equals("Bilbao")) {
        		continue;
        	} else if (!esLlegada && v.getDestino().getCiudad().equals("Bilbao")) {
        		continue;
        	}
        	String ciudad = esLlegada ? v.getOrigen().getCiudad() : v.getDestino().getCiudad();
        	modelo.addRow(new Object[] {
        			v.getCodigo(),
        			ciudad,
        			v.getFechaHoraProgramada().format(formatterFecha),
        			v.getFechaHoraProgramada().format(formatterHora),
        			v.getDelayed()
        	});
        }
        
        // Creamos el sorter -- IAG
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        // Comparadores personalizados para cada columna
        sorter.setComparator(2, Comparator.naturalOrder()); // Fecha
        sorter.setComparator(3, Comparator.naturalOrder()); // Hora

        // Ordenar automáticamente por fecha (columna 0) y luego por hora (columna 1)
        List<RowSorter.SortKey> clavesOrden = new ArrayList<>();
        clavesOrden.add(new RowSorter.SortKey(2, SortOrder.ASCENDING)); // Fecha ascendente
        clavesOrden.add(new RowSorter.SortKey(3, SortOrder.ASCENDING)); // Hora ascendente
        sorter.setSortKeys(clavesOrden);
        sorter.sort(); // ¡Ordena automáticamente!
        
        // Tamaño minimo de las columnas
        int anchoMinimoTotal = 80*tabla.getModel().getColumnCount();
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
        
        // Imagenes Parte Derecha
        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        
        // cargar imagenes
        ImageIcon lupaIcon = new ImageIcon("resources\\img\\lupa.png");
        ImageIcon plusIcon = new ImageIcon("resources\\img\\plus.png");
        Image lupaImg = lupaIcon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
        Image plusImg = plusIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        
        // Metdo para filtrar la tabla
        Runnable aplicarFiltros = () -> {
        	String filtroVueloC = txtFiltroVuelo.getText().trim();
        	String filtroDO = txtFiltroDO.getText().toLowerCase().trim();
        	
        	// Limpiar la tabla
        	modelo.setRowCount(0);
        	
        	// Filtrar y agregar filar
        	for(Vuelo v: vuelos) {
        		String ciudad = (esLlegada ? v.getOrigen().getCiudad() : v.getDestino().getCiudad());
        		String codigo = v.getCodigo();
        		LocalDateTime fechaHora = v.getFechaHoraProgramada();
        
        		boolean coincide = true;
        		if (!filtroVueloC.isBlank() && !v.getCodigo().contains(filtroVueloC)) {
        			coincide = false;
        		}
        		if (!filtroDO.isEmpty() && !(ciudad.toLowerCase()).contains(filtroDO.toLowerCase())) {
        			coincide = false;
        		}
        		if (dateChooserFiltro.getDate() != null && !fechaHora.toLocalDate().equals(dateChooseraLocalDate(dateChooserFiltro))) {
        			coincide = false;
        		}
        		if (chkFiltroHora.isSelected() && !(fechaHora.toLocalTime().withSecond(0).withNano(0)).equals(spinnerToLocalTime(spinnerFiltroHora))) {
        			coincide = false;
        		}
        		
        		if (coincide) {
        			modelo.addRow(new Object[] {
                			v.getCodigo(),
                			ciudad,
                			v.getFechaHoraProgramada().format(formatterFecha),
                			v.getFechaHoraProgramada().format(formatterHora),
                			v.getDelayed()
                	});
        		}
        		
        	}
        };
        
        // Aplicar filtro cuando haya cambios en cualquier filtro
        txtFiltroVuelo.addActionListener(e -> aplicarFiltros.run());
        txtFiltroDO.addActionListener(e -> aplicarFiltros.run());
        dateChooserFiltro.getDateEditor().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if("date".equals(evt.getPropertyName())) {
					aplicarFiltros.run();
				}
			}
		});
        xLabel.addMouseListener(new MouseAdapter() {
        	
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		// Borrar fecha de JDateChooser
        		dateChooserFiltro.setDate(null);
        		aplicarFiltros.run();
                
        	}
        });
        chkFiltroHora.addActionListener(e -> aplicarFiltros.run());
        
        // Para que vaya aplicando los filtros mientras se escribe
        DocumentListener filtroListener = new DocumentListener() {
        	public void changedUpdate(DocumentEvent e) {aplicarFiltros.run();}
        	public void removeUpdate(DocumentEvent e) {aplicarFiltros.run();}
        	public void insertUpdate(DocumentEvent e) {aplicarFiltros.run();}
        	
        };
        
        txtFiltroVuelo.getDocument().addDocumentListener(filtroListener);
        txtFiltroDO.getDocument().addDocumentListener(filtroListener);
        
        // Lupa Label
        JLabel lupaLabel = new JLabel(new ImageIcon(lupaImg));
        // Añadiendo un mouse listener para filtrar
        lupaLabel.addMouseListener(new MouseAdapter() {
        	
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		// Alternar visibilidad del panel de filtros
                panelFiltros.setVisible(!panelFiltros.isVisible());
                
                // Si se oculta, limpiar filtros y restaurar tabla
                if (!panelFiltros.isVisible()) {
                    txtFiltroVuelo.setText("");
                    txtFiltroDO.setText("");
                    dateChooserFiltro.cleanup();
                    chkFiltroHora.setSelected(false);
                    aplicarFiltros.run();
                }
                
                mainPanel.revalidate();
                mainPanel.repaint();
        	}
        });
        
        // Plus Label
        JLabel plusLabel = new JLabel(new ImageIcon(plusImg));
        // Añadiendo un mouse listener para Crear Vuelos
        plusLabel.addMouseListener(new MouseAdapter() {
        	
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		dialogNVuelo = new JDialogNVuelo(esLlegada, aeropuertos, aers, panelVuelos, avs, puertas, vg, modelo);
        	}
        });
        
        panelDerecha.add(lupaLabel);
        panelDerecha.add(plusLabel);
        
        panelSdeTPS.add(panelDerecha, BorderLayout.EAST);
        
        tablaPSuperior.add(panelSdeTPS, BorderLayout.NORTH);
        tablaPSuperior.add(panelFiltros, BorderLayout.SOUTH);
        
        mainPanel.add(tablaPSuperior, BorderLayout.NORTH);	
        
        
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
	
	
	private LocalDate dateChooseraLocalDate(JDateChooser dateChooser) {
		return dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();		
	}
	
	private LocalTime spinnerToLocalTime(JSpinner sHora) {
		return ((((Date) sHora.getValue()).toInstant()).atZone(java.time.ZoneId.systemDefault())).toLocalTime().withSecond(0).withNano(0);
	}
}
