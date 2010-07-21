package org.mobicents.slee.sipevent.server.subscription.eventlist;

import java.util.Set;

import gov.nist.javax.sip.Utils;

import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.ChildRelation;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.rlscache.RLSService;
import org.mobicents.slee.sipevent.server.rlscache.RLSServiceActivity;
import org.mobicents.slee.sipevent.server.rlscache.events.RLSServicesRemovedEvent;
import org.mobicents.slee.sipevent.server.rlscache.events.RLSServicesUpdatedEvent;
import org.mobicents.slee.sipevent.server.subscription.EventListSubscriberParentSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.EventListSubscriber;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParentSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Event;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Status;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;

/**
 * 
 * Sbb that acts as the back end subscriber to the entries in a resource list.
 *  
 * @author Eduardo Martins
 * 
 */
public abstract class EventListSubscriberSbb implements Sbb,
		EventListSubscriber {

	private static final Logger logger = Logger
			.getLogger(EventListSubscriberSbb.class);
	
	// --- CMPs
	
	public abstract void setRLSServiceACI(ActivityContextInterface value);
	public abstract ActivityContextInterface getRLSServiceACI();
	
	public abstract void setNotificationData(NotificationData value);
	public abstract NotificationData getNotificationData();
		
	public abstract void setSubscriptionKey(SubscriptionKey subscriptionKey);
	public abstract SubscriptionKey getSubscriptionKey();
	
	public abstract void setSubscriber(String subscriber);
	public abstract String getSubscriber();
	
	public abstract void setParentSbbCMP(EventListSubscriberParentSbbLocalObject parentSbb);
	public abstract EventListSubscriberParentSbbLocalObject getParentSbbCMP();
	
	// --- sbb logic
	
	public void setParentSbb(EventListSubscriberParentSbbLocalObject parentSbb) {
		setParentSbbCMP(parentSbb);
	}
	
	private String getVirtualSubscriptionId(SubscriptionKey originalSubscriptionKey,String virtualSubscriptionNotifier) {
		return new StringBuilder(originalSubscriptionKey.toString()).append(":list:").append(virtualSubscriptionNotifier).toString();
	}
	
	public void subscribe(Subscription subscription, RLSService rlsService, ActivityContextInterface rlsServiceAci) {
		if (logger.isDebugEnabled()) {
			logger.debug("creating backend subscriptions for rls subscription "+subscription.getKey());
		}
		// store subscription data in cmp
		setSubscriptionKey(subscription.getKey());
		setSubscriber(subscription.getSubscriber());
		setRLSServiceACI(rlsServiceAci);
		// set notification data object, when a notification comes and this
		// object exists the notification data will be added, otherwise a new
		// NotificationData object for a single entry is created and the parent
		// is notified with the resulting multipart
		setNotificationData(new NotificationData(subscription.getNotifier().getUriWithParam(),subscription.getVersion(),rlsService,Utils.getInstance().generateTag(),Utils.getInstance().generateTag()));
		// get subscription client child
		SubscriptionClientControlSbbLocalObject subscriptionClient = getSubscriptionClientControlSbb();
		// create "virtual" subscriptions
		for (EntryType entryType : rlsService.getEntries()) {
			subscriptionClient.subscribe(subscription.getSubscriber(), subscription.getSubscriberDisplayName(), entryType.getUri(), subscription.getKey().getEventPackage(), getVirtualSubscriptionId(subscription.getKey(),entryType.getUri()), subscription.getExpires(), null, null, null);
		}
	}
	
	public void onRLSServicesRemovedEvent(RLSServicesRemovedEvent event, ActivityContextInterface aci) {
		if (logger.isDebugEnabled()) {
			logger.debug("rls service removed, terminating subscription "+getSubscriptionKey());
		}
		// time to remove the subscription
		unsubscribe(getSubscriber(), getSubscriptionKey(),getRLService());
	}
	
	public void onRLSServicesUpdatedEvent(RLSServicesUpdatedEvent event, ActivityContextInterface aci) {
		Subscription subscription = getParentSbbCMP().getSubscription(getSubscriptionKey());
		if (subscription != null) {
			resubscribe(subscription,getRLService(),event.getNewEntries(),event.getOldEntries(),event.getRemovedEntries());
		}
	}
		
	public void resubscribe(Subscription subscription, RLSService rlsService) {		
		resubscribe(subscription, rlsService, null, rlsService.getEntries(), null);
	}
	
	private void resubscribe(Subscription subscription, RLSService rlsService, Set<EntryType> newEntries, Set<EntryType> oldEntries, Set<EntryType> removedEntries) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("refreshing backend subscriptions for rls subscription "+subscription.getKey());
		}
		// version is incremented
		subscription.incrementVersion();
		subscription.store();
		// prepare for a full state notification
		setNotificationData(new NotificationData(subscription.getNotifier().getUriWithParam(),subscription.getVersion(),rlsService,Utils.getInstance().generateTag(),Utils.getInstance().generateTag()));
		// get subscription client child
		SubscriptionClientControlSbbLocalObject subscriptionClient = getSubscriptionClientControlSbb();
		// update "virtual" subscriptions
		if (removedEntries != null) {
			for (EntryType entryType : removedEntries) {
				subscriptionClient.unsubscribe(subscription.getSubscriber(), entryType.getUri(), subscription.getKey().getEventPackage(), getVirtualSubscriptionId(subscription.getKey(),entryType.getUri()));
			}
		}
		if (oldEntries != null) {
			for (EntryType entryType : oldEntries) {
				subscriptionClient.resubscribe(subscription.getSubscriber(), entryType.getUri(), subscription.getKey().getEventPackage(), getVirtualSubscriptionId(subscription.getKey(),entryType.getUri()), subscription.getExpires());
			}
		}
		if (newEntries != null) {
			for (EntryType entryType : newEntries) {
				subscriptionClient.subscribe(subscription.getSubscriber(), subscription.getSubscriberDisplayName(), entryType.getUri(), subscription.getKey().getEventPackage(), getVirtualSubscriptionId(subscription.getKey(),entryType.getUri()), subscription.getExpires(),null,null,null);
			}
		}
	}
	
	public void unsubscribe(Subscription subscription, RLSService rlsService) {
		unsubscribe(subscription.getSubscriber(), subscription.getKey(), rlsService);
	}
	
	private void unsubscribe(String subscriber, SubscriptionKey key, RLSService rlsService) {
		if (logger.isDebugEnabled()) {
			logger.debug("removing backend subscriptions for rls subscription "+key);
		}
		for (ActivityContextInterface aci : sbbContext.getActivities()) {
			aci.detach(sbbContext.getSbbLocalObject());
		}
		// let's set the key as null so there are no further notifications from back end subscriptions
		setSubscriptionKey(null);
		
		if(rlsService != null) {
			// get subscription client child
			SubscriptionClientControlSbbLocalObject subscriptionClient = getSubscriptionClientControlSbb();
			// remove "virtual" subscriptions
			for (EntryType entryType : rlsService.getEntries()) {
				subscriptionClient.unsubscribe(subscriber, entryType.getUri(), key.getEventPackage(), getVirtualSubscriptionId(key,entryType.getUri()));
			}
		}
	}
	
	private RLSService getRLService() {
		ActivityContextInterface aci = getRLSServiceACI();
		if (aci == null) return null;
		RLSServiceActivity activity = (RLSServiceActivity) aci.getActivity();
		return activity.getRLSService();
	}
	
	private Subscription getSubscription(EventListSubscriberParentSbbLocalObject parentSbb, SubscriptionKey key, String subscriber) {
		Subscription subscription = parentSbb.getSubscription(key);
		if (subscription == null && getSubscriptionKey() != null) {
			logger.warn("Unable to get subscription "+key+" from parent sbb, it does not exists anymore! Removing all virtual subscriptions");
			unsubscribe(subscriber, key,getRLService());
		}
		return subscription;
	}
	
	// --- SIP EVETN CHILD SBB CALL BACKS
	
	private NotificationData createPartialStateNotificationData(EventListSubscriberParentSbbLocalObject parentSbb, SubscriptionKey subscriptionKey, String subscriber, String notifier) {
		// get subscription
		Subscription subscription = getSubscription(parentSbb, subscriptionKey, subscriber);
		if (subscription != null) {
			// increment subscription version
			subscription.incrementVersion();
			subscription.store();
			// create notification data for a single resource
			RLSService rlsService = getRLService();
			if (rlsService != null) {
				for (EntryType entryType : rlsService.getEntries()) {
					if (entryType.getUri().equals(notifier)) {
						return new NotificationData(subscription.getNotifier().getUriWithParam(),subscription.getVersion(),entryType,Utils.getInstance().generateTag(), Utils.getInstance().generateTag());
					}
				}
				
			}			
		}
		return null;
	}
	
	public void notifyEvent(String subscriber, String notifier,
			String eventPackage, String subscriptionId,
			Event terminationReason, Status status, String content,
			String contentType, String contentSubtype) {
		
		SubscriptionKey subscriptionKey = getSubscriptionKey();
		
		// if key is null we are removing subscriptions and have no interest in further notifications
		
		if (subscriptionKey != null) {
		
			
			if (logger.isDebugEnabled()) {
				logger.debug("notification for rls subscription "+subscriptionKey+" from " + notifier);
			}

			EventListSubscriberParentSbbLocalObject parentSbb = getParentSbbCMP();
			NotificationData notificationData = getNotificationData();

			if (notificationData == null) {
				notificationData = createPartialStateNotificationData(parentSbb, subscriptionKey, subscriber, notifier);
				if (notificationData == null) {
					// null then abort notification
					return;
				}
			}

			// add notification data
			String id = notifier;
			String cid = content != null ? id : null;
			MultiPart multiPart = null;
			try {
				multiPart = notificationData.addNotificationData(notifier, cid, id, content, contentType, contentSubtype, status.toString(), (terminationReason == null ? null : terminationReason.toString()));
			}
			catch (IllegalStateException e) {
				if (logger.isDebugEnabled()) {
					logger.debug(e.getMessage(),e);
				}
				// there is a chance that on a full state update concurrent backend subscriptions may try add notification data after multipart was built, if that happens we will get this exception and do a partial notification 
				notificationData = createPartialStateNotificationData(parentSbb, subscriptionKey, subscriber, notifier);
				if (notificationData == null) {
					// null then abort notification
					return;
				}
				multiPart = notificationData.addNotificationData(notifier, cid, id, content, contentType, contentSubtype, status.toString(), (terminationReason == null ? null : terminationReason.toString()));
			}
			// notify parent?
			if (multiPart != null) {
				setNotificationData(null);
				parentSbb.notifyEventListSubscriber(subscriptionKey, multiPart);
			}
		}
	}
	
	public void resubscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("resubscribeError: sid="+subscriptionId+", error="+error);
		}
		
		EventListSubscriberParentSbbLocalObject parentSbb = getParentSbbCMP();
		SubscriptionKey key = getSubscriptionKey();
		
		switch (error) {
		case Response.CONDITIONAL_REQUEST_FAILED:
			// perhaps virtual subscription died, lets try to subscribe again
			Subscription subscription = getSubscription(parentSbb, key, subscriber);
			if (subscription != null) {
				getSubscriptionClientControlSbb().subscribe(subscriber, subscription.getSubscriberDisplayName(), notifier, eventPackage, subscriptionId, subscription.getExpires(), null, null, null);
			}
			break;
	
		default:
			break;
		}
	}
	
	public void resubscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires) {
		// ignore
		if (logger.isDebugEnabled()) {
			logger.debug("resubscribeOk: sid="+subscriptionId);
		}		
	}
	
	public void subscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		if (logger.isDebugEnabled()) {
			logger.debug("subscribeError: sid="+subscriptionId+", error="+error);
		}
		
		EventListSubscriberParentSbbLocalObject parentSbb = getParentSbbCMP();
		SubscriptionKey subscriptionKey = getSubscriptionKey();
		NotificationData notificationData = getNotificationData();
		
		if (notificationData == null) {
			notificationData = createPartialStateNotificationData(parentSbb, subscriptionKey, subscriber, notifier);
		}
		
		String cid = notifier;
		MultiPart multiPart = null;
		// add notification data
		switch (error) {
		case Response.FORBIDDEN:
			try {
				multiPart = notificationData.addNotificationData(notifier, null, cid, null, null, null, "terminated", "rejected");
			}
			catch (IllegalStateException e) {
				// there is a chance that on a full state update concurrent backend subscriptions may try add notification data after multipart was built, if that happens we will get this exception and do a partial notification 
				notificationData = createPartialStateNotificationData(parentSbb, subscriptionKey, subscriber, notifier);
				multiPart = notificationData.addNotificationData(notifier, null, cid, null, null, null, "terminated", "rejected");
			}			
			break;
	
		default:
			try {
				multiPart = notificationData.notificationDataNotNeeded(notifier);
			}
			catch (IllegalStateException e) {
				// there is a chance that on a full state update concurrent backend subscriptions may try add notification data after multipart was built, if that happens we will get this exception and do a partial notification 
				notificationData = createPartialStateNotificationData(parentSbb, subscriptionKey, subscriber, notifier);
				multiPart = notificationData.notificationDataNotNeeded(notifier);
			}		
			break;
		}
		
		// notify parent?
		if (multiPart != null) {
			setNotificationData(null);
			parentSbb.notifyEventListSubscriber(subscriptionKey, multiPart);
		}
	}
	
	public void subscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires,
			int responseCode) {
		// ignore
		if (logger.isDebugEnabled()) {
			logger.debug("subscribeOk: sid="+subscriptionId);
		}
	}
	
	public void unsubscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		if (logger.isDebugEnabled()) {
			logger.debug("unsubscribeError: sid="+subscriptionId+", error="+error);
		}
	}
	
	public void unsubscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId) {
		if (logger.isDebugEnabled()) {
			logger.debug("unsubscribeOk: sid="+subscriptionId);
		}
	}
	
	// --- SIP EVENT CLIENT CHILD SBB
	
	public abstract ChildRelation getSubscriptionClientControlChildRelation();

	public abstract SubscriptionClientControlSbbLocalObject getSubscriptionClientControlChildSbbCMP();

	public abstract void setSubscriptionClientControlChildSbbCMP(
			SubscriptionClientControlSbbLocalObject value);

	public SubscriptionClientControlSbbLocalObject getSubscriptionClientControlSbb() {
		SubscriptionClientControlSbbLocalObject childSbb = getSubscriptionClientControlChildSbbCMP();
		if (childSbb == null) {
			try {
				childSbb = (SubscriptionClientControlSbbLocalObject) getSubscriptionClientControlChildRelation()
						.create();
			} catch (Exception e) {
				logger.error("Failed to create child sbb", e);
				return null;
			}
			setSubscriptionClientControlChildSbbCMP(childSbb);
			childSbb
					.setParentSbb((SubscriptionClientControlParentSbbLocalObject) this.sbbContext
							.getSbbLocalObject());
		}
		return childSbb;
	}
	
	// ----------- SBB OBJECT's LIFE CYCLE

	private SbbContext sbbContext;
	
	public void setSbbContext(SbbContext sbbContext) {
		this.sbbContext = sbbContext;
	}
	
	public void sbbActivate() {
	}

	public void sbbCreate() throws CreateException {
	}

	public void sbbExceptionThrown(Exception arg0, Object arg1,
			ActivityContextInterface arg2) {
	}

	public void sbbLoad() {
	}

	public void sbbPassivate() {
	}

	public void sbbPostCreate() throws CreateException {
	}

	public void sbbRemove() {
	}

	public void sbbRolledBack(RolledBackContext arg0) {
	}

	public void sbbStore() {
	}

	public void unsetSbbContext() {
		this.sbbContext = null;
	}
		
}