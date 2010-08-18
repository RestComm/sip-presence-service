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

	private static final String PATH_SEPARATOR = "/";
	private static final String USERS = "users";
	private static final String GLOBAL = "global";
	
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
			// split document parent
			final String[] documentParentParts = documentSelector.getDocumentParent().split(PATH_SEPARATOR);
			// part 0 is the auid child directory, global or index	
			if (documentParentParts[0].equalsIgnoreCase(USERS)) {
				// /auid/users directory, get its child, the user directory 
				final String userDirectory = documentParentParts[1];
				// only the user is authorized to operate on it's directory 
				if (user.equalsIgnoreCase(userDirectory)) {
					return true;
				} else {
					return false;
				}
			} else if (documentParentParts[0].equalsIgnoreCase(GLOBAL)) {
				// /auid/global dir, authorize operation only if is a get operation
				if(operation.equals(AuthorizationPolicy.Operation.GET)) {
					return true;
				}
				else {
					return false;
				}
			} else {
				return false;
			}
		}
		catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("invalid document selector");
		}
		
	}
}
