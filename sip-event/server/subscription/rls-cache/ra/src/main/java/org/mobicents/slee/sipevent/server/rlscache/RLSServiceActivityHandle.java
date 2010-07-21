package org.mobicents.slee.sipevent.server.rlscache;

import javax.slee.resource.ActivityHandle;

public class RLSServiceActivityHandle implements ActivityHandle {

	public final static Class<?> TYPE = RLSServiceActivityHandle.class;
	
	private final String serviceURI;
	
	public RLSServiceActivityHandle(String serviceURI) {
		if (serviceURI == null) {
			throw new NullPointerException("null serviceURI");
		}
		this.serviceURI = serviceURI;
	}
	
	public String getServiceURI() {
		return serviceURI;
	}
	

	@Override
	public int hashCode() {
		return serviceURI.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RLSServiceActivityHandle other = (RLSServiceActivityHandle) obj;
		return this.serviceURI.equals(other.serviceURI);
	}
		
	@Override
	public String toString() {
		return new StringBuilder("RLSServicesActivityHandle[uri=").append(serviceURI).append("]").toString();
	}
}
