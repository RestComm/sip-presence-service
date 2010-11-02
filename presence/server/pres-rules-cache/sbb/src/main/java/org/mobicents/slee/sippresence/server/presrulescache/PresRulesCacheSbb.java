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
import org.openxdm.xcap.server.slee.resource.datasource.AppUsageActivity;
import org.openxdm.xcap.server.slee.resource.datasource.AttributeUpdatedEvent;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceActivityContextInterfaceFactory;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceSbbInterface;
import org.openxdm.xcap.server.slee.resource.datasource.DocumentUpdatedEvent;
import org.openxdm.xcap.server.slee.resource.datasource.ElementUpdatedEvent;

public abstract class PresRulesCacheSbb implements Sbb {

	private static final SipPresenceServerManagement configuration = SipPresenceServerManagement.getInstance();

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

		String presRulesAUID = configuration.getPresRulesAUID();
		// lets attach to the app usage activity, to receive events related with updates on its docs
		AppUsageActivity appUsageActivity = dataSourceSbbInterface.createAppUsageActivity(presRulesAUID);
		ActivityContextInterface appUsageActivityContextInterface = dataSourceActivityContextInterfaceFactory.getActivityContextInterface(appUsageActivity);
		appUsageActivityContextInterface.attach(context.getSbbLocalObject());
		setPresRulesAppUsageACI(appUsageActivityContextInterface);
		// now fetch all existent docs
		DocumentSelector documentSelector = null;
		try {
			Document[] documents = dataSourceSbbInterface.getDocuments(presRulesAUID);
			for (Document document : documents) {
				documentSelector = new DocumentSelector(presRulesAUID, document.getDocumentParent(), document.getDocumentName());
				if (tracer.isFineEnabled()) {
					tracer.fine("Retrieving document "+documentSelector);
				}
				presRulesSbbInterface.rulesetUpdated(documentSelector, null, document.getETag(), document.getAsString());					
			}
		} catch (InternalServerErrorException e) {
			tracer.severe("unable to fetch current pres rules docs",e);
		}
		tracer.info("Total time to update pres rules cache with initial rls-services docs: "+(System.currentTimeMillis()-start)+"ms");
	}
	
	public void onUnsubscribePresRulesAppUsageEvent(UnsubscribePresRulesAppUsageEvent event, ActivityContextInterface aci) {
		SbbLocalObject sbbLocalObject = context.getSbbLocalObject();
		ActivityContextInterface appUsageActivityContextInterface = getPresRulesAppUsageACI();
		if (appUsageActivityContextInterface != null) {
			appUsageActivityContextInterface.detach(sbbLocalObject);
		}
		aci.detach(sbbLocalObject);
	}
	
	public void onAttributeUpdatedEvent(AttributeUpdatedEvent event,
			ActivityContextInterface aci) {
		presRulesSbbInterface.rulesetUpdated(event.getDocumentSelector(), event.getOldETag(), event.getNewETag(), event.getDocumentAsString());
	}

	public void onDocumentUpdatedEvent(DocumentUpdatedEvent event,
			ActivityContextInterface aci) {
		presRulesSbbInterface.rulesetUpdated(event.getDocumentSelector(), event.getOldETag(), event.getNewETag(), event.getDocumentAsString());
	}

	public void onElementUpdatedEvent(ElementUpdatedEvent event,
			ActivityContextInterface aci) {
		presRulesSbbInterface.rulesetUpdated(event.getDocumentSelector(), event.getOldETag(), event.getNewETag(), event.getDocumentAsString());
	}
}
