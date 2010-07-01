package org.mobicents.slee.sipevent.examples;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.slee.ActivityContextInterface;
import javax.slee.ActivityEndEvent;
import javax.slee.ChildRelation;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.SLEEException;
import javax.slee.SbbContext;
import javax.slee.TransactionRequiredLocalException;
import javax.slee.facilities.TimerEvent;
import javax.slee.facilities.TimerFacility;
import javax.slee.facilities.TimerOptions;
import javax.slee.facilities.Tracer;
import javax.slee.serviceactivity.ServiceActivity;
import javax.slee.serviceactivity.ServiceActivityContextInterfaceFactory;
import javax.slee.serviceactivity.ServiceActivityFactory;

import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParent;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParentSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Event;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Status;

/**
 * Example of an application that uses
 * {@link SubscriptionClientControlSbbLocalObject} as a child sbb, and
 * implements {@link SubscriptionClientControlParentSbbLocalObject}, to
 * interact with the Mobicents SIP Event Subscription service.
 * 
 * @author Eduardo Martins
 * 
 */
public abstract class InternalSubscriberExampleSbb implements javax.slee.Sbb,
		SubscriptionClientControlParent {

	String presenceDomain = System.getProperty("bind.address","127.0.0.1");
	String subscriber = "sip:internal-subscriber@"+presenceDomain;
	String notifier = "sip:user@"+presenceDomain;
	String eventPackage = "presence";
	int expires = 300;

	// --- INTERNAL CHILD SBB

	public abstract ChildRelation getSubscriptionControlChildRelation();

	public abstract SubscriptionClientControlSbbLocalObject getSubscriptionControlChildSbbCMP();

	public abstract void setSubscriptionControlChildSbbCMP(
			SubscriptionClientControlSbbLocalObject value);

	private SubscriptionClientControlSbbLocalObject getSubscriptionControlChildSbb()
			throws TransactionRequiredLocalException, SLEEException,
			CreateException {
		SubscriptionClientControlSbbLocalObject childSbb = getSubscriptionControlChildSbbCMP();
		if (childSbb == null) {
			childSbb = (SubscriptionClientControlSbbLocalObject) getSubscriptionControlChildRelation()
					.create();
			setSubscriptionControlChildSbbCMP(childSbb);
			childSbb
					.setParentSbb((SubscriptionClientControlParentSbbLocalObject) this.sbbContext
							.getSbbLocalObject());
		}
		return childSbb;
	}

	// --- CMP

	public abstract void setSubscriptionId(String value);

	public abstract String getSubscriptionId();

	/*
	 * service activation event, create subscription
	 */
	public void onServiceStartedEvent(
			javax.slee.serviceactivity.ServiceStartedEvent event,
			ActivityContextInterface aci) {

		// check if it's my service that is starting
			tracer.info("Service activated, subscribing state...");
			try {
				// create sub id
				String subscriptionId = subscriber + ":" + notifier + ":"
						+ eventPackage;
				// save subscription id in cmp
				setSubscriptionId(subscriptionId);
				// subscribe
				getSubscriptionControlChildSbb().subscribe(subscriber, "voyer",
						notifier, eventPackage, subscriptionId, expires, null,
						null, null);
			} catch (Exception e) {
				tracer.severe("",e);
			}
	}

	public void subscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires,
			int responseCode) {
		tracer.info("subscribe ok: responseCode=" + responseCode + ",expires="
				+ expires);
		// let's set a periodic timer in the service activity, that originated
		// this sbb entity (onServiceStartedEvent()...), to refresh the
		// subscription
		TimerOptions timerOptions = new TimerOptions();
		ServiceActivity serviceActivity = serviceActivityFactory.getActivity();
		ActivityContextInterface aci = null;
		try {
			aci = serviceActivityContextInterfaceFactory
					.getActivityContextInterface(serviceActivity);
		} catch (Exception e) {
			tracer.severe("Failed to retreive service activity aci", e);
			try {
				getSubscriptionControlChildSbb().unsubscribe(subscriber,
						notifier, eventPackage, subscriptionId);
			} catch (Exception f) {
				tracer.severe("Dude, now I can't get the child sbb!!", f);
			}
			return;
		}
		timerFacility.setTimer(aci, null, System.currentTimeMillis() + expires
				* 1000, expires * 1000, 0, timerOptions);
	}

	public void subscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		tracer.info("error on subscribe: error=" + error);
	}

	public void notifyEvent(String subscriber, String notifier,
			String eventPackage, String subscriptionId, Event terminationReason,
			Status status, String content, String contentType,
			String contentSubtype) {
		String notification = "\nNOTIFY EVENT:" + "\n+-- Subscriber: "
				+ subscriber + "\n+-- Notifier: " + notifier
				+ "\n+-- EventPackage: " + eventPackage
				+ "\n+-- SubscriptionId: " + subscriptionId				
				+ "\n+-- Subscription status: " + status
				+ "\n+-- Subscription terminationReason: " + terminationReason
				+ "\n+-- Content Type: " + contentType + '/' + contentSubtype
				+ "\n+-- Content:\n\n" + content;
		tracer.info(notification);

	}

	public void onTimerEvent(TimerEvent event, ActivityContextInterface aci) {
		// resubscribe
		try {
			getSubscriptionControlChildSbb().resubscribe(subscriber, notifier,
					eventPackage, getSubscriptionId(), expires);
		} catch (Exception e) {
			tracer.severe("",e);
		}
	}

	public void resubscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires) {
		tracer.info("resubscribe Ok : expires=" + expires);

	}

	public void resubscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		tracer.info("error on resubscribe: error=" + error);
	}

	/**
	 * service deactivation, unsubscribe
	 * 
	 * @param event
	 * @param aci
	 */
	public void onActivityEndEvent(ActivityEndEvent event,
			ActivityContextInterface aci) {
		if (getSubscriptionId() != null) {
			tracer.info("Service deactivated, removing subscription...");
			try {
				getSubscriptionControlChildSbb().unsubscribe(subscriber,
						notifier, eventPackage, getSubscriptionId());
			} catch (Exception e) {
				tracer.severe("",e);
			}
		} else {
			tracer.info("Service deactivated, no subscription to remove.");
		}
	}

	public void unsubscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId) {
		tracer.info("unsubscribe Ok");

	}

	public void unsubscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		tracer.info("error on unsubscribe: error=" + error);
	}

	// --- SBB OBJECT

	private SbbContext sbbContext = null; // This SBB's context

	private TimerFacility timerFacility = null;
	private ServiceActivityFactory serviceActivityFactory = null;
	private ServiceActivityContextInterfaceFactory serviceActivityContextInterfaceFactory = null;

	/**
	 * Called when an sbb object is instantied and enters the pooled state.
	 */
	public void setSbbContext(SbbContext sbbContext) {

		this.sbbContext = sbbContext;
		this.tracer = sbbContext.getTracer("InternalSubscriberExampleSbb");
		try {
			Context context = (Context) new InitialContext()
					.lookup("java:comp/env");
			timerFacility = (TimerFacility) context
					.lookup("slee/facilities/timer");
			serviceActivityFactory = (ServiceActivityFactory) context
				.lookup("slee/serviceactivity/factory");
			serviceActivityContextInterfaceFactory = (ServiceActivityContextInterfaceFactory) context
				.lookup("slee/serviceactivity/activitycontextinterfacefactory");
		} catch (Exception e) {
			tracer.severe("Unable to retrieve factories, facilities & providers",
					e);
		}
	}

	public void unsetSbbContext() {
		this.sbbContext = null;
	}

	public void sbbCreate() throws javax.slee.CreateException {
	}

	public void sbbPostCreate() throws javax.slee.CreateException {
	}

	public void sbbActivate() {
	}

	public void sbbPassivate() {
	}

	public void sbbRemove() {
	}

	public void sbbLoad() {
	}

	public void sbbStore() {
	}

	public void sbbExceptionThrown(Exception exception, Object event,
			ActivityContextInterface activity) {
	}

	public void sbbRolledBack(RolledBackContext sbbRolledBack) {
	}

	private Tracer tracer;

}