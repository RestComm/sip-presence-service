package org.mobicents.xdm.server.appusage.oma.lockeduserprofile;

import org.mobicents.xdm.server.appusage.AuthorizationPolicy;
import org.openxdm.xcap.common.uri.DocumentSelector;

public class OMALockedUserProfileAuthorizationPolicy extends
		AuthorizationPolicy {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AuthorizationPolicy#isAuthorized(java
	 * .lang.String,
	 * org.mobicents.xdm.server.appusage.AuthorizationPolicy.Operation,
	 * org.openxdm.xcap.common.uri.DocumentSelector)
	 */
	@Override
	public boolean isAuthorized(String user, Operation operation,
			DocumentSelector documentSelector) throws NullPointerException {
		if (!documentSelector.isUserDocument()) {
			// no global docs
			return false;
		}
		/*
		 * The Service Provider SHALL be the only entity allowed to create the
		 * document on behalf of the Primary Principal. The Service Provider
		 * SHALL have all permissions on the document. The Primary Principal
		 * SHALL only have the read permission to this document.
		 */
		String xui = documentSelector.getDocumentParent().substring(6);
		if (operation == Operation.GET) {
			if (user.equals(xui)) {
				return true;
			} else {
				return false;
			}
		} else {
			// modifications done by service provider locally, i.e., no user, no
			// auth
			return false;
		}
	}

}
