package org.quil.server;

import java.util.Arrays;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

 
 public class QuilServer {
 
	static final Logger logger = LoggerFactory.getLogger(QuilServer.class);
 
    public static void main(String[] args) throws Exception {
	
		logger.info("Starting QuilServer...");
	
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
 
        int port = 8080;
        try {
        	port = Integer.parseInt(System.getenv("QUIL_PORT"));
        	logger.info("Using port " + port);
        } catch (Exception e) {
        	logger.info("Defaulting to port 8080");
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
        
        try {
        	
        	// TODO Make configurable
        	TcpDiscoverySpi spi = new TcpDiscoverySpi(); 
        	TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        	ipFinder.setAddresses(Arrays.asList("127.0.0.1", "127.0.0.1:47500..47509"));
        	spi.setIpFinder(ipFinder);    	
        	IgniteConfiguration cfg = new IgniteConfiguration();
        	cfg.setDiscoverySpi(spi);
        	cfg.setClientMode(true);
        	cfg.setPeerClassLoadingEnabled(true);
        	
        	Ignition.start(cfg);
            jettyServer.start();
            
			logger.info("QuilServer running.");
			
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }
}