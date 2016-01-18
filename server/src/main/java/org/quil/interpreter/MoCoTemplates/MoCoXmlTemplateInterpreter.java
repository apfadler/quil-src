package org.quil.interpreter.MoCoTemplates;

import java.util.Iterator;

import org.json.simple.JSONObject;
import org.quil.interpreter.Interpreter;
import org.quil.server.SimpleCache;
import org.quil.server.Tasks.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dfine.moco.*;

public class MoCoXmlTemplateInterpreter implements Interpreter {
	
	private boolean _error = false;
	
	
	final static Logger logger = LoggerFactory.getLogger(TaskRunner.class);
	
	protected JSONObject _data = new JSONObject();
	protected JSONObject _result = new JSONObject();
	
	public MoCoXmlTemplateInterpreter() {
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
		
		JSONObject tradeData = (JSONObject) _data.get("TradeData");
		if (tradeData != null) {
			
			logger.info("Injecting trade parameters.");
			
			for(Iterator iterator = tradeData.keySet().iterator(); iterator.hasNext();) {
			    String key = (String) iterator.next();
			    
			    // TODO implement
			    
			    logger.info( key + " = " + (String)tradeData.get(key));
			}
		}
		
		try {
			MoCoLoader.loadMoco();
			long sessID = MoCoSessionWrapper.createSession();
			
			
			JSONObject marketData = (JSONObject) _data.get("MarketData");
			if (marketData != null) {
				
				logger.info("Storing MoCo Market Data");
				
				String key =  (String) marketData.get("Key");
				if (key == null)  {
					throw new Exception("No 'Key' property");
				}
					
				String markets =  (String) marketData.get("Repository");
				if (key == null)  {
					throw new Exception("No 'Repository' property");
				}
								
				String moCoMarketData = SimpleCache.getOrCreate(markets).get(key);
				if (moCoMarketData == null)  {
					throw new Exception("No market data with ID '"+key+"' found.");
				}
				
				MoCoXmlLogWrapper.runMoCoXML(sessID, moCoMarketData);
	

				JSONObject overrideMarketData = (JSONObject) marketData.get("Additional");
				if (overrideMarketData != null) {
					for(Iterator iterator = overrideMarketData.keySet().iterator(); iterator.hasNext();) {
					    String quote = (String) iterator.next();
					
					    // TODO implement
					    
					    logger.info( "Market delta: " + key + " = " + (String)overrideMarketData.get(quote));
					}
				}
				
			}

			
			_result.put("MoCoResult", MoCoXmlLogWrapper.runMoCoXML(sessID, templateContent));
			
			MoCoSessionWrapper.deleteSession(sessID);
			
		} catch (Exception e) {
			_error = true;
			_result.put("ERROR", "Could not run MoCo Template.");
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