package org.mobicents.xdm.server.appusage.oma.userprofile;

import org.mobicents.xdm.server.appusage.AuthorizationPolicy;
import org.openxdm.xcap.common.uri.DocumentSelector;

public class OMAUserProfileAuthorizationPolicy extends AuthorizationPolicy {

	private final static String ALLOWED_DOC_NAME = "user-profile";
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.xdm.server.appusage.AuthorizationPolicy#isAuthorized(java.lang.String, org.mobicents.xdm.server.appusage.AuthorizationPolicy.Operation, org.openxdm.xcap.common.uri.DocumentSelector)
	 */
	@Override
	public boolean isAuthorized(String user, Operation operation,
			DocumentSelector documentSelector) throws NullPointerException {
		if (!documentSelector.isUserDocument()) {
			// no global docs
			return false;
		}
		if (operation == Operation.GET) {
			// any principal can retrieve profiles
			return true;	
		}
		else {
			// modifications can only be done by the profile principal
			String xui = documentSelector.getDocumentParent().substring(6);
			if (user.equals(xui)) {
				if (operation == Operation.PUT) {
					// check doc name
					return ALLOWED_DOC_NAME.equals(documentSelector.getDocumentName());					
				}
				else {
					return true;
				}
			}
			else {
				return false;
			}
		}
	}

}
