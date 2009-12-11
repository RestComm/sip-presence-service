/**
 * 
 */
package org.mobicents.slee.sippresence.server.subscription;

import java.util.HashMap;

import javax.sip.header.HeaderFactory;
import javax.xml.bind.Unmarshaller;

import org.mobicents.slee.sipevent.server.publication.PublicationControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControl;
import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlParentSbbLocalObject;
import org.mobicents.slee.sippresence.server.subscription.rules.PublishedSphereSource;
import org.mobicents.slee.xdm.server.XDMClientControlParent;
import org.mobicents.slee.xdm.server.XDMClientControlSbbLocalObject;

/**
 * @author martins
 *
 */
public interface PresenceSubscriptionControlSbbInterface extends XDMClientControlParent,
ImplementedSubscriptionControl, PublishedSphereSource {

	public HashMap getCombinedRules();

	public void setCombinedRules(HashMap combinedRules);

	public ImplementedSubscriptionControlParentSbbLocalObject getParentSbbCMP();

	public XDMClientControlSbbLocalObject getXDMClientControlSbb();

	public PublicationControlSbbLocalObject getPublicationChildSbb();

	public HeaderFactory getHeaderFactory();

	public Unmarshaller getUnmarshaller();
	
}
