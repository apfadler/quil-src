package org.quil.interpreter.QuantLibTemplates;

import java.util.HashMap;
import java.util.UUID;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.quil.interpreter.QuantLibScript.QuantLibScript;
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

	private String _script;
	private static ILoop interp;

	
	public ScriptedController(String script)
	{
		_script = script;
	}


	static HashMap<Integer,Class> compiledClasses = new HashMap<Integer,Class>();

	public GenericScalaScript compile(scala.tools.nsc.Interpreter intp, String script, String ID, int hashCode)
	{

		try {

			intp.compileString(script);
			Class compiledClass = intp.classLoader().loadClass("Script_"+ID);
			compiledClasses.put(hashCode, compiledClass);
			GenericScalaScript scriptClass = (GenericScalaScript) compiledClass.newInstance();
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
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	@Override
	public  Parameters run()
	{
		try {
			Parameters P = (Parameters)_context.getBean("P");
			long start = System.currentTimeMillis();

		    String ID = UUID.randomUUID().toString().replace("-", "_");

			int hashCode = _script.hashCode();
			GenericScalaScript scalaScript;
			if (compiledClasses.containsKey(hashCode)) {

				logger.info("Class is cached.");

				scalaScript = (GenericScalaScript) compiledClasses.get(hashCode).newInstance();

			} else {

				logger.info("Class does not exist in cache. Compiling...");

				scala.tools.nsc.Settings settings = new scala.tools.nsc.Settings(null) ;
				settings.usejavacp().tryToSetFromPropertyValue("true");
				scala.tools.nsc.Interpreter interp = new scala.tools.nsc.Interpreter( settings);
				interp.setContextClassLoader();

				_script = _script.replaceAll("class Script extends", "class Script_"+ID+" extends");
				scalaScript = compile(interp, _script,ID, hashCode);
			}
		    
		    logger.debug("Script compilation/class lookup took " + (System.currentTimeMillis() - start) + "ms");
		    start = System.currentTimeMillis();
		    scalaScript.main(_context, Ignition.ignite());
		
		    logger.debug("Script execution " + (System.currentTimeMillis() - start) + "ms");

			Parameters O = (Parameters)_context.getBean("O");
			    	
			return O;
			
		} catch (Exception e) {
			
			e.printStackTrace();

			Parameters O = new Parameters();
			O.set("ERROR", e.toString());
			_error = true;
			return O;
			
		}
	}
}