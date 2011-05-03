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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBElement;

import org.mobicents.slee.sipevent.server.rlscache.RLSService.Status;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryRefType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ExternalType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ListType;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.ElementSelectorStep;
import org.openxdm.xcap.common.uri.ElementSelectorStepByAttr;

public class ResourceList extends AbstractListReferenceTo implements ListReferenceFrom {
		
	private final ListReferenceFrom parent;
	
	private final ConcurrentHashMap<String,Entry> localEntries = new ConcurrentHashMap<String,Entry>();
	
	private final ConcurrentHashMap<String,ResourceList> lists = new ConcurrentHashMap<String,ResourceList>(1);
	
	private final ConcurrentHashMap<ListReferenceEndpointAddress, ListReferenceTo> toReferences = new ConcurrentHashMap<ListReferenceEndpointAddress, ListReferenceTo>(1);
	
	private final RLSServicesCacheResourceAdaptor ra;
	
	private ListType listType;
	
	private boolean updating = false;
	
	public ResourceList(ListReferenceEndpointAddress address, ListReferenceFrom parent, RLSServicesCacheResourceAdaptor ra) {
		super(address);
		this.parent = parent;
		this.ra = ra;
	}

	public void setListType(ListType listType) {
				
		synchronized (this) {		
			
			updating = true;

			this.listType = listType;
			
			if (listType == null) {
				if (status != RLSService.Status.DOES_NOT_EXISTS) {
					status = RLSService.Status.DOES_NOT_EXISTS;
					for (Entry entry : localEntries.values()) {
						entry.setEntryType(null);
					}
					for (ResourceList list : lists.values()) {
						list.setListType(null);
					}
					updated();
				}
			} else {
				RLSService.Status oldStatus = status;
				processListType(listType);
				if (status == RLSService.Status.OK || oldStatus != status) {				
					updated();
				}
			}
			
			updating = false;
		}		
	}
		
