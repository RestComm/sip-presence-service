package org.mobicents.xdm.server.appusage;

import java.util.UUID;

public abstract class AbstractEvent {

	private String id = UUID.randomUUID().toString();

	@Override
	public int hashCode() {
		return id.hashCode();		
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractEvent other = (AbstractEvent) obj;
		return this.id.equals(other.id);
	}

}
