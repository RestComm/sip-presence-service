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

package org.mobicents.xdm.server.appusage.oma.xcapdirectory;

import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.mobicents.slee.xdm.server.ServerConfiguration;
import org.mobicents.xdm.common.util.dom.DomUtils;
import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageManagement;
import org.mobicents.xdm.server.appusage.InterceptedDocument;
import org.mobicents.xdm.server.appusage.oma.xcapdirectory.uri.UriBuilder;
import org.mobicents.xdm.server.appusage.oma.xcapdirectory.uri.UriComponentEncoder;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author martins
 * 
 */
public class XcapDirectoryAppUsageDataSourceInterceptor implements
		AppUsageDataSourceInterceptor {

	private static final Logger logger = Logger
			.getLogger(XcapDirectoryAppUsage.class);

	public static final String DIRECTORY_DOCUMENT_NAME = "directory.xml";
	private static final String ETAG_ATTRIBUTE_NAME = "etag";
	private static final String ROOT_DIRECTORY_ELEMENT_NAME = "xcap-directory";
	private static final String FOLDER_ELEMENT_NAME = "folder";
	private static final String AUID_ATTR_NAME = "auid";
	private static final String ENTRY_ELEMENT_NAME = "entry";
	private static final String URI_ATTR_NAME = "uri";

	private static final ServerConfiguration XDM_SERVER_CONFIGURATION = ServerConfiguration
			.getInstance();

	@Override
	public boolean interceptsCollection(String collection,
			boolean includeChildCollections) {
		// intercepts all collections
		return true;
	}

	@Override
	public boolean interceptsDocument(DocumentSelector documentSelector) {
		// intercepts all docs
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor#getDocument
	 * (org.openxdm.xcap.common.uri.DocumentSelector,
	 * org.mobicents.xdm.server.appusage.AppUsageDataSource)
	 */
	@Override
	public InterceptedDocument getDocument(DocumentSelector documentSelector,
			AppUsageDataSource dataSource) throws InternalServerErrorException {
		if (logger.isDebugEnabled()) {
			logger.debug("building xcap directory doc " + documentSelector
					+ " on request");
		}

		Document document = null;
		try {
			document = DomUtils.DOCUMENT_BUILDER_NS_AWARE_FACTORY
					.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			throw new InternalServerErrorException(e.getMessage(), e);
		}
		Element rootElement = document.createElementNS(
				XcapDirectoryAppUsage.DEFAULT_DOC_NAMESPACE,
				ROOT_DIRECTORY_ELEMENT_NAME);
		document.appendChild(rootElement);
		// lets prepare the collection suffix by removing the auid from doc
		// selector
		int i = documentSelector.getCollection().indexOf('/', 1);
		String collectionSuffix = null;
		if (i > 0) {
			collectionSuffix = documentSelector.getCollection().substring(i);
		} else {
			collectionSuffix = "";
		}
		for (String appUsageId : AppUsageManagement.getInstance()
				.getAppUsages()) {
			if (!appUsageId.equals(XcapDirectoryAppUsage.ID)) {
				Element folderElement = document.createElementNS(
						XcapDirectoryAppUsage.DEFAULT_DOC_NAMESPACE,
						FOLDER_ELEMENT_NAME);
				rootElement.appendChild(folderElement);
				folderElement.setAttributeNS(null, AUID_ATTR_NAME, appUsageId);
				for (org.openxdm.xcap.common.datasource.Document storedDoc : dataSource
						.getDocuments(appUsageId + collectionSuffix, false)) {
					Element entryElement = document.createElementNS(
							XcapDirectoryAppUsage.DEFAULT_DOC_NAMESPACE,
							ENTRY_ELEMENT_NAME);
					folderElement.appendChild(entryElement);
					entryElement.setAttributeNS(
							null,
							URI_ATTR_NAME,
							getDocumentURI(new DocumentSelector(storedDoc
									.getCollection(), storedDoc
									.getDocumentName())));
					entryElement.setAttributeNS(null, ETAG_ATTRIBUTE_NAME,
							storedDoc.getETag());
				}
			}
		}
		return new InterceptedDocument(documentSelector, document);
	}

	private String getDocumentURI(DocumentSelector documentSelector)
			throws InternalServerErrorException {
		UriBuilder uriBuilder = new UriBuilder();
		uriBuilder.setDocumentSelector(UriComponentEncoder
				.encodePath(documentSelector.toString()));
		uriBuilder.setXcapRoot(XDM_SERVER_CONFIGURATION.getXcapRoot());
		uriBuilder.setSchemeAndAuthority(XDM_SERVER_CONFIGURATION
				.getSchemeAndAuthority());
		try {
			return uriBuilder.toURI().toString();
		} catch (URISyntaxException e) {
			throw new InternalServerErrorException(e.getMessage(), e);
		}
	}

	private static final InterceptedDocument[] EMPTY_RESULT = {};

	@Override
	public InterceptedDocument[] getDocuments(String documentParent,
			boolean includeChildCollections, AppUsageDataSource dataSource)
			throws InternalServerErrorException {
		// not supported
		return EMPTY_RESULT;
	}
}
