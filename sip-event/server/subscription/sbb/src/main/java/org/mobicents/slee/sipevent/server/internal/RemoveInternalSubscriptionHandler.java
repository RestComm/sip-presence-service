package org.mobicents.slee.sipevent.server.internal;

import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionControlSbb;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionControlDataSource;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Status;

/**
 * Handles the removal of a SIP subscription
 * 
 * @author martins
 * 
 */
public class RemoveInternalSubscriptionHandler {

	private static Logger logger = Logger
			.getLogger(SubscriptionControlSbb.class);

	private InternalSubscriptionHandler internalSubscriptionHandler;

	public RemoveInternalSubscriptionHandler(
			InternalSubscriptionHandler sipSubscriptionHandler) {
		this.internalSubscriptionHandler = sipSubscriptionHandler;
	}

	public void removeInternalSubscription(String subscriber, String notifier,
			String eventPackage, String subscriptionId,
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
			sbb.getParentSbbCMP().unsubscribeError(subscriber, notifier,
					eventPackage, subscriptionId,
					Response.CONDITIONAL_REQUEST_FAILED);
			return;
		}

		ActivityContextInterface aci = sbb.getActivityContextNamingfacility()
				.lookup(subscriptionKey.toString());
		if (aci == null) {
			logger
					.error("Failed to retrieve aci for internal subscription with key "
							+ subscriptionKey);
			sbb.getParentSbbCMP().unsubscribeError(subscriber, notifier,
					eventPackage, subscriptionId,
					Response.SERVER_INTERNAL_ERROR);
			return;
		}

		// send OK response
		sbb.getParentSbbCMP().unsubscribeOk(subscriber, notifier, eventPackage,
				subscriptionId);

		if (subscription.getResourceList()) {
			internalSubscriptionHandler.sbb.getEventListSubscriptionHandler().removeSubscription(subscription);
		}
		
		removeInternalSubscription(aci, subscription, dataSource, childSbb);
		
	}

	public void removeInternalSubscription(ActivityContextInterface aci,
			Subscription subscription, SubscriptionControlDataSource dataSource,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {

		// cancel timer
		internalSubscriptionHandler.sbb.getTimerFacility().cancelTimer(
				subscription.getTimerID());

		if (subscription.getStatus() != Status.terminated && subscription.getStatus() != Status.waiting) {
			// change subscription state
			subscription.setStatus(Subscription.Status.terminated);
			subscription.setLastEvent(null);
		}

		// check resulting subscription state
		if (subscription.getStatus() == Subscription.Status.terminated) {
			if (logger.isInfoEnabled()) {
				logger.info("Status changed for " + subscription);
			}
			// remove subscription data
			internalSubscriptionHandler.sbb.removeSubscriptionData(
					dataSource, subscription, null, aci, childSbb);
		} else if (subscription.getStatus() == Subscription.Status.waiting) {
			if (logger.isInfoEnabled()) {
				logger.info("Status changed for " + subscription);
			}
			// keep the subscription for default waiting time so notifier may
			// know about this attemp to subscribe him
			// refresh subscription
			int defaultWaitingExpires = internalSubscriptionHandler.sbb
					.getConfiguration().getDefaultWaitingExpires();
			subscription.refresh(defaultWaitingExpires);
			// set waiting timer
			internalSubscriptionHandler.sbb
					.setSubscriptionTimerAndPersistSubscription(subscription, defaultWaitingExpires + 1, aci);
		}

		// notify winfo subscription(s)
		internalSubscriptionHandler.sbb
				.getWInfoSubscriptionHandler()
				.notifyWinfoSubscriptions(dataSource, subscription, childSbb);

		// notify subscriber
		internalSubscriptionHandler.getInternalSubscriberNotificationHandler()
		.notifyInternalSubscriber( subscription, aci,
				childSbb);

	}

}
