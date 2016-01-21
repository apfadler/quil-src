package org.quil.server;


import java.util.Arrays;



import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.collision.fifoqueue.FifoQueueCollisionSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import static org.apache.ignite.events.EventType.EVTS_TASK_EXECUTION;
 
public class QuilServer {
 
	static final Logger logger = LoggerFactory.getLogger(QuilServer.class);
	
	
    public static void main(String[] args) throws Exception {
    	
    	
    	boolean workerNode = false;
        try {
        	String env = System.getenv("QUIL_WORKER");
        	if (env.compareToIgnoreCase("yes") == 0 || env.compareToIgnoreCase("true") == 0 )
        	{
        		workerNode = true;
        	}
        	
        } catch (Exception e) {
        }


        logger.info("Starting QuilServer...");

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        int port = 8081;
        try {
        	port = Integer.parseInt(System.getenv("QUIL_PORT"));
        	logger.info("Using port " + port);
        } catch (Exception e) {
        	logger.info("Defaulting to port 8081");
        }

        Server jettyServer = new Server(port);
        jettyServer.setHandler(context);

        ServletHolder jerseyServletDocumentCacheAPI = context.addServlet(
        		org.glassfish.jersey.servlet.ServletContainer.class, "/api/documentcache/*");

        jerseyServletDocumentCacheAPI.setInitOrder(0);

        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServletDocumentCacheAPI.setInitParameter(
        		"jersey.config.server.provider.classnames",
        		DocumentCacheAPI.class.getCanonicalName() );

        ServletHolder jerseyServletSimpleCacheAPI = context.addServlet(
        		org.glassfish.jersey.servlet.ServletContainer.class, "/api/simplecache/*");

        jerseyServletSimpleCacheAPI.setInitOrder(0);

        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServletSimpleCacheAPI.setInitParameter(
        		"jersey.config.server.provider.classnames",
        		SimpleCacheAPI.class.getCanonicalName() );

        ServletHolder jerseyServletTaskAPI = context.addServlet(
        		org.glassfish.jersey.servlet.ServletContainer.class, "/api/compute/*");

        jerseyServletTaskAPI.setInitOrder(0);

        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServletTaskAPI.setInitParameter(
        		"jersey.config.server.provider.classnames",
        		TaskAPI.class.getCanonicalName() );

        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServletTaskAPI.setInitParameter(
        		"jersey.config.server.provider.classnames",
        		TaskAPI.class.getCanonicalName() );

        ServletHolder jerseyServletSSE = context.addServlet(
        		org.glassfish.jersey.servlet.ServletContainer.class, "/log/*");

        jerseyServletSSE.setInitOrder(0);
        jerseyServletSSE.setAsyncSupported(true);

        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServletSSE.setInitParameter(
        		"jersey.config.server.provider.classnames",
        		LogBroadcaster.class.getCanonicalName() );

        
        try {
        	
        	// TODO Make configurable
        	TcpDiscoverySpi spi = new TcpDiscoverySpi(); 
        	TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        	ipFinder.setAddresses(Arrays.asList("127.0.0.1", "127.0.0.1:47500..47509"));
        	spi.setIpFinder(ipFinder);    	
        	IgniteConfiguration cfg = new IgniteConfiguration();
        	cfg.setDiscoverySpi(spi);

        	FifoQueueCollisionSpi spiCollision = new FifoQueueCollisionSpi();	
        	spiCollision.setParallelJobsNumber(4);
        	cfg.setCollisionSpi(spiCollision);
        	
        	if (!workerNode) {
        		boolean clientmode = true;
        		try {
        			String env = System.getenv("QUIL_SERVER_STANDALONE");
        			if (env.compareToIgnoreCase("yes") == 0 || env.compareToIgnoreCase("true") == 0 )
        			{
        				clientmode = false;
        			}

        		} catch (Exception e) {
        		}

        		logger.info("Client mode = " + clientmode);

        		cfg.setClientMode(clientmode);
        	}
        	
        	cfg.setPeerClassLoadingEnabled(true);
        	cfg.setIncludeEventTypes(EVTS_TASK_EXECUTION);
        	
        	Ignition.start(cfg);
        	
        	if (!workerNode)  {
        		jettyServer.start();
            
        		logger.info("QuilServer running.");
			
        		jettyServer.join();
        	}
        } finally {
            jettyServer.destroy();
        }
    }
}