package org.quil.interpreter.QuantLibScript;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.quil.JSON.Document;
import org.quil.interpreter.Interpreter;
import org.quil.interpreter.QuantLibTemplates.Market;
import org.quil.server.DocumentCache;
import org.quil.server.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;




public class QuantLibScriptInterpreter implements Interpreter {

private boolean _error = false;
	
	static {
		System.loadLibrary("QuantLibJNI");
	}
	
	final static Logger logger = LoggerFactory.getLogger(QuantLibScriptInterpreter.class);
	
	protected JSONObject _data = new JSONObject();
	protected JSONObject _result = new JSONObject();
	
	public QuantLibScriptInterpreter() {
	}
	
	static HashMap<Integer,Class> compiledClasses = new HashMap<Integer,Class>();
	
	public QuantLibScript compile(scala.tools.nsc.Interpreter intp, String script, String ID, int hashCode)
	{
		intp.compileString(script);
		try {
			Class compiledClass = intp.classLoader().loadClass("Script_"+ID);
			compiledClasses.put(hashCode, compiledClass);
			QuantLibScript scriptClass = (QuantLibScript)compiledClass.newInstance();
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
	public void interpret() throws Exception {
		
		logger.info("Running task: " +_data.toJSONString());
		
		String repository = (String) _data.get("Repository");
		if (repository == null) {
			throw new Exception("Empty repository in task definition.");
		}
		
		String template = (String) _data.get("PricerScript");
		if (template == null) {
			throw new Exception("Empty PricerScript in task definition.");
		}
		
		String script = SimpleCache.getOrCreate(repository).get(template);
		if (script == null) {
			throw new Exception("No such pricer script");
		}
		
		long start = System.currentTimeMillis();
		
		QuantLibScript compiledScript;
		
		int hashCode = script.hashCode();
		if (compiledClasses.containsKey(hashCode)) {
			
			logger.info("Class is cached.");
			
			compiledScript = (QuantLibScript) compiledClasses.get(hashCode).newInstance();
			
		} else {
		
			logger.info("Class does not exist in cache. Compiling...");
			
			scala.tools.nsc.Settings settings = new scala.tools.nsc.Settings(null) ;
		    settings.usejavacp().tryToSetFromPropertyValue("true");
		    scala.tools.nsc.Interpreter interp = new scala.tools.nsc.Interpreter( settings); 	
		    interp.setContextClassLoader();
		    
		    String ID = UUID.randomUUID().toString().replace("-", "_");
		    script = script.replaceAll("class Script extends", "class Script_"+ID+" extends");
		    compiledScript = compile(interp, script,ID, hashCode);
		}
	    
		long stop = System.currentTimeMillis();
		long compileTime = stop-start;
	    logger.debug("Script compilation took " + compileTime + "ms");
		
		JSONObject tradeData = (JSONObject) _data.get("TradeData");
		if (tradeData != null) {
			
			logger.info("Injecting trade parameters.");
			
			if (tradeData.containsKey("Repository") && tradeData.containsKey("Key"))
			{
				//TODO this is horrible...
				Document tradeDataFromRepo = DocumentCache.getOrCreate((String)tradeData.get("Repository")).get((String)(tradeData.get("Key")));
				compiledScript.setTradeData(tradeDataFromRepo);
			}
			else {
				compiledScript.setTradeData(  (new org.quil.JSON.Parser()).parse(tradeData.toString())  );
			}
		}
		
		JSONObject marketData = (JSONObject) _data.get("MarketData");
		if (marketData != null) {
			
			logger.info("Injecting market parameters.");
			
			String base =  (String) marketData.get("Base");
			Market MD = new Market();
			
			MD.setBase(base);
			
			JSONObject overrideMarketData = (JSONObject) marketData.get("Additional");
			if (overrideMarketData != null) {
				for(Iterator iterator = overrideMarketData.keySet().iterator(); iterator.hasNext();) {
				    String key = (String) iterator.next();
				    MD.set(key, (String)overrideMarketData.get(key));
				    
				    logger.info( "Market delta: " + key + " = " + (String)overrideMarketData.get(key));
				}
			}
			
			compiledScript.setMarketData(MD);
		}
		
		start = System.currentTimeMillis();
		_result = (JSONObject) (new org.json.simple.parser.JSONParser()).parse(compiledScript.run().toString());
		stop = System.currentTimeMillis();
		
		_result.put("exec_time", (stop-start)+ " ms");
		_result.put("compile_time", compileTime + " ms");
	}

	@Override
	public void setData(JSONObject data) {
		_data = data;
	}

	@Override
	public JSONObject getResult() {
		return _result;
	}

	@Override
	public boolean getError() {
		// TODO Auto-generated method stub
		return _error;
	}
}
