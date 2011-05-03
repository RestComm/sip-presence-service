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

    private transient Boolean internalSubscription;
    private transient Boolean wInfoSubscription;
    
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

    private transient int hashCode = 0;
    @Override
	public int hashCode() {
		if (hashCode == 0) {
			final int prime = 31;
			int result = dialogId.hashCode();
			result = prime * result + eventPackage.hashCode();
			if (eventId != null) {
				result = prime * result + eventId.hashCode();
			}
			hashCode = result;
		}
		return hashCode;
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
		return toString().equals(other.toString());
	}

	private transient String toString = null;
    public String toString() {
    	if (toString == null) {
    		final StringBuilder sb = new StringBuilder(dialogId).append('@').append(eventPackage);
    		if(eventId != null) {
    			sb.append('@').append(eventId);
    		}
    		toString = sb.toString();
    	}
        return toString; 
    }

    public boolean isInternalSubscription() {
    	if (internalSubscription == null) {
    		// no need to test both call id and remote tag
    		internalSubscription = Boolean.valueOf(dialogId.equals(SubscriptionKey.NO_DIALOG_ID));
    	}
    	return internalSubscription.booleanValue();
	}
    
    private static final String WINFO = ".winfo";
    
    public boolean isWInfoSubscription() {
    	if (wInfoSubscription == null) {
    		// no need to test both call id and remote tag
    		wInfoSubscription = Boolean.valueOf(eventPackage.endsWith(WINFO));
    	}
    	return wInfoSubscription.booleanValue();
	}
}