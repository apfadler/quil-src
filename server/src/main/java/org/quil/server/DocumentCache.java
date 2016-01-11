package org.quil.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

public class DocumentCache {
	
	static final Logger logger = LoggerFactory.getLogger(DocumentCache.class);
	
	static private HashMap<String, DocumentCache> caches = new HashMap<String, DocumentCache>();
	
	private String _cacheName;
	
	static public DocumentCache getOrCreate(String cacheName)
	{
		if (caches.containsKey(cacheName)) {
			return caches.get(cacheName);
		}
		else {
			DocumentCache cache = new DocumentCache(cacheName);
			caches.put(cacheName, cache);
			
			return cache;
		}
		
	}

	public DocumentCache(String cacheName)
	{
		logger.debug("Creating cache " + cacheName);
		
		_cacheName = cacheName;
		
		CacheConfiguration<String, Document> cfg = new CacheConfiguration<String, Document>();
		
        cfg.setCacheMode(CacheMode.REPLICATED);
        cfg.setName(_cacheName);

        //cfg.setIndexedTypes(String.class, org.quil.JSON.ComplexElement.class);
   
        Ignite ignite = Ignition.ignite();
        ignite.getOrCreateCache(cfg);
        
        logger.debug("Cache created");
	}
	
	public int size()
	{
		Ignite ignite = Ignition.ignite();
        IgniteCache<String, Document>  cache = ignite.cache(_cacheName);
        
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
	
	public void put(String key, Document doc)
	{
		CacheConfiguration<String, Document> cfg = new CacheConfiguration<String, Document>();
		
        cfg.setCacheMode(CacheMode.REPLICATED);
        cfg.setName(_cacheName);

        Ignite ignite = Ignition.ignite();
        IgniteCache<String, Document>  cache = ignite.getOrCreateCache(cfg);
        
        cache.put(key, doc);
	}
	
	public Document get(String key)
	{
		Ignite ignite = Ignition.ignite();
        IgniteCache<String, Document>  cache = ignite.cache(_cacheName);
        
        return cache.get(key);
	}
	
	public void removeAll()
	{
		Ignite ignite = Ignition.ignite();
        IgniteCache<String, Document>  cache = ignite.cache(_cacheName);
        cache.removeAll();
	}
	
	public  List<Document> filter(IgniteBiPredicate<String, Document> predicate)
	{
		ArrayList<Document> res = new ArrayList<Document>();
				
		Ignite ignite = Ignition.ignite();
		
        IgniteCache<String, Document>  cache = ignite.cache(_cacheName);

		QueryCursor<javax.cache.Cache.Entry<String, Document>> cursor = cache.query(new ScanQuery<String, Document>(predicate));

        for (javax.cache.Cache.Entry<String, Document> e : cursor)
        {
        	logger.debug(e.toString());
        	res.add(e.getValue());
        }
        
        cursor.close();
	
		return res;
		
	}
}


