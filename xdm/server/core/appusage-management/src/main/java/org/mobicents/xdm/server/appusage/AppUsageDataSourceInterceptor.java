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

package org.mobicents.xdm.server.appusage;

import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;

/**
 * The data source interceptor allows retrieval of dom docs that are built on
 * request.
 * 
 * @author martins
 * 
 */
public interface AppUsageDataSourceInterceptor {

	/**
	 * 
	 * @param documentSelector
	 * @param dataSource
	 * @return
	 * @throws InternalServerErrorException
	 */
	public InterceptedDocument getDocument(DocumentSelector documentSelector,
			AppUsageDataSource dataSource) throws InternalServerErrorException;

	/**
	 * 
	 * @param collection
	 * @param includeChildCollections
	 * @param dataSource
	 * @return
	 * @throws InternalServerErrorException
	 */
	public InterceptedDocument[] getDocuments(String collection,
			boolean includeChildCollections, AppUsageDataSource dataSource)
			throws InternalServerErrorException;

	/**
	 * Indicates if the interceptor is responsible for a document.
	 * 
	 * @param documentSelector
	 * @return false if the interceptor does not retrieves the document with the
	 *         specified selector
	 */
	public boolean interceptsDocument(DocumentSelector documentSelector);

	/**
	 * Indicates if the interceptor is responsible for a collection.
	 * 
	 * @param collection
	 * @param includeChildCollections
	 * @return false if the interceptor does not retrieves the documents in the
	 *         specified collection
	 */
	public boolean interceptsCollection(String collection,
			boolean includeChildCollections);

}
