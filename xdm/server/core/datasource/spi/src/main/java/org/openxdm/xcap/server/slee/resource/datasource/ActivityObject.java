package org.openxdm.xcap.server.slee.resource.datasource;

public class ActivityObject {

	protected final String id;
	
	protected ActivityObject(String id) {
		this.id = id;
	}
	
	public int hashCode() {
		return id.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			ActivityObject other = (ActivityObject) obj;
			return this.id
					.equals(other.id);
		}
		return false;
	}
		
}
