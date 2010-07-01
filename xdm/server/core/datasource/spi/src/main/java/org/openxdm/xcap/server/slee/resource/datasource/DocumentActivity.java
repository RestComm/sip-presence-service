package org.openxdm.xcap.server.slee.resource.datasource;

import org.openxdm.xcap.common.uri.DocumentSelector;

/**
 * Activity Object for the DataSource Resource Adaptor. Represents the events
 * that update a Document.
 * 
 * @author Eduardo Martins
 * 
 */
public class DocumentActivity extends ActivityObject {
	
	public DocumentActivity(DocumentSelector documentSelector) {
		super(documentSelector.toString());
	}

	public String getDocumentSelector() {
		return id;
	}

	public String toString() {
		return new StringBuilder("DocumentActivity[documentSelector="
				+ id+ "]").toString();
	}
}
