/**
 * 
 */
package org.mobicents.slee.sipevent.server.subscription;

import javax.slee.ActivityContextInterface;

import org.mobicents.slee.sipevent.server.rlscache.RLSService;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;

/**
 * @author martins
 *
 */
public interface EventListSubscriber extends SubscriptionClientControlParent {

	public void setParentSbb(EventListSubscriberParentSbbLocalObject parentSbb);
	
	public void subscribe(Subscription subscription, RLSService rlsService, ActivityContextInterface rlsServiceAci);
	
	public void resubscribe(Subscription subscription, RLSService rlsService);
	
	public void unsubscribe(Subscription subscription, RLSService rlsService);
	
	public SubscriptionKey getSubscriptionKey();
}
