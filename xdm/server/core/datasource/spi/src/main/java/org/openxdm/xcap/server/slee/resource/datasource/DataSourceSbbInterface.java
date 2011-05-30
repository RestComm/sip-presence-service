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

package org.openxdm.xcap.server.slee.resource.datasource;

import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.AttributeSelector;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.NodeSelector;
import org.w3c.dom.Element;

/**
 * Sbb Interface implementation for a DataSource Resource Adaptor. TODO complete
 * javadoc
 * 
 * @author Eduardo Martins
 * 
 */
public class DataSourceSbbInterface {

	private DataSourceResourceAdaptor ra;

	public DataSourceSbbInterface(DataSourceResourceAdaptor ra) {
		this.ra = ra;
	}

	public void createDocument(DocumentSelector documentSelector,
			String defaultDocNamespace, org.w3c.dom.Document newDocumentDOM,
			String newDocumentString, String newETag)
			throws InternalServerErrorException {
		// insert in data source
		ra.getDataSource().createDocument(documentSelector, newETag,
				newDocumentString);
		// fire event
		ra.postDocumentUpdatedEvent(new DocumentUpdatedEvent(documentSelector,
				defaultDocNamespace, null, newDocumentDOM, newDocumentString,
				newETag));
	}

	public void deleteDocument(DocumentSelector documentSelector,
			String defaultDocNamespace,
			org.openxdm.xcap.common.datasource.Document oldDocument)
			throws InternalServerErrorException {
		// delete in data source
		ra.getDataSource().deleteDocument(documentSelector);
		// fire event
		ra.postDocumentUpdatedEvent(new DocumentUpdatedEvent(documentSelector,
				defaultDocNamespace, oldDocument, null, null, null));
	}

	/**
	 * 
	 * @param collection
	 * @param includeChildCollections
	 * @return
	 * @throws InternalServerErrorException
	 */
	public Document[] getDocuments(String collection, boolean includeChildCollections) throws InternalServerErrorException {
		return ra.getDataSource().getDocuments(collection, includeChildCollections);
	}
	
	public Document getDocument(DocumentSelector documentSelector)
			throws InternalServerErrorException {
		return ra.getDataSource().getDocument(documentSelector);
	}

	public void updateAttribute(DocumentSelector documentSelector,
			String defaultDocNamespace,
			org.openxdm.xcap.common.datasource.Document oldDocument,
			org.w3c.dom.Document newDocumentDOM, String newDocumentString,
			String newETag, NodeSelector nodeSelector,
			AttributeSelector attributeSelector, String oldAttributeValue,
			String newAttributeValue) throws InternalServerErrorException {
		// update doc in data source
		ra.getDataSource().updateDocument(documentSelector, newETag,
				newDocumentString);
		// fire event
		ra.postAttributeUpdatedEvent(new AttributeUpdatedEvent(
				documentSelector, defaultDocNamespace, oldDocument,
				newDocumentDOM, newDocumentString, newETag, nodeSelector,
				attributeSelector, oldAttributeValue, newAttributeValue));
	}

	public void updateDocument(DocumentSelector documentSelector,
			String defaultDocNamespace,
			org.openxdm.xcap.common.datasource.Document oldDocument,
			org.w3c.dom.Document newDocumentDOM, String newDocumentString,
			String newETag) throws InternalServerErrorException {
		// update doc in data source
		ra.getDataSource().updateDocument(documentSelector, newETag,
				newDocumentString);
		// fire event
		ra.postDocumentUpdatedEvent(new DocumentUpdatedEvent(documentSelector,
				defaultDocNamespace, oldDocument, newDocumentDOM,
				newDocumentString, newETag));
	}

	public void updateElement(DocumentSelector documentSelector,
			String defaultDocNamespace,
			org.openxdm.xcap.common.datasource.Document oldDocument,
			org.w3c.dom.Document newDocumentDOM, String newDocumentString,
			String newETag, NodeSelector nodeSelector, Element oldElement,
			Element newElement)
			throws InternalServerErrorException {
		// update doc in data source
		ra.getDataSource().updateDocument(documentSelector, newETag,
				newDocumentString);
		// fire event
		ra.postElementUpdatedEvent(new ElementUpdatedEvent(documentSelector,
				defaultDocNamespace, oldDocument, newDocumentDOM,
				newDocumentString, newETag, nodeSelector, oldElement,
				newElement));
	}

	public DocumentActivity createDocumentActivity(
			DocumentSelector documentSelector) {
		return ra.createDocumentActivity(documentSelector);
	}

	public CollectionActivity createCollectionActivity(String collection) {
		return ra.createCollectionActivity(collection);
	}

}
