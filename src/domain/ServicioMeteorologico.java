package domain;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import domain.Clima.*; 

public class ServicioMeteorologico {

    private static final String LAT = "43.2627"; // Bilbao
    private static final String LON = "-2.9253";
    
    private static final String API_URL = "https://api.open-meteo.com/v1/forecast?latitude=" + LAT + "&longitude=" + LON 
            + "&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,weather_code,surface_pressure,cloud_cover,visibility,wind_speed_10m,wind_direction_10m"
            + "&timezone=auto&forecast_days=1";

    public List<Clima> obtenerPronosticoReal() {
        List<Clima> listaClima = new ArrayList<>();
        
        try {
            @SuppressWarnings("deprecation")
			URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            
            if (conn.getResponseCode() != 200) {
                System.err.println("Error API: " + conn.getResponseCode());
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) sb.append(linea);
            br.close();
            
            String json = sb.toString();
            
            int indexHourly = json.indexOf("\"hourly\"");
            if (indexHourly == -1) return null;
            String jsonDatos = json.substring(indexHourly);

            // Extracción de datos
            List<Double> temps = extraerDoublesRegex(jsonDatos, "temperature_2m");
            List<Double> vientos = extraerDoublesRegex(jsonDatos, "wind_speed_10m");
            List<Double> direcciones = extraerDoublesRegex(jsonDatos, "wind_direction_10m");
            List<Double> visibilidad = extraerDoublesRegex(jsonDatos, "visibility");
            List<Double> humedades = extraerDoublesRegex(jsonDatos, "relative_humidity_2m");
            List<Double> presiones = extraerDoublesRegex(jsonDatos, "surface_pressure");
            List<Double> nubes = extraerDoublesRegex(jsonDatos, "cloud_cover");
            List<Double> precipitacionReal = extraerDoublesRegex(jsonDatos, "precipitation"); // <--- NUEVO CAMPO REAL
            List<Integer> codigos = extraerEnterosRegex(jsonDatos, "weather_code");
            List<Integer> probLluvia = extraerEnterosRegex(jsonDatos, "precipitation_probability");

            if (temps.isEmpty() || codigos.isEmpty()) {
                System.err.println("La API respondió pero no se pudieron leer los números.");
                return null;
            }

            for (int i = 0; i < 24; i++) {
                if (i >= temps.size()) break;

                double t = temps.get(i);
                double v = (vientos.size() > i) ? vientos.get(i) : 0.0;
                double dir = (direcciones.size() > i) ? direcciones.get(i) : 0.0;
                double visKm = (visibilidad.size() > i) ? visibilidad.get(i) / 1000.0 : 10.0;
                double hum = (humedades.size() > i) ? humedades.get(i) : 50.0;
                double pres = (presiones.size() > i) ? presiones.get(i) : 1013.0;
                double nubesVal = (nubes.size() > i) ? nubes.get(i) : 0.0;
                
                // Lluvia real de la API
                double precipMm = (precipitacionReal.size() > i) ? precipitacionReal.get(i) : 0.0;
                
                int nubeTecho = (int) (2000 - (nubesVal * 10)); 
                if (nubeTecho < 200) nubeTecho = 200;
                
                int prob = (probLluvia.size() > i) ? probLluvia.get(i) : 0;
                int wmoCode = (codigos.size() > i) ? codigos.get(i) : 0;

                // Pasamos 'precipMm' a la factoría para que lo use
                Clima c = factoryClima(wmoCode, t, v, visKm, nubeTecho, prob, hum, pres, precipMm);
                c.setDireccionViento(dir);
                listaClima.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return listaClima;
    }

    private Clima factoryClima(int code, double t, double v, double vis, int nubes, int prob, double hum, double pres, double lluviaReal) {
        
        if (lluviaReal > 0.1 && code < 70) {
            return new ClimaLluvioso(t, v, vis, lluviaReal, nubes, prob, hum, pres, false);
        }

        if (code <= 3) return new ClimaDespejado(t, v, vis, hum, pres, IntensidadSol.ALTA);
        
        if (code == 45 || code == 48) return new ClimaNublado(t, v, Math.min(vis, 0.5), nubes, prob, hum, pres);
        
        if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82)) {
            double mm = (lluviaReal > 0) ? lluviaReal : ((code > 60) ? 5.0 : 1.0);
            return new ClimaLluvioso(t, v, vis, mm, nubes, prob, hum, pres, false);
        }
        
        if ((code >= 71 && code <= 77) || code == 85 || code == 86) {
            double mm = (lluviaReal > 0) ? lluviaReal : 1.0;
            return new ClimaNevado(t, v, vis, mm, nubes, prob, hum, pres, mm);
        }
        
        if (code >= 95) {
            double mm = (lluviaReal > 0) ? lluviaReal : 15.0;
            return new ClimaLluvioso(t, v, vis, mm, nubes, prob, hum, pres, true);
        }
        
        // Por defecto
        return new ClimaNublado(t, v, vis, nubes, prob, hum, pres);
    }
    
    private List<Double> extraerDoublesRegex(String json, String key) {
        List<Double> lista = new ArrayList<>();
        Pattern p = Pattern.compile("\"" + key + "\":\\s*\\[(.*?)\\]");
        Matcher m = p.matcher(json);
        if (m.find()) {
            String arrayContent = m.group(1);
            for (String val : arrayContent.split(",")) {
                try {
                    if (!val.trim().equals("null")) 
                        lista.add(Double.parseDouble(val.trim()));
                    else lista.add(0.0);
                } catch (Exception e) {}
            }
        }
        return lista;
    }

    private List<Integer> extraerEnterosRegex(String json, String key) {
        List<Integer> lista = new ArrayList<>();
        Pattern p = Pattern.compile("\"" + key + "\":\\s*\\[(.*?)\\]");
        Matcher m = p.matcher(json);
        if (m.find()) {
            String arrayContent = m.group(1);
            for (String val : arrayContent.split(",")) {
                try {
                    if (!val.trim().equals("null")) 
                        lista.add(Integer.parseInt(val.trim()));
                    else lista.add(0);
                } catch (Exception e) {}
            }
        }
        return lista;
    }
}