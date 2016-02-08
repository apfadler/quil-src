package org.quil.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.quil.JSON.Document;
import org.quil.repository.FileSystemRepository;

public class ObjectIndex {
	
	public static HashMap<String, String> All = new HashMap<String,String>();
	public static ArrayList<String> usedCaches = new ArrayList<String>();
	
	public static void add(String cacheType, String cacheID, String fileID, String filePath) throws IOException {
		
		FileSystemRepository repo = new FileSystemRepository();
		
		if (cacheType.compareTo("SimpleCache") == 0) {
			SimpleCache cache = SimpleCache.getOrCreate(cacheID);
        	cache.put(fileID, repo.getFile(filePath));
        	
        	All.put(fileID,cacheID);
        	
        	if (!usedCaches.contains(cacheID)) {
        		usedCaches.add(cacheID);
        	}
		}
		
		if (cacheType.compareTo("DocumentCache") == 0) {
			DocumentCache cache = DocumentCache.getOrCreate(cacheID);
        	cache.put(fileID, (Document)((new org.quil.JSON.Parser()).parse(repo.getFile(filePath))));
        	
        	All.put(fileID,cacheID);
        	
        	if (!usedCaches.contains(cacheID)) {
        		usedCaches.add(cacheID);
        	}
		}
		
	}

}
