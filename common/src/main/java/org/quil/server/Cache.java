package org.quil.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CachePeekMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cache {

	final Logger logger = LoggerFactory.getLogger(Cache.class);
	
	protected static HashMap<String, Cache> caches = new HashMap<String, Cache>();
	
	protected String _cacheName;

	public String getCacheName() {
		return _cacheName;
	}
	
	public static HashMap<String, Cache> getAllCaches()
	{
		return caches;
	}
	
	public static Cache findCache(String cacheName) 
	{
		return caches.get(cacheName);
	}
	
	public synchronized void removeAll()
	{
		try {
			logger.info("Clearing cache " + _cacheName);
			Ignite ignite = Ignition.ignite();
			ignite.cache(_cacheName).removeAll();
			caches.remove(_cacheName);
			
			Iterator<Map.Entry<String,String>> it = ObjectIndex.all().entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String,String> e = it.next();
				if (e.getValue().compareTo(_cacheName) == 0) {
					ObjectIndex.removeFromIndex(e.getKey(), e.getValue());
				}
			}
		}
		catch(Exception e) {
			logger.error("Failed to clear cache "+_cacheName+": "+e.toString());
		}
	}
	
	public int size() {
		try {
			
			Ignite ignite = Ignition.ignite();
			return ignite.cache(_cacheName).size(CachePeekMode.ALL);
		}
		catch(Exception e) {
			logger.error("Failed to get size of cache "+_cacheName+": "+_cacheName);
			return 0;
		}
	}
}
