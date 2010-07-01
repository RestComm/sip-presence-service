/**
 * 
 */
package org.mobicents.sipevent.server.subscription.util;

import java.rmi.dgc.VMID;

/**
 * Abstract base class for a custom slee event.
 * 
 * @author martins
 *
 */
public class AbstractEvent {

	private final String eventId;
	
	/**
	 * 
	 */
	public AbstractEvent() {
		eventId = new VMID().toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			return ((AbstractEvent) obj).eventId.equals(this.eventId);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return eventId.hashCode();
	}
}
