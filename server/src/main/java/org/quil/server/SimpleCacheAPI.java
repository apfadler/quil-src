package org.quil.server;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class SimpleCacheAPI {

	final Logger logger = LoggerFactory.getLogger(SimpleCacheAPI.class);
 
    @POST
    @Path("{cacheid}/create")
    @Produces(MediaType.APPLICATION_JSON)
    public String create(@PathParam("cacheid") String cacheid) {
        try
        {
        	SimpleCache.getOrCreate(cacheid);
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
        
        return success();
    }
    
    @POST
    @Path("{cacheid}/createFromCSV")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public String createFromCSV(@PathParam("cacheid") String cacheid, String data) throws Exception {
    	SimpleCache cache = SimpleCache.getOrCreate(cacheid);
    	cache.removeAll();
    	
    	return addFromCSV(cacheid, data);
    }
    
    @POST
    @Path("{cacheid}/addFromCSV")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public String addFromCSV(@PathParam("cacheid") String cacheid, String data) {
    	try
        {
        	SimpleCache cache = SimpleCache.getOrCreate(cacheid);

        	// TODO Use String Reader or something to improve mem consumption
        	String[] lines = data.split("\\r?\\n");
        	
        	if (lines.length == 0) {
        		throw new Exception("No data.");
        	}
        	
        	if (lines.length > 0) {
        		for (int j=0; j < lines.length; j++) {
        			
        			String[] values = lines[j].split(";|,");
        			if (values.length > 1)
        				cache.put(values[0], values[1]);
        		}
        	}
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
        
        return success();
    }
    
  
    
    @POST
    @Path("{cacheid}/put/{key}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public String put(@PathParam("cacheid") String cacheid, @PathParam("key") String key, String data) {
    	try
        {
        	SimpleCache cache = SimpleCache.getOrCreate(cacheid);
        	cache.put(key, data);
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
        
        return success();
    }
    
    @GET
    @Path("{cacheid}/get/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public String get(@PathParam("cacheid") String cacheid, @PathParam("key") String key) {
    	try
        {
        	SimpleCache cache = SimpleCache.getOrCreate(cacheid);
            String value = cache.get(key);
            
            if (value != null) {
            	return value;
            }
            else {
            	return "";
            }
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
    }
    
    @POST
    @Path("{cacheid}/removeAll")
    @Produces(MediaType.APPLICATION_JSON)
    public String removeAll(@PathParam("cacheid") String cacheid) {
    	
    	try {
    		SimpleCache cache = SimpleCache.getOrCreate(cacheid);
    		cache.removeAll();
    	}
    	catch (Exception e)
    	{
    		return error(e.getMessage());
    	}
    	
    	return success();
    }
    
    private String success() {
    	return "{ \"status\" : \"SUCCESS\" }";
    }
    
    private String error(String msg) {
    	return "{ \"status\" : \"ERROR\", \"msg\" : \""+msg+"\" }";
    }
    
}
