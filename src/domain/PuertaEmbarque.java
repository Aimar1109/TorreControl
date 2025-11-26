package domain;

import java.util.Objects;

public class PuertaEmbarque {
	
	public static int contador = 1;
	
	private String codigo;
    private Integer numero;
    private boolean ocupada;
    
    public PuertaEmbarque(boolean ocupada) {

    	this.numero = contador++;
    	this.codigo = "BIO" + this.numero;
        this.ocupada = ocupada;
    }
    public PuertaEmbarque(String codigo, Integer numero, boolean ocupada) {

    	this.numero = numero;
    	this.codigo = codigo;
        this.ocupada = ocupada;
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
