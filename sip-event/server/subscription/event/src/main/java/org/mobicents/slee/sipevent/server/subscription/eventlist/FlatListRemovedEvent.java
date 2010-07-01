package org.mobicents.slee.sipevent.server.subscription.eventlist;

import org.mobicents.sipevent.server.subscription.util.AbstractEvent;
import org.mobicents.slee.sipevent.server.subscription.eventlist.FlatList;

/**
 * 
 * @author martins
 * 
 */
public class FlatListRemovedEvent extends AbstractEvent {

	private final FlatList flatList;
	
	public FlatListRemovedEvent(FlatList flatList) {
		this.flatList = flatList;
	}

	public FlatList getFlatList() {
		return flatList;
	}
}
