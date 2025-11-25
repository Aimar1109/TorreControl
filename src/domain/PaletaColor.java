package domain;

import java.awt.Color;

// IAG
public enum PaletaColor {
    PRIMARIO(44, 62, 80, "Azul profesional"),
    SECUNDARIO(41, 128, 185, "Azul oscuro"),
    FONDO(236, 240, 241, "Gris claro"),
    BLANCO(255, 255, 255, "Blanco"),
    TEXTO(44, 62, 80, "Gris oscuro"),
    TEXTO_SUAVE(127, 140, 141, "Gris medio"),
    ACENTO(230, 126, 34, "Naranja"),
    EXITO(39, 174, 96, "Verde"),
    HOVER(220, 240, 255, "Azul claro"),
    FILA_ALT(250, 250, 250, "Blanco alternado"),
    DELAYED(231, 76, 60, "Rojo");
    
    private final Color color;
    private final String descripcion;
    
    PaletaColor(int r, int g, int b, String descripcion) {
        this.color = new Color(r, g, b);
        this.descripcion = descripcion;
    }
    
    public Color getColor() {
        return color;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    // Método de conveniencia para usar directamente como Color
    public static Color get(PaletaColor appColor) {
        return appColor.color;
    }
}