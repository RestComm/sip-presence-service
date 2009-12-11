package org.mobicents.slee.xdm.server;

import javax.slee.SbbLocalObject;

/**
 * Client interface to interact with an XDM Server. IF used by an sbb in a child
 * relation, then that sbb's local object must implement
 * {@link XDMClientControlParentSbbLocalObject}.
 * 
 * @author martins
 * 
 */
public interface XDMClientControlSbbLocalObject extends SbbLocalObject, XDMClientControl {

	

}
