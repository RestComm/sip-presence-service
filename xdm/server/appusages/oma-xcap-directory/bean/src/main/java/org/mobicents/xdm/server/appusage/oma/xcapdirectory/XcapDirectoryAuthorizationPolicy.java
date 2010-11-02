package org.mobicents.xdm.server.appusage.oma.xcapdirectory;

import org.mobicents.xdm.server.appusage.AuthorizationPolicy;
import org.openxdm.xcap.common.uri.DocumentSelector;

public class XcapDirectoryAuthorizationPolicy extends AuthorizationPolicy {

	private static final String PATH_SEPARATOR = "/";

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.xdm.server.appusage.AuthorizationPolicy#isAuthorized(java.lang.String, org.mobicents.xdm.server.appusage.AuthorizationPolicy.Operation, org.openxdm.xcap.common.uri.DocumentSelector)
	 */
	@Override
	public boolean isAuthorized(String user, Operation operation,
			DocumentSelector documentSelector) throws NullPointerException {
		if (operation == Operation.GET) {
			if (documentSelector.isUserDocument()) {
				final String[] documentParentParts = documentSelector.getDocumentParent().split(PATH_SEPARATOR);
				if (user.equalsIgnoreCase(documentParentParts[1])) {
					return true;
				} else {
					return false;
				}
			}
			else {
				// /auid/global dir, never authorize operation except pre-authorized users
				// which will not need to use the auth policy
				return false;
			}		
		}
		else {
			// modifications are done by the app usage itself
			return false;
		}
	}

}
