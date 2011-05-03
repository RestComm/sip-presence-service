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

package org.mobicents.slee.sippresence.server.subscription.rules;

import java.io.Serializable;

import org.mobicents.slee.sipevent.server.subscription.data.Notifier;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;

public class PresRuleCMPKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1212963002333839692L;
	
	private final String subscriber;
	private final Notifier notifier;
	private final SubscriptionKey subscriptionKey;
	
	public PresRuleCMPKey(String subscriber, Notifier notifier, SubscriptionKey subscriptionKey) {
		this.subscriber = subscriber;
		this.notifier = notifier;
		this.subscriptionKey = subscriptionKey;
	}
	
	public Notifier getNotifier() {
		return notifier;
	}
	
	public String getSubscriber() {
		return subscriber;
	}
	
	public SubscriptionKey getSubscriptionKey() {
		return subscriptionKey;
	}
	
	
	
	@Override
	public int hashCode() {
		return subscriptionKey.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj.getClass() == this.getClass()) {
			PresRuleCMPKey other = (PresRuleCMPKey)obj;
			return this.subscriptionKey.equals(other.subscriptionKey);
		}
		else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "PresRuleCMPKey{ subscriber = "+subscriber+" , subscriptionKey = "+subscriptionKey+" }";
	}
}
