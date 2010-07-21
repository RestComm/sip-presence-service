package org.mobicents.slee.sipevent.server.subscription.sip;

import javax.sip.Dialog;
import javax.sip.ResponseEvent;
import javax.sip.header.EventHeader;
import javax.slee.ActivityContextInterface;

import net.java.slee.resource.sip.DialogActivity;

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
public class RemoveSipSubscriptionHandler {

	private static Logger logger = Logger
			.getLogger(SubscriptionControlSbb.class);

	private SipSubscriptionHandler sipSubscriptionHandler;

	public RemoveSipSubscriptionHandler(
			SipSubscriptionHandler sipSubscriptionHandler) {
		this.sipSubscriptionHandler = sipSubscriptionHandler;
	}

	/**
	 * 
	 * Handles a request to remove an existing SIP subscription
	 * 
	 * @param aci
	 * @param eventPackage
	 * @param eventId
	 * @param subscription
	 * @param entityManager
	 * @param childSbb
	 */
	public void removeSipSubscription(ActivityContextInterface aci,
			Subscription subscription,
			SubscriptionControlDataSource dataSource,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {

		// cancel timer
		sipSubscriptionHandler.sbb.getTimerFacility().cancelTimer(
				subscription.getTimerID());

		if (subscription.getStatus() != Status.terminated && subscription.getStatus() != Status.waiting) {
			// change subscription state
			subscription.setStatus(Subscription.Status.terminated);
			subscription.setLastEvent(null);
		}

		// get dialog from aci
		DialogActivity dialog = (DialogActivity) aci.getActivity();

		// notify winfo subscription(s)
		sipSubscriptionHandler.sbb
		.getWInfoSubscriptionHandler()
		.notifyWinfoSubscriptions(dataSource, subscription, childSbb);
		
		// notify subscriber
		try {
			sipSubscriptionHandler.getSipSubscriberNotificationHandler()
			.createAndSendNotify(dataSource, subscription, dialog,
					childSbb);
		} catch (Exception e) {
			logger.error("failed to notify subscriber", e);
		}
		
		// check resulting subscription state
		if (subscription.getStatus() == Subscription.Status.terminated) {
			if (logger.isInfoEnabled()) {
				logger.info("Status changed for " + subscription);
			}
			// remove subscription data
			sipSubscriptionHandler.sbb.removeSubscriptionData(dataSource,
					subscription, dialog, aci, childSbb);
		} else if (subscription.getStatus() == Subscription.Status.waiting) {
			if (logger.isInfoEnabled()) {
				logger.info("Status changed for " + subscription);
			}
			// keep the subscription for default waiting time so notifier may
			// know about this attemp to subscribe him
			// refresh subscription
			int defaultWaitingExpires = sipSubscriptionHandler.sbb
			.getConfiguration().getDefaultWaitingExpires();
			subscription.refresh(defaultWaitingExpires);
			// set waiting timer
			sipSubscriptionHandler.sbb
			.setSubscriptionTimerAndPersistSubscription(subscription, defaultWaitingExpires + 1, aci);
		}
		
	}

	/**
	 * removes a subscription due to error response on notify
	 * 
	 * @param event
	 */
	public void removeSipSubscriptionOnNotifyError(SubscriptionControlDataSource dataSource,ResponseEvent event) {
		EventHeader eventHeader = (EventHeader) event.getResponse().getHeader(
				EventHeader.NAME);
		Dialog dialog = event.getDialog();
		if (eventHeader != null && dialog != null) {
			Subscription subscription = dataSource.get(new SubscriptionKey(dialog.getDialogId(), eventHeader.getEventType(),
					eventHeader.getEventId()));
			if (subscription != null) {
				if (logger.isInfoEnabled()) {
					logger.info("Removing " + subscription.getKey()
							+ " data due to error on notify response.");
				}
				if (!subscription.getResourceList()) {
					sipSubscriptionHandler.sbb.getEventListSubscriptionHandler().removeSubscription(subscription);
				}
				sipSubscriptionHandler.sbb.removeSubscriptionData(
						dataSource, subscription, dialog,
						sipSubscriptionHandler.sbb
						.getActivityContextNamingfacility().lookup(
								subscription.getKey().toString()),
								sipSubscriptionHandler.sbb
								.getImplementedControlChildSbb());
				
			}
		}
	}
}
