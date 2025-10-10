package domain;

public class Aereopuerto {
	
	// ATRIBUTOS
	private String codigo;
	private String nombre;
	private String ciudad;
	
	// CONSTRUCTOR
	public Aereopuerto(String codigo, String nombre, String ciudad) {
		super();
		this.codigo = codigo;
		this.nombre = nombre;
		this.ciudad = ciudad;
	}
	
	// GETTERS Y SETTERS
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCiudad() {
		return ciudad;
	}

	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}

}
