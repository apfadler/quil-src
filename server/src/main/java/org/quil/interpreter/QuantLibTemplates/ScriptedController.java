package org.quil.interpreter.QuantLibTemplates;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import scala.tools.nsc.Interpreter;
import scala.tools.nsc.interpreter.ILoop;
import scala.tools.nsc.interpreter.IMain;
import scala.tools.nsc.interpreter.NamedParam;
import scala.tools.nsc.interpreter.NamedParamClass;
import scala.tools.nsc.interactive.REPL;


public class ScriptedController extends Controller  {

	final static Logger logger = LoggerFactory.getLogger(ScriptedController.class);

	private String script;
	private static ILoop interp;

	
	public ScriptedController(String script)
	{
		this.script = script;
	}
	
	
	public GenericScalaScript compile(Interpreter intp, String script, String ID)
	{
		intp.compileString(script);
		try {
			GenericScalaScript scriptClass =  (GenericScalaScript) intp.classLoader().loadClass("Script_"+ID).newInstance();
			return scriptClass;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		
		return null;
	}
	
	@Override
	public  Parameters run()
	{
		try {
			Parameters P = (Parameters)_context.getBean("P");
			long start = System.currentTimeMillis();
			
			scala.tools.nsc.Settings settings = new scala.tools.nsc.Settings(null) ;
		    settings.usejavacp().tryToSetFromPropertyValue("true");
		    Interpreter interp = new Interpreter( settings); 	
		    interp.setContextClassLoader();
		    
		    String ID = UUID.randomUUID().toString().replace("-", "_");
		    this.script = this.script.replaceAll("class Script extends", "class Script_"+ID+" extends");
		    GenericScalaScript scalaScript = compile(interp,this.script,ID);
		    
		    logger.debug("Script compilation took " + (System.currentTimeMillis() - start) + "ms");
		    start = System.currentTimeMillis();
		    scalaScript.main(_context, Ignition.ignite());
		
		    logger.debug("Script execution " + (System.currentTimeMillis() - start) + "ms");

			Parameters O = (Parameters)_context.getBean("O");
			    	
			return O;
			
		} catch (Exception e) {
			
			e.printStackTrace();

			Parameters O = new Parameters();
			O.set("ERROR", e.toString());
			return O;
			
		}
	}
}