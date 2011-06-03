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

import java.util.Set;

import javax.xml.validation.Validator;

import org.mobicents.xdm.common.util.dom.DomUtils;
import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.openxdm.xcap.common.error.ConstraintFailureConflictException;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.error.UniquenessFailureConflictException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * OMA XDM 2.0 Pres Content XCAP App Usage.
 * 
 * @author martins
 * 
 */
public class OMAPresContentAppUsage extends AppUsage {

	public static final String ID = "org.openmobilealliance.pres-content";
	public static final String DEFAULT_DOC_NAMESPACE = "urn:oma:xml:prs:pres-content";
	public static final String MIMETYPE = "application/vnd.oma.pres-content+xml";

	private static final String MIMETYPE_ELEMENT_NAME = "mime-type";
	private static final String ENCODING_ELEMENT_NAME = "encoding";
	private static final String DATA_ELEMENT_NAME = "data";
	private static final String MIMETYPE_ERROR_PHRASE = "Unsupported MIME type";
	private static final String ENCODING_ERROR_PHRASE = "Unsupported encoding";
	private static final String MAX_DATA_SIZE_ERROR_PHRASE_PREFIX = "Size limit exceeded, maximum allowed size is ";
	private static final String MAX_DATA_SIZE_ERROR_PHRASE_SUFFIX = " bytes";

	private final String MAX_DATA_SIZE_ERROR_PHRASE;

	private final Set<String> encodingsAllowed;
	private final int maxDataSize;
	private final Set<String> mimetypesAllowed;

	/**
	 * 
	 * @param schemaValidator
	 */
	public OMAPresContentAppUsage(Validator schemaValidator,
			Set<String> encodingsAllowed, int maxDataSize,
			Set<String> mimetypesAllowed, String presRulesAUID,
			String presRulesDocumentName) {
		super(ID, DEFAULT_DOC_NAMESPACE, MIMETYPE, schemaValidator,
				new OMAPresContentAuthorizationPolicy(presRulesAUID,
						presRulesDocumentName));
		this.encodingsAllowed = encodingsAllowed;
		this.maxDataSize = maxDataSize;
		if (maxDataSize != 0) {
			this.MAX_DATA_SIZE_ERROR_PHRASE = new StringBuilder(
					MAX_DATA_SIZE_ERROR_PHRASE_PREFIX)
					.append(Integer.toString(maxDataSize))
					.append(MAX_DATA_SIZE_ERROR_PHRASE_SUFFIX).toString();
		} else {
			this.MAX_DATA_SIZE_ERROR_PHRASE = null;
		}
		this.mimetypesAllowed = mimetypesAllowed;
	}

	@Override
	public void checkConstraintsOnPut(Document document, String xcapRoot,
			DocumentSelector documentSelector, AppUsageDataSource dataSource)
			throws UniquenessFailureConflictException,
			InternalServerErrorException, ConstraintFailureConflictException {

		boolean mimytypeFound = mimetypesAllowed == null
				|| mimetypesAllowed.isEmpty();
		boolean encodingFound = encodingsAllowed == null
				|| encodingsAllowed.isEmpty();
		boolean dataFound = maxDataSize == 0;
		
		if (!mimytypeFound || !encodingFound || !dataFound) {
			Element content = document.getDocumentElement();
			NodeList contentChildNodeList = content.getChildNodes();
			for (int i = 0; i < contentChildNodeList.getLength(); i++) {
				Node contentChildNode = contentChildNodeList.item(i);
				if (!mimytypeFound
						&& DomUtils.isElementNamed(contentChildNode,
								MIMETYPE_ELEMENT_NAME)) {
					Element mimeTypeElement = (Element) contentChildNode;
					if (mimeTypeElement.getTextContent() == null
							|| !mimetypesAllowed.contains(mimeTypeElement.getTextContent())) {
						throw new ConstraintFailureConflictException(
								MIMETYPE_ERROR_PHRASE);
					}
					mimytypeFound = true;
					if (encodingFound && dataFound) {
						return;
					}
				} else if (!encodingFound
						&& DomUtils.isElementNamed(contentChildNode,
								ENCODING_ELEMENT_NAME)) {
					Element encodingElement = (Element) contentChildNode;
					if (encodingElement.getTextContent() == null
							|| !encodingsAllowed.contains(encodingElement.getTextContent())) {
						throw new ConstraintFailureConflictException(
								ENCODING_ERROR_PHRASE);
					}
					encodingFound = true;
					if (mimytypeFound && dataFound) {
						return;
					}
				} else if (!dataFound
						&& DomUtils.isElementNamed(contentChildNode,
								DATA_ELEMENT_NAME)) {
					// ensure data size is not higher than max size
					Element dataElement = (Element) contentChildNode;
					if (dataElement.getTextContent() != null) {
						int dataSize = dataElement.getTextContent().getBytes().length;
						if (dataSize > maxDataSize) {
							throw new ConstraintFailureConflictException(
									MAX_DATA_SIZE_ERROR_PHRASE);
						}
					}
					dataFound = true;
					if (mimytypeFound && encodingFound) {
						return;
					}
				}
			}
		}
	}

}
