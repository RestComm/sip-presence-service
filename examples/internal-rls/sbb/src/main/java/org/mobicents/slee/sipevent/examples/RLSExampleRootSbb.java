package org.mobicents.slee.sipevent.examples;

import javax.slee.ActivityContextInterface;
import javax.slee.ActivityEndEvent;
import javax.slee.RolledBackContext;
import javax.slee.SbbContext;
import javax.slee.facilities.Tracer;

import org.mobicents.slee.ChildRelationExt;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParentSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlSbbLocalObject;

/**
 * Example of an application that uses
 * {@link SubscriptionClientControlSbbLocalObject} as a child sbb, and
 * implements {@link SubscriptionClientControlParentSbbLocalObject}, to
 * interact with the Mobicents SIP Event Subscription service.
 * 
 * @author Eduardo Martins
 * 
 */
public abstract class RLSExampleRootSbb implements javax.slee.Sbb,
		RLSExampleRoot {

	String presenceDomain = System.getProperty("bind.address","127.0.0.1");
	String[] publishers = {"sip:alice@"+presenceDomain,"sip:bob@"+presenceDomain};
	
	/*
	 * service activation event, create subscription
	 */
	public void onServiceStartedEvent(
			javax.slee.serviceactivity.ServiceStartedEvent event,
			ActivityContextInterface aci) {

		
			tracer.info("Service activated...");
			try {
				((RLSExamplePublisherSbbLocalObject) getPublisherChildRelation().create(publishers[0])).start(publishers[0]);
			} catch (Exception e) {
				tracer.severe(e.getMessage(),e);
			}
		
	}

	public void publisherNotStarted(String publisher) {
		tracer.info("publisher didn't started "+publisher);		
	}
	
	public void publisherStarted(String publisher) {
		tracer.info("publisher started "+publisher);
		if (publisher.equals(publishers[0])) {
			// start the other publisher
			try {
				((RLSExamplePublisherSbbLocalObject) getPublisherChildRelation().create(publishers[1])).start(publishers[1]);
			} catch (Exception e) {
				tracer.severe(e.getMessage(),e);
			}
		}	
		else {
			try {
				((RLSExampleSubscriberSbbLocalObject) getSubscriberChildRelation().create(ChildRelationExt.DEFAULT_CHILD_NAME)).start(publishers);				
			} catch (Exception e) {
				tracer.severe(e.getMessage(),e);
			}
		}
	}
	
	public void subscriberNotStarted() {
		tracer.info("subscriber didn't started ");			
	}
	
	public void subscriberStarted() {
		tracer.info("subscriber started");	
		
	}
	
	public void subscriberStopped() {
		tracer.info("subscriber stopped");		
	}
		
	
	/**
	 * service deactivation, unsubscribe
	 * 
	 * @param event
	 * @param aci
	 */
	public void onActivityEndEvent(ActivityEndEvent event,
			ActivityContextInterface aci) {
		
		tracer.info("Service deactivated...");
		try {
			for(Object publisher : getPublisherChildRelation()) {
				((RLSExamplePublisherSbbLocalObject)publisher).stop();
			}
			RLSExampleSubscriberSbbLocalObject subscriber = (RLSExampleSubscriberSbbLocalObject) getSubscriberChildRelation().get(ChildRelationExt.DEFAULT_CHILD_NAME);
			if (subscriber != null) {
				subscriber.stop();
			}			
		} catch (Exception e) {
			tracer.severe("",e);
		}

	}

	// --- CHILDS
	
	public abstract ChildRelationExt getPublisherChildRelation();
	
	public abstract ChildRelationExt getSubscriberChildRelation();
	
	// --- SBB OBJECT
	
	/**
	 * Called when an sbb object is instantied and enters the pooled state.
	 */
	public void setSbbContext(SbbContext sbbContext) {
		tracer = sbbContext.getTracer("RLSExampleRootSbb");
	}

	public void unsetSbbContext() {
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