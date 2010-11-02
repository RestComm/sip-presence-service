package org.openxdm.xcap.server.slee.appusage.xcapcaps;

import org.mobicents.xdm.server.appusage.AuthorizationPolicy;
import org.openxdm.xcap.common.uri.DocumentSelector;

public class XCAPCapsAppUsageAuthorizationPolicy extends AuthorizationPolicy {

	@Override
	public boolean isAuthorized(String user, Operation operation,
			DocumentSelector documentSelector) throws NullPointerException {
		if (documentSelector.isUserDocument()) {
			// there are no user docs
			return false;
		}
		else {
			if (operation == Operation.GET) {
				return true;
			}
			else {
				// puts are done internally by the app usage itself, i.e., user is null
				return user == null;
			}					
		}
	}

}
