package org.quil.server;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class TaskAPI {

	final Logger logger = LoggerFactory.getLogger(TaskAPI.class);
 
    @POST
    @Path("task/submit")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public String submit(String taskDescriptorXML) {
        try
        {
        	Task task = Task.fromXML(taskDescriptorXML);
        	TaskRunner.runTask(task);
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
        
        return success();
    }
    
    @GET
    @Path("tasks")
    @Produces(MediaType.APPLICATION_JSON)
    public String tasks(@PathParam("cacheid") String cacheid) {
        
    	HashMap<String, Task> tasks = Task.allTasks();
    	JSONArray jsonTasks = new JSONArray();
    	for (Map.Entry<String, Task> entry : tasks.entrySet() ) {
    		jsonTasks.add(entry.getValue());
    	}
    	
        return jsonTasks.toJSONString();
    }
    
    @GET
    @Path("tasks/{taskid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String task(@PathParam("taskid") String taskid) {
        
        try {
        	return Task.get(taskid).toJSONString();
        } catch (Exception e) {
        	return empty();
        }
    }
    
    @GET
    @Path("tasks/{taskid}/result")
    @Produces(MediaType.TEXT_PLAIN)
    public String result(@PathParam("taskid") String taskid) {
    	try {
        	return Task.get(taskid).getResult();
        } catch (Exception e) {
        	return empty();
        }
    }
    
    @GET
    @Path("tasks/{taskid}/status")
    @Produces(MediaType.TEXT_PLAIN)
    public int status(@PathParam("taskid") String taskid) {
    	try {
        	return Task.get(taskid).getStatus();
        } catch (Exception e) {
        	return -1;
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