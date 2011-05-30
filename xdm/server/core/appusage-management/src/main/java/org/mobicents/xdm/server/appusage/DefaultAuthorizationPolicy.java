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

package org.mobicents.xdm.server.appusage;

import org.apache.log4j.Logger;
import org.openxdm.xcap.common.uri.DocumentSelector;

/**
 * This XCAP Authorization Policy implements the Default Authorization Policy.
 * 
 * By XCAP Specs:
 * 
 * "By default, each user is able to access (read, modify, and delete) all of
 * the documents below their home directory, and any user is able to read
 * documents within the global directory. However, only trusted users,
 * explicitly provisioned into the server, can modify global documents."
 * 
 * @author Eduardo Martins
 * 
 */
public class DefaultAuthorizationPolicy implements AuthorizationPolicy {

	private static final Logger LOGGER = Logger
			.getLogger(DefaultAuthorizationPolicy.class);

	private final String authorizedUserDocumentName;

	public DefaultAuthorizationPolicy(String authorizedUserDocumentName) {
		this.authorizedUserDocumentName = authorizedUserDocumentName;
	}

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
	public boolean isAuthorized(String user,
			AuthorizationPolicy.Operation operation,
			DocumentSelector documentSelector, AppUsageDataSource dataSource)
			throws NullPointerException {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("checking " + user + " is authorized to " + operation
					+ " for " + documentSelector);
		}

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
				String xui = documentSelector.getUser();
				// only the user is authorized to operate on it's directory
				if (user.equalsIgnoreCase(xui)) {
					if (operation == Operation.PUT) {
						// check doc name if is set
						if (authorizedUserDocumentName != null) {
							return authorizedUserDocumentName
									.equals(documentSelector.getDocumentName());
						} else {
							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug("request authorized");
							}
							return true;
						}
					} else {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("request authorized");
						}
						return true;
					}
				} else {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("request not authorized, user "
								+ user
								+ " may not interact with documents not under user "
								+ xui + " directory.");
					}
					return false;
				}
			} else {
				// /auid/global or invalid dir, authorize operation only if is a
				// get operation
				if (operation == Operation.GET) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("non user document retrieval authorized");
					}
					return true;
				} else {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("non user document modification not authorized");
					}
					return false;
				}
			}
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("invalid document selector");
		}

	}
}
