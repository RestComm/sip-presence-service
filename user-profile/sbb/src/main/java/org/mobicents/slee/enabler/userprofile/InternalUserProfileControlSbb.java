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

package org.mobicents.slee.enabler.userprofile;

import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;

import org.mobicents.slee.enabler.userprofile.jpa.jmx.UserProfileControlManagement;

public abstract class InternalUserProfileControlSbb implements Sbb,UserProfileControl {
	
	/**
	 * Called when an sbb object is created and enters the pooled state.
	 */
	public void setSbbContext(SbbContext sbbContext) {
		
	}

	// -- MANAGEMENT
	
	/**
	 * the Management MBean
	 */
	private static final UserProfileControlManagement management = UserProfileControlManagement.getInstance();
		
	// -- SBB LOCAL OBJECT METHODS

	public UserProfile find(String username) {
		
		org.mobicents.slee.enabler.userprofile.jpa.UserProfile jpaUserProfile = management.getUser(username);
		if (jpaUserProfile != null) {
			return new UserProfile(jpaUserProfile);
		}
		else {
		 	return null;
		}
		
	}

	// SBB OBJECT LIFECYCLE METHODS

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
	}

}
