/**
 * 
 */
package org.mobicents.slee.sippresence.client;

import org.mobicents.slee.sipevent.server.subscription.data.Subscription;

/**
 * @author martins
 * 
 */
public interface PresenceClientControlParent {

	/**
	 * Ok Response about a new publication request.
	 * 
	 * @param requestId
	 * @param eTag
	 * @param expires
	 */
	public void newPublicationOk(Object requestId, String eTag, int expires);

	/**
	 * Ok Response about a refresh publication request.
	 * 
	 * @param requestId
	 * @param eTag
	 * @param expires
	 */
	public void refreshPublicationOk(Object requestId, String eTag, int expires);

	/**
	 * Ok Response about a modify publication request.
	 * 
	 * @param requestId
	 * @param eTag
	 * @param expires
	 */
	public void modifyPublicationOk(Object requestId, String eTag, int expires);

	/**
	 * Ok Response about a remove publication request.
	 * 
	 * @param requestId
	 */
	public void removePublicationOk(Object requestId);

	/**
	 * Error Response about a new publication request.
	 * 
	 * @param requestId
	 * @param error
	 *            sip matching error status code
	 */
	public void newPublicationError(Object requestId, int error);

	/**
	 * Error about a refresh publication request.
	 * 
	 * @param requestId
	 * @param error
	 *            sip matching error status code
	 */
	public void refreshPublicationError(Object requestId, int error);

	/**
	 * Error about a modify publication request.
	 * 
	 * @param requestId
	 * @param error
	 *            sip matching error status code
	 */
	public void modifyPublicationError(Object requestId, int error);

	/**
	 * Error about a remove publication request.
	 * 
	 * @param requestId
	 * @param error
	 *            sip matching error status code
	 */
	public void removePublicationError(Object requestId, int error);

	/**
	 * informs the parent sbb that a new subscription request was successful
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param expires
	 * @param responseCode
	 *            OK or CREATED
	 */
	public void newSubscriptionOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires,
			int responseCode);

	/**
	 * informs the parent sbb that a new subscription request was not successful
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param error
	 *            the sip error response status code
	 */
	public void newSubscriptionError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error);

	/**
	 * informs the parent sbb that a refresh subscription request was successful
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param expires
	 */
	public void refreshSubscriptionOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires);

	/**
	 * informs the parent sbb that a refresh subscription request was not
	 * successful
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param error
	 *            the sip error response status code
	 */
	public void refreshSubscriptionError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error);

	/**
	 * informs the parent sbb that a remove subscription request was successful
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 */
	public void removeSubscriptionOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId);

	/**
	 * informs the parent sbb that a remove subscription request was not
	 * successful
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param error
	 *            the sip error response status code
	 */
	public void removeSubscriptionError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error);

	/**
	 * Notifies the client.
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param status
	 *            the subscription status
	 * @param terminationReason
	 *            if the subscription was unexpectedly terminated the event that
	 *            caused it will be provided
	 * @param document
	 * @param contentType
	 * @param contentSubtype
	 */
	public void notifyEvent(String subscriber, String notifier,
			String eventPackage, String subscriptionId,
			Subscription.Event terminationReason, Subscription.Status status,
			String content, String contentType, String contentSubtype);

}
