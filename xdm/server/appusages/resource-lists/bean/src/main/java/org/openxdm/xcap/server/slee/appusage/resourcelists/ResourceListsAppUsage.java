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

package org.openxdm.xcap.server.slee.appusage.resourcelists;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.validation.Validator;

import org.mobicents.xdm.common.util.dom.DomUtils;
import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.openxdm.xcap.common.error.ConstraintFailureConflictException;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.error.NotAuthorizedRequestException;
import org.openxdm.xcap.common.error.UniquenessFailureConflictException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ResourceListsAppUsage extends AppUsage {

	public static final String ID = "resource-lists";
	public static final String DEFAULT_DOC_NAMESPACE = "urn:ietf:params:xml:ns:resource-lists";
	public static final String MIMETYPE = "application/resource-lists+xml";
	
	private static final String LIST_ELEMENT_NAME = "list";
	private static final String NAME_ATTRIBUTE_NAME = "name";
	private static final String NAME_ATTRIBUTE_REQUIRED_ERROR_PHRASE = "Name attribute is required.";
	
	private final boolean omaCompliant;

	/**
	 * 
	 * @param schemaValidator
	 * @param omaCompliant
	 */
	public ResourceListsAppUsage(Validator schemaValidator, boolean omaCompliant) {
		super(ID,DEFAULT_DOC_NAMESPACE,MIMETYPE,schemaValidator,"index");
		this.omaCompliant = omaCompliant;
	}	
	
	/**
	 * For extensions such as OMA Group List Usage
	 * @param auid
	 * @param defaultDocumentNamespace
	 * @param mimetype
	 * @param schemaValidator
	 * @param authorizedUserDocumentName
	 */
	public ResourceListsAppUsage(String auid, String defaultDocumentNamespace,
			String mimetype, Validator schemaValidator, String authorizedUserDocumentName, boolean omaCompliant) {
		super(auid,defaultDocumentNamespace,mimetype,schemaValidator,authorizedUserDocumentName);
		this.omaCompliant = omaCompliant;
	}
	
	public static void checkNodeResourceListConstraints(Node node, boolean omaCompliant) throws UniquenessFailureConflictException, ConstraintFailureConflictException {
		
		Set<String> nameSet = new HashSet<String>();
		Set<String> uriSet = new HashSet<String>();
		Set<String> refUriSet = new HashSet<String>();
		Set<String> anchorUriSet = new HashSet<String>();
		
		// get childs
		NodeList childNodes = node.getChildNodes();
		// process each one
		for(int i=0;i<childNodes.getLength();i++) {
			Node childNode = childNodes.item(i);			
			if (DomUtils.isElementNamed(childNode,LIST_ELEMENT_NAME)) {
				// list element
				Attr nameAttr = ((Element)childNode).getAttributeNode(NAME_ATTRIBUTE_NAME);
				if (nameAttr != null) {
					/*
					 o  The "name" attribute in a <list> element MUST be unique amongst
					 all other "name" attributes of <list> elements within the same
					 parent element.  Uniqueness is determined by case sensitive string
					 comparison.
					*/
					// name attr exists, it must be unique
					if (nameSet.contains(nameAttr.getNodeValue())) {
						// not unique, raise exception
						throw new UniquenessFailureConflictException();
					}
					else {
						// unique so far, add it to the name set
						nameSet.add(nameAttr.getNodeValue());
						// and process this list
						checkNodeResourceListConstraints(childNode,omaCompliant);
					}
				}
				else {
					if (omaCompliant) {
						// OMA requires that name attr is set
						throw new ConstraintFailureConflictException(NAME_ATTRIBUTE_REQUIRED_ERROR_PHRASE);
					}
				}
				
			}
			else if (DomUtils.isElementNamed(childNode,"entry")) {
				// entry element
				Attr uriAttr = ((Element)childNode).getAttributeNode("uri");
				/*
				 o  The "uri" attribute in a <entry> element MUST be unique amongst
				 all other "uri" attributes of <entry> elements within the same
				 parent element.  Uniqueness is determined by case sensitive string
				 comparison.
				 */
				// uri attr must be unique
				if (uriSet.contains(uriAttr.getNodeValue())) {
					// not unique, raise exception
					throw new UniquenessFailureConflictException();
				}
				else {
					// unique so far, add it to the uri set
					uriSet.add(uriAttr.getNodeValue());
				}							
			}
			else if (DomUtils.isElementNamed(childNode,"entry-ref")) {
				// entry-ref element
				Attr refUriAttr = ((Element)childNode).getAttributeNode("ref");
				/*
				 o  The URI in the "ref" attribute of the <entry-ref> element MUST be
				 unique amongst all other "ref" attributes of <entry-ref> elements
				 within the same parent element.  Uniqueness is determined by case
				 sensitive string comparison.  The value of the attribute MUST be a
				 relative path reference.  Note that the server is not responsible
				 for verifying that the reference resolves to an <entry> element in
				 a document within the same XCAP root.
				*/
				// ref attr must be unique
				if (refUriSet.contains(refUriAttr.getNodeValue())) {
					// not unique, raise exception
					throw new UniquenessFailureConflictException();
				}
				else {						
					// unique so far
					// check is relative ref
					try {
						URI uri = new URI(refUriAttr.getNodeValue());
						if(uri.isAbsolute()) {							
							throw new Exception();
						}
					}
					catch (Exception e) {						
						throw new ConstraintFailureConflictException("Bad URI in resource-list element >> "+refUriAttr.getNodeValue());
					}
					// add it to the ref uri set
					refUriSet.add(refUriAttr.getNodeValue());
				}				
			}
			else if (DomUtils.isElementNamed(childNode,"external")) {
				// external element
				Attr anchorUriAttr = ((Element)childNode).getAttributeNode("anchor");
				/*
				 o  The URI in the "anchor" attribute of the <external> element MUST
				 be unique amongst all other "anchor" attributes of <external>
				 elements within the same parent element.  Uniqueness is determined
				 by case sensitive string comparison.  The value of the attribute
				 MUST be an absolute HTTP URI.  Note that the server is not
				 responsible for verifying that the URI resolves to a <list>
				 element in a document.  Indeed, since the URI may reference a
				 server in another domain, referential integrity cannot be
				 guaranteed without adding substantial complexity to the system.		 
				 */
				// anchor attr must be unique
				if (anchorUriSet.contains(anchorUriAttr.getNodeValue())) {
					// not unique, raise exception
					throw new UniquenessFailureConflictException();
				}
				else {						
					// unique so far
					// check is absolute http uri
					try {
						URI uri = new URI(anchorUriAttr.getNodeValue());
						if(uri == null || (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https"))) {							
							throw new Exception();
						}
					}
					catch (Exception e) {						
						throw new ConstraintFailureConflictException("Bad URI in resource-list element >> "+anchorUriAttr.getNodeValue());
					}
					// add it to the anchor uri set
					anchorUriSet.add(anchorUriAttr.getNodeValue());
				}							
			}
		}		
	}

	@Override
	public void checkConstraintsOnPut(Document document, String xcapRoot,
			DocumentSelector documentSelector, AppUsageDataSource dataSource)
			throws UniquenessFailureConflictException,
			InternalServerErrorException, ConstraintFailureConflictException,
			NotAuthorizedRequestException {

		super.checkConstraintsOnPut(document, xcapRoot, documentSelector,
				dataSource);
		// check this app usage constraints below the root resource-lists node
		checkNodeResourceListConstraints(document.getDocumentElement(),
				omaCompliant);
	}

}
