package org.quil.interpreter;

import org.json.simple.JSONObject;

public interface Interpreter {

	public void interpret() throws Exception;
	public void setData(JSONObject data);
	public JSONObject getResult();
	public boolean getError();
	
}
