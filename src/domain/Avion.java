package domain;

public class Avion {

    private String modelo;
    private String matricula;
    private int capacidad;
    
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
}

