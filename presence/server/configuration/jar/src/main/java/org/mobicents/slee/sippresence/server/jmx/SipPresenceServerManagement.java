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

/**
 * @author martins
 *
 */
public class SipPresenceServerManagement implements SipPresenceServerManagementMBean {

	private String presRulesAUID;
	private String presRulesDocumentName;
	private String jaxbPackageNames;
	
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
	private SipPresenceServerManagement() {}
	
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

	/**
	 * Retrieves the package names for jaxb pojos, to be used when (un)marshalling presence content.
	 * @return
	 */
	public String getJaxbPackageNames() {
		return jaxbPackageNames;
	}

	/**
	 * Sets the package names (separated by ':' char) for jaxb pojos, to be used when (un)marshalling
	 * presence content. All whitespaces will be removed.
	 * 
	 * @param packageNames
	 */
	public void setJaxbPackageNames(String packageNames) {
		this.jaxbPackageNames = packageNames.replaceAll("\\s","");
	}
}
