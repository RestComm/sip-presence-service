/**
 * 
 */
package org.mobicents.slee.sipevent.examples;

import org.mobicents.slee.sipevent.server.publication.PublicationClientControlParent;

/**
 * @author martins
 *
 */
public interface RLSExamplePublisher extends PublicationClientControlParent{

	/**
	 * Starts publishing for the specified publisher...
	 * @param publisher
	 */
	public void start(String publisher);
	
	/**
	 * Stops publishing...
	 */
	public void stop();
	
}
