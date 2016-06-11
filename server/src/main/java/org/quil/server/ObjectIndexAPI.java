package org.quil.server;

import java.util.List;
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
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.internal.processors.cache.QueryCursorImpl;
import org.apache.ignite.internal.processors.query.GridQueryFieldMetadata;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.quil.server.Tasks.Task;
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


	@POST
	@Path("query")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public String query(String query) {
		try
		{
			Ignite ignite = Ignition.ignite();
			IgniteCache<String, Task> cache = ignite.cache("Tasks");

			SqlFieldsQuery sql = new SqlFieldsQuery(query);
			sql.setLocal(true);

			JSONArray result = new JSONArray();

			logger.info("Query: " + query);

			try (QueryCursor<List<?>> cursor = cache.query(sql)) {

				JSONArray jsonHeaderRow = new JSONArray();

				logger.info("Getting results.");

				for(int i=0; i < ((QueryCursorImpl) cursor).fieldsMeta().size(); i++) {

					GridQueryFieldMetadata m = (GridQueryFieldMetadata)(((QueryCursorImpl) cursor).fieldsMeta()).get(i);

					jsonHeaderRow.add(m.fieldName());
				}

				result.add(jsonHeaderRow);

				for (List<?> row : cursor) {
					JSONArray jsonRow = new JSONArray();

					for (int i = 0; i < row.size(); i++) {
						GridQueryFieldMetadata m = (GridQueryFieldMetadata)(((QueryCursorImpl) cursor).fieldsMeta()).get(i);
						jsonRow.add(row.get(i).toString());
					}

					result.add(jsonRow);
				}
			}

			logger.info("returning results.");

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