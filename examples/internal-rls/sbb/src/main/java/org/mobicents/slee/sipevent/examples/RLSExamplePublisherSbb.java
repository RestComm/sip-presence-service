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

package org.mobicents.slee.sipevent.examples;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.SLEEException;
import javax.slee.SbbContext;
import javax.slee.SbbLocalObject;
import javax.slee.TransactionRequiredLocalException;
import javax.slee.facilities.TimerEvent;
import javax.slee.facilities.TimerFacility;
import javax.slee.facilities.TimerID;
import javax.slee.facilities.TimerOptions;
import javax.slee.facilities.TimerPreserveMissed;
import javax.slee.facilities.Tracer;
import javax.slee.nullactivity.NullActivity;
import javax.slee.nullactivity.NullActivityContextInterfaceFactory;
import javax.slee.nullactivity.NullActivityFactory;

import org.mobicents.slee.ActivityContextInterfaceExt;
import org.mobicents.slee.ChildRelationExt;
import org.mobicents.slee.SbbContextExt;
import org.mobicents.slee.sipevent.server.publication.PublicationClientControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.publication.Result;

/**
 * 
 * @author Eduardo Martins
 * 
 */
public abstract class RLSExamplePublisherSbb implements javax.slee.Sbb,
	RLSExamplePublisher {

	String eventPackage = "presence";
	String contentType = "application";
	String contentSubType = "pidf+xml";
	int expires = 300;

	private String getDocument(String publisher) {
		return  
		"<?xml version='1.0' encoding='UTF-8'?>" +
		"<presence xmlns='urn:ietf:params:xml:ns:pidf' xmlns:dm='urn:ietf:params:xml:ns:pidf:data-model' xmlns:rpid='urn:ietf:params:xml:ns:pidf:rpid' xmlns:c='urn:ietf:params:xml:ns:pidf:cipid' entity='"+publisher+"'>" +
			"<tuple id='t54bb0569'><status><basic>open</basic></status></tuple>" +
			"<dm:person id='p65f3307a'>" +
				"<rpid:activities><rpid:busy/></rpid:activities>" +
				"<dm:note>Busy</dm:note>" +
			"</dm:person>" +
		"</presence>";
	}
	
	// --- INTERNAL CHILD SBB

	public abstract ChildRelationExt getPublicationControlChildRelation();

	private PublicationClientControlSbbLocalObject getPublicationControlChildSbb()
			throws TransactionRequiredLocalException, SLEEException,
			CreateException {
		final ChildRelationExt childRelation = getPublicationControlChildRelation();
		PublicationClientControlSbbLocalObject child = (PublicationClientControlSbbLocalObject) childRelation.get(ChildRelationExt.DEFAULT_CHILD_NAME);
		if (child == null) {
			child = (PublicationClientControlSbbLocalObject) childRelation.create(ChildRelationExt.DEFAULT_CHILD_NAME);
		}
		return child;
	}

	protected RLSExamplePublisherParentSbbLocalObject getParent() {
		return (RLSExamplePublisherParentSbbLocalObject) sbbContext.getSbbLocalObject().getParent();		
	}
	
	// --- CMPs
	
	public abstract void setPublisher(String value);

	public abstract String getPublisher();
	
	public abstract void setETag(String eTag);

	public abstract String getETag();

	// --- SBB LOCAL OBJECT
	
	@Override
	public void start(String publisher) {
		
		setPublisher(publisher);
		
		try {
			final Result result = getPublicationControlChildSbb().newPublication(
					publisher, eventPackage, getDocument(publisher),
					contentType, contentSubType, expires);
			if (result.getStatusCode() == 200) {
				tracer.info("publication ok: eTag=" + result.getETag());
				// save etag in cmp
				setETag(result.getETag());
				// let's set a periodic timer in a null activity to refresh the
				// publication
				TimerOptions timerOptions = new TimerOptions();
				timerOptions.setPreserveMissed(TimerPreserveMissed.ALL);
				
				NullActivity nullActivity = nullActivityFactory.createNullActivity();
				ActivityContextInterface aci = nullACIFactory.getActivityContextInterface(nullActivity);
				aci.attach(this.sbbContext.getSbbLocalObject());
				timerFacility.setTimer(aci, null, System.currentTimeMillis() + (expires-5)
						* 1000, (expires-5) * 1000, 0, timerOptions);
				
				getParent().publisherStarted(publisher);
			}
			else {
				tracer.info("error on mew publication: error=" + result.getStatusCode());
				getParent().publisherNotStarted(publisher);
			}
		} catch (Exception e) {
			tracer.severe("failed to create publication",e);
			getParent().publisherNotStarted(publisher);
		}		
			
	}
	
	public void onTimerEvent(TimerEvent event, ActivityContextInterface aci) {
		// refresh publication
		String publisher = getPublisher();
		try {
			final Result result = getPublicationControlChildSbb().refreshPublication(publisher, eventPackage, getETag(),
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
	 * stop publishing
	 */
	@Override
	public void stop() {
		try {
			try {
				getPublicationControlChildSbb().removePublication(getPublisher(), eventPackage, getETag());
			} catch (Exception e) {
				tracer.severe("failed to remove publication",e);
			}
			SbbLocalObject sbbLocalObject = sbbContext.getSbbLocalObject();
			for(ActivityContextInterface aci : this.sbbContext.getActivities()) {
				aci.detach(sbbLocalObject);
				for(TimerID timerID : ((ActivityContextInterfaceExt)aci).getTimers()) {
					timerFacility.cancelTimer(timerID);
				}
			}
		} catch (Exception e) {
			tracer.severe(e.getMessage(),e);			
		}
	}
		
			
	// --- SBB OBJECT

	private SbbContextExt sbbContext = null; // This SBB's context

	private TimerFacility timerFacility = null;
	private NullActivityContextInterfaceFactory nullACIFactory;
	private NullActivityFactory nullActivityFactory;

	/**
	 * Called when an sbb object is instantied and enters the pooled state.
	 */
	public void setSbbContext(SbbContext sbbContext) {

		this.sbbContext = (SbbContextExt) sbbContext;
		tracer = sbbContext.getTracer("RLSExamplePublisherSbb");
		try {
			Context context = (Context) new InitialContext()
					.lookup("java:comp/env");
			timerFacility = (TimerFacility) context
				.lookup("slee/facilities/timer");
			nullACIFactory = (NullActivityContextInterfaceFactory) context
				.lookup("slee/nullactivity/activitycontextinterfacefactory");
			nullActivityFactory = (NullActivityFactory) context
				.lookup("slee/nullactivity/factory");
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