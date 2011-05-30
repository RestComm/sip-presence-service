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

package org.openxdm.xcap.common.uri;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

/**
 * A document selector points to a document resource on
 * a XCAP server. It's built from a application usage id (auid), document selector string, a document parent selector string, and the document name.
 * 
 * Usage Example that creates a document selector pointing to 'resource-lists' document named
 * 'index', for user 'sip:eduardo@mobicents.org'
 * 
 * DocumentSelector documentSelector = new DocumentSelector(
 * "resource-lists", "users/user/sip%3Aeduardo%40mobicents.org",index");
 * 
 * DocumentSelector documentSelector = DocumentSelector.valueOf("resource-lists/users/user/sip%3Aeduardo%40mobicents.org/index");
 * 
 * Note that you need to take care of percent encoding chars that are not
 * allowed in a valid URI.
 * 
 * @author Eduardo Martins
 *
 */
public class DocumentSelector implements Externalizable {

	private String collection;
	private String documentName;

	private transient String auid = null;
	private transient String user = null;
	
	private transient String toString = null;
	private transient Set<String> parentCollections = null; 
	
	public DocumentSelector() {
		// needed by externalizable
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		collection = in.readUTF();
		documentName = in.readUTF();		
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(collection);		
		out.writeUTF(documentName);
	}
	
	/**
	 * Builds a {@link DocumentSelector} from a {@link String} value. 
	 * @param documentSelector the document selector string
	 * @return
	 * @throws ParseException
	 */
	public static DocumentSelector valueOf(String documentSelector) throws ParseException {
		try {
			// get documentName & documentParent
			int documentNameSeparator = documentSelector.lastIndexOf("/");
			if (documentNameSeparator != -1) {				
				final String collection = documentSelector.substring(0,documentNameSeparator);
				final String documentName = documentSelector.substring(documentNameSeparator+1);				
				return new DocumentSelector(collection,documentName);				
			} else {
				throw new ParseException(null);
			}			
		}
		catch (IndexOutOfBoundsException e) {
			throw new ParseException(null,e);
		}
	}

	/**
	 * Creates a new instance of a document selector, from the specified collection and document name. 
	 * @param collection the collection of the document.
	 * @param documentName the document name.
	 */
	public DocumentSelector(String collection,
			String documentName) {
		this.collection = collection;
		this.documentName = documentName;
	}

	/**
	 * Retreives the application usage id of the document resource.
	 * @return
	 */
	public String getAUID() {
		if (auid == null) {
			int i = collection.indexOf('/');
			if (i > 0) {
				auid = collection.substring(0,i);
			}
			else {
				auid = "";
			}
		}
		return auid;
	}

	public String getUser() {
		if (user == null) {
			String[] collectionParts = collection.split("/");
			if (collectionParts.length > 2 && collectionParts[1].equals("users")) {
				user = collectionParts[2];
			}
		}
		return user;
	}
	
	/**
	 * Retreives the document's name of the document resource. 
	 * @return
	 */
	public String getDocumentName() {
		return documentName;
	}
	
	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}
	
	/**
	 * Retreives the document's parent of the document resource, including the auid 
	 * @return
	 */
	public String getCollection() {
		return collection;
	}
	
	public void setCollection(String collection) {
		this.collection = collection;
	}
	
	/**
	 * Indicates if the document selector is pointing to a document in the users tree.
	 * @return
	 */
	public boolean isUserDocument() {
		return getUser() != null ? true : false;
	}
	
	@Override
	public String toString() {
		if (toString == null) {
			toString = new StringBuilder(collection).append('/').append(documentName).toString(); 
		}
		return toString;
	}
	
	@Override
	public int hashCode() {
		return (collection.hashCode())*31+documentName.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			final DocumentSelector other = (DocumentSelector) obj;
			return this.collection.equals(other.collection) && this.documentName.equals(other.documentName);
		}
		else {
			return false;
		}
	}
	
	public Set<String> getParentCollections() {
		if (parentCollections == null) {
			parentCollections = new HashSet<String>();
			StringBuilder sb = new StringBuilder();
			String[] docSelectorParentParts = collection.split("/");
			String current = null;
			boolean first = true;
			for (String s : docSelectorParentParts) {
				if (s.length() != 0) {
					if (first) {
						current = sb.append(s).toString();
						first = false;
					}
					else {
						current = sb.append('/').append(s).toString();
					}
					parentCollections.add(current);
				}
			}
		}
		return parentCollections;
	}
	
}
