package domain;

import java.util.Objects;

public class Aeropuerto {
	
	// ATRIBUTOS
	private String codigo;
	private String nombre;
	private String ciudad;
	
	// CONSTRUCTOR
	public Aeropuerto(String codigo, String nombre, String ciudad) {
		super();
		if(codigo == null || codigo.trim().isEmpty()) {
			throw new IllegalArgumentException("El codigo no puede estar vacio.");
		}
		if(nombre == null || nombre.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre no puede estar vacio.");
		}
		if(ciudad == null || ciudad.trim().isEmpty()) {
			throw new IllegalArgumentException("La ciudad no puede estar vacio.");
		}
		if(!codigo.matches("^[A-Z]{4}$")) { //IAG
			throw new IllegalArgumentException("Código ICAO inválido: debe tener 4 letras mayúsculas");
		}
		
		
		String codigoNormalizado = codigo.trim().toUpperCase();

		this.codigo = codigoNormalizado;
		this.nombre = nombre.trim();
		this.ciudad = ciudad.trim();

	}
	
	// Getters
    public String getCodigo() {
        return codigo;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public String getCiudad() {
    	return ciudad;
    }
    
    //Setter
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nombre.trim();
    }
    
    public void setCiudad(String ciudad) {
        if (ciudad == null || ciudad.trim().isEmpty()) {
            throw new IllegalArgumentException("La ciudad no puede estar vacío");
        }
        this.ciudad = ciudad.trim();
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aeropuerto that = (Aeropuerto) o;
        return codigo.equals(that.codigo); // La igualdad se basa solo en el código
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }
    
    @Override
    public String toString() {
        return nombre;
    }
	
}
