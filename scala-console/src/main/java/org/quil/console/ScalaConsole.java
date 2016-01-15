package org.quil.console;

import static org.apache.ignite.events.EventType.EVTS_TASK_EXECUTION;

import java.util.Arrays;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.quil.server.QuilServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.ILoop;


public class ScalaConsole 
{
	static final Logger logger = LoggerFactory.getLogger(ScalaConsole.class);
	
    public static void main( String[] args )
    {
    	// TODO Make configurable
    	TcpDiscoverySpi spi = new TcpDiscoverySpi(); 
    	TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
    	ipFinder.setAddresses(Arrays.asList("127.0.0.1", "127.0.0.1:47500..47509"));
    	spi.setIpFinder(ipFinder);    	
    	IgniteConfiguration cfg = new IgniteConfiguration();
    	cfg.setDiscoverySpi(spi);
    	
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
    	cfg.setPeerClassLoadingEnabled(true);
    	cfg.setIncludeEventTypes(EVTS_TASK_EXECUTION);
    	
    	Ignition.start(cfg);

		logger.info("Connected to Ignite Cluster.");
		
		scala.tools.nsc.Settings settings = new scala.tools.nsc.Settings(null) ;
	    settings.usejavacp().tryToSetFromPropertyValue("true");
	    QuilILoop loop = new QuilILoop(); 	

	    //loop.interpreter().setContextClassLoader();
	    
	    loop.process(settings);
	   
    }
}
