package org.mobicents.slee.sipevent.server.rlscache;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;

public abstract class AbstractListReferenceTo extends AbstractListReferenceEndpoint implements ListReferenceTo {

	protected RLSService.Status status = RLSService.Status.RESOLVING;
	
	private ConcurrentHashMap<ListReferenceEndpointAddress, ListReferenceFrom> fromReferences = new ConcurrentHashMap<ListReferenceEndpointAddress, ListReferenceFrom>();

	public AbstractListReferenceTo(ListReferenceEndpointAddress address) {
		super(address);
	}
	
	@Override
	public ListReferenceTo addFromReference(ListReferenceFrom from, ListReferenceEndpointAddress toAddress) {
		fromReferences.put(from.getAddress(), from);
		return this;
	}
	
	@Override
	public void removeFromReference(ListReferenceEndpointAddress fromAddress, ListReferenceEndpointAddress toAddress) {
		fromReferences.remove(fromAddress);
	}
	
	@Override
	public boolean hasFromReferences() {
		return fromReferences.isEmpty();
	}

	@Override
	public abstract Set<EntryType> getEntries();
	
	void updated() {
		for (ListReferenceFrom from : fromReferences.values()) {
			from.updated(this);
		}
	}
	
	@Override
	public RLSService.Status getStatus() {
		return status;
	}
}
