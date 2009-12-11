package org.mobicents.slee.sippresence.server.subscription;

import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
import org.mobicents.slee.xdm.server.XDMClientControlParentSbbLocalObject;

/**
 * Extending the mandatory interfaces with methods needed by {@link PresenceSubscriptionControl}
 * @author martins
 *
 */
public interface PresenceSubscriptionControlSbbLocalObject extends
		XDMClientControlParentSbbLocalObject,
		ImplementedSubscriptionControlSbbLocalObject, PresenceSubscriptionControlSbbInterface {

}
