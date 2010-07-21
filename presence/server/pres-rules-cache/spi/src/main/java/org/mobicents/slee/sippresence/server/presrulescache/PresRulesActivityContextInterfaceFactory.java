package org.mobicents.slee.sippresence.server.presrulescache;

import javax.slee.ActivityContextInterface;
import javax.slee.FactoryException;
import javax.slee.UnrecognizedActivityException;

public interface PresRulesActivityContextInterfaceFactory {

	public ActivityContextInterface getActivityContextInterface(
    		PresRulesActivity activity) throws NullPointerException,
            UnrecognizedActivityException, FactoryException;
}
