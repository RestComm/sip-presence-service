/**
 * 
 */
package org.mobicents.slee.sipevent.examples;

import org.mobicents.slee.enabler.xdmc.XDMClientParent;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParent;

/**
 * @author martins
 *
 */
public interface RLSExampleSubscriber extends SubscriptionClientControlParent,XDMClientParent{

	/**
	 * Creates resource list in xdm with the specified entries, then subscribes its uri
	 */
	public void start(String[] entryURIs);
	
	/**
	 * Unsubscribes resource list and removes it from the xdm
	 */
	public void stop();
	
}
