package org.quil.server;

import java.util.HashMap;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cache {

	final Logger logger = LoggerFactory.getLogger(Cache.class);
	
	protected static HashMap<String, Cache> caches = new HashMap<String, Cache>();
	
	protected String _cacheName;

	public static HashMap<String, Cache> getAllCaches()
	{
		return caches;
	}
	
	public static Cache findCache(String cacheName) 
	{
		return caches.get(cacheName);
	}
	
	public void removeAll()
	{
		try {
			Ignite ignite = Ignition.ignite();
			ignite.cache(_cacheName).removeAll();
		}
		catch(Exception e) {
			logger.error("Failed to clear cache "+_cacheName+": "+e.toString());
		}
	}
}
