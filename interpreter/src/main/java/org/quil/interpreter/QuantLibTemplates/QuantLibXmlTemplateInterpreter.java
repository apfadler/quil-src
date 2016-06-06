package org.quil.interpreter.QuantLibTemplates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

import org.json.simple.JSONObject;
import org.quil.JSON.Document;
import org.quil.interpreter.Interpreter;
import org.quil.server.DocumentCache;
import org.quil.server.SimpleCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class QuantLibXmlTemplateInterpreter implements Interpreter {
	
	private boolean _error = false;
	
	static {
		System.loadLibrary("QuantLibJNI");
	}
	
	final static Logger logger = LoggerFactory.getLogger(QuantLibXmlTemplateInterpreter.class);
	
	protected JSONObject _data = new JSONObject();
	protected JSONObject _result = new JSONObject();
	
	public QuantLibXmlTemplateInterpreter() {
	}
	
	@Override
	public void interpret() throws Exception {
		logger.info("Running task: " +_data.toJSONString());
		
		String repository = (String) _data.get("Repository");
		if (repository == null) {
			throw new Exception("Empty repository in task definition.");
		}
		
		String template = (String) _data.get("Template");
		if (template == null) {
			throw new Exception("Empty Template in task definition.");
		}
		
		String templateContent = SimpleCache.getOrCreate(repository).get(template);
		if (templateContent == null) {
			throw new Exception("No such template");
		}
		
		File temp = File.createTempFile("tempfile", ".tmp"); 
		BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
		bw.write(templateContent);
		bw.close();
		
		FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(temp.getAbsolutePath());
		
		JSONObject tradeData = (JSONObject) _data.get("TradeData");
		if (tradeData != null) {
			
			if (tradeData.containsKey("Repository") && tradeData.containsKey("Key"))
			{
				//TODO this is horrible...
				Document tradeDataFromRepo = DocumentCache.getOrCreate((String)tradeData.get("Repository")).get((String)(tradeData.get("Key")));
				tradeData = (JSONObject) (new org.json.simple.parser.JSONParser()).parse(tradeDataFromRepo.toString());
			}
			
			logger.info("Injecting trade parameters.");
			
			Parameters P = (Parameters) context.getBean("P");
			for(Iterator iterator = tradeData.keySet().iterator(); iterator.hasNext();) {
			    String key = (String) iterator.next();
			    P.set(key, (String)tradeData.get(key));
			    
			    logger.info( key + " = " + (String)tradeData.get(key));
			}
		}
		
		JSONObject marketData = (JSONObject) _data.get("MarketData");
		if (tradeData != null) {
			
			logger.info("Injecting market parameters.");
			
			String base =  (String) marketData.get("Base");
			Market MD = (Market) context.getBean("MD");
			
			MD.setBase(base);
			
			JSONObject overrideMarketData = (JSONObject) marketData.get("Additional");
			if (overrideMarketData != null) {
				for(Iterator iterator = overrideMarketData.keySet().iterator(); iterator.hasNext();) {
				    String key = (String) iterator.next();
				    MD.set(key, (String)overrideMarketData.get(key));
				    
				    logger.info( "Market delta: " + key + " = " + (String)overrideMarketData.get(key));
				}
			}
			
		}

		for (String id : context.getBeanNamesForType(Controller.class))
		{
			Controller executor = (Controller) context.getBean(id);
			executor.setApplicationContext(context);
			Parameters p = executor.run();
			
			_result = p.toJSONObject();
			
			if (executor.getError())
				_error = true;
		}
		
		
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