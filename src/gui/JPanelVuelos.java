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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import javax.swing.DefaultCellEditor;
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
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableModel;

import com.toedter.calendar.JDateChooser;

import domain.Aerolinea;
import domain.Aeropuerto;
import domain.Avion;
import domain.PaletaColor;
import domain.Pista;
import domain.PuertaEmbarque;
import domain.Vuelo;
import jdbc.GestorBD;
import threads.ObservadorTiempo;
import threads.RelojGlobal;

public class JPanelVuelos extends JPanel implements ObservadorTiempo {
	
	private static final long serialVersionUID = 1L;
	
	private DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm");
	private DateTimeFormatter formatterHoraReloj = DateTimeFormatter.ofPattern("HH:mm:ss");
	
	private ArrayList<Vuelo> vuelos;
	private JDialogNVuelo dialogNVuelo;
	private JPanel panelVuelos = this;
	
	private JLabel lblreloj = new JLabel();
	
	private GestorBD gestorBD;
	
	public JPanelVuelos(GestorBD gestorBD, ArrayList<Vuelo> vuelos) {
		
		this.gestorBD = gestorBD;
		
		setLayout(new BorderLayout());
		setBackground(PaletaColor.get(PaletaColor.FONDO));
		
		// Datos necesarios
		this.vuelos = vuelos;
		
		// Creacion del main panel
		JPanel mainVuelos = new JPanel(new BorderLayout());
		mainVuelos.setBackground(PaletaColor.get(PaletaColor.FONDO));
		
		// Panel Superior
		JPanel panelSuperior = new JPanel(new BorderLayout());
		panelSuperior.setBackground(PaletaColor.get(PaletaColor.PRIMARIO));
		panelSuperior.setBorder(new EmptyBorder(15, 20, 15, 20));
		
		// Titulo VUELOS
		JLabel titu = new JLabel("VUELOS", SwingConstants.CENTER);
		titu.setFont(new Font("Segoe UI", Font.BOLD, 24));
		titu.setForeground(PaletaColor.get(PaletaColor.BLANCO));
        
        panelSuperior.add(titu, BorderLayout.CENTER);
        
        // Reloj - A la izquierda
        int widthLados = 120;
        
        lblreloj.setPreferredSize(new Dimension(widthLados, 0));
        lblreloj.setFont(new Font("Consolas", Font.BOLD, 18));
        lblreloj.setForeground(PaletaColor.get(PaletaColor.BLANCO));        
        panelSuperior.add(lblreloj, BorderLayout.WEST);
        
        // Derecha vacio para vuelos centrado
        JLabel vacioD = new JLabel("");
        vacioD.setPreferredSize(new Dimension(widthLados, 0));
        vacioD.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); // margen a la izquierda
        
        panelSuperior.add(vacioD, BorderLayout.EAST);
		
		mainVuelos.add(panelSuperior, BorderLayout.NORTH);
		
		// Panel Central
		JPanel panelCentral = new JPanel(new GridLayout(1, 2, 5, 5));
		panelCentral.setBorder(new EmptyBorder(10, 30, 30, 30));
		panelCentral.setBackground(PaletaColor.get(PaletaColor.FONDO));
		
		// crear tablas
		JPanel mainLlegadas = creadorTablaVuelos("LLEGADAS", vuelos, true);
		JPanel mainSalidas = creadorTablaVuelos("SALIDAS", vuelos, false);
        
		// MAIN
        panelCentral.add(mainLlegadas);
        panelCentral.add(mainSalidas);
        
        mainVuelos.add(panelCentral, BorderLayout.CENTER);
		add(mainVuelos);
		
