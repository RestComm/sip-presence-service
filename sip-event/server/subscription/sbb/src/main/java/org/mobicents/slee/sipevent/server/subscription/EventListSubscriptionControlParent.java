/**
 * 
 */
package org.mobicents.slee.sipevent.server.subscription;

import org.mobicents.slee.sipevent.server.subscription.eventlist.MultiPart;
import org.mobicents.slee.sipevent.server.subscription.pojo.Subscription;
import org.mobicents.slee.sipevent.server.subscription.pojo.SubscriptionKey;

/**
 * @author martins
 *
 */
public interface EventListSubscriptionControlParent {

	/**
	 * @see EventListSubscriberParentSbbLocalObject#notifyEventListSubscriber(SubscriptionKey, Multipart)
	 */
	public void notifyEventListSubscriber(SubscriptionKey key, MultiPart multiPart);
	/**
	 * 
	 * @see EventListSubscriberParentSbbLocalObject#getSubscription(SubscriptionKey)
	 */
	public Subscription getSubscription(SubscriptionKey key);
	
	/**
	 * Warns the parent about an updated RLS Service.
	 * @param uri
	 */
	public void rlsServiceUpdated(String uri);
	
	/**
	 * Warns the parent about a RLS Service that was removed from the XDM
	 * @param uri
	 */
	public void rlsServiceRemoved(String uri);
	
}
