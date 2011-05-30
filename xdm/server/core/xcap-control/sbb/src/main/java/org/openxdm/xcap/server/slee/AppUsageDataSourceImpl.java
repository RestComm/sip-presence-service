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

package org.openxdm.xcap.server.slee;

import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceSbbInterface;

/**
 * 
 * @author martins
 * 
 */
public class AppUsageDataSourceImpl implements AppUsageDataSource {

	/**
	 * 
	 */
	private final DataSourceSbbInterface dataSource;

	/**
	 * 
	 * @param dataSource
	 */
	public AppUsageDataSourceImpl(DataSourceSbbInterface dataSource) {
		this.dataSource = dataSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AppUsageDataSource#getDocument(org.
	 * openxdm.xcap.common.uri.DocumentSelector)
	 */
	@Override
	public Document getDocument(DocumentSelector documentSelector)
			throws InternalServerErrorException {
		return dataSource.getDocument(documentSelector);
	}

	@Override
	public Document[] getDocuments(String collection,
			boolean includeChildCollections)
			throws InternalServerErrorException {
		return dataSource.getDocuments(collection, includeChildCollections);
	}
}
