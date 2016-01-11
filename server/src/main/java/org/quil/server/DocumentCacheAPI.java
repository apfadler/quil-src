package org.quil.server;
 
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
@Path("/api/documentcache")
public class DocumentCacheAPI {

	final Logger logger = LoggerFactory.getLogger(DocumentCacheAPI.class);
 
    @POST
    @Path("{cacheid}/create")
    @Produces(MediaType.APPLICATION_JSON)
    public String create(@PathParam("cacheid") String cacheid) {
        try
        {
        	DocumentCache.getOrCreate(cacheid);
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
    public String createFromCSV(@PathParam("cacheid") String cacheid, String data) {
    	DocumentCache cache = DocumentCache.getOrCreate(cacheid);
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
        	DocumentCache cache = DocumentCache.getOrCreate(cacheid);

        	// TODO Use String Reader or something to improve mem consumption
        	String[] lines = data.split(";|,");
        	
        	if (lines.length == 0) {
        		throw new Exception("No data.");
        	}
        	
        	String header = lines[0];
        	String[] columns = header.split(";|,");
        	
        	if (lines.length > 1) {
        		for (int j=1; j < lines.length; j++) {
        			String[] values = lines[j].split(";|,");
        			Document doc = new Document();
					for (int i=0; i < Math.min(values.length,columns.length); i++)
					{
						doc.put(columns[i],values[i]);
					}
					
					cache.put(cacheid+"_" + UUID.randomUUID(), doc);
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
    @Path("{cacheid}/createFromJSONObject")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public String createFromJSONObject(@PathParam("cacheid") String cacheid, String data) {
    	DocumentCache cache = DocumentCache.getOrCreate(cacheid);
    	cache.removeAll();
    	
    	return addJSONObject(cacheid, data);
    }
    
    @POST
    @Path("{cacheid}/addJSONObject")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public String addJSONObject(@PathParam("cacheid") String cacheid, String data) {
    	try
        {
        	DocumentCache cache = DocumentCache.getOrCreate(cacheid);
        	cache.put(cacheid+"_" + UUID.randomUUID(), new Document(data));
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
        	DocumentCache cache = DocumentCache.getOrCreate(cacheid);
        	cache.put(key, new Document(data));
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
        	DocumentCache cache = DocumentCache.getOrCreate(cacheid);
        	
            Document doc = cache.get(key);
            if (doc != null) {
            	return doc.toString();
            } else {
            	return empty();
            }
        	
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
    }
    
    @POST
    @Path("{cacheid}/createFromJSONArray")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public String createFromJSONArray(@PathParam("cacheid") String cacheid, String data) {
    	DocumentCache cache = DocumentCache.getOrCreate(cacheid);
    	cache.removeAll();
    	
    	return addJSONArray(cacheid, data);
    }
    
    @POST
    @Path("{cacheid}/addJSONArray")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public String addJSONArray(@PathParam("cacheid") String cacheid, String data) {
    	try
        {
        	DocumentCache cache = DocumentCache.getOrCreate(cacheid);
        	
        	JSONParser parser = new JSONParser();
        	JSONArray jsonArray = (JSONArray) parser.parse(data);
        	
        	for (Object obj : jsonArray ) {
        		cache.put(cacheid+"_" + UUID.randomUUID(), new Document(((JSONObject)obj).toJSONString()));
        	}
        		
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
        
        return success();
    }
    
    @POST
    @Path("{cacheid}/removeAll")
    @Produces(MediaType.APPLICATION_JSON)
    public String removeAll(@PathParam("cacheid") String cacheid) {
    	
    	try {
    		DocumentCache cache = DocumentCache.getOrCreate(cacheid);
    		cache.removeAll();
    	}
    	catch (Exception e)
    	{
    		return error(e.getMessage());
    	}
    	
    	return success();
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