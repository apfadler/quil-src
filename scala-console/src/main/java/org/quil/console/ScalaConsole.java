package org.quil.console;

import static org.apache.ignite.events.EventType.EVTS_TASK_EXECUTION;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.quil.server.QuilServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.ILoop;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class ScalaConsole 
{
	static Logger logger = LoggerFactory.getLogger(ScalaConsole.class);
	static Options options = new Options();
	
    public static void main( String[] args )
    {

    	// Parse Args
    	options.addOption("h", "help", false, "show help.");
		options.addOption("e", "execute", true, "execute [code].");

		CommandLineParser parser = new BasicParser();

		String scriptInit = "import org.quil.server._;import org.quil.interpreter._; "+
		"import org.quil.server.Tasks._; import org.quil.server.Tasks._; import org.quil.interpreter.QuantLibTemplates; import scala.collection.JavaConversions._;";

		String loadScript = "";
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);

			if (cmd.hasOption("h"))
				help();

			if (cmd.hasOption("e")) {
				logger.debug("Using cli argument -e=" + cmd.getOptionValue("e"));
				// Whatever you want to do with the setting goes here
				scriptInit += cmd.getOptionValue("e");
			} else {
			
				for (String arg : args)
				{
					if (!arg.startsWith("--"))
					{
						loadScript = arg;
					}
				}
			}

		} catch (ParseException e) {
			logger.error("Failed to parse comand line properties", e);
			help();
		}
   
    	
    	// Initialize Ignite
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
    	
    	// Connect to ignite and run console 
    	try (Ignite ignite =  Ignition.start(cfg)) {

			logger.info("Connected to Ignite Cluster.");
			
			scala.tools.nsc.Settings settings = new scala.tools.nsc.Settings(null) ;
		    settings.usejavacp().tryToSetFromPropertyValue("true");
		    QuilILoop loop = new QuilILoop(scriptInit, loadScript); 	
		    
		    //loop.interpreter().setContextClassLoader();
		    
		    //loop.pasteCommand(scriptInit);
		    loop.process(settings);
	    
    	}
	   
    }
    
    private static void help() {
		// This prints out some help
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", options);
		System.exit(0);
	}
}
