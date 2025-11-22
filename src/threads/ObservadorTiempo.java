package threads;

import java.time.LocalDateTime;

public interface ObservadorTiempo {

    void actualizarTiempo(LocalDateTime nuevoTiempo);

    void cambioEstadoPausa(boolean pausa);
}
