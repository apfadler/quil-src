package org.quil.server;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class Log4JSseAppender extends AppenderSkeleton {

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void activateOptions()
	{
		super.activateOptions();
	}

	@Override
	public boolean requiresLayout() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected void append(LoggingEvent event) {
		// TODO Auto-generated method stub

		String message = this.layout.format(event);
		LogBroadcaster.broadcastMessage(message);
	}

}
