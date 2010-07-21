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

