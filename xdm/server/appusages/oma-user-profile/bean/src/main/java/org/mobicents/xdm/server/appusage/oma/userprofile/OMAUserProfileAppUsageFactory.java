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

package org.mobicents.xdm.server.appusage.oma.userprofile;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class OMAUserProfileAppUsageFactory implements AppUsageFactory {

	private final Schema schema;

	public OMAUserProfileAppUsageFactory(Schema schema) {
		this.schema = schema;
	}
	
	public AppUsage getAppUsageInstance() {
		return new OMAUserProfileAppUsage(schema.newValidator());
	}
	
	public String getAppUsageId() {
		return OMAUserProfileAppUsage.ID;
	}
	
	@Override
	public AppUsageDataSourceInterceptor getDataSourceInterceptor() {
		return null;
	}
}
