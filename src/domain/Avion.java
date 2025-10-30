package domain;

import java.awt.*;

public class Avion {

    private String modelo;
    private String matricula;
    private int capacidad;
    private int x;
    private int y;
    private double angulo;
    private Image imagen;
    private int combustible;

    public Avion() {
        this.modelo = "";
        this.matricula = "";
        this.capacidad = 0;
    }

    public Avion(String modelo, String matricula, int capacidad) {
        this.modelo = modelo;
        this.matricula = matricula;
        this.capacidad = capacidad;
    }

    public Avion(String modelo, String matricula, int capacidad, int x, int y, double angulo) {
        this.modelo = modelo;
        this.matricula = matricula;
        this.capacidad = capacidad;
        this.x = x;
        this.y = y;
        this.angulo = angulo;
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

    public void mover(int x, int y) {
        this.x += x;
        this.y += y;
    }
}