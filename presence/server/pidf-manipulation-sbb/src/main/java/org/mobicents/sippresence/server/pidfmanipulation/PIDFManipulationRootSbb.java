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

package org.mobicents.sippresence.server.pidfmanipulation;

import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.slee.ActivityContextInterface;
import javax.slee.ActivityEndEvent;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.facilities.Tracer;
import javax.slee.serviceactivity.ServiceActivity;
import javax.slee.serviceactivity.ServiceStartedEvent;

import org.mobicents.slee.ChildRelationExt;
import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.server.slee.resource.datasource.CollectionActivity;
import org.openxdm.xcap.server.slee.resource.datasource.AttributeUpdatedEvent;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceActivityContextInterfaceFactory;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceSbbInterface;
import org.openxdm.xcap.server.slee.resource.datasource.DocumentUpdatedEvent;
import org.openxdm.xcap.server.slee.resource.datasource.ElementUpdatedEvent;

/**
 * A SBB that upon handling service activation event subscribes the
 * pidf-manipulation app usage in the XDMS, and manages the publications of all
 * the docs it contains.
 * 
 * @author martins
 * 
 */
public abstract class PIDFManipulationRootSbb implements Sbb {

	private SbbContext context;
	private Tracer tracer;
	private DataSourceSbbInterface dataSourceSbbInterface;
	private DataSourceActivityContextInterfaceFactory dataSourceActivityContextInterfaceFactory;

