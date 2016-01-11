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
 
        Server jettyServer = new Server(8080);
        jettyServer.setHandler(context);
 
        ServletHolder jerseyServlet = context.addServlet(
             org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
 
        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServlet.setInitParameter(
           "jersey.config.server.provider.classnames",
           DocumentCacheAPI.class.getCanonicalName());
 
        try {
        	
        	TcpDiscoverySpi spi = new TcpDiscoverySpi(); 
        	TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        	ipFinder.setAddresses(Arrays.asList("127.0.0.1", "127.0.0.1:47500..47509"));
        	spi.setIpFinder(ipFinder);    	
        	IgniteConfiguration cfg = new IgniteConfiguration();
        	cfg.setDiscoverySpi(spi);
        	cfg.setClientMode(true);
        	
        	Ignition.start(cfg);
            jettyServer.start();
            
			logger.info("QuilServer running.");
			
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }
}