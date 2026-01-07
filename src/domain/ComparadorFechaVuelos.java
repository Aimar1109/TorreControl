package domain;

import java.time.LocalDateTime;
import java.util.Comparator;

public class ComparadorFechaVuelos implements Comparator<Vuelo> {
    @Override
    public int compare(Vuelo vuelo1, Vuelo vuelo2) {
    	LocalDateTime llegadaVuelo1 = null;
    	if (vuelo1.getDestino().getCodigo().equals("LEBB")) {
    		llegadaVuelo1 = vuelo1.getFechaHoraProgramada().plusMinutes((long) (vuelo1.getDelayed()+vuelo1.getDuracion()));
    	} else {
    		llegadaVuelo1 = vuelo1.getFechaHoraProgramada().plusMinutes(vuelo1.getDelayed());
    	}
    	LocalDateTime llegadaVuelo2 = null;
    	if (vuelo2.getDestino().getCodigo().equals("LEBB")) {
    		llegadaVuelo2 = vuelo2.getFechaHoraProgramada().plusMinutes((long) (vuelo2.getDelayed()+vuelo2.getDuracion()));
    	} else {
    		llegadaVuelo2 = vuelo2.getFechaHoraProgramada().plusMinutes(vuelo2.getDelayed());
    	}       
        return llegadaVuelo1.compareTo(llegadaVuelo2);
    }
}
