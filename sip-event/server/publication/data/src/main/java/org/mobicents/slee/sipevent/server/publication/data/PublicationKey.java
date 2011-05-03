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

package org.mobicents.slee.sipevent.server.publication.data;

import java.io.Serializable;

/**
 * 
 * @author eduardomartins
 *
 */

public class PublicationKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6638892043798746768L;
	
    private String eTag;
    private String entity;
    private String eventPackage;
    
    public PublicationKey(String eTag, String entity, String eventPackage) {
        this.eTag = eTag;
        this.entity = entity;
        this.eventPackage = eventPackage;
    }

    public String getEntity() {
		return entity;
	}
    
    public String getETag() {
		return eTag;
	}
    
    public String getEventPackage() {
		return eventPackage;
	}
    
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == this.getClass()) {
            PublicationKey other = (PublicationKey) obj;
            return this.eTag.equals(other.eTag) && this.entity.equals(other.entity) && this.eventPackage.equals(other.eventPackage);
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        int result = eTag.hashCode();
        result = 31 * result + eventPackage.hashCode();
        result = 31 * result + entity.hashCode();
        return result;
    }

    private transient String toString = null;
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	if (toString == null) {
    		toString = new StringBuilder(eTag).append(':').append(entity).append(':').append(eventPackage).toString();
    	}
    	return toString;
    }

}

