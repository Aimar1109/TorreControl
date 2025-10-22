package domain;

public class PuertaEmbarque {
    
    private String numero;
    private boolean ocupada;
    
    public PuertaEmbarque(String numero, boolean ocupada) {
        this.numero = numero;
        this.ocupada = ocupada;
    }
    
    public String getNumero() {
        return numero;
    }
    
    public void setNumero(String numero) {
        this.numero = numero;
    }
    
    public boolean isOcupada() {
        return ocupada;
    }
    
    public void setOcupada(boolean ocupada) {
        this.ocupada = ocupada;
    }
}