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


/**
 * Factory to generate AppUsage instances.
 * @author Eduardo Martins
 *
 */
public interface AppUsageFactory {

	/**
	 * Returns a new AppUsage instance.
	 * @return
	 */
	public AppUsage getAppUsageInstance();
	
	/**
	 * Retrieves the id of the AppUsage objects created by this factory.
	 * @return
	 */
	public String getAppUsageId();
	
	/**
	 * Retrieves the interceptor to build docs content on request.
	 * @return null if the app usage defines no interceptor.
	 */
	public AppUsageDataSourceInterceptor getDataSourceInterceptor();
	
}
