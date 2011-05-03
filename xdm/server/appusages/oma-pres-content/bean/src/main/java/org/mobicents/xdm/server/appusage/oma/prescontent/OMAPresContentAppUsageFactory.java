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

import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class OMAPresContentAppUsageFactory implements AppUsageFactory {

	private final Schema schema;
	private final Set<String> encodingsAllowed;
	private final int maxDataSize;
	private final Set<String> mimetypesAllowed;
	private final String presRulesAUID;
	private final String presRulesDocumentName;
	
	public OMAPresContentAppUsageFactory(Schema schema, Set<String> encodingsAllowed, int maxDataSize, Set<String> mimetypesAllowed, String presRulesAUID, String presRulesDocumentName) {
		this.schema = schema;
		this.encodingsAllowed = encodingsAllowed;
		this.maxDataSize = maxDataSize;
		this.mimetypesAllowed = mimetypesAllowed;
		this.presRulesAUID = presRulesAUID;
		this.presRulesDocumentName = presRulesDocumentName;
	}
	
	public AppUsage getAppUsageInstance() {
		return new OMAPresContentAppUsage(schema.newValidator(), encodingsAllowed, maxDataSize, mimetypesAllowed, presRulesAUID, presRulesDocumentName);
	}
	
	public String getAppUsageId() {
		return OMAPresContentAppUsage.ID;
	}
	
	@Override
	public AppUsageDataSourceInterceptor getDataSourceInterceptor() {
		return null;
	}
}
