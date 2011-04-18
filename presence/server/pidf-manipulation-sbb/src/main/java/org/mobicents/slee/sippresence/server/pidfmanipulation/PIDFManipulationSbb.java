package org.mobicents.slee.sippresence.server.pidfmanipulation;

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

import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;
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
public abstract class PIDFManipulationSbb implements Sbb {

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

	// event handlers

	public void onServiceStartedEvent(
			ServiceStartedEvent event,
			ActivityContextInterface aci) {
		long start = System.currentTimeMillis();

		// lets attach to the app usage activity, to receive events related with
		// updates on its docs
		AppUsageActivity appUsageActivity = dataSourceSbbInterface
				.createAppUsageActivity("pidf-manipulation");
		ActivityContextInterface appUsageActivityContextInterface = dataSourceActivityContextInterfaceFactory
				.getActivityContextInterface(appUsageActivity);
		appUsageActivityContextInterface.attach(context.getSbbLocalObject());
		// now fetch all existent docs
		DocumentSelector documentSelector = null;
		try {
			Document[] documents = dataSourceSbbInterface
					.getDocuments(appUsageActivity.getAUID());
			for (Document document : documents) {
				documentSelector = new DocumentSelector(appUsageActivity.getAUID(),
						document.getDocumentParent(),
						document.getDocumentName());
				if (tracer.isFineEnabled()) {
					tracer.fine("Retrieving document " + documentSelector);
				}
				// TODO CREATE PUBLICATION
			}
		} catch (InternalServerErrorException e) {
			tracer.severe("unable to fetch current pres rules docs", e);
		}
		tracer.info("Total time to init pres rules cache: "
				+ (System.currentTimeMillis() - start) + "ms");
	}

	public void onActivityEndEvent(
			ActivityEndEvent event,
			ActivityContextInterface aci) {
		if (aci.getActivity() instanceof ServiceActivity) {
			// TODO REMOVE ALL PUBLICATIONS
			context.getSbbLocalObject().remove();			
		}
	}

	public void onAttributeUpdatedEvent(AttributeUpdatedEvent event,
			ActivityContextInterface aci) {
		publicationUpdated();
	}

	public void onDocumentUpdatedEvent(DocumentUpdatedEvent event,
			ActivityContextInterface aci) {
		publicationUpdated();
	}

	public void onElementUpdatedEvent(ElementUpdatedEvent event,
			ActivityContextInterface aci) {
		publicationUpdated();
	}
	
	private void publicationUpdated() {
		// TODO UPDATE PUBLICATION
	}
}
