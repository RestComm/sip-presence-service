package org.mobicents.xdm.server.appusage;

public class AppUsageAddedEvent extends AbstractEvent {

	private final String auid;
	
	public AppUsageAddedEvent(String auid) {
		this.auid = auid;
	}
	
	public String getAuid() {
		return auid;
	}
}
