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

package org.mobicents.slee.xdm.server.subscription;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.openxdm.xcap.common.uri.DocumentSelector;

public class Subscriptions implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final SubscriptionKey key;
	private final String subscriber;
	
	private transient Set<String> appUsages;
	private transient Set<DocumentSelector> documentSelectors;
	
	public Subscriptions(SubscriptionKey key, String subscriber, Set<String> appUsages,
			Set<DocumentSelector> documentSelectors) {
		this.key = key;
		this.subscriber = subscriber;
		this.appUsages = appUsages;
		this.documentSelectors = documentSelectors;
		filter();
	}
	
	/*
	 * removes resources that are contained in other resources
	 */
	private void filter() {
		for (Iterator<DocumentSelector> i=documentSelectors.iterator();i.hasNext();) {
			DocumentSelector ds = (DocumentSelector) i.next();
			if(appUsages.contains(ds.getAUID())) {
				// we don't need this resource
				i.remove();
			}
		}
	}
	
	public SubscriptionKey getKey() {
		return key;
	}
	
	/**
	 * @return the subscriber
	 */
	public String getSubscriber() {
		return subscriber;
	}
	
	public Set<String> getAppUsages() {
		return appUsages;
	}
	
	public Set<DocumentSelector> getDocumentSelectors() {
		return documentSelectors;
	}
	
	// serialization
	
	private final static String[] EMPTY_STRING_ARRAY = {};
	private final static DocumentSelector[] EMPTY_DOCUMENTSELECTOR_ARRAY = {};

	private void writeObject(ObjectOutputStream stream) throws IOException {
		
		// write everything not static or transient
		stream.defaultWriteObject();
		
		// write app usages as array
		int arraySize = appUsages.size();
		String[] appUsagesArray;
		if (arraySize == 0) {
			appUsagesArray = EMPTY_STRING_ARRAY;
		}
		else {
			appUsagesArray = appUsages.toArray(new String[arraySize]);
		}
		stream.writeObject(appUsagesArray);
		
		// write document selectors as array
		arraySize = documentSelectors.size();
		DocumentSelector[] documentSelectorsArray;
		if (arraySize == 0) {
			documentSelectorsArray = EMPTY_DOCUMENTSELECTOR_ARRAY;
		}
		else {
			documentSelectorsArray = documentSelectors.toArray(new DocumentSelector[arraySize]);
		}
		stream.writeObject(documentSelectorsArray);
	}
	
	private void readObject(ObjectInputStream stream)  throws IOException, ClassNotFoundException {
				
		stream.defaultReadObject();

		// read app usages from array
		String[] appUsagesArray = (String[]) stream.readObject();
		appUsages = new HashSet<String>();
		for (String appUsage : appUsagesArray) {
			appUsages.add(appUsage);
		}
		
		// read document selectors from array
		DocumentSelector[] documentSelectorsArray = (DocumentSelector[]) stream.readObject();
		documentSelectors = new HashSet<DocumentSelector>();
		for (DocumentSelector documentSelector : documentSelectorsArray) {
			documentSelectors.add(documentSelector);
		}
		
	}
	
}
