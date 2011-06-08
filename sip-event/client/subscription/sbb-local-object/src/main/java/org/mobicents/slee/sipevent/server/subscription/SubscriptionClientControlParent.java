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

import org.mobicents.slee.sipevent.server.subscription.data.Subscription;

/**
 * @author martins
 *
 */
public interface SubscriptionClientControlParent {

	/**
	 * informs the parent sbb that a subscribe request was successful
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param expires
	 * @param responseCode
	 *            OK or CREATED
	 */
	public void subscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires,
			int responseCode);

	/**
	 * informs the parent sbb that a subscribe request was not successful
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param error
	 *            the sip error response status code
	 */
	public void subscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error);

	/**
	 * informs the parent sbb that a resubscribe request was successful
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param expires
	 */
	public void resubscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires);

	/**
	 * informs the parent sbb that a resubscribe request was not successful
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param error
	 *            the sip error response status code
	 */
	public void resubscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error);

	/**
	 * informs the parent sbb that a unsubscribe request was successful
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 */
	public void unsubscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId);

	/**
	 * informs the parent sbb that a unsubscribe request was not successful
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param error
	 *            the sip error response status code
	 */
	public void unsubscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error);

	/**
	 * Notifies the client.
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param status
	 * @param terminationReason if occurs an unexpected change that terminates the subscription a reason code will be provided
	 * @param document
	 * @param contentType
	 * @param contentSubtype
	 */
	public void notifyEvent(String subscriber, String notifier,
			String eventPackage, String subscriptionId,
			Subscription.Event terminationReason, Subscription.Status status,
			String content, String contentType, String contentSubtype);
	
}
