package org.mobicents.xdm.server.appusage;

import org.openxdm.xcap.common.uri.DocumentSelector;

public abstract class AuthorizationPolicy {
	
	public abstract boolean isAuthorized(String user, Operation operation, DocumentSelector documentSelector) throws NullPointerException;
	
	public static enum Operation { GET, PUT, DELETE }
	
}
