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

package org.mobicents.slee.sipevent.server.subscription;

import org.mobicents.sipevent.server.subscription.util.AbstractEvent;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;

/**
 * Event that is fired on a subscription aci, to end the subscription.
 * 
 * @author martins
 * 
 */
public class TerminateSubscriptionEvent extends AbstractEvent {

	private final SubscriptionKey subscriptionKey;
	
	public TerminateSubscriptionEvent(SubscriptionKey subscriptionKey) {
		super();
		this.subscriptionKey = subscriptionKey;
	}
	
	public SubscriptionKey getSubscriptionKey() {
		return subscriptionKey;
	}
	
	private String toString = null;
	@Override
	public String toString() {
		if (toString == null) {
			toString = new StringBuilder("TERMINATE SUBSCRIPTION EVENT [").append(String.valueOf(subscriptionKey))
				.append("]").toString();
		}
		return toString;
	}
}
