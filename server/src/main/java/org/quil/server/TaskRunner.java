package org.quil.server;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.events.TaskEvent;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.LoggerResource;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;
import org.quil.interpreter.Interpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.ignite.events.EventType.EVTS_TASK_EXECUTION;

public class TaskRunner {
	
	final static Logger logger = LoggerFactory.getLogger(TaskRunner.class);
	
	private static  boolean initialized = false;
	
	private static void init() {
		
		if (!initialized) {
			
			logger.info("Initializing task runner");
			
			Ignite ignite = Ignition.ignite();
			
	        IgnitePredicate<TaskEvent> locLsnr = new IgnitePredicate<TaskEvent>() {
	            @Override public boolean apply(TaskEvent evt) {
	                // Remote filter only accepts tasks whose name being with "good-task" prefix.
	                logger.info("Received task event [evt=" + evt.name() + ", taskName=" + evt.taskName()+"]");

	                if (evt.name().compareTo("TASK_FINISHED") == 0)
	                {
	                	if (Task.get(evt.taskName()).getStatus() != Task.Status.ERROR  ) {
	                		Task.updateStatus(evt.taskName(),Task.Status.FINISHED);
	                	}
	                }
	                if (evt.name().compareTo("TASK_STARTED") == 0)
	                {
	                	Task.updateStatus(evt.taskName(),Task.Status.RUNNING);
	                }
	                if (evt.name().compareTo("TASK_FAILED") == 0)
	                {
	                	Task.updateStatus(evt.taskName(),Task.Status.ERROR);
	                }
	                
	                return true; // Return true to continue listening.
	            }
	        };
	
	        // Register event listeners on all nodes to listen for task events.
	        ignite.events().localListen(locLsnr, EVTS_TASK_EXECUTION);
		
			initialized = true;
		}
	}

	public static void runTask(final Task task) throws ParseException {
		
		logger.info("Running task " + task.getName());
		
		init();
		
		Ignite ignite = Ignition.ignite();
		
		JSONParser parser = new JSONParser();
		final JSONObject taskDescription = (JSONObject) parser.parse(task.getDescription());

        // Generate task events.
        ignite.compute().withName(task.getName()).withAsync().run(new IgniteRunnable() {
        	
        	@LoggerResource
            private IgniteLogger log;
        	
            @Override public void run() {
                logger.info("Executing task " + task.getName());
                
                try {
					
                	Interpreter interpreter = (Interpreter) Class.forName((String) taskDescription.get("Interpreter")).newInstance();
					interpreter.setData(taskDescription);
					interpreter.interpret();
					Task.updateResult(task.getName(),interpreter.getResult().toJSONString());
					
				} catch (Exception e) {
					
					e.printStackTrace();
					logger.info("Interpretation failed.");
					Task.updateStatus(task.getName(),Task.Status.ERROR);
				}
                           
            }
        });


	}
	
}
