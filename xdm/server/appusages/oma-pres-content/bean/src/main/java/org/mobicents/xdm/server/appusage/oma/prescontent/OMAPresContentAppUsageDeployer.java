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

package org.mobicents.xdm.server.appusage.oma.prescontent;

import java.util.Set;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsageDeployer;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class OMAPresContentAppUsageDeployer extends AppUsageDeployer {
	
	private Set<String> encodingsAllowed;
	private Integer maxDataSize;
	private Set<String> mimetypesAllowed;
	private String presRulesAUID;
	private String presRulesDocumentName;
	
	@Override
	public AppUsageFactory createAppUsageFactory(Schema schema) {
		return new OMAPresContentAppUsageFactory(schema,encodingsAllowed,(maxDataSize != null ? maxDataSize : 0),mimetypesAllowed,presRulesAUID,presRulesDocumentName);
	}

	public Set<String> getEncodingsAllowed() {
		return encodingsAllowed;
	}
	
	public void setEncodingsAllowed(Set<String> encodingsAllowed) {
		this.encodingsAllowed = encodingsAllowed;
	}
	
	public Integer getMaxDataSize() {
		return maxDataSize;
	}
	
	public void setMaxDataSize(Integer maxDataSize) {
		this.maxDataSize = maxDataSize;
	}
	
	public Set<String> getMimetypesAllowed() {
		return mimetypesAllowed;
	}
	
	public void setMimetypesAllowed(Set<String> mimetypesAllowed) {
		this.mimetypesAllowed = mimetypesAllowed;
	}
	
	public String getPresRulesAUID() {
		return presRulesAUID;
	}
	
	public String getPresRulesDocumentName() {
		return presRulesDocumentName;
	}

	@Override
	public String getSchemaRootNamespace() {
		return OMAPresContentAppUsage.DEFAULT_DOC_NAMESPACE;
	}

	public void setPresRulesAUID(String auid) {
		this.presRulesAUID = auid;		
	}

	public void setPresRulesDocumentName(String documentName) {
		this.presRulesDocumentName = documentName;
	}

}
