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

package org.mobicents.slee.xdm.server.datasource.jpa;

import java.io.Serializable;
import java.io.StringReader;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.error.NotWellFormedConflictException;
import org.openxdm.xcap.common.xml.XMLValidator;


/**
 *     
 * @author eduardomartins
 *
 */
@Entity
@Table(name = "XDM_DATASOURCE_DOCUMENTS")
@NamedQueries({
	@NamedQuery(name=Document.JPA_QUERY_SELECT_DOCUMENTS_IN_COLLECTION,query="SELECT x FROM Document x WHERE x.key.collection = :collection"),
	@NamedQuery(name=Document.JPA_QUERY_SELECT_DOCUMENTS_MATCH_COLLECTION,query="SELECT x FROM Document x WHERE x.key.collection LIKE :collection"),
	@NamedQuery(name=Document.JPA_QUERY_UPDATE_DOCUMENTS_FROM_KEY,query="UPDATE Document x SET x.xml=:xml,x.eTag=:eTag WHERE x.key.collection = :collection AND x.key.documentName = :documentName"),
	@NamedQuery(name=Document.JPA_QUERY_DELETE_DOCUMENTS_FROM_KEY,query="DELETE FROM Document x WHERE x.key.collection = :collection AND x.key.documentName = :documentName")
	})
public class Document implements org.openxdm.xcap.common.datasource.Document, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3697052553779974529L;

	public static final String JPA_QUERY_SELECT_DOCUMENTS_IN_COLLECTION = "selectDocumentsInCollection";
	public static final String JPA_QUERY_SELECT_DOCUMENTS_MATCH_COLLECTION = "selectDocumentsMatchCollection";
	public static final String JPA_QUERY_UPDATE_DOCUMENTS_FROM_KEY = "updateDocumentFromKey";
	public static final String JPA_QUERY_DELETE_DOCUMENTS_FROM_KEY = "deleteDocumentFromKey";
	
	private transient org.w3c.dom.Document domDocument = null;
	
	@EmbeddedId
	protected DocumentPrimaryKey key;
	
	/**
	 * the document XML
	 */
	@Column(name = "XML", length=65535, nullable = false)
	private String xml;
	
	/**
	 * the document entity tag, a.k.a. the version
	 */
	@Column(name = "ETAG", nullable =false)
	private String eTag;
	
	public Document() {
		// TODO Auto-generated constructor stub
	}
	
	public Document(String collection, String documentName) {
		setKey(new DocumentPrimaryKey(collection,documentName));
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			Document other = (Document) obj;
			return other.key.equals(this.key);
		}
		else {
			return false;
		}
	}

	// -- GETTERS AND SETTERS
	
	public DocumentPrimaryKey getKey() {
		return key;
	}

	public void setKey(DocumentPrimaryKey key) {
		this.key = key;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public String getETag() {
		return eTag;
	}

	public void setETag(String tag) {
		eTag = tag;
	}
	
	//
	
	public String getCollection() {
		return key.getCollection();
	}
	
	public String getDocumentName() {
		return key.getDocumentName();
	}
	
    public String getAsString() throws InternalServerErrorException {
        return getXml();
    }

    public org.w3c.dom.Document getAsDOMDocument() throws InternalServerErrorException {
        if (domDocument == null) {
        	try {
        		domDocument = XMLValidator.getWellFormedDocument(new StringReader(xml));
            } catch (NotWellFormedConflictException e) {
                throw new InternalServerErrorException(e.getMessage());
            }
        }
    	return domDocument;
    }
}