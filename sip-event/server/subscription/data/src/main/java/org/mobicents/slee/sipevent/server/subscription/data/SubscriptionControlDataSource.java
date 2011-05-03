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

package org.mobicents.slee.sipevent.server.subscription.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheStatus;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.notifications.annotation.CacheListener;
import org.jboss.cache.notifications.annotation.CacheStarted;
import org.jboss.cache.notifications.event.CacheStartedEvent;
import org.mobicents.cache.MobicentsCache;

@CacheListener(sync=false)
public class SubscriptionControlDataSource {

	/*
	 data is structured as:
	 
	  root
	  +--- msps-sub
	  +------ dialogId
	  +--------- eventPackage
	  +------------ eventId (Boolean.TRUE = subscription)
	  +--- msps-sub-timers
	  +------ timerID (Boolean.TRUE = subscriptionKey)
	  +--- msps-sub-notifiers
	  +------ subscriptionKey
	  
	  note: subscriptionKey is (dialogId,eventPackage,eventId), i.e., 
	 */
	private final Cache jbcache;
	
	private Node subRoot;  
	private Node timersRoot;
	private Node notifiersRoot;	
	
	private static final String NO_EVENT_ID = "";
	
	public SubscriptionControlDataSource(MobicentsCache cache) {
		jbcache = cache.getJBossCache();
		if (jbcache.getCacheStatus() != CacheStatus.STARTED) {
			jbcache.addCacheListener(this);
		}
		else {
			initNodes();
		}
	}
	
	@CacheStarted
	public void onCacheStartedEvent(CacheStartedEvent event) {
		initNodes();		
	}
	
	private void initNodes() {
		Node root = jbcache.getRoot();
		subRoot = jbcache.getRoot().addChild(Fqn.fromElements("msps-sub"));			
		notifiersRoot = jbcache.getRoot().addChild(Fqn.fromElements("msps-sub-notifiers"));
		timersRoot = jbcache.getRoot().addChild(Fqn.fromElements("msps-sub-timers"));
	}
	
	// ----
	
	void add(Subscription s) {
		
		final SubscriptionKey sk = s.getKey();
		// create dialog id node if does not exists
		Node dialogIdNode = subRoot.getChild(sk.getDialogId());
		if (dialogIdNode == null) {
			dialogIdNode = subRoot.addChild(Fqn.fromElements(sk.getDialogId()));
		}
		// create event package node if does not exists
		Node eventPackageNode = dialogIdNode.getChild(sk.getEventPackage());
		if (eventPackageNode == null) {
			eventPackageNode = dialogIdNode.addChild(Fqn.fromElements(sk.getEventPackage()));
		}
		// create event id node and store subscription
		final String eventId = sk.getEventId() == null ? NO_EVENT_ID : sk.getEventId();
		Node eventIdNode = eventPackageNode.getChild(eventId);
		if (eventIdNode == null) {
			eventIdNode = eventPackageNode.addChild(Fqn.fromElements(eventId));
		}
		else {
			throw new IllegalStateException("subscription "+sk+" already exists");
		}
		eventIdNode.put(Boolean.TRUE, s);
		// add notfier -> subscription reference 
		notifiersRoot.addChild(Fqn.fromElements(s.getNotifier().getUri(),sk));
		s.setDataSource(this);
	}

	private Node getSubscriptionNode(SubscriptionKey sk) {
				
		// get dialog id node
		Node dialogIdNode = subRoot.getChild(sk.getDialogId());
		if (dialogIdNode == null) {
			return null;
		}
		// get event package node
		Node eventPackageNode = dialogIdNode.getChild(sk.getEventPackage());
		if (eventPackageNode == null) {
			return null;
		}
		// get event id node
		final String eventId = sk.getEventId() == null ? NO_EVENT_ID : sk.getEventId();
		return eventPackageNode.getChild(eventId);		
	}
	
	public Subscription get(SubscriptionKey sk) {
		
		final Node subscriptionNode = getSubscriptionNode(sk);
		if (subscriptionNode == null) {
			return null;
		}
		Subscription s = (Subscription) subscriptionNode.get(Boolean.TRUE);
		s.setDataSource(this);
		return s;
	}
	
