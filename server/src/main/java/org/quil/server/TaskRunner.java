package org.quil.server;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.events.TaskEvent;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.lang.IgniteRunnable;
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
			
			// This optional local callback is called for each event notification
	        // that passed remote predicate listener.
	        IgniteBiPredicate<UUID, TaskEvent> locLsnr = new IgniteBiPredicate<UUID, TaskEvent>() {
	            @Override public boolean apply(UUID nodeId, TaskEvent evt) {
	                // Remote filter only accepts tasks whose name being with "good-task" prefix.
	                logger.info("Received task event [evt=" + evt.name() + ", taskName=" + evt.taskName()+"]");
	
	                if (evt.name() == "TASK_FINISHED")
	                {
	                	Task.get(evt.taskName()).setStatus(Task.Status.FINISHED);
	                }
	                
	                return true; // Return true to continue listening.
	            }
	        };
	
	        // Remote filter which only accepts tasks whose name begins with "good-task" prefix.
	        IgnitePredicate<TaskEvent> rmtLsnr = new IgnitePredicate<TaskEvent>() {
	            @Override public boolean apply(TaskEvent evt) {
	                return true;
	            }
	        };
	
	        // Register event listeners on all nodes to listen for task events.
	        ignite.events().remoteListen(locLsnr, rmtLsnr, EVTS_TASK_EXECUTION);
		
			initialized = true;
		}
	}

	public static void runTask(Task task){
		
		logger.info("Running task " + task.getName());
		
		init();
		
		Ignite ignite = Ignition.ignite();
		
        // Generate task events.
        ignite.compute().withName(task.getName()).run(new IgniteRunnable() {
            @Override public void run() {
                System.out.println("Executing sample job.");
            }
        });


	}
	
}
