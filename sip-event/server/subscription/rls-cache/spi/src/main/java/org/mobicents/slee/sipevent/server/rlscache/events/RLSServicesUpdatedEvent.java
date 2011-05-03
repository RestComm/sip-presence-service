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

package org.mobicents.slee.sipevent.server.rlscache.events;

import java.util.Set;

import javax.slee.EventTypeID;

import org.mobicents.slee.sipevent.server.rlscache.RLSService;
import org.mobicents.slee.sipevent.server.rlscache.RLSService.Status;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;

public class RLSServicesUpdatedEvent extends AbstractEvent {

	/**
	 * IMPORTANT: must sync with the event descriptor data!!!!
	 */
	public static final EventTypeID EVENT_TYPE_ID = new EventTypeID("RLSServicesUpdatedEvent","org.mobicents","1.0");
	
	private final String uri;
	private final RLSService.Status newStatus;
	private final RLSService.Status oldStatus;
	private final Set<EntryType> oldEntries;
	private final Set<EntryType> removedEntries;
	private final Set<EntryType> newEntries;

	public RLSServicesUpdatedEvent(String uri,Status newStatus, Status oldStatus,
			Set<EntryType> oldEntries, Set<EntryType> removedEntries,
			Set<EntryType> newEntries) {
		super();
		this.uri = uri;
		this.newStatus = newStatus;
		this.oldStatus = oldStatus;
		this.oldEntries = oldEntries;
		this.removedEntries = removedEntries;
		this.newEntries = newEntries;
	}

	public Set<EntryType> getNewEntries() {
		return newEntries;
	}
	
	public RLSService.Status getNewStatus() {
		return newStatus;
	}
	
	public Set<EntryType> getOldEntries() {
		return oldEntries;
	}
	
	public RLSService.Status getOldStatus() {
		return oldStatus;
	}
	
	public Set<EntryType> getRemovedEntries() {
		return removedEntries;
	}
		
	public String getUri() {
		return uri;
	}
}

