package org.mobicents.slee.sipevent.server.rlscache;


public class AbstractListReferenceEndpoint implements ListReferenceEndpoint {

	private final ListReferenceEndpointAddress address;
	
	public AbstractListReferenceEndpoint(ListReferenceEndpointAddress address) {
		this.address = address;
	}
	
	@Override
	public ListReferenceEndpointAddress getAddress() {
		return address;
	}

	@Override
	public int hashCode() {
		return address.hashCode();		
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractListReferenceEndpoint other = (AbstractListReferenceEndpoint) obj;
		if (!address.equals(other.address))
			return false;
		return true;
	}

	
}
