package gui;

import domain.Pista;
import domain.Vuelo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;


public class DialogoAsignarPista extends JDialog {

    private static final long serialVersionUID = 1L;

    //IAG (herramienta: CHAT-GPT) paleta de colores generada con IA
    private static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private static final Color COLOR_PELIGRO = new Color(231, 76, 60);
    private static final Color COLOR_TEXTO_OSCURO = new Color(44, 62, 80);
    private static final Color COLOR_FONDO = new Color(250, 251, 252);
    private static final Color COLOR_PISTA1 = new Color(52, 152, 219);
    private static final Color COLOR_PISTA2 = new Color(155, 89, 182);


    //Componentes
    private JComboBox<Vuelo> comboVuelos;
    private JToggleButton btnPista1;
    private JToggleButton btnPista2;
    private JToggleButton btnCancelar;

    //Listas
    private DefaultListModel<Vuelo> modeloVuelosRecientes;
    private ArrayList<Vuelo> vuelosDisponibles;
    private DefaultListModel<Vuelo> modeloPista1;
    private DefaultListModel<Vuelo> modeloPista2;

    //Pistas
    private Pista pista1;
    private Pista pista2;

    DialogoAsignarPista(Frame parent, DefaultListModel<Vuelo> modeloVuelosRecientes, DefaultListModel<Vuelo> modeloVuelosPista1, DefaultListModel<Vuelo> modeloVuelosPista2, Pista pista1, Pista pista2) {
        super(parent, "Asignacion pistas");
        this.modeloVuelosRecientes = modeloVuelosRecientes;
        this.modeloPista1 = modeloVuelosPista1;
        this.modeloPista2 = modeloVuelosPista2;
        this.pista1 = pista1;
        this.pista2 = pista2;

        vuelosDisponibles = new ArrayList<>();
        for (int i = 0; i < modeloVuelosRecientes.size(); i++) {
            Vuelo vuelo = modeloVuelosRecientes.get(i);
            if (esArrastrable(vuelo)) {
                vuelosDisponibles.add(vuelo);
            }
        }

        initComponents();
        setLayout();
        anadirListeners();

        setSize(450, 250);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private boolean esArrastrable(Vuelo vuelo) {
        return vuelo != null && "Bilbao".equals(vuelo.getDestino().getCiudad());
    }

    private void initComponents() {
        comboVuelos = new JComboBox<>();
        comboVuelos.setFont(new Font("Arial", Font.PLAIN, 14));
        comboVuelos.setBackground(Color.WHITE);
        comboVuelos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        //Añado vuelos al combobox
        for (Vuelo v : vuelosDisponibles) {
            comboVuelos.addItem(v);
        }

        //Renderer del combobox
        comboVuelos.setRenderer(new ListCellRenderer<Vuelo>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Vuelo> list, Vuelo value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel devolver = new JLabel();

                if (value != null) {
                    String texto = value.getCodigo() + " | " + value.getOrigen() + " -> " + value.getDestino();
                    devolver.setText(texto);

                    if(value.isEmergencia()) {
                        devolver.setBackground(new Color(255, 240, 240));
                    }
                    else if (value.getDelayed() > 0) {
                        devolver.setBackground(new Color(255, 248, 228));
                    } else {
                        devolver.setBackground(new Color(240, 248, 255));
                    }

                    devolver.setForeground(COLOR_TEXTO_OSCURO);
                }

                if (isSelected) {
                    devolver.setBackground(COLOR_PRIMARIO);
                    devolver.setForeground(Color.WHITE);
                }

                devolver.setOpaque(true);
                devolver.setBorder(new  EmptyBorder(10, 15, 10, 15));

                return devolver;
            }
        });

        //Boton Pista 1
        btnPista1 = new JToggleButton();
        btnPista1.setText("Asignar Pista 1");
        estilizarBotonToggle(btnPista1, COLOR_PISTA1);


        btnPista2 = new JToggleButton();
        btnPista2.setText("Asignar Pista 2");
        estilizarBotonToggle(btnPista2, COLOR_PISTA2);

        btnCancelar = new JToggleButton();
        btnCancelar.setText("Cancelar");
        estilizarBotonToggle(btnCancelar, COLOR_PELIGRO);
    }

    private void estilizarBotonToggle(JToggleButton boton, Color colorBase) {
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setBorderPainted(true);
        boton.setBackground(colorBase);
        boton.setForeground(Color.WHITE);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(colorBase.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(colorBase);
            }
        });
    }

    private void setLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(COLOR_FONDO);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        //Headerr
        JLabel labelHeader = new JLabel();
        labelHeader.setText("Asignar Pista");
        labelHeader.setFont(new Font("Arial", Font.BOLD, 18));
        labelHeader.setHorizontalAlignment(SwingConstants.CENTER);
        labelHeader.setForeground(Color.WHITE);

        //Contenido
        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBackground(COLOR_FONDO);

        //ComboBox
        comboVuelos.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(Box.createVerticalStrut(8));
        panelContenido.add(comboVuelos);

        //Botones
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new GridLayout(1, 3, 10, 0));

        panelBotones.add(btnPista1);
        panelBotones.add(btnPista2);
        panelBotones.add(btnCancelar);

        panelContenido.add(Box.createVerticalStrut(20));
        panelContenido.add(panelBotones);

        mainPanel.add(labelHeader);
        mainPanel.add(panelContenido);

        add(mainPanel);
    }

    private void anadirListeners() {
        //Botón pista 1
        btnPista1.addActionListener(e -> asignacion(1));
        //Botón pista 2
        btnPista2.addActionListener(e -> asignacion(2));
        //Botón cancelar
        btnCancelar.addActionListener(e -> dispose());
    }


    private void asignacion(int pista) {
        Vuelo vuelo = (Vuelo) comboVuelos.getSelectedItem();
        if (vuelo == null) {
            return;
        }

        if (pista == 1) {
            modeloPista1.addElement(vuelo);
            vuelo.setPista(pista1);
            modeloVuelosRecientes.removeElement(vuelo);
        } else if (pista == 2) {
            modeloPista2.addElement(vuelo);
            vuelo.setPista(pista2);
            modeloVuelosRecientes.removeElement(vuelo);
        }

        dispose();
    }
}