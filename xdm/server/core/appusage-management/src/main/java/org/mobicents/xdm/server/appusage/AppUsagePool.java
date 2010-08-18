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
