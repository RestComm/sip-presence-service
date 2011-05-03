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
package org.mobicents.slee.xdm.server.subscription;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;

/**
 * @author martins
 *
 */
public class SubscriptionsMap implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private transient Map<SubscriptionKey,Subscriptions> map;
	
	/**
	 * 
	 */
	public SubscriptionsMap() {
		map = new HashMap<SubscriptionKey, Subscriptions>();
	}
		
	public Collection<Subscriptions> getSubscriptions() {
		return map.values();
	}

	/**
	 * @param key
	 * @return
	 */
	public Subscriptions get(SubscriptionKey key) {
		return map.get(key);
	}

	/**
	 * @param subscriptions
	 */
	public void put(Subscriptions subscriptions) {
		map.put(subscriptions.getKey(), subscriptions);		
	}

	/**
	 * @param key
	 * @return
	 */
	public Subscriptions remove(SubscriptionKey key) {
		return map.remove(key);
	}

	// serialization
	
	private static final Subscriptions[] EMPTY_ARRAY = {};

	private void writeObject(ObjectOutputStream stream) throws IOException {
		
		// write everything not static or transient
		stream.defaultWriteObject();
		
		// write map values as array
		final Collection<Subscriptions> subscriptions = map.values();
		int arraySize = subscriptions.size();
		Subscriptions[] subscriptionsArray;
		if (arraySize == 0) {
			subscriptionsArray = EMPTY_ARRAY;
		}
		else {
			subscriptionsArray = subscriptions.toArray(new Subscriptions[arraySize]);
		}
		stream.writeObject(subscriptionsArray);
		
	}
	
	private void readObject(ObjectInputStream stream)  throws IOException, ClassNotFoundException {
				
		stream.defaultReadObject();

		// read from array
		Subscriptions[] subscriptionsArray = (Subscriptions[]) stream.readObject();
		map = new HashMap<SubscriptionKey,Subscriptions>();
		for (Subscriptions s : subscriptionsArray) {
			map.put(s.getKey(), s);
		}		
		
	}
}
