package org.quil.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import org.quil.JSON.Document;
import org.quil.interpreter.QuantLibTemplates.GenericScalaScript;
import org.quil.interpreter.QuantLibTemplates.Parameters;
import org.quil.server.Cache;

import scala.tools.nsc.Interpreter;

public class DocumentCache extends Cache {
	
	static final Logger logger = LoggerFactory.getLogger(DocumentCache.class);
	
	static public DocumentCache getOrCreate(String cacheName)
	{
		if (caches.containsKey(cacheName) ) {
			
			Cache cache = caches.get(cacheName);
			
			if (cache.getClass() == DocumentCache.class)
			{
				return (DocumentCache) caches.get(cacheName);
			} 
			else
			{
				return null;
			}
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

        cfg.setIndexedTypes(String.class, org.quil.JSON.ComplexElement.class);
   
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
        
        cache.put(key, (Document)doc);
	}
	
	public Document get(String key)
	{
		Ignite ignite = Ignition.ignite();
        IgniteCache<String, Document>  cache = ignite.cache(_cacheName);
        
        return cache.get(key);
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
	
	public  List<Document> filter(String predicate)
	{
		try {
			
			scala.tools.nsc.Settings settings = new scala.tools.nsc.Settings(null) ;
		    settings.usejavacp().tryToSetFromPropertyValue("true");
		    Interpreter interp = new Interpreter( settings); 	
		    interp.setContextClassLoader();
		    
		    String className = "Filter_"+UUID.randomUUID();
		    className = className.replace('-', '_');
		    String script =   "   import org.quil.JSON._; import org.apache.ignite.lang._;  \n" 
		    				+ " class "+className+" extends IgniteBiPredicate[java.lang.String,org.quil.JSON.Document] {  \n" 
		    				+ "	 override def apply(ID:java.lang.String, V:org.quil.JSON.Document):Boolean =  { \n "
		    				+ "	return (" + predicate +")   \n "
		    				+ " }  \n"
		    				+ "}";
		    
		    interp.compileString(script);
		    
		    final IgniteBiPredicate<String, Document> predicateObject =
		    		(IgniteBiPredicate<String, Document>) interp.classLoader().loadClass(className).newInstance();
		    
		    IgniteBiPredicate<String, Document> forIgnite = new IgniteBiPredicate<String, Document>() {
		    	@Override 
		    	public boolean apply(String key, Document value ) {
		    		return predicateObject.apply(key,value);
		    	}
		    };
		    
			return this.filter(forIgnite);
			
		} catch (Exception e) {
			
			e.printStackTrace();

			return new ArrayList<Document>();
			
		}
	}
	
	
}


