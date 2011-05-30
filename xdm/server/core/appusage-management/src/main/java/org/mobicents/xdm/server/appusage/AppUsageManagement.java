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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VFSUtils;

/**
 * 
 * @author martins
 * 
 */
public class AppUsageManagement {

	private final ConcurrentHashMap<String, AppUsage> appUsages = new ConcurrentHashMap<String, AppUsage>();

	private final ConcurrentHashMap<String, AppUsageDataSourceInterceptor> interceptors = new ConcurrentHashMap<String, AppUsageDataSourceInterceptor>();

	private static final AppUsageManagement INSTANCE = new AppUsageManagement();

	private final URI defaultSchemaDir;

	public static AppUsageManagement getInstance() {
		return INSTANCE;
	}

	private static final Logger LOGGER = Logger
			.getLogger(AppUsageManagement.class);

	private AppUsageManagement() {
		// establish default xsd dir
		try {
			java.net.URL url = VFSUtils.getCompatibleURL(VFS
					.getRoot(AppUsageManagement.class.getClassLoader()
							.getResource("../xsd")));
			defaultSchemaDir = new java.net.URI(url.toExternalForm()
					.replaceAll(" ", "%20"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieves the app usage pool for the specified id.
	 * 
	 * @param auid
	 * @return
	 */
	public AppUsage getAppUsage(String auid) {
		return appUsages.get(auid);
	}

	public URI getDefaultSchemaDir() {
		return defaultSchemaDir;
	}

	/**
	 * Caches an appusage using the factory to generate instances into a
	 * concurrency pool.
	 * 
	 * @param appUsageFactory
	 */
	public void put(AppUsageFactory appUsageFactory) {
		if (appUsages.putIfAbsent(appUsageFactory.getAppUsageId(),
				appUsageFactory.getAppUsageInstance()) == null) {
			LOGGER.info("Added app usage " + appUsageFactory.getAppUsageId());
			if (appUsageFactory.getDataSourceInterceptor() != null) {
				interceptors.put(appUsageFactory.getAppUsageId(),
						appUsageFactory.getDataSourceInterceptor());
			}
		}
	}

	/**
	 * Removes the app usage from cache with the specified id
	 * 
	 * @param auid
	 */
	public void remove(String auid) {
		final AppUsage appUsage = appUsages.remove(auid);
		if (appUsage != null) {
			LOGGER.info("Removed app usage " + auid);
			interceptors.remove(auid);
		}
	}

	/**
	 * Retrieves the set of app usage ids.
	 * 
	 * @return
	 */
	public Set<String> getAppUsages() {
		return appUsages.keySet();
	}

	/**
	 * 
	 * @param auid
	 * @return
	 */
	public AppUsageDataSourceInterceptor getDataSourceInterceptor(String auid) {
		return interceptors.get(auid);
	}
}
