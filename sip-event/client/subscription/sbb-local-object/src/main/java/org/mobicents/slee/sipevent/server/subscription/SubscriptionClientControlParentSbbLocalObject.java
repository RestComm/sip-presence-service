package org.mobicents.slee.sipevent.server.subscription;

import org.mobicents.slee.SbbLocalObjectExt;

/**
 * Call back interface for the parent sbb of the
 * {@link SubscriptionClientControlSbbLocalObject}. Provides the responses to
 * the requests sent by the parent sbb and event notifications.
 * 
 * @author Eduardo Martins
 * 
 */
public interface SubscriptionClientControlParentSbbLocalObject extends
		SbbLocalObjectExt,SubscriptionClientControlParent {

	

}
