package domain;

import java.util.ArrayList;
import java.util.List;

public class AnalisisDatos {

    public static double buscarTempMaximaRecursiva(List<Clima> datos, int indice) {
        if (indice == datos.size() - 1) return datos.get(indice).getTemperatura();
        double maxResto = buscarTempMaximaRecursiva(datos, indice + 1);
        return Math.max(datos.get(indice).getTemperatura(), maxResto);
    }

    public static double calcularLluviaTotalRecursiva(List<Clima> datos, int indice) {
        if (indice >= datos.size()) return 0.0;
        return datos.get(indice).getPrecipitacion() + calcularLluviaTotalRecursiva(datos, indice + 1);
    }

    public static int contarAlertasRecursivo(List<Clima> datos, int indice) {
        if (indice >= datos.size()) return 0;
        int esPeligroso = datos.get(indice).isSenalPeligro() ? 1 : 0;
        return esPeligroso + contarAlertasRecursivo(datos, indice + 1);
    }
    
    public static String generarInformeTexto(List<Clima> datos) {
        if (datos == null || datos.isEmpty()) return "No hay datos disponibles.";
        double maxTemp = buscarTempMaximaRecursiva(datos, 0);
        double totalLluvia = calcularLluviaTotalRecursiva(datos, 0);
        int horasPeligro = contarAlertasRecursivo(datos, 0);
        double mediaLluvia = totalLluvia / datos.size();
        
        StringBuilder sb = new StringBuilder();
        sb.append("INFORME DIARIO\n\n");
        sb.append(String.format("- Temp. Máxima:   %.1f °C\n", maxTemp));
        sb.append(String.format("- Lluvia Total:   %.1f mm\n", totalLluvia));
        sb.append(String.format("- Lluvia Media:   %.2f mm/h\n", mediaLluvia));
        sb.append(String.format("- Horas Alerta:   %d horas\n", horasPeligro));
        
        if (horasPeligro > 5) sb.append("\nADVERTENCIA: Día de alto riesgo meteorológico.");
        else sb.append("\nDía con condiciones mayormente estables.");
        
        return sb.toString();
    }

    public static List<Clima> ordenarPorTemperatura(List<Clima> datos) {
        List<Clima> copia = new ArrayList<>(datos);
        quickSortRecursivo(copia, 0, copia.size() - 1);
        return copia;
    }

    private static void quickSortRecursivo(List<Clima> lista, int bajo, int alto) {
        if (bajo < alto) {
            int pi = particion(lista, bajo, alto);

            quickSortRecursivo(lista, bajo, pi - 1);
            quickSortRecursivo(lista, pi + 1, alto);
        }
    }

    private static int particion(List<Clima> lista, int bajo, int alto) {
        double pivote = lista.get(alto).getTemperatura();
        int i = (bajo - 1); 

        for (int j = bajo; j < alto; j++) {
            if (lista.get(j).getTemperatura() > pivote) {
                i++;
                Clima temp = lista.get(i);
                lista.set(i, lista.get(j));
                lista.set(j, temp);
            }
        }

        Clima temp = lista.get(i + 1);
        lista.set(i + 1, lista.get(alto));
        lista.set(alto, temp);

        return i + 1;
    }
}