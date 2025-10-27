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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.toedter.calendar.JDateChooser;

import domain.Vuelo;

public class JPanelVuelos extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Vuelo> vuelos;
	
	public JPanelVuelos(ArrayList<Vuelo> vuelos) {
		
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
        JCheckBox chkFiltroFecha = new JCheckBox("Fecha");
        JDateChooser dateChooserFiltro = new JDateChooser();
        dateChooserFiltro.setDateFormatString("dd/MM/yyyy"); // formato de fecha
        dateChooserFiltro.setEnabled(false);
        chkFiltroFecha.add(dateChooserFiltro);
        
        chkFiltroFecha.addActionListener(e -> {
        	dateChooserFiltro.setEnabled(chkFiltroFecha.isSelected());
        });
        panelFiltros.add(chkFiltroFecha);
        
        // Filtro Hora
        JCheckBox chkFiltroHora = new JCheckBox("Hora");
        JSpinner spinnerFiltroHora = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorFiltroHora = new JSpinner.DateEditor(spinnerFiltroHora, "HH:mm");
        spinnerFiltroHora.setEditor(editorFiltroHora);
        spinnerFiltroHora.setEnabled(false);
        chkFiltroHora.add(spinnerFiltroHora);
        
        chkFiltroHora.addActionListener(e -> {
        	spinnerFiltroHora.setEnabled(chkFiltroHora.isSelected());
        });
        panelFiltros.add(chkFiltroHora);
        
        // Tabla
        String ae = esLlegada ? "ORIGEN": "DESTINO";
        String[] columnas = {"VUELO", ae, "FECHA", "HORA", "RETRASO"};
         
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);
        JTable tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
		
        
        for(Vuelo v: vuelos) {
        	String ciudad = esLlegada ? v.getOrigen().getCiudad() : v.getDestino().getCiudad();
        	modelo.addRow(new Object[] {
        			v.getCodigo(),
        			ciudad,
        			v.getFechaHoraProgramada().format(formatterFecha),
        			v.getFechaHoraProgramada().format(formatterHora),
        			v.getDelayed()
        	});
        }
        
        for (int i=0; i<tabla.getColumnCount(); i++) {
        	tabla.getColumnModel().getColumn(i).setMinWidth(80);
        }
        
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
        	int filtroVuelo = txtFiltroVuelo.getText().isEmpty() ? 0: Integer.parseInt(txtFiltroVuelo.getText().toLowerCase().trim());
        	LocalDateTime filtroFechaHora = creadorLDTdeSpinner(dateChooserFiltro, spinnerFiltroHora);
        	String filtroDO = txtFiltroDO.getText().toLowerCase().trim();
        	
        	// Limpiar la tabla
        	modelo.setRowCount(0);
        	
        	// Filtrar y agregar filar
        	for(Vuelo v: vuelos) {
        		String ciudad = (esLlegada ? v.getOrigen().getCiudad() : v.getDestino().getCiudad()).toLowerCase();
        		int codigo = v.getCodigo();
        		LocalDateTime fechaHora = v.getFechaHoraProgramada();
        		
        		boolean coincide = true;
        		if (filtroVuelo != 0 && codigo != filtroVuelo) {
        			coincide = false;
        		}
        		if (!filtroDO.isEmpty() && !ciudad.contains(filtroDO)) {
        			coincide = false;
        		}
        		if (chkFiltroFecha.isEnabled() && !fechaHora.equals(filtroFechaHora)) {
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
        
        txtFiltroVuelo.addActionListener(e -> aplicarFiltros.run());
        txtFiltroDO.addActionListener(e -> aplicarFiltros.run());
        chkFiltroFecha.addActionListener(e -> aplicarFiltros.run());
        chkFiltroHora.addActionListener(e -> aplicarFiltros.run());
        
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
                    chkFiltroFecha.setEnabled(false);
                    chkFiltroHora.setEnabled(false);
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
        		abrirDialogoNuevoVuelo(esLlegada);
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
	
	private void abrirDialogoNuevoVuelo(boolean esLlegada) {
	    // Crear el diálogo
	    JDialog dialog = new JDialog();
	    dialog.setTitle(esLlegada ? "Nuevo Vuelo - Llegada" : "Nuevo Vuelo - Salida");
	    dialog.setModal(true); // Bloquea la ventana principal hasta que se cierre
	    dialog.setSize(400, 400);
	    dialog.setLocationRelativeTo(this); // Centrar en la ventana principal
	    
	    // Panel principal del diálogo
	    JPanel panelFormulario = new JPanel(new BorderLayout(10, 10));
	    panelFormulario.setBorder(new EmptyBorder(20, 20, 20, 20));
	    
	    // Panel de campos del formulario
	    JPanel panelCampos = new JPanel(new GridLayout(6, 2, 10, 10));
	    
	    // Código del vuelo
	    panelCampos.add(new JLabel("Código:"));
	    JTextField txtCodigo = new JTextField();
	    panelCampos.add(txtCodigo);
	    
	    // Origen
	    panelCampos.add(new JLabel("Origen:"));
	    JTextField txtOrigen = new JTextField();
	    if (esLlegada) {
	        txtOrigen.setEnabled(true);
	    } else {
	        txtOrigen.setText("Bilbo");
	        txtOrigen.setEnabled(false); // Bloqueado si es salida
	    }
	    panelCampos.add(txtOrigen);
	    
	    // Destino
	    panelCampos.add(new JLabel("Destino:"));
	    JTextField txtDestino = new JTextField();
	    if (!esLlegada) {
	        txtDestino.setEnabled(true);
	    } else {
	        txtDestino.setText("Bilbo");
	        txtDestino.setEnabled(false); // Bloqueado si es llegada
	    }
	    panelCampos.add(txtDestino);
	    
	    // Fecha y Hora
	    
	    //Fecha
	    panelCampos.add(new JLabel("Fecha:"));
        
        // Fecha
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy"); // formato de fecha
        panelCampos.add(dateChooser);
	    
	    
        // Hora
	    panelCampos.add(new JLabel("Hora:"));
	    
	    // Spinner Hora
	    JSpinner spinnerHora = new JSpinner(new SpinnerDateModel());
        
        // Configurar el formato de hora
        JSpinner.DateEditor editorHora = new JSpinner.DateEditor(spinnerHora, "HH:mm");
        spinnerHora.setEditor(editorHora);
        panelCampos.add(spinnerHora);
	    
	    // Retraso
	    panelCampos.add(new JLabel("Retraso (min):"));
	    JTextField txtRetraso = new JTextField("0");
	    panelCampos.add(txtRetraso);
	    
	    panelFormulario.add(panelCampos, BorderLayout.CENTER);
	    
	    // Panel de botones
	    JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
	    
	    JButton btnGuardar = new JButton("Guardar");
	    btnGuardar.setPreferredSize(new Dimension(100, 30));
	    btnGuardar.addActionListener(ev -> {
	        // Validar y guardar
	        if (validarFormulario()) {
	        	LocalDateTime fechaHora = creadorLDTdeSpinner(dateChooser, spinnerHora);
	        	
	        	System.out.println(fechaHora);
	        	
	            dialog.dispose(); // Cerrar el diálogo
	        } else {
	            JOptionPane.showMessageDialog(dialog, 
	                "Por favor, completa todos los campos correctamente", 
	                "Error", 
	                JOptionPane.ERROR_MESSAGE);
	        }
	    });
	    
	    JButton btnCancelar = new JButton("Cancelar");
	    btnCancelar.setPreferredSize(new Dimension(100, 30));
	    btnCancelar.addActionListener(ev -> dialog.dispose());
	    
	    panelBotones.add(btnCancelar);
	    panelBotones.add(btnGuardar);
	    
	    panelFormulario.add(panelBotones, BorderLayout.SOUTH);
	    
	    dialog.add(panelFormulario);
	    dialog.setVisible(true); // Mostrar el diálogo
	}

	private boolean validarFormulario() {
	    return true;
	}

	private void guardarVuelo(String codigo, String origen, String destino, 
	                         String fecha, String hora, String retraso, boolean esLlegada) {
	    // Aquí implementas la lógica para crear y guardar el vuelo
	    System.out.println("Guardando vuelo:");
	    System.out.println("Código: " + codigo);
	    System.out.println("Origen: " + origen);
	    System.out.println("Destino: " + destino);
	    System.out.println("Fecha: " + fecha);
	    System.out.println("Hora: " + hora);
	    System.out.println("Retraso: " + retraso);
	    
	    // TODO: Crear objeto Vuelo y añadirlo a la lista
	    // TODO: Actualizar la tabla
	}
	
	private LocalDateTime creadorLDTdeSpinner(JDateChooser dateChooser, JSpinner sHora) {
		// Funcion para crear un Local Date Time apartir dos spinners de fecha y de hora
		LocalDate localDate = dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    	LocalTime localHora = ((((Date) sHora.getValue()).toInstant()).atZone(java.time.ZoneId.systemDefault())).toLocalTime();
    	LocalDateTime fechaHora = LocalDateTime.of(localDate, localHora);
    	
    	return fechaHora;		
	}
}
