package org.quil.server;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.NetworkTrafficSelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.annotation.Name;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.quil.repository.CachedFileSystemRepository;
import org.quil.server.Tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.collision.fifoqueue.FifoQueueCollisionSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import scala.tools.nsc.interpreter.IMain;
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

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[]{ setupApiContext(), setupWebAppContext()});
        
        int port = 8081;
        try {
        	port = Integer.parseInt(System.getenv("QUIL_PORT"));
        	logger.info("Using port " + port);
        } catch (Exception e) {
        	logger.info("Defaulting to port 8081");
        }


		QueuedThreadPool tp = new QueuedThreadPool();
		tp.setMaxThreads(1000);tp.setMinThreads(10);
		Server jettyServer = new Server(tp);
		org.eclipse.jetty.server.nio.NetworkTrafficSelectChannelConnector c =
				new org.eclipse.jetty.server.nio.NetworkTrafficSelectChannelConnector(jettyServer);
		c.setPort(port);
		jettyServer.addConnector(c);
        jettyServer.setHandler(contexts);
 
        try {
        	if (!workerNode) {
        		boolean clientmode = true;
				boolean standalone = false;
        		try {
        			String env = System.getenv("QUIL_SERVER_STANDALONE");
        			if (env.compareToIgnoreCase("yes") == 0 || env.compareToIgnoreCase("true") == 0 )
        			{
        				clientmode = false;
        				standalone = true;
					}

        		} catch (Exception e) {
        		}

        		logger.info("Client mode = " + clientmode);

				if (clientmode) {
					Ignition.start("config/quil-client.xml");
				}
				else
				{
					if (standalone) {
						Ignition.start("config/quil-worker.xml");
					}
					else {
						Ignition.start("config/quil-server.xml");
					}
				}
        		//cfg.setClientMode(clientmode);
        	}
			else {
				Ignition.start("config/quil-worker.xml");


			}

        	if (!workerNode)  {

				Task.cache();

				runQuilStartupScript();

				CachedFileSystemRepository.instance();

				jettyServer.start();
            
        		logger.info("QuilServer running.");
			
        		jettyServer.join();
        	}

        } finally {
            jettyServer.destroy();
        }
    }
    
    private static WebAppContext setupWebAppContext() {

    	String warPathEnv = "";
    	warPathEnv = System.getenv("QUIL_WARPATH");
    	if (warPathEnv == null) {
    		System.out.println("QUIL_WARPATH is not set.");
    		System.exit(8);
    	}
    	WebAppContext webApp = new WebAppContext();
    	webApp.setContextPath("/frontend");
    	File warPath = new File(warPathEnv);
    	
    	if (warPath.isDirectory()) {
    		webApp.setResourceBase(warPath.getPath());
    		webApp.setParentLoaderPriority(true);
    	} else {
    		webApp.setWar(warPath.getAbsolutePath());
    	}
    	
    	webApp.addServlet(new ServletHolder(new DefaultServlet()), "/*");

    	return webApp;
    }
    
    private static ServletContextHandler setupApiContext() {

    	ServletContextHandler apiContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
    	apiContext.setContextPath("/api");
    	ServletHolder jerseyServletDocumentCacheAPI = apiContext.addServlet(
    			org.glassfish.jersey.servlet.ServletContainer.class, "/documentcache/*");
    	jerseyServletDocumentCacheAPI.setInitOrder(0);


    	jerseyServletDocumentCacheAPI.setInitParameter(
    			"jersey.config.server.provider.classnames",
    			DocumentCacheAPI.class.getCanonicalName() );
    	ServletHolder jerseyServletSimpleCacheAPI = apiContext.addServlet(
    			org.glassfish.jersey.servlet.ServletContainer.class, "/simplecache/*");
    	jerseyServletSimpleCacheAPI.setInitOrder(0);
    	jerseyServletSimpleCacheAPI.setInitParameter(
    			"jersey.config.server.provider.classnames",
    			SimpleCacheAPI.class.getCanonicalName() );

    	ServletHolder jerseyServletTaskAPI = apiContext.addServlet(
    			org.glassfish.jersey.servlet.ServletContainer.class, "/compute/*");
    	jerseyServletTaskAPI.setInitOrder(0);
    	jerseyServletTaskAPI.setInitParameter(
    			"jersey.config.server.provider.classnames",
    			TaskAPI.class.getCanonicalName() );
    	jerseyServletTaskAPI.setInitParameter(
    			"jersey.config.server.provider.classnames",
    			TaskAPI.class.getCanonicalName() );

    	ServletHolder jerseyServletSSE = apiContext.addServlet(
    			org.glassfish.jersey.servlet.ServletContainer.class, "/log/*");
    	jerseyServletSSE.setInitOrder(0);
    	jerseyServletSSE.setAsyncSupported(true);
    	jerseyServletSSE.setInitParameter(
    			"jersey.config.server.provider.classnames",
    			LogBroadcaster.class.getCanonicalName() );
    	
    	ServletHolder jerseyServletCluster = apiContext.addServlet(
    			org.glassfish.jersey.servlet.ServletContainer.class, "/cluster/*");
    	jerseyServletCluster.setInitOrder(0);
    	jerseyServletCluster.setAsyncSupported(true);
    	jerseyServletCluster.setInitParameter(
    			"jersey.config.server.provider.classnames",
    			ClusterAPI.class.getCanonicalName() );
    	
    	ServletHolder jerseyServletRepository = apiContext.addServlet(
    			org.glassfish.jersey.servlet.ServletContainer.class, "/repository/*");
    	jerseyServletRepository.setInitOrder(0);
    	jerseyServletRepository.setAsyncSupported(true);
    	jerseyServletRepository.setInitParameter(
    			"jersey.config.server.provider.classnames",
    			RepositoryAPI.class.getCanonicalName() );

    	
    	ServletHolder jerseyServletObjectIndex = apiContext.addServlet(
    			org.glassfish.jersey.servlet.ServletContainer.class, "/objects/*");
    	jerseyServletObjectIndex.setInitOrder(0);
    	jerseyServletObjectIndex.setAsyncSupported(true);
    	jerseyServletObjectIndex.setInitParameter(
    			"jersey.config.server.provider.classnames",
    			ObjectIndexAPI.class.getCanonicalName() );
    	
    	return apiContext;
    }
    
    private static void runQuilStartupScript()
    {
    	logger.info("Running startup script");
    	
    	try {
    		IMain main = new IMain();
    		main.settings().usejavacp().tryToSetFromPropertyValue("true");
    		main.setContextClassLoader();

    		String scriptPath = System.getenv("QUIL_HOME") + "/config/quil-boot.scala";
    		File file = new File(scriptPath);
    		
    		main.eval(new String(Files.readAllBytes(file.toPath())));

    	} catch(Exception e) {
    		logger.error("Failed to run start up script");
    	}
    }
}