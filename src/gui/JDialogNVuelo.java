package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;

import domain.Aerolinea;
import domain.Aeropuerto;
import domain.Avion;
import domain.PaletaColor;
import domain.Pista;
import domain.PuertaEmbarque;
import domain.Vuelo;
import jdbc.GestorBD;

public class JDialogNVuelo extends 	JDialog {
	
	private JTextField txtNumero, txtDuracion;
	private JComboBox<Aeropuerto> boxAeropuerto;
	private JComboBox<Aerolinea> boxAerolinea;
	private JDateChooser dateChooser;
	private JSpinner spinnerHora;
	private boolean guardado = false;
	private LocalDateTime fechaHoraSeleccionada;
	private JComboBox<Avion> boxAvion;
	private JComboBox<PuertaEmbarque> boxPuerta;
	
	private GestorBD gestorBD;
	
	public JDialogNVuelo(boolean esLlegada, JPanel panel, DefaultTableModel modelo, GestorBD gestorBD) {
		this.setTitle(esLlegada ? "Nuevo Vuelo - Llegada" : "Nuevo Vuelo - Salida");
	    this.setModal(true); // Bloquea la ventana principal hasta que se cierre
	    this.setSize(400, 500);
	    this.setLocationRelativeTo(panel);
	    
	    this.gestorBD = gestorBD;
	    
	    // Panel principal del diálogo
	    JPanel panelFormulario = new JPanel(new BorderLayout(10, 10));
	    panelFormulario.setBorder(new EmptyBorder(20, 20, 20, 20));
	    
	    // Panel de título
	    JPanel panelTitulo = new JPanel();
	    panelTitulo.setBackground(PaletaColor.PRIMARIO.getColor());
	    JLabel lblTitulo = new JLabel(esLlegada ? "Nuevo Vuelo de Llegada" : "Nuevo Vuelo de Salida");
	    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
	    lblTitulo.setForeground(PaletaColor.BLANCO.getColor());
	    lblTitulo.setBorder(new EmptyBorder(15, 0, 15, 0));
	    panelTitulo.add(lblTitulo);
	    panelFormulario.add(panelTitulo, BorderLayout.NORTH);
	    
	    // Panel de campos del formulario
	    JPanel panelCampos = new JPanel(new GridLayout(8, 2, 10, 10));
	    panelCampos.setBorder(new EmptyBorder(10, 0, 10, 0));
	    
	    // Código del vuelo
	    panelCampos.add(new JLabel("Numero:"));
	    txtNumero = new JTextField();
	    txtNumero.setBorder(BorderFactory.createCompoundBorder(
	    	    BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
	    	    BorderFactory.createEmptyBorder(5, 10, 5, 10)
	    	));
	    panelCampos.add(txtNumero);
	    
	    //Aeropuerto
	    Aeropuerto origen;
	    Aeropuerto destino;
	    JLabel tituAeropuerto;
	    boxAeropuerto = new JComboBox<Aeropuerto>();
	    for(Aeropuerto aeropuertob: (ArrayList<Aeropuerto>) gestorBD.loadAeropuertos()) {
	    	boxAeropuerto.addItem(aeropuertob);
	    }
	    Aeropuerto aeOtro = gestorBD.getAeropuertoByCodigo("LEBB");
	    
	    if (esLlegada) {
	    	tituAeropuerto = new JLabel("Origen");
	    	destino = aeOtro;
	    	origen = (Aeropuerto) boxAeropuerto.getSelectedItem();
	    } else {
	    	tituAeropuerto = new JLabel("Destino");
	    	destino = (Aeropuerto) boxAeropuerto.getSelectedItem();
	    	origen =  aeOtro;
	    }
	    boxAeropuerto.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
	    ((JLabel)boxAeropuerto.getRenderer()).setBorder(new EmptyBorder(5, 10, 5, 10));
	    
	    panelCampos.add(tituAeropuerto);
	    panelCampos.add(boxAeropuerto);
	    
	    //Aerolinea
	    panelCampos.add(new JLabel("Aerolinea:"));
	    boxAerolinea = new JComboBox<Aerolinea>();
	    for(Aerolinea aerolineab: (ArrayList<Aerolinea>) gestorBD.loadAerolineas()) {
	    	boxAerolinea.addItem(aerolineab);
	    }
	    boxAerolinea.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
	    ((JLabel)boxAerolinea.getRenderer()).setBorder(new EmptyBorder(5, 10, 5, 10));
	    panelCampos.add(boxAerolinea);
	    
	    // Fecha y Hora
	    
	    //Fecha
	    panelCampos.add(new JLabel("Fecha:"));
        
        // Fecha JDateChoser
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy"); // formato de fecha
        panelCampos.add(dateChooser);
	    
	    
        // Hora
	    panelCampos.add(new JLabel("Hora:"));
	    
	    // Spinner Hora
	    spinnerHora = new JSpinner(new SpinnerDateModel());
        
        // Configurar el formato de hora
        JSpinner.DateEditor editorHora = new JSpinner.DateEditor(spinnerHora, "HH:mm");
        spinnerHora.setEditor(editorHora);
        panelCampos.add(spinnerHora);
	    
	    panelFormulario.add(panelCampos, BorderLayout.CENTER);
	    
	    // Duracion
	    panelCampos.add(new JLabel("Duracion:"));
	    txtDuracion = new JTextField();
	    panelCampos.add(txtDuracion);
	    
	    // Avion
	    panelCampos.add(new JLabel("Avion:"));
	    boxAvion = new JComboBox<Avion>();
	    for(Avion avionb: (ArrayList<Avion>) gestorBD.loadAviones()) {
	    	boxAvion.addItem(avionb);
	    }
	    boxAvion.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
	    ((JLabel)boxAvion.getRenderer()).setBorder(new EmptyBorder(5, 10, 5, 10));
	    panelCampos.add(boxAvion);
	    
	    // Puerta
	    panelCampos.add(new JLabel("Puerta:"));
	    boxPuerta = new JComboBox<PuertaEmbarque>();
	    for(PuertaEmbarque puertab: (ArrayList<PuertaEmbarque>) gestorBD.loadPuertasEmbarque()) {
	    	boxPuerta.addItem(puertab);
	    }
	    boxPuerta.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
	    ((JLabel)boxPuerta.getRenderer()).setBorder(new EmptyBorder(5, 10, 5, 10));
	    panelCampos.add(boxPuerta);
	    
	    
	    // Panel de botones
	    JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
	    
	    JButton btnGuardar = new JButton("Guardar");
	    btnGuardar.setPreferredSize(new Dimension(120, 40));
	    btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 12));
	    btnGuardar.setBackground(PaletaColor.GUARDAR.getColor());
	    btnGuardar.setForeground(PaletaColor.BLANCO.getColor());
	    btnGuardar.setFocusPainted(false);
	    btnGuardar.setBorderPainted(false);
	    btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
	    btnGuardar.addActionListener(ev -> {
	        // Validar y guardar
	        if (validarFormulario(txtNumero, txtDuracion, dateChooser, (Aerolinea)boxAerolinea.getSelectedItem())) {
	        	LocalDateTime fechaHora = creadorLDTdeSpinner(dateChooser, spinnerHora);
	        	
	        	guardarVuelo(Integer.parseInt(txtNumero.getText().toString().trim()), origen, destino, (Aerolinea)boxAerolinea.getSelectedItem(),
	        				 (PuertaEmbarque)boxPuerta.getSelectedItem(), fechaHora, Float.parseFloat(txtDuracion.getText().toString()), (Avion) boxAvion.getSelectedItem(),
	        				 modelo);
	        	
	            this.dispose();
	        }
	    });
	    // Efecto hover
	    btnGuardar.addMouseListener(new java.awt.event.MouseAdapter() {
	        public void mouseEntered(java.awt.event.MouseEvent evt) {
	            btnGuardar.setBackground(PaletaColor.GUARDAR_H.getColor());
	        }
	        public void mouseExited(java.awt.event.MouseEvent evt) {
	            btnGuardar.setBackground(PaletaColor.GUARDAR.getColor());
	        }
	    });
	    
	    JButton btnCancelar = new JButton("Cancelar");
	    btnCancelar.setPreferredSize(new Dimension(120, 40));
	    btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 12));
	    btnCancelar.setBackground(PaletaColor.CANCELAR.getColor());
	    btnCancelar.setForeground(PaletaColor.BLANCO.getColor());
	    btnCancelar.setFocusPainted(false);
	    btnCancelar.setBorderPainted(false);
	    btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
	    btnCancelar.addActionListener(ev -> this.dispose());
	    // Efecto hover
	    btnCancelar.addMouseListener(new java.awt.event.MouseAdapter() {
	        public void mouseEntered(java.awt.event.MouseEvent evt) {
	        	btnCancelar.setBackground(PaletaColor.CANCELAR_H.getColor());
	        }
	        public void mouseExited(java.awt.event.MouseEvent evt) {
	        	btnCancelar.setBackground(PaletaColor.CANCELAR.getColor());
	        }
	    });
	    
	    panelBotones.add(btnCancelar);
	    panelBotones.add(btnGuardar);
	    
	    panelFormulario.add(panelBotones, BorderLayout.SOUTH);
	    
	    this.add(panelFormulario);
	    this.setVisible(true); // Mostrar el diálogo
	    
	}
	
	private boolean validarFormulario(JTextField txtNumero, JTextField txtDuracion, JDateChooser dateChoseer,
									  Aerolinea aerolinea) {
	    if (txtNumero.getText().toString().trim().isEmpty()) {
	    	JOptionPane.showMessageDialog(this, 
	                "Por favor, rellene el numero", 
	                "Error", 
	                JOptionPane.ERROR_MESSAGE);
	    	return false;
	    }
	    if (txtDuracion.getText().toString().trim().isEmpty()) {
	    	JOptionPane.showMessageDialog(this, 
	                "Por favor, rellene la duracion", 
	                "Error", 
	                JOptionPane.ERROR_MESSAGE);
	    	return false;
	    }
	    if (dateChooser.getDate() == null) {
	    	JOptionPane.showMessageDialog(this, 
	                "Por favor, rellene la fecha", 
	                "Error", 
	                JOptionPane.ERROR_MESSAGE);
	    	return false;
	    } else {
	    	Date fechaSeleccionada = dateChooser.getDate();
	    	LocalDate fecha = fechaSeleccionada.toInstant()
	                .atZone(ZoneId.systemDefault())
	                .toLocalDate();

	        if (fecha.isBefore(LocalDate.now())) {
	        	JOptionPane.showMessageDialog(this, 
		                "Por favor, ponga la fecha de hoy en adelante", 
		                "Error", 
		                JOptionPane.ERROR_MESSAGE);
	        	return false;
	        }
	    }
	    try {
	    	Integer.parseInt(txtNumero.getText().toString().trim());
	    	Float.parseFloat(txtDuracion.getText().toString().trim());
	    } catch (NumberFormatException e) {
	    	JOptionPane.showMessageDialog(this, 
	                "Por favor, en el numero y en la duracion no escriba caracteres", 
	                "Error", 
	                JOptionPane.ERROR_MESSAGE);
	    	return false;
	    }
	    if (txtNumero.getText().toString().trim().length() != 4) {
	    	JOptionPane.showMessageDialog(this, 
	                "Por favor, el numero tiene que ser de 4 digitos", 
	                "Error", 
	                JOptionPane.ERROR_MESSAGE);
	    	return false;
	    }
	    if (gestorBD.getAerolineaByCodigo(aerolinea.getCodigo()+txtNumero.getText().toString().trim())!=null) {
	    	JOptionPane.showMessageDialog(this, 
	                "Por favor, el vuelo ya esta creado", 
	                "Error", 
	                JOptionPane.ERROR_MESSAGE);
	    	return false;
	    }
	    return true;	    
	}
	
	private void guardarVuelo(int numero, Aeropuerto origen, Aeropuerto destino, Aerolinea aerolinea,
			PuertaEmbarque puerta, LocalDateTime fechaHoraProgramada, float duracion, Avion avion,
			DefaultTableModel modelo) {
			
			DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("dd:MM:yyyy");
			DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm");
			
			Vuelo v = new Vuelo(numero, origen, destino, aerolinea, puerta, fechaHoraProgramada, duracion, avion);
			gestorBD.insertVuelo(v);
			Aeropuerto ciudad;
			if (origen.getCiudad().equals("Bilbao")) {
				ciudad = destino;
			} else {
				ciudad = origen;
			}
        	LocalDateTime llega = v.getFechaHoraProgramada().plusMinutes((long) v.getDuracion());
        	modelo.addRow(new Object[] {
        			v.getCodigo(),
        			ciudad,
        			v.getFechaHoraProgramada().format(formatterFecha),
        			v.getFechaHoraProgramada().format(formatterHora),
        			llega.format(formatterHora),
        			v.getPuerta(),
        			v.getDelayed()
        	});
		}
	
	private LocalDateTime creadorLDTdeSpinner(JDateChooser dateChooser, JSpinner sHora) {
		// Funcion para crear un Local Date Time apartir dos spinners de fecha y de hora
		LocalDate localDate = dateChooseraLocalDate(dateChooser);
    	LocalTime localHora = spinnerToLocalTime(sHora);
    	LocalDateTime fechaHora = LocalDateTime.of(localDate, localHora);
    	return fechaHora;		
	}
	
	private LocalDate dateChooseraLocalDate(JDateChooser dateChooser) {
		return dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();		
	}
	
	private LocalTime spinnerToLocalTime(JSpinner sHora) {
		return ((((Date) sHora.getValue()).toInstant()).atZone(java.time.ZoneId.systemDefault())).toLocalTime().withSecond(0).withNano(0);
	}

}
