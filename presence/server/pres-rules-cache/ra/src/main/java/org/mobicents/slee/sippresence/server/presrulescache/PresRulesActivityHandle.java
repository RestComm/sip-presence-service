package org.mobicents.slee.sippresence.server.presrulescache;

import javax.slee.resource.ActivityHandle;

import org.openxdm.xcap.common.uri.DocumentSelector;

public class PresRulesActivityHandle implements ActivityHandle {

	private final DocumentSelector documentSelector;
	
	public PresRulesActivityHandle(DocumentSelector documentSelector) {
		if (documentSelector == null) {
			throw new NullPointerException("null document selector");
		}
		this.documentSelector = documentSelector;
	}
	
	public DocumentSelector getDocumentSelector() {
		return documentSelector;
	}

	@Override
	public int hashCode() {
		return documentSelector.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PresRulesActivityHandle other = (PresRulesActivityHandle) obj;
		return this.documentSelector.equals(other.documentSelector);
	}
		
	@Override
	public String toString() {
		return new StringBuilder("PresRulesActivityHandle[ds=").append(documentSelector).append("]").toString();
	}
}
