package org.mobicents.slee.sipevent.server.internal;

import org.mobicents.sipevent.server.subscription.util.AbstractEvent;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;

/**
 * Internal event that is fired on a internal subscription aci, so we can use
 * the right callback to notify.
 * 
 * @author martins
 * 
 */
public class InternalNotifyEvent extends AbstractEvent {

	private final String subscriber;
	private final String notifier;
	private final String eventPackage;
	private final String subscriptionId;

	private final Subscription.Event terminationReason;
	private final Subscription.Status subscriptionStatus;

	private final String content;
	private final String contentType;
	private final String contentSubtype;

	public InternalNotifyEvent(String subscriber, String notifier,
			String eventPackage, String subscriptionId,
			Subscription.Event terminationReason,
			Subscription.Status subscriptionStatus, String content,
			String contentType, String contentSubtype) {
		super();
		this.subscriber = subscriber;
		this.notifier = notifier;
		this.eventPackage = eventPackage;
		this.subscriptionId = subscriptionId;
		this.terminationReason = terminationReason;
		this.subscriptionStatus = subscriptionStatus;
		this.content = content;
		this.contentType = contentType;
		this.contentSubtype = contentSubtype;
	}

	public String getContentSubtype() {
		return contentSubtype;
	}

	public String getContentType() {
		return contentType;
	}

	public String getContent() {
		return content;
	}

	public String getEventPackage() {
		return eventPackage;
	}

	public String getNotifier() {
		return notifier;
	}

	public String getSubscriber() {
		return subscriber;
	}

	public Subscription.Event getTerminationReason() {
		return terminationReason;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public Subscription.Status getSubscriptionStatus() {
		return subscriptionStatus;
	}

	private String toString = null;
	@Override
	public String toString() {
		if (toString == null) {
			toString = "\nINTERNAL NOTIFY EVENT:" +
			"\n+-- Subscriber: " + subscriber +
			"\n+-- Notifier: " + notifier +
			"\n+-- EventPackage: " + eventPackage +
			"\n+-- SubscriptionId: " + subscriptionId +
			"\n+-- Subscription status: " + subscriptionStatus +
			"\n+-- Subscription termination reason: " + terminationReason +
			"\n+-- Content Type: " + contentType + '/' + contentSubtype +
			"\n+-- Content:\n\n" + content;
		}
		return toString;
	}
}
