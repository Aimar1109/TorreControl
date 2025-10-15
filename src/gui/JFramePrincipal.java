package gui;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class JFramePrincipal extends JFrame {

	private static final long serialVersionUID = 1L;
	
	public JFramePrincipal() {
		
		// PANEL PRINCIPAL
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		// MENU DE ARRIBA
		JPanel menuPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		menuPanel.add(new Button("1"));
		menuPanel.add(new Button("2"));
		menuPanel.add(new Button("3"));
		menuPanel.add(new Button("4"));
		
		mainPanel.add(menuPanel, BorderLayout.NORTH);

		//Panel Mapa
		MapPanel mapa = new MapPanel();
		mainPanel.add(mapa, BorderLayout.CENTER);
		
		
		// CONFIGURACION DE LA VENTANA
		this.add(mainPanel);
		
		this.setTitle("Torre de Control");		
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		this.setSize(800, 600);
		this.setLocationRelativeTo(null);
		this.setVisible(true);	
	}
}