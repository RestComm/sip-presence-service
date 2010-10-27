package org.mobicents.slee.xdm.server.subscription;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sip.ServerTransaction;
import javax.sip.header.HeaderFactory;
import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.java.slee.resource.sip.SleeSipProvider;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlParentSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.NotifyContent;
import org.mobicents.slee.sipevent.server.subscription.data.Notifier;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.openxdm.xcap.server.slee.resource.datasource.AttributeUpdatedEvent;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceActivityContextInterfaceFactory;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceSbbInterface;
import org.openxdm.xcap.server.slee.resource.datasource.DocumentUpdatedEvent;
import org.openxdm.xcap.server.slee.resource.datasource.ElementUpdatedEvent;

/**
 * Subscription control sbb for a XDM Server.
 * 
 * @author eduardomartins
 * 
 */
public abstract class XcapDiffSubscriptionControlSbb implements Sbb,
		XcapDiffSubscriptionControlSbbInterface {

	private static Logger logger = Logger
			.getLogger(XcapDiffSubscriptionControlSbb.class);

	private DataSourceSbbInterface dataSourceSbbInterface = null;
	private DataSourceActivityContextInterfaceFactory dataSourceACIF = null;

	private static final XcapDiffSubscriptionControl XCAP_DIFF_SUBSCRIPTION_CONTROL = new XcapDiffSubscriptionControl();
	
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
			// appUsageCache = (AppUsageCacheResourceAdaptorSbbInterface)
			// context.lookup("slee/resources/xdm/appusagecache/sbbrainterface");
			dataSourceSbbInterface = (DataSourceSbbInterface) context
					.lookup("slee/resources/xdm/datasource/sbbrainterface");
			dataSourceACIF = (DataSourceActivityContextInterfaceFactory)
				context.lookup("slee/resources/xdm/datasource/1.0/acif");
		} catch (NamingException e) {
			logger.error("Can't set sbb context.", e);
		}
	}

	// ------------ ImplementedSubscriptionControlSbbLocalObject

	public boolean acceptsEventList() { 
		return false;
	};
	
	public abstract ImplementedSubscriptionControlParentSbbLocalObject getParentSbbCMP();

	public abstract void setParentSbbCMP(
			ImplementedSubscriptionControlParentSbbLocalObject sbbLocalObject);

	public void setParentSbb(
			ImplementedSubscriptionControlParentSbbLocalObject sbbLocalObject) {
		setParentSbbCMP(sbbLocalObject);
	}

	public String[] getEventPackages() {
		return XcapDiffSubscriptionControl.getEventPackages();
	}

	public void isSubscriberAuthorized(String subscriber,
			String subscriberDisplayName, Notifier notifier, SubscriptionKey key,
			int expires, String content, String contentType,
			String contentSubtype, boolean eventList,ServerTransaction serverTransaction) {
		// exposing to XcapDiffSubscriptionControl
		XCAP_DIFF_SUBSCRIPTION_CONTROL.isSubscriberAuthorized(
				subscriber, subscriberDisplayName, notifier, key, expires,
				content, contentType, contentSubtype,eventList,serverTransaction, this);
	}

	public void removingSubscription(Subscription subscription) {
		// delegate to XcapDiffSubscriptionControl
		XCAP_DIFF_SUBSCRIPTION_CONTROL
				.removingSubscription(subscription, this);
	}

	public NotifyContent getNotifyContent(Subscription subscription) {
		// delegate to XcapDiffSubscriptionControl
		return XCAP_DIFF_SUBSCRIPTION_CONTROL
				.getNotifyContent(subscription, this);
	}

	public Object filterContentPerSubscriber(String subscriber,
			Notifier notifier, String eventPackage, Object unmarshalledContent) {
		// delegate to XcapDiffSubscriptionControl
		return XCAP_DIFF_SUBSCRIPTION_CONTROL
				.filterContentPerSubscriber(subscriber, notifier, eventPackage,
						unmarshalledContent);
	}

	public Marshaller getMarshaller() {
		try {
			return jaxbContext.createMarshaller();
		} catch (JAXBException e) {
			logger.error("failed to create marshaller", e);
			return null;
		}
	}

	// ------------ XcapDiffSubscriptionControlSbbLocalObject

	// --- CMP

	/*
	 * 
	 */
	public abstract void setSubscriptionsMap(SubscriptionsMap map);

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.subscription.XcapDiffSubscriptionControlSbbInterface#getSubscriptionsMap()
	 */
	public abstract SubscriptionsMap getSubscriptionsMap();

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.subscription.XcapDiffSubscriptionControlSbbInterface#getDataSourceSbbInterface()
	 */
	public DataSourceSbbInterface getDataSourceSbbInterface() {
		return dataSourceSbbInterface;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.subscription.XcapDiffSubscriptionControlSbbInterface#getDataSourceActivityContextInterfaceFactory()
	 */
	public DataSourceActivityContextInterfaceFactory getDataSourceActivityContextInterfaceFactory() {
		return dataSourceACIF;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.subscription.XcapDiffSubscriptionControlSbbInterface#getSbbContext()
	 */
	public SbbContext getSbbContext() {
		return sbbContext;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.subscription.XcapDiffSubscriptionControlSbbInterface#getHeaderFactory()
	 */
	public HeaderFactory getHeaderFactory() {
		return headerFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.subscription.XcapDiffSubscriptionControlSbbInterface#getUnmarshaller()
	 */
	public Unmarshaller getUnmarshaller() {
		try {
			return jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			logger.error("failed to create unmarshaller", e);
			return null;
		}
	}

	// ------------ updates on xdm docs

	public void onAttributeUpdatedEvent(AttributeUpdatedEvent event,
			ActivityContextInterface aci) {
		XCAP_DIFF_SUBSCRIPTION_CONTROL.documentUpdated(event.getDocumentSelector(), event.getOldETag(), event.getNewETag(), event.getDocumentAsString(), this);		
	}

	public void onDocumentUpdatedEvent(DocumentUpdatedEvent event,
			ActivityContextInterface aci) {
		XCAP_DIFF_SUBSCRIPTION_CONTROL.documentUpdated(event.getDocumentSelector(), event.getOldETag(), event.getNewETag(), event.getDocumentAsString(), this);
	}

	public void onElementUpdatedEvent(ElementUpdatedEvent event,
			ActivityContextInterface aci) {
		XCAP_DIFF_SUBSCRIPTION_CONTROL.documentUpdated(event.getDocumentSelector(), event.getOldETag(), event.getNewETag(), event.getDocumentAsString(), this);
	}

	// --------- JAXB

	/*
	 * JAXB context is thread safe
	 */
	private static final JAXBContext jaxbContext = initJAXBContext();

	private static JAXBContext initJAXBContext() {
		try {
			return JAXBContext.newInstance("org.openxdm.xcap.common.xcapdiff"
					+ ":org.openxdm.xcap.client.appusage.resourcelists.jaxb");
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
	}
}