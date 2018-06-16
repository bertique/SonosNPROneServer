package me.michaeldick.sonosnpr;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.sonos.services._1.AbstractMedia;

import me.michaeldick.npr.model.Media;
import me.michaeldick.npr.model.Rating;

public class SonosServiceCache {

    private static Logger logger = Logger.getLogger(SonosService.class.getSimpleName());
	
	private static String JDBC_DATABASE_URL = "";
	
	public static void setDatabaseUrl(String dbUrl) {
		JDBC_DATABASE_URL = dbUrl;
	}	
	
    private static Connection getConnection() throws URISyntaxException, SQLException {
        String dbUrl = JDBC_DATABASE_URL;
        return DriverManager.getConnection(dbUrl);
    }
	
	public static void initializeDb(String dbUrl) {
		JDBC_DATABASE_URL = dbUrl;
		
		Connection c;
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
		}    
	}
	
	public static void resetDb() {
		Connection c;
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
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
	}
		
	public static Media getListeningResponseIfPresent(String id) {		
		Connection c;
		try {
			c = getConnection();
			
			PreparedStatement listeningResponseCachePs = c.prepareStatement("SELECT jsonBlob FROM ListeningResponseCache WHERE userid = ?");
			listeningResponseCachePs.setString(1, id);
			ResultSet rs = listeningResponseCachePs.executeQuery();
			if(rs.next()) {
				Gson gson = new Gson();  
				Media m = gson.fromJson(rs.getString(1), Media.class);  
		        listeningResponseCachePs.close();
				return(m);
			} else {
				listeningResponseCachePs.close();
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
		} 		
	}
	
	public static void putListeningResponse(String id, Media m) {
		Connection c;
		try {
			c = getConnection();
			
			PreparedStatement listeningResponseCachePs = c.prepareStatement("INSERT INTO ListeningResponseCache("
					+ "userid, "
					+ "lastUpdated, "
					+ "jsonBlob) "
					+ "VALUES (?,DEFAULT,?::JSON)"
					+ "ON CONFLICT (userid) DO UPDATE "
					+ "SET lastUpdated = EXCLUDED.lastUpdated, "
					+ "jsonBlob = EXCLUDED.jsonBlob");
			
			listeningResponseCachePs.setString(1, id);	
			Gson gson = new Gson();
			listeningResponseCachePs.setObject(2, gson.toJson(m));
			
			listeningResponseCachePs.executeUpdate();
			listeningResponseCachePs.close();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} 		
	}
	
	public static void invalidateListeningResponse(String id) {
		Connection c;
		try {
			c = getConnection();
			
			PreparedStatement listeningResponseCachePs = c.prepareStatement("DELETE FROM ListeningResponseCache WHERE userid = ?");
			
			listeningResponseCachePs.setString(1, id);						
			
			listeningResponseCachePs.executeUpdate();
			listeningResponseCachePs.close();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} 		
	}
	
	public static List<Rating> getRatingIfPresent(String id) {
		Connection c;
		try {
			c = getConnection();
			logger.error("We hit it before"+id);
			PreparedStatement listeningResponseCachePs = c.prepareStatement("SELECT jsonBlob FROM RatingCache WHERE userid = ?");
			listeningResponseCachePs.setString(1, id);
			ResultSet rs = listeningResponseCachePs.executeQuery();			
			if(rs.next()) {
				Gson gson = new Gson();				
				List<Rating> lr = gson.fromJson(rs.getString(1), new TypeToken<ArrayList<Rating>>(){}.getType());				
		        listeningResponseCachePs.close();       		        
				return(lr);
			} else {
				listeningResponseCachePs.close();
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
		} 	
	}
	
	public static void putRating(String id, List<Rating> r) {
		Connection c;
		try {
			c = getConnection();
			logger.error("Setting rating"+id);
			
			PreparedStatement listeningResponseCachePs = c.prepareStatement("INSERT INTO RatingCache("
					+ "userid, "
					+ "lastUpdated, "
					+ "jsonBlob) "
					+ "VALUES (?,DEFAULT,?::JSON) "
					+ "ON CONFLICT (userid) DO UPDATE "
					+ "SET lastUpdated = EXCLUDED.lastUpdated, "
					+ "jsonBlob = EXCLUDED.jsonBlob");
			
			listeningResponseCachePs.setString(1, id);			
			Gson gson = new Gson();				
			listeningResponseCachePs.setObject(2, gson.toJson(r));			
			listeningResponseCachePs.executeUpdate();
			listeningResponseCachePs.close();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} 
	}
	
	public static void invalidateRatings(String id) {
		Connection c;
		try {
			c = getConnection();
			
			PreparedStatement listeningResponseCachePs = c.prepareStatement("DELETE FROM RatingCache WHERE userid = ?");
			
			listeningResponseCachePs.setString(1, id);						
			
			listeningResponseCachePs.executeUpdate();
			listeningResponseCachePs.close();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} 		
	}

	public static List<String> getLastPlayerResponse(String id) {
		Connection c;
		try {
			c = getConnection();
			logger.error("We hit it before"+id);
			PreparedStatement listeningResponseCachePs = c.prepareStatement("SELECT jsonBlob FROM ResponseToPlayer WHERE userid = ?");
			listeningResponseCachePs.setString(1, id);
			ResultSet rs = listeningResponseCachePs.executeQuery();			
			if(rs.next()) {
				Gson gson = new Gson();				
				List<String> m = gson.fromJson(rs.getString(1), new TypeToken<ArrayList<String>>(){}.getType());							
		        listeningResponseCachePs.close();		      
				return(m);
			} else {
				listeningResponseCachePs.close();
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
		} 				
	}
	
	public static void putLastPlayerResponse(String id, List<String> m) {
		Connection c;
		try {
			c = getConnection();
			
			PreparedStatement listeningResponseCachePs = c.prepareStatement("INSERT INTO ResponseToPlayer("
					+ "userid, "
					+ "lastUpdated, "
					+ "jsonBlob) "
					+ "VALUES (?,DEFAULT,?::JSON) "
					+ "ON CONFLICT (userid) DO UPDATE "
					+ "SET lastUpdated = EXCLUDED.lastUpdated, "
					+ "jsonBlob = EXCLUDED.jsonBlob");
			listeningResponseCachePs.setString(1, id);
			Gson gson = new Gson();				
			listeningResponseCachePs.setObject(2, gson.toJson(m));
			
			listeningResponseCachePs.executeUpdate();
			listeningResponseCachePs.close();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} 
			}
}
