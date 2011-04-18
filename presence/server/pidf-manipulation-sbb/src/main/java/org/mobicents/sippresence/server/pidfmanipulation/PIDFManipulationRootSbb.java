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
import org.openxdm.xcap.server.slee.resource.datasource.AppUsageActivity;
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

		// lets attach to the app usage activity, to receive events related with
		// updates on its docs
		AppUsageActivity appUsageActivity = dataSourceSbbInterface
				.createAppUsageActivity("pidf-manipulation");
		ActivityContextInterface appUsageActivityContextInterface = dataSourceActivityContextInterfaceFactory
				.getActivityContextInterface(appUsageActivity);
		appUsageActivityContextInterface.attach(context.getSbbLocalObject());
		// now fetch all existent docs and simulation doc creation
		try {
			Document[] documents = dataSourceSbbInterface
					.getDocuments(appUsageActivity.getAUID());
			for (Document document : documents) {
				try {
					documentCreated(getEntity(document.getDocumentParent()),
							document.getDocumentName(), document.getAsString());
				} catch (InternalServerErrorException e) {
					tracer.severe("unable to start publication for entity "
							+ getEntity(document.getDocumentParent()), e);
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
		documentUpdated(getEntity(event.getDocumentSelector()
				.getDocumentParent()), event.getDocumentSelector()
				.getDocumentName(), event.getDocumentAsString());
	}

	/**
	 * A doc was updated in the Datasource
	 * 
	 * @param event
	 * @param aci
	 */
	public void onDocumentUpdatedEvent(DocumentUpdatedEvent event,
			ActivityContextInterface aci) {
		if (event.getOldETag() == null) {
			// new doc
			documentCreated(getEntity(event.getDocumentSelector()
					.getDocumentParent()), event.getDocumentSelector()
					.getDocumentName(), event.getDocumentAsString());
		} else if (event.getNewETag() == null) {
			// doc removed
			documentRemoved(getEntity(event.getDocumentSelector()
					.getDocumentParent()), event.getDocumentSelector()
					.getDocumentName());
		} else {
			// doc update
			documentUpdated(getEntity(event.getDocumentSelector()
					.getDocumentParent()), event.getDocumentSelector()
					.getDocumentName(), event.getDocumentAsString());
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
		documentUpdated(getEntity(event.getDocumentSelector()
				.getDocumentParent()), event.getDocumentSelector()
				.getDocumentName(), event.getDocumentAsString());
	}

	// ----- aux

	private void documentCreated(String entity, String documentName,
			String documentContent) {
		String childName = getChildName(entity, documentName);
		PIDFManipulationChildSbbLocalObject child = null;
		try {
			child = (PIDFManipulationChildSbbLocalObject) getChildRelation().create(childName);
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

	private String getEntity(String documentParent) {
		// document parent is users/ + entity
		return documentParent.substring(6);
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
