package domain;

import java.util.List;

public class AnalisisDatos {

    public static double buscarTempMaximaRecursiva(List<Clima> datos, int indice) {
        if (indice == datos.size() - 1) {
            return datos.get(indice).getTemperatura();
        }

        double maxResto = buscarTempMaximaRecursiva(datos, indice + 1);

        return Math.max(datos.get(indice).getTemperatura(), maxResto);
    }

    public static double calcularLluviaTotalRecursiva(List<Clima> datos, int indice) {
        if (indice >= datos.size()) {
            return 0.0;
        }

        return datos.get(indice).getPrecipitacion() + calcularLluviaTotalRecursiva(datos, indice + 1);
    }

    public static int contarAlertasRecursivo(List<Clima> datos, int indice) {
        if (indice >= datos.size()) {
            return 0;
        }

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
        sb.append("INFORME DIARIO \n\n");
        sb.append(String.format("- Temp. Máxima:   %.1f °C\n", maxTemp));
        sb.append(String.format("- Lluvia Total:   %.1f mm\n", totalLluvia));
        sb.append(String.format("- Lluvia Media:   %.2f mm/h\n", mediaLluvia));
        sb.append(String.format("- Horas Alerta:   %d horas\n", horasPeligro));
        
        if (horasPeligro > 5) {
            sb.append("\n(!) ADVERTENCIA: Día de alto riesgo meteorológico.");
        } else {
            sb.append("\n Día con condiciones mayormente estables.");
        }
        
        return sb.toString();
    }
}