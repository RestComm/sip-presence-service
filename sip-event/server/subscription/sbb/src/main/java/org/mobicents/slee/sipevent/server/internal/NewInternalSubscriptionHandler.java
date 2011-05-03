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
import javax.slee.nullactivity.NullActivity;

import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionControlSbb;
import org.mobicents.slee.sipevent.server.subscription.data.Notifier;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionControlDataSource;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;

/**
 * Handles the creation of a new SIP subscription
 * 
 * @author martins
 * 
 */
public class NewInternalSubscriptionHandler {

	private static Tracer tracer;
	
	private InternalSubscriptionHandler internalSubscriptionHandler;

	public NewInternalSubscriptionHandler(
			InternalSubscriptionHandler internalSubscriptionHandler) {
		this.internalSubscriptionHandler = internalSubscriptionHandler;
		if (tracer == null) {
			tracer = internalSubscriptionHandler.sbb.getSbbContext().getTracer(getClass().getSimpleName());
		}
	}

	public void newInternalSubscription(String subscriber,
			String subscriberDisplayName, Notifier notifier, String eventPackage,
			String subscriptionId, int expires, String content,
			String contentType, String contentSubtype, boolean eventList,
			SubscriptionControlDataSource dataSource,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {

		if (tracer.isFineEnabled()) {
			tracer.fine("newInternalSubscription()");
		}

		SubscriptionControlSbb sbb = internalSubscriptionHandler.sbb;

		// check if expires is not less than the allowed min expires
		if (expires >= sbb.getConfiguration().getMinExpires()) {
			// ensure expires is not bigger than max expires
			if (expires > sbb.getConfiguration().getMaxExpires()) {
				expires = sbb.getConfiguration().getMaxExpires();
			}
		} else {
			// expires is > 0 but < min expires, respond (Interval
			// Too Brief) with Min-Expires = MINEXPIRES
			sbb.getParentSbb().subscribeError(subscriber, notifier.getUri(),
					eventPackage, subscriptionId, Response.INTERVAL_TOO_BRIEF);
			return;
		}

		// create subscription key
		SubscriptionKey key = new SubscriptionKey(
				SubscriptionKey.NO_DIALOG_ID, 
				eventPackage, subscriptionId);
		// find subscription
		Subscription subscription = dataSource.get(key);

		if (subscription != null) {
			// subscription exists
			sbb.getParentSbb().subscribeError(subscriber, notifier.getUri(),
					eventPackage, subscriptionId,
					Response.CONDITIONAL_REQUEST_FAILED);
		} else {
			authorizeNewInternalSubscription(subscriber, subscriberDisplayName, notifier, key, expires, content, contentType, contentSubtype, eventList, dataSource, childSbb);						
		}
	}

	private void authorizeNewInternalSubscription(String subscriber, String subscriberDisplayName, Notifier notifier, SubscriptionKey key, int expires, String content, String contentType, String contentSubtype, boolean eventList, SubscriptionControlDataSource dataSource, ImplementedSubscriptionControlSbbLocalObject childSbb) {
		// ask authorization
		if (key.getEventPackage().endsWith(".winfo")) {
			// winfo package, only accept subscriptions when subscriber and
			// notifier are the same
			newInternalSubscriptionAuthorization(subscriber,
					subscriberDisplayName, notifier, key,
					expires, (subscriber.equals(notifier.getUri()) ? Response.OK
							: Response.FORBIDDEN), eventList, dataSource, childSbb);
		} else {
			if (notifier.isPresList() && subscriber.equals(notifier.getUri())) {
				// self subscribe to a pres list, no need to auth
				newInternalSubscriptionAuthorization(subscriber,
						subscriberDisplayName, notifier, key,
						expires, Response.OK, eventList, dataSource, childSbb);
			}
			else { 
				childSbb.isSubscriberAuthorized(subscriber,
					subscriberDisplayName, notifier, key,
					expires, content, contentType, contentSubtype,eventList,null);
			}
		}
	}
	/**
	 * Used by {@link ImplementedSubscriptionControlSbbLocalObject} to provide
	 * the authorization to a new internal subscription request.
	 * 
	 * @param event
	 * @param subscriber
	 * @param notifier
	 * @param subscriptionKey
	 * @param expires
	 * @param responseCode
	 * @param eventList 
	 * @param entityManager
	 * @param childSbb
	 */
	public void newInternalSubscriptionAuthorization(String subscriber,
			String subscriberDisplayName, Notifier notifier,
			SubscriptionKey subscriptionKey, int expires, int responseCode,
			boolean eventList, SubscriptionControlDataSource dataSource,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {

		if (tracer.isFineEnabled()) {
			tracer.fine("newInternalSubscriptionAuthorization()");
		}

		SubscriptionControlSbb sbb = internalSubscriptionHandler.sbb;
		ActivityContextInterface aci = null;

		// send response
		if (responseCode == Response.ACCEPTED || responseCode == Response.OK) {
			// create null activity, bind a name and attach the sbb
			NullActivity nullActivity = sbb.getNullActivityFactory()
					.createNullActivity();
			try {
				aci = sbb.getNullACIFactory().getActivityContextInterface(
						nullActivity);
				sbb.getActivityContextNamingfacility().bind(aci,
						subscriptionKey.toString());
			} catch (Exception e) {
				tracer.severe("Failed to create internal subscription aci", e);
				sbb.getParentSbb().subscribeError(subscriber, notifier.getUri(),
						subscriptionKey.getEventPackage(),
						subscriptionKey.getEventId(),
						Response.SERVER_INTERNAL_ERROR);
				return;
			}
			aci.attach(sbb.getSbbContext().getSbbLocalObject());
			// inform parent
			sbb.getParentSbb().subscribeOk(subscriber, notifier.getUri(),
					subscriptionKey.getEventPackage(),
					subscriptionKey.getEventId(), expires, responseCode);
		} else {
			sbb.getParentSbb().subscribeError(subscriber, notifier.getUri(),
					subscriptionKey.getEventPackage(),
					subscriptionKey.getEventId(), responseCode);
			if (tracer.isInfoEnabled()) {
				tracer.info("Subscription: subscriber=" + subscriber
						+ ",notifier=" + notifier + ",eventPackage="
						+ subscriptionKey.getEventPackage()
						+ " not authorized (" + responseCode + ")");
			}
			return;
		}

		// create subscription, initial status depends on authorization
		Subscription.Status initialStatus = responseCode == Response.ACCEPTED ? Subscription.Status.pending
				: Subscription.Status.active;
		Subscription subscription = new Subscription(subscriptionKey,
				subscriber, notifier, initialStatus, subscriberDisplayName,
				expires, eventList,dataSource);

		if (!eventList || (responseCode == Response.ACCEPTED)) {
			// notify subscriber
			internalSubscriptionHandler.getInternalSubscriberNotificationHandler()
			.notifyInternalSubscriber(subscription, aci,
					childSbb);
		}
		
		// notify winfo subscribers
		sbb.getWInfoSubscriptionHandler().notifyWinfoSubscriptions(
				dataSource, subscription, childSbb);

		// set new timer
		sbb.setSubscriptionTimerAndPersistSubscription(subscription, expires + 1, aci);

		if (eventList && (responseCode == Response.OK)) {
			// resource list and active subscription, ask the event list control child to create the subscription 
			if (!internalSubscriptionHandler.sbb.getEventListSubscriptionHandler().createSubscription(subscription)) {
				internalSubscriptionHandler.getRemoveInternalSubscriptionHandler().removeInternalSubscription(aci, subscription, dataSource, childSbb);
			}
		}
		
		if (tracer.isInfoEnabled()) {
			tracer.info("Created " + subscription);
		}
	}
}