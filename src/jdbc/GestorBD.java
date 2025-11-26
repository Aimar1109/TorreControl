package jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import domain.Aerolinea;
import domain.Aeropuerto;
import domain.Avion;
import domain.Pista;
import domain.PuertaEmbarque;
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
    	
    	String sql = "INSERT OR IGNORE INTO VUELO"
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
    
    public void insertAeropuerto(Aeropuerto aeropuerto) {
    	
    	String sql = "INSERT OR IGNORE INTO AEROPUERTO"
    	+ "(CODIGO, NOMBRE, CIUDAD)"
    	+ "VALUES (?, ?, ?)";
    	
    	try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            
    		pstmt.setString(1,  aeropuerto.getCodigo());
    		pstmt.setString(2,  aeropuerto.getNombre());
    		pstmt.setString(3,  aeropuerto.getCiudad());

    		pstmt.executeUpdate();
    		
    		//System.out.format("- Vuelo '%s' insertado\n", vuelo.getCodigo());
            
    		
           } catch (SQLException e) {
               System.err.format("* Error al insertar Vuelo '%s': %s\n", aeropuerto.getCodigo(), e.getMessage());
           }
    }
    
    public void insertAerolinea(Aerolinea aerolinea) {
    	
    	String sql = "INSERT OR IGNORE INTO AEROLINEA"
    	+ "(CODIGO, NOMBRE)"
    	+ "VALUES (?, ?)";
    	
    	try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            
    		pstmt.setString(1,  aerolinea.getCodigo());
    		pstmt.setString(2,  aerolinea.getNombre());

    		pstmt.executeUpdate();
    		
    		//System.out.format("- Vuelo '%s' insertado\n", vuelo.getCodigo());
            
    		
           } catch (SQLException e) {
               System.err.format("* Error al insertar Vuelo '%s': %s\n", aerolinea.getCodigo(), e.getMessage());
           }
    }
    
    public void insertAvion(Avion avion) {
    	
    	String sql = "INSERT OR IGNORE INTO AVION"
    	+ "(MATRICULA, MODELO)"
    	+ "VALUES (?, ?)";
    	
    	try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            
    		pstmt.setString(1,  avion.getMatricula());
    		pstmt.setString(2,  avion.getModelo());

    		pstmt.executeUpdate();
    		
    		//System.out.format("- Vuelo '%s' insertado\n", vuelo.getCodigo());
            
    		
           } catch (SQLException e) {
               System.err.format("* Error al insertar Vuelo '%s': %s\n", avion.getMatricula(), e.getMessage());
           }
    }

    public void insertPuerta(PuertaEmbarque puerta) {
    	
    	String sql = "INSERT OR IGNORE INTO PUERTAEMBARQUE"
    	+ "(CODIGO, NUMERO, OCUPADA)"
    	+ "VALUES (?, ?, ?)";
    	
    	try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            
    		pstmt.setString(1,  puerta.getCodigo());
    		pstmt.setInt(2,  puerta.getNumero());
    		pstmt.setBoolean(3,  puerta.isOcupada());

    		pstmt.executeUpdate();
    		
    		//System.out.format("- Vuelo '%s' insertado\n", vuelo.getCodigo());
            
    		
           } catch (SQLException e) {
               System.err.format("* Error al insertar Vuelo '%s': %s\n", puerta.getCodigo(), e.getMessage());
           }
    }
    
    public void insertPista(Pista pista) {
    	
    	String sql = "INSERT OR IGNORE INTO PISTA"
    	+ "(NUMERO, DISPONIBLE)"
    	+ "VALUES (?, ?)";
    	
    	try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            
    		pstmt.setString(1,  pista.getNumero());
    		pstmt.setBoolean(2,  pista.isDisponible());

    		pstmt.executeUpdate();
    		
    		//System.out.format("- Vuelo '%s' insertado\n", vuelo.getCodigo());
            
    		
           } catch (SQLException e) {
               System.err.format("* Error al insertar Vuelo '%s': %s\n", pista.getNumero(), e.getMessage());
           }
    }
    
}
