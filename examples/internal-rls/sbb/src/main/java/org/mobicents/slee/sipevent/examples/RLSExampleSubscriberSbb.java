package org.mobicents.slee.sipevent.examples;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.slee.ActivityContextInterface;
import javax.slee.ChildRelation;
import javax.slee.RolledBackContext;
import javax.slee.SbbContext;
import javax.slee.facilities.TimerEvent;
import javax.slee.facilities.TimerFacility;
import javax.slee.facilities.TimerOptions;
import javax.slee.facilities.TimerPreserveMissed;
import javax.slee.facilities.Tracer;
import javax.slee.nullactivity.NullActivity;
import javax.slee.nullactivity.NullActivityContextInterfaceFactory;
import javax.slee.nullactivity.NullActivityFactory;
import javax.xml.bind.JAXBContext;

import org.mobicents.slee.enabler.userprofile.jpa.jmx.UserProfileControlManagement;
import org.mobicents.slee.enabler.xdmc.XDMClientChildSbbLocalObject;
import org.mobicents.slee.enabler.xdmc.XDMClientParentSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Event;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Status;
import org.mobicents.slee.sippresence.client.PresenceClientControlParentSbbLocalObject;
import org.mobicents.slee.sippresence.client.PresenceClientControlSbbLocalObject;
import org.mobicents.slee.xdm.server.ServerConfiguration;
import org.mobicents.xcap.client.uri.DocumentSelectorBuilder;
import org.mobicents.xcap.client.uri.UriBuilder;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ListType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType.DisplayName;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.ObjectFactory;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.PackagesType;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.RlsServices;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.ServiceType;

/**
 * 
 * @author Eduardo Martins
 * 
 */
