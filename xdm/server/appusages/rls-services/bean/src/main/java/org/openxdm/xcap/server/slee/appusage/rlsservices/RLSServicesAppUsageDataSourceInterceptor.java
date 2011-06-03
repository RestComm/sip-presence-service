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

package org.openxdm.xcap.server.slee.appusage.rlsservices;

import javax.xml.parsers.ParserConfigurationException;

import org.mobicents.xdm.common.util.dom.DomUtils;
import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.InterceptedDocument;
import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author martins
 *
 */
public class RLSServicesAppUsageDataSourceInterceptor implements
		AppUsageDataSourceInterceptor {

	public final static DocumentSelector GLOBAL_DOCUMENT_SELECTOR = new DocumentSelector(
			RLSServicesAppUsage.ID + "/global", "index");

	@Override
	public boolean interceptsCollection(String collection,
			boolean includeChildCollections) {
		// no point in providing the global doc in a collection
		return false;
	}

	@Override
	public boolean interceptsDocument(DocumentSelector documentSelector) {
		return documentSelector.equals(GLOBAL_DOCUMENT_SELECTOR);
	}

	@Override
	public InterceptedDocument getDocument(DocumentSelector documentSelector,
			AppUsageDataSource dataSource) throws InternalServerErrorException {

		// create dom doc and root element
		org.w3c.dom.Document domDocument = null;
		try {
			domDocument = DomUtils.DOCUMENT_BUILDER_NS_AWARE_FACTORY
					.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			throw new InternalServerErrorException("failed to created dom doc",
					e);
		}
		Element rlsServices = domDocument.createElementNS(
				RLSServicesAppUsage.DEFAULT_DOC_NAMESPACE, "rls-services");
		domDocument.appendChild(rlsServices);
		// fetch all user docs
		DocumentSelector ds = null;
		NodeList nodeList = null;
		Node node = null;
		for (Document document : dataSource.getDocuments("rls-services/users",
				true)) {
			ds = new DocumentSelector(document.getCollection(),
					document.getDocumentName());
			if (RLSServicesAppUsage.isUserIndexDoc(ds)) {
				// only process index docs at the user dir collection
				// get all 2 level elements
				nodeList = document.getAsDOMDocument().getDocumentElement()
						.getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					node = nodeList.item(i);
					if (DomUtils.isElementNamed(node, "service")) {
						// import and append to result dom doc
						rlsServices.appendChild(domDocument.importNode(node,
								true));
					}
				}
			}
		}
		return new InterceptedDocument(GLOBAL_DOCUMENT_SELECTOR, domDocument);
	}

	@Override
	public InterceptedDocument[] getDocuments(String collection,
			boolean includeChildCollections, AppUsageDataSource dataSource)
			throws InternalServerErrorException {
		throw new UnsupportedOperationException();
	}

}
