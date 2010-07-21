package org.mobicents.slee.sipevent.server.rlscache;

import java.util.Set;

import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.PackagesType;

public interface RLSService {

	public enum Status { RESOLVING, BAD_GATEWAY, DOES_NOT_EXISTS, OK } 
		
	public Set<EntryType> getEntries();
	
	public PackagesType getPackages();
	
	public Status getStatus();
	
	public String getURI();
	
}