	public Subscription getFromTimerID(Serializable timerID) {
		
		Node timerIDNode = timersRoot.getChild(timerID);
		if (timerIDNode == null) {
			return null;
		}
		return get((SubscriptionKey)timerIDNode.get(Boolean.TRUE));
	}
	
	private static final List<Subscription> NO_RESULT = Collections.emptyList();
	
	public List<Subscription> getSubscriptionsByNotifierAndEventPackage(String notifier,String eventPackage) {
		
		List<Subscription> result = null;
		// get notifier node
		Node notifierNode = notifiersRoot.getChild(notifier);
		if (notifierNode == null) {
			result = NO_RESULT;
		}
		else {
			result = new ArrayList<Subscription>();
			Subscription s = null;
			for (SubscriptionKey sk : (Set<SubscriptionKey>) notifierNode.getChildrenNames()) {
				if (eventPackage != null && !eventPackage.equals(sk.getEventPackage())) {
					continue;
				}
				s = get(sk);
				if (s != null) {
					s.setDataSource(this);
					result.add(s);
				}				
			}
		}
		return result;
	}
	
	public List<Subscription> getSubscriptionsByNotifier(String notifier) {
		
		return getSubscriptionsByNotifierAndEventPackage(notifier, null);
	}

	public List<Subscription> getSubscriptionsByDialog(String dialogId) {
		
		List<Subscription> result = null;
		// get dialog id node
		Node dialogIdNode = subRoot.getChild(dialogId);
		if (dialogIdNode == null) {
			result = NO_RESULT;
		}
		else {
			result = new ArrayList<Subscription>();
			Subscription s = null;
			for (Node eventPackageNode : (Set<Node>) dialogIdNode.getChildren()) {
				for (Node eventIdNode : (Set<Node>) eventPackageNode.getChildren()) {
					s = (Subscription) eventIdNode.get(Boolean.TRUE);
					s.setDataSource(this);					
				}
			}			
		}
		return result;
			
	}
	
	void update(Subscription s) {
		
		final Node subscriptionNode = getSubscriptionNode(s.getKey());
		if (subscriptionNode == null) {
			throw new IllegalStateException("original subscription "+s.getKey()+" not found");
		}
		subscriptionNode.put(Boolean.TRUE, s);
	}
	
	void addTimerReference(SubscriptionKey sk, Serializable timerId) {
		
		timersRoot.addChild(Fqn.fromElements(timerId)).put(Boolean.TRUE, sk);		
	}
	
	void removeTimerReference(Serializable timerId) {
		
		timersRoot.removeChild(timerId);				
	}
	
	void remove(Subscription s) {
		
		final SubscriptionKey sk = s.getKey();
		// get dialog id node
		Node dialogIdNode = subRoot.getChild(sk.getDialogId());
		if (dialogIdNode == null) {
			return;
		}
		// get event package node
		Node eventPackageNode = dialogIdNode.getChild(sk.getEventPackage());
		if (eventPackageNode == null) {
			return;
		}
		// remove event id node
		final String eventId = sk.getEventId() == null ? NO_EVENT_ID : sk.getEventId();
		eventPackageNode.removeChild(eventId);
		// if event package node has no childs remove it
		if (eventPackageNode.isLeaf()) {
			dialogIdNode.removeChild(sk.getEventPackage());
		}
		// if dialog id node has no childs remove it
		if (dialogIdNode.isLeaf()) {
			subRoot.removeChild(sk.getDialogId());
		}
		// delete timer id reference
		removeTimerReference(s.getTimerID());
		// delete dialog reference
		Node notifierNode = notifiersRoot.getChild(s.getNotifier().getUri());
		if (notifierNode == null) {
			return;
		}
		notifierNode.removeChild(sk);
		if (notifierNode.isLeaf()) {
			notifiersRoot.removeChild(s.getNotifier().getUri());
		}
	}
	
}
