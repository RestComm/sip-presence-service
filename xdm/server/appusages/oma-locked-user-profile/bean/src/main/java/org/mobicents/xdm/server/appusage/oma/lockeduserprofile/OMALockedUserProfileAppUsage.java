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

import javax.xml.validation.Validator;

import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.mobicents.xdm.server.appusage.oma.userprofile.OMAUserProfileAppUsage;
import org.openxdm.xcap.common.error.ConstraintFailureConflictException;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.error.UniquenessFailureConflictException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.w3c.dom.Document;
/**
 * OMA XDM 2.0 Locked User Profile XCAP App Usage.
 * @author martins
 *
 */
public class OMALockedUserProfileAppUsage extends OMAUserProfileAppUsage {

	public static final String ID = "org.openmobilealliance.locked-user-profile";
		
	/**
	 * 
	 * @param schemaValidator
	 */
	public OMALockedUserProfileAppUsage(Validator schemaValidator) {
		super(ID,schemaValidator,new OMALockedUserProfileAuthorizationPolicy());
	}
	
	@Override
	public void checkConstraintsOnPut(Document document, String xcapRoot,
			DocumentSelector documentSelector, AppUsageDataSource dataSource)
			throws UniquenessFailureConflictException,
			InternalServerErrorException, ConstraintFailureConflictException {
		// avoid constraints from super
	}
	
}
