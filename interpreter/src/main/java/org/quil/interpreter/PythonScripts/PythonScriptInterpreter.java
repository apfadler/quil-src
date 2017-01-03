package org.quil.interpreter.PythonScripts;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.python.util.PythonInterpreter;
import org.python.core.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;


public class PythonScriptInterpreter implements org.quil.interpreter.Interpreter {

private boolean _error = false;

	static {
		System.loadLibrary("QuantLibJNI");
	}

	final static Logger logger = LoggerFactory.getLogger(PythonScriptInterpreter.class);

	protected String _script = "";
	protected JSONObject _data = new JSONObject();
	protected JSONObject _result = new JSONObject();

	public PythonScriptInterpreter() {
	}
	
	@Override
	public void interpret() throws Exception {
		try {
			
			String script = (String)_data.get("Script");
			
			String init =  "";

			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			StringWriter out = new StringWriter();
			PrintWriter stream = new PrintWriter(out);

			PythonInterpreter interp =
					new PythonInterpreter();

			interp.setOut(out);
			interp.setErr(baos);

			interp.exec(script);

		    _result.put("python_out", out.toString() );
		    _result.put("python_err", baos.toString() );
			
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
