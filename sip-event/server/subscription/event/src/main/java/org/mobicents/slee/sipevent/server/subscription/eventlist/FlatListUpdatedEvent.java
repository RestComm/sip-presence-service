package org.mobicents.slee.sipevent.server.subscription.eventlist;

import java.util.Set;

import org.mobicents.sipevent.server.subscription.util.AbstractEvent;
import org.mobicents.slee.sipevent.server.subscription.eventlist.FlatList;

/**
 * 
 * @author martins
 * 
 */
public class FlatListUpdatedEvent  extends AbstractEvent {

	private final FlatList flatList;
	private final String[] oldEntries;
	private final String[] removedEntries;
	private final String[] newEntries;
	
	private static final String[] EMPTY_ARRAY = {};
	
	public FlatListUpdatedEvent(FlatList flatList,Set<String> newEntries, Set<String> oldEntries, Set<String> removedEntries) {
		this.flatList = flatList;
		this.newEntries = newEntries.toArray(EMPTY_ARRAY);
		this.oldEntries = oldEntries.toArray(EMPTY_ARRAY);
		this.removedEntries = removedEntries.toArray(EMPTY_ARRAY);
	}

	public FlatList getFlatList() {
		return flatList;
	}
	
	public String[] getNewEntries() {
		return newEntries;
	}
	
	public String[] getOldEntries() {
		return oldEntries;
	}
	
	public String[] getRemovedEntries() {
		return removedEntries;
	}
}