	private void processListType(ListType listType) {
		
		status = RLSService.Status.OK;
		
		HashSet<String> entriesUpdated = new HashSet<String>();
		HashSet<String> listsUpdated = new HashSet<String>();
		HashSet<ListReferenceEndpointAddress> referencesToAdd = new HashSet<ListReferenceEndpointAddress>();
		
		/*
		 * At this point, the RLS has a <list> element in its possession. The
		 * next step is to obtain a flat list of URIs from this element. To do
		 * that, it traverses the tree of elements rooted in the <list> element.
		 * Before traversal begins, the RLS initializes two lists: the "flat
		 * list", which will contain the flat list of the URI after traversal,
		 * and the "traversed list", which contains a list of HTTP URIs in
		 * <external> elements that have already been visited. Both lists are
		 * initially empty. Next, tree traversal begins. A server can use any
		 * tree-traversal ordering it likes, such as depth-first search or
		 * breadth-first search. The processing at each element in the tree
		 * depends on the name of the element:
		 */
		for (Iterator<Object> i=listType.getListOrExternalOrEntry().iterator(); i.hasNext();) {
			JAXBElement<?> element = (JAXBElement<?>) i.next();
			
			if (element.getValue() instanceof EntryType) {
				/* o If the element is <entry>, the URI in the "uri" attribute of the
				 * element is added to the flat list if it is not already present (based
				 * on case-sensitive string equality) in that list, and the URI scheme
				 * represents one that can be used to service subscriptions, such as SIP
				 * [4] and pres [15].
				 */
				EntryType entryType = (EntryType) element.getValue();
				entriesUpdated.add(entryType.getUri());
				Entry entry = localEntries.get(entryType.getUri());
				if (entry == null) {
					entry = addEntry(entryType.getUri());
				}
				entry.setEntryType(entryType);
			}
			
			else if (element.getValue() instanceof EntryRefType) {
				/* o If the element is an <entry-ref>, the relative path reference
				 * making up the value of the "ref" attribute is resolved into an
				 * absolute URI. This is done using the procedures defined in Section
				 * 5.2 of RFC 3986 [7], using the XCAP root of the RLS services document
				 * as the base URI. This absolute URI is resolved. If the result is not
				 * a 200 OK containing a <entry> element, the SUBSCRIBE request SHOULD
				 * be rejected with a 502 (Bad Gateway). Otherwise, the <entry> element
				 * returned is processed as described in the previous step.
				 */
				EntryRefType entryRefType = (EntryRefType) element.getValue();
				ListReferenceEndpointAddress entryRefAddress = ra.getAddressParser().getAddress(entryRefType.getRef(),false);
				if (entryRefAddress == null) {
					status = RLSService.Status.BAD_GATEWAY;
					continue;
				}
				if (!entryRefAddress.getElementSelector().getLastStep().getNameWithoutPrefix().equals("entry")) {
					// not pointing to an entry
					status = RLSService.Status.BAD_GATEWAY;
					continue;
				}
				referencesToAdd.add(entryRefAddress);
			}
			
			else if (element.getValue() instanceof ExternalType) {
				
				 /* o If the element is an <external> element, the absolute URI making up
				 * the value of the "anchor" attribute of the element is examined. If
				 * the URI is on the traversed list, the server MUST cease traversing
				 * the tree, and SHOULD reject the SUBSCRIBE request with a 502 (Bad
				 * Gateway). If the URI is not on the traversed list, the server adds
				 * the URI to the traversed list, and dereferences the URI. If the
				 * result is not a 200 OK containing a <list> element, the SUBSCRIBE
				 * request SHOULD be rejected with a 502 (Bad Gateway). Otherwise, the
				 * RLS replaces the <external> element in its local copy of the tree
				 * with the <list> element that was returned, and tree traversal
				 * continues.
				 * 
				 * 
				 * Because the <external> element is used to dynamically construct the
				 * tree, there is a possibility of recursive evaluation of references.
				 * The traversed list is used to prevent this from happening.
				 * 
				 * Once the tree has been traversed, the RLS can create virtual
				 * subscriptions to each URI in the flat list, as defined in [14]. In
				 * the processing steps outlined above, when an <entry-ref> or
				 * <external> element contains a reference that cannot be resolved,
				 * failing the request is at SHOULD strength. In some cases, an RLS may
				 * provide better service by creating virtual subscriptions to the URIs
				 * in the flat list that could be obtained, omitting those that could
				 * not. Only in those cases should the SHOULD recommendation be ignored.
				 * 
				 * 
				 */
				
				//FIXME add support to really external uris
				ExternalType externalType = (ExternalType)element.getValue();
				ListReferenceEndpointAddress externalTypeAddress = ra.getAddressParser().getAddress(externalType.getAnchor(),true);
				if (externalTypeAddress == null) {
					status = RLSService.Status.BAD_GATEWAY;
					continue;					
				}
				if (!externalTypeAddress.getElementSelector().getLastStep().getNameWithoutPrefix().equals("list")) {
					// not pointing to a list
					status = RLSService.Status.BAD_GATEWAY;
					continue;
				}
				referencesToAdd.add(externalTypeAddress);
			}
			else if (element.getValue() instanceof ListType) {
				ListType innerListType = (ListType) element.getValue();
				listsUpdated.add(innerListType.getName());
				ResourceList resourceList = lists.get(innerListType.getName());
				if (resourceList == null) {
					addResourceList(innerListType.getName());
				}	
				resourceList.setListType(innerListType);				
			}
		}
		// remove all entries that exist but are not in the list type
		Entry entry = null;
		String entryName = null;
		for (Iterator<String> it = localEntries.keySet().iterator();it.hasNext();) {
			entryName = it.next();
			if (!entriesUpdated.contains(entryName)) {
				entry = localEntries.get(entryName);
				if (entry.hasFromReferences()) {
					entry.setEntryType(null);
				}
				else {
					// no refs and does not really exist, remove
					it.remove();
				}
			}
		}
		// remove all inner lists that exist but are not in the list type
		ResourceList innerList = null;
		String innerListName = null;
		for (Iterator<String> it = lists.keySet().iterator();it.hasNext();) {
			innerListName = it.next();
			if (!listsUpdated.contains(innerListName)) {
				innerList = lists.get(innerListName);
				if (innerList.hasFromReferences()) {
					innerList.setListType(null);					
				}
				else {
					// no refs and does not really exist, remove
					it.remove();
				}				
			}
		}
		// remove existent references not found on this update and remove existent ones from the set to add
		ListReferenceEndpointAddress referenceAddress = null;
		for (Iterator<ListReferenceEndpointAddress> it = toReferences.keySet().iterator();it.hasNext();) {
			referenceAddress = it.next();
			if (!referencesToAdd.remove(referenceAddress)) {
				it.remove();
				ra.removeReference(getAddress(),referenceAddress);
			}
		}

		if (!referencesToAdd.isEmpty()) {
			// create references
			ListReferenceTo referenceTo = null;
			for (ListReferenceEndpointAddress address : referencesToAdd) {
				referenceTo = ra.addReference(this,address);
				if (referenceTo == null) {
					status = Status.BAD_GATEWAY;
				}
				else {
					toReferences.put(address, referenceTo);
				}
			}
		}
	}

