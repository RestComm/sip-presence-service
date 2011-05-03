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

package org.openxdm.xcap.common.xml;

import java.util.Iterator;
import java.util.Map;

import javax.xml.*;

public class NamespaceContext implements javax.xml.namespace.NamespaceContext {

	private Map<String,String> namespaces;
	
	public NamespaceContext(Map<String,String> namespaces) {
		this.namespaces = namespaces;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
	 */
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
        	throw new IllegalArgumentException("Null prefix");
        }
        else {        	
        	String namespace = namespaces.get(prefix);        	
        	if (namespace == null) {
        		return XMLConstants.NULL_NS_URI;
        	} else {
        		return namespace;
        	}
        }        
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    public String getPrefix(String uri) {
        for(Iterator<String> i=namespaces.keySet().iterator();i.hasNext();) {
        	String prefix = i.next();
        	if ((namespaces.get(prefix)).equals(uri)) {
        		return prefix;
        	}
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
    public Iterator<String> getPrefixes(String uri) {
        return namespaces.keySet().iterator();
    }

    /**
     * 
     * @return
     */
    public Map<String, String> getNamespaces() {
		return namespaces;
	}
    
    /**
     * 
     * @param namespace
     */
    public void setDefaultDocNamespace(String namespace) {
    	namespaces.put(XMLConstants.DEFAULT_NS_PREFIX,namespace);
    }
    
}