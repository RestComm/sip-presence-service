package org.mobicents.slee.sipevent.server.rlscache;

import org.openxdm.xcap.common.uri.DocumentSelector;

public class ResourceListActivityImpl implements ResourceListsActivity {

	public final static Class<?> TYPE = ResourceListActivityImpl.class;
	
	private final DocumentSelector documentSelector;
		
	public ResourceListActivityImpl(DocumentSelector documentSelector) {
		if (documentSelector == null) {
			throw new NullPointerException("null documentSelector");
		}
		this.documentSelector = documentSelector;
	}
	
	@Override
	public DocumentSelector getDocumentSelector() {
		return documentSelector;
	}
	
	@Override
	public String toString() {
		return new StringBuilder("ResourceListsActivityImpl[ds=").append(documentSelector).append("]").toString();
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
		ResourceListActivityImpl other = (ResourceListActivityImpl) obj;
		return this.documentSelector.equals(other.documentSelector);
	}
	
	
}