	private Entry addEntry(String uri) {
		LinkedList<ElementSelectorStep> elementSelectorSteps = cloneListAddressElementSelectorSteps();
		elementSelectorSteps.add(new ElementSelectorStepByAttr("entry", "uri", uri));
		Entry newEntry = new Entry(new ListReferenceEndpointAddress(getAddress().getDocumentSelector(), new ElementSelector(elementSelectorSteps)));
		Entry entry = localEntries.putIfAbsent(uri, newEntry);
		if (entry == null) {
			entry = newEntry;
		}
		return entry;
	}

	private ResourceList addResourceList(String name) {
		LinkedList<ElementSelectorStep> elementSelectorSteps = cloneListAddressElementSelectorSteps();
		elementSelectorSteps.add(new ElementSelectorStepByAttr("list", "name", name));
		ResourceList newResourceList = new ResourceList(new ListReferenceEndpointAddress(getAddress().getDocumentSelector(), new ElementSelector(elementSelectorSteps)),this,ra);
		ResourceList resourceList = lists.putIfAbsent(name, newResourceList);
		if (resourceList == null) {
			resourceList = newResourceList;
		}
		return resourceList;
	}
	
	private LinkedList<ElementSelectorStep> cloneListAddressElementSelectorSteps() {
		LinkedList<ElementSelectorStep> elementSelectorSteps = new LinkedList<ElementSelectorStep>();
		ListReferenceEndpointAddress listAddress = getAddress();
		ElementSelector listElementSelector = listAddress.getElementSelector();
		for (int j=0;j<listElementSelector.getStepsSize();j++) {
			elementSelectorSteps.add(listElementSelector.getStep(j));
		}
		return elementSelectorSteps;
	}
	
	public ConcurrentHashMap<ListReferenceEndpointAddress, ListReferenceTo> getToReferences() {
		return toReferences;
	}
	
	@Override
	public RLSService.Status getStatus() {
		if (status == RLSService.Status.BAD_GATEWAY || status == RLSService.Status.DOES_NOT_EXISTS) {
			return status;
		}
		else {
			boolean resolving = false;
			RLSService.Status s = null;
			for (ResourceList list : lists.values()) {
				s = list.getStatus();
				if (s == RLSService.Status.BAD_GATEWAY) {
					return RLSService.Status.BAD_GATEWAY;
				}
				else if (s == RLSService.Status.RESOLVING) {
					resolving = true;
				}
			}
			for (ListReferenceTo reference : toReferences.values()) {
				s = reference.getStatus();
				if (s == RLSService.Status.BAD_GATEWAY) {
					return RLSService.Status.BAD_GATEWAY;
				}
				else if (s == RLSService.Status.RESOLVING) {
					resolving = true;
				}
			}
			if (resolving) {
				return RLSService.Status.RESOLVING;				
			}
			
			return status;			
		}
	}
	
	@Override
	public void updated(ListReferenceTo referenceUpdated) {
		
		synchronized (this) {
			
			if (updating) {
				// ignore
				return;
			}
			
			RLSService.Status referenceStatus = referenceUpdated.getStatus();
		
			if (status == RLSService.Status.BAD_GATEWAY) {
				if (referenceStatus != RLSService.Status.BAD_GATEWAY) {
					// the bad gateway status may have come from a loop in this reference or entry ref not found, rebuild list
					setListType(listType);
				}
			}
			else {
				if (referenceStatus == RLSService.Status.BAD_GATEWAY) {
					status = RLSService.Status.BAD_GATEWAY;
					updated();			
				}
				else {
					if (referenceUpdated.getClass() == this.getClass()) {
						// resource list
						if (referenceStatus == RLSService.Status.OK) {
							if (isLoop((ResourceList)referenceUpdated, new HashSet<ListReferenceEndpointAddress>())) {
								status = RLSService.Status.BAD_GATEWAY;
							}
						}
					}
					else {
						// entry ref
						if (referenceStatus == RLSService.Status.DOES_NOT_EXISTS) {
							status = RLSService.Status.BAD_GATEWAY;
						}
					}
					updated();
				}
			}
		}
	}
	
	private Set<EntryType> entriesCached;
	
	@Override
	void updated() {
		entriesCached = null;
		if (parent != null) {
			parent.updated(this);
		}
		super.updated();
	}
	
