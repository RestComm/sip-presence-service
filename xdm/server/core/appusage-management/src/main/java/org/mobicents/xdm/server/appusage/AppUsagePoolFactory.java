package org.mobicents.xdm.server.appusage;

import org.apache.commons.pool.PoolableObjectFactory;

public class AppUsagePoolFactory implements PoolableObjectFactory {

	private final AppUsageFactory appUsageFactory;
	
	public AppUsagePoolFactory(AppUsageFactory appUsageFactory) {
		this.appUsageFactory = appUsageFactory;
	}
	
	@Override
	public void activateObject(Object arg0) throws Exception {
		// nothing to do
	}

	@Override
	public void destroyObject(Object arg0) throws Exception {
		// nothing to do
	}

	@Override
	public Object makeObject() throws Exception {
		return appUsageFactory.getAppUsageInstance();
	}

	@Override
	public void passivateObject(Object arg0) throws Exception {
		// nothing to do
	}

	@Override
	public boolean validateObject(Object arg0) {
		// nothing to do
		return true;
	}

}
