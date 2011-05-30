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

package org.openxdm.xcap.common.datasource;

import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;

/**
 * TODO
 * Important: Implementations must be thread safe!
 * @author Eduardo Martins
 *
 */
public interface DataSource {

	/**
	 * Open the datasource, enables all required resources. 
	 * @throws InternalServerErrorException
	 */
	public void open() throws InternalServerErrorException;
	
	/**
	 * Closes the datasource, disables all required resources. 
	 * @throws InternalServerErrorException
	 */
	public void close() throws InternalServerErrorException;
	
	/**
	 * 
	 * @param documentSelector
	 * @return
	 * @throws InternalServerErrorException
	 */
	public Document getDocument(DocumentSelector documentSelector) throws InternalServerErrorException;

	/**
	 * 
	 * @param documentSelector
	 * @param eTag
	 * @param xml
	 * @throws InternalServerErrorException
	 */
	public void createDocument(DocumentSelector documentSelector, String eTag, String xml) throws InternalServerErrorException;

	/**
	 * 
	 * @param documentSelector
	 * @param eTag
	 * @param xml
	 * @throws InternalServerErrorException
	 */
	public void updateDocument(DocumentSelector documentSelector,
			String eTag, String xml) throws InternalServerErrorException;
	
	/**
	 * 
	 * @param documentSelector
	 * @throws InternalServerErrorException
	 */
	public void deleteDocument(DocumentSelector documentSelector) throws InternalServerErrorException;
	
	/**
	 * 
	 * @param collection
	 * @param includeChildCollections
	 * @return
	 * @throws InternalServerErrorException
	 */
	public Document[] getDocuments(String collection, boolean includeChildCollections) throws InternalServerErrorException;
	
}
