package org.mobicents.slee.sipevent.server.publication;

import javax.slee.facilities.Tracer;

public class SLEEPublicationControlLogger implements PublicationControlLogger {

	private final Tracer tracer;
	
	public SLEEPublicationControlLogger(Tracer tracer) {
		this.tracer = tracer;
	}
	
	@Override
	public boolean isDebugEnabled() {
		return tracer.isFineEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return tracer.isInfoEnabled();
	}

	@Override
	public void debug(String msg) {
		tracer.fine(msg);
	}

	@Override
	public void info(String msg) {
		tracer.info(msg);
	}

	@Override
	public void warn(String msg) {
		tracer.warning(msg);
	}

	@Override
	public void error(String msg, Throwable t) {
		tracer.severe(msg,t);
	}

}
