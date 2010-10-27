/**
 * 
 */
package org.mobicents.slee.xdm.server.subscription;

import javax.sip.header.HeaderFactory;
import javax.slee.SbbContext;
import javax.xml.bind.Unmarshaller;

import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControl;
import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlParentSbbLocalObject;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceActivityContextInterfaceFactory;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceSbbInterface;

/**
 * @author martins
 *
 */
public interface XcapDiffSubscriptionControlSbbInterface extends ImplementedSubscriptionControl {

	public DataSourceActivityContextInterfaceFactory getDataSourceActivityContextInterfaceFactory();

	public DataSourceSbbInterface getDataSourceSbbInterface();

	public HeaderFactory getHeaderFactory();

	public ImplementedSubscriptionControlParentSbbLocalObject getParentSbbCMP();

	public SbbContext getSbbContext();
	
	public SubscriptionsMap getSubscriptionsMap();

	public Unmarshaller getUnmarshaller();

	public void setSubscriptionsMap(SubscriptionsMap rules);
}
