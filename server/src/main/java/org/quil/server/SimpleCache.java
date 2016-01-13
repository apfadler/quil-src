package org.quil.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.cache.Cache.Entry;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimpleCache extends Cache {
	
	static final Logger logger = LoggerFactory.getLogger(SimpleCache.class);
	
	static private HashMap<String, SimpleCache> caches = new HashMap<String, SimpleCache>();
	
	private String _cacheName;
	
	static public SimpleCache getOrCreate(String cacheName)
	{
		if (caches.containsKey(cacheName)) {
			
			Cache cache = caches.get(cacheName);
			
			if (cache.getClass() == SimpleCache.class)
			{
				return (SimpleCache) caches.get(cacheName);
			} 
			else
			{
				return null;
			}
		}
		else {
			SimpleCache cache = new SimpleCache(cacheName);
			caches.put(cacheName, cache);
			
			return cache;
		}
		
	}

	public SimpleCache(String cacheName)
	{
		logger.debug("Creating cache " + cacheName);
		
		_cacheName = cacheName;
		
		CacheConfiguration<String, String> cfg = new CacheConfiguration<String, String>();
		
        cfg.setCacheMode(CacheMode.REPLICATED);
        cfg.setName(_cacheName);

        cfg.setIndexedTypes(String.class, String.class);
   
        Ignite ignite = Ignition.ignite();
        ignite.getOrCreateCache(cfg);
        
        logger.debug("Cache created");
	}
	
	public int size()
	{
		Ignite ignite = Ignition.ignite();
        IgniteCache<?, ?>  cache = ignite.cache(_cacheName);
        
        int s;
        try
		{
        	s = cache.size(CachePeekMode.PRIMARY);	
		}
        catch (Exception e)
        {
        	System.out.println(e.getMessage());
        	return 0;
        }
        
        return s;
	}
	
	public void put(String key, String value)
	{
		CacheConfiguration<String, String> cfg = new CacheConfiguration<String, String>();
		
        cfg.setCacheMode(CacheMode.REPLICATED);
        cfg.setName(_cacheName);

        Ignite ignite = Ignition.ignite();
        IgniteCache<String, String>  cache = ignite.getOrCreateCache(cfg);
        
        cache.put(key, value);
	}
	
	public String get(String key)
	{
		Ignite ignite = Ignition.ignite();
        IgniteCache<String, String>  cache = ignite.cache(_cacheName);
        
        return cache.get(key);
	}
	
	public HashMap<String, String> all() {
		Ignite ignite = Ignition.ignite();
        IgniteCache<String,String> elements = ignite.cache(_cacheName);
        
        HashMap<String, String> all = new HashMap<String, String>();
        
        for (Entry<String, String> entry : elements) {
        	all.put(entry.getKey(),entry.getValue());
        }
        
		return all;
	}
	
	
	public  List<javax.cache.Cache.Entry<String, String>> filter(IgniteBiPredicate<String, String> predicate)
	{
		ArrayList<javax.cache.Cache.Entry<String, String>> res = new ArrayList<javax.cache.Cache.Entry<String, String>>();
				
		Ignite ignite = Ignition.ignite();
		
        IgniteCache<String, String>  cache = ignite.cache(_cacheName);

		QueryCursor<javax.cache.Cache.Entry<String, String>> cursor = cache.query(new ScanQuery<String, String>(predicate));

        for (javax.cache.Cache.Entry<String, String> e : cursor)
        {
        	logger.debug(e.toString());
        	res.add(e);
        }
        
        cursor.close();
	
		return res;
		
	}
}