	@Override
	public void setSbbContext(SbbContext context) {
		this.context = context;
		this.tracer = context.getTracer(getClass().getSimpleName());
		try {
			Context ic = (Context) new InitialContext().lookup("java:comp/env");
			dataSourceSbbInterface = (DataSourceSbbInterface) ic
					.lookup("slee/resources/xdm/datasource/1.0/sbbinterface");
			dataSourceActivityContextInterfaceFactory = (DataSourceActivityContextInterfaceFactory) ic
					.lookup("slee/resources/xdm/datasource/1.0/acif");
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
	}

	// child relation

	public abstract ChildRelationExt getChildRelation();

	// event handlers

	/**
	 * Service activation event handling.
	 * 
	 * @param event
	 * @param aci
	 */
	public void onServiceStartedEvent(ServiceStartedEvent event,
			ActivityContextInterface aci) {

		long start = System.currentTimeMillis();

		// lets attach to the pidf manipulation app usage users collection
		// activity, to receive events related with
		// updates on its docs
		CollectionActivity collectionActivity = dataSourceSbbInterface
				.createCollectionActivity("pidf-manipulation/users");
		ActivityContextInterface appUsageActivityContextInterface = dataSourceActivityContextInterfaceFactory
				.getActivityContextInterface(collectionActivity);
		appUsageActivityContextInterface.attach(context.getSbbLocalObject());
		// now fetch all existent docs and simulation doc creation
		try {
			Document[] documents = dataSourceSbbInterface.getDocuments(
					collectionActivity.getCollection(), true);
			for (Document document : documents) {
				try {
					documentCreated(
							new DocumentSelector(document.getCollection(),
									document.getDocumentName()).getUser(),
							document.getDocumentName(), document.getAsString());
				} catch (InternalServerErrorException e) {
					tracer.severe("unable to start publication for entity "
							+ new DocumentSelector(document.getCollection(),
									document.getDocumentName()).getUser(), e);
				}
			}
		} catch (InternalServerErrorException e) {
			tracer.severe("unable to fetch current pidf manipulation docs", e);
		}
		tracer.info("Total time to init pidf manipulation publications: "
				+ (System.currentTimeMillis() - start) + "ms");
	}

	/**
	 * Service deactivation event handling.
	 * 
	 * @param event
	 * @param aci
	 */
	public void onActivityEndEvent(ActivityEndEvent event,
			ActivityContextInterface aci) {
		if (aci.getActivity() instanceof ServiceActivity) {
			// REMOVE ALL PUBLICATIONS
			PIDFManipulationChildSbbLocalObject child = null;
			for (Iterator<?> it = getChildRelation().iterator(); it.hasNext();) {
				child = (PIDFManipulationChildSbbLocalObject) it.next();
				child.removePublication();
				it.remove();
			}
		}
	}

	/**
	 * An attribute was updated in the Datasource.
	 * 
	 * @param event
	 * @param aci
	 */
	public void onAttributeUpdatedEvent(AttributeUpdatedEvent event,
			ActivityContextInterface aci) {
		documentUpdated(event.getDocumentSelector().getUser(), event
				.getDocumentSelector().getDocumentName(),
				event.getNewDocumentString());
	}

	/**
	 * A doc was updated in the Datasource
	 * 
	 * @param event
	 * @param aci
	 */
	public void onDocumentUpdatedEvent(DocumentUpdatedEvent event,
			ActivityContextInterface aci) {
		if (event.getOldDocument() == null) {
			// new doc
			documentCreated(event.getDocumentSelector().getUser(), event
					.getDocumentSelector().getDocumentName(),
					event.getNewDocumentString());
		} else if (event.getNewETag() == null) {
			// doc removed
			documentRemoved(event.getDocumentSelector().getUser(), event
					.getDocumentSelector().getDocumentName());
		} else {
			// doc update
			documentUpdated(event.getDocumentSelector().getUser(), event
					.getDocumentSelector().getDocumentName(),
					event.getNewDocumentString());
		}
	}

	/**
	 * An element was updated in the Datasource
	 * 
	 * @param event
	 * @param aci
	 */
	public void onElementUpdatedEvent(ElementUpdatedEvent event,
			ActivityContextInterface aci) {
		documentUpdated(event.getDocumentSelector().getUser(), event
				.getDocumentSelector().getDocumentName(),
				event.getNewDocumentString());
	}

	// ----- aux

	private void documentCreated(String entity, String documentName,
			String documentContent) {
		String childName = getChildName(entity, documentName);
		PIDFManipulationChildSbbLocalObject child = null;
		try {
			child = (PIDFManipulationChildSbbLocalObject) getChildRelation()
					.create(childName);
		} catch (Throwable e) {
			tracer.severe("Failed to create child sbb");
			return;
		}
		child.newPublication(entity, documentContent);
	}

	private String getChildName(String entity, String documentName) {
		return new StringBuilder(entity).append('/').append(documentName)
				.toString();
	}

	private void documentUpdated(String entity, String documentName,
			String documentContent) {
		String childName = getChildName(entity, documentName);
		PIDFManipulationChildSbbLocalObject child = (PIDFManipulationChildSbbLocalObject) getChildRelation()
				.get(childName);
		if (child != null) {
			child.modifyPublication(documentContent);
		} else {
			// the last publication failed, let's try to reset it
			documentCreated(entity, documentName, documentContent);
		}
	}

	private void documentRemoved(String entity, String documentName) {
		String childName = getChildName(entity, documentName);
		PIDFManipulationChildSbbLocalObject child = (PIDFManipulationChildSbbLocalObject) getChildRelation()
				.get(childName);
		if (child != null) {
			child.removePublication();
			child.remove();
		}
	}

	// unused sbb life cycle methods

	@Override
	public void sbbActivate() {

	}

	@Override
	public void sbbCreate() throws CreateException {

	}

	@Override
	public void sbbExceptionThrown(Exception exception, Object event,
			ActivityContextInterface aci) {

	}

	@Override
	public void sbbLoad() {

	}

	@Override
	public void sbbPassivate() {

	}

	@Override
	public void sbbPostCreate() throws CreateException {

	}

	@Override
	public void sbbRemove() {

	}

	@Override
	public void sbbRolledBack(RolledBackContext context) {

	}

	@Override
	public void sbbStore() {

	}

}