	@Override
	public Set<EntryType> getEntries()  {
		
		if (status == RLSService.Status.BAD_GATEWAY) {
			throw new IllegalStateException("list is in bad gateway state");
		}
		
		if (entriesCached != null) {
			return entriesCached;
		}
		
		synchronized (this) {
			Set<EntryType> result = new HashSet<EntryType>();

			Set<String> entryURIs = new HashSet<String>();
			for (Entry entry : localEntries.values()) {
				if (entryURIs.add(entry.getEntryType().getUri())) {
					result.add(entry.getEntryType());
				}
			}
			for (ResourceList list : lists.values()) {
				for (EntryType entryType : list.getEntries()) {
					if (entryURIs.add(entryType.getUri())) {
						result.add(entryType);
					}
				}
			}
			for (ListReferenceTo reference : toReferences.values()) {
				for (EntryType entryType : reference.getEntries()) {
					if (entryURIs.add(entryType.getUri())) {
						result.add(entryType);
					}
				}
			}
			
			entriesCached = result;
			return entriesCached;
		}
	}
	
	@Override
	public ListReferenceTo addFromReference(ListReferenceFrom from, ListReferenceEndpointAddress toAddress) {
		int thisAddressStepsSize = getAddress().getElementSelector().getStepsSize();
		if (toAddress.getElementSelector().getStepsSize() > thisAddressStepsSize) {
			// reference for a sub element
			ElementSelectorStepByAttr elementSelectorStep = (ElementSelectorStepByAttr) toAddress.getElementSelector().getStep(thisAddressStepsSize);
			if (elementSelectorStep.getNameWithoutPrefix().equals("list")) {
				// resource list
				ResourceList resourceList = lists.get(elementSelectorStep.getAttrValue());
				if (resourceList == null) {
					addResourceList(elementSelectorStep.getAttrValue());
				}	
				return resourceList.addFromReference(from, toAddress);
			}
			else {
				// entry ref
				Entry entry = localEntries.get(elementSelectorStep.getAttrValue());
				if (entry == null) {
					entry = addEntry(elementSelectorStep.getAttrValue());
				}
				return entry.addFromReference(from,toAddress);
			}
		}
		else {
			return super.addFromReference(from,toAddress);
		}
	}

	@Override
	public void removeFromReference(ListReferenceEndpointAddress fromAddress, ListReferenceEndpointAddress toAddress) {
		int thisAddressStepsSize = getAddress().getElementSelector().getStepsSize();
		if (toAddress.getElementSelector().getStepsSize() > thisAddressStepsSize) {
			// reference for a sub element
			ElementSelectorStepByAttr elementSelectorStep = (ElementSelectorStepByAttr) toAddress.getElementSelector().getStep(thisAddressStepsSize);
			if (elementSelectorStep.getNameWithoutPrefix().equals("list")) {
				// resource list
				ResourceList resourceList = lists.get(elementSelectorStep.getAttrValue());
				if (resourceList != null) {
					resourceList.removeFromReference(fromAddress, toAddress);
					if (resourceList.getStatus() == RLSService.Status.DOES_NOT_EXISTS && !resourceList.hasFromReferences()) {
						lists.remove(elementSelectorStep.getAttrValue());
					}
				}					
			}
			else {
				// entry ref
				Entry entry = localEntries.get(elementSelectorStep.getAttrValue());
				if (entry != null) {
					entry.removeFromReference(fromAddress, toAddress);
					if (entry.getStatus() == RLSService.Status.DOES_NOT_EXISTS && !entry.hasFromReferences()) {
						localEntries.remove(elementSelectorStep.getAttrValue());
					}
				}				
			}
		}
		else {
			super.removeFromReference(fromAddress,toAddress);
		}
	}
	
	@Override
	public boolean hasFromReferences() {
		if (super.hasFromReferences()) {
			return true;
		}
		for (ResourceList list : lists.values()) {
			if (list.hasFromReferences()) {
				return true;
			}
		}
		for (Entry entry : localEntries.values()) {
			if (entry.hasFromReferences()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isLoop(ResourceList list, Set<ListReferenceEndpointAddress> references) {
		
		for (ResourceList innerList : list.lists.values()) {
			if (references.add(innerList.getAddress())) {
				if (isLoop(innerList, references)); {
					return true;
				}
			}
		}
		for (ListReferenceTo reference : list.toReferences.values()) {
			if (references.add(reference.getAddress())) {
				// check address is not the same as this one or an ancestor
				if (this.getAddress().getDocumentSelector().equals(reference.getAddress().getDocumentSelector()) &&
						this.getAddress().getElementSelector().toString().startsWith(reference.getAddress().getElementSelector().toString())) {
					// loop found
					return true;
				}
				// no loop, if resource list dig in
				if (reference.getClass() == this.getClass()) {
					// resource list
					if (isLoop((ResourceList)reference, references)); {
						return true;
					}
				}
			}
		}
		return false;
	}
}
