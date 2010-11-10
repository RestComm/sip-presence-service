package org.mobicents.xdm.server.appusage;

import org.openxdm.xcap.common.uri.DocumentSelector;

/**
 * This XCAP Authorization Policy implements the Default
 * Authorization Policy.
 * 
 * By XCAP Specs:
 * 
 * "By default, each user is able to access (read, modify, and delete)
 * all of the documents below their home directory, and any user is able
 * to read documents within the global directory.  However, only trusted
 * users, explicitly provisioned into the server, can modify global
 * documents."
 *    
 * @author Eduardo Martins
 *
 */
public class DefaultAuthorizationPolicy extends AuthorizationPolicy {

	private final String authorizedUserDocumentName;
	
	public DefaultAuthorizationPolicy(String authorizedUserDocumentName) {
		this.authorizedUserDocumentName = authorizedUserDocumentName;
	}
	
	public boolean isAuthorized(String user, AuthorizationPolicy.Operation operation, DocumentSelector documentSelector) throws NullPointerException {
		
		// check args
		if (user == null) {
			throw new NullPointerException("user is null");
		}
		if (operation == null) {
			throw new NullPointerException("operation is null");
		}		
		if (documentSelector == null) {
			throw new NullPointerException("document selector is null");
		}
				
		try {
			if (documentSelector.isUserDocument()) {
				String xui = documentSelector.getDocumentParent().substring(6);
				// only the user is authorized to operate on it's directory 
				if (user.equalsIgnoreCase(xui)) {
					if (operation == Operation.PUT) {
						// check doc name if is set
						if (authorizedUserDocumentName != null) {
							return authorizedUserDocumentName.equals(documentSelector.getDocumentName());
						}
						else {
							return true;
						}
					}
					else {
						return true;
					}
				} else {
					return false;
				}
			}
			else {
				// /auid/global  or invalid dir, authorize operation only if is a get operation
				if(operation == Operation.GET) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("invalid document selector");
		}
		
	}
}
