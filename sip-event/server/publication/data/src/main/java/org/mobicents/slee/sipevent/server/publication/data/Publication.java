package org.mobicents.slee.sipevent.server.publication.data;

import java.io.Serializable;

import javax.xml.bind.JAXBElement;

/**
 *  
 *     This class is JPA pojo for a publication of sip events.
 *     
 * @author eduardomartins
 *
 */
public class Publication implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8020033417766370446L;

	/**
	 * the publication key
	 */   
	private final PublicationKey publicationKey;
	
	/**
	 * the id of the SLEE timer associated with this subscription
	 */
	private Serializable timerID;
	
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
	
	public Publication(PublicationKey publicationKey, String document, String contentType, String contentSubType, JAXBContentHandler jaxbContentHandler) {
		this.publicationKey = publicationKey;
		this.document = document;
		this.contentType = contentType;
		this.contentSubType = contentSubType;
		this.jaxbContentHandler = jaxbContentHandler;
	}

	// -- GETTERS AND SETTERS
	
	public PublicationKey getPublicationKey() {
		return publicationKey;
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

	public Serializable getTimerID() {
		return timerID;
	}

	public void setTimerID(Serializable timerID) {
		this.timerID = timerID;
	}
	
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
	
	@Override
	public String toString() {
		return new StringBuilder("publication: key=").append(publicationKey).toString();
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
