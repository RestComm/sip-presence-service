/**
 * 
 */
package org.mobicents.slee.sipevent.server.subscription;

import javax.sip.ServerTransaction;
import javax.xml.bind.Marshaller;

import org.mobicents.slee.sipevent.server.subscription.pojo.Subscription;
import org.mobicents.slee.sipevent.server.subscription.pojo.SubscriptionKey;

/**
 * @author martins
 *
 */
public interface ImplementedSubscriptionControl {

	/**
	 * Used to set the call back sbb local object in the sbb implementing this
	 * interface. Must be used whenever a new object of this interface is
	 * created.
	 * 
	 * An example:
	 * 
	 * ChildRelation childRelation = getChildRelation();
	 * ImplementedSubscriptionControlSbbLocalObject childSbb =
	 * (ImplementedSubscriptionControlSbbLocalObject) childRelation.create();
	 * childSbb.setParentSbb(
	 * (ImplementedSubscriptionControlParentSbbLocalObject)this.getSbbContext().getSbbLocalObject());
	 * 
	 * 
	 * @param parent
	 */
	public void setParentSbb(
			ImplementedSubscriptionControlParentSbbLocalObject sbbLocalObject);

	/**
	 * Asks authorization to concrete implementation for new subscription
	 * request SUBSCRIBE. This method is invoked from the abstract sip event
	 * subscription control to authorize a subscriber, the concrete
	 * implemeentation must then invoke newSubscriptionAuthorization(...) so the
	 * new subscription process is completed
	 * @param eventList 
	 * 
	 * @param serverTransaction
	 *            in case it is a sip subscription the server transaction must
	 *            be provided to be used later when providing the response
	 * 
	 * @return
	 */
	public void isSubscriberAuthorized(String subscriber,
			String subscriberDisplayName, String notifier, SubscriptionKey key,
			int expires, String content, String contentType,
			String contentSubtype, boolean eventList, ServerTransaction serverTransaction);

	/**
	 * Retrieves the content for the NOTIFY request of the specified
	 * Subscription
	 * 
	 * @param subscription
	 * @return
	 */
	public NotifyContent getNotifyContent(Subscription subscription);

	/**
	 * Filters content per subscriber.
	 * 
	 * @return content filtered
	 */
	public Object filterContentPerSubscriber(String subscriber,
			String notifier, String eventPackage, Object unmarshalledContent);

	/**
	 * Retrieves a JAXB Marshaller to convert a JAXBElement to a String.
	 * 
	 * @return
	 */
	public Marshaller getMarshaller();

	/**
	 * notifies the event package impl that a subscription is about to be
	 * removed, may have resources to releases
	 */
	public void removingSubscription(Subscription subscription);

	/**
	 * the event packages supported
	 * 
	 * @return
	 */
	public String[] getEventPackages();
	
	/**
	 * Indicates if the implementation accepts event lists, that is, if it makes sense to act as a RLS.
	 * @return
	 */
	public boolean acceptsEventList();
}
