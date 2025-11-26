package jdbc;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;

public class GestorBDInitializer {
	private static final String SQLITE_FILE = "resources/db/torrecontrol.db";
    private static final String CONNECTION_STRING = "jdbc:sqlite:" + SQLITE_FILE;
    
	public GestorBDInitializer() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.err.format("* Error al cargar el driver de la BBDD: %s\\n", e.getMessage());
		}
	}
	
    public void crearTablas() {
    	// Borrar el fichero de la base de datos si existe
    	try {
    		File dbFile = new File(SQLITE_FILE);
    		
    		if (dbFile.exists()) {
    			dbFile.delete();
    		}
    		
    	} catch (Exception e) {
    		
    		System.err.format("* Error al borrar el fichero '%s' de la base de datos: %s\n", SQLITE_FILE, e.getMessage());
    		System.exit(1);
    	}    	
    	
        try (Connection con = DriverManager.getConnection(CONNECTION_STRING)) {
            String sqlVuelos = "CREATE TABLE IF NOT EXISTS VUELO (\n"
                    + " NUMERO INTEGER NOT NULL,\n"
                    + " CODIGO_ORIGEN TEXT NOT NULL,\n"
                    + " CODIGO_DESTINO TEXT NOT NULL,\n"
                    + " CODIGO_AEROLINEA TEXT NOT NULL,\n"
                    + " CODIGO_PISTA TEXT,\n"
                    + " CODIGO_PUERTAEMBARQUE TEXT NOT NULL,\n"                    
                    + " ESTADO BOOLEAN NOT NULL,\n"
                    + " FECHAHORAPROGRAMADA TEXT NOT NULL,\n"
                    + " DURACION FLOAT NOT NULL,\n"
                    + " MATRICULA_AVION TEXT NOT NULL,\n"
                    + " EMERGENCIA BOOLEAN NOT NULL,\n"
                    + " DELAYED INTEGER NOT NULL,\n"
                    
                    + " PRIMARY KEY(NUMERO, CODIGO_AEROLINEA)\n"
                    
                    + " FOREIGN KEY(CODIGO_ORIGEN) REFERENCES AEROPUERTO(CODIGO)\n"
                    + " FOREIGN KEY(CODIGO_DESTINO) REFERENCES AEROPUERTO(CODIGO)\n"
                    + " FOREIGN KEY(CODIGO_AEROLINEA) REFERENCES AEROLINEA(CODIGO)\n"
                    + " FOREIGN KEY(CODIGO_PISTA) REFERENCES PISTA(CODIGO)\n"
                    + " FOREIGN KEY(CODIGO_PUERTAEMBARQUE) REFERENCES PUERTAEMBARQUE(CODIGO)\n"
                    + " FOREIGN KEY(MATRICULA_AVION) REFERENCES AVION(MATRICULA)\n"
                    + ");";
            
            String sqlAeropuertos = "CREATE TABLE IF NOT EXISTS AEROPUERTO (\n"
                    + " CODIGO TEXT PRIMARY KEY NOT NULL,\n"
                    + " NOMBRE TEXT NOT NULL,\n"
                    + " CIUDAD TEXT NOT NULL\n"
                    + ");";
            
            String sqlAerolineas = "CREATE TABLE IF NOT EXISTS AEROLINEA (\n"
                    + " CODIGO TEXT PRIMARY KEY NOT NULL,\n"
                    + " NOMBRE TEXT NOT NULL\n"
                    + ");";
            
            String sqlAviones = "CREATE TABLE IF NOT EXISTS AVION (\n"
                    + " MATRICULA TEXT PRIMARY KEY NOT NULL,\n"
                    + " MODELO TEXT NOT NULL,\n"
                    + " CAPACIDAD INTEGER NOT NULL\n"
                    + ");";
            
            String sqlPuertas = "CREATE TABLE IF NOT EXISTS PUERTAEMBARQUE (\n"
                    + " CODIGO TEXT PRIMARY KEY NOT NULL,\n"
                    + " NUMERO INTEGER NOT NULL,\n"
                    + " OCUPADA BOOLEAN NOT NULL\n"
                    + ");";
            
            String sqlPistas = "CREATE TABLE IF NOT EXISTS PISTA (\n"
                    + " NUMERO TEXT PRIMARY KEY NOT NULL,\n"
                    + " DISPONIBLE BOOLEAN NOT NULL\n"
                    + ");";

            try (Statement stmt = con.createStatement()) {
                stmt.execute(sqlVuelos);
                stmt.execute(sqlAeropuertos);
                stmt.execute(sqlAerolineas);
                stmt.execute(sqlAviones);
                stmt.execute(sqlPuertas);
                stmt.execute(sqlPistas);
            }
            
            // System.out.println("- Tablas creadas.");

        } catch (SQLException e) {
            System.err.format("* Error al crear las tablas: %s\n", e.getMessage());
        }
    }

	
}
