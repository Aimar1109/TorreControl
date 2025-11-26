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
    DELAYED(231, 76, 60, "Rojo"),
	FONDO_OSCURO(30, 35, 40, "Fondo Global Oscuro"),
    TILE_INICIO(50, 55, 60, "Gradiente Tile Inicio"),
    TILE_FIN(40, 45, 50, "Gradiente Tile Fin"),
    TILE_BORDE(60, 65, 70, "Borde Tile"),
    NEGRO_SUAVE(20, 20, 20, "Negro para barras/iconos"),
    OCUPADO(52, 152, 219, "Azul claro seatmap"),
    LIBRE(236, 240, 241, "Gris claro seatmap");
    
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