package org.quil.interpreter.ScalaScripts;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.awt.List;

import org.json.simple.JSONObject;
import org.quil.server.DocumentCache;
import org.quil.server.SimpleCache;
import org.quil.server.Tasks.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.tools.nsc.Interpreter;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.ILoop;
import scala.tools.nsc.interpreter.IMain;
import scala.tools.nsc.interpreter.NamedParam;
import scala.tools.nsc.interpreter.NamedParamClass;
import scala.tools.nsc.interactive.REPL;


public class ScalaScriptInterpreter implements org.quil.interpreter.Interpreter {

private boolean _error = false;
	
	static {
		System.loadLibrary("QuantLibJNI");
	}
	
	final static Logger logger = LoggerFactory.getLogger(TaskRunner.class);
	
	protected String _script = "";
	protected JSONObject _data = new JSONObject();
	protected JSONObject _result = new JSONObject();
	
	public ScalaScriptInterpreter() {
	}
	
	@Override
	public void interpret() throws Exception {
		try {
			
			String script = (String)_data.get("Script");
			
			String init =  "import org.quil.server._;import org.quil.interpreter._; "+
						   "import org.json.simple._; var result = new JSONObject();" +
						   "import org.quil.server.Tasks._; import org.quil.server.Tasks._; import org.quil.interpreter.QuantLibTemplates; import scala.collection.JavaConversions._;";
		
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			Settings settings = new Settings();
			settings.usejavacp().tryToSetFromPropertyValue("true");
			
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			StringWriter out = new StringWriter();
			PrintWriter stream = new PrintWriter(out);
			
			IMain imain = new IMain(settings, stream);
			
			((scala.Console)imain.get("Console")).setOut(baos);
			
			imain.interpret(init);
			imain.interpret(script);
			
		    
		    _result.put("scala_output", out.toString() );
		    _result.put("console_output", baos.toString() );
			
		} catch (Exception e) {
			e.printStackTrace();
			_error = true;
		}
	}

	@Override
	public void setData(JSONObject data) {
		_data = data;
	}
	

	@Override
	public JSONObject getResult() {
		return _result;
	}

	@Override
	public boolean getError() {
		// TODO Auto-generated method stub
		return _error;
	}
}
