package domain;

import java.util.HashSet;
import java.util.Set;

import java.util.Objects;

public class Aerolinea {
	
	
	private String codigo;
	private String nombre;
	
	
	public Aerolinea(String codigo, String nombre) {
		if(codigo == null || codigo.trim().isEmpty()) {
			throw new IllegalArgumentException("El codigo no puede estar vacio.");
		}
		if(nombre == null || nombre.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre no puede estar vacio.");
		}
		if(codigo.length()!= 2 && codigo.length()!=3) {
			throw new IllegalArgumentException("El codigo tiene que se de 2 o 3 letras.");
		}
		String codigoNormalizado = codigo.trim().toUpperCase();

		this.codigo = codigoNormalizado;
		this.nombre = nombre.trim();
				
	}

	// Getters
    public String getCodigo() {
        return codigo;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    // Setter solo para nombre (el código no debe cambiar)
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nombre.trim();
    }
        
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aerolinea that = (Aerolinea) o;
        return codigo.equals(that.codigo); // La igualdad se basa solo en el código
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }
    
    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
