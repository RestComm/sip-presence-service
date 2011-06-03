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

package org.mobicents.slee.sipevent.server.rlscache;

import java.io.StringReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.slee.Address;
import javax.slee.ServiceID;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ActivityFlags;
import javax.slee.resource.ActivityHandle;
import javax.slee.resource.ActivityIsEndingException;
import javax.slee.resource.ConfigProperties;
import javax.slee.resource.FailureReason;
import javax.slee.resource.FireableEventType;
import javax.slee.resource.InvalidConfigurationException;
import javax.slee.resource.Marshaler;
import javax.slee.resource.ReceivableService;
import javax.slee.resource.ResourceAdaptor;
import javax.slee.resource.ResourceAdaptorContext;
import javax.slee.resource.SleeEndpoint;
import javax.slee.resource.StartActivityException;
import javax.slee.resource.UnrecognizedActivityHandleException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.mobicents.slee.sipevent.server.rlscache.events.RLSServicesAddedEvent;
import org.mobicents.slee.sipevent.server.rlscache.events.RLSServicesRemovedEvent;
import org.mobicents.slee.sipevent.server.rlscache.events.RLSServicesUpdatedEvent;
import org.mobicents.slee.sipevent.server.rlscache.events.WatchRLSServicesEvent;
import org.mobicents.slee.sipevent.server.rlscache.events.WatchResourceListsEvent;
import org.mobicents.slee.sipevent.server.subscription.jmx.SubscriptionControlManagement;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ResourceLists;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.RlsServices;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.ServiceType;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.ElementSelectorStep;
import org.openxdm.xcap.common.uri.ElementSelectorStepByAttr;

