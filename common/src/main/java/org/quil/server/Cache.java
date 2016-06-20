package org.quil.server;

import java.util.*;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cache {

	final static Logger logger = LoggerFactory.getLogger(Cache.class);

	protected String _cacheName;


	public static void register(Cache cache) throws Exception {
		Ignite ignite = Ignition.ignite();
		IgniteCache<String, Cache> managedCaches = ignite.getOrCreateCache("ManagedCaches");

		if (managedCaches.containsKey(cache.getCacheName()))
			throw new Exception("Cache already exists");

		managedCaches.put(cache.getCacheName(), cache);
	}

	public static  void deregister(Cache cache) {
		Ignite ignite = Ignition.ignite();
		IgniteCache<String, Cache> managedCaches = ignite.getOrCreateCache("ManagedCaches");

		if (managedCaches.containsKey(cache.getCacheName())) {
			managedCaches.remove(cache.getCacheName());
		}
	}

	public static Cache findCache(String cacheName) throws Exception {
		Ignite ignite = Ignition.ignite();
		IgniteCache<String, Cache> managedCaches = ignite.getOrCreateCache("ManagedCaches");

		return managedCaches.get(cacheName);
	}

	public static boolean exists(String cacheName) throws Exception {
		Ignite ignite = Ignition.ignite();
		IgniteCache<String, Cache> managedCaches = ignite.getOrCreateCache("ManagedCaches");

		return managedCaches.containsKey(cacheName);
	}

	public static HashMap<String, Cache> allCaches() {
		Ignite ignite = Ignition.ignite();
		IgniteCache<String, Cache> managedCaches = ignite.getOrCreateCache("ManagedCaches");

		HashMap<String, Cache> all = new HashMap<String, Cache>();

		for (javax.cache.Cache.Entry<String, Cache> entry : managedCaches) {
			all.put(entry.getKey(),entry.getValue());
		}

		return all;
	}

	public String getCacheName() {

		return _cacheName;
	}


	public synchronized void removeAll()
	{
		try {
			logger.info("Clearing cache " + _cacheName);
			Ignite ignite = Ignition.ignite();
			ignite.cache(_cacheName).removeAll();
			
			Iterator<Map.Entry<String,String>> it = ObjectIndex.all().entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String,String> e = it.next();
				if (e.getValue().compareTo(_cacheName) == 0) {
					ObjectIndex.removeFromIndex(e.getKey(), e.getValue());
				}
			}

			Cache.deregister(this);

		}
		catch(Exception e) {
			logger.error("Failed to clear cache "+_cacheName+": "+e.toString());
		}
	}
	
	public int size() {
		try
		{
			Ignite ignite = Ignition.ignite();
			IgniteCache<String, ManagedObject> cache = ignite.cache("IndexedObjects");

			String query = "SELECT COUNT(*) FROM  \"IndexedObjects\".ManagedObject WHERE _CACHENAME='"+_cacheName+"'";
			SqlFieldsQuery sql = new SqlFieldsQuery(query);
			sql.setLocal(true);

			JSONArray result = new JSONArray();

			logger.debug("Query: " + query);

			try (QueryCursor<List<?>> cursor = cache.query(sql)) {

				return Integer.parseInt(cursor.iterator().next().get(0).toString());
			}
		}
		catch(Exception e) {
			logger.error("Failed to get size of cache "+_cacheName+": "+_cacheName);
			return 0;
		}
	}
}
