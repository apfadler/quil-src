package org.quil.server;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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