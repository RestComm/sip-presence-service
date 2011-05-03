/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
