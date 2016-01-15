package org.quil.console;


import scala.tools.nsc.interpreter.ILoop;

public class QuilILoop extends ILoop {

	@Override 
	public void printWelcome() {
		System.out.println("  ");
		System.out.println("  ");
		System.out.println("  ");
		System.out.println(" ******* Quil Scala Console  ******* ");
		System.out.println("  ");
		System.out.println("  ");
		System.out.println("  ");
	}
}
