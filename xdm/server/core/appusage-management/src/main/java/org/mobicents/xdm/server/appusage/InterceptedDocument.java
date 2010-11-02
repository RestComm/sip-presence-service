package org.mobicents.xdm.server.appusage;

import javax.xml.transform.TransformerException;

import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.xml.TextWriter;

public class InterceptedDocument implements Document {

	private final DocumentSelector documentSelector;
	private final org.w3c.dom.Document domDocument;
	private String documentAsString;
	
	public InterceptedDocument(DocumentSelector documentSelector,org.w3c.dom.Document domDocument) {
		this.documentSelector = documentSelector;
		this.domDocument = domDocument;
	}
	
	@Override
	public String getAUID() {
		return documentSelector.getAUID();
	}

	@Override
	public org.w3c.dom.Document getAsDOMDocument()
			throws InternalServerErrorException {
		return domDocument;
	}

	@Override
	public String getAsString() throws InternalServerErrorException {
		if (documentAsString == null) {
			try {
				documentAsString = TextWriter.toString(domDocument);
			} catch (TransformerException e) {
				throw new InternalServerErrorException(e.getMessage(),e);
			}
		}
		return documentAsString;
	}

	@Override
	public String getDocumentName() {
		return documentSelector.getDocumentName();
	}

	@Override
	public String getDocumentParent() {
		return documentSelector.getDocumentParent();
	}

	@Override
	public String getETag() {
		return "NA";
	}
	
}