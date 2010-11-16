package org.mobicents.slee.sipevent.examples;

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

import org.mobicents.slee.sipevent.server.publication.PublicationClientControlParent;
import org.mobicents.slee.sipevent.server.publication.PublicationClientControlParentSbbLocalObject;
import org.mobicents.slee.sipevent.server.publication.PublicationClientControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.publication.Result;

/**
 * Example of an application that uses
 * {@link PublicationClientControlSbbLocalObject} as a child sbb, and implements
 * {@link PublicationClientControlParentSbbLocalObject}, to interact with the
 * Mobicents SIP Event Publication service.
 * 
 * @author Eduardo Martins
 * 
 */
public abstract class InternalPublisherExampleSbb implements javax.slee.Sbb,
		PublicationClientControlParent {

	String presenceDomain = System.getProperty("bind.address","127.0.0.1");
	String entity = "sip:internal-publisher@" + presenceDomain;
	String eventPackage = "presence";
	String contentType = "application";
	String contentSubType = "pidf+xml";
	String document = 
		"<?xml version='1.0' encoding='UTF-8'?>" +
		"<presence xmlns='urn:ietf:params:xml:ns:pidf' xmlns:dm='urn:ietf:params:xml:ns:pidf:data-model' xmlns:rpid='urn:ietf:params:xml:ns:pidf:rpid' xmlns:c='urn:ietf:params:xml:ns:pidf:cipid' entity='sip:internal-publisher@"+presenceDomain+"'>" +
			"<tuple id='t54bb0569'><status><basic>open</basic></status></tuple>" +
			"<dm:person id='p65f3307a'>" +
				"<rpid:activities><rpid:busy/></rpid:activities>" +
				"<dm:note>Busy</dm:note>" +
			"</dm:person>" +
		"</presence>";
	int expires = 300;

	// --- INTERNAL CHILD SBB

	public abstract ChildRelation getPublicationControlChildRelation();

	private PublicationClientControlSbbLocalObject getPublicationControlChildSbb()
			throws TransactionRequiredLocalException, SLEEException,
			CreateException {
		final ChildRelation childRelation = getPublicationControlChildRelation();
		if (childRelation.isEmpty()) {
			return (PublicationClientControlSbbLocalObject) childRelation.create();
		}
		else {
			return (PublicationClientControlSbbLocalObject) childRelation.iterator().next();
		}
	}

	// --- ETAG CMP

	public abstract void setETag(String eTag);

	public abstract String getETag();

	/*
	 * service activation event, publish initial state
	 */
	public void onServiceStartedEvent(
			javax.slee.serviceactivity.ServiceStartedEvent event,
			ActivityContextInterface aci) {
		
		tracer.info("Service activated, publishing state...");
		try {
			final Result result = getPublicationControlChildSbb().newPublication(
					entity, eventPackage, document,
					contentType, contentSubType, expires);
			if (result.getStatusCode() == 200) {
				tracer.info("publication ok: eTag=" + result.getETag());
				// save etag in cmp
				setETag(result.getETag());
				// set refresh timer
				timerFacility.setTimer(aci, null, System.currentTimeMillis() + expires
						* 1000, expires * 1000, 0, new TimerOptions());
			}
			else {
				tracer.info("error on mew publication: error=" + result.getStatusCode());
			}
		} catch (Exception e) {
			tracer.severe("failed to create publication",e);
		}		
	}

	public void onTimerEvent(TimerEvent event, ActivityContextInterface aci) {
		// refresh publication
		try {
			final Result result = getPublicationControlChildSbb().refreshPublication(entity, eventPackage, getETag(),
					expires);
			if (result.getStatusCode() == 200) {
				tracer.info("refreshed publication ok: eTag=" + result.getETag() + ",expires=" + result.getExpires());
				// update tag in cmp, it changes on refreshes too
				setETag(result.getETag());
			}
			else {
				tracer.info("error when refreshing publication: error=" + result.getStatusCode());
			}
		} catch (Exception e) {
			tracer.severe("failed to refresh publication",e);
		}
	}

	/**
	 * service deactivation, remove published state
	 * 
	 * @param event
	 * @param aci
	 */
	public void onActivityEndEvent(ActivityEndEvent event,
			ActivityContextInterface aci) {
		if (getETag() != null) {
			tracer.info("Service deactivated, removing publication...");
			try {
				getPublicationControlChildSbb().removePublication(entity, eventPackage, getETag());
			} catch (Exception e) {
				tracer.severe("failed to remove publication",e);
			}
		} else {
			tracer.info("Service deactivated, no published state to remove.");
		}
	}

	// --- SBB OBJECT LIFECYCLE

	private TimerFacility timerFacility = null;

	/**
	 * Called when an sbb object is instantied and enters the pooled state.
	 */
	public void setSbbContext(SbbContext sbbContext) {
		tracer = sbbContext.getTracer(getClass().getSimpleName());
		try {
			timerFacility = (TimerFacility) new InitialContext().lookup(TimerFacility.JNDI_NAME);
		} catch (Exception e) {
			tracer.severe("Unable to timer facility",e);
		}
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