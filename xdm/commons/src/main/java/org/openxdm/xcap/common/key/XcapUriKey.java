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

import java.io.Serializable;

import org.openxdm.xcap.common.uri.ResourceSelector;

/**
 * This key selects a resource on a XCAP server. It's built from a resource selector, the lowest level selector for a XCAP resource.
 * 
 * Note that the resource selector provided must take care of percent enconding chars that are not
 * allowed in a valid XCAP URI.
 * 
 * 
 * @author Eduardo Martins
 *
 */

public class XcapUriKey implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ResourceSelector resourceSelector;
		
	public XcapUriKey(ResourceSelector resourceSelector) {
		this.resourceSelector = resourceSelector;
	}
	
	public ResourceSelector getResourceSelector() {
		return resourceSelector;
	}

	public String toString() {
		if(toString==null){
			toString = resourceSelector.toString();			
		}
		return toString;
	}
	
	private String toString = null;

	public boolean equals(Object obj) {
        if (obj instanceof XcapUriKey)
        	return toString().equals(((XcapUriKey)obj).toString());
        else
        	return false;
    }    
        
    public int hashCode() {
    	return toString().hashCode();
    }
    
}

