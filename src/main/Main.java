package main;

import java.util.ArrayList;
import javax.swing.SwingUtilities;
import domain.Vuelo;
import gui.JFramePrincipal;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ArrayList<Vuelo> vuelos = new ArrayList<>();
            vuelos.add(new Vuelo(1001, null, null, null, null, true, 60, null, false, new ArrayList<>(), new ArrayList<>(), 0));
            vuelos.add(new Vuelo(1002, null, null, null, null, false, 45, null, false, new ArrayList<>(), new ArrayList<>(), 5));

            new JFramePrincipal(vuelos); // ahora funciona porque el constructor acepta vuelos
        });
    }
}
