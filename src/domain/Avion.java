package domain;

import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
    
    private static Set<String> matriculasRegistradas = new HashSet<>();

    public Avion() {
        this.modelo = "";
        this.matricula = "";
        this.capacidad = 0;
        this.speed = 1.0;
    }

    public Avion(String modelo, String matricula, int capacidad) {
    	if (matricula == null || matricula.trim().isEmpty() || !matricula.matches(regex)) {
    		throw new IllegalArgumentException("La matricula no puede estar vacio y tiene que cumplir la condicion");
    	}
        this.modelo = modelo;
        this.matricula = matricula;
        this.capacidad = capacidad;
        this.speed = 1.0;
        
        matriculasRegistradas.add(matricula);
    }

    public Avion(String modelo, String matricula, int capacidad, int x, int y, double angulo) {
        this.modelo = modelo;
        this.matricula = matricula;
        this.capacidad = capacidad;
        this.x = x;
        this.y = y;
        this.angulo = angulo;
        this.futureX = x;
        this.futureY = y;
        this.speed = 1.0;
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
        
        matriculasRegistradas.add(matricula);
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
            return;
        }

        //Si la distancia es mayor que la velocidad(pixels por frame) el avión se movera, sino no porque se pasaría
        if (d > speed) {
            x += (int) (normalDx * speed);
            y += (int) (normalDy * speed);

            //Se le suman pi/2 radianes ya que es el desfase. La imagen del avión apunta hacia arriba por lo que tiene un desfase de 90 grados
            angulo = Math.atan2(dx, dy) + Math.PI/2;
        } else {
            //Si está muy cerca (speed>d) se coloca directamente encima
            x = futureX;
            y = futureY;
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
    
    public static boolean existeMatricula(String matricula) {
    	if(matricula==null) return false;
    	return matriculasRegistradas.contains(matricula.trim().toUpperCase());
    }
    
    public static Set<String> getMatriculasRegistradas() {
    	return new HashSet<>(matriculasRegistradas);
    }
    
    public static void clear() {
    	matriculasRegistradas.clear();
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
}
