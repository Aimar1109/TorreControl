package jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import domain.Aerolinea;
import domain.Aeropuerto;
import domain.Avion;
import domain.Pasajero;
import domain.Pista;
import domain.PuertaEmbarque;
import domain.Tripulante;
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
    
    
    // INSERTAR
    public void insertVuelo(Vuelo vuelo) {
    	
    	String sql = "INSERT OR IGNORE INTO VUELO"
    	+ "(CODIGO, NUMERO, CODIGO_ORIGEN, CODIGO_DESTINO, CODIGO_AEROLINEA, NUMERO_PISTA, CODIGO_PUERTAEMBARQUE, ESTADO, FECHAHORAPROGRAMADA,"
    	+ "DURACION, MATRICULA_AVION, EMERGENCIA, DELAYED)"
    	+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    	
    	try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            
    		
    		pstmt.setString(1, vuelo.getCodigo());
    		pstmt.setInt(2,  vuelo.getNumero());
    		pstmt.setString(3,  vuelo.getOrigen().getCodigo());
    		pstmt.setString(4,  vuelo.getDestino().getCodigo());
    		pstmt.setString(5,  vuelo.getAereolinea().getCodigo());
    		String pista = vuelo.getPista()!=null ? vuelo.getPista().getNumero() : null;
    		pstmt.setString(6, pista);
    		pstmt.setString(7,  vuelo.getPuerta().getCodigo());
    		pstmt.setBoolean(8,  vuelo.isEstado());
    		pstmt.setString(9,  vuelo.getFechaHoraProgramada().toString());
    		pstmt.setFloat(10,  vuelo.getDuracion());
    		pstmt.setString(11,  vuelo.getAvion().getMatricula());
    		pstmt.setBoolean(12,  vuelo.isEmergencia());
    		pstmt.setInt(13,  vuelo.getDelayed());
    		pstmt.executeUpdate();
    		
    		if (vuelo.getPasajeros().size()>0) {
    			insertVueloListaPasajeros(vuelo.getPasajeros(), vuelo);
    		}
    		if (vuelo.getTripulacion().size()>0) {
    			insertVueloListaTripulacion(vuelo.getTripulacion(), vuelo);
    		}
    		
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
    	+ "(MATRICULA, MODELO, CAPACIDAD)"
    	+ "VALUES (?, ?, ?)";
    	
    	try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            
    		pstmt.setString(1,  avion.getMatricula());
    		pstmt.setString(2,  avion.getModelo());
    		pstmt.setInt(3,  avion.getCapacidad());

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
    
    public void insertTripulante(Tripulante tripulante) {
    	
    	String sql = "INSERT OR IGNORE INTO TRIPULANTE"
    	+ "(NOMBRE)"
    	+ "VALUES (?)";
    	
    	try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            
    		pstmt.setString(1,  tripulante.getNombre());

    		pstmt.executeUpdate();
    		
    		//System.out.format("- Vuelo '%s' insertado\n", vuelo.getCodigo());
            
    		
           } catch (SQLException e) {
               System.err.format("* Error al insertar Vuelo '%s': %s\n", tripulante.getNombre(), e.getMessage());
           }
    }
    
    public void insertPasajero(Pasajero pasajero) {
    	
    	String sql = "INSERT OR IGNORE INTO PASAJERO"
    	+ "(NOMBRE)"
    	+ "VALUES (?)";
    	
    	try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            
    		pstmt.setString(1,  pasajero.getNombre());

    		pstmt.executeUpdate();
    		
    		//System.out.format("- Vuelo '%s' insertado\n", vuelo.getCodigo());
            
    		
           } catch (SQLException e) {
               System.err.format("* Error al insertar Vuelo '%s': %s\n", pasajero.getNombre(), e.getMessage());
           }
    }
    
    public void insertVueloPasajero(Pasajero pasajero, Vuelo vuelo) {
    	
    	String sql = "INSERT OR IGNORE INTO VUELO_PASAJERO"
    	+ "(CODIGO_VUELO, NOMBRE_PASAJERO)"
    	+ "VALUES (?, ?)";
    	
    	try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            
    		pstmt.setString(1,  vuelo.getCodigo());
    		pstmt.setString(2,  pasajero.getNombre());

    		pstmt.executeUpdate();
    		
    		//System.out.format("- Vuelo '%s' insertado\n", vuelo.getCodigo());
            
    		
           } catch (SQLException e) {
               System.err.format("* Error al insertar pasajero a Vuelo '%s': %s\n", pasajero.getNombre(), e.getMessage());
           }
    }
    
    public void insertVueloListaPasajeros(ArrayList<Pasajero> pasajeros, Vuelo vuelo) {
    	
    	String sql = "INSERT OR IGNORE INTO VUELO_PASAJERO"
    	+ "(CODIGO_VUELO, NOMBRE_PASAJERO)"
    	+ "VALUES (?, ?)";
    	
    	try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            
    		for(Pasajero p: pasajeros) {
    			pstmt.setString(1,  vuelo.getCodigo());
        		pstmt.setString(2,  p.getNombre());

        		pstmt.executeUpdate();
    		}
    		
    		
    		//System.out.format("- Vuelo '%s' insertado\n", vuelo.getCodigo());
            
    		
           } catch (SQLException e) {
               System.err.format("* Error al insertar lista de pasajeros a Vuelo %s: %s\n", vuelo.getCodigo(), e.getMessage());
           }
    }
    
    public void insertVueloTripulante(Tripulante tripulante, Vuelo vuelo) {
    	
    	String sql = "INSERT OR IGNORE INTO VUELO_TRIPULANTE"
    	+ "(CODIGO_VUELO, NOMBRE_TRIPULANTE)"
    	+ "VALUES (?, ?)";
    	
    	try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            
    		pstmt.setString(1,  vuelo.getCodigo());
    		pstmt.setString(2,  tripulante.getNombre());

    		pstmt.executeUpdate();
    		
    		//System.out.format("- Vuelo '%s' insertado\n", vuelo.getCodigo());
            
    		
           } catch (SQLException e) {
               System.err.format("* Error al insertar tripulante a Vuelo '%s': %s\n", tripulante.getNombre(), e.getMessage());
           }
    }
    
    public void insertVueloListaTripulacion(ArrayList<Tripulante> tripulantes, Vuelo vuelo) {
    	
    	String sql = "INSERT OR IGNORE INTO VUELO_TRIPULANTE"
    	+ "(CODIGO_VUELO, NOMBRE_TRIPULANTE)"
    	+ "VALUES (?, ?)";
    	
    	try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            
    		for(Tripulante t: tripulantes) {
    			pstmt.setString(1,  vuelo.getCodigo());
        		pstmt.setString(2,  t.getNombre());

        		pstmt.executeUpdate();
    		}
    		
    		
    		//System.out.format("- Vuelo '%s' insertado\n", vuelo.getCodigo());
            
    		
           } catch (SQLException e) {
               System.err.format("* Error al insertar lista de tripulantes %s: %s\n", vuelo.getCodigo(), e.getMessage());
           }
    }
    
    // LOAD
    // terminar
    public List<Vuelo> loadVuelos() {
    	List<Vuelo> vuelos = new ArrayList<Vuelo>();
		
		String sqlVuelo = "SELECT * FROM VUELO";

        try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
             PreparedStatement pstVuelo = con.prepareStatement(sqlVuelo);) {
            ResultSet rsVuelo = pstVuelo.executeQuery();
            
            ResultSetMetaData rsmd = rsVuelo.getMetaData();
            boolean existePista = false;
            
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                if (rsmd.getColumnName(i).equalsIgnoreCase("NUMERO_PISTA")) {
                	existePista = true;
                    break;
                }
            }

            
            while (rsVuelo.next()) {
            	Integer numero = rsVuelo.getInt("NUMERO");
            	Aeropuerto origen = getAeropuertoByCodigo(rsVuelo.getString("CODIGO_ORIGEN"));
            	Aeropuerto destino = getAeropuertoByCodigo(rsVuelo.getString("CODIGO_DESTINO"));
            	Aerolinea aerolinea = getAerolineaByCodigo(rsVuelo.getString("CODIGO_AEROLINEA"));
            	Pista pista = existePista ? getPistaByNumero(rsVuelo.getString("NUMERO_PISTA")) : null;
            	PuertaEmbarque puerta = getPuertaEmbarqueByCodigo(rsVuelo.getString("CODIGO_PUERTAEMBARQUE"));
            	Boolean estado = rsVuelo.getBoolean("ESTADO");
            	LocalDateTime fecha = LocalDateTime.parse(rsVuelo.getString("FECHAHORAPROGRAMADA"));
            	Float duracion = rsVuelo.getFloat("DURACION");
            	Avion avion = getAvionByMatricula(rsVuelo.getString("MATRICULA_AVION"));
            	Boolean emergencia = rsVuelo.getBoolean("EMERGENCIA");
            	Integer delayed = rsVuelo.getInt("DELAYED");
            	
            	Vuelo vuelo = new Vuelo(numero, origen, destino, aerolinea, pista, puerta, estado, fecha, duracion, avion, emergencia, delayed);
            	
            	ArrayList<Pasajero> pasajeros = getVueloListaPasajeros(vuelo);
            	ArrayList<Tripulante> tripulacion = getVueloListaTripulacion(vuelo);
            	
            	vuelo.setPasajeros(pasajeros);
            	vuelo.setTripulacion(tripulacion);
            	
            	vuelos.add(vuelo);            
            }
            
            rsVuelo.close();
        } catch (Exception e) {
        	System.err.format("\n* Error recuperando vuelos: %s.", e.getMessage());
        }
				
		return vuelos;
	}
    
    public List<Aeropuerto> loadAeropuertos() {
    	List<Aeropuerto> aeropuertos = new ArrayList<Aeropuerto>();
		
		String sqlAeropuerto = "SELECT * FROM AEROPUERTO";

        try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
             PreparedStatement pstAeropuerto = con.prepareStatement(sqlAeropuerto);) {
            ResultSet rsAeropuerto = pstAeropuerto.executeQuery();
            
            while (rsAeropuerto.next()) {
            	String codigo = rsAeropuerto.getString("CODIGO");
            	String nombre = rsAeropuerto.getString("NOMBRE");
            	String ciudad = rsAeropuerto.getString("CIUDAD");
            	
            	Aeropuerto aeropuerto = new Aeropuerto(codigo, nombre, ciudad);
            	aeropuertos.add(aeropuerto);
            }
            
            rsAeropuerto.close();
        } catch (Exception e) {
        	System.err.format("\n* Error recuperando los aeropuertos: %s.", e.getMessage());
        }
				
		return aeropuertos;
	}
    
    public List<Aerolinea> loadAerolineas() {
    	List<Aerolinea> aerolineas = new ArrayList<Aerolinea>();
		
		String sqlAerolinea = "SELECT * FROM AEROLINEA";

        try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
             PreparedStatement pstAerolinea = con.prepareStatement(sqlAerolinea);) {
            ResultSet rsAerolinea = pstAerolinea.executeQuery();
            
            while (rsAerolinea.next()) {
            	String codigo = rsAerolinea.getString("CODIGO");
            	String nombre = rsAerolinea.getString("NOMBRE");
            	
            	Aerolinea aerolinea = new Aerolinea(codigo, nombre);
            	aerolineas.add(aerolinea);
            }
            
            rsAerolinea.close();
        } catch (Exception e) {
        	System.err.format("\n* Error recuperando las aerolineas: %s.", e.getMessage());
        }
				
		return aerolineas;
	}
    
    public List<Avion> loadAviones() {
    	List<Avion> aviones = new ArrayList<Avion>();
		
		String sqlAvion = "SELECT * FROM AVION";

        try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
             PreparedStatement pstAvion = con.prepareStatement(sqlAvion);) {
            ResultSet rsAvion = pstAvion.executeQuery();
            
            while (rsAvion.next()) {
            	String matricula = rsAvion.getString("MATRICULA");
            	String modelo = rsAvion.getString("MODELO");
            	Integer capacidad = rsAvion.getInt("CAPACIDAD");
            	
            	Avion avion = new Avion(modelo, matricula, capacidad);
            	aviones.add(avion);
            }
            
            rsAvion.close();
        } catch (Exception e) {
        	System.err.format("\n* Error recuperando los aviones: %s.", e.getMessage());
        }
				
		return aviones;
	}
    
    public List<PuertaEmbarque> loadPuertasEmbarque() {
    	List<PuertaEmbarque> puertas = new ArrayList<PuertaEmbarque>();
		
		String sqlPuertaEmbarque = "SELECT * FROM PUERTAEMBARQUE";

        try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
             PreparedStatement pstPuertaEmbarque = con.prepareStatement(sqlPuertaEmbarque);) {
            ResultSet rsPuertaEmbarque = pstPuertaEmbarque.executeQuery();
            
            while (rsPuertaEmbarque.next()) {
            	String codigo = rsPuertaEmbarque.getString("CODIGO");
            	Integer numero = rsPuertaEmbarque.getInt("NUMERO");
            	Boolean ocupada = rsPuertaEmbarque.getBoolean("OCUPADA");
            	
            	PuertaEmbarque puerta = new PuertaEmbarque(codigo, numero, ocupada);
            	puertas.add(puerta);
            }
            
            rsPuertaEmbarque.close();
        } catch (Exception e) {
        	System.err.format("\n* Error recuperando las puertas: %s.", e.getMessage());
        }
				
		return puertas;
	}
    
    public List<Pista> loadPistas() {
    	List<Pista> pistas = new ArrayList<Pista>();
		
		String sqlPista = "SELECT * FROM AEROLINEA";

        try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
             PreparedStatement pstPista = con.prepareStatement(sqlPista);) {
            ResultSet rsPista = pstPista.executeQuery();
            
            while (rsPista.next()) {
            	String numero = rsPista.getString("NUMERO");
            	Boolean disponible = rsPista.getBoolean("DISPONIBLE");
            	
            	Pista pista = new Pista(numero, disponible);
            	pistas.add(pista);
            }
            
            rsPista.close();
        } catch (Exception e) {
        	System.err.format("\n* Error recuperando las pistas: %s.", e.getMessage());
        }
				
		return pistas;
	}
    
    // GET
    
	public Aeropuerto getAeropuertoByCodigo(String codigo) {
		Aeropuerto aeropuerto = null;
		String sql = "SELECT * FROM AEROPUERTO WHERE CODIGO = ? LIMIT 1";
		
		try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
		     PreparedStatement pStmt = con.prepareStatement(sql)) {			
			pStmt.setString(1, codigo);
			
			ResultSet rsAeropuerto = pStmt.executeQuery();			
			
			//Se procesa el único resultado
			if (rsAeropuerto.next()) {
		
				aeropuerto = new Aeropuerto(rsAeropuerto.getString("CODIGO"),
											rsAeropuerto.getString("NOMBRE"),
											rsAeropuerto.getString("CIUDAD")
						);
				
				}
			
			rsAeropuerto.close();
			
		} catch (Exception ex) {
			System.err.format("Error recuperar el aeropuerto con codigo %d: %s", codigo, ex.getMessage());						
		}		
		
		return aeropuerto;
	}
	
	public Aerolinea getAerolineaByCodigo(String codigo) {
		Aerolinea aerolinea = null;
		String sql = "SELECT * FROM AEROLINEA WHERE CODIGO = ? LIMIT 1";
		
		try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
		     PreparedStatement pStmt = con.prepareStatement(sql)) {			
			
			pStmt.setString(1, codigo);
			
			ResultSet rsAerolinea = pStmt.executeQuery();			

			//Se procesa el único resultado
			if (rsAerolinea.next()) {
				aerolinea = new Aerolinea(rsAerolinea.getString("CODIGO"),
											rsAerolinea.getString("NOMBRE")
						);
			}
			
			rsAerolinea.close();
			
		} catch (Exception ex) {
			System.err.format("Error recuperar la aerolinea con codigo %d: %s", codigo, ex.getMessage());						
		}		
		
		return aerolinea;
	}
	
	public Avion getAvionByMatricula(String matricula) {
		Avion avion = null;
		String sql = "SELECT * FROM AVION WHERE MATRICULA = ? LIMIT 1";
		
		try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
		     PreparedStatement pStmt = con.prepareStatement(sql)) {			
			
			pStmt.setString(1, matricula);
			
			ResultSet rsAvion = pStmt.executeQuery();			

			//Se procesa el único resultado
			if (rsAvion.next()) {
				avion = new Avion(rsAvion.getString("MODELO"),
								  rsAvion.getString("MATRICULA"),
								  rsAvion.getInt("CAPACIDAD")
						);
			}
			
			rsAvion.close();
			
		} catch (Exception ex) {
			System.err.format("Error recuperar el avion con matricula %d: %s", matricula, ex.getMessage());						
		}		
		
		return avion;
	}
	
	public PuertaEmbarque getPuertaEmbarqueByCodigo(String codigo) {
		PuertaEmbarque puerta = null;
		String sql = "SELECT * FROM PUERTAEMBARQUE WHERE CODIGO = ? LIMIT 1";
		
		try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
		     PreparedStatement pStmt = con.prepareStatement(sql)) {			
			
			pStmt.setString(1, codigo);
			
			ResultSet rsPuertaEmbarque = pStmt.executeQuery();			

			//Se procesa el único resultado
			if (rsPuertaEmbarque.next()) {
				puerta = new PuertaEmbarque(rsPuertaEmbarque.getString("CODIGO"),
											rsPuertaEmbarque.getInt("NUMERO"),
											rsPuertaEmbarque.getBoolean("OCUPADA")
						);
			}
			
			rsPuertaEmbarque.close();
			
		} catch (Exception ex) {
			System.err.format("Error recuperar la puerta con codigo %d: %s", codigo, ex.getMessage());						
		}		
		
		return puerta;
	}
	
	public Pista getPistaByNumero(String numero) {
		Pista pista = null;
		String sql = "SELECT * FROM PISTA WHERE NUMERO = ? LIMIT 1";
		
		try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
		     PreparedStatement pStmt = con.prepareStatement(sql)) {			
			
			pStmt.setString(1, numero);
			
			ResultSet rsPista = pStmt.executeQuery();			
			
			//Se procesa el único resultado
			if (rsPista.next()) {
				pista = new Pista(rsPista.getString("NUMERO"),
								  rsPista.getBoolean("DISPONIBLE")
						);
			}
			rsPista.close();
			
		} catch (Exception ex) {
			System.err.format("Error recuperar la pista con numero %d: %s", numero, ex.getMessage());						
		}		
		
		return pista;
	}
	
	public ArrayList<Pasajero> getVueloListaPasajeros(Vuelo v) {
		ArrayList<Pasajero> pasajeros = new ArrayList<Pasajero>();
		String sql = "SELECT * FROM VUELO_PASAJERO WHERE CODIGO_VUELO=?";
		
		try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
	             PreparedStatement pst = con.prepareStatement(sql);) {
			
			pst.setString(1, v.getCodigo());
			
	        ResultSet rs = pst.executeQuery();
	            
	        while (rs.next()) {
	            pasajeros.add(new Pasajero(rs.getString("NOMBRE_PASAJERO")));
	            	
	        }
	            
	            rs.close();
	   } catch (Exception e) {
		   System.err.format("\n*Error recuperando pasajeros de vuelo %s: %s.", v.getCodigo(), e.getMessage());
	   }
		
		
		return pasajeros;
	}
	
	public ArrayList<Tripulante> getVueloListaTripulacion(Vuelo v) {
		ArrayList<Tripulante> tripulacion = new ArrayList<Tripulante>();
		String sql = "SELECT * FROM VUELO_TRIPULANTE WHERE CODIGO_VUELO=?";
		
		try (Connection con = DriverManager.getConnection(CONNECTION_STRING);
	             PreparedStatement pst = con.prepareStatement(sql);) {
			
			pst.setString(1, v.getCodigo());
			
	        ResultSet rs = pst.executeQuery();
	            
	        while (rs.next()) {
	        	tripulacion.add(new Tripulante(rs.getString("NOMBRE_TRIPULANTE")));
	            	
	        }
	            
	            rs.close();
	   } catch (Exception e) {
		   System.err.format("\n* Error recuperando tripulantes de vuelo %s: %s.", v.getCodigo(), e.getMessage());
	   }
			
		return tripulacion;
	}
}
