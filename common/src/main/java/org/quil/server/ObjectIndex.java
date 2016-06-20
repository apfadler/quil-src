package org.quil.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.cache.Cache.Entry;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.quil.repository.FileSystemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quil.JSON.Document;

public class ObjectIndex {
	
	final static Logger logger = LoggerFactory.getLogger(ObjectIndex.class);
	
	public static void addToIndex(String origKey, String cacheName) {
		
		Ignite ignite = Ignition.ignite();
		CacheConfiguration<String, ManagedObject> cfg = new CacheConfiguration<String, ManagedObject>();
		
        cfg.setCacheMode(CacheMode.REPLICATED);
        cfg.setName("IndexedObjects");
        cfg.setIndexedTypes(String.class, ManagedObject.class);
        
        IgniteCache<String,ManagedObject> objects = ignite.getOrCreateCache(cfg);
		
        String indexKey = cacheName + "_" + origKey;
        
		objects.put(indexKey, new ManagedObject(indexKey,origKey,cacheName));
		
	}
	
	public static void removeFromIndex(String key, String cacheName) {
		
		Ignite ignite = Ignition.ignite();
		CacheConfiguration<String, ManagedObject> cfg = new CacheConfiguration<String, ManagedObject>();
		
        cfg.setCacheMode(CacheMode.REPLICATED);
        cfg.setName("IndexedObjects");
        cfg.setIndexedTypes(String.class, ManagedObject.class);
        
        IgniteCache<String,ManagedObject> objects = ignite.getOrCreateCache(cfg);
		
        String indexKey = cacheName + "_" + key;
        
		objects.remove(indexKey);
		
	}
	
	synchronized static public HashMap<String, String> all() {
		
		try {
			Ignite ignite = Ignition.ignite();
			CacheConfiguration<String, ManagedObject> cfg = new CacheConfiguration<String, ManagedObject>();
			
	        cfg.setCacheMode(CacheMode.REPLICATED);
	        cfg.setName("IndexedObjects");
	        cfg.setIndexedTypes(String.class, ManagedObject.class);
	        
	        IgniteCache<String, ManagedObject> objects = ignite.getOrCreateCache(cfg);
	    
	        HashMap<String, String> all = new HashMap<String, String>();
	        
	        
	        if (objects != null) {
	        	
		        for (Entry<String, ManagedObject> entry : objects) {
		        	all.put(entry.getValue().getKeyInOriginalCache(),entry.getValue().getCacheName());
		        }
		        
	        }
	        
			return all;
        }
        catch (Exception e) {
        
        	return new HashMap<String, String>();
        }
	}
	
	
	public static void add(String cacheType, String cacheID, String fileID, String filePath) throws Exception {
		
		FileSystemRepository repo = new FileSystemRepository();
		
		if (cacheType.compareTo("SimpleCache") == 0) {
			SimpleCache cache = SimpleCache.getOrCreate(cacheID);
        	cache.put(fileID, repo.getFile(filePath));
        	
        	addToIndex(fileID,cacheID);
		}
		
		if (cacheType.compareTo("DocumentCache") == 0) {
			DocumentCache cache = DocumentCache.getOrCreate(cacheID);
        	cache.put(fileID, (Document)((new org.quil.JSON.Parser()).parse(repo.getFile(filePath))));
   
        	addToIndex(fileID,cacheID);
		}
		
	}
}
