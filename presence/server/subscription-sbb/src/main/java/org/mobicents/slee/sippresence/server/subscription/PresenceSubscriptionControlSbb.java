package org.mobicents.slee.sippresence.server.subscription;

import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sip.ServerTransaction;
import javax.sip.header.HeaderFactory;
import javax.slee.ActivityContextInterface;
import javax.slee.ChildRelation;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.SbbLocalObject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.java.slee.resource.sip.SleeSipProvider;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.publication.PublicationControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlParentSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.NotifyContent;
import org.mobicents.slee.sipevent.server.subscription.data.Notifier;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.mobicents.slee.sippresence.server.jmx.SipPresenceServerManagement;
import org.mobicents.slee.sippresence.server.presrulescache.PresRulesActivityContextInterfaceFactory;
import org.mobicents.slee.sippresence.server.presrulescache.PresRulesSbbInterface;
import org.mobicents.slee.sippresence.server.presrulescache.RulesetUpdatedEvent;

/**
 * Implemented Subscription control child sbb for a SIP Presence Server.
 * 
 * @author eduardomartins
 * 
 */
public abstract class PresenceSubscriptionControlSbb implements Sbb,
		PresenceSubscriptionControlSbbInterface {

	private static Logger logger = Logger
			.getLogger(PresenceSubscriptionControlSbb.class);

	private static final SipPresenceServerManagement configuration = SipPresenceServerManagement.getInstance();
	private static final PresenceSubscriptionControl presenceSubscriptionControl = new PresenceSubscriptionControl();
	
	/**
	 * JAIN-SIP provider & factories
	 * 
	 * @return
	 */
	// private SipActivityContextInterfaceFactory
	// sipActivityContextInterfaceFactory;
	protected SleeSipProvider sipProvider;
	// private AddressFactory addressFactory;
	// private MessageFactory messageFactory;
	protected HeaderFactory headerFactory;
	
	protected PresRulesSbbInterface presRulesSbbInterface;
	protected PresRulesActivityContextInterfaceFactory presRulesACIF;
	
	/**
	 * SbbObject's sbb context
	 */
	private SbbContext sbbContext;

	public void setSbbContext(SbbContext sbbContext) {
		this.sbbContext = sbbContext;
		// retrieve factories, facilities & providers
		try {
			Context context = (Context) new InitialContext()
					.lookup("java:comp/env");
			// sipActivityContextInterfaceFactory =
			// (SipActivityContextInterfaceFactory) context
			// .lookup("slee/resources/jainsip/1.2/acifactory");
			sipProvider = (SleeSipProvider) context
					.lookup("slee/resources/jainsip/1.2/provider");
			// addressFactory = sipProvider.getAddressFactory();
			headerFactory = sipProvider.getHeaderFactory();
			// messageFactory = sipProvider.getMessageFactory();
			presRulesSbbInterface = (PresRulesSbbInterface) context
				.lookup("slee/resources/presence/presrulescache/1.0/sbbinterface");
			presRulesACIF = (PresRulesActivityContextInterfaceFactory) context
				.lookup("slee/resources/presence/presrulescache/1.0/acif");
		} catch (NamingException e) {
			logger.error("Can't set sbb context.", e);
		}
	}

	// ------------ ImplementedSubscriptionControlSbbLocalObject

	public boolean acceptsEventList() { 
		return true;
	};
	
	public abstract ImplementedSubscriptionControlParentSbbLocalObject getParentSbbCMP();

	public abstract void setParentSbbCMP(
			ImplementedSubscriptionControlParentSbbLocalObject sbbLocalObject);

	public void setParentSbb(
			ImplementedSubscriptionControlParentSbbLocalObject sbbLocalObject) {
		setParentSbbCMP(sbbLocalObject);
	}

	public String[] getEventPackages() {
		return PresenceSubscriptionControl.getEventPackages();
	}

	public void isSubscriberAuthorized(String subscriber,
			String subscriberDisplayName, Notifier notifier, SubscriptionKey key,
			int expires, String content, String contentType,
			String contentSubtype, boolean eventList, ServerTransaction serverTransaction) {
		
		presenceSubscriptionControl.isSubscriberAuthorized(
				subscriber, subscriberDisplayName, notifier, key, expires,
				content, contentType, contentSubtype, eventList, configuration.getPresRulesAUID(),
				configuration.getPresRulesDocumentName(),serverTransaction, this);
	}

	public void removingSubscription(Subscription subscription) {
		presenceSubscriptionControl.removingSubscription(
				subscription, configuration.getPresRulesAUID(), configuration.getPresRulesDocumentName(),this);
	}

	public NotifyContent getNotifyContent(Subscription subscription) {
		return presenceSubscriptionControl
				.getNotifyContent(subscription,this);
	}

	public Object filterContentPerSubscriber(String subscriber,
			Notifier notifier, String eventPackage, Object unmarshalledContent) {
		return presenceSubscriptionControl
				.filterContentPerSubscriber(subscriber, notifier, eventPackage,
						unmarshalledContent);
	}

	public void onRulesetUpdatedEvent(RulesetUpdatedEvent event, ActivityContextInterface aci) {
		presenceSubscriptionControl.rulesetUpdated(event.getDocumentSelector(),event.getRuleset(),this);
	}
	
	public Marshaller getMarshaller() {
		try {
			return jaxbContext.createMarshaller();
		} catch (JAXBException e) {
			logger.error("failed to create marshaller", e);
			return null;
		}
	}
	
	public PresRulesActivityContextInterfaceFactory getPresRulesACIF() {
		return presRulesACIF;
	}
	
	public PresRulesSbbInterface getPresRulesSbbInterface() {
		return presRulesSbbInterface;
	}
	
	@Override
	public SbbLocalObject getSbbLocalObject() {
		return sbbContext.getSbbLocalObject();
	}
	
	// ------------ PresenceSubscriptionControlSbbLocalObject

	// --- PUBLICATION CHILD SBB
	public abstract ChildRelation getPublicationControlChildRelation();

	public abstract PublicationControlSbbLocalObject getPublicationControlChildSbbCMP();

	public abstract void setPublicationControlChildSbbCMP(
			PublicationControlSbbLocalObject value);

	public PublicationControlSbbLocalObject getPublicationChildSbb() {
		PublicationControlSbbLocalObject childSbb = getPublicationControlChildSbbCMP();
		if (childSbb == null) {
			try {
				childSbb = (PublicationControlSbbLocalObject) getPublicationControlChildRelation()
						.create();
			} catch (Exception e) {
				logger.error("Failed to create child sbb", e);
				return null;
			}
			setPublicationControlChildSbbCMP(childSbb);
		}
		return childSbb;
	}

	// --- COMBINED RULES CMP
	public abstract void setCombinedRules(HashMap rules);

	public abstract HashMap getCombinedRules();

	public HeaderFactory getHeaderFactory() {
		return headerFactory;
	}

	public Unmarshaller getUnmarshaller() {
		try {
			return jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			logger.error("failed to create unmarshaller", e);
			return null;
		}
	}

	// ---------- PublishedSphereSource
	/**
	 * interface used by rules processor to get sphere for a notifier
	 */
	public String getSphere(String notifier) {
		return presenceSubscriptionControl.getSphere(notifier,this);
	}

	// --------- JAXB

	/*
	 * JAXB context is thread safe
	 */
	private static final JAXBContext jaxbContext = initJAXBContext();

	private static JAXBContext initJAXBContext() {
		try {
			return JAXBContext
					.newInstance(configuration.getJaxbPackageNames());
		} catch (JAXBException e) {
			logger.error("failed to create jaxb context");
			return null;
		}
	}

	// ----------- SBB OBJECT's LIFE CYCLE

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
		this.sbbContext = null;
	}

}
