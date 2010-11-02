package org.openxdm.xcap.server.slee.appusage.xcapcaps;

import java.io.ByteArrayInputStream;

import javax.slee.ActivityContextInterface;
import javax.slee.ActivityEndEvent;
import javax.slee.ChildRelation;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.facilities.Tracer;
import javax.slee.serviceactivity.ServiceActivity;

import org.mobicents.slee.xdm.server.ServerConfiguration;
import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageAddedEvent;
import org.mobicents.xdm.server.appusage.AppUsageManagement;
import org.mobicents.xdm.server.appusage.AppUsagePool;
import org.mobicents.xdm.server.appusage.AppUsageRemovedEvent;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.ResourceSelector;
import org.openxdm.xcap.server.slee.RequestProcessorSbbLocalObject;

/**
 * JAIN SLEE Root Sbb for xcap-caps Xcap application usage.  
 * @author Eduardo Martins
 *
 */
public abstract class XCAPCapsAppUsageSbb implements Sbb {

	private SbbContext sbbContext;
	private Tracer tracer;

	/**
	 * Called when an sbb object is instantied and enters the pooled state.
	 */
	public void setSbbContext(SbbContext context) {
		this.sbbContext = context;
		this.tracer = context.getTracer(getClass().getSimpleName());
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
	
	public abstract ChildRelation getRequestProcessorChildRelation();

	private RequestProcessorSbbLocalObject getRequestProcessor()
			throws InternalServerErrorException {
		// get the child relation
		ChildRelation childRelation = getRequestProcessorChildRelation();
		// creates the child sbb if does not exist
		if (childRelation.isEmpty()) {
			try {
				return (RequestProcessorSbbLocalObject) childRelation.create();
			} catch (Exception e) {
				tracer.severe("unable to create the child sbb.", e);
				throw new InternalServerErrorException("");
			}
		} else {
			// return the child sbb
			return (RequestProcessorSbbLocalObject) childRelation.iterator()
					.next();
		}
	}

	// EVENT HANDLERS
	
	public void onAppUsageAddedEvent(AppUsageAddedEvent event, ActivityContextInterface aci) {
		aci.detach(sbbContext.getSbbLocalObject());
		if (!event.getAuid().equals(XCAPCapsAppUsage.ID)) {
			// update xcap caps global doc
			try {
				updateXCAPCapsGlobalDoc();
			} catch (InternalServerErrorException e) {
				tracer.severe("failed to update xcap caps global doc", e);
			}
		}
	}
	
	public void onAppUsageRemovedEvent(AppUsageRemovedEvent event, ActivityContextInterface aci) {
		aci.detach(sbbContext.getSbbLocalObject());
		if (!event.getAuid().equals(XCAPCapsAppUsage.ID)) {
			// update xcap caps global doc
			try {
				updateXCAPCapsGlobalDoc();
			} catch (InternalServerErrorException e) {
				tracer.severe("failed to update xcap caps global doc", e);
			}
		}
	}
		
	private void updateXCAPCapsGlobalDoc() throws InternalServerErrorException {

		// we can't use the xcap caps app usage class, may not be loaded
		final String xcapCapsAUID = "xcap-caps";
		final String xcapCapsMimetype = "application/xcap-caps+xml";

		// create xcap-caps global/index doc
		StringBuilder sb1 = new StringBuilder(
		"<?xml version='1.0' encoding='UTF-8'?><xcap-caps xmlns='urn:ietf:params:xml:ns:xcap-caps'><auids>");
		StringBuilder sb2 = new StringBuilder(
		"</auids><extensions/><namespaces>");
		AppUsageManagement appUsageManagement = AppUsageManagement.getInstance();
		AppUsagePool xcapCapsAppUsagePool = null;
		AppUsage xcapCapsAppUsage = null;
		for (String auid : appUsageManagement.getAppUsages()) {
			AppUsagePool appUsagePool = appUsageManagement.getAppUsagePool(auid);
			if (appUsagePool != null) {
				// borrow one app usage object from cache
				AppUsage appUsage = appUsagePool.borrowInstance();
				// add auid and namespace
				sb1.append("<auid>").append(appUsage.getAUID()).append(
				"</auid>");
				sb2.append("<namespace>").append(
						appUsage.getDefaultDocumentNamespace()).append(
								"</namespace>");
				if (auid.equals(xcapCapsAUID)) {
					xcapCapsAppUsage = appUsage;
					xcapCapsAppUsagePool = appUsagePool;
				} else {
					// release app usage object
					appUsagePool.returnInstance(appUsage);
				}
			}
		}
		sb1.append(sb2).append("</namespaces></xcap-caps>");

		if (xcapCapsAppUsage != null) {
			try {
				getRequestProcessor().put(
						new ResourceSelector("/" + xcapCapsAUID
								+ "/global/index", null),
								xcapCapsMimetype,
								new ByteArrayInputStream(sb1.toString().getBytes(
								"utf-8")), null,
								ServerConfiguration.getInstance().getXcapRoot(),null);
			} catch (Exception e) {
				throw new InternalServerErrorException(
						"Failed to put xcap-caps global document. Cause: "
						+ e.getCause() + " Message:"
						+ e.getMessage());
			}
			// release app usage object
			xcapCapsAppUsagePool.returnInstance(xcapCapsAppUsage);
		}

	}
	
	public void onServiceStartedEvent(
			javax.slee.serviceactivity.ServiceStartedEvent event,
			ActivityContextInterface aci) {
		new XCAPCapsAppUsageDeployer().start();
		// update xcap caps global doc
		try {
			updateXCAPCapsGlobalDoc();
		} catch (InternalServerErrorException e) {
			tracer.severe("failed to update xcap caps global doc", e);
		}		
	}

	public void onActivityEndEvent(ActivityEndEvent event,
			ActivityContextInterface aci) {
		if (aci.getActivity() instanceof ServiceActivity) {
			// service activity ending
			new XCAPCapsAppUsageDeployer().stop();
		}
	}
}