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

import gov.nist.javax.sip.Utils;
import gov.nist.javax.sip.header.HeaderFactoryExt;
import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.ims.PChargingVectorHeader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Map.Entry;

import javax.sip.Dialog;
import javax.sip.SipException;
import javax.sip.TransactionDoesNotExistException;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.message.Request;
import javax.slee.ActivityContextInterface;
import javax.slee.facilities.Tracer;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import net.java.slee.resource.sip.DialogActivity;

import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.NotifyContent;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionControlDataSource;
import org.openxdm.xcap.common.xml.TextWriter;
import org.w3c.dom.Node;

/**
 * Handles the notification of a SIP subscriber
 * 
 * @author martins
 * 
 */
public class SipSubscriberNotificationHandler {

	private static Tracer tracer;

	private SipSubscriptionHandler sipSubscriptionHandler;

	public SipSubscriberNotificationHandler(
			SipSubscriptionHandler sipSubscriptionHandler) {
		this.sipSubscriptionHandler = sipSubscriptionHandler;
		if (tracer == null) {
			tracer = sipSubscriptionHandler.sbb.getSbbContext().getTracer(getClass().getSimpleName());
		}
	}

	public void notifySipSubscriber(NotifyContent notifyContent, Subscription subscription,
			ActivityContextInterface dialogACI,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {

		try {
			DialogActivity dialog = (DialogActivity) dialogACI.getActivity();
			// create notify
			Request notify = createNotify(dialog, subscription, notifyContent.getEventHeaderParams());
			// add content
			if (notifyContent.getContent() != null) {
				notify = setNotifyContent(subscription, notify, notifyContent.getContent(),
						notifyContent.getContentTypeHeader(), childSbb);
				
			}
			// ....aayush added code here (with ref issue #567)
			notify.addHeader(addPChargingVectorHeader());
			// send notify in dialog related with subscription
			dialog.sendRequest(notify);							
		} catch (Exception e) {
			tracer.severe("failed to notify subscriber", e);
		}
	}

	public Request setNotifyContent(Subscription subscription, Request notify,
			Object content, ContentTypeHeader contentTypeHeader,
			ImplementedSubscriptionControlSbbLocalObject childSbb)
			throws JAXBException, ParseException, IOException {

		if (!subscription.getResourceList() && !subscription.getKey().isWInfoSubscription()) {
			// filter content per subscriber (notifier rules)
			Object filteredContent = childSbb.filterContentPerSubscriber(subscription,content);
			// filter content per notifier (subscriber rules)
			// TODO
			// marshall content and add to sip message
			if (content instanceof Node) {
				// dom
				try {
					notify.setContent(TextWriter.toString((Node)content), contentTypeHeader);
				} catch (TransformerException e) {
					throw new IOException("failed to marshall DOM content",e);
				}
			}
			else {
				// jaxb
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				childSbb.getMarshaller().marshal(filteredContent, baos);
				notify.setContent(baos.toByteArray(), contentTypeHeader);
			}
		}
		else {
			// resource list or winfo subscription, no filtering, it's done at each backend subscription
			if (content instanceof JAXBElement<?>) {
				// marshall content and add to sip message
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				childSbb.getMarshaller().marshal(content, baos);
				notify.setContent(baos.toByteArray(), contentTypeHeader);
			}
			else {
				notify.setContent(content, contentTypeHeader);
			}
		}
		return notify;
	}

	/*
	 * for internal usage, creates a NOTIFY notify, asks the content to the
	 * concrete implementation component, and then sends the request to the
	 * subscriber
	 */
	public void createAndSendNotify(SubscriptionControlDataSource dataSource,
			Subscription subscription, DialogActivity dialog,
			ImplementedSubscriptionControlSbbLocalObject childSbb)
			throws TransactionDoesNotExistException, SipException,
			ParseException {

		// create notify
		Request notify = null;
		// add content if subscription is active
		if (subscription.getStatus().equals(Subscription.Status.active)) {
			if (subscription.getKey().getEventPackage().endsWith(".winfo")) {
				notify = createNotify(dialog, subscription,null);
				// winfo content, increment version before adding the content
				subscription.incrementVersion();
				subscription.store();
				notify.setContent(
						sipSubscriptionHandler.sbb
								.getWInfoSubscriptionHandler()
								.getFullWatcherInfoContent(dataSource,
										subscription),
						sipSubscriptionHandler.sbb
								.getWInfoSubscriptionHandler()
								.getWatcherInfoContentHeader());
			} else {
				// specific event package content
				NotifyContent notifyContent = childSbb
						.getNotifyContent(subscription);
				if (notifyContent == null) {
					notify = createNotify(dialog, subscription, null);
				}
				else {
					notify = createNotify(dialog, subscription, notifyContent.getEventHeaderParams());
					// add content
					if (notifyContent.getContent() != null) {
						try {
							notify = setNotifyContent(subscription, notify,
									notifyContent.getContent(), notifyContent
											.getContentTypeHeader(), childSbb);
						} catch (Exception e) {
							tracer.severe("failed to set notify content", e);
						}
					}
				}				
			}
		}
		else {
			notify = createNotify(dialog, subscription,null);
		}
		
		// ....aayush added code here (with ref issue #567)
		notify.addHeader(addPChargingVectorHeader());
		
		// send notify
		dialog.sendRequest(notify);
	}

	// creates a notify request and fills headers
	public Request createNotify(Dialog dialog, Subscription subscription, Map<String, String> eventHeaderParams) {

		Request notify = null;
		try {
			notify = dialog.createRequest(Request.NOTIFY);
			// add event header
			EventHeader eventHeader = sipSubscriptionHandler.sbb
					.getHeaderFactory().createEventHeader(
							subscription.getKey().getEventPackage());
			if (subscription.getKey().getEventId() != null)
				eventHeader.setEventId(subscription.getKey().getEventId());
			if (eventHeaderParams != null) {
				for(Entry<String, String> entry : eventHeaderParams.entrySet()) {
					eventHeader.setParameter(entry.getKey(), entry.getValue());
				}
			}
			notify.setHeader(eventHeader);
			// add max forwards header
			notify.setHeader(sipSubscriptionHandler.sbb.getHeaderFactory()
					.createMaxForwardsHeader(
							sipSubscriptionHandler.sbb.getConfiguration()
									.getMaxForwards()));
			/*
			 * NOTIFY requests MUST contain a "Subscription-State" header with a
			 * value of "active", "pending", or "terminated". The "active" value
			 * indicates that the subscription has been accepted and has been
			 * authorized (in most cases; see section 5.2.). The "pending" value
			 * indicates that the subscription has been received, but that
			 * policy information is insufficient to accept or deny the
			 * subscription at this time. The "terminated" value indicates that
			 * the subscription is not active.
			 */
			SubscriptionStateHeader ssh = null;
			if (subscription.getStatus().equals(Subscription.Status.active)
					|| subscription.getStatus().equals(
							Subscription.Status.pending)) {
				ssh = sipSubscriptionHandler.sbb.getHeaderFactory()
						.createSubscriptionStateHeader(
								subscription.getStatus().toString());
				/*
				 * If the value of the "Subscription-State" header is "active"
				 * or "pending", the notifier SHOULD also include in the
				 * "Subscription- State" header an "expires" parameter which
				 * indicates the time remaining on the subscription.
				 */
				ssh.setExpires(subscription.getRemainingExpires());
			} else if (subscription.getStatus().equals(
					Subscription.Status.waiting)
					|| subscription.getStatus().equals(
							Subscription.Status.terminated)) {
				ssh = sipSubscriptionHandler.sbb.getHeaderFactory()
						.createSubscriptionStateHeader("terminated");
				/*
				 * If the value of the "Subscription-State" header is
				 * "terminated", the notifier SHOULD also include a "reason"
				 * parameter.
				 */
				if(subscription.getLastEvent() != null) { 
					ssh.setReasonCode(subscription.getLastEvent().toString());
				}
			}
			notify.addHeader(ssh);
			
			// if it's a RLS notify a required header must be present
			if (subscription.getResourceList()) {
				notify.addHeader(sipSubscriptionHandler.sbb.getHeaderFactory().createRequireHeader("eventlist"));
			}
			
		} catch (Exception e) {
			tracer.severe("unable to fill notify headers", e);
		}
		return notify;
	}
	/**
	 * 
	 * @return the newly created P-charging-vector header
	 * @throws ParseException 
	 */
	private PChargingVectorHeader addPChargingVectorHeader() throws ParseException
	{
		// aayush..started adding here.
		
		/*
		 * (with ref to issue #567)
		 * Need to add a P-charging-vector header here with a unique ICID parameter
		 * and an orig-ioi parameter pointing to the home domain of the PS.
		 */
		
		// sbb.getHeaderFactory() does not provide the API for creating P-headers.
		HeaderFactoryExt extensions = new HeaderFactoryImpl();
		
		// Ideally,there should also be an ICID generator in Utils, that generates a unique ICID.
		PChargingVectorHeader pcv = extensions.createChargingVectorHeader(Utils.getInstance().generateBranchId()+System.currentTimeMillis());
		pcv.setOriginatingIOI(sipSubscriptionHandler.sbb.getConfiguration().getPChargingVectorHeaderTerminatingIOI());
		
		return pcv;
		//aayush...added code till here.
		
	}
}
