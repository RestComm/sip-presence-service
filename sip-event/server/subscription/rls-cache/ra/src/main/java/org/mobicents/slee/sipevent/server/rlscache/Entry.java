package org.mobicents.slee.sipevent.server.rlscache;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;

public class Entry extends AbstractListReferenceTo {
	
	private EntryType entryType;
		
	public Entry(ListReferenceEndpointAddress address) {
		super(address);
	}
	
	public void setEntryType(EntryType entryType) {
		EntryType oldEntryType = this.entryType;
		this.entryType = entryType;
		if (status == RLSService.Status.RESOLVING) {
			if (entryType == null) {
				status = RLSService.Status.DOES_NOT_EXISTS;				
			}
			else {
				status = RLSService.Status.OK;
			}			
			updated();
		}
		else {
			if (status == RLSService.Status.OK) {
				if (entryType == null) {
					status = RLSService.Status.DOES_NOT_EXISTS;
					updated();
				}
				else {
					if (oldEntryType.getDisplayName().getValue() == null) {
						if (this.entryType.getDisplayName().getValue() != null) {
							updated();
						}
					}
					else {
						if (this.entryType.getDisplayName().getValue() != null) {
							if(!oldEntryType.getDisplayName().getValue().equals(this.entryType.getDisplayName().getValue())) {
								updated();
							}
						}
					}				
				}
			}
			else {
				if (entryType != null) {
					status = RLSService.Status.OK;
					updated();
				}
			}
		}		
	}
	
	public EntryType getEntryType() {
		return entryType;
	}
	
	@Override
	public Set<EntryType> getEntries() {
		if (entryType == null) {
			return Collections.emptySet();
		}
		else {
			Set<EntryType> entryTypes = new HashSet<EntryType>();
			entryTypes.add(entryType);
			return entryTypes;
		}		
	}

}
