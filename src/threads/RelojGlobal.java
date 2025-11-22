package threads;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RelojGlobal {

    private static RelojGlobal instanciaUnica;

    private LocalDateTime tiempoActual;

    private boolean pausado;
    private boolean ejecutando;

    private final ReentrantReadWriteLock lock;

    private final List<ObservadorTiempo> observadores;

    private threadGlobal thread;

    private int aceleracion;

    private static final int TICK = 100;
    private static final int ACELERACIONESTANDAR = 1;


    public RelojGlobal() {
        this.observadores = new ArrayList<>();
        this.tiempoActual = LocalDateTime.now();
        this.pausado = false;
        this.ejecutando = false;
        this.aceleracion = ACELERACIONESTANDAR;
        this.lock = new ReentrantReadWriteLock();
    }

    //Se obtiene la unica instancia del controlador, todos los paneles del proyecto utilizan el mismo objeto
    public static synchronized RelojGlobal getInstancia() {
        if (instanciaUnica == null) {
            instanciaUnica = new RelojGlobal();
        }
        return instanciaUnica;
    }

    //Se inicia la ejecución
    public void iniciar() {
        if (ejecutando) {
            return;
        }

        ejecutando = true;
        pausado = false;

        thread = new threadGlobal();
        thread.setDaemon(true);
        thread.start();
    }

    //Detiene la ejecución
    public void detener() {
        ejecutando = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    //Pausa o reanuda la ejecución
    public void pausarReanudar() {
        lock.writeLock().lock();
        try {
            pausado = !pausado;
        } finally {
            lock.writeLock().unlock();
        }

        notificarPausa();
    }

    //Establece si se pausa
    public void setPausa(boolean pausa) {
        lock.writeLock().lock();
        try {
            pausado = pausa;
        } finally {
            lock.writeLock().unlock();
        }

        notificarPausa();
    }

    //Devuelve si está pausado
    public boolean isPausado() {
        lock.readLock().lock();
        try {
            return pausado;
        } finally {
            lock.readLock().unlock();
        }
    }

    //Obtiene el tiempo actual
    public LocalDateTime getTiempoActual() {
        lock.readLock().lock();
        try {
            return tiempoActual;
        } finally {
            lock.readLock().unlock();
        }
    }

    //Establece o modifica el momento actual
    public void setTiempoActual(LocalDateTime tiempoActual) {
        lock.writeLock().lock();
        try {
            this.tiempoActual = tiempoActual;
        } finally {
            lock.writeLock().unlock();
        }

        notificarObservadores();
    }

    //Reiniciar el tiempo actual
    public void reiniciar() {
        lock.writeLock().lock();
        try {
            this.tiempoActual = LocalDateTime.now();
        } finally {
            lock.writeLock().unlock();
        }

        notificarObservadores();
    }

    //Avanza el tiempo en segundos
    public void avanzarTiempoSegundos(long seconds) {
        lock.writeLock().lock();
        try {
            tiempoActual.plusSeconds(seconds);
        } finally {
            lock.writeLock().unlock();
        }

        notificarObservadores();
    }

    //Avanza el tiempo en minutos
    public void avanzarTiempoMinutos(long minutos) {
        lock.writeLock().lock();
        try {
            tiempoActual.plusMinutes(minutos);
        } finally {
            lock.writeLock().unlock();
        }

        notificarObservadores();
    }

    //Avanza el tiempo en horas
    public void avanzarTiempoHoras(long horas) {
        lock.writeLock().lock();
        try {
            tiempoActual.plusHours(horas);
        } finally {
            lock.writeLock().unlock();
        }

        notificarObservadores();
    }

    //Devuelve la aceleración
    public int getAceleracion() {
        lock.readLock().lock();
        try {
            return aceleracion;
        } finally {
            lock.readLock().unlock();
        }
    }

    //Actualiza la aceleración
    public void setAceleracion(int aceleracion) {
        if (aceleracion < 1 || aceleracion > 3600) {
            System.err.println("La aceleración no puede adoptar este valor");
        }
        lock.writeLock().lock();
        try {
            this.aceleracion = aceleracion;
        } finally {
            lock.writeLock().unlock();
        }
    }

    //Añade observadores
    public void addObservador(ObservadorTiempo observador) {
        synchronized (observadores) {
            if (!observadores.contains(observador)) {
                observadores.add(observador);
            }
        }
    }

    //Elimina observador
    public void eliminarObservador(ObservadorTiempo observador) {
        synchronized (observadores) {
            if (observadores.contains(observador)) {
                observadores.remove(observador);
            }
        }
    }

    //Notifica a los observadores del cambio de tiempo
    private void notificarObservadores() {
        LocalDateTime momentoActual = getTiempoActual();
        synchronized (observadores) {
            for (ObservadorTiempo o : observadores) {
                try {
                    o.actualizarTiempo(momentoActual);
                } catch (Exception e) {

                }
            }
        }
    }

    //Notifica si está pausado o si se reanuda
    private void notificarPausa() {
        boolean estadoPausa = isPausado();

        synchronized (observadores) {
            for (ObservadorTiempo o : observadores) {
                try {
                    o.cambioEstadoPausa(estadoPausa);
                } catch (Exception e) {

                }
            }
        }

    }

    //Thread principal que gestiona toda la ejecución, actualizando el tiempo y notificando a los observadores
    private class threadGlobal extends Thread {
        @Override
        public void run() {
            long ultimoTick = System.currentTimeMillis();

            //Bucle principal de la simulación
            while (ejecutando && !this.isInterrupted()) {
                try {
                    if (!pausado) {
                        //Calculo el tiempo trascurrido
                        long tickActual = System.currentTimeMillis();
                        long diferencia = tickActual - ultimoTick;

                        //Actualizo el último tick
                        ultimoTick = tickActual;

                        //Calculo el tiempo real que depende de la aceleración
                        int factor = getAceleracion();
                        double msExtra = factor * diferencia;
                        double sExtra = msExtra / 1000;

                        //Actualizo el tiempo
                        lock.readLock().lock();
                        tiempoActual = tiempoActual.plusSeconds((long) sExtra);
                        lock.writeLock().unlock();

                        //Actualizo los observadores
                        notificarObservadores();
                    }

                    Thread.sleep(TICK);
                } catch (InterruptedException e) {
                    this.interrupt();
                    break;
                }
            }
        }
    }

}
