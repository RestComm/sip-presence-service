/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.openxdm.xcap.server.slee.appusage.rlsservices;

import org.mobicents.xdm.server.appusage.AppUsageDataSource;
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

public class RLSServicesAuthorizationPolicy implements AuthorizationPolicy {

	private final static String authorizedUserDocumentName = "index";
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.xdm.server.appusage.AuthorizationPolicy#isAuthorized(java.lang.String, org.mobicents.xdm.server.appusage.AuthorizationPolicy.Operation, org.openxdm.xcap.common.uri.DocumentSelector, org.mobicents.xdm.server.appusage.AppUsageDataSource)
	 */
	@Override
	public boolean isAuthorized(String user, AuthorizationPolicy.Operation operation, DocumentSelector documentSelector, AppUsageDataSource dataSource) throws NullPointerException {
		if (documentSelector.isUserDocument()) {
			if (user.equalsIgnoreCase(documentSelector.getUser())) {
				if (operation == Operation.PUT && !documentSelector.getDocumentName().equals(authorizedUserDocumentName)) {
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
