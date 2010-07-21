/**
 * 
 */
package org.mobicents.slee.sipevent.server.subscription;

import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.mobicents.slee.sipevent.server.subscription.eventlist.MultiPart;

/**
 * @author martins
 *
 */
public interface EventListSubscriberParent {

	/**
	 * Requests notification on a specific subscription, providing the multipart content.
	 * @param key
	 * @param multipart
	 */
	public void notifyEventListSubscriber(SubscriptionKey key, MultiPart multipart);
	
	/**
	 * Requests the subscription for the specified key. The subscription's entity manager must be available.
	 * @param key
	 * @return
	 */
	public Subscription getSubscription(SubscriptionKey key);

}
