package domain;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;


public class Avion {

    private String modelo;
    private String matricula;
    private int capacidad;
    private int x;
    private int y;
    private double angulo;
    private Image imagen;
    private int combustible;
    private int futureX;
    private int futureY;
    //Speed en pixels/Frame
    private double speed;
    private String regex = "^(?:[A-Z]{1,2}-[A-Z0-9]{3,4}|N\\d{1,5}[A-Z]{0,2})$"; //IAG

    private ArrayList<Point> rutaActual;
    private int pointIndex;
    private boolean enHangar;
    private Point estacionamientoHangar;
    private EstadoAvion estadoAvion;
    private ArrayList<Double> velocidadesRuta = new ArrayList<>();

    private boolean marchaAtras;
    private Point destinoMarchaAtras;


    public Avion() {
        this.modelo = "";
        this.matricula = "";
        this.capacidad = 0;
        this.speed = 1.0;
        this.rutaActual = new ArrayList<>();
        this.pointIndex = 0;
        this.enHangar = false;
    }

    public Avion(String modelo, String matricula, int capacidad) {
        if (matricula == null || matricula.trim().isEmpty() || !matricula.matches(regex)) {
            throw new IllegalArgumentException("La matricula no puede estar vacio y tiene que cumplir la condicion");
        }
        this.modelo = modelo;
        this.matricula = matricula;
        this.capacidad = capacidad;
        this.speed = 1.0;

        this.rutaActual = new ArrayList<>();
        this.pointIndex = 0;
        this.enHangar = false;

//        matriculasRegistradas.add(matricula);
    }

