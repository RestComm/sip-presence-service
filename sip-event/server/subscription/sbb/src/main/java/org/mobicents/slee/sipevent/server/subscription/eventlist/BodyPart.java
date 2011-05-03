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
