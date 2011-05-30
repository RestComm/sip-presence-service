package org.mobicents.xdm.common.util.dom;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DocumentCloner {

	private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = initDomFactory();
	
	private static DocumentBuilderFactory initDomFactory() {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		return documentBuilderFactory;
	}

	/**
	 * Clones a DOM document.
	 * @param document
	 * @return
	 * @throws InternalServerErrorException
	 */
	public static Document clone(Document document) throws InternalServerErrorException {
		Document documentClone = null;
		try {
			documentClone = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			throw new InternalServerErrorException("failed to clone document", e);
		}
		Node rootElementClone = documentClone.importNode(document.getDocumentElement(), true);
		documentClone.appendChild(rootElementClone);
		return documentClone;
	}
}
