package org.quil.interpreter;

import org.json.simple.JSONObject;

public interface Interpreter {

	public void interpret();
	public void setData(JSONObject data);
	public JSONObject getResult();
	
}
