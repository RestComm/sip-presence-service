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

package org.mobicents.xdm.server.appusage.oma.prescontent;

import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.mobicents.xdm.server.appusage.AuthorizationPolicy;
import org.openxdm.xcap.common.uri.DocumentSelector;

public class OMAPresContentAuthorizationPolicy implements AuthorizationPolicy {

	private final static String FOLDER_PREFIX = "oma_";
	
	private final String presRulesAUID;
	private final String presRulesDocumentName;
	
	public OMAPresContentAuthorizationPolicy(String presRulesAUID, String presRulesDocumentName) {
		this.presRulesAUID = presRulesAUID;
		this.presRulesDocumentName = presRulesDocumentName;
	}
	
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
		
		String xui = documentSelector.getUser();
		if (!user.equals(xui)) {
			if (operation != Operation.GET) {
				// other user may only read
				return false;
			}
			// if doc is in home dir, i.e., not in a subfolder, then it is auth
			String[] documentNameParts = documentSelector.getDocumentName().split("/");
			if (documentNameParts.length == 1) {
				return true;
			}
			else if (documentNameParts.length > 2) {
				return false;						
			}
			final String folder = documentNameParts[0];
			final String docName = documentNameParts[1];
			// check if has permissions in xui pres-rules
			if (this.presRulesAUID == null || this.presRulesDocumentName == null) {
				// unable to build pres-rules doc selector, consider that users may get anything
				return true;
			}
			// FIXME must check if pres-rules allows subscriptions to particular data, which is identified from document selector (folder name)
			// extract code in presence server that deals with pres-rules
			/*
			try {
				Document document = dataSource.getDocument(new DocumentSelector(this.presRulesAUID, "users/"+xui, this.presRulesDocumentName));
				if (document == null) {
					// no pres rules, assume every user is authorized
					return true;
				}
							
			} catch (InternalServerErrorException e) {
				return false;
			}	*/
			return true;	

		}
		else {
			if (operation == Operation.PUT) {
				// check doc name and "folder"
				String[] documentNameParts = documentSelector.getDocumentName().split("/");
				if (documentNameParts.length == 1) {
					// just doc name
					if (documentNameParts[0].startsWith(FOLDER_PREFIX)) {
						return false;
					}
				}
				else if (documentNameParts.length == 2) {
					// folder / doc name
					if (!documentNameParts[0].startsWith(FOLDER_PREFIX)) {
						return false;
					}
					if (documentNameParts[1].startsWith(FOLDER_PREFIX)) {
						return false;
					}						
				}
				else {
					return false;
				}
				return true;					
			}
			else {
				// user principal may get and delete without any constraint
				return true;
			}
		}
		
	}

}
