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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.SbbLocalObject;
import javax.slee.facilities.Tracer;

import org.mobicents.slee.sippresence.server.jmx.SipPresenceServerManagement;
import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.server.slee.resource.datasource.CollectionActivity;
import org.openxdm.xcap.server.slee.resource.datasource.AttributeUpdatedEvent;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceActivityContextInterfaceFactory;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceSbbInterface;
import org.openxdm.xcap.server.slee.resource.datasource.DocumentUpdatedEvent;
import org.openxdm.xcap.server.slee.resource.datasource.ElementUpdatedEvent;

public abstract class PresRulesCacheSbb implements Sbb {

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
	private DataSourceSbbInterface dataSourceSbbInterface;
	private DataSourceActivityContextInterfaceFactory dataSourceActivityContextInterfaceFactory;
	private PresRulesSbbInterface presRulesSbbInterface;
	
	@Override
	public void setSbbContext(SbbContext context) {
		this.context = context;
		this.tracer = context.getTracer(getClass().getSimpleName());
		try {
			Context ic = (Context) new InitialContext()
					.lookup("java:comp/env");
			dataSourceSbbInterface = (DataSourceSbbInterface) ic
					.lookup("slee/resources/xdm/datasource/1.0/sbbinterface");
			dataSourceActivityContextInterfaceFactory = (DataSourceActivityContextInterfaceFactory) ic
					.lookup("slee/resources/xdm/datasource/1.0/acif");
			presRulesSbbInterface = (PresRulesSbbInterface) ic
				.lookup("slee/resources/presence/presrulescache/1.0/sbbinterface");
		} catch (NamingException e) {
			tracer.severe("Failed to set sbb context", e);
		}
	}
	
	@Override
	public void unsetSbbContext() {
		context = null;
		tracer = null;		
		dataSourceSbbInterface = null;
		dataSourceActivityContextInterfaceFactory = null;
		presRulesSbbInterface = null;
	}
	
	// cmp
	
	public abstract ActivityContextInterface getPresRulesAppUsageACI();
	
	public abstract void setPresRulesAppUsageACI(ActivityContextInterface aci);
	
	// event handlers
	
	public void onGetAndSubscribePresRulesAppUsageEvent(GetAndSubscribePresRulesAppUsageEvent event, ActivityContextInterface aci) {
		long start = System.currentTimeMillis();

		String presRulesAUID = SipPresenceServerManagement.getInstance().getPresRulesAUID();
		// lets attach to the app usage activity, to receive events related with updates on its docs
		CollectionActivity appUsageActivity = dataSourceSbbInterface.createCollectionActivity(presRulesAUID);
		ActivityContextInterface appUsageActivityContextInterface = dataSourceActivityContextInterfaceFactory.getActivityContextInterface(appUsageActivity);
		appUsageActivityContextInterface.attach(context.getSbbLocalObject());
		setPresRulesAppUsageACI(appUsageActivityContextInterface);
		// now fetch all existent docs
		DocumentSelector documentSelector = null;
		try {
			Document[] documents = dataSourceSbbInterface.getDocuments(presRulesAUID+"/users",true);
			for (Document document : documents) {
				documentSelector = new DocumentSelector(document.getCollection(), document.getDocumentName());
				if (tracer.isFineEnabled()) {
					tracer.fine("Retrieving document "+documentSelector);
				}
				presRulesSbbInterface.rulesetUpdated(documentSelector, null, document.getETag(), document.getAsString());					
			}
		} catch (InternalServerErrorException e) {
			tracer.severe("unable to fetch current pres rules docs",e);
		}
		tracer.info("Total time to init pres rules cache: "+(System.currentTimeMillis()-start)+"ms");
	}
	
	public void onUnsubscribePresRulesAppUsageEvent(UnsubscribePresRulesAppUsageEvent event, ActivityContextInterface aci) {
		SbbLocalObject sbbLocalObject = context.getSbbLocalObject();
		ActivityContextInterface appUsageActivityContextInterface = getPresRulesAppUsageACI();
		if (appUsageActivityContextInterface != null) {
			appUsageActivityContextInterface.detach(sbbLocalObject);
		}
		aci.detach(sbbLocalObject);
	}
	
	private String getDocumentETag(Document document) {
		return document == null ? null : document.getETag();

	}
	public void onAttributeUpdatedEvent(AttributeUpdatedEvent event,
			ActivityContextInterface aci) {
		presRulesSbbInterface.rulesetUpdated(event.getDocumentSelector(), getDocumentETag(event.getOldDocument()), event.getNewETag(), event.getNewDocumentString());
	}

	public void onDocumentUpdatedEvent(DocumentUpdatedEvent event,
			ActivityContextInterface aci) {
		presRulesSbbInterface.rulesetUpdated(event.getDocumentSelector(), getDocumentETag(event.getOldDocument()), event.getNewETag(), event.getNewDocumentString());
	}

	public void onElementUpdatedEvent(ElementUpdatedEvent event,
			ActivityContextInterface aci) {
		presRulesSbbInterface.rulesetUpdated(event.getDocumentSelector(), getDocumentETag(event.getOldDocument()), event.getNewETag(), event.getNewDocumentString());
	}
}
