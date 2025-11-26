package jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import domain.Vuelo;

public class GestorBD {
	
	private static final String SQLITE_FILE = "resources/db/torrecontrol.db";
    private static final String CONNECTION_STRING = "jdbc:sqlite:" + SQLITE_FILE;
    
    public GestorBD() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.err.format("* Error al cargar el driver de la BBDD: %s\n", e.getMessage());
		}
	}
    
    
    public void insertVuelo(Vuelo vuelo) {
    	
    	String sql = "INSERT INTO VUELO"
    	+ "(CODIGO, CODIGO_ORIGEN, CODIGO_DESTINO, CODIGO_AEROLINEA, CODIGO_PISTA, CODIGO_PUERTAEMBARQUE, ESTADO, FECHAHORAPROGRAMADA,"
    	+ "DURACION, MATRICULA_AVION, EMERGENCIA, DELAYED)"
    	+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    	
    	try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            
    		pstmt.setString(1,  vuelo.getCodigo());
    		pstmt.setString(2,  vuelo.getOrigen().getCodigo());
    		pstmt.setString(3,  vuelo.getDestino().getCodigo());
    		pstmt.setString(4,  vuelo.getAereolinea().getCodigo());
    		String pista = vuelo.getPista()!=null ? vuelo.getPista().getNumero() : null;
    		pstmt.setString(5, pista);
    		pstmt.setString(6,  vuelo.getPuerta().getCodigo());
    		pstmt.setBoolean(7,  vuelo.isEstado());
    		pstmt.setString(8,  vuelo.getFechaHoraProgramada().toString());
    		pstmt.setFloat(9,  vuelo.getDuracion());
    		pstmt.setString(10,  vuelo.getAvion().getMatricula());
    		pstmt.setBoolean(11,  vuelo.isEmergencia());
    		pstmt.setInt(12,  vuelo.getDelayed());
    		pstmt.executeUpdate();
    		
    		//System.out.format("- Vuelo '%s' insertado\n", vuelo.getCodigo());
            
    		
           } catch (SQLException e) {
               System.err.format("* Error al insertar Vuelo '%s': %s\n", vuelo.getCodigo(), e.getMessage());
           }
    }
    
}
