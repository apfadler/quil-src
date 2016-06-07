package org.quil.repository;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteFileSystem;
import org.apache.ignite.Ignition;
import org.apache.ignite.igfs.IgfsException;
import org.apache.ignite.igfs.IgfsInputStream;
import org.apache.ignite.igfs.IgfsPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;

//TODO extract interface

public class CachedFileSystemRepository {

	static final Logger logger = LoggerFactory.getLogger(CachedFileSystemRepository.class);

	private static CachedFileSystemRepository _instance;

	private String _baseFolder;

	public static CachedFileSystemRepository instance() {
		if (_instance == null)
			_instance =  new CachedFileSystemRepository();

		return _instance;
	}

	public CachedFileSystemRepository() {

		_baseFolder = System.getenv("QUIL_HOME")+"/sampledata/";

		logger.info("Building IGFS cache for repository.");
		initIgfs(null, "/");
	}

	protected void initIgfs(File node, String path) {

		logger.info("initIGFS: " + path);

		Ignite ignite = Ignition.ignite();
		IgniteFileSystem fs = ignite.fileSystem("quil-igfs");

		if (node == null) {
			node = new File(_baseFolder);
		}

		if(node.isDirectory()){

			mkdirs(fs, new IgfsPath(path));
			for(File subNode : node.listFiles()){

				if (node.getAbsolutePath().equals((new File(_baseFolder)).getAbsolutePath()))
					initIgfs(subNode, path + subNode.getName() );
				else
					initIgfs(subNode, path + "/" + subNode.getName() );
			}

		} else {
			try {
				File file = new File(_baseFolder + path);
				create(fs, new IgfsPath(path), Files.readAllBytes(file.toPath()));
			}catch (IOException e ) {
				e.printStackTrace();
				logger.info("IOException occured during file creation in igfs.");
			}
		}
	}

	public void deleteFile(String path) {

		Ignite ignite = Ignition.ignite();
		IgniteFileSystem fs = ignite.fileSystem("quil-igfs");

		File file = new File(_baseFolder+path);
		file.delete();

		delete(fs, new IgfsPath("/"+path));
	}
	
	public void putFolder(String path) {

		Ignite ignite = Ignition.ignite();
		IgniteFileSystem fs = ignite.fileSystem("quil-igfs");

		File file = new File(_baseFolder+path);
		file.mkdir();

		mkdirs(fs, new IgfsPath("/"+path));
	}

	public String getFile(String path) throws IOException {

		Ignite ignite = Ignition.ignite();
		IgniteFileSystem fs = ignite.fileSystem("quil-igfs");

		return new String(read(fs, new IgfsPath("/"+path)));
	}


	public String putFile(String path, String content) throws FileNotFoundException {

		PrintWriter out;
		out = new PrintWriter(_baseFolder+path);
		out.print(content);
		out.close();

		Ignite ignite = Ignition.ignite();
		IgniteFileSystem fs = ignite.fileSystem("quil-igfs");

		try {
			create(fs, new IgfsPath("/"+path), content.getBytes());
		} catch (IOException e) {
			logger.info("IOException occured during put file.");
		}

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

	//TODO different behavior for igfs and normal file system
	private static void delete(IgniteFileSystem fs, IgfsPath path) throws IgniteException {
		assert fs != null;
		assert path != null;

		if (fs.exists(path)) {
			boolean isFile = fs.info(path).isFile();

			try {
				fs.delete(path, true);

				System.out.println();
				System.out.println(">>> Deleted " + (isFile ? "file" : "directory") + ": " + path);
			}
			catch (IgfsException e) {
				System.out.println();
				System.out.println(">>> Failed to delete " + (isFile ? "file" : "directory") + " [path=" + path +
						", msg=" + e.getMessage() + ']');
			}
		}
		else {
			System.out.println();
			System.out.println(">>> Won't delete file or directory (doesn't exist): " + path);
		}
	}

	/**
	 * Creates directories.
	 *
	 * @param fs IGFS.
	 * @param path Directory path.
	 * @throws IgniteException In case of error.
	 */
	private static void mkdirs(IgniteFileSystem fs, IgfsPath path) throws IgniteException {
		assert fs != null;
		assert path != null;

		try {
			fs.mkdirs(path);

			System.out.println();
			System.out.println(">>> Created directory: " + path);
		}
		catch (IgfsException e) {
			System.out.println();
			System.out.println(">>> Failed to create a directory [path=" + path + ", msg=" + e.getMessage() + ']');
		}

		System.out.println();
	}

	/**
	 * Creates file and writes provided data to it.
	 *
	 * @param fs IGFS.
	 * @param path File path.
	 * @param data Data.
	 * @throws IgniteException If file can't be created.
	 * @throws IOException If data can't be written.
	 */
	private static void create(IgniteFileSystem fs, IgfsPath path, byte[] data)
			throws IgniteException, IOException {
		assert fs != null;
		assert path != null;

		try (OutputStream out = fs.create(path, true)) {
			System.out.println();
			System.out.println(">>> Created file: " + path);

			if (data != null) {
				out.write(data);

				System.out.println();
				System.out.println(">>> Wrote data to file: " + path);
			}
		}

		System.out.println();
	}

	/**
	 * Opens file and appends provided data to it.
	 *
	 * @param fs IGFS.
	 * @param path File path.
	 * @param data Data.
	 * @throws IgniteException If file can't be created.
	 * @throws IOException If data can't be written.
	 */
	private static void append(IgniteFileSystem fs, IgfsPath path, byte[] data) throws IgniteException, IOException {
		assert fs != null;
		assert path != null;
		assert data != null;
		assert fs.info(path).isFile();

		try (OutputStream out = fs.append(path, true)) {
			System.out.println();
			System.out.println(">>> Opened file: " + path);

			out.write(data);
		}

		System.out.println();
		System.out.println(">>> Appended data to file: " + path);
	}

	/**
	 * Opens file and reads it to byte array.
	 *
	 * @param fs IgniteFs.
	 * @param path File path.
	 * @throws IgniteException If file can't be opened.
	 * @throws IOException If data can't be read.
	 */
	private static byte[] read(IgniteFileSystem fs, IgfsPath path) throws IgniteException, IOException {
		assert fs != null;
		assert path != null;
		assert fs.info(path).isFile();

		byte[] data = new byte[(int)fs.info(path).length()];

		try (IgfsInputStream in = fs.open(path)) {
			in.read(data);
		}

		System.out.println();
		System.out.println(">>> Read data from " + path );

		return data;
	}

	/**
	 * Lists files in directory.
	 *
	 * @param fs IGFS.
	 * @param path Directory path.
	 * @throws IgniteException In case of error.
	 */
	private static void list(IgniteFileSystem fs, IgfsPath path) throws IgniteException {
		assert fs != null;
		assert path != null;
		assert fs.info(path).isDirectory();

		Collection<IgfsPath> files = fs.listPaths(path);

		if (files.isEmpty()) {
			System.out.println();
			System.out.println(">>> No files in directory: " + path);
		}
		else {
			System.out.println();
			System.out.println(">>> List of files in directory: " + path);

			for (IgfsPath f : files)
				System.out.println(">>>     " + f.name());
		}

		System.out.println();
	}
}