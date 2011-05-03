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

package org.openxdm.xcap.server.slee.resource.datasource;

import java.io.Serializable;

import javax.slee.EventTypeID;

import org.openxdm.xcap.common.uri.DocumentSelector;
import org.w3c.dom.Document;

public final class DocumentUpdatedEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * IMPORTANT: must sync with the event descriptor data!!!!
	 */
	public static final EventTypeID EVENT_TYPE_ID = new EventTypeID("DocumentUpdatedEvent","org.openxdm","1.0");
	
	private final String id;
		
	private final DocumentSelector documentSelector;
	private final String oldETag;
	private final String newETag;
	private final String documentAsString;
	private final Document document;
	
	public DocumentUpdatedEvent(DocumentSelector documentSelector,
			String oldETag, String newETag, String documentAsString,
			Document document) {		
		this.documentSelector = documentSelector;
		this.oldETag = oldETag;
		this.newETag = newETag;
		this.documentAsString = documentAsString;
		this.document = document;
		// if doc was deleted add a non hex char to the old etag as event id
		id = (newETag != null) ? newETag : oldETag + "g";
	}
	
	public Document getDocument() {
		return document;
	}
	
	public String getDocumentAsString() {
		return documentAsString;
	}
	
	public DocumentSelector getDocumentSelector() {
		return documentSelector;
	}
	
	public String getNewETag() {
		return newETag;
	}
	
	public String getOldETag() {
		return oldETag;
	}
	
	public boolean equals(Object o) {
		if (o != null && o.getClass() == this.getClass()) {
			return ((DocumentUpdatedEvent)o).id.equals(id);
		}
		else {
			return false;
		}	
	}
	
	public int hashCode() {		
		return id.hashCode();
	}
}
