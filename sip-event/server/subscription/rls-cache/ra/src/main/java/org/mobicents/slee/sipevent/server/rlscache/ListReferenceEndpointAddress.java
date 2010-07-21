package org.mobicents.slee.sipevent.server.rlscache;

import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.ElementSelector;

public class ListReferenceEndpointAddress {
	
	private final DocumentSelector documentSelector;
	
	private final ElementSelector elementSelector;
	
	public ListReferenceEndpointAddress(DocumentSelector documentSelector,
			ElementSelector elementSelector) {
		this.documentSelector = documentSelector;
		this.elementSelector = elementSelector;
	}
	
	public DocumentSelector getDocumentSelector() {
		return documentSelector;
	}

	public ElementSelector getElementSelector() {
		return elementSelector;
	}
	
	@Override
	public int hashCode() {
		return documentSelector.hashCode()*31+elementSelector.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListReferenceEndpointAddress other = (ListReferenceEndpointAddress) obj;
		if (!documentSelector.equals(other.documentSelector))
			return false;
		if (!elementSelector.toString().equals(other.elementSelector.toString()))
			return false;
		return true;
	}
	
	private String toString = null;
	
	@Override
	public String toString() {
		if (toString == null) {
			toString = new StringBuilder("ListReferenceEndpointAddress[ ds = ").append(documentSelector).append(", es = ").append(elementSelector).append(" ]").toString();
		}
		return toString;
	}
	
}
