package org.mobicents.slee.sipevent.server.rlscache;

import javax.slee.ActivityContextInterface;
import javax.slee.FactoryException;
import javax.slee.UnrecognizedActivityException;

public interface RLSServicesCacheActivityContextInterfaceFactory {

	public ActivityContextInterface getActivityContextInterface(
    		RLSServiceActivity activity) throws NullPointerException,
            UnrecognizedActivityException, FactoryException;
	
	public ActivityContextInterface getActivityContextInterface(
    		ResourceListsActivity activity) throws NullPointerException,
            UnrecognizedActivityException, FactoryException;
}
