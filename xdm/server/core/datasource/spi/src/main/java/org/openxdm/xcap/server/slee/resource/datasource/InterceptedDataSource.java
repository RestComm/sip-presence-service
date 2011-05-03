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

import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageManagement;
import org.openxdm.xcap.common.datasource.DataSource;
import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;

/**
 * 
 * @author martins
 *
 */
public class InterceptedDataSource implements DataSource,AppUsageDataSource {

	private static final AppUsageManagement APP_USAGE_MANAGEMENT = AppUsageManagement.getInstance();
	
	private final DataSource dataSource;
	
	public InterceptedDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Override
	public Document getDocument(DocumentSelector documentSelector)
			throws InternalServerErrorException {
		final AppUsageDataSourceInterceptor interceptor = APP_USAGE_MANAGEMENT.getDataSourceInterceptor(documentSelector.getAUID());
		if (interceptor != null) {
			return interceptor.getDocument(documentSelector, this);
		}
		else {
			return dataSource.getDocument(documentSelector);
		}
	}
	
	@Override
	public Document[] getDocuments(String auid)
			throws InternalServerErrorException {
		final AppUsageDataSourceInterceptor interceptor = APP_USAGE_MANAGEMENT.getDataSourceInterceptor(auid);
		if (interceptor != null) {
			return interceptor.getDocuments(this);
		}
		else {
			return dataSource.getDocuments(auid);
		}
	}
	
	@Override
	public Document[] getDocuments(String auid, String documentParent)
			throws InternalServerErrorException {
		final AppUsageDataSourceInterceptor interceptor = APP_USAGE_MANAGEMENT.getDataSourceInterceptor(auid);
		if (interceptor != null) {
			return interceptor.getDocuments(documentParent,this);
		}
		else {
			return dataSource.getDocuments(auid,documentParent);
		}
	}

	@Override
	public void close() throws InternalServerErrorException {
		dataSource.close();
	}

	@Override
	public void createDocument(DocumentSelector documentSelector, String eTag,
			String xml, org.w3c.dom.Document document)
			throws InternalServerErrorException {
		dataSource.createDocument(documentSelector, eTag, xml, document);
	}

	@Override
	public void deleteDocument(DocumentSelector documentSelector, String oldETag)
			throws InternalServerErrorException {
		dataSource.deleteDocument(documentSelector, oldETag);
	}

	@Override
	public void open() throws InternalServerErrorException {
		dataSource.open();
	}

	@Override
	public void updateDocument(DocumentSelector documentSelector,
			String oldETag, String newETag, String documentAsString,
			org.w3c.dom.Document document) throws InternalServerErrorException {
		dataSource.updateDocument(documentSelector, oldETag, newETag, documentAsString, document);
	}
	
}
