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

import javax.sip.RequestEvent;
import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.facilities.Tracer;

import net.java.slee.resource.sip.DialogActivity;

import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionControlDataSource;

/**
 * Handles the refresh of a SIP subscription
 * 
 * @author martins
 * 
 */
public class RefreshSipSubscriptionHandler {

	private static Tracer tracer;

	private SipSubscriptionHandler sipSubscriptionHandler;

	public RefreshSipSubscriptionHandler(
			SipSubscriptionHandler sipSubscriptionHandler) {
		this.sipSubscriptionHandler = sipSubscriptionHandler;
		if (tracer == null) {
			tracer = sipSubscriptionHandler.sbb.getSbbContext().getTracer(getClass().getSimpleName());
		}
	}

	/**
	 * Refreshes an existing SIP Subscription.
	 * 
	 * @param event
	 * @param aci
	 * @param expires
	 * @param subscription
	 * @param entityManager
	 * @param sbb
	 * @param childSbb
	 */
	public void refreshSipSubscription(RequestEvent event,
			ActivityContextInterface aci, int expires,
			Subscription subscription, SubscriptionControlDataSource dataSource,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {

		// cancel actual timer
		sipSubscriptionHandler.sbb.getTimerFacility().cancelTimer(
				subscription.getTimerID());

		// refresh subscription
		subscription.refresh(expires);

		// send OK response
		try {
			Response response = sipSubscriptionHandler.sbb.getMessageFactory()
					.createResponse(Response.OK, event.getRequest());
			response = sipSubscriptionHandler.addContactHeader(response);
			response.addHeader(sipSubscriptionHandler.sbb.getHeaderFactory()
					.createExpiresHeader(expires));
			event.getServerTransaction().sendResponse(response);			
		} catch (Exception e) {
			tracer.severe("Can't send RESPONSE", e);
		}

		if (!subscription.isResourceList()) {
			// notify subscriber
			try {
				sipSubscriptionHandler.getSipSubscriberNotificationHandler()
				.createAndSendNotify(dataSource, subscription,
						(DialogActivity) aci.getActivity(), childSbb);
			} catch (Exception e) {
				tracer.severe("failed to notify subscriber", e);
			}
		}

		// set new timer
		sipSubscriptionHandler.sbb.setSubscriptionTimerAndPersistSubscription(subscription, expires + 1, aci);

		if (tracer.isInfoEnabled()) {
			tracer.info("Refreshed " + subscription + " for " + expires
					+ " seconds");
		}
		
		if (subscription.isResourceList()) {
			// it's a resource list subscription thus pas control to rls
			sipSubscriptionHandler.sbb.getEventListSubscriptionHandler().refreshSubscription(subscription);
		}
	}

}
