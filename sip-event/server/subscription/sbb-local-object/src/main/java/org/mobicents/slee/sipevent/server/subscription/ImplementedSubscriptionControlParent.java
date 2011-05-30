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
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;

/**
 * @author martins
 *
 */
public interface ImplementedSubscriptionControlParent {

	/**
	 * Used by {@link ImplementedSubscriptionControlSbbLocalObject} to notify
	 * that the authorization of a subscription has changed
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param eventId
	 * @param authorizationCode
	 */
	public void authorizationChanged(String subscriber, Notifier notifier,
			String eventPackage, String eventId, int authorizationCode);

	/**
	 * 
	 * Used by {@link ImplementedSubscriptionControlSbbLocalObject} to provide
	 * the authorization to a new subscription request.
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param key
	 * @param expires
	 * @param responseCode
	 * @param serverTransaction
	 *            if the subscription request was for a sip subscription then
	 *            this param must provide the server transaction provided on the
	 *            authorization request
	 */
	public void newSubscriptionAuthorization(String subscriber,
			String subscriberDisplayName, Notifier notifier, SubscriptionKey key,
			int expires, int responseCode, boolean eventList, ServerTransaction serverTransaction);

	/**
	 * Through this method the subscription control sbb can be informed that the
	 * state of the notifier has changed, allowing subscribers to be notified.
	 * 
	 * @param notifier
	 * @param eventPackage
	 * @param notifyContent
	 */
	public void notifySubscribers(String notifier, String eventPackage,
			NotifyContent notifyContent);

	/**
	 * Requests notification on a specific subscription, providing the content.
	 * 
	 * @param key
	 * @param notifyContent
	 */
	public void notifySubscriber(SubscriptionKey key, 
			NotifyContent notifyContent);
}