    public Avion(String modelo, String matricula, int capacidad, int x, int y, double angulo) {
        /*if (matricula == null || matricula.trim().isEmpty() || !matricula.matches(regex)) {
            throw new IllegalArgumentException("La matricula no puede estar vacio y tiene que cumplir la condicion");
        }*/

        this.modelo = modelo;
        this.matricula = matricula;
        this.capacidad = capacidad;
        this.x = x;
        this.y = y;
        this.angulo = angulo;
        this.futureX = x;
        this.futureY = y;
        this.speed = 1.0;

        this.rutaActual = new ArrayList<>();
        this.pointIndex = 0;
        this.enHangar = false;

        //matriculasRegistradas.add(matricula);
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        if (matricula == null || matricula.trim().isEmpty() || !matricula.matches(regex)) {
            throw new IllegalArgumentException("La matricula no puede estar vacio y tiene que cumplir la condicion");
        }
        this.matricula = matricula;

    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getAngulo() {
        return angulo;
    }

    public void setAngulo(double angulo) {
        this.angulo = angulo;
    }

    public Image getImagen() {
        return imagen;
    }

    public void setImagen(Image imagen) {
        this.imagen = imagen;
    }

    public Point getDestinoMarchaAtras() {
        return destinoMarchaAtras;
    }

    public void setDestinoMarchaAtras(Point destinoMarchaAtras) {
        this.destinoMarchaAtras = destinoMarchaAtras;
    }

    public boolean isMarchaAtras() {
        return marchaAtras;
    }

    public void setMarchaAtras(boolean marchaAtras) {
        this.marchaAtras = marchaAtras;
    }

    public void mover(int x, int y) {
        this.x += x;
        this.y += y;
    }

    //Hace que el movimento del avión sea más suave
    public void actualizarPosicion() {
        //Vector desde la posición actual al objetivo
        double dx = futureX - x;
        double dy = futureY - y;
        //Módulo del vector (Teorema de pitagoras)
        double d = Math.sqrt(Math.pow(dx,2) + Math.pow(dy,2));
        //Normalizo el vector
        double normalDx = dx/d;
        double normalDy = dy/d;

        if (d < 0.0001) {
            //Si hemos llegado al destino y era tambien el final de la marcha atras se desactiva
            if (marchaAtras && destinoMarchaAtras != null && futureX == destinoMarchaAtras.x && futureY == destinoMarchaAtras.y) {
                marchaAtras = false;
                destinoMarchaAtras = null;
            }
            return;
        }

        //Si la distancia es mayor que la velocidad(pixels por frame) el avión se movera, sino no porque se pasaría
        if (d > speed) {
            double moveX = normalDx * speed;
            double moveY = normalDy * speed;

            int moveXi = (int) Math.round(moveX);
            int moveYi = (int) Math.round(moveY);

            // Si el redondeo da 0, pero hay aunque sea un movimiento mínimo, se fuerza que se mueva al menos 1 px
            if (moveXi == 0 && Math.abs(moveX) > 0){
                moveXi = (int) Math.signum(moveX);
            }
            if (moveYi == 0 && Math.abs(moveY) > 0) {
                moveYi = (int) Math.signum(moveY);
            }

            x += moveXi;
            y += moveYi;

            //Se le suman piradianes ya que es el desfase.
            double anguloIntermedio = Math.atan2(normalDy, normalDx) + Math.PI / 2;

            //Si va marcha atrás se invierte el ángulo
            if(marchaAtras && destinoMarchaAtras != null) {
                anguloIntermedio = anguloIntermedio + Math.PI;
            }

            angulo = anguloIntermedio;
        } else {
            //Si está muy cerca (speed>d) se coloca directamente encima
            x = futureX;
            y = futureY;

            //Si hemos llegado al destino y era también el final de la marcha atras, se desactiva
            if (marchaAtras && destinoMarchaAtras != null && futureX == destinoMarchaAtras.x && futureY == destinoMarchaAtras.y) {
                marchaAtras = false;
                destinoMarchaAtras = null;
            }
        }
    }

    public void setDestino(int x, int y) {
        futureY = y;
        futureX = x;
    }

    public int getCombustible() {
        return combustible;
    }

    public void setCombustible(int combustible) {
        this.combustible = combustible;
    }

    public int getFutureX() {
        return futureX;
    }

    public void setFutureX(int futureX) {
        this.futureX = futureX;
    }

    public int getFutureY() {
        return futureY;
    }

    public void setFutureY(int futureY) {
        this.futureY = futureY;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Avion that = (Avion) o;
        return matricula.equals(that.matricula);

    }

    @Override
    public int hashCode() {
        return Objects.hash(matricula);
    }

    @Override
    public String toString() {
        return modelo + " - " + matricula;
    }

    public void setRuta(ArrayList<Point> ruta) {
        this.rutaActual = ruta;
        this.pointIndex = 0;
        if (!ruta.isEmpty()) {
            setDestino(ruta.get(0).x, ruta.get(0).y);
        }
        this.velocidadesRuta = new ArrayList<>();
        for (int i = 0; i < (ruta.size() - 1); i++) {
            this.velocidadesRuta.add(1.3);
        }
    }

    public boolean enDestino() {
        boolean devolver = true;
        double distancia = Math.sqrt(
                Math.pow(x - futureX, 2) + Math.pow(y - futureY, 2)
        );

        if (distancia > 0) {
            devolver = false;
        }

        return devolver;
    }

    public boolean siguientePunto() {
        if (rutaActual != null && pointIndex < rutaActual.size() - 1) {
            pointIndex++;
            Point siguiente = rutaActual.get(pointIndex);
            setDestino(siguiente.x, siguiente.y);
            return true;
        }
        return false;
    }

    public boolean isEnHangar() {
        return enHangar;
    }

    public void setEnHangar(boolean enHangar) {
        this.enHangar = enHangar;
    }

    public Point getEstacionamientoHangar() {
        return estacionamientoHangar;
    }

    public void setEstacionamientoHangar(Point estacionamientoHangar) {
        this.estacionamientoHangar = estacionamientoHangar;
    }

    public EstadoAvion getEstadoAvion() {
        return estadoAvion;
    }

    public void setEstadoAvion(EstadoAvion estadoAvion) {
        this.estadoAvion = estadoAvion;
    }

    public int getPointIndex() {
        return pointIndex;
    }

    public ArrayList<Point> getRutaActual() {
        if (rutaActual != null) {
            return rutaActual;
        } else {
            return new ArrayList<>();
        }
    }

    public int getRutaLength() {
        if (rutaActual != null) {
            return rutaActual.size();
        } else {
            return 0;
        }
    }

    public ArrayList<Double> getVelocidadesRuta() {
        return velocidadesRuta;
    }

    public void setVelocidadSegmento(int index, double velocidad) {
        int tramos = rutaActual.size() - 1;

        //Si la longitud de la lista de velocidades es distinta a la cantidad de tramos se redimensiona
        if (velocidadesRuta.size() != tramos) {
            ArrayList<Double> nueva = new ArrayList<>();
            for (int i = 0; i < tramos; i++) {
                if (i < velocidadesRuta.size()) {
                    nueva.add(velocidadesRuta.get(i));
                } else {
                    nueva.add(1.3);
                }
            }
            velocidadesRuta = nueva;
        }

        velocidadesRuta.set(index, velocidad);
    }

    public void resetPointIndex() {
        this.pointIndex = 0;
    }
}
