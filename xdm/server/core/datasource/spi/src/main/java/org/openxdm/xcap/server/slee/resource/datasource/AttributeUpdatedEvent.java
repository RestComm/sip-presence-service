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
import java.util.Map;

import javax.slee.EventTypeID;

import org.openxdm.xcap.common.uri.AttributeSelector;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.NodeSelector;

public final class AttributeUpdatedEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * IMPORTANT: must sync with the event descriptor data!!!!
	 */
	public static final EventTypeID EVENT_TYPE_ID = new EventTypeID("AttributeUpdatedEvent","org.openxdm","1.0");
		
	private final DocumentSelector documentSelector;
	private final NodeSelector nodeSelector;
	private final AttributeSelector attributeSelector;
	private final Map<String, String> namespaces;
	private final String oldETag;
	private final String newETag;
	private final String documentAsString;
	
	private final String attributeValue;
	
	public AttributeUpdatedEvent(DocumentSelector documentSelector,
			NodeSelector nodeSelector, AttributeSelector attributeSelector,
			Map<String, String> namespaces, String oldETag, String newETag,
			String documentAsString, String attributeValue) {
		if (newETag == null) {
			throw new IllegalArgumentException("newETag arg can't be null");
		}
		this.documentSelector = documentSelector;
		this.nodeSelector = nodeSelector;
		this.attributeSelector = attributeSelector;
		this.namespaces = namespaces;
		this.oldETag = oldETag;
		this.newETag = newETag;
		this.documentAsString = documentAsString;
		
		this.attributeValue = attributeValue;
	}

	public AttributeSelector getAttributeSelector() {
		return attributeSelector;
	}
	
	public String getAttributeValue() {
		return attributeValue;
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
	
	public Map<String, String> getNamespaces() {
		return namespaces;
	}
	
	public NodeSelector getNodeSelector() {
		return nodeSelector;
	}
	
	public String getOldETag() {
		return oldETag;
	}
	
	public boolean equals(Object o) {
		if (o != null && o.getClass() == this.getClass()) {
			return ((AttributeUpdatedEvent)o).newETag.equals(newETag);
		}
		else {
			return false;
		}	
	}
	
	public int hashCode() {		
		return newETag.hashCode();
	}
}
