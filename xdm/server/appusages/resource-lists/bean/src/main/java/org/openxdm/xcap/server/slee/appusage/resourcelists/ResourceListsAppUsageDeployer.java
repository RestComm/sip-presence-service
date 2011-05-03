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

package org.openxdm.xcap.server.slee.appusage.resourcelists;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsageDeployer;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class ResourceListsAppUsageDeployer extends AppUsageDeployer {
	
	private final boolean omaCompliant;

	public ResourceListsAppUsageDeployer(boolean omaCompliant) {
		super();
		this.omaCompliant = omaCompliant;
	}
	
	@Override
	public AppUsageFactory createAppUsageFactory(Schema schema) {
		return new ResourceListsAppUsageFactory(schema,omaCompliant);
	}

	@Override
	public String getSchemaRootNamespace() {
		return ResourceListsAppUsage.DEFAULT_DOC_NAMESPACE;
	}

}
