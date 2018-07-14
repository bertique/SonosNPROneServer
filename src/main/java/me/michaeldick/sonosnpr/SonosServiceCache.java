package me.michaeldick.sonosnpr;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import me.michaeldick.npr.model.Media;
import me.michaeldick.npr.model.Rating;

public class SonosServiceCache {

    private static Logger logger = Logger.getLogger(SonosService.class.getSimpleName());
	
    private BasicDataSource connectionPool;
	
	public SonosServiceCache (String DATABASE_URL) {		
		String dbUrl = "";
		try {
			URI dbUri = new URI(DATABASE_URL);
		
			dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":"+ dbUri.getPort() + dbUri.getPath();
			connectionPool = new BasicDataSource();
		
			if (dbUri.getUserInfo() != null) {
			  connectionPool.setUsername(dbUri.getUserInfo().split(":")[0]);
			  connectionPool.setPassword(dbUri.getUserInfo().split(":")[1]);
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		connectionPool.setDriverClassName("org.postgresql.Driver");
		connectionPool.setUrl(dbUrl);
		connectionPool.setInitialSize(1);
	}
	
    private Connection getConnection() throws URISyntaxException, SQLException {
        return connectionPool.getConnection();
    }
	
	public void initializeDb() {
		
		Connection c = null;
		try {
			c = getConnection();
			PreparedStatement listeningResponseCachePs = c.prepareStatement("CREATE TABLE IF NOT EXISTS ListeningResponseCache("
					+ "userid VARCHAR PRIMARY KEY, "
					+ "lastUpdated TIMESTAMP DEFAULT NOW(), "
					+ "jsonBlob JSON)");
			listeningResponseCachePs.executeUpdate();
			listeningResponseCachePs.close();
	    				
	    	PreparedStatement ratingCachePs = c.prepareStatement("CREATE TABLE IF NOT EXISTS RatingCache("
	    			+ "userid VARCHAR PRIMARY KEY, "
					+ "lastUpdated TIMESTAMP DEFAULT NOW(), "
	    			+ "jsonBlob JSON)");
	    	ratingCachePs.executeUpdate();
	    	ratingCachePs.close();
	    	
			PreparedStatement lastResponseToPlayerPs = c.prepareStatement("CREATE TABLE IF NOT EXISTS ResponseToPlayer("
					+ "userid VARCHAR PRIMARY KEY, "
					+ "lastUpdated TIMESTAMP DEFAULT NOW(), "
					+ "jsonBlob JSON)");	    				
			lastResponseToPlayerPs.executeUpdate();
			lastResponseToPlayerPs.close();

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				c.close();
			} catch (SQLException e) {}
		}
	}
	
	public void resetDb() {
		Connection c = null;
		try {
			c = getConnection();
			
			PreparedStatement listeningResponseCachePs = c.prepareStatement("DROP TABLE ListeningResponseCache");
			listeningResponseCachePs.executeUpdate();
			listeningResponseCachePs.close();
	    				
	    	PreparedStatement ratingCachePs = c.prepareStatement("DROP TABLE RatingCache");
	    	ratingCachePs.executeUpdate();
	    	ratingCachePs.close();
	    	
			PreparedStatement lastResponseToPlayerPs = c.prepareStatement("DROP TABLE ResponseToPlayer");			
			lastResponseToPlayerPs.executeUpdate();
			lastResponseToPlayerPs.close();
			c.close();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				c.close();
			} catch (SQLException e) {}
		}
	}
		
