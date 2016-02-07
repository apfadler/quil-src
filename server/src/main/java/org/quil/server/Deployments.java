package org.quil.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.quil.repository.FileSystemRepository;

public class Deployments {
	
	public static HashMap<String, String> All = new HashMap<String,String>();
	public static ArrayList<String> usedCaches = new ArrayList<String>();
	
	public static void add(String cacheType, String cacheID, String fileID, String filePath) throws IOException {
		
		FileSystemRepository repo = new FileSystemRepository();
		
		if (cacheType.compareTo("SimpleCache") == 0) {
			SimpleCache cache = SimpleCache.getOrCreate(cacheID);
        	cache.put(fileID, repo.getFile(filePath));
        	
        	All.put(cacheID, fileID);
        	
        	if (!usedCaches.contains(cacheID)) {
        		usedCaches.add(cacheID);
        	}
		}
		
		if (cacheType.compareTo("DocumentCache") == 0) {
			
		}
		
	}

}
