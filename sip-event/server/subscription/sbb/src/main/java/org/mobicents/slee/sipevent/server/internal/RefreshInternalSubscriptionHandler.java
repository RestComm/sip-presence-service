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

package org.mobicents.slee.sipevent.server.internal;

import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.facilities.Tracer;

import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionControlSbb;
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

	private static Tracer tracer;
	
	private InternalSubscriptionHandler internalSubscriptionHandler;

	public RefreshInternalSubscriptionHandler(
			InternalSubscriptionHandler sipSubscriptionHandler) {
		this.internalSubscriptionHandler = sipSubscriptionHandler;
		if (tracer == null) {
			tracer = internalSubscriptionHandler.sbb.getSbbContext().getTracer(getClass().getSimpleName());
		}
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
			sbb.getParentSbb().resubscribeError(subscriber, notifier,
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
			sbb.getParentSbb().resubscribeError(subscriber, notifier,
					eventPackage, subscriptionId, Response.INTERVAL_TOO_BRIEF);
			return;
		}

		ActivityContextInterface aci = sbb.getActivityContextNamingfacility()
				.lookup(subscriptionKey.toString());
		if (aci == null) {
			tracer
					.severe("Failed to retrieve aci for internal subscription with key "
							+ subscriptionKey);
			sbb.getParentSbb().resubscribeError(subscriber, notifier,
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
		internalSubscriptionHandler.sbb.getParentSbb().resubscribeOk(
				subscriber, notifier, eventPackage, subscriptionId, expires);

		if (!subscription.isResourceList()) {
			// notify subscriber
			internalSubscriptionHandler.getInternalSubscriberNotificationHandler()
			.notifyInternalSubscriber(subscription, aci,
					childSbb);
		}

		// set new timer
		internalSubscriptionHandler.sbb
				.setSubscriptionTimerAndPersistSubscription(subscription, expires + 1, aci);

		if (tracer.isInfoEnabled()) {
			tracer.info("Refreshed " + subscription + " for " + expires
					+ " seconds");
		}
		
		if (subscription.isResourceList()) {
			// it's a resource list subscription thus pas control to rls
			internalSubscriptionHandler.sbb.getEventListSubscriptionHandler().refreshSubscription(subscription);
		}
	}
}
