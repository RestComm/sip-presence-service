/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.slee.sipevent.server.subscription.sip;

import javax.sip.Dialog;
import javax.sip.ResponseEvent;
import javax.sip.header.EventHeader;
import javax.slee.ActivityContextInterface;
import javax.slee.facilities.Tracer;

import net.java.slee.resource.sip.DialogActivity;

import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Status;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionControlDataSource;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;

/**
 * Handles the removal of a SIP subscription
 * 
 * @author martins
 * 
 */
public class RemoveSipSubscriptionHandler {

	private static Tracer tracer;

	private SipSubscriptionHandler sipSubscriptionHandler;

	public RemoveSipSubscriptionHandler(
			SipSubscriptionHandler sipSubscriptionHandler) {
		this.sipSubscriptionHandler = sipSubscriptionHandler;
		if (tracer == null) {
			tracer = sipSubscriptionHandler.sbb.getSbbContext().getTracer(getClass().getSimpleName());
		}
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
			tracer.severe("failed to notify subscriber", e);
		}
		
		// check resulting subscription state
		if (subscription.getStatus() == Subscription.Status.terminated) {
			if (tracer.isInfoEnabled()) {
				tracer.info("Status changed for " + subscription);
			}
			// remove subscription data
			sipSubscriptionHandler.sbb.removeSubscriptionData(dataSource,
					subscription, dialog, aci, childSbb);
		} else if (subscription.getStatus() == Subscription.Status.waiting) {
			if (tracer.isInfoEnabled()) {
				tracer.info("Status changed for " + subscription);
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
				if (tracer.isInfoEnabled()) {
					tracer.info("Removing " + subscription.getKey()
							+ " data due to error on notify response.");
				}
				if (!subscription.isResourceList()) {
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
