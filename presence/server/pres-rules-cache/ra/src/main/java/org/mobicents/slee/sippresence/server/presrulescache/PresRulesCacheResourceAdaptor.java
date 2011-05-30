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

package org.mobicents.slee.sippresence.server.presrulescache;

import java.io.StringReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.slee.Address;
import javax.slee.ServiceID;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ActivityFlags;
import javax.slee.resource.ActivityHandle;
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.openxdm.xcap.client.appusage.presrules.jaxb.commonpolicy.Ruleset;
import org.openxdm.xcap.common.uri.DocumentSelector;

public class PresRulesCacheResourceAdaptor implements ResourceAdaptor,
		PresRulesSbbInterface {

	private PresRulesCacheDataSource dataSource;
	private Tracer tracer;

	// flags used to control when it's time to init the service which is responsible for providing pres rules
	private boolean serviceActive = false;
	private boolean raActive = false;
	
	private FireableEventType rulesetUpdatedEventType;
	private FireableEventType getAndSubscribePresRulesAppUsageEventType;
	private FireableEventType unsubscribePresRulesAppUsageEventType;
	private final ServiceID serviceID = new ServiceID("PresRulesCacheService",
			"org.mobicents", "1.0");

	private SleeEndpoint sleeEndpoint;

	private static final int ACTIVITY_FLAGS = ActivityFlags
			.setRequestSleeActivityGCCallback(ActivityFlags.REQUEST_ENDED_CALLBACK);

	// dummy activity for watcher of the pres rules app usage
	private final PresRulesActivityImpl presRulesAppUsageActivity = new PresRulesActivityImpl(
			new DocumentSelector("null", "null"), this);
	private final PresRulesActivityHandle presRulesAppUsageActivityHandle = new PresRulesActivityHandle(
			presRulesAppUsageActivity.getDocumentSelector());

	public static JAXBContext jaxbContext = initJAxbContext();

	private ExecutorService executorService;
	
	public PresRulesCacheDataSource getDataSource() {
		return dataSource;
	}

	private static JAXBContext initJAxbContext() {
		try {
			return JAXBContext
					.newInstance("org.openxdm.xcap.client.appusage.presrules.jaxb.commonpolicy:org.openxdm.xcap.client.appusage.presrules.jaxb:org.openxdm.xcap.client.appusage.omapresrules.jaxb");
		} catch (JAXBException e) {
			throw new RuntimeException(
					"unable to create jaxb context for pres rules docs", e);
		}
	}

	@Override
	public void activityEnded(ActivityHandle handle) {
		dataSource.removeActivity((PresRulesActivityHandle) handle);
	}

	@Override
	public void activityUnreferenced(ActivityHandle handle) {
		// end activity
		try {
			sleeEndpoint.endActivity(handle);
		}
		catch (Throwable e) {
			tracer.warning("failed to end activity after becoming unreferenced: "+handle,e);
			activityEnded(handle);
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
		PresRulesActivityHandle presRulesActivityHandle = (PresRulesActivityHandle) handle;
		return dataSource.getActivity(presRulesActivityHandle);
	}

	@Override
	public ActivityHandle getActivityHandle(Object activity) {
		PresRulesActivityImpl presRulesActivity = (PresRulesActivityImpl) activity;
		PresRulesActivityHandle handle = new PresRulesActivityHandle(
				presRulesActivity.getDocumentSelector());
		return dataSource.getActivity(handle) != null ? handle : null;
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
		if (dataSource.getActivity((PresRulesActivityHandle) handle) == null) {
			try {
				sleeEndpoint.endActivity(handle);
			} catch (Exception e) {
				tracer.severe("failed to end idle activity " + handle, e);
			}
		}
	}

	@Override
	public void raActive() {
		dataSource = new PresRulesCacheDataSource();
		executorService = Executors.newSingleThreadExecutor();
		raActive = true;
		if (serviceActive) {
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
			stopService();
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
			if (raActive) {
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
				// fire event to init service
				try {
					sleeEndpoint.startActivity(
							presRulesAppUsageActivityHandle,
							presRulesAppUsageActivity);
					dataSource.putActivity(presRulesAppUsageActivityHandle,
							presRulesAppUsageActivity);
					sleeEndpoint.fireEvent(presRulesAppUsageActivityHandle,
							getAndSubscribePresRulesAppUsageEventType,
							new GetAndSubscribePresRulesAppUsageEvent(),
							null, null);
				} catch (Throwable e) {
					tracer
							.severe(
									"failed to signal service to watch pres rules app usage in the xdm",
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
				stopService();
			}
		}
	}

	private void stopService() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// fire event to stop watching pres rules
				try {
					sleeEndpoint.fireEvent(presRulesAppUsageActivityHandle,
							unsubscribePresRulesAppUsageEventType,
							new UnsubscribePresRulesAppUsageEvent(), null,
							null);
					sleeEndpoint
							.endActivity(presRulesAppUsageActivityHandle);
				} catch (Throwable e) {
					tracer
							.severe(
									"failed to signal service to stop watching pres rules app usage in the xdm",
									e);
				}
			}
		};
		new Thread(runnable).start();
	}

	@Override
	public void setResourceAdaptorContext(ResourceAdaptorContext context) {
		tracer = context.getTracer(getClass().getSimpleName());
		sleeEndpoint = context.getSleeEndpoint();
		try {
			rulesetUpdatedEventType = context.getEventLookupFacility()
					.getFireableEventType(RulesetUpdatedEvent.EVENT_TYPE_ID);
			getAndSubscribePresRulesAppUsageEventType = context
					.getEventLookupFacility()
					.getFireableEventType(
							GetAndSubscribePresRulesAppUsageEvent.EVENT_TYPE_ID);
			unsubscribePresRulesAppUsageEventType = context
					.getEventLookupFacility().getFireableEventType(
							UnsubscribePresRulesAppUsageEvent.EVENT_TYPE_ID);
		} catch (Exception e) {
			throw new RuntimeException(
					"Unable to retreive the event type for the event fired by this RA, unable to proceed",
					e);
		}
	}

	@Override
	public void unsetResourceAdaptorContext() {
		tracer = null;
		sleeEndpoint = null;
		rulesetUpdatedEventType = null;
		getAndSubscribePresRulesAppUsageEventType = null;
		unsubscribePresRulesAppUsageEventType = null;
	}

	// SBB INTERFACE IMPL

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.sippresence.server.presrulescache.PresRulesSbbInterface
	 * #rulesetUpdated(org.openxdm.xcap.common.uri.DocumentSelector,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	public void rulesetUpdated(final DocumentSelector documentSelector,
			final String oldETag, final String newETag, final String rulesetString) {

		if(tracer.isInfoEnabled()) {
			tracer.info("Ruleset for "+documentSelector+" updated ");
		}
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				Ruleset ruleset = null;
				try {
					ruleset = (Ruleset) jaxbContext.createUnmarshaller().unmarshal(
							new StringReader(rulesetString));
				} catch (JAXBException e) {
					tracer.severe("unmarshalling of ruleset failed", e);
					return;
				}

				if (ruleset != null) {
					dataSource.putRuleset(documentSelector, ruleset);
				} else {
					dataSource.removeRuleset(documentSelector);
				}
				PresRulesActivityHandle handle = new PresRulesActivityHandle(
						documentSelector);
				PresRulesActivityImpl activity = dataSource.getActivity(handle);
				if (activity != null) {
					// fire event transacted, this method is invoked from sbb
					try {
						sleeEndpoint.fireEvent(handle,
								rulesetUpdatedEventType, new RulesetUpdatedEvent(
										documentSelector, oldETag, newETag, ruleset),
								null, null);
					} catch (Exception e) {
						tracer.severe("unable to fire event for handle " + handle, e);
					}
				}				
			}
		};
		
		executorService.submit(r);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.sippresence.server.presrulescache.PresRulesSbbInterface
	 * #getActivity(org.openxdm.xcap.common.uri.DocumentSelector)
	 */
	public PresRulesActivity getActivity(DocumentSelector documentSelector)
			throws StartActivityException {
		PresRulesActivityHandle handle = new PresRulesActivityHandle(
				documentSelector);
		PresRulesActivityImpl activity = new PresRulesActivityImpl(
				documentSelector, this);
		PresRulesActivityImpl existentActivity = dataSource.putActivity(handle,
				activity);
		if (existentActivity == null) {
			sleeEndpoint.startActivitySuspended(handle, activity,
					ACTIVITY_FLAGS);
		} else {
			activity = existentActivity;
		}
		return activity;
	}
}
