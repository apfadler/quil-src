package org.quil.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cluster.ClusterMetrics;
import org.apache.ignite.cluster.ClusterNode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class ClusterAPI {

	final Logger logger = LoggerFactory.getLogger(TaskAPI.class);
 
    @GET
    @Path("nodes")
    @Produces(MediaType.APPLICATION_JSON)
    public String status() {
    	try
        {
    		Ignite ignite = Ignition.ignite();
			
			Collection<ClusterNode> nodes = ignite.cluster().nodes();
			
			JSONArray nodeStates = new JSONArray();
			
			int id = 0;
			for ( ClusterNode node : nodes )
			{
				JSONObject nodeStatus = new JSONObject();
				
				ClusterMetrics metrics = node.metrics();
				double cpuLoad = metrics.getCurrentCpuLoad();
				long usedHeap = metrics.getHeapMemoryUsed();
				int numberOfCores = metrics.getTotalCpus();
				int activeJobs = metrics.getCurrentActiveJobs();
				
				nodeStatus.put("cpu", new Double(Math.round(cpuLoad*100)).toString());
				nodeStatus.put("mem", (new Double((usedHeap / 1024 / 1024 ))).toString() + " MB");
				nodeStatus.put("nocores", numberOfCores);
				nodeStatus.put("activejobs", activeJobs);
				nodeStatus.put("id", id++);
				
				nodeStates.add(nodeStatus);
			}
			
			return nodeStates.toJSONString();
        }
        catch (Exception e)
        {
        	return error(e.toString());
        }
    }
    
    @GET
    @Path("caches")
    @Produces(MediaType.APPLICATION_JSON)
    public String caches() {
    	try
        {
    		Ignite ignite = Ignition.ignite();

			JSONArray cacheStates = new JSONArray();

			try
			{
				HashMap<String,Cache> caches = Cache.allCaches();
				for (Map.Entry<String, Cache> c : caches.entrySet()) {
					JSONObject cacheStatus = new JSONObject();

					cacheStatus.put("name", c.getValue().getCacheName());
					cacheStatus.put("type", c.getValue().getClass().toString());
					cacheStatus.put("size", c.getValue().size());

					cacheStates.add(cacheStatus);

				}

			}
			catch(Exception e) {
				logger.error("Failed to get list of active caches ");
				error("Failed to get list of active caches.");
			}
			

			return cacheStates.toJSONString();
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