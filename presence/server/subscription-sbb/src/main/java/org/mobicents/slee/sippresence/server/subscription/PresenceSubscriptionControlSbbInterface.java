/**
 * 
 */
package org.mobicents.slee.sippresence.server.subscription;

import java.util.HashMap;

import javax.sip.header.HeaderFactory;
import javax.slee.SbbLocalObject;
import javax.xml.bind.Unmarshaller;

import org.mobicents.slee.sipevent.server.publication.PublicationControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControl;
import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlParentSbbLocalObject;
import org.mobicents.slee.sippresence.server.presrulescache.PresRulesActivityContextInterfaceFactory;
import org.mobicents.slee.sippresence.server.presrulescache.PresRulesSbbInterface;
import org.mobicents.slee.sippresence.server.subscription.rules.PublishedSphereSource;

/**
 * @author martins
 *
 */
public interface PresenceSubscriptionControlSbbInterface extends ImplementedSubscriptionControl, PublishedSphereSource {

	public HashMap getCombinedRules();

	public void setCombinedRules(HashMap combinedRules);

	public ImplementedSubscriptionControlParentSbbLocalObject getParentSbbCMP();

	public PresRulesActivityContextInterfaceFactory getPresRulesACIF();
	
	public PresRulesSbbInterface getPresRulesSbbInterface();

	public PublicationControlSbbLocalObject getPublicationChildSbb();

	public SbbLocalObject getSbbLocalObject();
	
	public HeaderFactory getHeaderFactory();

	public Unmarshaller getUnmarshaller();
	
}
