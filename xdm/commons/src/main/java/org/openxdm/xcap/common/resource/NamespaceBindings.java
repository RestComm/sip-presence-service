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

package org.openxdm.xcap.common.resource;

import java.util.Iterator;
import java.util.Map;

public class NamespaceBindings implements Resource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String elementLocalName;
	private Map<String,String> namespaceBindings;
	
	public NamespaceBindings(String elementLocalName, Map<String,String> namespaceBindings) {
		this.elementLocalName = elementLocalName;
		this.namespaceBindings = namespaceBindings;
	}
	
	public String getMimetype() {		
		return "application/xcap-ns+xml";
	}

	public String getElementLocalName() {
		return elementLocalName;
	}
	
	public Map<String,String> getNamespaceBindings() {
		return namespaceBindings;
	}
	
	public String toXML() {
		StringBuilder sb = new StringBuilder("<").append(elementLocalName);
		for(Iterator<String> it=namespaceBindings.keySet().iterator();it.hasNext();) {
			String namespaceKey = it.next();
			sb.append(' ').append(namespaceKey).append("=\"").append(namespaceBindings.get(namespaceKey)).append("\"");
		}
		sb.append("/>");
		return sb.toString();
	}

}
