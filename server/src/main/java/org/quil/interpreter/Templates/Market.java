package org.quil.interpreter.Templates;

import java.util.HashMap;

import org.quil.server.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Market {

	final static Logger logger = LoggerFactory.getLogger(Market.class);

	protected String _base;
	protected HashMap<String,String> _local = new HashMap<String,String>();
	
	public Market() {
	}
	
	public void setBase(String base) {
		_base = base;
	}
	
	public void set(String key, String value) {
		_local.put(key, value);
	}
	
	public String get(String key) throws Exception {
		
		String value = _local.get(key);
		if (value != null) {
			return value;
		}
		
		value = SimpleCache.getOrCreate(_base).get(key);
		if (value != null) {
			return value;
		} 
		
		throw new Exception("Failed to find MD element " + key);
	}
	
}
