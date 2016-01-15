package org.quil.interpreter.QuantLibTemplates;

import java.util.HashMap;

import java.util.Map;
import org.json.simple.JSONObject;

public class Parameters {

	public HashMap<String,String> _params = new HashMap<String,String>();
	
	public Parameters(HashMap<String,String> params)
	{
		this._params = params;
	}
	
	public Parameters()
	{
	}
	
	public String get(String Id)
	{
		return  _params.get(Id);
	}
	
	public String set(String Id, String Value)
	{
		return  _params.put(Id, Value);
	}
	
	public JSONObject toJSONObject()
	{
		JSONObject obj = new JSONObject();
		
		for (Map.Entry<String, String> entry : _params.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    obj.put(key,value); 
		}

		return obj;
	}
	
	public String toJSONString()
	{
		return toJSONObject().toJSONString();
	}
	
	public HashMap<String, String> all() {
			return _params;
	}
	
}

