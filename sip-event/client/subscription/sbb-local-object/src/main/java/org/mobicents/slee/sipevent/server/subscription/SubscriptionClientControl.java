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

/**
 * @author martins
 * 
 */
public interface SubscriptionClientControl {

	/**
	 * creates an internal subscription
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param expires
	 * @param content
	 * @param contentType
	 * @param contentSubtype
	 */
	public void subscribe(String subscriber, String subscriberdisplayName,
			String notifier, String eventPackage, String subscriptionId,
			int expires, String content, String contentType,
			String contentSubtype);

	/**
	 * refreshes an internal subscription
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 * @param expires
	 */
	public void resubscribe(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires);

	/**
	 * Requests the termination of an internal subscription.
	 * 
	 * @param subscriber
	 * @param notifier
	 * @param eventPackage
	 * @param subscriptionId
	 */
	public void unsubscribe(String subscriber, String notifier,
			String eventPackage, String subscriptionId);

}
