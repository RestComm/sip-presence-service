package org.mobicents.slee.sipevent.server.subscription;

import javax.sip.header.ContentTypeHeader;

import org.mobicents.sipevent.server.subscription.util.AbstractEvent;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;

/**
 * Event that is fired on a subscription aci, so notifications for a subscription are serialized.
 * 
 * @author martins
 * 
 */
public class NotifyEvent extends AbstractEvent {

	private final SubscriptionKey subscriptionKey;
	private final Object content;
	private final ContentTypeHeader contentTypeHeader;
	
	public NotifyEvent(SubscriptionKey subscriptionKey, Object content,
			ContentTypeHeader contentTypeHeader) {
		super();
		this.subscriptionKey = subscriptionKey;
		this.content = content;
		this.contentTypeHeader = contentTypeHeader;
	}
	
	public Object getContent() {
		return content;
	}
	
	public ContentTypeHeader getContentTypeHeader() {
		return contentTypeHeader;
	}
	
	public SubscriptionKey getSubscriptionKey() {
		return subscriptionKey;
	}
	
	private String toString = null;
	@Override
	public String toString() {
		if (toString == null) {
			toString = new StringBuilder("NOTIFY EVENT [").append(String.valueOf(subscriptionKey))
				.append("]: Content:\n\n").append(content).toString();
		}
		return toString;
	}
}
