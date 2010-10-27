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

