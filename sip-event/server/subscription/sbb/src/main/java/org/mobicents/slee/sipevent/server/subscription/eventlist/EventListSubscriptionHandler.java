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

package org.mobicents.slee.sipevent.server.subscription.eventlist;

import java.util.ListIterator;

import javax.sip.RequestEvent;
import javax.sip.header.AcceptHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.facilities.Tracer;

import org.mobicents.slee.ChildRelationExt;
import org.mobicents.slee.sipevent.server.rlscache.RLSService;
import org.mobicents.slee.sipevent.server.rlscache.RLSServiceActivity;
import org.mobicents.slee.sipevent.server.rlscache.RLSServicesCacheSbbInterface;
import org.mobicents.slee.sipevent.server.subscription.EventListSubscriberSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionControlSbb;
import org.mobicents.slee.sipevent.server.subscription.data.Notifier;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;

/**
 * 
 * 
 * @author Eduardo Martins
 * 
 */
public class EventListSubscriptionHandler {

	private static Tracer tracer;

	private final SubscriptionControlSbb sbb;

	/**
	 * can verify if a service type object has a certain event package
	 */
	private static final ServiceTypePackageVerifier serviceTypePackageVerifier = new ServiceTypePackageVerifier();
	
	public EventListSubscriptionHandler(
			SubscriptionControlSbb sbb) {
		this.sbb = sbb;
		if (tracer == null) {
			tracer = sbb.getSbbContext().getTracer(getClass().getSimpleName());
		}
	}


	public int validateSubscribeRequest(String subscriber, Notifier notifier,
			String eventPackage, RequestEvent event) {

		boolean fineLog = tracer.isFineEnabled();
				
		RLSService rlsService = sbb.getRlsServicesCacheRASbbInterface().getRLSService(notifier.getUriWithParam());

		final RLSService.Status rlsServiceStatus = rlsService != null ? rlsService.getStatus() : RLSService.Status.DOES_NOT_EXISTS;
		
		if (fineLog) {
			tracer.fine(notifier.getUriWithParam()+" rlsService status retreived from rls services cache: "+rlsServiceStatus);
		}
		
		if (rlsServiceStatus != RLSService.Status.DOES_NOT_EXISTS && rlsServiceStatus != RLSService.Status.RESOLVING) {

			if (fineLog) {
				tracer.fine(notifier + " is a resource list.");
			}

			if (event != null) {
				// check event list support is present in UA
				boolean isEventListSupported = false;
				for (ListIterator<?> lit = event.getRequest().getHeaders(
						SupportedHeader.NAME); lit.hasNext();) {
					SupportedHeader sh = (SupportedHeader) lit.next();
					if (sh.getOptionTag().equals("eventlist")) {
						isEventListSupported = true;
						break;
					}
				}
				if (!isEventListSupported) {
					if (tracer.isInfoEnabled()) {
						tracer
								.info("SIP subscription request for resource list doesn't included Supported: eventlist header");
					}
					return Response.EXTENSION_REQUIRED;
				}

				boolean isMultipartAccepted = false;
				boolean isRlmiAccepted = false;
				for (ListIterator<?> lit = event.getRequest().getHeaders(
						AcceptHeader.NAME); lit.hasNext();) {
					AcceptHeader ah = (AcceptHeader) lit.next();
					if (ah.allowsAllContentTypes()
							&& ah.allowsAllContentSubTypes()) {
						isMultipartAccepted = true;
						isRlmiAccepted = true;
						break;
					}
					if (!isMultipartAccepted
							&& ah.getContentSubType().equals("related")
							&& ah.getContentType().equals("multipart")) {
						isMultipartAccepted = true;
					}
					if (!isRlmiAccepted
							&& ah.getContentSubType().equals("rlmi+xml")
							&& ah.getContentType().equals("application")) {
						isRlmiAccepted = true;
					}
				}
				if (!isMultipartAccepted || !isRlmiAccepted) {
					if (tracer.isInfoEnabled()) {
						tracer
								.info("SIP subscription request for resource list doesn't included proper Accept headers");
					}
					return Response.NOT_ACCEPTABLE;
				}
			}
			if (rlsServiceStatus == RLSService.Status.BAD_GATEWAY) {
				return Response.BAD_GATEWAY;
			}
			
			// check service's packages contains provided event package
			if (!serviceTypePackageVerifier.hasPackage(rlsService.getPackages(), eventPackage)) {
				if (tracer.isInfoEnabled()) {
					tracer.info("Resource list " + notifier
							+ " doesn't applies to event package "
							+ eventPackage);
				}
				return Response.BAD_EVENT;
			}

			// it is a subscribe for a resource list and it is ok (note: the
			// flat list may had errors, but it's not empty so we let it
			// proceed
			if (tracer.isFineEnabled()) {
				tracer.fine("Resource list " + notifier
						+ " subscription request validated with sucess.");
			}
			return Response.OK;
			
		} else {
			// no resource list found
			if (fineLog) {
				tracer.fine(notifier + " is not a known resource list.");
			}
			return Response.NOT_FOUND;
		}

	}

	public boolean createSubscription(Subscription subscription) {

		RLSServicesCacheSbbInterface rlsServicesCacheSbbInterface = sbb.getRlsServicesCacheRASbbInterface();
		RLSServiceActivity activity = null;
		ActivityContextInterface aci = null;
		try {
			activity = rlsServicesCacheSbbInterface.getRLSServiceActivity(subscription.getNotifier().getUriWithParam());
			aci = sbb.getRlsServicesCacheACIF().getActivityContextInterface(activity);
		}
		catch (Throwable e) {
			tracer.severe("failed to get rls service activity "+subscription.getNotifier().getUriWithParam(),e);
			return false;
		}
		
		// get flat list
		RLSService rlsService = activity.getRLSService();
		if (rlsService == null || rlsService.getStatus() != RLSService.Status.OK) {
			return false;
		}
		// now create a event list subscriber child sbb
		ChildRelationExt childRelationExt = sbb.getEventListSubscriberChildRelation();
		
		EventListSubscriberSbbLocalObject subscriptionChildSbb = null;
		try {
			subscriptionChildSbb = (EventListSubscriberSbbLocalObject) childRelationExt.create(subscription.getKey().toString());
		}
		catch (Exception e) {
			tracer.severe("failed to create event list subscriber",e);
			return false;
		}
		aci.attach(subscriptionChildSbb);
				
		// give the child control over the subscription
		subscriptionChildSbb.subscribe(subscription, rlsService, aci);
		return true;
	}

	public void refreshSubscription(Subscription subscription) {
		EventListSubscriberSbbLocalObject childSbb = (EventListSubscriberSbbLocalObject) sbb.getEventListSubscriberChildRelation().get(subscription.getKey().toString());
		if (childSbb != null) {
			childSbb.resubscribe(subscription,sbb.getRlsServicesCacheRASbbInterface().getRLSService(subscription.getNotifier().getUriWithParam()));
		} else {
			tracer
					.warning("trying to refresh a event list subscription but child sbb not found");
		}
	}

	public void removeSubscription(Subscription subscription) {
		EventListSubscriberSbbLocalObject childSbb = (EventListSubscriberSbbLocalObject) sbb.getEventListSubscriberChildRelation().get(subscription.getKey().toString());
		if (childSbb != null) {
			childSbb.unsubscribe(subscription,sbb.getRlsServicesCacheRASbbInterface().getRLSService(subscription.getNotifier().getUriWithParam()));
		} else {
			tracer
					.warning("trying to unsubscribe a event list subscription but child sbb not found");
		}
	}

}