	public Media getListeningResponseIfPresent(String id) {		
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			c = getConnection();
			
			ps = c.prepareStatement("SELECT jsonBlob FROM ListeningResponseCache WHERE userid = ?");
			ps.setString(1, id);
			rs = ps.executeQuery();
			if(rs.next()) {
				Gson gson = new Gson();  
				Media m = gson.fromJson(rs.getString(1), Media.class);  
		        ps.close();		        
				return(m);
			} else {
				ps.close();				
				return null;
			}
						
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
			return null;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {}
			try {
				rs.close();
			} catch (SQLException e) {}
			try {
				c.close();
			} catch (SQLException e) {}
		}
	}
	
	public void putListeningResponse(String id, Media m) {
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = getConnection();
			
			ps = c.prepareStatement("INSERT INTO ListeningResponseCache("
					+ "userid, "
					+ "lastUpdated, "
					+ "jsonBlob) "
					+ "VALUES (?,DEFAULT,?::JSON)"
					+ "ON CONFLICT (userid) DO UPDATE "
					+ "SET lastUpdated = EXCLUDED.lastUpdated, "
					+ "jsonBlob = EXCLUDED.jsonBlob");
			
			ps.setString(1, id);	
			Gson gson = new Gson();
			ps.setObject(2, gson.toJson(m));
			
			ps.executeUpdate();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {}
			try {
				c.close();
			} catch (SQLException e) {}
		}	
	}
	
	public void invalidateListeningResponse(String id) {
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = getConnection();
			
			ps = c.prepareStatement("DELETE FROM ListeningResponseCache WHERE userid = ?");			
			ps.setString(1, id);									
			ps.executeUpdate();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {}
			try {
				c.close();
			} catch (SQLException e) {}
		} 		
	}
	
	public List<Rating> getRatingIfPresent(String id) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			c = getConnection();
			ps = c.prepareStatement("SELECT jsonBlob FROM RatingCache WHERE userid = ?");
			ps.setString(1, id);
			rs = ps.executeQuery();			
			if(rs.next()) {
				Gson gson = new Gson();				
				List<Rating> lr = gson.fromJson(rs.getString(1), new TypeToken<ArrayList<Rating>>(){}.getType());				
				return(lr);
			} else {
				return null;
			}
						
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}  finally {
			try {
				ps.close();
			} catch (SQLException e) {}
			try {
				rs.close();
			} catch (SQLException e) {}
			try {
				c.close();
			} catch (SQLException e) {}
		}	
	}
	
	public void putRating(String id, List<Rating> r) {
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = getConnection();
			logger.error("Setting rating"+id);
			
			ps = c.prepareStatement("INSERT INTO RatingCache("
					+ "userid, "
					+ "lastUpdated, "
					+ "jsonBlob) "
					+ "VALUES (?,DEFAULT,?::JSON) "
					+ "ON CONFLICT (userid) DO UPDATE "
					+ "SET lastUpdated = EXCLUDED.lastUpdated, "
					+ "jsonBlob = EXCLUDED.jsonBlob");
			
			ps.setString(1, id);			
			Gson gson = new Gson();				
			ps.setObject(2, gson.toJson(r));			
			ps.executeUpdate();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {}
			try {
				c.close();
			} catch (SQLException e) {}
		} 
	}
	
	public void invalidateRatings(String id) {
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = getConnection();
			
			ps = c.prepareStatement("DELETE FROM RatingCache WHERE userid = ?");
			
			ps.setString(1, id);						
			
			ps.executeUpdate();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {}
			try {
				c.close();
			} catch (SQLException e) {}
		} 		
	}

	public List<String> getLastPlayerResponse(String id) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			c = getConnection();
			ps = c.prepareStatement("SELECT jsonBlob FROM ResponseToPlayer WHERE userid = ?");
			ps.setString(1, id);
			rs = ps.executeQuery();			
			if(rs.next()) {
				Gson gson = new Gson();				
				List<String> m = gson.fromJson(rs.getString(1), new TypeToken<ArrayList<String>>(){}.getType());							
				return(m);
			} else {
				return null;
			}
						
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {}
			try {
				rs.close();
			} catch (SQLException e) {}
			try {
				c.close();
			} catch (SQLException e) {}
		} 				
	}
	
	public void putLastPlayerResponse(String id, List<String> m) {
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = getConnection();
			
			ps = c.prepareStatement("INSERT INTO ResponseToPlayer("
					+ "userid, "
					+ "lastUpdated, "
					+ "jsonBlob) "
					+ "VALUES (?,DEFAULT,?::JSON) "
					+ "ON CONFLICT (userid) DO UPDATE "
					+ "SET lastUpdated = EXCLUDED.lastUpdated, "
					+ "jsonBlob = EXCLUDED.jsonBlob");
			ps.setString(1, id);
			Gson gson = new Gson();				
			ps.setObject(2, gson.toJson(m));
			
			ps.executeUpdate();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}  finally {
			try {
				ps.close();
			} catch (SQLException e) {}
			try {
				c.close();
			} catch (SQLException e) {}
		}
			}
}
