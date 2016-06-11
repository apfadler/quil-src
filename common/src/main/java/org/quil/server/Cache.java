package org.quil.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.internal.processors.cache.QueryCursorImpl;
import org.apache.ignite.internal.processors.query.GridQueryFieldMetadata;
import org.json.simple.JSONArray;
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
		try
		{
			Ignite ignite = Ignition.ignite();
			IgniteCache<String, ManagedObject> cache = ignite.cache("IndexedObjects");

			String query = "SELECT COUNT(*) FROM  \"IndexedObjects\".ManagedObject WHERE _CACHENAME='"+_cacheName+"'";
			SqlFieldsQuery sql = new SqlFieldsQuery(query);
			sql.setLocal(true);

			JSONArray result = new JSONArray();

			logger.info("Query: " + query);

			try (QueryCursor<List<?>> cursor = cache.query(sql)) {

				JSONArray jsonHeaderRow = new JSONArray();

				return Integer.parseInt(cursor.iterator().next().get(0).toString());
			}
		}
		catch(Exception e) {
			logger.error("Failed to get size of cache "+_cacheName+": "+_cacheName);
			return 0;
		}
	}
}
