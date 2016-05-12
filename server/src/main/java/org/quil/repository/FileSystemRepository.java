package org.quil.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class FileSystemRepository {

	private String _baseFolder;


	public FileSystemRepository() {

		_baseFolder = System.getenv("QUIL_HOME")+"/sampledata/";
	}
	
	public void deleteFile(String path) {

		File file = new File(_baseFolder+path);
		file.delete();

	}
	
	public void putFolder(String path) {
		File file = new File(_baseFolder+path);
		file.mkdir();
	}

	public String getFile(String path) throws IOException {

		File file = new File(_baseFolder+path);
		return new String(Files.readAllBytes(file.toPath()));

	}


	public String putFile(String path, String content) throws FileNotFoundException {

		PrintWriter out;
		out = new PrintWriter(_baseFolder+path);
		out.print(content);
		out.close();

		return null;
	}

	public void genRepositoryObject(File node, JSONObject obj, String path) {
		
		// TODO Too Specific for js UI

	
		if ( node == null) {
			node = new File(_baseFolder);
			obj.put("id", "/" );
			obj.put("path", path + "/"  );
			obj.put("text", "Root");
		}
		else {
			obj.put("id", path + "/" + node.getName()  );
			obj.put("path", path + "/" + node.getName()  );
			obj.put("text", node.getName());
		}
		
		
		
		
 
		if(node.isDirectory()){
			
			JSONArray children = new JSONArray();
			
			String[] subNodes = node.list();
			for(String filename : subNodes){
				JSONObject file = new JSONObject();
				if (node.getAbsolutePath().equals((new File(_baseFolder)).getAbsolutePath()))
					genRepositoryObject(new File(node, filename),file, path );
				else
					genRepositoryObject(new File(node, filename),file, path + "/" + node.getName() );
				children.add(file);
			}
			
			obj.put("children", children);
			obj.put("type", "dir");
			obj.put("icon", "jstree-custom-folder");
		}
		else
		{
			obj.put("type", "file");
			obj.put("icon", "jstree-custom-file");
		}
	}

}