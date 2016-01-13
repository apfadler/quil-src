package org.quil.server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.cache.Cache.Entry;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.resources.LoggerResource;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Task implements Serializable {
	
	@LoggerResource
    private IgniteLogger logger;

	@QuerySqlField(index=true)
	protected String _taskName = "";
	
	@QuerySqlField
	protected String _taskDescription = "";
	
	@QuerySqlField
	protected String _taskResult = "";
	
	@QuerySqlField
	protected int _taskStatus = 0;
	
	static class Status {
		final static int PENDING = 0;
		final static int RUNNING = 1;
		final static int FINISHED = 2;
		final static int ERROR = 3;
	}
	
	
	static Task fromString(String taskDescription) {
		String taskName = UUID.randomUUID().toString();
		
		Ignite ignite = Ignition.ignite();
		CacheConfiguration<String, Task> cfg = new CacheConfiguration<String, Task>();
		
        cfg.setCacheMode(CacheMode.REPLICATED);
        cfg.setName("Tasks");
        cfg.setIndexedTypes(String.class, Task.class);
        
        IgniteCache<String,Task> tasks = ignite.getOrCreateCache(cfg);       
        tasks.put(taskName, new Task(taskName, taskDescription));
		
		return tasks.get(taskName);
	}
	
	static Task fromJSONObject(JSONObject taskDescription) {
		return fromString(taskDescription.toJSONString());
	}
	
	public static Task get(String taskName) {
		
		Ignite ignite = Ignition.ignite();
        IgniteCache<String,Task> tasks = ignite.getOrCreateCache("Tasks");
        
		return tasks.get(taskName);
	}
	
	public static void updateStatus(String taskName, int status) {
		
		Ignite ignite = Ignition.ignite();
        IgniteCache<String,Task> tasks = ignite.getOrCreateCache("Tasks");
        Task task = tasks.get(taskName); 
        task.setStatus(status);
        tasks.put(taskName, task);
	}
	
	public static void updateResult(String taskName, String result) {
		Ignite ignite = Ignition.ignite();
        IgniteCache<String,Task> tasks = ignite.getOrCreateCache("Tasks");
        Task task = tasks.get(taskName); 
        task.setResult(result);
        tasks.put(taskName, task);
	}
	
	public static HashMap<String, Task> allTasks() {
		Ignite ignite = Ignition.ignite();
        IgniteCache<String,Task> tasks = ignite.getOrCreateCache("Tasks");
        
        HashMap<String, Task> all = new HashMap<String, Task>();
        
        for (Entry<String, Task> entry : tasks) {
        	all.put(entry.getKey(),entry.getValue());
        }
        
		return all;
	}
	
	public Task(String taskName, String taskXML) {
		_taskStatus = Status.PENDING;
		_taskDescription = taskXML;
		_taskName = taskName;
	}
	
	
	public void setStatus(int status) {
		_taskStatus = status;
	}
	
	public String getDescription() {
		return _taskDescription;
	}
	
	public void setResult(String result) {
		 _taskResult = result;
	}
	
	public String getResult() {
		return _taskResult;
	}
	
	public String getName() {
		return _taskName;
	}
	
	public int getStatus() {
		return _taskStatus;
	}
	
	public String toJSONString()  {
		return toJSONObj().toJSONString();
	}
	
	public JSONObject toJSONObj()  {
		
		JSONObject obj = new JSONObject();
		obj.put("name", _taskName);
		obj.put("status", _taskStatus);
		obj.put("result", _taskResult);
	
		return obj;
	}
}
