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
 * Event that is fired on a subscription aci, so notifications for a
 * subscription are serialized.
 * 
 * @author martins
 * 
 */
public class WInfoNotifyEvent extends AbstractEvent {

	private final Watcher watcher;
	private final SubscriptionKey subscriptionKey;
	private final SubscriptionKey watcherSubscriptionKey;

	public WInfoNotifyEvent(SubscriptionKey subscriptionKey,
			SubscriptionKey watcherSubscriptionKey, Watcher watcher) {
		super();
		this.subscriptionKey = subscriptionKey;
		this.watcherSubscriptionKey = watcherSubscriptionKey;
		this.watcher = watcher;
	}

	public Watcher getWatcher() {
		return watcher;
	}

	public SubscriptionKey getWatcherSubscriptionKey() {
		return watcherSubscriptionKey;
	}

	public SubscriptionKey getSubscriptionKey() {
		return subscriptionKey;
	}

	private String toString = null;

	@Override
	public String toString() {
		if (toString == null) {
			toString = new StringBuilder("WINFO NOTIFY EVENT [ Subscription = ")
					.append(String.valueOf(subscriptionKey)).append(" ]")
					.toString();
		}
		return toString;
	}

	public static class Watcher {

		private String value;
		private String displayName;
		private String status;
		private String event;
		private Integer expiration;
		private String id;
		private Integer durationSubscribed;
		private String lang;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getEvent() {
			return event;
		}

		public void setEvent(String event) {
			this.event = event;
		}

		public Integer getExpiration() {
			return expiration;
		}

		public void setExpiration(Integer expiration) {
			this.expiration = expiration;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Integer getDurationSubscribed() {
			return durationSubscribed;
		}

		public void setDurationSubscribed(Integer durationSubscribed) {
			this.durationSubscribed = durationSubscribed;
		}

		public String getLang() {
			return lang;
		}

		public void setLang(String lang) {
			this.lang = lang;
		}

	}
}
