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
import java.util.LinkedList;
import java.util.Set;

import org.mobicents.slee.sipevent.server.rlscache.events.RLSServicesAddedEvent;
import org.mobicents.slee.sipevent.server.rlscache.events.RLSServicesRemovedEvent;
import org.mobicents.slee.sipevent.server.rlscache.events.RLSServicesUpdatedEvent;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ListType;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.PackagesType;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.ServiceType;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.ElementSelectorStep;

public class RLSServiceImpl implements RLSService, ListReferenceFrom {
	
	private Set<EntryType> entries;
	
	private final ListReferenceEndpointAddress address;
	private final RLSServicesCacheResourceAdaptor ra;
	
	private ResourceList list;
	private ListReferenceTo reference;
	
	private boolean updating = false;
	
	private final String uri;
	
	private PackagesType packages;
	
	private Status status = Status.DOES_NOT_EXISTS;
	
	public RLSServiceImpl(String uri, ListReferenceEndpointAddress address, RLSServicesCacheResourceAdaptor ra) {
		this.uri = uri;
		this.address = address;
		this.ra = ra;
	}
	
	@Override
	public String getURI() {
		return uri;
	}
	
	public Set<EntryType> getEntries() {
		synchronized (this) {
			final Status status = getStatus();
			if (status != Status.OK) {
				throw new IllegalStateException("rls service is in state "+status);
			}
			if (entries == null) {
				if (reference != null) {
					entries = Collections.unmodifiableSet(reference.getEntries());
				}
				else {
					entries = Collections.unmodifiableSet(list.getEntries());
				}
			}
			return entries;
		}				
	} 
	
	@Override
	public PackagesType getPackages() {
		return packages;
	}
	
	public void setServiceType(ServiceType serviceType) {
				
		synchronized (this) {
			
			updating = true;
			
			final Status oldStatus = status;

			if (serviceType == null) {
				if (oldStatus != Status.DOES_NOT_EXISTS) {
					status = Status.DOES_NOT_EXISTS;
					Set<EntryType> oldEntries = entries;
					if (oldEntries == null) {
						oldEntries = Collections.emptySet();
					}
					RLSServicesRemovedEvent event = new RLSServicesRemovedEvent(uri, Status.DOES_NOT_EXISTS, oldStatus, oldEntries);
					entries = null;
					ra.fireRLSServicesRemovedEvent(uri,event);
				}
			} else {
				Set<EntryType> oldEntries = entries;
				processServiceType(serviceType);
				processUpdate(oldStatus,oldEntries);
			}
			updating = false;
			
		}
		

	}
	
	private void processUpdate(Status oldStatus, Set<EntryType> oldEntries) {
		
		final Status currentStatus = status;

		if (currentStatus == Status.BAD_GATEWAY) {
			if (oldEntries == null) {
				oldEntries = Collections.emptySet();
			}
			RLSServicesRemovedEvent event = new RLSServicesRemovedEvent(uri, currentStatus, oldStatus, oldEntries);
			entries = null;
			ra.fireRLSServicesRemovedEvent(uri,event);
		}
		else if (currentStatus == Status.OK) {				
			if (oldStatus != Status.OK) {
				RLSServicesAddedEvent event = new RLSServicesAddedEvent(uri);
				ra.fireRLSServicesAddedEvent(uri,event);
			}
			Set<EntryType> removedEntries = null;
			Set<EntryType> newEntries = null;
			if (oldEntries == null) {
				oldEntries = Collections.emptySet();
				removedEntries = Collections.emptySet();
				newEntries = getEntries();
			}
			else {
				removedEntries = new HashSet<EntryType>(oldEntries);
				oldEntries = new HashSet<EntryType>();
				newEntries = new HashSet<EntryType>();
				for (EntryType entryType : this.getEntries()) {
					if (!removedEntries.remove(entryType)) {
						newEntries.add(entryType);
					}
					else {
						oldEntries.add(entryType);
					}
				}
			}
			if (removedEntries.isEmpty() && newEntries.isEmpty()) {
				// nothing to update after all
				return;
			}
			RLSServicesUpdatedEvent event = new RLSServicesUpdatedEvent(uri, currentStatus, oldStatus, oldEntries, removedEntries, newEntries);
			ra.fireRLSServicesUpdatedEvent(uri,event);
		}
	}

	private void processServiceType(ServiceType serviceType) {
		
		packages = serviceType.getPackages();
		
		/*
		 * 
		 * If the <service> element had a <list> element, it is extracted. If
		 * the <service> element had a <resource-list> element, its URI content
		 * is dereferenced. The result should be a <list> element. If it is not,
		 * the request SHOULD be rejected with a 502 (Bad Gateway). Otherwise,
		 * that <list> element is extracted.
		 * 
		 */
		ListType listType = serviceType.getList();
		if (listType != null) {
			reference = null;
			LinkedList<ElementSelectorStep> steps = copyListAddressElementSelectorSteps();
			steps.add(new ElementSelectorStep("list"));
			ElementSelector elementSelector = new ElementSelector(steps);
			ListReferenceEndpointAddress listAddress = new ListReferenceEndpointAddress(getAddress().getDocumentSelector(), elementSelector);
			list = new ResourceList(listAddress,this,ra);
			list.setListType(listType);
			status = list.getStatus();
		}
		else {
			list = null;
			String resourceListURI = serviceType.getResourceList().trim();
			ListReferenceEndpointAddress listAddress = ra.getAddressParser().getAddress(resourceListURI, true);
			if (listAddress != null) {
				reference = ra.addReference(this,listAddress);
			}
			if (reference == null) {
				status = Status.BAD_GATEWAY;
			}
			else {
				status = reference.getStatus();
			}
		}
	}
	
	private LinkedList<ElementSelectorStep> copyListAddressElementSelectorSteps() {
		LinkedList<ElementSelectorStep> elementSelectorSteps = new LinkedList<ElementSelectorStep>();
		ListReferenceEndpointAddress listAddress = getAddress();
		ElementSelector listElementSelector = listAddress.getElementSelector();
		for (int j=0;j<listElementSelector.getStepsSize();j++) {
			elementSelectorSteps.add(listElementSelector.getStep(j));
		}
		return elementSelectorSteps;
	}

	public Status getStatus() {
		return status;
	}	
	
	@Override
	public int hashCode() { 
		return uri.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			return ((RLSServiceImpl)obj).uri.equals(this.uri);
		}
		else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return new StringBuilder("RLSService(uri=").append(uri).append(") = ").append(getEntries().toString()).toString();		
	}

	@Override
	public void updated(ListReferenceTo reference) {
		
		synchronized (this) {
						
			if (updating) {
				return;
			}

			RLSService.Status oldStatus = status;
			Set<EntryType> oldEntries = entries;
			status = reference.getStatus();
			entries = null;
			processUpdate(oldStatus, oldEntries);
			
		}				
				
	}

	@Override
	public ListReferenceEndpointAddress getAddress() {
		return address;
	}
	
}
