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
