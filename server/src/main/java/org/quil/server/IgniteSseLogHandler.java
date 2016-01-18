package org.quil.server;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class IgniteSseLogHandler extends Handler {

	@Override
	public void publish(LogRecord record) {
		// TODO Auto-generated method stub
		String logMessage = "";
		
		logMessage += record.getLevel() + ": ";
		logMessage += record.getSourceClassName() + ":";
		logMessage += record.getSourceMethodName() + ":";
		logMessage += "<" + record.getMessage() + ">";
		logMessage += "\n";
		
		LogBroadcaster.broadcastMessage(logMessage);
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}

}
