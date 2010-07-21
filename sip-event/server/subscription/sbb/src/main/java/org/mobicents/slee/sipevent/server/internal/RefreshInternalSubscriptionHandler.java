package org.mobicents.slee.sipevent.server.internal;

import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionControlSbb;
import org.mobicents.slee.sipevent.server.subscription.data.Notifier;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionControlDataSource;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;

/**
 * Handles the refresh of an internal subscription
 * 
 * @author martins
 * 
 */
public class RefreshInternalSubscriptionHandler {

	private static Logger logger = Logger
			.getLogger(SubscriptionControlSbb.class);

	private InternalSubscriptionHandler internalSubscriptionHandler;

	public RefreshInternalSubscriptionHandler(
			InternalSubscriptionHandler sipSubscriptionHandler) {
		this.internalSubscriptionHandler = sipSubscriptionHandler;
	}

	/**
	 * Refreshes an internal subscription
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param expires
	 * @param entityManager
	 * @param childSbb
	 */
	public void refreshInternalSubscription(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires,
			SubscriptionControlDataSource dataSource,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {

		SubscriptionControlSbb sbb = internalSubscriptionHandler.sbb;

		// create subscription key
		SubscriptionKey subscriptionKey = new SubscriptionKey(
				SubscriptionKey.NO_DIALOG_ID,
				eventPackage, subscriptionId);
		// find subscription
		Subscription subscription = dataSource.get(subscriptionKey);

		if (subscription == null) {
			// subscription does not exists
			sbb.getParentSbbCMP().resubscribeError(subscriber, notifier,
					eventPackage, subscriptionId,
					Response.CONDITIONAL_REQUEST_FAILED);
			return;
		}

		// check if expires is not less than the allowed min expires
		if (expires >= sbb.getConfiguration().getMinExpires()) {
			// ensure expires is not bigger than max expires
			if (expires > sbb.getConfiguration().getMaxExpires()) {
				expires = sbb.getConfiguration().getMaxExpires();
			}
		} else {
			// expires is > 0 but < min expires, respond (Interval
			// Too Brief) with Min-Expires = MINEXPIRES
			sbb.getParentSbbCMP().resubscribeError(subscriber, notifier,
					eventPackage, subscriptionId, Response.INTERVAL_TOO_BRIEF);
			return;
		}

		ActivityContextInterface aci = sbb.getActivityContextNamingfacility()
				.lookup(subscriptionKey.toString());
		if (aci == null) {
			logger
					.error("Failed to retrieve aci for internal subscription with key "
							+ subscriptionKey);
			sbb.getParentSbbCMP().resubscribeError(subscriber, notifier,
					eventPackage, subscriptionId,
					Response.SERVER_INTERNAL_ERROR);
			return;
		}

		// cancel actual timer
		internalSubscriptionHandler.sbb.getTimerFacility().cancelTimer(
				subscription.getTimerID());

		// refresh subscription
		subscription.refresh(expires);

		// send OK response
		internalSubscriptionHandler.sbb.getParentSbbCMP().resubscribeOk(
				subscriber, notifier, eventPackage, subscriptionId, expires);

		if (!subscription.getResourceList()) {
			// notify subscriber
			internalSubscriptionHandler.getInternalSubscriberNotificationHandler()
			.notifyInternalSubscriber(subscription, aci,
					childSbb);
		}

		// set new timer
		internalSubscriptionHandler.sbb
				.setSubscriptionTimerAndPersistSubscription(subscription, expires + 1, aci);

		if (logger.isInfoEnabled()) {
			logger.info("Refreshed " + subscription + " for " + expires
					+ " seconds");
		}
		
		if (subscription.getResourceList()) {
			// it's a resource list subscription thus pas control to rls
			internalSubscriptionHandler.sbb.getEventListSubscriptionHandler().refreshSubscription(subscription);
		}
	}
}
