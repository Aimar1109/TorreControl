package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

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

import com.toedter.calendar.JDateChooser;

import domain.Aerolinea;
import domain.Aeropuerto;
import domain.Avion;

public class JDialogNVuelo extends 	JDialog {
	
	private JTextField txtNumero, txtDuracion;
	private JComboBox<Aeropuerto> boxAeropuerto;
	private JComboBox<Aerolinea> boxAerolinea;
	private JDateChooser dateChooser;
	private JSpinner spinnerHora;
	private boolean guardado = false;
	private LocalDateTime fechaHoraSeleccionada;
	private JComboBox<Avion> boxAvion;
	
	public JDialogNVuelo(boolean esLlegada, ArrayList<Aeropuerto> aeropuertos, ArrayList<Aerolinea> aers, JPanel panel, ArrayList<Avion> avs) {
		this.setTitle(esLlegada ? "Nuevo Vuelo - Llegada" : "Nuevo Vuelo - Salida");
	    this.setModal(true); // Bloquea la ventana principal hasta que se cierre
	    this.setSize(400, 400);
	    this.setLocationRelativeTo(panel);
	    
	 // Panel principal del diálogo
	    JPanel panelFormulario = new JPanel(new BorderLayout(10, 10));
	    panelFormulario.setBorder(new EmptyBorder(20, 20, 20, 20));
	    
	    // Panel de campos del formulario
	    JPanel panelCampos = new JPanel(new GridLayout(8, 2, 10, 10));
	    
	    // Código del vuelo
	    panelCampos.add(new JLabel("Numero:"));
	    txtNumero = new JTextField();
	    panelCampos.add(txtNumero);
	    
	    //Aeropuerto
	    JLabel tituAeropuerto;
	    boxAeropuerto = new JComboBox<Aeropuerto>(aeropuertos.toArray(new Aeropuerto[0])); // IAG
	    
	    if (esLlegada) {
	    	tituAeropuerto = new JLabel("Origen");
	    } else {
	    	tituAeropuerto = new JLabel("Destino");
	    }
	    
	    panelCampos.add(tituAeropuerto);
	    panelCampos.add(boxAeropuerto);
	    
	    //Aerolinea
	    panelCampos.add(new JLabel("Aerolinea:"));
	    boxAerolinea = new JComboBox<Aerolinea>(aers.toArray(new Aerolinea[0]));
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
	    boxAvion = new JComboBox<Avion>(avs.toArray(new Avion[0]));
	    panelCampos.add(boxAvion);
	    
	    
	    // Panel de botones
	    JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
	    
	    JButton btnGuardar = new JButton("Guardar");
	    btnGuardar.setPreferredSize(new Dimension(100, 30));
	    btnGuardar.addActionListener(ev -> {
	        // Validar y guardar
	        if (validarFormulario()) {
	        	LocalDateTime fechaHora = creadorLDTdeSpinner(dateChooser, spinnerHora);
	        	
	        	System.out.println(fechaHora);
	        	
	            this.dispose(); // Cerrar el diálogo
	        } else {
	            JOptionPane.showMessageDialog(this, 
	                "Por favor, completa todos los campos correctamente", 
	                "Error", 
	                JOptionPane.ERROR_MESSAGE);
	        }
	    });
	    
	    JButton btnCancelar = new JButton("Cancelar");
	    btnCancelar.setPreferredSize(new Dimension(100, 30));
	    btnCancelar.addActionListener(ev -> this.dispose());
	    
	    panelBotones.add(btnCancelar);
	    panelBotones.add(btnGuardar);
	    
	    panelFormulario.add(panelBotones, BorderLayout.SOUTH);
	    
	    this.add(panelFormulario);
	    this.setVisible(true); // Mostrar el diálogo
	    
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
