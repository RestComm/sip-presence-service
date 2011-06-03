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

package org.mobicents.xdm.server.appusage.oma.groupusagelist;

import javax.xml.validation.Validator;

import org.mobicents.xdm.common.util.dom.DomUtils;
import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.openxdm.xcap.common.error.ConstraintFailureConflictException;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.error.NotAuthorizedRequestException;
import org.openxdm.xcap.common.error.UniquenessFailureConflictException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.server.slee.appusage.resourcelists.ResourceListsAppUsage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * OMA XDM 1.1 Group Usage List XCAP App Usage.
 * @author martins
 *
 */
public class OMAGroupUsageListAppUsage extends ResourceListsAppUsage {

	public static final String ID = "org.openmobilealliance.group-usage-list";
	public static final String DEFAULT_DOC_NAMESPACE = "urn:ietf:params:xml:ns:resource-lists";
	public static final String MIMETYPE = "application/vnd.oma.group-usage-list+xml";
	
	private static final String LIST_ELEMENT_NAME = "list";
	private static final String EXTERNAL_ELEMENT_NAME = "external";
	private static final String ENTRY_REF_ELEMENT_NAME = "entry-ref";
	private static final String NOT_ALLOWED_ERROR_PHRASE = "Not allowed";
	
	/**
	 * 
	 * @param schemaValidator
	 */
	public OMAGroupUsageListAppUsage(Validator schemaValidator) {
		super(ID,DEFAULT_DOC_NAMESPACE,MIMETYPE,schemaValidator,"index",true);
	}
	
	@Override
	public void checkConstraintsOnPut(Document document, String xcapRoot,
			DocumentSelector documentSelector, AppUsageDataSource dataSource)
			throws UniquenessFailureConflictException,
			InternalServerErrorException, ConstraintFailureConflictException,
			NotAuthorizedRequestException {
		super.checkConstraintsOnPut(document, xcapRoot, documentSelector, dataSource);
		
		/*
		 * In addition to the XML schema, the additional validation constraints
		 * on a Group Usage List SHALL conform to those described in [RFC4826]
		 * Section 3.4.5, with the following clarifications: The �name�
		 * attribute of the <list> element SHALL be present. If this constraint
		 * is violated, an HTTP �409 Conflict� response SHALL be returned with
		 * the error condition identified by the <constraint-failure> element.
		 * If included, the �phrase� attribute of this element SHOULD be set to
		 * �Name attribute is required.� If the XDMC uses or adds an <entry-ref>
		 * or an <external> child element (specified in [RFC4826]) to the <list>
		 * element, the Shared XDMS SHALL return an error code �409 Conflict�
		 * response which includes the XCAP error element <constraint- failure>.
		 * If included, the "phrase" attribute SHOULD be set to "Not allowed".
		 */
		Element resourceLists = document.getDocumentElement();
		NodeList resourceListsChildNodeList = resourceLists.getChildNodes();
		for (int i=0;i<resourceListsChildNodeList.getLength();i++) {
			Node resourceListsChildNode = resourceListsChildNodeList.item(i);
			if (DomUtils.isElementNamed(resourceListsChildNode,LIST_ELEMENT_NAME)) {
				// note: name constraint checked by super()
				NodeList listChildNodeList = resourceListsChildNode.getChildNodes();
				for (int j=0;j<listChildNodeList.getLength();j++) {
					Node listChildNode = listChildNodeList.item(j);
					if (listChildNode.getNodeType() == Node.ELEMENT_NODE) {
						if (DomUtils.getElementName(listChildNode).equals(ENTRY_REF_ELEMENT_NAME)) {
							throw new ConstraintFailureConflictException(NOT_ALLOWED_ERROR_PHRASE);
						}
						else if (DomUtils.getElementName(listChildNode).equals(EXTERNAL_ELEMENT_NAME)) {
							throw new ConstraintFailureConflictException(NOT_ALLOWED_ERROR_PHRASE);
						}
					}
				}
			}
		}
		
	}
	
}
