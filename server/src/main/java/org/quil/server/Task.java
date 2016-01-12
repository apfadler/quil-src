package org.quil.server;

import java.util.HashMap;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Task {
	
	final Logger logger = LoggerFactory.getLogger(Task.class);

	protected String _taskName = "";
	protected String _taskXML = "";
	protected String _taskResult = "";
	
	protected int _taskStatus = 0;
	
	static class Status {
		final static int PENDING = 0;
		final static int RUNNING = 1;
		final static int FINISHED = 2;
		final static int ERROR = 3;
	}
	
	static HashMap<String, Task> tasks = new HashMap<String, Task>();
	
	static Task fromXML(String taskXML) {
		String taskName = UUID.randomUUID().toString();
		tasks.put(taskName, new Task(taskName, taskXML));
		
		return tasks.get(taskName);
	}
	
	public static Task get(String taskName) {
		return tasks.get(taskName);
	}
	
	public static HashMap<String, Task> allTasks() {
		return tasks;
	}
	
	public Task(String taskName, String taskXML) {
		_taskStatus = Status.PENDING;
		_taskXML = taskXML;
		_taskName = taskName;
	}
	
	
	public void setStatus(int status) {
		_taskStatus = status;
	}
	
	public String getXML() {
		return _taskXML;
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
	
		JSONObject obj = new JSONObject();
		obj.put("name", _taskName);
		obj.put("status", _taskStatus);
		obj.put("result", _taskResult);
	
		return obj.toJSONString();
	}
	
	public JSONObject toJSONObj()  {
		
		JSONObject obj = new JSONObject();
		obj.put("name", _taskName);
		obj.put("status", _taskStatus);
		obj.put("result", _taskResult);
	
		return obj;
	}
}
