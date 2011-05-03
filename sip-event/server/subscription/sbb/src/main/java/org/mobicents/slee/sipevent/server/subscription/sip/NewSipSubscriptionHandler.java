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

import gov.nist.javax.sip.message.SIPRequest;

import javax.sip.Dialog;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.address.Address;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.SbbLocalObject;
import javax.slee.facilities.Tracer;

import net.java.slee.resource.sip.DialogActivity;

import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
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
public class NewSipSubscriptionHandler {

	private static Tracer tracer;

	private SipSubscriptionHandler sipSubscriptionHandler;

	public NewSipSubscriptionHandler(
			SipSubscriptionHandler sipSubscriptionHandler) {
		this.sipSubscriptionHandler = sipSubscriptionHandler;
		if (tracer == null) {
			tracer = sipSubscriptionHandler.sbb.getSbbContext().getTracer(getClass().getSimpleName());
		}
	}

	/**
	 * Starts the process of handling a new sip subscription
	 * 
	 * @param event
	 * @param aci
	 * @param eventPackage
	 * @param eventId
	 * @param expires
	 * @param entityManager
	 * @param childSbb
	 */
	public void newSipSubscription(RequestEvent event,
			ActivityContextInterface aci, String eventPackage, String eventId,
			int expires, SubscriptionControlDataSource dataSource,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {

		// get subscription data from request
		Address fromAddress = ((FromHeader) event.getRequest().getHeader(
				FromHeader.NAME)).getAddress();
		String subscriber = fromAddress.getURI().toString();
		String subscriberDisplayName = fromAddress.getDisplayName();
		
		Notifier notifier = new Notifier(event.getRequest().getRequestURI().toString());
				
		// get content
		String content = null;
		String contentType = null;
		String contentSubtype = null;
		ContentTypeHeader contentTypeHeader = (ContentTypeHeader) event
				.getRequest().getHeader(ContentTypeHeader.NAME);
		if (contentTypeHeader != null) {
			contentType = contentTypeHeader.getContentType();
			contentSubtype = contentTypeHeader.getContentSubType();
			content = new String(event.getRequest().getRawContent());
		}
		
		// create dialog if does not exists
		Dialog dialog = event.getDialog();
		if (dialog == null) {
			try {
				dialog = sipSubscriptionHandler.sbb.getSipProvider()
						.getNewDialog(event.getServerTransaction());				
			} catch (Exception e) {
				tracer.severe("Can't create dialog", e);
				// cleanup
				try {
					Response response = sipSubscriptionHandler.sbb
							.getMessageFactory().createResponse(
									Response.SERVER_INTERNAL_ERROR,
									event.getRequest());
					response = sipSubscriptionHandler
							.addContactHeader(response);
					event.getServerTransaction().sendResponse(response);					
				} catch (Exception f) {
					tracer.severe("Can't send RESPONSE", f);
				}
				return;
			}
		}

		// if dialog id is null (cause it's a new dialog and no response was sent yet) then build it manually 
		String dialogId = dialog.getDialogId() != null ? dialog.getDialogId() : ((SIPRequest) event.getRequest()).getDialogId(
				true, dialog.getLocalTag());
		SubscriptionKey key = new SubscriptionKey(dialogId, eventPackage, eventId);
		
		if (sipSubscriptionHandler.sbb.getConfiguration().getEventListSupportOn()) {
			// we need to find out if the notifier is a resource list
			int rlsResponse = sipSubscriptionHandler.sbb.getEventListSubscriptionHandler().validateSubscribeRequest(subscriber, notifier,eventPackage,event);

			switch (rlsResponse) {
			case Response.NOT_FOUND:
				// the notifier is not a resource list, proceed with normal authorization means
				authorizeNewSipSubscription(event, aci, subscriber, subscriberDisplayName, notifier, key, expires, content, contentType, contentSubtype, false, dataSource, childSbb);
				break;
			case Response.OK:
				// the notifier is a resource list
				authorizeNewSipSubscription(event, aci, subscriber, subscriberDisplayName, notifier, key, expires, content, contentType, contentSubtype, true, dataSource, childSbb);
				break;			
			default:
				// the rls request validation returned an error
				try {
					Response response = sipSubscriptionHandler.sbb.getMessageFactory().createResponse(rlsResponse, event.getRequest());
					response = sipSubscriptionHandler
					.addContactHeader(response);
					response.addHeader(sipSubscriptionHandler.sbb.getHeaderFactory().createRequireHeader("eventlist"));
					event.getServerTransaction().sendResponse(response);					
				} catch (Exception f) {
					tracer.severe("Can't send RESPONSE", f);
				}
				return;			
			}
		}
		else {
			authorizeNewSipSubscription(event, aci, subscriber, subscriberDisplayName, notifier, key, expires, content, contentType, contentSubtype, false, dataSource, childSbb);
		}
	}

	/**
	 * Requests authorization for the new sip subscription.
	 * 
	 * @param event
	 * @param aci
	 * @param subscriber
	 * @param subscriberDisplayName
	 * @param notifier
	 * @param key
	 * @param expires
	 * @param content
	 * @param contentType
	 * @param contentSubtype
	 * @param entityManager
	 * @param childSbb
	 */
	public void authorizeNewSipSubscription(RequestEvent event,
			ActivityContextInterface aci, String subscriber, String subscriberDisplayName, Notifier notifier, SubscriptionKey key, int expires, String content,
			String contentType, String contentSubtype, boolean eventList,SubscriptionControlDataSource dataSource,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {
		
		// ask authorization
		if (key.getEventPackage().endsWith(".winfo")) {
			// winfo package, only accept subscriptions when subscriber and
			// notifier are the same
			newSipSubscriptionAuthorization(event.getServerTransaction(), aci,
					subscriber, subscriberDisplayName, notifier, key,
					expires, (subscriber.equals(notifier.getUri()) ? Response.OK
							: Response.FORBIDDEN), eventList, dataSource,childSbb);
		} else {
			if (notifier.isPresList() && subscriber.equals(notifier.getUri())) {
				// no need to auth a subscription to subscriber's own pres list
				newSipSubscriptionAuthorization(event.getServerTransaction(), aci,
						subscriber, subscriberDisplayName, notifier, key,
						expires, Response.OK, eventList, dataSource,childSbb);
			}
			else {
				childSbb.isSubscriberAuthorized(subscriber, subscriberDisplayName, notifier, key, expires, content,
					contentType, contentSubtype, eventList, event.getServerTransaction());
			}
		}
	}
	
	/**
	 * Used by {@link ImplementedSubscriptionControlSbbLocalObject} to provide
	 * the authorization to a new sip subscription request.
	 * 
	 * @param event
	 * @param subscriber
	 * @param notifier
	 * @param subscriptionKey
	 * @param expires
	 * @param responseCode
	 * @param entityManager
	 * @param childSbb
	 */
	public void newSipSubscriptionAuthorization(
			ServerTransaction serverTransaction,
			ActivityContextInterface serverTransactionACI, String subscriber,
			String subscriberDisplayName, Notifier notifier, SubscriptionKey key,
			int expires, int responseCode, boolean eventList,SubscriptionControlDataSource dataSource,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {

		DialogActivity dialog = (DialogActivity) serverTransaction.getDialog();
		ActivityContextInterface dialogAci = null;

		// send response
		try {
			Response response = sipSubscriptionHandler.sbb.getMessageFactory()
					.createResponse(responseCode,
							serverTransaction.getRequest());
			if (responseCode == Response.ACCEPTED
					|| responseCode == Response.OK) {
				// attach to dialog
				SbbLocalObject sbbLocalObject = sipSubscriptionHandler.sbb
						.getSbbContext().getSbbLocalObject();
				dialogAci = sipSubscriptionHandler.sbb
						.getSipActivityContextInterfaceFactory()
						.getActivityContextInterface(dialog);
				dialogAci.attach(sbbLocalObject);
				if (serverTransactionACI != null) {
					serverTransactionACI.detach(sbbLocalObject);
				}
				// finish and send response
				response = sipSubscriptionHandler.addContactHeader(response);
				response.addHeader(sipSubscriptionHandler.sbb
						.getHeaderFactory().createExpiresHeader(expires));
				serverTransaction.sendResponse(response);				
			} else {
				response = sipSubscriptionHandler.addContactHeader(response);
				serverTransaction.sendResponse(response);
				if (tracer.isInfoEnabled()) {
					tracer.info("Subscription: subscriber=" + subscriber
							+ ",notifier=" + notifier + ",eventPackage="
							+ key.getEventPackage() + " not authorized ("
							+ responseCode + ")");
				}
				return;
			}
		} catch (Exception e) {
			tracer.severe("Can't send new subscription request's reponse", e);
			// cleanup
			try {
				Response response = sipSubscriptionHandler.sbb
						.getMessageFactory().createResponse(
								Response.SERVER_INTERNAL_ERROR,
								serverTransaction.getRequest());
				response = sipSubscriptionHandler.addContactHeader(response);
				serverTransaction.sendResponse(response);				
			} catch (Exception f) {
				tracer.severe("Can't send RESPONSE", f);
			}
			return;
		}

		// create subscription, initial status depends on authorization
		Subscription.Status initialStatus = responseCode == Response.ACCEPTED ? Subscription.Status.pending
				: Subscription.Status.active;
		Subscription subscription = new Subscription(key, subscriber, notifier,
				initialStatus, subscriberDisplayName, expires, eventList,dataSource);

		if (!eventList || (responseCode == Response.ACCEPTED)) {
			// single resource or pending subscription (no notify content), notify subscriber
			try {
				sipSubscriptionHandler.getSipSubscriberNotificationHandler()
				.createAndSendNotify(dataSource, subscription, dialog,
						childSbb);
			} catch (Exception e) {
				tracer.severe("failed to notify subscriber", e);
			}
		}
		
		// notify winfo subscribers
		sipSubscriptionHandler.sbb
				.getWInfoSubscriptionHandler()
				.notifyWinfoSubscriptions(dataSource, subscription, childSbb);

		// bind name for dialog aci
		try {
			sipSubscriptionHandler.sbb.getActivityContextNamingfacility().bind(
					dialogAci, key.toString());
		} catch (Exception e) {
			tracer.severe("failed to bind a name to dialog's aci", e);
		}

		// set new timer
		sipSubscriptionHandler.sbb.setSubscriptionTimerAndPersistSubscription(subscription, expires + 1, dialogAci);

		if (eventList && (responseCode == Response.OK))  {
			// it's a resource list and active subscription, delegate to the event list control for further process of the new subscription
			if (!sipSubscriptionHandler.sbb.getEventListSubscriptionHandler().createSubscription(subscription)) {
				// sip subscription
				sipSubscriptionHandler
				.getRemoveSipSubscriptionHandler()
				.removeSipSubscription(dialogAci, subscription, dataSource,childSbb);
			}
		}
		
		if (tracer.isInfoEnabled()) {
			tracer.info("Created " + subscription);
		}
	}
}