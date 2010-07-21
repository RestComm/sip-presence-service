package org.mobicents.slee.sipevent.server.rlscache.events;

import javax.slee.EventTypeID;

import org.openxdm.xcap.common.uri.DocumentSelector;

public class WatchResourceListsEvent extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * IMPORTANT: must sync with the event descriptor data!!!!
	 */
	public static final EventTypeID EVENT_TYPE_ID = new EventTypeID("WatchResourceListsEvent","org.mobicents","1.0");

	private final DocumentSelector documentSelector;
	
	public WatchResourceListsEvent(DocumentSelector documentSelector) {
		this.documentSelector = documentSelector;
	}
	
	public DocumentSelector getDocumentSelector() {
		return documentSelector;
	}
}
