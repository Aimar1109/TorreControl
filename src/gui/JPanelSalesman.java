package gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import domain.Vuelo;

public class JPanelSalesman extends JPanel {

    private static final long serialVersionUID = 1L;
    
    private ArrayList<Vuelo> vuelos;
    
    // Componentes
    private JTable tablaVuelos;
    private JTable tablaPasajerosTripulacion;
    private JTable tablaInfoVuelo;
    private JComboBox<String> comboOpciones;

    public JPanelSalesman(ArrayList<Vuelo> vuelos) {
        this.vuelos = vuelos != null ? vuelos : new ArrayList<>();
        
        setLayout(new BorderLayout());
        
        // -----------------
        // Panel superior con título
        JPanel panelSuperior = new JPanel();
        JLabel lblTitulo = new JLabel("Panel Salesman");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        panelSuperior.add(lblTitulo);
        add(panelSuperior, BorderLayout.NORTH);
        
        // Panel central con margen
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Panel izquierdo: tabla de vuelos
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setBorder(new EmptyBorder(5,5,5,5));
        
        String[] columnasVuelos = {"Código", "Origen", "Destino", "Duración", "Delayed"};
        DefaultTableModel modeloVuelos = new DefaultTableModel(columnasVuelos, 0);
        tablaVuelos = new JTable(modeloVuelos);
        
        tablaVuelos.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollVuelos = new JScrollPane(tablaVuelos);
        panelIzquierdo.add(scrollVuelos, BorderLayout.CENTER);
        panelIzquierdo.setPreferredSize(new java.awt.Dimension(400,0));
        panelCentral.add(panelIzquierdo, BorderLayout.WEST);
        
        // Panel derecho dividido en dos filas iguales
        JPanel panelDerecho = new JPanel(new GridLayout(2, 1, 5, 5)); // 2 filas, 1 columna, 5px de separación
        panelDerecho.setBorder(new EmptyBorder(5,5,5,5));

        // Arriba: combo + tabla pasajeros/tripulación
        JPanel panelArriba = new JPanel(new BorderLayout());
        comboOpciones = new JComboBox<>(new String[] {"Pasajeros", "Tripulación"});
        panelArriba.add(comboOpciones, BorderLayout.NORTH);

        tablaPasajerosTripulacion = new JTable(new DefaultTableModel());
        JScrollPane scrollPasajeros = new JScrollPane(tablaPasajerosTripulacion);
        panelArriba.add(scrollPasajeros, BorderLayout.CENTER);

        // Abajo: tabla info vuelo
        tablaInfoVuelo = new JTable(new DefaultTableModel());
        JScrollPane scrollInfo = new JScrollPane(tablaInfoVuelo);

        // Añadimos los dos paneles al panel derecho
        panelDerecho.add(panelArriba);
        panelDerecho.add(scrollInfo);

        
        panelCentral.add(panelDerecho, BorderLayout.CENTER);
        
        add(panelCentral, BorderLayout.CENTER);
    }
}
