package org.mobicents.xdm.server.appusage;

import org.openxdm.xcap.common.uri.DocumentSelector;

public interface AuthorizationPolicy {
	
	public boolean isAuthorized(String user, Operation operation, DocumentSelector documentSelector, AppUsageDataSource dataSource) throws NullPointerException;
	
	public static enum Operation { GET, PUT, DELETE }
	
}
