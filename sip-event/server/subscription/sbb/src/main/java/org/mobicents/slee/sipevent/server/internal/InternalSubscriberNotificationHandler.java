package org.mobicents.slee.sipevent.server.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;

import javax.sip.header.ContentTypeHeader;
import javax.slee.ActivityContextInterface;
import javax.slee.nullactivity.NullActivity;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.NotifyContent;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParentSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionControlSbb;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;

/**
 * Handles the notification of a SIP subscriber
 * 
 * @author martins
 * 
 */
public class InternalSubscriberNotificationHandler {

	private static Logger logger = Logger
			.getLogger(SubscriptionControlSbb.class);

	private InternalSubscriptionHandler internalSubscriptionHandler;

	public InternalSubscriberNotificationHandler(
			InternalSubscriptionHandler sbb) {
		this.internalSubscriptionHandler = sbb;
	}

	public void notifyInternalSubscriber(
			Subscription subscription, ActivityContextInterface aci,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {
		
		NotifyContent notifyContent = null; 
		// only get notify content if subscription status is active
		if (subscription.getStatus().equals(Subscription.Status.active)) {
			notifyContent = childSbb.getNotifyContent(subscription); 
		}
			
		if (notifyContent != null) {
			notifyInternalSubscriber(subscription, notifyContent
					.getContent(), notifyContent.getContentTypeHeader(), aci,childSbb);
		}
		else {
			notifyInternalSubscriber(subscription, null, null, aci,childSbb);
		}
		
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

		SubscriptionClientControlParentSbbLocalObject parent = internalSubscriptionHandler.sbb.getParentSbbCMP();
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
			Subscription subscription, Object content,
			ContentTypeHeader contentTypeHeader,
			ActivityContextInterface aci,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {

		try {
			if (!subscription.getResourceList()) {
				notifyInternalSubscriber(subscription,
						(content != null ? getFilteredNotifyContent(subscription, content,
								childSbb) : null), contentTypeHeader, aci);
			}
			else {
				// resource list subscription, no filtering
				notifyInternalSubscriber(subscription,
						(content != null ? (String)content : null), contentTypeHeader, aci);
			}
		} catch (Exception e) {
			logger.error("failed to notify internal subscriber", e);
		}
	}
	
	private String getFilteredNotifyContent(Subscription subscription,
			Object content, ImplementedSubscriptionControlSbbLocalObject childSbb)
			throws JAXBException, ParseException, IOException {

		// filter content per subscriber (notifier rules)
		Object filteredContent = childSbb.filterContentPerSubscriber(
				subscription.getSubscriber(), subscription.getNotifier(),
				subscription.getKey().getEventPackage(), content);
		// filter content per notifier (subscriber rules)
		// TODO
		// marshall content to string
		StringWriter stringWriter = new StringWriter();
		childSbb.getMarshaller().marshal(filteredContent, stringWriter);
		String result = stringWriter.toString();
		stringWriter.close();

		return result;
	}
}
