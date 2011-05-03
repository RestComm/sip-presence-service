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

import org.apache.commons.pool.ObjectPool;
import org.openxdm.xcap.common.error.InternalServerErrorException;

/**
 * 
 * @author martins
 * 
 */
public class AppUsagePool {

	private final ObjectPool objectPool;

	/**
	 * 
	 * @param objectPool
	 */
	public AppUsagePool(ObjectPool objectPool) {
		this.objectPool = objectPool;
	}

	/**
	 * Borrows an instance from the app usage object pool.
	 * 
	 * @return
	 * @throws InternalServerErrorException
	 */
	public AppUsage borrowInstance() throws InternalServerErrorException {
		try {
			return (AppUsage) objectPool.borrowObject();
		} catch (Throwable e) {
			throw new InternalServerErrorException(
					"Failed to borrow app usage instance from pool.", e);
		}
	}

	/**
	 * Returns the instance to its object pool.
	 * 
	 * @param appUsage
	 * @throws InternalServerErrorException
	 */
	public void returnInstance(AppUsage appUsage)
			throws InternalServerErrorException {
		try {
			objectPool.returnObject(appUsage);
		} catch (Exception e) {
			throw new InternalServerErrorException(
					"Failed to return app usage instance to pool.", e);
		}
	}
	
	/**
	 * Closes the pool.
	 */
	public void close() {
		try {
			objectPool.close();
		} catch (Exception e) {
			throw new RuntimeException("Failed to close object pool.",e);
		}
	}
}
