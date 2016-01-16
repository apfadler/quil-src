package org.quil.console;


import scala.tools.nsc.interpreter.ILoop;

public class QuilILoop extends ILoop {

	private String _initCmd = "";
	private String _initFile = "";
	
	public QuilILoop() {
		super();
	}
	
	public QuilILoop(String initCmd, String initFile) {
		super();
		
		_initCmd = initCmd;
		_initFile = initFile;
	}
	
	@Override 
	public void printWelcome() {
		System.out.println("  ");
		System.out.println("  ");
		System.out.println("  ");
		System.out.println(" ******* Quil Scala Console  ******* ");
		System.out.println("  ");
		System.out.println("  ");
		System.out.println("  ");
		
		processLine(_initCmd);
		
		if (_initFile.length() > 0)
			command(":load " + _initFile);
	}
}
