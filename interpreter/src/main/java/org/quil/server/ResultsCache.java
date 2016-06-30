package org.quil.server;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.resources.LoggerResource;
import org.quil.server.Tasks.ResultItem;

public class ResultsCache extends Cache {

	@LoggerResource
    private static IgniteLogger logger;
	
	public static void add(String _taskName, String _taskTag, int _index, String _id,
			String _key, String _stringValue, double _doubleValue,
			int _intValue) {
		
		ResultItem r = new ResultItem( _taskName,  _taskTag,  _index, _id, _key,  _stringValue,  _doubleValue,_intValue);
		
		Ignite ignite = Ignition.ignite();
		CacheConfiguration<String, ResultItem> cfg = new CacheConfiguration<String, ResultItem>();
		
        cfg.setCacheMode(CacheMode.REPLICATED);
        cfg.setName("Results");
        cfg.setIndexedTypes(String.class, ResultItem.class);
        
        IgniteCache<String, ResultItem> results = ignite.getOrCreateCache(cfg);
        
        results.put(r.getTaskName()+"_"+_index+"_"+_key, r);
	}
	
}
