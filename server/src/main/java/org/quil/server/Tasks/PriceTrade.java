package org.quil.server.Tasks;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.quil.interpreter.Interpreter;

public class PriceTrade extends Task {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PriceTrade(String taskName, String taskXML) {
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

	}

}
