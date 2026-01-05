package domain;

import java.util.Objects;

public class PuertaEmbarque {
	
	public static int contador = 1;
	
	private String codigo;
    private Integer numero;
    private boolean ocupada;
    private Vuelo llegada;
    private Vuelo salida;
    
    public PuertaEmbarque(boolean ocupada) {

    	this.numero = contador++;
    	this.codigo = "BIO" + this.numero;
        this.ocupada = ocupada;
        this.llegada = null;
        this.salida = null;
    }
    public PuertaEmbarque(String codigo, Integer numero, boolean ocupada) {

    	this.numero = numero;
    	this.codigo = codigo;
        this.ocupada = ocupada;
        this.llegada = null;
        this.salida = null;
    }
    public PuertaEmbarque(String codigo, Integer numero, boolean ocupada, Vuelo llegada, Vuelo salida) {
		super();
		this.codigo = codigo;
		this.numero = numero;
		this.ocupada = ocupada;
		this.llegada = llegada;
		this.salida = salida;
	}
    
    
	public String getCodigo() {
		return codigo;
	}
	public Integer getNumero() {
		return numero;
	}
	public boolean isOcupada() {
		return ocupada;
	}
	public void setOcupada(boolean ocupada) {
		this.ocupada = ocupada;
	}    
    public Vuelo getLlegada() {
		return llegada;
	}
	public void setLlegada(Vuelo llegada) {
		this.llegada = llegada;
	}
	public Vuelo getSalida() {
		return salida;
	}
	public void setSalida(Vuelo salida) {
		this.salida = salida;
	}
	
	
	@Override
    public String toString() {
    	return codigo;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PuertaEmbarque that = (PuertaEmbarque) o;
        return codigo.equals(that.codigo); // La igualdad se basa solo en el código
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }
}