		// Al final del constructor de JPanelVuelos:
		RelojGlobal.getInstancia().addObservador(this);
		// Mostrar tiempo inicial inmediatamente
		actualizarTiempo(RelojGlobal.getInstancia().getTiempoActual());
	}
	
	private JPanel creadorTablaVuelos(String titulo, ArrayList<Vuelo> vuelos, boolean esLlegada) {
		// Funcion para crear tabla de Vuelos tanto llegadas como salidas
		
		
		final int[] filaHover = {-1};
		
		// Formater para que solo aparezca la hora
		
		
		// cellRenderer para los Titulos
		TableCellRenderer headerRenderer = (table, value, isSelected, hasFocus, row, column) -> {
			JLabel result = new JLabel(value.toString());			
			result.setHorizontalAlignment(JLabel.CENTER);
			
			switch (value.toString()) {
				case "ORIGEN":
				case "DESTINO":
					result.setHorizontalAlignment(JLabel.LEFT);
					result.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
					break;
			}
			
			result.setBackground(PaletaColor.get(PaletaColor.PRIMARIO));
			result.setForeground(PaletaColor.get(PaletaColor.BLANCO));
			result.setOpaque(true);
			result.setFont(new Font("Segoe UI", Font.BOLD, 13));
			result.setBorder(BorderFactory.createCompoundBorder(
				result.getBorder(),
				BorderFactory.createEmptyBorder(8, 5, 8, 5)
			));
			
			return result;
		};
		
		// cellRenderer para las Tablas
		TableCellRenderer cellRenderer = (table, value, isSelected, hasFocus, row, column) -> {
			JLabel result = new JLabel(value.toString());
			
			if (column == 0 || column == 2 || column == 3 || column == 4) {
				result.setHorizontalAlignment(JLabel.CENTER);
			} else {
				result.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
			}
			
			result.setFont(new Font("Segoe UI", Font.PLAIN, 12));
			result.setForeground(PaletaColor.get(PaletaColor.TEXTO));
			
			// Color de fondo según si es la fila hover o no
			if (row == filaHover[0]) {
				// Fila con hover - color más oscuro
				result.setBackground(PaletaColor.get(PaletaColor.HOVER));
			} else if (row % 2 == 0) {
				result.setBackground(PaletaColor.get(PaletaColor.BLANCO));
			} else {
				result.setBackground(PaletaColor.get(PaletaColor.FILA_ALT));
			}
			
			result.setOpaque(true);
			result.setBorder(BorderFactory.createCompoundBorder(
				result.getBorder(),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)
			));
			
			return result;
		};	
		
		
		//Panel de la tabla
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(PaletaColor.get(PaletaColor.BLANCO));
		mainPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
				BorderFactory.createEmptyBorder(0, 0, 0, 0)
				));
	     
        // Panel superior de la tabla
		JPanel tablaPSuperior = new JPanel(new BorderLayout());
		tablaPSuperior.setBackground(PaletaColor.get(PaletaColor.BLANCO));
		
		// JPanel para titulos y imagenes parte de arriba
		JPanel panelSdeTPS = new JPanel(new BorderLayout());
		panelSdeTPS.setBackground(PaletaColor.get(PaletaColor.BLANCO));
		panelSdeTPS.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
		
		// Titulo Parte Izquierda
        JLabel tituT = new JLabel(titulo, SwingConstants.LEFT);
        tituT.setFont(new Font("Segoe UI", Font.BOLD, 22));
        tituT.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        tituT.setForeground(PaletaColor.get(PaletaColor.PRIMARIO));
        
        panelSdeTPS.add(tituT, BorderLayout.WEST);
        
        // PANEL DE FILTROS (inicialmente oculto)
        JPanel panelFiltros = new JPanel(new GridLayout(1, 4, 10, 10));
        panelFiltros.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelFiltros.setVisible(false);
        panelFiltros.setBackground(PaletaColor.get(PaletaColor.BLANCO));
        
        // Campos filtro
        
        // Filtro vuelo
        JTextField txtFiltroVuelo = new JTextField();
        estilizarTextField(txtFiltroVuelo);
        txtFiltroVuelo.setBorder(BorderFactory.createTitledBorder( //IAG
        	BorderFactory.createLineBorder(PaletaColor.get(PaletaColor.SECUNDARIO), 1),
        	"VUELO",
        	0,
        	0,
        	new Font("Segoe UI", Font.BOLD, 10),
        	PaletaColor.get(PaletaColor.TEXTO_SUAVE)
        ));
        panelFiltros.add(txtFiltroVuelo);
        
        // Filtro para Destino/Origen
        JTextField txtFiltroDO = new JTextField();
        estilizarTextField(txtFiltroDO);
        String tituloFDO = esLlegada ? "ORIGEN": "DESTINO";
        txtFiltroDO.setBorder(BorderFactory.createTitledBorder(
        	BorderFactory.createLineBorder(PaletaColor.get(PaletaColor.SECUNDARIO), 1),
        	tituloFDO,
        	0,
        	0,
        	new Font("Segoe UI", Font.BOLD, 10),
        	PaletaColor.get(PaletaColor.TEXTO_SUAVE)
        ));
        panelFiltros.add(txtFiltroDO);
        
        // Filtro Fecha
        JPanel panelFecha = new JPanel(new BorderLayout());
        panelFecha.setBackground(PaletaColor.get(PaletaColor.BLANCO));
        panelFecha.setBorder(BorderFactory.createTitledBorder(
        	BorderFactory.createLineBorder(PaletaColor.get(PaletaColor.SECUNDARIO), 1),
        	"FECHA",
        	0,
        	0,
        	new Font("Segoe UI", Font.BOLD, 10),
        	PaletaColor.get(PaletaColor.TEXTO_SUAVE)
        ));
        // JDateChooser para fecha
        JDateChooser dateChooserFiltro = new JDateChooser();
        dateChooserFiltro.setDateFormatString("dd/MM/yyyy"); // formato de fecha
        panelFecha.add(dateChooserFiltro, BorderLayout.CENTER);
        dateChooserFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        // Boton para borrar
        ImageIcon xIcon = new ImageIcon("resources\\img\\x.png");
        Image xImg = xIcon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
        JLabel xLabel = new JLabel(new ImageIcon(xImg));
        xLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)); // IAG
        panelFecha.add(xLabel, BorderLayout.EAST);
        panelFiltros.add(panelFecha);
        
        // Filtro Hora
        JPanel panelHora = new JPanel(new BorderLayout());
        panelHora.setBackground(PaletaColor.get(PaletaColor.BLANCO));
        panelHora.setBorder(BorderFactory.createTitledBorder(
        	BorderFactory.createLineBorder(PaletaColor.get(PaletaColor.SECUNDARIO), 1),
        	"HORA",
        	0,
        	0,
        	new Font("Segoe UI", Font.BOLD, 10),
        	PaletaColor.get(PaletaColor.TEXTO_SUAVE)
        ));
        // CheckBox para hora
        JCheckBox chkFiltroHora = new JCheckBox();
        chkFiltroHora.setFont(new Font("Segoe UI", Font.BOLD, 10));
        chkFiltroHora.setBackground(PaletaColor.get(PaletaColor.BLANCO));
        panelHora.add(chkFiltroHora, BorderLayout.WEST);
        chkFiltroHora.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        // Spinner para la hora
        JSpinner spinnerFiltroHora = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorFiltroHora = new JSpinner.DateEditor(spinnerFiltroHora, "HH:mm");
        spinnerFiltroHora.setEditor(editorFiltroHora);
        spinnerFiltroHora.setEnabled(false);
        spinnerFiltroHora.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        chkFiltroHora.addActionListener(e -> {
        	spinnerFiltroHora.setEnabled(chkFiltroHora.isSelected());
        });
        
        panelHora.add(spinnerFiltroHora, BorderLayout.CENTER);
        
        panelFiltros.add(panelHora);
        
        // Tabla
        String ae = esLlegada ? "ORIGEN": "DESTINO";
        String[] columnas = {"VUELO", ae, "FECHA", "HORA", "PUERTA", "PISTA", "RETRASO"};
         
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
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setBackground(PaletaColor.get(PaletaColor.BLANCO));
        tabla.setSelectionBackground(PaletaColor.get(PaletaColor.HOVER));
        tabla.setSelectionForeground(PaletaColor.get(PaletaColor.BLANCO));
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Añadir MouseMotionListener para detectar hover
        tabla.addMouseMotionListener(new MouseAdapter() {
        	@Override
        	public void mouseMoved(MouseEvent e) {
        		int row = tabla.rowAtPoint(e.getPoint());
        		if (row != filaHover[0]) {
        			filaHover[0] = row;
        			tabla.repaint();
        		}
        	}
        });
        
        // Añadir MouseListener para detectar cuando el ratón sale de la tabla
        tabla.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseExited(MouseEvent e) {
        		filaHover[0] = -1;
        		tabla.repaint();
        	}
        });		
        
        for(Vuelo v: vuelos) {
        	if (esLlegada && v.getOrigen().getCiudad().equals("Bilbao")) {
        		continue;
        	} else if (!esLlegada && v.getDestino().getCiudad().equals("Bilbao")) {
        		continue;
        	}
        	String ciudad = esLlegada ? v.getOrigen().getCiudad() : v.getDestino().getCiudad();
        	String pista = v.getPista()==null ? "": v.getPista().toString();
        	modelo.addRow(new Object[] {
        			v.getCodigo(),
        			ciudad,
        			v.getFechaHoraProgramada().format(formatterFecha),
        			v.getFechaHoraProgramada().format(formatterHora),
        			v.getPuerta(),
        			pista,
        			v.getDelayed()
        	});
        }
        
        // Creamos el sorter -- IAG
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        // Comparadores para columnas 2(fecha) y 3(hora), naturalOrder=cronologicamente para LocalDate and LocalTime
        // no es necesario
        //sorter.setComparator(2, Comparator.naturalOrder()); // Fecha
        //sorter.setComparator(3, Comparator.naturalOrder()); // Hora

        // clavesOrden es una lista que tendra el orden de las reglas de ordenamiento
        List<RowSorter.SortKey> clavesOrden = new ArrayList<>();
        // primera regla ordernar por la segunda columna y de manera ascendente
        clavesOrden.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        // segunda regla ordenar por la tercera columna de manera ascendente
        clavesOrden.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
        // añadir esta lista de reglas a al sorter
        sorter.setSortKeys(clavesOrden);
        // ordenar
        sorter.sort();
        
        // ComboBox para PUERTA
        ArrayList<PuertaEmbarque> puertas = (ArrayList<PuertaEmbarque>) gestorBD.loadPuertasEmbarque();
        JComboBox<String> comboPuerta = new JComboBox<>();
        comboPuerta.addItem("");
        for (PuertaEmbarque pu: puertas) {
        	comboPuerta.addItem(pu.toString());
        }
        tabla.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(comboPuerta));
        
        // ComboBox para PISTA
        ArrayList<Pista> pistas = (ArrayList<Pista>) gestorBD.loadPistas();
        JComboBox<String> comboPista = new JComboBox<>();
        comboPista.addItem("");
        for (Pista p: pistas) {
        	comboPista.addItem(p.getNumero());
        }
        tabla.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(comboPista));
        
        // Actualizaciones
        modelo.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int fila = e.getFirstRow();
                int columna = e.getColumn();
                
                if (fila >= 0 && (columna == 4 || columna == 5)) {
                    // Convertir índice de vista a modelo (importante con filtros)
                    int filaModelo = tabla.convertRowIndexToModel(fila);
                    
                    String codigoVuelo = (String) modelo.getValueAt(filaModelo, 0);
                    Object nuevoValor = modelo.getValueAt(filaModelo, columna);
                    
                    // Buscar el vuelo en el ArrayList
                    Vuelo vuelo = vuelos.stream()
                        .filter(v -> v.getCodigo().equals(codigoVuelo))
                        .findFirst()
                        .orElse(null);
                    
                    if (vuelo != null) {
                        if (columna == 4) { // PUERTA
                            // Actualizar puerta
                            PuertaEmbarque nuevaPuerta = gestorBD.getPuertaEmbarqueByCodigo((String) nuevoValor);
                            vuelo.setPuerta(nuevaPuerta);
                        } else if (columna == 5) { // PISTA
                            // Actualizar pista
                            Integer nuevaPista = nuevoValor.toString().isEmpty() ? 
                                null : Integer.parseInt(nuevoValor.toString());
                            Pista np = gestorBD.getPistaByNumero(nuevaPista.toString());
                            vuelo.setPista(np);
                        }
                        
                        // Actualizar en BD
                        gestorBD.updateVuelo(vuelo);
                    }
                }
            }
        });
        
        // Tamaño minimo de las columnas
        int anchoMinimoTotal = 80*tabla.getModel().getColumnCount();
        tabla.setPreferredScrollableViewportSize(new Dimension(anchoMinimoTotal, 0));
     	
        // Crear scroll si es necesario
        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTabla.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());
        scrollTabla.getViewport().setBackground(PaletaColor.get(PaletaColor.BLANCO));
        
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
        
        tabla.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Comprobar si se presiona Ctrl + +
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_PLUS) {
                	dialogNVuelo = new JDialogNVuelo(esLlegada, panelVuelos, modelo, gestorBD);
                }
            }
        });        
        
        
        // Imagenes Parte Derecha
        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panelDerecha.setBackground(PaletaColor.get(PaletaColor.BLANCO));
        
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
        		LocalDateTime fechaHora = v.getFechaHoraProgramada();
        
        		boolean coincide = true;
        		if (esLlegada && v.getOrigen().getCiudad().equals("Bilbao")) {
        			coincide = false;
        		}
        		if (!esLlegada && v.getDestino().getCiudad().equals("Bilbao")) {
        			coincide = false;
        		}
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
        			String pista = v.getPista() == null ? "" : v.getPista().toString();
        			modelo.addRow(new Object[] {
                			v.getCodigo(),
                			ciudad,
                			v.getFechaHoraProgramada().format(formatterFecha),
                			v.getFechaHoraProgramada().format(formatterHora),
                			v.getPuerta().toString(),
                			pista,
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
        lupaLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lupaLabel.setToolTipText("Filtrar vuelos");
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
        	@Override
        	public void mouseEntered(MouseEvent e) {
        		lupaLabel.setOpaque(true);
        		lupaLabel.setBackground(PaletaColor.get(PaletaColor.FONDO));
        	}
        	
        	@Override
        	public void mouseExited(MouseEvent e) {
        		lupaLabel.setOpaque(false);
        	}
        });
        
        // Plus Label
        JLabel plusLabel = new JLabel(new ImageIcon(plusImg));
        plusLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        plusLabel.setToolTipText("Crear nuevo vuelo");
        // Añadiendo un mouse listener para Crear Vuelos
        plusLabel.addMouseListener(new MouseAdapter() {
        	
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		dialogNVuelo = new JDialogNVuelo(esLlegada, panelVuelos, modelo, gestorBD);
        	}
        	@Override
        	public void mouseEntered(MouseEvent e) {
        		plusLabel.setOpaque(true);
        		plusLabel.setBackground(PaletaColor.get(PaletaColor.FONDO));
        	}
        	
        	@Override
        	public void mouseExited(MouseEvent e) {
        		plusLabel.setOpaque(false);
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
	
	// Método auxiliar para estilizar TextFields
	private void estilizarTextField(JTextField textField) { //IAG
		textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		textField.setForeground(PaletaColor.get(PaletaColor.TEXTO));
		textField.setBackground(PaletaColor.get(PaletaColor.BLANCO));
		textField.setCaretColor(PaletaColor.get(PaletaColor.SECUNDARIO));
	}
	
	public ArrayList<Vuelo> loadBDVuelos(GestorBD gestorBD) {
		ArrayList<Vuelo> vuelos = new ArrayList<Vuelo>();
		
		
		
		return vuelos;
	}
	
	@Override
	public void actualizarTiempo(LocalDateTime nuevoTiempo) {
		SwingUtilities.invokeLater(() -> {
			lblreloj.setText(nuevoTiempo.format(formatterHoraReloj));
		});
		
	}

	@Override
	public void cambioEstadoPausa(boolean pausa) {
		SwingUtilities.invokeLater(() -> {
			if (pausa) {
				lblreloj.setForeground(PaletaColor.get(PaletaColor.ACENTO));
			} else {
				lblreloj.setForeground(PaletaColor.get(PaletaColor.BLANCO));
			}
		});
	}
}
