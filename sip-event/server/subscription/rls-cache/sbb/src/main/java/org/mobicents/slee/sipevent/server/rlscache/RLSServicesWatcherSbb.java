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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.slee.ActivityContextInterface;
import javax.slee.ActivityEndEvent;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.SbbLocalObject;
import javax.slee.facilities.Tracer;

import org.mobicents.slee.sipevent.server.rlscache.events.WatchRLSServicesEvent;
import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.server.slee.resource.datasource.CollectionActivity;
import org.openxdm.xcap.server.slee.resource.datasource.AttributeUpdatedEvent;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceActivityContextInterfaceFactory;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceSbbInterface;
import org.openxdm.xcap.server.slee.resource.datasource.DocumentUpdatedEvent;
import org.openxdm.xcap.server.slee.resource.datasource.ElementUpdatedEvent;

public abstract class RLSServicesWatcherSbb implements Sbb {
	
	@Override
	public void sbbActivate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void sbbCreate() throws CreateException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void sbbExceptionThrown(Exception exception, Object event,
			ActivityContextInterface aci) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void sbbLoad() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void sbbPassivate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void sbbPostCreate() throws CreateException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void sbbRemove() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void sbbRolledBack(RolledBackContext context) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void sbbStore() {
		// TODO Auto-generated method stub
		
	}
	
	private SbbContext context;
	private Tracer tracer;
	private DataSourceSbbInterface dataSourceRASbbInterface;
	private DataSourceActivityContextInterfaceFactory dataSourceRAActivityContextInterfaceFactory;
	private RLSServicesCacheSbbInterface rlsCacheRASbbInterface;
	
	@Override
	public void setSbbContext(SbbContext context) {
		this.context = context;
		this.tracer = context.getTracer(getClass().getSimpleName());
		try {
			Context ic = (Context) new InitialContext()
					.lookup("java:comp/env");
			dataSourceRASbbInterface = (DataSourceSbbInterface) ic
					.lookup("slee/resources/xdm/datasource/1.0/sbbinterface");
			dataSourceRAActivityContextInterfaceFactory = (DataSourceActivityContextInterfaceFactory) ic
					.lookup("slee/resources/xdm/datasource/1.0/acif");
			rlsCacheRASbbInterface = (RLSServicesCacheSbbInterface) ic
				.lookup("slee/resources/sipevent/rlscache/1.0/sbbinterface");
		} catch (NamingException e) {
			tracer.severe("Failed to set sbb context", e);
		}
	}
	
	@Override
	public void unsetSbbContext() {
		context = null;
		tracer = null;		
		dataSourceRASbbInterface = null;
		dataSourceRAActivityContextInterfaceFactory = null;
		rlsCacheRASbbInterface = null;
	}
	
	// event handlers
	
	public void onWatchRLSServicesEvent(WatchRLSServicesEvent event, ActivityContextInterface aci) {
		long start = System.currentTimeMillis();

		if(tracer.isFineEnabled()) {
			tracer.fine("onWatchRLSServicesEvent");
		}
		// lets attach to the app usage activity, to receive events related with updates on its docs
		CollectionActivity appUsageActivity = dataSourceRASbbInterface.createCollectionActivity("rls-services");
		ActivityContextInterface appUsageActivityContextInterface = dataSourceRAActivityContextInterfaceFactory.getActivityContextInterface(appUsageActivity);
		appUsageActivityContextInterface.attach(context.getSbbLocalObject());
		// now fetch all existent docs
		DocumentSelector documentSelector = null;
		try {
			Document[] documents = dataSourceRASbbInterface.getDocuments("rls-services/users",true);
			for (Document document : documents) {
				documentSelector = new DocumentSelector(document.getCollection(), document.getDocumentName());
				if (!documentSelector.isUserDocument()) {
					// ignore global
					continue;
				}
				rlsCacheRASbbInterface.rlsServicesUpdated(documentSelector,document.getAsString());						
			}
		} catch (InternalServerErrorException e) {
			tracer.severe("unable to fetch current rls services docs",e);
		}
		tracer.info("Total time to init rls cache: "+(System.currentTimeMillis()-start)+"ms");
	}
	
	public void onActivityEndEvent(ActivityEndEvent event, ActivityContextInterface aci) {
		Object activity = aci.getActivity();
		if (activity != null && activity instanceof RLSServiceActivity) {
			SbbLocalObject sbbLocalObject = context.getSbbLocalObject();
			for (ActivityContextInterface attachedACIs : context.getActivities()) {
				attachedACIs.detach(sbbLocalObject);
			}
		}		
	}
	
	public void onAttributeUpdatedEvent(AttributeUpdatedEvent event,
			ActivityContextInterface aci) {
		if (!event.getDocumentSelector().isUserDocument()) {
			// ignore global
			return;
		}
		rlsCacheRASbbInterface.rlsServicesUpdated(event.getDocumentSelector(),event.getNewDocumentString());
	}

	public void onDocumentUpdatedEvent(DocumentUpdatedEvent event,
			ActivityContextInterface aci) {
		if (!event.getDocumentSelector().isUserDocument()) {
			// ignore global
			return;
		}
		rlsCacheRASbbInterface.rlsServicesUpdated(event.getDocumentSelector(),event.getNewDocumentString());
	}

	public void onElementUpdatedEvent(ElementUpdatedEvent event,
			ActivityContextInterface aci) {
		if (!event.getDocumentSelector().isUserDocument()) {
			// ignore global
			return;
		}		
		rlsCacheRASbbInterface.rlsServicesUpdated(event.getDocumentSelector(),event.getNewDocumentString());
	}
}
