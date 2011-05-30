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

package org.mobicents.xdm.server.appusage.oma.userprofile;

import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.mobicents.xdm.server.appusage.AuthorizationPolicy;
import org.openxdm.xcap.common.uri.DocumentSelector;

public class OMAUserProfileAuthorizationPolicy implements AuthorizationPolicy {

	private final static String ALLOWED_DOC_NAME = "user-profile";
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.xdm.server.appusage.AuthorizationPolicy#isAuthorized(java.lang.String, org.mobicents.xdm.server.appusage.AuthorizationPolicy.Operation, org.openxdm.xcap.common.uri.DocumentSelector, org.mobicents.xdm.server.appusage.AppUsageDataSource)
	 */
	@Override
	public boolean isAuthorized(String user, Operation operation,
			DocumentSelector documentSelector, AppUsageDataSource dataSource) throws NullPointerException {
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
			String xui = documentSelector.getUser();
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
