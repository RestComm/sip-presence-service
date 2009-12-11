package org.mobicents.slee.sippresence.client;

import javax.slee.SbbLocalObject;

/**
 * Interface that needs to be implemented by an sbb that uses
 * {@link PresenceClientControlSbbLocalObject} in a child relation. This
 * interface will be used for callbacks from the child to the parent sbb.
 * 
 * @author martins
 * 
 */
public interface PresenceClientControlParentSbbLocalObject extends
		SbbLocalObject,PresenceClientControlParent {

}
