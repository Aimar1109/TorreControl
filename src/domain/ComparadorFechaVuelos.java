package domain;

import java.time.LocalDateTime;
import java.util.Comparator;

public class ComparadorFechaVuelos implements Comparator<Vuelo> {
    @Override
    public int compare(Vuelo vuelo1, Vuelo vuelo2) {
        LocalDateTime llegadaVuelo1 = vuelo1.getFechaHoraProgramada().plusMinutes(vuelo1.getDelayed());
        LocalDateTime llegadaVuelo2 = vuelo2.getFechaHoraProgramada().plusMinutes(vuelo2.getDelayed());
        return llegadaVuelo1.compareTo(llegadaVuelo2);
    }
}
