package org.quil.server;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;
import org.quil.repository.CachedFileSystemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class RepositoryAPI {

	final Logger logger = LoggerFactory.getLogger(TaskAPI.class);
 
    @GET
    @Path("content")
    @Produces(MediaType.APPLICATION_JSON)
    public String content() {
    	try
        {
    		// TODO Re-Desgn Repo Class (still ugly)
    		JSONObject repo = new JSONObject();
            CachedFileSystemRepository.instance().genRepositoryObject(null, repo, "");
			return repo.toJSONString();
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
    }
    
    @GET
    @Path("files/{path : .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFile(@PathParam("path") String path) {
    	try
        {
    		JSONObject fileData = new JSONObject();
    		fileData.put("fileData", CachedFileSystemRepository.instance().getFile(path));
			return fileData.toJSONString();
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
    }
    
    @POST
    @Path("files/{path : .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public String putFile(@PathParam("path") String path, String content) {
    	try
        {
            CachedFileSystemRepository.instance().putFile(path, content);
			
			return success();
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
		
    }
    
    @POST
    @Path("files/{path : .+}/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteFile(@PathParam("path") String path) {
    	try
        {
            CachedFileSystemRepository.instance().deleteFile(path);
			
			return success();
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
		
    }
    
    @POST
    @Path("folders/{path : .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public String createFolder(@PathParam("path") String path) {
    	try
        {
            CachedFileSystemRepository.instance().putFolder(path);
			
			return success();
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
		
    }
    
    @POST
    @Path("folders/{path : .+}/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteFolder(@PathParam("path") String path) {
    	try
        {
            CachedFileSystemRepository.instance().deleteFile(path);
			
			return success();
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
    	return "{ \"Status\" : \"ERROR\", \"Msg\" : \""+JSONObject.escape(msg)+"\" }";
    }
    
    private String empty() {
    	return "{  }";
    }
}