package org.mobicents.slee.sipevent.server.subscription;

import javax.slee.SbbLocalObject;

/**
 * Call back interface for the parent sbb of the
 * {@link EventListSubscriptionControlSbbLocalObject}. Provides the responses to
 * the requests sent by the parent sbb and event notifications.
 * 
 * @author Eduardo Martins
 * 
 */
public interface EventListSubscriptionControlParentSbbLocalObject extends
		SbbLocalObject, EventListSubscriptionControlParent {

}
