package org.mobicents.slee.sipevent.server.publication.data;

import java.io.Serializable;

import javax.xml.bind.JAXBElement;

/**
 *  
 *     
 * @author eduardomartins
 *
 */
public class ComposedPublication implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8020033417766370446L;

	/**
	 * the publication key
	 */   
	private final ComposedPublicationKey composedPublicationKey;
	
	/**
	 * the document published
	 */
	private String document;
	
	/**
	 * the type of document published
	 */
	private String contentType;
	private String contentSubType;
	
	/**
	 * unmarshalled version of the document
	 */
	private transient JAXBElement<?> unmarshalledContent;
	
	/**
	 * handles marshalling and unmarshalling of jaxb pojos
	 */
	private transient JAXBContentHandler jaxbContentHandler;
	
	public static ComposedPublication fromPublication(Publication p) {
		final ComposedPublicationKey cpk = new ComposedPublicationKey(p.getPublicationKey().getEntity(),p.getPublicationKey().getEventPackage()); 
		final ComposedPublication cp = new ComposedPublication(cpk);
		cp.setDocument(p.getDocument());
		cp.setContentType(p.getContentType());
		cp.setContentSubType(p.getContentSubType());
		cp.setJaxbContentHandler(p.getJaxbContentHandler());
		cp.setUnmarshalledContent(p.getUnmarshalledContent());
		return cp;
	}
	
	public ComposedPublication(ComposedPublicationKey composedPublicationKey) {
		this.composedPublicationKey = composedPublicationKey;		
	}

	// -- GETTERS AND SETTERS
	
	public ComposedPublicationKey getComposedPublicationKey() {
		return composedPublicationKey;
	}

	public String getContentSubType() {
		return contentSubType;
	}
	
	public void setContentSubType(String contentSubType) {
		this.contentSubType = contentSubType;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getDocument() {
		if (document == null && unmarshalledContent != null) {
			if (jaxbContentHandler == null) {
				throw new IllegalStateException("jaxbContentHandler not set");
			}
			document = jaxbContentHandler.marshallToString(unmarshalledContent);
		}
		return document;
	}

	public void setDocument(String document) {
		this.document = document;
	}
	
	/**
	 * Retrieves cached unmarshalled version of the content, if available.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JAXBElement getUnmarshalledContent() {
		if (unmarshalledContent == null && document != null) {
			if (jaxbContentHandler == null) {
				throw new IllegalStateException("jaxbContentHandler not set");
			}
			unmarshalledContent = jaxbContentHandler.unmarshallFromString(document);
		}
		return unmarshalledContent;
	}
	
	/**
	 * Sets unmarshalled version of the content for caching, this is not persisted.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void setUnmarshalledContent(JAXBElement unmarshalledContent) {
		this.unmarshalledContent = unmarshalledContent;		
	}
	
	/**
	 * @return the jaxbContentHandler
	 */
	public JAXBContentHandler getJaxbContentHandler() {
		return jaxbContentHandler;
	}
	
	/**
	 * @param jaxbContentHandler the jaxbContentHandler to set
	 */
	public void setJaxbContentHandler(JAXBContentHandler jaxbContentHandler) {
		this.jaxbContentHandler = jaxbContentHandler;
	}
	
	/**
	 * Forces the update of the marshalled document, to be used when there is an
	 * update using the unmarshalled content
	 */
	public void forceDocumentUpdate() {
		setDocument(null);
		getDocument();
	}
}
