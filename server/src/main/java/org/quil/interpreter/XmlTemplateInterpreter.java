package org.quil.interpreter;

import org.apache.ignite.IgniteLogger;
import org.apache.ignite.resources.LoggerResource;
import org.json.simple.JSONObject;
import org.quil.server.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlTemplateInterpreter implements Interpreter {
	
	final static Logger logger = LoggerFactory.getLogger(TaskRunner.class);
	
	protected JSONObject _data = new JSONObject();
	protected JSONObject _result = new JSONObject();
	
	public XmlTemplateInterpreter() {
	}
	
	@Override
	public void interpret() {
		logger.info("Running task: " +_data.toJSONString());
	}

	@Override
	public void setData(JSONObject data) {
		_data = data;
		
		_result.put("PV", "0.00");
	}

	@Override
	public JSONObject getResult() {
		return _result;
	}

}