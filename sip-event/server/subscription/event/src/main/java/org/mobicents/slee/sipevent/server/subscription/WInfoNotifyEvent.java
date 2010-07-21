package org.mobicents.slee.sipevent.server.subscription;

import org.mobicents.sipevent.server.subscription.util.AbstractEvent;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.mobicents.slee.sipevent.server.subscription.winfo.pojo.Watcher;

/**
 * Event that is fired on a subscription aci, so notifications for a subscription are serialized.
 * 
 * @author martins
 * 
 */
public class WInfoNotifyEvent extends AbstractEvent {

	private final Watcher watcher;
	private final SubscriptionKey subscriptionKey;
	private final SubscriptionKey watcherSubscriptionKey;
	
	public WInfoNotifyEvent(SubscriptionKey subscriptionKey, SubscriptionKey watcherSubscriptionKey, Watcher watcher) {
		super();
		this.subscriptionKey = subscriptionKey;
		this.watcherSubscriptionKey = watcherSubscriptionKey;
		this.watcher = watcher;		
	}
	
	public Watcher getWatcher() {
		return watcher;
	}
	
	public SubscriptionKey getWatcherSubscriptionKey() {
		return watcherSubscriptionKey;
	}
	
	public SubscriptionKey getSubscriptionKey() {
		return subscriptionKey;
	}
	
	private String toString = null;
	@Override
	public String toString() {
		if (toString == null) {
			toString = new StringBuilder("WINFO NOTIFY EVENT [ Subscription = ").append(String.valueOf(subscriptionKey)).append(" ]").toString();
		}
		return toString;
	}
}
