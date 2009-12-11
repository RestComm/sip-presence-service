package org.mobicents.slee.xdm.server.subscription;

import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
import org.mobicents.slee.xdm.server.XDMClientControlParentSbbLocalObject;

/**
 * Extending the mandatory interfaces with methods needed by
 * {@link XcapDiffSubscriptionControl}
 * 
 * @author martins
 * 
 */
public interface XcapDiffSubscriptionControlSbbLocalObject extends
		XcapDiffSubscriptionControlSbbInterface,XDMClientControlParentSbbLocalObject,
		ImplementedSubscriptionControlSbbLocalObject {

}
