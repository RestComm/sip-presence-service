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
	
}
