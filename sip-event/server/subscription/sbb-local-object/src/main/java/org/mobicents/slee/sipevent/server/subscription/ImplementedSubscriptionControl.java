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

/**
 * 
 */
package org.mobicents.slee.sipevent.server.subscription;

import javax.sip.ServerTransaction;

import org.mobicents.slee.sipevent.server.subscription.data.Notifier;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;

/**
 * @author martins
 * 
 */
public interface ImplementedSubscriptionControl {

	/**
	 * Asks authorization to concrete implementation for new subscription
	 * request SUBSCRIBE. This method is invoked from the abstract sip event
	 * subscription control to authorize a subscriber, the concrete
	 * implemeentation must then invoke newSubscriptionAuthorization(...) so the
	 * new subscription process is completed
	 * 
	 * @param eventList
	 * 
	 * @param serverTransaction
	 *            in case it is a sip subscription the server transaction must
	 *            be provided to be used later when providing the response
	 * 
	 * @return
	 */
	public void isSubscriberAuthorized(String subscriber,
			String subscriberDisplayName, Notifier notifier,
			SubscriptionKey key, int expires, String content,
			String contentType, String contentSubtype, boolean eventList,
			ServerTransaction serverTransaction);

	/**
	 * Retrieves the content for the NOTIFY request of the specified
	 * Subscription.
	 * 
	 * This assumes the returned result is filtered if there was any rules that
	 * applies to the subscription.
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
	public Object filterContentPerSubscriber(Subscription subscription,
			Object unmarshalledContent);

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
	 * Indicates if the implementation accepts event lists, that is, if it makes
	 * sense to act as a RLS.
	 * 
	 * @return
	 */
	public boolean acceptsEventList();
}
