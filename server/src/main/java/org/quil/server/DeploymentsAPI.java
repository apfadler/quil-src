package org.quil.server;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class DeploymentsAPI {

	final Logger logger = LoggerFactory.getLogger(DeploymentsAPI.class);
 
    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public String deployments() {
    	try
        {
			JSONArray deployed = new JSONArray();
			
			int id = 0;
			for ( Map.Entry<String, String> e : Deployments.All.entrySet() )
			{
				JSONObject obj = new JSONObject();
				
				obj.put("cacheId", e.getKey());
				obj.put("fileId", e.getValue());
				
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
			for ( String e : Deployments.usedCaches)
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
     
    private String success() {
    	return "{ Status : \"SUCCESS\" }";
    }
    
    private String error(String msg) {
    	return "{ Status : \"ERROR\", Msg : \""+msg+"\" }";
    }
    
    private String empty() {
    	return "{  }";
    }
}