package org.quil.interpreter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import org.json.simple.JSONObject;
import org.quil.interpreter.Templates.Controller;
import org.quil.interpreter.Templates.Parameters;
import org.quil.server.SimpleCache;
import org.quil.server.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class XmlTemplateInterpreter implements Interpreter {
	
	static {
		System.loadLibrary("QuantLibJNI");
	}
	
	final static Logger logger = LoggerFactory.getLogger(TaskRunner.class);
	
	protected JSONObject _data = new JSONObject();
	protected JSONObject _result = new JSONObject();
	
	public XmlTemplateInterpreter() {
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
			
			logger.info("Injecting parameters.");
			
			Parameters P = (Parameters) context.getBean("P");
			for(Iterator iterator = tradeData.keySet().iterator(); iterator.hasNext();) {
			    String key = (String) iterator.next();
			    P.set(key, (String)tradeData.get(key));
			    
			    logger.info( key + " = " + (String)tradeData.get(key));
			}
		}

		for (String id : context.getBeanNamesForType(Controller.class))
		{
			Controller executor = (Controller) context.getBean(id);
			executor.setApplicationContext(context);
			Parameters p = executor.run();
			
			_result = p.toJSONObject();
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

}