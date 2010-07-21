package org.mobicents.slee.sipevent.server.subscription.eventlist;

import java.util.ListIterator;

import javax.sip.RequestEvent;
import javax.sip.header.AcceptHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.rlscache.RLSService;
import org.mobicents.slee.sipevent.server.rlscache.RLSServiceActivity;
import org.mobicents.slee.sipevent.server.rlscache.RLSServicesCacheSbbInterface;
import org.mobicents.slee.sipevent.server.subscription.EventListSubscriberParentSbbLocalObject;
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

	private static final Logger logger = Logger
			.getLogger(EventListSubscriptionHandler.class);

	private final SubscriptionControlSbb sbb;

	/**
	 * can verify if a service type object has a certain event package
	 */
	private static final ServiceTypePackageVerifier serviceTypePackageVerifier = new ServiceTypePackageVerifier();
	
	public EventListSubscriptionHandler(
			SubscriptionControlSbb sbb) {
		this.sbb = sbb;
	}


	public int validateSubscribeRequest(String subscriber, Notifier notifier,
			String eventPackage, RequestEvent event) {

		boolean debugLog = logger.isDebugEnabled();
				
		RLSService rlsService = sbb.getRlsServicesCacheRASbbInterface().getRLSService(notifier.getUriWithParam());

		final RLSService.Status rlsServiceStatus = rlsService != null ? rlsService.getStatus() : RLSService.Status.DOES_NOT_EXISTS;
		
		if (debugLog) {
			logger.debug(notifier.getUriWithParam()+" rlsService status retreived from rls services cache: "+rlsServiceStatus);
		}
		
		if (rlsServiceStatus != RLSService.Status.DOES_NOT_EXISTS && rlsServiceStatus != RLSService.Status.RESOLVING) {

			if (debugLog) {
				logger.debug(notifier + " is a resource list.");
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
					if (logger.isInfoEnabled()) {
						logger
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
					if (logger.isInfoEnabled()) {
						logger
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
				if (logger.isInfoEnabled()) {
					logger.info("Resource list " + notifier
							+ " doesn't applies to event package "
							+ eventPackage);
				}
				return Response.BAD_EVENT;
			}

			// it is a subscribe for a resource list and it is ok (note: the
			// flat list may had errors, but it's not empty so we let it
			// proceed
			if (logger.isDebugEnabled()) {
				logger.debug("Resource list " + notifier
						+ " subscription request validated with sucess.");
			}
			return Response.OK;
			
		} else {
			// no resource list found
			if (debugLog) {
				logger.debug(notifier + " is not a known resource list.");
			}
			return Response.NOT_FOUND;
		}

	}

	public boolean createSubscription(Subscription subscription) {

		RLSServicesCacheSbbInterface rlsServicesCacheSbbInterface = sbb.getRlsServicesCacheRASbbInterface();
		RLSServiceActivity activity = null;
		try {
			activity = rlsServicesCacheSbbInterface.getRLSServiceActivity(subscription.getNotifier().getUriWithParam());
		}
		catch (Throwable e) {
			logger.error("failed to get rls service activity "+subscription.getNotifier().getUriWithParam(),e);
			return false;
		}
		
		// get flat list
		RLSService rlsService = activity.getRLSService();
		if (rlsService == null || rlsService.getStatus() != RLSService.Status.OK) {
			return false;
		}
		// now create a event list subscriber child sbb
		EventListSubscriberSbbLocalObject subscriptionChildSbb = null;
		ActivityContextInterface aci = null;
		try {
			subscriptionChildSbb = (EventListSubscriberSbbLocalObject) sbb.getEventListSubscriberChildRelation()
					.create();
			subscriptionChildSbb
					.setParentSbb((EventListSubscriberParentSbbLocalObject) sbb.getSbbContext().getSbbLocalObject());
			aci = sbb.getRlsServicesCacheACIF().getActivityContextInterface(activity);
			aci.attach(subscriptionChildSbb);
		} catch (Exception e) {
			logger.error("Failed to create child sbb", e);
			return false;
		}
		
		// give the child control over the subscription
		subscriptionChildSbb.subscribe(subscription, rlsService, aci);
		return true;
	}

	public void refreshSubscription(Subscription subscription) {
		EventListSubscriberSbbLocalObject childSbb = sbb.getEventListSubscriberSbb(subscription
				.getKey());
		if (childSbb != null) {
			childSbb.resubscribe(subscription,sbb.getRlsServicesCacheRASbbInterface().getRLSService(subscription.getNotifier().getUriWithParam()));
		} else {
			logger
					.warn("trying to refresh a event list subscription but child sbb not found");
		}
	}

	public void removeSubscription(Subscription subscription) {
		EventListSubscriberSbbLocalObject childSbb = sbb.getEventListSubscriberSbb(subscription
				.getKey());
		if (childSbb != null) {
			childSbb.unsubscribe(subscription,sbb.getRlsServicesCacheRASbbInterface().getRLSService(subscription.getNotifier().getUriWithParam()));
		} else {
			logger
					.warn("trying to unsubscribe a event list subscription but child sbb not found");
		}
	}

}