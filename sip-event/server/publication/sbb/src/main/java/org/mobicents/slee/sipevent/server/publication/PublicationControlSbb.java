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

package org.mobicents.slee.sipevent.server.publication;

import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.facilities.ActivityContextNamingFacility;
import javax.slee.facilities.TimerEvent;
import javax.slee.facilities.TimerFacility;
import javax.slee.facilities.TimerID;
import javax.slee.facilities.TimerOptions;
import javax.slee.facilities.TimerPreserveMissed;
import javax.slee.nullactivity.NullActivity;
import javax.slee.nullactivity.NullActivityContextInterfaceFactory;
import javax.slee.nullactivity.NullActivityFactory;

import org.mobicents.slee.ChildRelationExt;
import org.mobicents.slee.SbbContextExt;
import org.mobicents.slee.sipevent.server.publication.data.Publication;

/**
 * Sbb to control publication of sip events
 * 
 * @author Eduardo Martins
 * 
 */
public abstract class PublicationControlSbb extends AbstractPublicationControl implements Sbb {

	private static SLEEPublicationControlLogger logger;
	
	protected TimerFacility timerFacility;
	protected ActivityContextNamingFacility activityContextNamingfacility;
	protected NullActivityContextInterfaceFactory nullACIFactory;
	protected NullActivityFactory nullActivityFactory;

	/**
	 * SbbObject's sbb context
	 */
	protected SbbContextExt sbbContext;
	
	/**
	 * SbbObject's context setting
	 */
	public void setSbbContext(SbbContext sbbContext) {
		this.sbbContext = (SbbContextExt) sbbContext;
		if(logger == null) {
			logger = new SLEEPublicationControlLogger(sbbContext.getTracer("PublicationControlSbb"));
		}
		// retrieve factories, facilities & providers
		timerFacility = this.sbbContext.getTimerFacility();
		nullACIFactory = this.sbbContext.getNullActivityContextInterfaceFactory();
		nullActivityFactory = this.sbbContext.getNullActivityFactory();
		activityContextNamingfacility = this.sbbContext.getActivityContextNamingFacility();		
	}

	private static final TimerOptions timerOptions = createTimerOptions();

	@SuppressWarnings("deprecation")
	private static TimerOptions createTimerOptions() {
		TimerOptions options = new TimerOptions();
		options.setPersistent(true);
		options.setPreserveMissed(TimerPreserveMissed.ALL);
		return options;
	}

	// --- IMPL CHILD SBB

	public abstract ChildRelationExt getImplementedSbbChildRelation();

	protected ImplementedPublicationControlSbbLocalObject getImplementedPublicationControl() {
		final ChildRelationExt childRelation = getImplementedSbbChildRelation();
		ImplementedPublicationControlSbbLocalObject child = (ImplementedPublicationControlSbbLocalObject) childRelation.get(ChildRelationExt.DEFAULT_CHILD_NAME); 
		if (child == null) {
			try {
				child = (ImplementedPublicationControlSbbLocalObject) childRelation.create(ChildRelationExt.DEFAULT_CHILD_NAME);
			} catch (Exception e) {
				getLogger().error("Failed to create child sbb",e);
			}
		}
		return child;
	}	

	/**
	 * a timer has occurred in a dialog regarding a publication
	 * 
	 * @param event
	 * @param aci
	 */
	public void onTimerEvent(TimerEvent event, ActivityContextInterface aci) {
		// cancel current timer
		//timerFacility.cancelTimer(event.getTimerID());
		// detach from aci
		aci.detach(this.sbbContext.getSbbLocalObject());
		// end it
		((NullActivity) aci.getActivity()).endActivity();
		// delegate to abstract code
		timerExpired(event.getTimerID());
	}

	// ----------- IMPL OF ABSTRACT METHODS
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.AbstractPublicationControl#getLogger()
	 */
	@Override
	protected PublicationControlLogger getLogger() {
		return logger;
	}
		
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.AbstractPublicationControl#resetTimer(org.mobicents.slee.sipevent.server.publication.pojo.Publication, org.mobicents.slee.sipevent.server.publication.pojo.Publication, int)
	 */
	@Override
	protected void resetTimer(Publication publication,
			Publication newPublication, int expires) throws Exception {
		
		if (publication.getTimerID() != null) {
			// cancel current timer
			timerFacility.cancelTimer((TimerID) publication.getTimerID());
		}
		// get null aci
		ActivityContextInterface aci = activityContextNamingfacility
				.lookup(publication.getPublicationKey().toString());
		if (aci == null) {
			throw new IllegalStateException("Aborting, unable to find the publication's timer aci.");
		}
		// change aci name
		activityContextNamingfacility.unbind(publication
				.getPublicationKey().toString());
		activityContextNamingfacility.bind(aci, newPublication
				.getPublicationKey().toString());
		if (expires != -1) {
			// set timer with 5 sec more
			TimerID newTimerID = timerFacility
					.setTimer(aci, null, System.currentTimeMillis()
							+ ((expires + 5) * 1000), 1, 1,
							timerOptions);
			newPublication.setTimerID(newTimerID);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.AbstractPublicationControl#cancelTimer(org.mobicents.slee.sipevent.server.publication.pojo.Publication)
	 */
	@Override
	protected void cancelTimer(Publication publication) {
		if (publication.getTimerID() != null) {
			// cancel current timer
			timerFacility.cancelTimer((TimerID) publication.getTimerID());
		}
		// lookup timer aci
		final ActivityContextInterface aci = activityContextNamingfacility
				.lookup(publication.getPublicationKey().toString());
		if (aci != null) {
			// explictly end the null activity
			((NullActivity) aci.getActivity()).endActivity();
		}		
		else {
			throw new IllegalStateException("Aborting, unable to find the publication's timer aci.");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.AbstractPublicationControl#setTimer(org.mobicents.slee.sipevent.server.publication.pojo.Publication, int)
	 */
	@Override
	protected void setTimer(Publication publication, int expires) throws Exception {
		// create null aci
		NullActivity nullActivity = nullActivityFactory.createNullActivity();
		final ActivityContextInterface aci = nullACIFactory
				.getActivityContextInterface(nullActivity);
		// attach to aci
		aci.attach(sbbContext.getSbbLocalObject());
		// bind a name to the aci
		activityContextNamingfacility.bind(aci, publication
				.getPublicationKey().toString());
		if (expires != -1) {
			// set a timer for this publication and store it in the
			// publication pojo
			TimerID timerID = timerFacility.setTimer(aci, null, System
					.currentTimeMillis()
					+ ((expires + 5) * 1000), 1, 1, timerOptions);
			publication.setTimerID(timerID);
		}
	}
	
	
	// ----------- SBB OBJECT's LIFE CYCLE

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