package domain;

import java.util.HashSet;
import java.util.Set;

import java.util.Objects;

public class Aerolinea {
	
	public static Set<String> codigosRegistrados = new HashSet<>();
	
	private String codigo;
	private String nombre;
	
	
	public Aerolinea(String codigo, String nombre) {
		if(codigo == null || codigo.trim().isEmpty()) {
			throw new IllegalArgumentException("El codigo no puede estar vacio.");
		}
		if(nombre == null || nombre.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre no puede estar vacio.");
		}
		String codigoNormalizado = codigo.trim().toUpperCase();
		if(codigosRegistrados.contains(codigoNormalizado)) {
			throw new IllegalArgumentException("El codigo esta utilizado.");
		}
		
		this.codigo = codigoNormalizado;
		this.nombre = nombre.trim();
		
		codigosRegistrados.add(codigoNormalizado);
		
		
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
    
    /**
     * Método para eliminar una aerolínea y liberar su código
     * Útil si necesitas eliminar aerolíneas
     */
    public void eliminar() {
        codigosRegistrados.remove(this.codigo);
    }
    
    /**
     * Verifica si un código ya está registrado
     */
    public static boolean existeCodigo(String codigo) {
        if (codigo == null) return false;
        return codigosRegistrados.contains(codigo.trim().toUpperCase());
    }
    
    /**
     * Obtiene todos los códigos registrados
     */
    public static Set<String> getCodigosRegistrados() {
        return new HashSet<>(codigosRegistrados); // Retorna copia para evitar modificaciones
    }
    
    /**
     * Limpia todos los códigos registrados (útil para testing)
     */
    public static void limpiarRegistros() {
        codigosRegistrados.clear();
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
