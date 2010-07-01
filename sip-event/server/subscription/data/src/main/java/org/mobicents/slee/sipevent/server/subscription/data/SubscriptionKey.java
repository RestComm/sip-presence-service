package org.mobicents.slee.sipevent.server.subscription.data;

import java.io.Serializable;

/**
 * 
 * @author eduardomartins
 *
 */
public class SubscriptionKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6638892043798746768L;
	
	public static final String NO_DIALOG_ID = "_";
	
    private final String dialogId;
    private final String eventPackage;
    private final String eventId;

    public SubscriptionKey(String dialogId, String eventPackage, String eventId) {
        if (dialogId == null) {
        	throw new NullPointerException("null dialog id");
        }
    	this.dialogId = dialogId;    	
    	this.eventPackage = eventPackage;
        this.eventId = eventId;
    }

    public String getDialogId() {
		return dialogId;
	}
    
    public String getEventId() {
    	return eventId;
	}
    
    public String getEventPackage() {
		return eventPackage;
	}

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dialogId == null) ? 0 : dialogId.hashCode());
		result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
		result = prime * result
				+ ((eventPackage == null) ? 0 : eventPackage.hashCode());		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubscriptionKey other = (SubscriptionKey) obj;
		if (dialogId == null) {
			if (other.dialogId != null)
				return false;
		} else if (!dialogId.equals(other.dialogId))
			return false;
		if (eventId == null) {
			if (other.eventId != null)
				return false;
		} else if (!eventId.equals(other.eventId))
			return false;
		if (eventPackage == null) {
			if (other.eventPackage != null)
				return false;
		} else if (!eventPackage.equals(other.eventPackage))
			return false;		
		return true;
	}

	private transient String toString = null;
    public String toString() {
    	if (toString == null) {
    		toString = "SubscriptionKey:dialogId="+dialogId+",eventPackage="+eventPackage+",eventId="+eventId;
    	}
        return toString; 
    }

    public boolean isInternalSubscription() {
    	// no need to test both call id and remote tag
		return dialogId.equals(SubscriptionKey.NO_DIALOG_ID);
	}
}