public abstract class RLSExampleSubscriberSbb implements javax.slee.Sbb,
	RLSExampleSubscriber {

	private String presenceDomain = System.getProperty("bind.address","127.0.0.1");
	private String subscriber = "sip:carol@"+presenceDomain;
	private String notifier = "sip:carol@"+presenceDomain+";pres-list=Default";
	private String notifierPassword = "password";
	private String eventPackage = "presence";
	private int expires = 300;
	private URI uri = buildRLSServicesURI();
	
	// --- PRESENCE CLIENT CHILD SBB

	public abstract ChildRelation getPresenceClientControlSbbChildRelation();

	private URI buildRLSServicesURI() {
		String documentSelector = DocumentSelectorBuilder.getUserDocumentSelectorBuilder("rls-services",notifier,"index").toPercentEncodedString();
		UriBuilder uriBuilder = new UriBuilder()
			.setSchemeAndAuthority(ServerConfiguration.getInstance().getSchemeAndAuthority())
			.setXcapRoot(ServerConfiguration.getInstance().getXcapRoot())
			.setDocumentSelector(documentSelector);
		try {
			return uriBuilder.toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	private PresenceClientControlSbbLocalObject getPresenceClientControlSbb() {
		
		final ChildRelation childRelation = getPresenceClientControlSbbChildRelation();
		if (childRelation.isEmpty()) {
			try {
				PresenceClientControlSbbLocalObject sbb = (PresenceClientControlSbbLocalObject) childRelation.create();
				sbb.setParentSbb((PresenceClientControlParentSbbLocalObject) sbbContext.getSbbLocalObject());
				return sbb;
			} catch (Exception e) {
				tracer.severe("Failed to create child sbb", e);
				return null;
			}
		}
		else {
			return (PresenceClientControlSbbLocalObject) childRelation.iterator().next();
		}
	}

	// --- XDM CLIENT CHILD SBB
	
	public abstract ChildRelation getXDMClientChildRelation();

	public XDMClientChildSbbLocalObject getXDMClientChildSbb() {
		final ChildRelation childRelation = getXDMClientChildRelation();
		if (childRelation.isEmpty()) {
			try {
				XDMClientChildSbbLocalObject sbb = (XDMClientChildSbbLocalObject) childRelation.create();
				sbb.setParentSbb((XDMClientParentSbbLocalObject) sbbContext.getSbbLocalObject());
				return sbb;
			} catch (Exception e) {
				tracer.severe("Failed to create child sbb", e);
				return null;
			}
		}
		else {
			return (XDMClientChildSbbLocalObject) childRelation.iterator().next();
		}
	}
	
	// --- CMPs

	public abstract void setParentSbbCMP(RLSExampleSubscriberParentSbbLocalObject value);

	public abstract RLSExampleSubscriberParentSbbLocalObject getParentSbbCMP();
	
	// --- SBB LOCAL OBJECT
	
	public void setParentSbb(RLSExampleSubscriberParentSbbLocalObject parentSbb) {
		setParentSbbCMP(parentSbb);
	}
	
	private EntryType createEntryType(String uri) {
		EntryType entryType = new EntryType();
		entryType.setUri(uri);
		DisplayName displayName = new EntryType.DisplayName();
		displayName.setValue(uri);
		entryType.setDisplayName(displayName);
		return entryType;
	}
	
	private String getRlsServices(String[] entryURIs) {
		StringWriter stringWriter = new StringWriter();
		try {			
			JAXBContext context = JAXBContext.newInstance("org.openxdm.xcap.client.appusage.rlsservices.jaxb");
			ListType listType = new ListType();
			for (String entryURI : entryURIs) {
				listType.getListOrExternalOrEntry().add(createEntryType(entryURI));
			}
			ServiceType serviceType = new ServiceType();
			serviceType.setList(listType);
			PackagesType packagesType = new PackagesType();
			packagesType.getPackageAndAny().add(new ObjectFactory().createPackagesTypePackage(eventPackage));
			serviceType.setPackages(packagesType);
			serviceType.setUri(notifier);
			RlsServices rlsServices = new RlsServices();
			rlsServices.getService().add(serviceType);
			context.createMarshaller().marshal(rlsServices, stringWriter);
			return stringWriter.toString();			
		} catch (Exception e) {
			tracer.severe("failed to read rls-services.xml",e);
		}
		finally {		
			try {
				stringWriter.close();
			} catch (IOException e) {
				tracer.severe(e.getMessage(),e);
			}
		}
		return null;
	}
		
	public void start(String[] entryURIs) {
		try {
			UserProfileControlManagement.getInstance().addUser(notifier,notifierPassword);
			XDMClientChildSbbLocalObject xdm = getXDMClientChildSbb();
			xdm.put(uri, "application/rls-services+xml", getRlsServices(entryURIs).getBytes("UTF-8"),notifier);			
		} catch (Exception e) {
			tracer.severe(e.getMessage(), e);
			getParentSbbCMP().subscriberNotStarted();
		}
	}

	private String getSubscriptionId() {
		return "rls-example~"+subscriber+"~"+notifier+"~"+eventPackage;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.enabler.xdmc.XDMClientParent#putResponse(java.net.URI, int, java.lang.String, java.lang.String)
	 */
	@Override
	public void putResponse(URI uri, int responseCode, String responseContent,
			String eTag) {
		tracer.info("Response to the insertion of the rls services document: status="+responseCode+",content="+responseContent);
		if (responseCode != 200 && responseCode != 201) {			
			getParentSbbCMP().subscriberNotStarted();
		}
		else {			
			// now subscribe the presence of it
			getPresenceClientControlSbb().newSubscription(subscriber, "...", notifier, eventPackage, getSubscriptionId(), expires);			
		}
	}
		
	public void newSubscriptionOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires,
			int responseCode) {
		
		tracer.info("subscribe ok: responseCode=" + responseCode + ",expires="
				+ expires);
		try {
			// let's set a periodic timer in a null activity to refresh the
			// publication
			TimerOptions timerOptions = new TimerOptions();
			timerOptions.setPreserveMissed(TimerPreserveMissed.ALL);

			NullActivity nullActivity = nullActivityFactory.createNullActivity();
			ActivityContextInterface aci = nullACIFactory.getActivityContextInterface(nullActivity);
			aci.attach(this.sbbContext.getSbbLocalObject());
			timerFacility.setTimer(aci, null, System.currentTimeMillis() + (expires-1)
					* 1000, (expires-1) * 1000, 0, timerOptions);

			getParentSbbCMP().subscriberStarted();
		}
		catch (Exception e) {
			tracer.severe(e.getMessage(),e);
		}
		
	}
	
	private void deleteRlsServices() {
		try {
			XDMClientChildSbbLocalObject xdm = getXDMClientChildSbb();
			xdm.delete(uri,notifier);			
		} catch (Exception e) {
			tracer.severe(e.getMessage(), e);			
		}
		UserProfileControlManagement.getInstance().removeUser(notifier);
	}
	
	public void newSubscriptionError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		tracer.info("error on subscribe: error=" + error);
		deleteRlsServices();		
	}
	
	public void notifyEvent(String subscriber, String notifier,
			String eventPackage, String subscriptionId,
			Event terminationReason, Status status, String content,
			String contentType, String contentSubtype) {
		String notification = "\nNOTIFY EVENT:" + "\n+-- Subscriber: "
		+ subscriber + "\n+-- Notifier: " + notifier
		+ "\n+-- EventPackage: " + eventPackage
		+ "\n+-- SubscriptionId: " + subscriptionId				
		+ "\n+-- Subscription status: " + status
		+ "\n+-- Subscription terminationReason: " + terminationReason
		+ "\n+-- Content Type: " + contentType + '/' + contentSubtype
		+ "\n+-- Content:\n\n" + content;
		tracer.info(notification);
		if (status == Subscription.Status.terminated && terminationReason != null && terminationReason == Subscription.Event.deactivated) {
			tracer.info("The subscription was deactivated, re-subscribing");
			// re-subscribe
			getPresenceClientControlSbb().newSubscription(subscriber, "...", notifier, eventPackage, getSubscriptionId(), expires);
		}
	}
	
	public void onTimerEvent(TimerEvent event, ActivityContextInterface aci) {
		// refresh subscription
		getPresenceClientControlSbb().refreshSubscription(subscriber, notifier, eventPackage, getSubscriptionId(), expires);
	}

	public void refreshSubscriptionOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires) {
		tracer.info("resubscribe Ok : expires=" + expires);

	}

	public void refreshSubscriptionError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		tracer.info("error on resubscribe: error=" + error);
		deleteRlsServices();
	}

	public void stop() {
		getPresenceClientControlSbb().removeSubscription(subscriber, notifier, eventPackage, getSubscriptionId());
		deleteRlsServices();
	}
	
	public void removeSubscriptionError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		tracer.info("error on unsubscribe: error=" + error);		
	}
	
	public void removeSubscriptionOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId) {
		tracer.info("unsubscribe Ok");		
	}
	
	@Override
	public void deleteResponse(URI uri, int responseCode,
			String responseContent, String eTag) {
		getParentSbbCMP().subscriberStopped();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.enabler.xdmc.XDMClientParent#getResponse(java.net.URI, int, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void getResponse(URI uri, int responseCode, String mimetype, String content, String eTag) {};
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#modifyPublicationError(java.lang.Object, int)
	 */
	@Override
	public void modifyPublicationError(Object requestId, int error) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#modifyPublicationOk(java.lang.Object, java.lang.String, int)
	 */
	@Override
	public void modifyPublicationOk(Object requestId, String eTag, int expires) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#newPublicationError(java.lang.Object, int)
	 */
	@Override
	public void newPublicationError(Object requestId, int error) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#newPublicationOk(java.lang.Object, java.lang.String, int)
	 */
	@Override
	public void newPublicationOk(Object requestId, String eTag, int expires) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#refreshPublicationError(java.lang.Object, int)
	 */
	@Override
	public void refreshPublicationError(Object requestId, int error) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#refreshPublicationOk(java.lang.Object, java.lang.String, int)
	 */
	@Override
	public void refreshPublicationOk(Object requestId, String eTag, int expires) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#removePublicationError(java.lang.Object, int)
	 */
	@Override
	public void removePublicationError(Object requestId, int error) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#removePublicationOk(java.lang.Object)
	 */
	@Override
	public void removePublicationOk(Object requestId) {
		// TODO Auto-generated method stub
		
	}
		
	
	// --- SBB OBJECT

	private SbbContext sbbContext = null; // This SBB's context

	private TimerFacility timerFacility = null;
	private NullActivityContextInterfaceFactory nullACIFactory;
	private NullActivityFactory nullActivityFactory;

	/**
	 * Called when an sbb object is instantied and enters the pooled state.
	 */
	public void setSbbContext(SbbContext sbbContext) {

		this.sbbContext = sbbContext;
		tracer = sbbContext.getTracer("RLSExampleSubscriberSbb");
		try {
			Context context = (Context) new InitialContext()
					.lookup("java:comp/env");
			timerFacility = (TimerFacility) context
				.lookup("slee/facilities/timer");
			nullACIFactory = (NullActivityContextInterfaceFactory) context
				.lookup("slee/nullactivity/activitycontextinterfacefactory");
			nullActivityFactory = (NullActivityFactory) context
				.lookup("slee/nullactivity/factory");
		} catch (Exception e) {
			tracer.severe("Unable to retrieve factories, facilities & providers",
					e);
		}
	}

	public void unsetSbbContext() {
		this.sbbContext = null;
	}

	public void sbbCreate() throws javax.slee.CreateException {
	}

	public void sbbPostCreate() throws javax.slee.CreateException {
	}

	public void sbbActivate() {
	}

	public void sbbPassivate() {
	}

	public void sbbRemove() {
	}

	public void sbbLoad() {
	}

	public void sbbStore() {
	}

	public void sbbExceptionThrown(Exception exception, Object event,
			ActivityContextInterface activity) {
	}

	public void sbbRolledBack(RolledBackContext sbbRolledBack) {
	}

	private Tracer tracer;

}