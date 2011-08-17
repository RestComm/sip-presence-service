/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.slee.sipevent.examples;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.slee.ActivityContextInterface;
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

import org.mobicents.slee.ChildRelationExt;
import org.mobicents.slee.SbbContextExt;
import org.mobicents.slee.enabler.sip.SubscriptionStatus;
import org.mobicents.slee.enabler.sip.TerminationReason;
import org.mobicents.slee.enabler.userprofile.jpa.jmx.UserProfileControlManagement;
import org.mobicents.slee.enabler.xdmc.XDMClientChildSbbLocalObject;
import org.mobicents.slee.enabler.xdmc.jaxb.xcapdiff.XcapDiff;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Event;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Status;
import org.mobicents.slee.xdm.server.ServerConfiguration;
import org.mobicents.xcap.client.uri.DocumentSelectorBuilder;
import org.mobicents.xcap.client.uri.UriBuilder;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType.DisplayName;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ListType;
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

	public abstract ChildRelationExt getSubscriptionControlChildRelation();

	private SubscriptionClientControlSbbLocalObject getSubscriptionControlChildSbb() {
		SubscriptionClientControlSbbLocalObject childSbb = (SubscriptionClientControlSbbLocalObject) getSubscriptionControlChildRelation().get(ChildRelationExt.DEFAULT_CHILD_NAME);
		if (childSbb == null) {
			try {
				childSbb = (SubscriptionClientControlSbbLocalObject) getSubscriptionControlChildRelation()
						.create(ChildRelationExt.DEFAULT_CHILD_NAME);
			} catch (Throwable e) {
				tracer.severe("failed to create child",e);
			}			
		}
		return childSbb;
	}

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

	protected RLSExampleSubscriberParentSbbLocalObject getParent() {
		return (RLSExampleSubscriberParentSbbLocalObject) sbbContext.getSbbLocalObject().getParent();
	}

	// --- XDM CLIENT CHILD SBB
	
	public abstract ChildRelationExt getXDMClientChildRelation();
	
	// --- SBB LOCAL OBJECT
	
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
			XDMClientChildSbbLocalObject xdm = (XDMClientChildSbbLocalObject) getXDMClientChildRelation().create(ChildRelationExt.DEFAULT_CHILD_NAME);
			xdm.put(uri, "application/rls-services+xml", getRlsServices(entryURIs).getBytes("UTF-8"),notifier);			
		} catch (Exception e) {
			tracer.severe(e.getMessage(), e);
			getParent().subscriberNotStarted();
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
			getParent().subscriberNotStarted();
		}
		else {			
			// now subscribe the presence of it
			try {
				getSubscriptionControlChildSbb().subscribe(subscriber, "...", notifier, eventPackage, getSubscriptionId(), expires,null,null,null);
			} catch (Throwable e) {
				tracer.severe("failed to subscribe presence",e);
			}			
		}
	}
	
	@Override
	public void subscribeOk(String subscriber, String notifier,
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

			getParent().subscriberStarted();
		}
		catch (Exception e) {
			tracer.severe(e.getMessage(),e);
		}
		
	}
	
	private void deleteRlsServices() {
		try {
			XDMClientChildSbbLocalObject xdm = (XDMClientChildSbbLocalObject) getXDMClientChildRelation().get(ChildRelationExt.DEFAULT_CHILD_NAME);
			xdm.delete(uri,notifier);			
		} catch (Exception e) {
			tracer.severe(e.getMessage(), e);			
		}
		UserProfileControlManagement.getInstance().removeUser(notifier);
	}
	
	@Override
	public void subscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		tracer.info("error on subscribe: error=" + error);
		deleteRlsServices();		
	}
	
	@Override
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
			// re-subscribe after a sec (just to let container release any possible state related with old subscription
			try {
				Thread.sleep(1000);
			}
			catch (Exception e) {
				// ignore
			}
			getSubscriptionControlChildSbb().subscribe(subscriber, "...", notifier, eventPackage, getSubscriptionId(), expires,null,null,null);
		}
	}
	
	public void onTimerEvent(TimerEvent event, ActivityContextInterface aci) {
		// refresh subscription
		getSubscriptionControlChildSbb().resubscribe(subscriber, notifier, eventPackage, getSubscriptionId(), expires);
	}

	@Override
	public void resubscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires) {
		tracer.info("resubscribe Ok : expires=" + expires);
	}

	@Override
	public void resubscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		tracer.info("error on resubscribe: error=" + error);
		deleteRlsServices();
	}

	public void stop() {
		getSubscriptionControlChildSbb().unsubscribe(subscriber, notifier, eventPackage, getSubscriptionId());
		deleteRlsServices();
	}
	
	@Override
	public void unsubscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		tracer.info("error on unsubscribe: error=" + error);		
	}
	
	@Override
	public void unsubscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId) {
		tracer.info("unsubscribe Ok");		
	}
	
	@Override
	public void deleteResponse(URI uri, int responseCode,
			String responseContent, String eTag) {
		getParent().subscriberStopped();
	}
	
	// UNUSED XDM CLIENT ENABLER METHODS
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.enabler.xdmc.XDMClientParent#getResponse(java.net.URI, int, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void getResponse(URI uri, int responseCode, String mimetype, String content, String eTag) {
		
	}
	
	@Override
	public void resubscribeFailed(int arg0, XDMClientChildSbbLocalObject arg1,
			String arg2) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void subscribeFailed(int arg0, XDMClientChildSbbLocalObject arg1,
			String arg2) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void subscriptionNotification(XcapDiff arg0, SubscriptionStatus arg1) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void unsubscribeFailed(int arg0, XDMClientChildSbbLocalObject arg1,
			String arg2) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void subscriptionTerminated(XDMClientChildSbbLocalObject arg0,
			String arg1, TerminationReason arg2) {
		// TODO Auto-generated method stub
		
	}
	
	// --- SBB OBJECT

	private SbbContextExt sbbContext = null; // This SBB's context

	private TimerFacility timerFacility = null;
	private NullActivityContextInterfaceFactory nullACIFactory;
	private NullActivityFactory nullActivityFactory;

	/**
	 * Called when an sbb object is instantied and enters the pooled state.
	 */
	public void setSbbContext(SbbContext sbbContext) {

		this.sbbContext = (SbbContextExt) sbbContext;
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