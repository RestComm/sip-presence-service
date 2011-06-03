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

import javax.xml.validation.Validator;

import org.mobicents.xdm.common.util.dom.DomUtils;
import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.mobicents.xdm.server.appusage.AuthorizationPolicy;
import org.openxdm.xcap.common.error.ConstraintFailureConflictException;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.error.UniquenessFailureConflictException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * OMA XDM 2.0 User Profile XCAP App Usage.
 * @author martins
 *
 */
public class OMAUserProfileAppUsage extends AppUsage {

	public static final String ID = "org.openmobilealliance.user-profile";
	public static final String DEFAULT_DOC_NAMESPACE = "urn:oma:xml:xdm:user-profile";
	public static final String MIMETYPE = "application/vnd.oma.user-profile+xml";
	
	private static final String USER_PROFILE_ELEMENT_NAME = "user-profile";
	private static final String URI_ATTRIBUTE_NAME = "uri";
	private static final String ERROR_PHRASE = "Wrong User Profile URI";
	
	/**
	 * 
	 * @param schemaValidator
	 */
	public OMAUserProfileAppUsage(Validator schemaValidator) {
		this(ID,schemaValidator,new OMAUserProfileAuthorizationPolicy());
	}
	
	protected OMAUserProfileAppUsage(String auid,Validator schemaValidator,
			AuthorizationPolicy authorizationPolicy) {
		super(auid,DEFAULT_DOC_NAMESPACE,MIMETYPE,schemaValidator,authorizationPolicy);
	}
	
	@Override
	public void checkConstraintsOnPut(Document document, String xcapRoot,
			DocumentSelector documentSelector, AppUsageDataSource dataSource)
			throws UniquenessFailureConflictException,
			InternalServerErrorException, ConstraintFailureConflictException {

		if (!documentSelector.isUserDocument()) {
			return;
		}
		
		String xui = documentSelector.getUser();
		
		/*
		 * The value of the �uri� attribute of the <user-profile> element SHALL
		 * be the same as the XUI value of the Document URI for the User Profile
		 * document. If not, the XDMS SHALL return an HTTP �409 Conflict�
		 * response as described in [RFC4825], including the
		 * <constraint-failure> error element. If included, the �phrase�
		 * attribute SHOULD be set to �Wrong User Profile URI�.
		 */
		Element userProfiles = document.getDocumentElement();
		NodeList userProfilesChildNodeList = userProfiles.getChildNodes();
		for (int i=0;i<userProfilesChildNodeList.getLength();i++) {
			Node userProfilesChildNode = userProfilesChildNodeList.item(i);
			if (DomUtils.isElementNamed(userProfilesChildNode,USER_PROFILE_ELEMENT_NAME)) {
				Element userProfileElement = (Element) userProfilesChildNode;
				String userProfileUri = userProfileElement.getAttributeNode(URI_ATTRIBUTE_NAME).getNodeValue();
				if (!xui.equals(userProfileUri)) {
					throw new ConstraintFailureConflictException(ERROR_PHRASE);
				}
			}
		}
	}
	
}
