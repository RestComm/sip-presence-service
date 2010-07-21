package org.mobicents.slee.sipevent.server.rlscache;

import java.util.Set;

import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;

public interface ListReferenceTo extends ListReferenceEndpoint {
	
	public Set<EntryType> getEntries();
	
	public RLSService.Status getStatus();
	
	public ListReferenceTo addFromReference(ListReferenceFrom from, ListReferenceEndpointAddress toAddress);
	
	public void removeFromReference(ListReferenceEndpointAddress fromAddress, ListReferenceEndpointAddress toAddress);
	
	public boolean hasFromReferences();
		
}
