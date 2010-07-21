package org.mobicents.slee.sippresence.server.subscription.rules;

import java.io.Serializable;

import org.mobicents.slee.sipevent.server.subscription.data.Notifier;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;

public class PresRuleCMPKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1212963002333839692L;
	
	private final String subscriber;
	private final Notifier notifier;
	private final SubscriptionKey subscriptionKey;
	
	public PresRuleCMPKey(String subscriber, Notifier notifier, SubscriptionKey subscriptionKey) {
		this.subscriber = subscriber;
		this.notifier = notifier;
		this.subscriptionKey = subscriptionKey;
	}
	
	public Notifier getNotifier() {
		return notifier;
	}
	
	public String getSubscriber() {
		return subscriber;
	}
	
	public SubscriptionKey getSubscriptionKey() {
		return subscriptionKey;
	}
	
	
	
	@Override
	public int hashCode() {
		return subscriptionKey.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj.getClass() == this.getClass()) {
			PresRuleCMPKey other = (PresRuleCMPKey)obj;
			return this.subscriptionKey.equals(other.subscriptionKey);
		}
		else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "PresRuleCMPKey{ subscriber = "+subscriber+" , subscriptionKey = "+subscriptionKey+" }";
	}
}
