package org.mobicents.slee.sipevent.server.publication.data;

import java.io.Serializable;

/**
 * 
 * @author eduardomartins
 *
 */

public class ComposedPublicationKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6638892043798746768L;
	
    private String entity;
    private String eventPackage;

    public ComposedPublicationKey(String entity, String eventPackage) {
        this.entity = entity;
        this.eventPackage = eventPackage;
    }

    public String getEntity() {
		return entity;
	}
    
    public String getEventPackage() {
		return eventPackage;
	}
    
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == this.getClass()) {
            ComposedPublicationKey other = (ComposedPublicationKey) obj;
            return this.entity.equals(other.entity) && this.eventPackage.equals(other.eventPackage);
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        int result;
        result = eventPackage.hashCode();
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
    		toString = new StringBuilder(entity).append(':').append(eventPackage).toString();
    	}
    	return toString;
    }
    
}

