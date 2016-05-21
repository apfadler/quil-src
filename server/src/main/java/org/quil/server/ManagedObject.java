package org.quil.server;

import java.io.Serializable;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class ManagedObject implements Serializable {
		
		@QuerySqlField(index=true)
		private String _indexInThisCache = "0";
		
		@QuerySqlField
		private String _keyInOriginalCache = "";
		
		@QuerySqlField
		private String _cacheName = "";
		
		public ManagedObject(String indexKey, String origKey, String cacheName) {
			_indexInThisCache = indexKey;
			_keyInOriginalCache = origKey;
			_cacheName = cacheName;
		}
		
		public String getCacheName() {
			return _cacheName;
		}

		public String getKeyInOriginalCache() {
			return _keyInOriginalCache;
		}
}
