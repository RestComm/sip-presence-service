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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ListType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ResourceLists;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.ElementSelectorStep;
import org.openxdm.xcap.common.uri.ElementSelectorStepByAttr;

public class ReferencedResourceLists {
	
	private static final ElementSelectorStep rootStep = new ElementSelectorStep("resource-lists");
	
	private final DocumentSelector documentSelector;
	
	private final ConcurrentHashMap<String, ResourceList> lists = new ConcurrentHashMap<String, ResourceList>(); 
	
	private final RLSServicesCacheResourceAdaptor ra;
	
	private boolean resolved = false;
	
	public ReferencedResourceLists(DocumentSelector documentSelector, RLSServicesCacheResourceAdaptor ra) {
		this.documentSelector = documentSelector;
		this.ra = ra;
	}
	
	public void setResourceLists(ResourceLists resourceLists) {
		if (resourceLists == null) {
			// doc deleted
			for(ResourceList resourceList : lists.values()) {
				resourceList.setListType(null);
			}
		}
		else {
			// update lists
			HashSet<String> listsUpdated = new HashSet<String>();
			for(ListType listType : resourceLists.getList()) {
				ResourceList resourceList = lists.get(listType.getName());
				if (resourceList == null) {
					resourceList = addResourceList(listType.getName());
				}				
				resourceList.setListType(listType);	
				listsUpdated.add(listType.getName());
			}
			for(java.util.Map.Entry<String, ResourceList> mapEntry : lists.entrySet()) {
				if (!listsUpdated.contains(mapEntry.getKey())) {
					mapEntry.getValue().setListType(null);
				}
			}
		}
		resolved = true;
	}
	
	private ResourceList addResourceList(String name) {
		ResourceList resourceList = null;
		LinkedList<ElementSelectorStep> elementSelectorSteps = new LinkedList<ElementSelectorStep>();
		elementSelectorSteps.add(rootStep);
		elementSelectorSteps.add(new ElementSelectorStepByAttr("list", "name", name));
		ResourceList newResourceList = new ResourceList(new ListReferenceEndpointAddress(documentSelector, new ElementSelector(elementSelectorSteps)), null,ra);
		resourceList = lists.putIfAbsent(name, newResourceList);
		if (resourceList == null) {
			resourceList = newResourceList;
		}
		return resourceList;
	}

	public boolean isResolved() {
		return resolved;
	}
		
	public ListReferenceTo addFromReference(ListReferenceFrom from, ListReferenceEndpointAddress toAddress) {
		ElementSelectorStepByAttr elementSelectorStepByAttr = (ElementSelectorStepByAttr) toAddress.getElementSelector().getStep(1);
		ResourceList resourceList = lists.get(elementSelectorStepByAttr.getAttrValue());
		if (resourceList == null) {
			// even without content we add so when it is resolved or updated the reference is notified
			resourceList = addResourceList(elementSelectorStepByAttr.getAttrValue());
		}
		return resourceList.addFromReference(from,toAddress);
	}
	
	public void removeFromReference(ListReferenceEndpointAddress fromAddress,ListReferenceEndpointAddress toAddress) {
		ElementSelectorStepByAttr elementSelectorStepByAttr = (ElementSelectorStepByAttr) fromAddress.getElementSelector().getStep(1);
		ResourceList resourceList = lists.get(elementSelectorStepByAttr.getAttrValue());
		if (resourceList != null) {
			resourceList.removeFromReference(fromAddress,toAddress);
			if (resourceList.getStatus() == RLSService.Status.DOES_NOT_EXISTS && !resourceList.hasFromReferences()) {
				// not needed anymore
				lists.remove(elementSelectorStepByAttr.getAttrValue());
			}
		}
	}
	
	public boolean hasFromReferences() {
		for (ResourceList resourceList : lists.values()) {
			if (resourceList.hasFromReferences()) {
				return true;
			}
		}
		return false;
	}
}
