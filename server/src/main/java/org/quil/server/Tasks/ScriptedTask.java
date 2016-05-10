package org.quil.server.Tasks;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.quil.interpreter.Interpreter;

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

		if (interpreter.getError())
			throw new Exception("Error during interpretation in task PriceTrade.");
	}
}
