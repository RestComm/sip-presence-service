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

import java.util.Map;

import org.openxdm.xcap.common.datasource.DataSource;
import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.AttributeSelector;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.NodeSelector;
import org.w3c.dom.Element;

/**
 * Sbb Interface implementation for a DataSource Resource Adaptor.
 * TODO complete javadoc 
 * @author Eduardo Martins
 *
 */
public class DataSourceSbbInterface implements DataSource {

	private DataSourceResourceAdaptor ra;
	
	public DataSourceSbbInterface(DataSourceResourceAdaptor ra) {
		this.ra = ra;
	}

	@Deprecated
	public void open() throws InternalServerErrorException {
		throw new InternalServerErrorException("forbidden to open datasource from sbb");		
	}

	@Deprecated
	public void close() throws InternalServerErrorException {
		throw new InternalServerErrorException("forbidden to close datasource from sbb");		
	}	
			
	public void createDocument(DocumentSelector documentSelector, String eTag, String documentAsString, org.w3c.dom.Document document) throws InternalServerErrorException {
		ra.getDataSource().createDocument(documentSelector, eTag, documentAsString, document);
		// fire event
		ra.postDocumentUpdatedEvent(new DocumentUpdatedEvent(documentSelector,null,eTag,documentAsString,document));
	}

	public void deleteDocument(DocumentSelector documentSelector, String oldETag)
			throws InternalServerErrorException {
		// delete in data source
		ra.getDataSource().deleteDocument(documentSelector,oldETag);	
		// fire event
		ra.postDocumentUpdatedEvent(new DocumentUpdatedEvent(documentSelector,oldETag,null,null,null));
	}
	
	@Override
	public Document[] getDocuments(String auid)
			throws InternalServerErrorException {
		return ra.getDataSource().getDocuments(auid);
	}
	
	public Document[] getDocuments(String auid, String documentParent)
			throws InternalServerErrorException {
		return ra.getDataSource().getDocuments(auid,documentParent);
	}
	
	public Document getDocument(DocumentSelector documentSelector) throws InternalServerErrorException {
		return ra.getDataSource().getDocument(documentSelector);
	}

	public void updateAttribute(DocumentSelector documentSelector,NodeSelector nodeSelector,
			AttributeSelector attributeSelector, Map<String,String> namespaces,
			String oldETag, String newETag, String documentAsString,org.w3c.dom.Document document,String attributeValue)
			throws InternalServerErrorException {
		// update doc in data source
		ra.getDataSource().updateDocument(documentSelector, oldETag,newETag, documentAsString, document);
		// fire event
		ra.postAttributeUpdatedEvent(new AttributeUpdatedEvent(documentSelector,nodeSelector,attributeSelector,namespaces,oldETag,newETag,documentAsString,attributeValue));
	}

	public void updateDocument(DocumentSelector documentSelector,
			String oldETag, String newETag, String documentAsString,
			org.w3c.dom.Document document) throws InternalServerErrorException {
		// update doc in data source
		ra.getDataSource().updateDocument(documentSelector, oldETag,newETag, documentAsString, document);
		// fire event
		ra.postDocumentUpdatedEvent(new DocumentUpdatedEvent(documentSelector,oldETag,newETag,documentAsString,document));
	}

	public void updateElement(DocumentSelector documentSelector, NodeSelector nodeSelector, Map<String,String> namespaces,
			String oldETag, String newETag, String documentAsString,org.w3c.dom.Document document,String elementAsString,
			Element element) throws InternalServerErrorException {
		// update doc in data source
		ra.getDataSource().updateDocument(documentSelector, oldETag,newETag, documentAsString, document);
		// fire event
		ra.postElementUpdatedEvent(new ElementUpdatedEvent(documentSelector,nodeSelector,namespaces,oldETag,newETag,documentAsString,elementAsString,element));
	}
	
	public DocumentActivity createDocumentActivity(DocumentSelector documentSelector) {
		return ra.createDocumentActivity(documentSelector);
	}
	
	public AppUsageActivity createAppUsageActivity(String auid) {
		return ra.createAppUsageActivity(auid);
	}
	
}
