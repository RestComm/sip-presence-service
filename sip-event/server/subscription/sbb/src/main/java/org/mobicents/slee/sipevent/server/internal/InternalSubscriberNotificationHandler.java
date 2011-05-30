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

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;

import javax.sip.header.ContentTypeHeader;
import javax.slee.ActivityContextInterface;
import javax.slee.facilities.Tracer;
import javax.slee.nullactivity.NullActivity;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.NotifyContent;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParentSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.openxdm.xcap.common.xml.TextWriter;
import org.w3c.dom.Node;

/**
 * Handles the notification of a SIP subscriber
 * 
 * @author martins
 * 
 */
public class InternalSubscriberNotificationHandler {

	private static Tracer tracer;
	
	private InternalSubscriptionHandler internalSubscriptionHandler;

	public InternalSubscriberNotificationHandler(
			InternalSubscriptionHandler sbb) {
		this.internalSubscriptionHandler = sbb;
		if (tracer == null) {
			tracer = internalSubscriptionHandler.sbb.getSbbContext().getTracer(getClass().getSimpleName());
		}
	}

	public void notifyInternalSubscriber(
			Subscription subscription, ActivityContextInterface aci,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {
		
		NotifyContent notifyContent = null; 
		// only get notify content if subscription status is active
		if (subscription.getStatus().equals(Subscription.Status.active)) {
			notifyContent = childSbb.getNotifyContent(subscription); 
		}
			
		notifyInternalSubscriber(subscription, notifyContent, aci,childSbb);		
		
	}

	public void notifyInternalSubscriber(
			Subscription subscription, String content,
			ContentTypeHeader contentTypeHeader, ActivityContextInterface aci) {
		
		String contentType = null;
		String contentSubtype = null;
		if (contentTypeHeader != null) {
			contentType = contentTypeHeader.getContentType();
			contentSubtype = contentTypeHeader.getContentSubType();
		}

		// if subscription status is waiting notify terminated status
		Subscription.Status status = subscription.getStatus();
		if (status == Subscription.Status.waiting) {
			status = Subscription.Status.terminated;
		}
		// put last event if subscription terminated
		Subscription.Event lastEvent = null;
		if (status == Subscription.Status.terminated) {
			lastEvent = subscription.getLastEvent();
			// end subscription aci
			((NullActivity) aci.getActivity()).endActivity();
		}

		SubscriptionClientControlParentSbbLocalObject parent = internalSubscriptionHandler.sbb.getParentSbb();
		if (parent != null) {
			parent.notifyEvent(
				subscription.getSubscriber(),
				subscription.getNotifier().getUriWithParam(),
				subscription.getKey().getEventPackage(),
				subscription.getKey().getEventId(), lastEvent, status, content,
				contentType, contentSubtype);
		}
	}

	public void notifyInternalSubscriber(
			Subscription subscription, NotifyContent notifyContent,
			ActivityContextInterface aci,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {

		try {
			Object content = null;
			ContentTypeHeader contentTypeHeader = null;
			if (notifyContent != null) {
				content = notifyContent.getContent();
				contentTypeHeader = notifyContent.getContentTypeHeader();
				if (content != null) {
					if (!subscription.getResourceList()) {
						notifyInternalSubscriber(subscription,getFilteredNotifyContent(subscription, content,
										childSbb), contentTypeHeader, aci);
					}
					else {
						// resource list subscription, no filtering
						notifyInternalSubscriber(subscription, (String)content, contentTypeHeader, aci);
					}
				}
				else {
					notifyInternalSubscriber(subscription,null, null, aci);
				}				
			}
			else {
				notifyInternalSubscriber(subscription,null, null, aci);
				
			}			
		} catch (Exception e) {
			tracer.severe("failed to notify internal subscriber", e);
		}
	}
	
	private String getFilteredNotifyContent(Subscription subscription,
			Object content, ImplementedSubscriptionControlSbbLocalObject childSbb)
			throws JAXBException, ParseException, IOException {

		// filter content per subscriber (notifier rules)
		Object filteredContent = childSbb.filterContentPerSubscriber(subscription, content);
		// filter content per notifier (subscriber rules)
		// TODO
		// marshall content to string
		String result = null;
		if (content instanceof Node) {
			// dom
			try {
				result = TextWriter.toString((Node)content);
			} catch (TransformerException e) {
				throw new IOException("failed to marshall DOM content",e);
			}
		}
		else {
			// jaxb
			StringWriter stringWriter = new StringWriter();
			childSbb.getMarshaller().marshal(filteredContent, stringWriter);
			result = stringWriter.toString();
			stringWriter.close();
		}
		return result;
	}
}
