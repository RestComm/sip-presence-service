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

package org.mobicents.xdm.server.appusage.oma.lockeduserprofile;

import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.mobicents.xdm.server.appusage.AuthorizationPolicy;
import org.openxdm.xcap.common.uri.DocumentSelector;

public class OMALockedUserProfileAuthorizationPolicy implements
		AuthorizationPolicy {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AuthorizationPolicy#isAuthorized(java
	 * .lang.String,
	 * org.mobicents.xdm.server.appusage.AuthorizationPolicy.Operation,
	 * org.openxdm.xcap.common.uri.DocumentSelector,
	 * org.mobicents.xdm.server.appusage.AppUsageDataSource)
	 */
	@Override
	public boolean isAuthorized(String user, Operation operation,
			DocumentSelector documentSelector, AppUsageDataSource dataSource)
			throws NullPointerException {
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
		String xui = documentSelector.getUser();
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
