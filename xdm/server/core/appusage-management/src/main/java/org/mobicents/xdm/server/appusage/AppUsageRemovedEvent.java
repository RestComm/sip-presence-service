package org.mobicents.xdm.server.appusage;

public class AppUsageRemovedEvent extends AbstractEvent {

	private final String auid;
	
	public AppUsageRemovedEvent(String auid) {
		this.auid = auid;
	}
	
	public String getAuid() {
		return auid;
	}
}
