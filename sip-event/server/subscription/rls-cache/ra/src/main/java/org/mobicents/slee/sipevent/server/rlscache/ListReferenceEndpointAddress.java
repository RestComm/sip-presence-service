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

package org.mobicents.slee.sipevent.server.rlscache;

import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.ElementSelector;

public class ListReferenceEndpointAddress {
	
	private final DocumentSelector documentSelector;
	
	private final ElementSelector elementSelector;
	
	public ListReferenceEndpointAddress(DocumentSelector documentSelector,
			ElementSelector elementSelector) {
		this.documentSelector = documentSelector;
		this.elementSelector = elementSelector;
	}
	
	public DocumentSelector getDocumentSelector() {
		return documentSelector;
	}

	public ElementSelector getElementSelector() {
		return elementSelector;
	}
	
	@Override
	public int hashCode() {
		return documentSelector.hashCode()*31+elementSelector.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListReferenceEndpointAddress other = (ListReferenceEndpointAddress) obj;
		if (!documentSelector.equals(other.documentSelector))
			return false;
		if (!elementSelector.toString().equals(other.elementSelector.toString()))
			return false;
		return true;
	}
	
	private String toString = null;
	
	@Override
	public String toString() {
		if (toString == null) {
			toString = new StringBuilder("ListReferenceEndpointAddress[ ds = ").append(documentSelector).append(", es = ").append(elementSelector).append(" ]").toString();
		}
		return toString;
	}
	
}
