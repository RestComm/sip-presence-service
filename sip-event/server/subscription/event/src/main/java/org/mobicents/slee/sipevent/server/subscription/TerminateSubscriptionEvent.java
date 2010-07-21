package org.mobicents.slee.sipevent.server.subscription;

import org.mobicents.sipevent.server.subscription.util.AbstractEvent;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;

/**
 * Event that is fired on a subscription aci, to end the subscription.
 * 
 * @author martins
 * 
 */
public class TerminateSubscriptionEvent extends AbstractEvent {

	private final SubscriptionKey subscriptionKey;
	
	public TerminateSubscriptionEvent(SubscriptionKey subscriptionKey) {
		super();
		this.subscriptionKey = subscriptionKey;
	}
	
	public SubscriptionKey getSubscriptionKey() {
		return subscriptionKey;
	}
	
	private String toString = null;
	@Override
	public String toString() {
		if (toString == null) {
			toString = new StringBuilder("TERMINATE SUBSCRIPTION EVENT [").append(String.valueOf(subscriptionKey))
				.append("]").toString();
		}
		return toString;
	}
}
