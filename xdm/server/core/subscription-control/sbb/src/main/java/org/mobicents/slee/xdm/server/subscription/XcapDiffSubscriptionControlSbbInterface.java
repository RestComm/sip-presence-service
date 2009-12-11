/**
 * 
 */
package org.mobicents.slee.xdm.server.subscription;

import javax.sip.header.HeaderFactory;
import javax.xml.bind.Unmarshaller;

import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControl;
import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlParentSbbLocalObject;
import org.mobicents.slee.xdm.server.XDMClientControlParent;
import org.mobicents.slee.xdm.server.XDMClientControlSbbLocalObject;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceSbbInterface;

/**
 * @author martins
 *
 */
public interface XcapDiffSubscriptionControlSbbInterface extends XDMClientControlParent,
ImplementedSubscriptionControl {

	public void setSubscriptionsMap(SubscriptionsMap rules);

	public SubscriptionsMap getSubscriptionsMap();

	public ImplementedSubscriptionControlParentSbbLocalObject getParentSbbCMP();

	public XDMClientControlSbbLocalObject getXDMClientControlSbb();

	public HeaderFactory getHeaderFactory();

	public Unmarshaller getUnmarshaller();

	public DataSourceSbbInterface getDataSourceSbbInterface();
}
