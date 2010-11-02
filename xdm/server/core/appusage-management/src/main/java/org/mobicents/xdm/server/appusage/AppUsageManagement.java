package org.mobicents.xdm.server.appusage;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.InitialContext;
import javax.slee.EventTypeID;
import javax.slee.connection.ExternalActivityHandle;
import javax.slee.connection.SleeConnection;
import javax.slee.connection.SleeConnectionFactory;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VFSUtils;

/**
 * 
 * @author martins
 *
 */
public class AppUsageManagement {

	private static final EventTypeID APPUSAGE_ADDED_EVENT_TYPE = new EventTypeID("AppUsageAddedEvent", "org.mobicents.xdm", "1.0");
	private static final EventTypeID APPUSAGE_REMOVED_EVENT_TYPE = new EventTypeID("AppUsageRemovedEvent", "org.mobicents.xdm", "1.0");
	
	private final ConcurrentHashMap<String, AppUsagePool> pools = new ConcurrentHashMap<String, AppUsagePool>();
	
	private final ConcurrentHashMap<String, AppUsageDataSourceInterceptor> interceptors = new ConcurrentHashMap<String, AppUsageDataSourceInterceptor>();

	private static final AppUsageManagement INSTANCE = new AppUsageManagement();
	
	private final URI defaultSchemaDir; 
	
	public static AppUsageManagement getInstance() {
		return INSTANCE;
	}
	
	private final GenericObjectPool.Config objectPoolConfig;
	
	private static final Logger LOGGER = Logger.getLogger(AppUsageManagement.class);
	
	private AppUsageManagement() {	
		// create pool config mbean with default pool configuration
		objectPoolConfig = new GenericObjectPool.Config();
		objectPoolConfig.maxActive = -1;
		objectPoolConfig.maxIdle = 50;
		objectPoolConfig.maxWait = -1;
		objectPoolConfig.minEvictableIdleTimeMillis = 60000;
		objectPoolConfig.minIdle = 0;
		objectPoolConfig.numTestsPerEvictionRun = -1;
		objectPoolConfig.testOnBorrow = false;
		objectPoolConfig.testOnReturn = false;
		objectPoolConfig.testWhileIdle = false;
		objectPoolConfig.timeBetweenEvictionRunsMillis = 300000;
		objectPoolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
		// establish default xsd dir
		try {
			java.net.URL url = VFSUtils.getCompatibleURL(VFS
					.getRoot(AppUsageManagement.class.getClassLoader().getResource(
							"../xsd")));
			defaultSchemaDir = new java.net.URI(url.toExternalForm()
					.replaceAll(" ", "%20"));			 
		} catch (Exception e) {
			throw new RuntimeException(e);
		}	
	}
		
	/**
	 * Retrieves the app usage pool for the specified id.
	 * @param auid
	 * @return
	 */
	public AppUsagePool getAppUsagePool(String auid) {
		return pools.get(auid);
	}
	
	public URI getDefaultSchemaDir() {
		return defaultSchemaDir;
	}
	
	/**
	 * Caches an appusage using the factory to generate instances into a concurrency pool.
	 * @param appUsageFactory
	 */
	public void put(AppUsageFactory appUsageFactory) {
		
		AppUsagePoolFactory appUsagePoolFactory = new AppUsagePoolFactory(appUsageFactory);
		ObjectPool objectPool = new GenericObjectPool(appUsagePoolFactory,objectPoolConfig);
		AppUsagePool pool = new AppUsagePool(objectPool);
		if (pools.putIfAbsent(appUsageFactory.getAppUsageId(),pool) == null) {
			LOGGER.info("Added app usage "+appUsageFactory.getAppUsageId());
			if (appUsageFactory.getDataSourceInterceptor() != null) {
				interceptors.put(appUsageFactory.getAppUsageId(), appUsageFactory.getDataSourceInterceptor());
			}
			// inform SLEE
			fireEventToSLEE(new AppUsageAddedEvent(appUsageFactory.getAppUsageId()), APPUSAGE_ADDED_EVENT_TYPE);
		}
	}
		
	private void fireEventToSLEE(Object event, EventTypeID eventTypeID) {
		try {
			InitialContext ic = new InitialContext();
			SleeConnectionFactory factory = (SleeConnectionFactory) ic.lookup("java:/MobicentsConnectionFactory");
			if (factory != null) {
				SleeConnection connection = factory.getConnection();
				ExternalActivityHandle handle = connection.createActivityHandle();
				// ensuring the event type exists in SLEE
				EventTypeID eventTypeID2 = connection.getEventTypeID(eventTypeID.getName(), eventTypeID.getVendor(), eventTypeID.getVersion());
				if (eventTypeID2 != null) {
					connection.fireEvent(event, eventTypeID2, handle, null);
				}
				connection.close();
			}
		} catch (Exception e) {
			// SLEE is not running ?
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(e.getMessage(),e);
			}
		}
	}

	/**
	 * Removes the app usage from cache with the specified id
	 * @param auid
	 */
	public void remove(String auid) {
		final AppUsagePool pool = pools.remove(auid);
		if (pool != null) {
			LOGGER.info("Removed app usage "+auid);
			pool.close();
			interceptors.remove(auid);
			// inform SLEE
			fireEventToSLEE(new AppUsageRemovedEvent(auid),APPUSAGE_REMOVED_EVENT_TYPE);
		}
	}

	/**
	 * Retrieves the set of app usage ids.
	 * @return
	 */
	public Set<String> getAppUsages() {
		return pools.keySet();
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
