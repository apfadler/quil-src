package org.quil.server.Tasks;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.quil.interpreter.Interpreter;
import org.quil.server.ResultsCache;

public class PriceTrade extends Task {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PriceTrade(String taskName, String taskXML) {
		super(taskName, taskXML);
		
	}

	private String tryToFindTradeID(JSONObject taskDescription) {
		String Id = "";
		try {
			JSONObject tradeData = null;

			try {
				tradeData = (JSONObject) taskDescription.get("TradeData");
			} catch(Exception e) {
			}

			if (tradeData == null) {
				try {
					tradeData = (JSONObject) new JSONParser().parse((String) taskDescription.get("TradeData"));
				} catch (Exception e) {
				}
			}

			Id = (String)tradeData.get("Id");
			if (Id == null)
				Id = (String)tradeData.get("ID");
			if (Id == null)
				Id = "";

		}catch (Exception e) {
		}

		return Id;
	}
	
	@Override
	public void run() throws Exception {

		Task.updateStatus(_taskName, Status.RUNNING);

		JSONParser parser = new JSONParser();

		final JSONObject taskDescription = (JSONObject) parser.parse(_taskDescription);

		String Id = tryToFindTradeID(taskDescription);

		Interpreter interpreter = (Interpreter) Class.forName((String) taskDescription.get("Interpreter")).newInstance();
		interpreter.setData(taskDescription);
		interpreter.interpret();

		Task.updateResult(_taskName,interpreter.getResult().toJSONString());
		
		for (Object r : interpreter.getResult().keySet()) {
			
			String key = (String)r;
			double doubleVal = 0.0;
			int intVal = 0;
			String strVal = "";
			try {
				doubleVal = Double.parseDouble((String)interpreter.getResult().get(key));
				intVal = Integer.parseInt((String)interpreter.getResult().get(key));
				strVal = (String)interpreter.getResult().get(key);
			}catch(Exception e) {
			}
			
			ResultsCache.add(_taskName,  _taskTag, 0, Id,
							  key, strVal,doubleVal,intVal);
		}

		if (interpreter.getError())
			throw new Exception("Error during interpretation in task PriceTrade.");
	}

}
