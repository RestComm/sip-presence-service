package org.openxdm.xcap.server.slee.appusage.rlsservices;

import org.mobicents.xdm.server.appusage.AuthorizationPolicy;
import org.openxdm.xcap.common.uri.DocumentSelector;

/**
 * This XCAP Authorization Policy implements the Default
 * Authorization Policy.
 * 
 * By XCAP Specs:
 * 
 * "This application usage does not modify the default XCAP authorization
 * policy, which is that only a user can read, write or modify their own
 * documents.  A server can allow privileged users to modify documents
 * that they don't own, but the establishment and indication of such
 * policies is outside the scope of this document.  It is anticipated
 * that a future application usage will define which users are allowed
 * to modify an RLS services document.
 * 
 * The index document maintained in the global tree represents sensitive
 * information, as it contains the union of all of the information for
 * all users on the server.  As such, its access MUST be restricted to
 * trusted elements within domain of the server.  Typically, this would
 * be limited to the RLSs that need access to this document."
 *    
 * @author Eduardo Martins
 *
 */

public class RLSServicesAuthorizationPolicy extends AuthorizationPolicy {

	private final static String authorizedUserDocumentName = "index";
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.xdm.server.appusage.AuthorizationPolicy#isAuthorized(java.lang.String, org.mobicents.xdm.server.appusage.AuthorizationPolicy.Operation, org.openxdm.xcap.common.uri.DocumentSelector)
	 */
	@Override
	public boolean isAuthorized(String user, AuthorizationPolicy.Operation operation, DocumentSelector documentSelector) throws NullPointerException {
		if (documentSelector.isUserDocument()) {
			final String[] documentParentParts = documentSelector.getDocumentParent().split("/");
			if (user.equalsIgnoreCase(documentParentParts[1])) {
				if (operation == Operation.PUT && !documentSelector.getDocumentName().equals(authorizedUserDocumentName) && documentParentParts.length != 2) {
					// doc name checked for puts 
					return false;
				}
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

}