public class RLSServicesCacheResourceAdaptor implements ResourceAdaptor,
		RLSServicesCacheSbbInterface {

	private RLSServicesCacheDataSource dataSource;
	private Tracer tracer;

	private FireableEventType watchRLSServicesEvent;
	private FireableEventType watchResourceListsEvent;
	private FireableEventType rLSServicesAddedEvent;
	private FireableEventType rLSServicesRemovedEvent;
	private FireableEventType rLSServicesUpdatedEvent;

	private final ServiceID serviceID = new ServiceID(
			"RLSServicesWatcherService", "org.mobicents", "1.0");

	private SleeEndpoint sleeEndpoint;

	private static final int ACTIVITY_FLAGS = ActivityFlags
			.setRequestSleeActivityGCCallback(ActivityFlags.REQUEST_ENDED_CALLBACK);

	// dummy activity for watcher of the rls services docs
	private final RLSServiceActivityImpl dummyRLSServicesDocActivity = new RLSServiceActivityImpl(
			"", this);
	private final RLSServiceActivityHandle dummyRLSServicesDocActivityHandle = new RLSServiceActivityHandle(
			dummyRLSServicesDocActivity.getServiceURI());
	private final DocumentSelector globalRLSServicesDocumentSelector = new DocumentSelector(
			"rls-services/global", "index");
	private LinkedList<ElementSelectorStep> rlsServicesBaseElementSelectorSteps = initRlsServicesBaseElementSelectorSteps();

	// flags used to control when it's time to init the service which is
	// responsible for providing rls services and resource lists
	private boolean serviceActive = false;
	private boolean raActive = false;

	private ListReferenceEndpointAddressParser addressParser;

	private ExecutorService executorService;

	public static JAXBContext jaxbContext = initJAxbContext();

	public RLSServicesCacheDataSource getDataSource() {
		return dataSource;
	}

	private LinkedList<ElementSelectorStep> initRlsServicesBaseElementSelectorSteps() {
		LinkedList<ElementSelectorStep> list = new LinkedList<ElementSelectorStep>();
		list.add(new ElementSelectorStep("rls-services"));
		return list;
	}

	private static JAXBContext initJAxbContext() {
		try {
			return JAXBContext
					.newInstance("org.openxdm.xcap.client.appusage.rlsservices.jaxb"
							+ ":org.openxdm.xcap.client.appusage.resourcelists.jaxb");
		} catch (JAXBException e) {
			throw new RuntimeException(
					"unable to create jaxb context for pres rules docs", e);
		}
	}

	@Override
	public void activityEnded(ActivityHandle handle) {
		if (tracer.isFineEnabled()) {
			tracer.fine(handle.toString() + " ended.");
		}

		dataSource.removeActivity(handle);
	}

	@Override
	public void activityUnreferenced(ActivityHandle handle) {

		if (tracer.isFineEnabled()) {
			tracer.fine(handle.toString()
					+ " is now unreferenced, ending activity.");
		}

		Object activity = getActivity(handle);
		if (activity instanceof RLSServiceActivityImpl) {
			synchronized (activity) {
				// sync due to possible concurrent updates on rls service
				((RLSServiceActivityImpl) activity).ending();
				// end activity
				try {
					sleeEndpoint.endActivity(handle);
				} catch (Throwable e) {
					tracer.warning(
							"failed to end activity after becoming unreferenced: "
									+ handle, e);
					activityEnded(handle);
				}
			}
		}
		else {
			// end activity
			try {
				sleeEndpoint.endActivity(handle);
			} catch (Throwable e) {
				tracer.warning(
						"failed to end activity after becoming unreferenced: "
								+ handle, e);
				activityEnded(handle);
			}
		}
		
	}

	@Override
	public void administrativeRemove(ActivityHandle handle) {
		activityEnded(handle);
	}

	@Override
	public void eventProcessingFailed(ActivityHandle handle,
			FireableEventType eventType, Object event, Address address,
			ReceivableService service, int flags, FailureReason reason) {
		// not used
	}

	@Override
	public void eventProcessingSuccessful(ActivityHandle handle,
			FireableEventType eventType, Object event, Address address,
			ReceivableService service, int flags) {
		// not used
	}

	@Override
	public void eventUnreferenced(ActivityHandle handle,
			FireableEventType eventType, Object event, Address address,
			ReceivableService service, int flags) {
		// not used
	}

	@Override
	public Object getActivity(ActivityHandle handle) {
		return dataSource.getActivity(handle);
	}

	@Override
	public ActivityHandle getActivityHandle(Object activity) {
		return dataSource.getActivityHandle(activity);
	}

	@Override
	public Marshaler getMarshaler() {
		return null;
	}

	@Override
	public Object getResourceAdaptorInterface(String className) {
		return this;
	}

	@Override
	public void queryLiveness(ActivityHandle handle) {
		if (dataSource.getActivity(handle) == null) {
			try {
				sleeEndpoint.endActivity(handle);
			} catch (Exception e) {
				tracer.severe("failed to end idle activity " + handle, e);
			}
		}
	}

	@Override
	public void raActive() {
		dataSource = new RLSServicesCacheDataSource();
		executorService = Executors.newSingleThreadExecutor();
		raActive = true;
		if (SubscriptionControlManagement.getInstance().getEventListSupportOn()
				&& serviceActive) {
			// only init service if service is already activated
			initService();
		}
	}

	@Override
	public void raConfigurationUpdate(ConfigProperties properties) {
		// not used

	}

	@Override
	public void raConfigure(ConfigProperties properties) {
		// not used
	}

	@Override
	public void raInactive() {
		dataSource = null;
		executorService.shutdownNow();
	}

	@Override
	public void raStopping() {
		raActive = false;
		if (serviceActive) {
			endAllActivities();
		}
	}

	@Override
	public void raUnconfigure() {
		// not used
	}

	@Override
	public void raVerifyConfiguration(ConfigProperties properties)
			throws InvalidConfigurationException {
		// not used
	}

	@Override
	public void serviceActive(ReceivableService serviceInfo) {
		if (serviceInfo.getService().equals(serviceID)) {
			serviceActive = true;
			if (SubscriptionControlManagement.getInstance()
					.getEventListSupportOn() && raActive) {
				// only init service if ra already active
				initService();
			}
		}
	}

	private void initService() {
		// init the service
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// create dummy activity and fire event to init service
				try {
					sleeEndpoint.startActivity(
							dummyRLSServicesDocActivityHandle,
							dummyRLSServicesDocActivity);
					dataSource.putIfAbsentRLSServiceActivity(
							dummyRLSServicesDocActivityHandle,
							dummyRLSServicesDocActivity);
					sleeEndpoint.fireEvent(dummyRLSServicesDocActivityHandle,
							watchRLSServicesEvent, new WatchRLSServicesEvent(),
							null, null);
				} catch (Throwable e) {
					tracer.severe(
							"failed to signal service to watch global rls services doc in the xdm",
							e);
					throw new RuntimeException(e);
				}
			}
		};
		new Thread(runnable).start();
	}

	@Override
	public void serviceInactive(ReceivableService serviceInfo) {
		// not used
	}

	@Override
	public void serviceStopping(ReceivableService serviceInfo) {
		if (serviceInfo.getService().equals(serviceID)) {
			serviceActive = false;
			if (raActive) {
				endAllActivities();
			}
		}
	}

	private void endAllActivities() {
		if (SubscriptionControlManagement.getInstance().getEventListSupportOn()) {
			final Set<ActivityHandle> handles = dataSource.getAllHandles();
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					for (ActivityHandle handle : handles) {
						try {
							sleeEndpoint.endActivity(handle);
						} catch (Throwable e) {
							tracer.severe("failed to end activity", e);
						}
					}
				}
			};
			new Thread(runnable).start();
		}
	}

	@Override
	public void setResourceAdaptorContext(ResourceAdaptorContext context) {
		tracer = context.getTracer(getClass().getSimpleName());
		sleeEndpoint = context.getSleeEndpoint();
		try {
			watchRLSServicesEvent = context.getEventLookupFacility()
					.getFireableEventType(WatchRLSServicesEvent.EVENT_TYPE_ID);
			watchResourceListsEvent = context
					.getEventLookupFacility()
					.getFireableEventType(WatchResourceListsEvent.EVENT_TYPE_ID);
			rLSServicesRemovedEvent = context
					.getEventLookupFacility()
					.getFireableEventType(RLSServicesRemovedEvent.EVENT_TYPE_ID);
			rLSServicesUpdatedEvent = context
					.getEventLookupFacility()
					.getFireableEventType(RLSServicesUpdatedEvent.EVENT_TYPE_ID);
			rLSServicesAddedEvent = context.getEventLookupFacility()
					.getFireableEventType(RLSServicesAddedEvent.EVENT_TYPE_ID);
		} catch (Exception e) {
			throw new RuntimeException(
					"Unable to retreive the event type for the event fired by this RA, unable to proceed",
					e);
		}
		addressParser = new ListReferenceEndpointAddressParser(context);
	}

	@Override
	public void unsetResourceAdaptorContext() {
		tracer = null;
		sleeEndpoint = null;
		watchRLSServicesEvent = null;
		watchResourceListsEvent = null;
		rLSServicesRemovedEvent = null;
		rLSServicesUpdatedEvent = null;
		addressParser = null;
	}

	// sbb interface

	// FROM SBB INTERFACE

	@Override
	public RLSService getRLSService(String serviceURI) {
		return dataSource.getRLSService(serviceURI);
	}

	@Override
	public RLSServiceActivity getRLSServiceActivity(String serviceURI)
			throws StartActivityException {

		if (tracer.isFineEnabled()) {
			tracer.fine("getRLSServiceActivity( uri = " + serviceURI + ")");
		}

		RLSServiceActivityHandle handle = new RLSServiceActivityHandle(
				serviceURI);
		RLSServiceActivityImpl activity = new RLSServiceActivityImpl(
				serviceURI, this);
		RLSServiceActivityImpl existentActivity = dataSource
				.putIfAbsentRLSServiceActivity(handle, activity);
		if (existentActivity == null) {
			sleeEndpoint.startActivitySuspended(handle, activity,
					ACTIVITY_FLAGS);
		} else {
			activity = existentActivity;
		}
		return activity;
	}

	@Override
	public void rlsServicesUpdated(final DocumentSelector documentSelector,
			final String document) {

		// op is done async in the executor service
		Runnable r = new Runnable() {

			@Override
			public void run() {

				if (tracer.isFineEnabled()) {
					tracer.fine("rlsServicesDocUpdated( ds = "
							+ documentSelector + ")");
				}

				if (document == null) {
					// doc was removed, remove all rls services of the doc
					RLSServiceImpl rlsService = null;
					for (String existentRLSServiceURI : dataSource
							.removeRlsServicesDocs(documentSelector)) {
						rlsService = dataSource
								.removeRLSService(existentRLSServiceURI);
						if (rlsService != null) {
							rlsService.setServiceType(null);
						}
						if (tracer.isInfoEnabled()) {
							tracer.info("Removed RLS Service "
									+ existentRLSServiceURI + " from cache.");
						}
					}
				} else {
					// doc was created or updated
					RlsServices rlsServices = null;
					try {
						rlsServices = (RlsServices) jaxbContext
								.createUnmarshaller().unmarshal(
										new StringReader(document));
					} catch (JAXBException e) {
						tracer.severe(
								"unmarshalling of global rls services failed",
								e);
						return;
					}

					Set<String> removedRlsServices = dataSource
							.removeRlsServicesDocs(documentSelector);
					Set<String> updatedRlsServices = new HashSet<String>();
					// create or update the ones provided by the update
					RLSServiceImpl rlsService = null;
					RLSServiceImpl anotherRlsService = null;
					for (ServiceType serviceType : rlsServices.getService()) {
						if (removedRlsServices != null) {
							removedRlsServices.remove(serviceType.getUri());
						}
						updatedRlsServices.add(serviceType.getUri());
						rlsService = dataSource.getRLSService(serviceType
								.getUri());
						if (rlsService == null) {
							LinkedList<ElementSelectorStep> steps = new LinkedList<ElementSelectorStep>(
									rlsServicesBaseElementSelectorSteps);
							steps.addLast(new ElementSelectorStepByAttr(
									"service", "uri", serviceType.getUri()));
							ListReferenceEndpointAddress address = new ListReferenceEndpointAddress(
									globalRLSServicesDocumentSelector,
									new ElementSelector(steps));
							anotherRlsService = new RLSServiceImpl(
									serviceType.getUri(), address,
									RLSServicesCacheResourceAdaptor.this);
							rlsService = dataSource.putRLSServiceIfAbsent(
									serviceType.getUri(), anotherRlsService);
							if (rlsService == null) {
								rlsService = anotherRlsService;
								if (tracer.isInfoEnabled()) {
									tracer.info("Added RLS Service "
											+ serviceType.getUri()
											+ " to cache.");
								}
							}
						}
						rlsService.setServiceType(serviceType);
					}
					dataSource.putRlsServicesDocs(documentSelector,
							updatedRlsServices);
					// update the ones removed with null service type
					if (removedRlsServices != null) {
						for (String removedRLSServiceURI : removedRlsServices) {
							rlsService = dataSource
									.removeRLSService(removedRLSServiceURI);
							if (rlsService != null) {
								rlsService.setServiceType(null);
							}
							if (tracer.isInfoEnabled()) {
								tracer.info("Removed RLS Service "
										+ removedRLSServiceURI + " from cache.");
							}
						}
					}
				}

			}
		};
		executorService.submit(r);
	}

	@Override
	public void resourceListsUpdated(final DocumentSelector documentSelector,
			final String document) {

		Runnable r = new Runnable() {

			@Override
			public void run() {

				if (tracer.isFineEnabled()) {
					tracer.fine("resourceListsUpdated, document selector is "
							+ documentSelector);
				}

				ResourceLists resourceLists = null;
				if (document != null) {
					try {
						resourceLists = (ResourceLists) jaxbContext
								.createUnmarshaller().unmarshal(
										new StringReader(document));
					} catch (JAXBException e) {
						tracer.severe("unmarshalling of resource lists failed",
								e);
						return;
					}
				}

				ReferencedResourceLists referencedResourceLists = dataSource
						.getResourceList(documentSelector);
				if (referencedResourceLists != null) {
					referencedResourceLists.setResourceLists(resourceLists);
				}
			}
		};
		executorService.submit(r);

	}

	// references among resource lists management

	public void removeReference(ListReferenceEndpointAddress fromAddress,
			ListReferenceEndpointAddress toAddress) {

		if (tracer.isFineEnabled()) {
			tracer.fine("removeReference from = " + fromAddress + ", to = "
					+ toAddress);
		}

		final DocumentSelector documentSelector = toAddress
				.getDocumentSelector();
		ReferencedResourceLists lists = dataSource
				.getResourceList(documentSelector);
		if (lists != null) {
			lists.removeFromReference(fromAddress, toAddress);
			if (!lists.hasFromReferences()) {

				if (tracer.isInfoEnabled()) {
					tracer.info("Removed Resource List " + documentSelector
							+ " from cache, no references.");
				}

				dataSource.removeResourceList(documentSelector);
				ResourceListActivityHandle handle = new ResourceListActivityHandle(
						documentSelector);
				try {
					sleeEndpoint.endActivity(handle);
				} catch (Throwable e) {
					tracer.severe("failed to end resource list activity "
							+ documentSelector, e);
					dataSource.removeResourceListActivity(handle);
				}
			}
		}
	}

	public ListReferenceTo addReference(ListReferenceFrom from,
			ListReferenceEndpointAddress toAddress) {

		if (tracer.isFineEnabled()) {
			tracer.fine("addReference from = " + from.getAddress() + ", to = "
					+ toAddress);
		}

		final DocumentSelector documentSelector = toAddress
				.getDocumentSelector();
		ReferencedResourceLists lists = dataSource
				.getResourceList(documentSelector);
		if (lists == null) {
			ReferencedResourceLists newLists = new ReferencedResourceLists(
					documentSelector, this);
			lists = dataSource.putResourceListIfAbsent(documentSelector,
					newLists);
			if (lists == null) {
				lists = newLists;
				if (tracer.isInfoEnabled()) {
					tracer.info("Added Resource List " + documentSelector
							+ " to cache.");
				}
				ResourceListActivityHandle handle = new ResourceListActivityHandle(
						documentSelector);
				try {
					sleeEndpoint.startActivity(handle,
							new ResourceListActivityImpl(documentSelector),
							ACTIVITY_FLAGS);
					sleeEndpoint.fireEvent(handle, watchResourceListsEvent,
							new WatchResourceListsEvent(documentSelector),
							null, null);
				} catch (Throwable e) {
					tracer.severe("failed to start resource list activity "
							+ documentSelector, e);
					dataSource.removeResourceListActivity(handle);
					dataSource.removeResourceList(documentSelector);
					return null;
				}
			}
		}
		return lists.addFromReference(from, toAddress);
	}

	// event fire methods

	public void fireRLSServicesAddedEvent(String uri,
			RLSServicesAddedEvent event) {

		final RLSServiceActivityHandle handle = new RLSServiceActivityHandle(
				uri);

		try {
			RLSServiceActivityImpl activity = dataSource
					.getRLSServiceActivity(handle);
			if (activity == null) {
				// start activity
				activity = new RLSServiceActivityImpl(uri, this);
				if (dataSource.putIfAbsentRLSServiceActivity(handle, activity) == null) {
					// added to datasource, now add to slee
					sleeEndpoint
							.startActivity(handle, activity, ACTIVITY_FLAGS);
				}
			}
			sleeEndpoint.fireEvent(handle, rLSServicesAddedEvent, event, null,
					null);
		} catch (Throwable e) {
			tracer.severe("failed to fire rls services added event", e);
		}
	}

	public void fireRLSServicesRemovedEvent(String uri,
			RLSServicesRemovedEvent event) {

		final RLSServiceActivityHandle handle = new RLSServiceActivityHandle(
				uri);
		final RLSServiceActivityImpl activity = dataSource
				.getRLSServiceActivity(handle);
		if (activity != null) {
			synchronized (activity) {
				if (!activity.isEnding()) {
					try {
						sleeEndpoint.fireEvent(handle, rLSServicesRemovedEvent,
								event, null, null);
					} catch (Throwable e) {
						tracer.severe("failed to fire event", e);
					}
				}
			}
		}
	}

	public void fireRLSServicesUpdatedEvent(String uri,
			RLSServicesUpdatedEvent event) {

		if (tracer.isInfoEnabled()) {
			tracer.info("Updated RLS Service " + uri + ". New entries: "
					+ event.getNewEntries() + ". Removed entries: "
					+ event.getRemovedEntries());
		}

		final RLSServiceActivityHandle handle = new RLSServiceActivityHandle(
				uri);
		final RLSServiceActivityImpl activity = dataSource
				.getRLSServiceActivity(handle);
		if (activity != null) {
			synchronized (activity) {
				if (!activity.isEnding()) {
					try {
						sleeEndpoint.fireEvent(handle, rLSServicesUpdatedEvent,
								event, null, null);
					} catch (Throwable e) {
						tracer.severe("failed to fire event", e);
					}
				}
			}
		}
	}

	// misc

	public ListReferenceEndpointAddressParser getAddressParser() {
		return addressParser;
	}

}
