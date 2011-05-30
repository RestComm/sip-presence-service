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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.server.slee.resource.datasource.NodeSubscription;

public class Subscriptions implements Externalizable {

	private static final Set<String> EMPTY_COLLECTION_SUBSCRIPTIONS = Collections
			.unmodifiableSet(new HashSet<String>());
	private static final Set<DocumentSelector> EMPTY_DOCUMENT_SUBSCRIPTIONS = Collections
			.unmodifiableSet(new HashSet<DocumentSelector>());
	private static final Set<NodeSubscription> EMPTY_NODESUBSCRIPTIONS_SUBSCRIPTIONS = Collections
			.unmodifiableSet(new HashSet<NodeSubscription>());

	private SubscriptionKey key;
	private String subscriber;
	private DiffProcessing diffProcessing;
	
	private Set<String> collectionSubscriptions;
	private Set<DocumentSelector> documentSubscriptions;
	private Set<NodeSubscription> nodeSubscriptions;

	public Subscriptions() {
		// required by externalizable
	}

	public Subscriptions(SubscriptionKey key, String subscriber,
			Set<String> collectionSubscriptions,
			Set<DocumentSelector> documentSubscriptions,
			Set<NodeSubscription> nodeSubscriptions, DiffProcessing diffProcessing) {
		this.key = key;
		this.subscriber = subscriber;
		this.collectionSubscriptions = collectionSubscriptions != null ? collectionSubscriptions
				: EMPTY_COLLECTION_SUBSCRIPTIONS;
		this.documentSubscriptions = documentSubscriptions != null ? documentSubscriptions
				: EMPTY_DOCUMENT_SUBSCRIPTIONS;
		this.nodeSubscriptions = nodeSubscriptions != null ? nodeSubscriptions
				: EMPTY_NODESUBSCRIPTIONS_SUBSCRIPTIONS;
		this.diffProcessing = diffProcessing;
		filter();
	}

	/*
	 * removes resources that are contained in other resources
	 */
	private void filter() {
		for (Iterator<NodeSubscription> i = nodeSubscriptions.iterator(); i
				.hasNext();) {
			DocumentSelector ds = (DocumentSelector) i.next()
					.getDocumentSelector();
			if (documentSubscriptions.contains(ds)) {
				// we don't need this resource
				i.remove();
			} else {
				for (String dsCollection : ds.getParentCollections()) {
					if (collectionSubscriptions.contains(dsCollection)) {
						// we don't need this resource
						i.remove();
					}
				}
			}
		}
		for (Iterator<DocumentSelector> i = documentSubscriptions.iterator(); i
				.hasNext();) {
			DocumentSelector ds = (DocumentSelector) i.next();
			for (String dsCollection : ds.getParentCollections()) {
				if (collectionSubscriptions.contains(dsCollection)) {
					// we don't need this resource
					i.remove();
				}
			}
		}
	}

	public DiffProcessing getDiffProcessing() {
		return diffProcessing;
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

	public Set<String> getCollectionSubscriptions() {
		return collectionSubscriptions;
	}

	public Set<DocumentSelector> getDocumentSubscriptions() {
		return documentSubscriptions;
	}

	public Set<NodeSubscription> getNodeSubscriptions() {
		return nodeSubscriptions;
	}

	public Set<DocumentSelector> getAllDocumentsToSubscribe() {
		if (documentSubscriptions.isEmpty() && nodeSubscriptions.isEmpty()) {
			return Collections.emptySet();
		} else {
			HashSet<DocumentSelector> result = new HashSet<DocumentSelector>(
					documentSubscriptions);
			for (NodeSubscription nodeSubscription : nodeSubscriptions) {
				result.add(nodeSubscription.getDocumentSelector());
			}
			return result;
		}
	}

	// serialization

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {

		subscriber = in.readUTF();
		key = (SubscriptionKey) in.readObject();
		diffProcessing = DiffProcessing.fromString(in.readUTF()); 
		
		// read collections
		if (in.readBoolean()) {
			collectionSubscriptions = new HashSet<String>();
			for (String c : (String[]) in.readObject()) {
				collectionSubscriptions.add(c);
			}
		} else {
			collectionSubscriptions = EMPTY_COLLECTION_SUBSCRIPTIONS;
		}

		// read docs
		if (in.readBoolean()) {
			documentSubscriptions = new HashSet<DocumentSelector>();
			for (DocumentSelector ds : (DocumentSelector[]) in.readObject()) {
				documentSubscriptions.add(ds);
			}
		} else {
			documentSubscriptions = EMPTY_DOCUMENT_SUBSCRIPTIONS;
		}

		// read collections
		if (in.readBoolean()) {
			nodeSubscriptions = new HashSet<NodeSubscription>();
			for (NodeSubscription ns : (NodeSubscription[]) in.readObject()) {
				nodeSubscriptions.add(ns);
			}
		} else {
			nodeSubscriptions = EMPTY_NODESUBSCRIPTIONS_SUBSCRIPTIONS;
		}

	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {

		out.writeUTF(subscriber);
		out.writeObject(key);
		out.writeUTF(diffProcessing.toString());

		// write collections as array
		int arraySize = collectionSubscriptions.size();
		if (arraySize == 0) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeObject(collectionSubscriptions
					.toArray(new String[arraySize]));
		}

		// write docs as array
		arraySize = documentSubscriptions.size();
		if (arraySize == 0) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeObject(documentSubscriptions
					.toArray(new DocumentSelector[arraySize]));
		}

		// write nodes as array
		arraySize = nodeSubscriptions.size();
		if (arraySize == 0) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeObject(nodeSubscriptions
					.toArray(new NodeSubscription[arraySize]));
		}

	}

}
