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

package org.openxdm.xcap.common.key;

import java.util.Map;

import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.ResourceSelector;

public class NamespaceBindingsUriKey extends XcapUriKey {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DocumentSelector documentSelector;
	private ElementSelector elementSelector;
	private Map<String,String> namespaces;
	
	public NamespaceBindingsUriKey(DocumentSelector documentSelector,ElementSelector elementSelector,Map<String,String> namespaces) {
		super(new ResourceSelector(documentSelector.toString(),getNodeSelector(elementSelector),namespaces));
		this.documentSelector = documentSelector;
		this.elementSelector = elementSelector;
		this.namespaces = namespaces;
	}
	
	public NamespaceBindingsUriKey(DocumentSelector documentSelector,ElementSelector elementSelector) {
		this(documentSelector,elementSelector,null);
	}
	
	public DocumentSelector getDocumentSelector() {
		return documentSelector;
	}
	
	public ElementSelector getElementSelector() {
		return elementSelector;
	}
	
	public Map<String, String> getNamespaces() {
		return namespaces;
	}
	
	private static String getNodeSelector(ElementSelector elementSelector) {
		return new StringBuilder(KeyUtils.getPercentEncondedElementSelector(elementSelector)).append("/namespace%3A%3A%2A").toString();
	}
		
}
