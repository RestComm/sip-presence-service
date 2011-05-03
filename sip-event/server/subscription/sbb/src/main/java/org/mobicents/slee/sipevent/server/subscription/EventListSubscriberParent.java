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
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.mobicents.slee.sipevent.server.subscription.eventlist.MultiPart;

/**
 * @author martins
 *
 */
public interface EventListSubscriberParent {

	/**
	 * Requests notification on a specific subscription, providing the multipart content.
	 * @param key
	 * @param multipart
	 */
	public void notifyEventListSubscriber(SubscriptionKey key, MultiPart multipart);
	
	/**
	 * Requests the subscription for the specified key. The subscription's entity manager must be available.
	 * @param key
	 * @return
	 */
	public Subscription getSubscription(SubscriptionKey key);

}
