package org.mobicents.slee.sipevent.server.subscription.eventlist;

import java.io.Serializable;

/**
 * Simple multipart/related body part string constructor.
 * @author martins
 *
 */
public class BodyPart implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String uri;
	private final String contentTransferEncoding;
	private final String contentID;
	private final String contentType;
	private final String contentSubType;
	private final String contentTypeCharset;
	private final String body;
	private final String toString;
	
	public BodyPart(String uri,String contentTransferEncoding, String contentID,
			String contentType, String contentSubType, String contentTypeCharset, String body) {		
		this.uri = uri;
		this.contentTransferEncoding = contentTransferEncoding;
		this.contentID = contentID;
		this.contentType = contentType;
		this.contentSubType = contentSubType;
		this.contentTypeCharset = contentTypeCharset;
		this.body = body;
		this.toString = buildToString();
	}
	
	private String buildToString() {
		return "Content-Transfer-Encoding: "+ contentTransferEncoding +
		"\nContent-ID: <" + contentID + ">"+
		"\nContent-Type: " + contentType + "/" + contentSubType + ";charset=\""+contentTypeCharset+"\"\n\n" +
		body + "\n\n";
	}
	
	public String getContentTransferEncoding() {
		return contentTransferEncoding;
	}

	public String getContentID() {
		return contentID;
	}

	public String getContentType() {
		return contentType;
	}

	public String getContentSubType() {
		return contentSubType;
	}

	public String getContentTypeCharset() {
		return contentTypeCharset;
	}

	public String getBody() {
		return body;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	
	public int hashCode() {
		return this.contentID.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			BodyPart other = (BodyPart) obj;
			return other.contentID.equals(this.contentID);
		}
		else {
			return false;
		}
	}
	
	public String toString() {		
		return toString;			
	}
}
