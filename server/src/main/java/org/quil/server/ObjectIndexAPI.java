package org.quil.server;

import java.util.Map;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.SqlQuery;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.SQLException;

@Path("/")
public class ObjectIndexAPI {

	final Logger logger = LoggerFactory.getLogger(ObjectIndexAPI.class);
 
    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public String objects() {
    	try
        {
			JSONArray deployed = new JSONArray();
			
			int id = 0;
			for ( Map.Entry<String, String> e : ObjectIndex.all().entrySet() )
			{
				JSONObject obj = new JSONObject();
				
				obj.put("cacheId", e.getValue());
				obj.put("fileId", e.getKey());
				
				deployed.add(obj);
			}
			
			return deployed.toJSONString();
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
    }
    
    @GET
    @Path("usedCaches")
    @Produces(MediaType.APPLICATION_JSON)
    public String usedCaches() {
    	try
        {
			JSONArray used = new JSONArray();
			
			int id = 0;
			for ( String e : ObjectIndex.usedCaches)
			{
				JSONObject obj = new JSONObject();
				
				obj.put("cacheId", e);

				used.add(obj);
			}
			
			return used.toJSONString();
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
    }
    
    //TODO remove
    @POST
    @Path("{cacheid}/put/{key}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public String put(@PathParam("cacheid") String cacheid, @PathParam("key") String key, String data) {
    	try
        {
        	ObjectIndex.addToIndex(key, cacheid);
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
        
        return success();
    }
    
    
    private static final String IGNITE_JDBC_DRIVER_NAME = "org.apache.ignite.IgniteJdbcDriver";
    
    @POST
    @Path("query")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public String query(String query) {
    	try
        {
    		String IGNITE_JDBC_URL = "jdbc:ignite://localhost:11211/Tasks";
    		Connection conn = null;

    		Statement curStmt;

    		try {
    			Class.forName(IGNITE_JDBC_DRIVER_NAME);
    		} catch (ClassNotFoundException e) {
    			return error("Can't open connection");
    		}

    		try {
    			logger.info("connect to " + IGNITE_JDBC_URL);

    			conn = DriverManager.getConnection(IGNITE_JDBC_URL);

    			logger.info("Successfully created JDBC connection");
    		} catch (SQLException e) {
    			return error("Can't open connection: ");

    		}

    		JSONArray result = new JSONArray();
    		try (Statement stmt = conn.createStatement()) {

    			curStmt = stmt;

    			try (ResultSet res = stmt.executeQuery(query)) {
    				ResultSetMetaData md = res.getMetaData();

    				JSONArray columns = new JSONArray();
    				for (int i = 1; i <= md.getColumnCount(); i++) {
    					columns.add(md.getColumnName(i));
					}
    				result.add(columns);
    				
    				while (res.next()) {
    					
    					JSONArray row = new JSONArray();
    					
    					for (int i = 1; i <= md.getColumnCount(); i++) {
    						row.add(res.getString(i));
    					}
    					
    					result.add(row);
    				}
    			} catch (Exception e) {
					return error(e.toString());

				}
    		} catch (Exception e) {
    			return error(e.toString());

    		} finally {
    			curStmt = null;
    		}

    		return result.toJSONString();
        }
    	catch (Exception e)
    	{
    		return error(e.toString());
    	}
    }
     
    private String success() {
    	return "{ \"Status\" : \"SUCCESS\" }";
    }
    
    private String error(String msg) {
    	return "{ \"Status\" : \"ERROR\", \"Msg\" : \""+msg+"\" }";
    }
    
    private String empty() {
    	return "{  }";
    }
}