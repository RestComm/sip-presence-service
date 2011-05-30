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
package org.mobicents.xdm.server.appusage.xcapcaps;

import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageManagement;
import org.mobicents.xdm.server.appusage.AppUsagePool;
import org.mobicents.xdm.server.appusage.InterceptedDocument;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A XDM data source interceptor to build XCAP Caps global/index doc on demand.
 * 
 * @author martins
 * 
 */
public class XCAPCapsAppUsageDataSourceInterceptor implements
		AppUsageDataSourceInterceptor {

	private static final DocumentSelector GLOBAL_INDEX_DOCUMENT_SELECTOR = new DocumentSelector(
			"xcap-caps/global", "index");
	private static final InterceptedDocument[] NO_DOCS_RESULT = {};

	private final DocumentBuilderFactory documentBuilderFactory;
	private final AppUsageManagement appUsageManagement;

	public XCAPCapsAppUsageDataSourceInterceptor() {
		this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
		this.documentBuilderFactory.setNamespaceAware(true);
		this.appUsageManagement = AppUsageManagement.getInstance();
	}

	@Override
	public InterceptedDocument getDocument(DocumentSelector documentSelector,
			AppUsageDataSource dataSource) throws InternalServerErrorException {

		// global/index doc is the only one which exists in the app usage
		if (!documentSelector.equals(GLOBAL_INDEX_DOCUMENT_SELECTOR)) {
			return null;
		}

		// create doc
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new InternalServerErrorException(e.getMessage(), e);
		}
		Document domDocument = documentBuilder.newDocument();

		// create root element
		Element rootElement = domDocument.createElementNS(
				XCAPCapsAppUsage.DEFAULT_DOC_NAMESPACE, "xcap-caps");
		domDocument.appendChild(rootElement);

		// add auids
		Element auidsElement = domDocument.createElement("auids");
		rootElement.appendChild(auidsElement);

		// add extensions
		rootElement.appendChild(domDocument.createElement("extensions"));

		// add namespaces
		Element namespacesElement = domDocument.createElement("namespaces");
		rootElement.appendChild(namespacesElement);

		// fill auids and namespaces
		AppUsagePool appUsagePool = null;
		AppUsage appUsage = null;
		Element element = null;
		Set<String> namespaces = new HashSet<String>();
		for (String auid : appUsageManagement.getAppUsages()) {
			appUsagePool = appUsageManagement.getAppUsagePool(auid);
			if (appUsagePool != null) {
				// borrow one app usage object
				appUsage = appUsagePool.borrowInstance();
				// add auid
				element = domDocument.createElement("auid");
				element.setNodeValue(auid);
				auidsElement.appendChild(element);
				// add namespace if not in a previous app usage
				if (namespaces.add(appUsage.getDefaultDocumentNamespace())) {
					element = domDocument.createElement("namespace");
					element.setNodeValue(appUsage.getDefaultDocumentNamespace());
					namespacesElement.appendChild(element);
				}
				// release app usage object
				appUsagePool.returnInstance(appUsage);
			}
		}

		return new InterceptedDocument(documentSelector, domDocument);
	}

	@Override
	public InterceptedDocument[] getDocuments(String documentParent,
			boolean includeChildCollections, AppUsageDataSource dataSource)
			throws InternalServerErrorException {
		// not supported
		return NO_DOCS_RESULT;
	}

	@Override
	public InterceptedDocument[] getDocuments(boolean includeChildCollections,
			AppUsageDataSource dataSource) throws InternalServerErrorException {
		// not supported
		return NO_DOCS_RESULT;
	}

}
