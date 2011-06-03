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

/**
 * 
 */
package org.mobicents.slee.sippresence.server.jmx;

import java.net.URI;

import javax.xml.validation.Schema;

import org.jboss.virtual.VFS;
import org.jboss.virtual.VFSUtils;
import org.openxdm.xcap.common.xml.SchemaContext;

/**
 * @author martins
 *
 */
public class SipPresenceServerManagement implements SipPresenceServerManagementMBean {

	private String presRulesAUID;
	private String presRulesDocumentName;
	
	private static SipPresenceServerManagement INSTANCE = new SipPresenceServerManagement();
	
	/**
	 * Retrieves the singleton.
	 * @return
	 */
	public static SipPresenceServerManagement getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 
	 */
	private SipPresenceServerManagement() {
		// establish default xsd dir
		try {
			java.net.URL url = VFSUtils.getCompatibleURL(VFS
				.getRoot(SipPresenceServerManagement.class.getClassLoader()
						.getResource("../xsd")));
			URI schemaDirURI = new java.net.URI(url.toExternalForm()
				.replaceAll(" ", "%20"));
			// create schema context
			SchemaContext schemaContext = SchemaContext.fromDir(schemaDirURI);
			// get schema from context
			schema = schemaContext
						.getCombinedSchema();
				
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.server.jmx.SipPresenceServerManagementMBean#getPresRulesAUID()
	 */
	@Override
	public String getPresRulesAUID() {
		return presRulesAUID;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.server.jmx.SipPresenceServerManagementMBean#getPresRulesDocumentName()
	 */
	@Override
	public String getPresRulesDocumentName() {
		return presRulesDocumentName;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.server.jmx.SipPresenceServerManagementMBean#setPresRulesAUID(java.lang.String)
	 */
	@Override
	public void setPresRulesAUID(String auid) {
		this.presRulesAUID = auid;		
	}

	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.server.jmx.SipPresenceServerManagementMBean#setPresRulesDocumentName(java.lang.String)
	 */
	@Override
	public void setPresRulesDocumentName(String documentName) {
		this.presRulesDocumentName = documentName;
	}
	
	private Schema schema;
	
	public Schema getCombinedSchema() {		
		return schema;
	}
}
