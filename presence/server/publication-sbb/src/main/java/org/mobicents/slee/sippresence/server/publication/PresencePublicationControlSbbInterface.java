package org.mobicents.slee.sippresence.server.publication;

import javax.naming.NamingException;
import javax.sip.header.HeaderFactory;

import org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl;
import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlParent;

public interface PresencePublicationControlSbbInterface extends ImplementedPublicationControl {

	public HeaderFactory getHeaderFactory() throws NamingException;

	public ImplementedSubscriptionControlParent getPresenceSubscriptionControl();

}
