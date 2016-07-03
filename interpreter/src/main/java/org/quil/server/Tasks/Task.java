package org.quil.server.Tasks;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.internal.util.IgniteExceptionRegistry;
import org.apache.ignite.resources.LoggerResource;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.cache.Cache.Entry;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public abstract class Task implements Serializable {
	
	@LoggerResource
    private static IgniteLogger logger;

	@QuerySqlField(index=true)
	protected String _taskName = "";
	
	@QuerySqlField
	protected String _taskDescription = "";
	
	@QuerySqlField
	protected String _taskResult = "";
	
	@QuerySqlField
	protected String _taskTag = "";
	
	@QuerySqlField
	protected int _taskStatus = 0;

	@QuerySqlField
	protected Date _submitTime = new Date(0);

	@QuerySqlField
	protected Date _startTime = new Date(0);

	@QuerySqlField
	protected Date _stopTime = new Date(0);

	static class Status {
		final static int PENDING = 0;
		final static int RUNNING = 1;
		final static int FINISHED = 2;
		final static int ERROR = 3;
	}
	
	
	public static Task fromString(String taskDescription, Boolean managed) {
		String taskName = UUID.randomUUID().toString();
        IgniteCache<String,Task> tasks = cache();
        
        try {
			JSONObject taskObj = (JSONObject) (new JSONParser()).parse(taskDescription);
			
			Constructor c =  Class.forName("org.quil.server.Tasks." +
					(String) taskObj.get("Task")).getConstructor(String.class, String.class);
			Task task = (Task) c.newInstance(taskName, taskDescription);

			if (managed)
				tasks.put(taskName,task);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		return tasks.get(taskName);
	}

	public static Task fromString(String taskDescription) {
		return fromString(taskDescription, true);
	}

	public static Task createUnmanaged(String taskDescription) {
		return fromString(taskDescription, false);
	}
	
	static Task fromJSONObject(JSONObject taskDescription) {
		return fromString(taskDescription.toJSONString());
	}
	
	public static Task get(String taskName) {
        IgniteCache<String,Task> tasks = cache();
        
		return tasks.get(taskName);
	}
	
	public static void updateStatus(String taskName, int status) {
        IgniteCache<String,Task> tasks = cache();

		try {
			Task task = tasks.get(taskName);
			task.setStatus(status);
			tasks.put(taskName, task);
		} catch(Exception e) {
			logger.error("Failed to update status:" + e.toString());
		}
	}
	
	public static void updateResult(String taskName, String result) {
        IgniteCache<String,Task> tasks = cache();

		try {
			Task task = tasks.get(taskName);
			task.setResult(result);
			tasks.put(taskName, task);
		} catch(Exception e) {
			logger.error("Failed to update status:" + e.toString());
		}
	}

	public static IgniteCache<String, Task> cache() {
		Ignite ignite = Ignition.ignite();
		CacheConfiguration<String, Task> cfg = new CacheConfiguration<String, Task>();

		cfg.setCacheMode(CacheMode.REPLICATED);
		cfg.setName("Tasks");
		try {
			cfg.setIndexedTypes(String.class, Task.class,
					String.class, PriceTrade.class,
					String.class, PricePortfolio.class,
					String.class, ScriptedTask.class,
					String.class, Class.forName("org.quil.server.Tasks.RunQLObjectsApplication"));
		}catch (Exception e) {
			logger.info("Failed to set indexed types");
		}
		IgniteCache<String,Task> tasks = ignite.getOrCreateCache(cfg);

		return tasks;
	}

	public static HashMap<String, Task> allTasks() {
        IgniteCache<String,Task> tasks = cache();
        
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
		
		JSONParser parser = new JSONParser();

		try {
			final JSONObject taskDescription = (JSONObject) parser.parse(_taskDescription);
			
			if (taskDescription.containsKey("Tag"))
				_taskTag = (String)taskDescription.get("Tag");
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		_submitTime = new Date();
	}
	
	
	public void setStatus(int status) {
		_taskStatus = status;

		if (status == Status.RUNNING) {
			_startTime = new Date();
		}

		if (status == Status.FINISHED || status == Status.ERROR) {
			_stopTime = new Date();
		}
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

		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

		JSONObject obj = new JSONObject();
		obj.put("name", _taskName);
		obj.put("status", _taskStatus);
		obj.put("result", _taskResult);
		obj.put("tag", _taskTag);

		if (_submitTime.compareTo(new Date(0)) == 0)
			obj.put("submitTime", " ");
		else
			obj.put("submitTime", df.format(_submitTime));

		if (_startTime.compareTo(new Date(0)) == 0)
			obj.put("startTime", " ");
		else
			obj.put("startTime", df.format(_startTime));

		if (_stopTime.compareTo(new Date(0)) == 0)
			obj.put("stopTime", " ");
		else
			obj.put("stopTime", df.format(_stopTime));

		return obj;
	}
	
	abstract public void run() throws Exception;
}
