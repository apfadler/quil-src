package org.quil.server.Tasks;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.events.TaskEvent;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.LoggerResource;
import org.json.simple.parser.ParseException;
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
	            	
	            	if (evt.taskName().compareTo("org.apache.ignite.internal.processors.cache.GridCacheAdapter$SizeTask") ==0 )
	            		return true;
	            	
	                logger.info("Received task event [evt=" + evt.name() + ", taskName=" + evt.taskName()+"]");

	                if (Task.get(evt.taskName()) == null)
	                	return true;
	                
	                if (evt.name().compareTo("TASK_FINISHED") == 0)
	                {
	                	if (Task.get(evt.taskName()).getStatus() != Task.Status.ERROR  ) {
	                		Task.updateStatus(evt.taskName(),Task.Status.FINISHED);
	                	}
	                }
	                if (evt.name().compareTo("TASK_STARTED") == 0)
	                {
	                	Task.updateStatus(evt.taskName(),Task.Status.PENDING);
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
		
        // submit task async
        ignite.compute(ignite.cluster().forAttribute("ROLE", "worker")).withName(task.getName()).withAsync().run(new IgniteRunnable() {
        	
        	@LoggerResource
            private IgniteLogger log;
        	
            @Override public void run() {
                logger.info("Executing task " + task.getName());
                
                try {
					
                	task.run();
					
				} catch (Exception e) {
					
					e.printStackTrace();
					logger.info("Interpretation failed.");
					Task.updateStatus(task.getName(),Task.Status.ERROR);
				}
                           
            }
        });


	}
	
public static void runTaskAndWait(final Task task) throws ParseException {
		
		logger.info("Running task " + task.getName());
		
		init();
		
		Ignite ignite = Ignition.ignite();
		
        // submit task sync
        ignite.compute(ignite.cluster().forAttribute("ROLE", "worker")).withName(task.getName()).run(new IgniteRunnable() {
        	
        	@LoggerResource
            private IgniteLogger log;
        	
            @Override public void run() {
                logger.info("Executing task " + task.getName());
                
                try {
					
                	task.run();
					
				} catch (Exception e) {
					
					e.printStackTrace();
					logger.info("Interpretation failed.");
					Task.updateStatus(task.getName(),Task.Status.ERROR);
				}
                           
            }
        });


	}
	
}
