package threads;

import java.time.LocalDateTime;

public interface ObservadorTiempo {

    void actualizarTiempo(LocalDateTime nuevoTiempo);

    default void cambioEstadoPausa(boolean pausa) {

    }
}
