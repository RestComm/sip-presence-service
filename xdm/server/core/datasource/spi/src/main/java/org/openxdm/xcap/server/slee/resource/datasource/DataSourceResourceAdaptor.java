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

import javax.slee.resource.ResourceAdaptor;

import org.openxdm.xcap.common.datasource.DataSource;
import org.openxdm.xcap.common.uri.DocumentSelector;

public interface DataSourceResourceAdaptor extends ResourceAdaptor {

	public DataSource getDataSource();

	public void postDocumentUpdatedEvent(DocumentUpdatedEvent event);
	
	public void postElementUpdatedEvent(ElementUpdatedEvent event);
	
	public void postAttributeUpdatedEvent(AttributeUpdatedEvent event);

	public void endActivity(ActivityHandle handle);

	public DocumentActivity createDocumentActivity(DocumentSelector documentSelector);

	public CollectionActivity createCollectionActivity(String collection);
	
}
