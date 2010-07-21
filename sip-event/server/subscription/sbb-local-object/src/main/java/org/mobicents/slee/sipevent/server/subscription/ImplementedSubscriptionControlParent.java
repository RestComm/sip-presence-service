/**
 * 
 */
package org.mobicents.slee.sipevent.server.subscription;

import javax.sip.ServerTransaction;
import javax.sip.header.ContentTypeHeader;

import org.mobicents.slee.sipevent.server.subscription.data.Notifier;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;

/**
 * @author martins
 *
 */
public interface ImplementedSubscriptionControlParent {

	/**
	 * Used by {@link ImplementedSubscriptionControlSbbLocalObject} to notify
	 * that the authorization of a subscription has changed
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param eventId
	 * @param authorizationCode
	 */
	public void authorizationChanged(String subscriber, Notifier notifier,
			String eventPackage, String eventId, int authorizationCode);

	/**
	 * 
	 * Used by {@link ImplementedSubscriptionControlSbbLocalObject} to provide
	 * the authorization to a new subscription request.
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param key
	 * @param expires
	 * @param responseCode
	 * @param serverTransaction
	 *            if the subscription request was for a sip subscription then
	 *            this param must provide the server transaction provided on the
	 *            authorization request
	 */
	public void newSubscriptionAuthorization(String subscriber,
			String subscriberDisplayName, Notifier notifier, SubscriptionKey key,
			int expires, int responseCode, boolean eventList, ServerTransaction serverTransaction);

	/**
	 * Through this method the subscription control sbb can be informed that the
	 * state of the notifier has changed, allowing subscribers to be notified.
	 */
	public void notifySubscribers(String notifier, String eventPackage,
			Object content, ContentTypeHeader contentTypeHeader);

	/**
	 * Requests notification on a specific subscription, providing the content.
	 * 
	 * @param key
	 * @param content
	 * @param contentTypeHeader
	 */
	public void notifySubscriber(SubscriptionKey key, Object content,
			ContentTypeHeader contentTypeHeader);
}
