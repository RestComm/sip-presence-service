/**
 * 
 */
package org.mobicents.slee.sipevent.server.subscription;

import javax.slee.ActivityContextInterface;

import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.mobicents.slee.sipevent.server.subscription.eventlist.FlatList;

/**
 * @author martins
 *
 */
public interface EventListSubscriber extends SubscriptionClientControlParent {

	public void setParentSbb(EventListSubscriberParentSbbLocalObject parentSbb);
	
	public void subscribe(Subscription subscription, FlatList flatList, ActivityContextInterface flatListACI);
	
	public void resubscribe(Subscription subscription, FlatList flatList);
	
	public void unsubscribe(Subscription subscription, FlatList flatList);
	
	public SubscriptionKey getSubscriptionKey();
}
