package org.mobicents.slee.sipevent.server.rlscache;

public class RLSServiceActivityImpl implements RLSServiceActivity {

	public final static Class<?> TYPE = RLSServiceActivityImpl.class;
	
	private final String serviceURI;
	
	private RLSServicesCacheResourceAdaptor ra;
	
	public RLSServiceActivityImpl(String serviceURI,RLSServicesCacheResourceAdaptor ra) {
		if (serviceURI == null) {
			throw new NullPointerException("null serviceURI");
		}
		this.serviceURI = serviceURI;
		this.ra = ra;
	}
	
	@Override
	public String getServiceURI() {
		return serviceURI;
	}
	
	@Override
	public RLSService getRLSService() {
		return ra.getDataSource().getRLSService(serviceURI);
	}
		
	@Override
	public String toString() {
		return new StringBuilder("RLSServicesActivityImpl[uri=").append(serviceURI).append("]").toString();
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
		RLSServiceActivityImpl other = (RLSServiceActivityImpl) obj;
		return this.serviceURI.equals(other.serviceURI);
	}
	
	
}
