package org.mobicents.slee.sipevent.server.publication;

/**
 * Interface to abstract what is the real logger impl.
 * 
 * @author martins
 *
 */
public interface PublicationControlLogger {

	public boolean isDebugEnabled();
	
	public boolean isInfoEnabled();
	
	public void debug(String msg);
	
	public void info(String msg);
	
	public void warn(String msg);
	
	public void error(String msg,Throwable t);
	
}
