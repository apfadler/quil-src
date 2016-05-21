package org.quil.server.Tasks;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.quil.interpreter.Interpreter;
import org.quil.server.ResultsCache;

public class ScriptedTask extends Task {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ScriptedTask(String taskName, String taskXML) {
		super(taskName, taskXML);
		
	}

	@Override
	public void run() throws Exception {
		
		JSONParser parser = new JSONParser();

		final JSONObject taskDescription = (JSONObject) parser.parse(_taskDescription);

		Interpreter interpreter = (Interpreter) Class.forName((String) taskDescription.get("Interpreter")).newInstance();
		interpreter.setData(taskDescription);
		interpreter.interpret();

		Task.updateResult(_taskName,interpreter.getResult().toJSONString());
		
		ResultsCache.add(_taskName,  _taskTag, 0,
				 "PV", interpreter.getResult().toJSONString(),
				  0,	0);

		if (interpreter.getError())
			throw new Exception("Error during interpretation in task PriceTrade.");
	}
}
