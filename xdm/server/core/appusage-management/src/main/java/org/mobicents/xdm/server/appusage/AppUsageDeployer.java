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

import java.net.URI;

import javax.xml.validation.Schema;

import org.apache.log4j.Logger;
import org.openxdm.xcap.common.xml.SchemaContext;

public abstract class AppUsageDeployer {

	private static final Logger logger = Logger.getLogger(AppUsageDeployer.class);
	
	private AppUsageFactory appUsageFactory;
	
	/**
	 * Creates the app usage factory.
	 * @return
	 */
	public abstract AppUsageFactory createAppUsageFactory(Schema schema);
	
	/**
	 * Retrieves the schema's root namespace for the app usage.
	 * @return
	 */
	public abstract String getSchemaRootNamespace();
	
	/**
	 * Retrieves the {@link URI} pointing to the directory containing the XML Schemas used to validate app usage documents.
	 * @return
	 */
	public URI getSchemaDirectoryURI() {
		return AppUsageManagement.getInstance().getDefaultSchemaDir();
	}
	
	/**
	 * This will be invoked by JBoss Microcontainer when the bean is deployed.
	 */
	public void start() {
		
		String schemaRootNamespace = getSchemaRootNamespace();
		URI schemaDirURI = getSchemaDirectoryURI();
		/*		
		if (schemaDirURI..get.getProtocol().equals("vfszip")) {
			try {
				schemaDirURL = new URL("file:"+schemaDirURL.getPath());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}*/
		
		if (logger.isDebugEnabled()) {
			logger.debug("Loading "+schemaRootNamespace+" combined schema from URI "+schemaDirURI);
		}
		
		try {
			// create schema context
			SchemaContext schemaContext = SchemaContext.fromDir(schemaDirURI);
			// get schema from context
			Schema schema = schemaContext
					.getCombinedSchema(schemaRootNamespace);
			if (logger.isInfoEnabled()) {
				logger.info("Loaded "+schemaRootNamespace+" combined schema from URI "+schemaDirURI);
			}	
			appUsageFactory = createAppUsageFactory(schema);
			AppUsageManagement.getInstance().put(appUsageFactory);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load "+schemaRootNamespace+" combined schema from URI "+schemaDirURI,e);
		}		
	}
	
	/**
	 * This will be invoked by JBoss Microcontainer when the bean is undeployed.
	 */
	public void stop() {
		if (appUsageFactory != null) 
			AppUsageManagement.getInstance().remove(appUsageFactory.getAppUsageId());
	}
	
}
