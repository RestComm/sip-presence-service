package org.mobicents.slee.sipevent.server.rlscache.events;

import javax.slee.EventTypeID;

public class RLSServicesAddedEvent extends AbstractEvent {

	/**
	 * IMPORTANT: must sync with the event descriptor data!!!!
	 */
	public static final EventTypeID EVENT_TYPE_ID = new EventTypeID("RLSServicesAddedEvent","org.mobicents","1.0");
	
	private final String uri;
	
	public RLSServicesAddedEvent(String uri) {
		super();
		this.uri = uri;		
	}
	
	public String getUri() {
		return uri;
	}
}

