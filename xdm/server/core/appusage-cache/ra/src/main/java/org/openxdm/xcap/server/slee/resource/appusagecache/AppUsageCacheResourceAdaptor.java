package org.openxdm.xcap.server.slee.resource.appusagecache;

import java.util.concurrent.ConcurrentHashMap;

import javax.slee.Address;
import javax.slee.resource.ActivityHandle;
import javax.slee.resource.ConfigProperties;
import javax.slee.resource.FailureReason;
import javax.slee.resource.FireableEventType;
import javax.slee.resource.InvalidConfigurationException;
import javax.slee.resource.Marshaler;
import javax.slee.resource.ReceivableService;
import javax.slee.resource.ResourceAdaptor;
import javax.slee.resource.ResourceAdaptorContext;

/**
 * This is the AppUsage Cache Resource Adaptor's Implementation. Manages the
 * object pools of app usage objects.
 * 
 * @author Eduardo Martins
 * @version 1.0
 * 
 */

public class AppUsageCacheResourceAdaptor implements ResourceAdaptor {
    
	private ResourceAdaptorContext context;
    
    private ConcurrentHashMap<String,AppUsagePool> pools;    
    private int appUsagePoolSize = 25;
    private boolean active = false;
    private AppUsageCacheResourceAdaptorSbbInterface sbbInterface;
    
    public ConcurrentHashMap<String, AppUsagePool> getPools() {
		return pools;
	}
    
    public int getAppUsagePoolSize() {
		return appUsagePoolSize;
	}
    
    /* (non-Javadoc)
     * @see javax.slee.resource.ResourceAdaptor#setResourceAdaptorContext(javax.slee.resource.ResourceAdaptorContext)
     */
    @Override
    public void setResourceAdaptorContext(ResourceAdaptorContext arg0) {
    	this.context = arg0;
    }
    
    /* (non-Javadoc)
     * @see javax.slee.resource.ResourceAdaptor#raActive()
     */
    @Override
    public void raActive() {
    	 if (!active) {
 	    	// create pool map
 	    	pools = new ConcurrentHashMap<String,AppUsagePool>();  
 			// init sbb interface
 			sbbInterface = new AppUsageCacheResourceAdaptorSbbInterfaceImpl(this);
 			active = true;
 	    }
    }
    
    
    /* (non-Javadoc)
     * @see javax.slee.resource.ResourceAdaptor#raInactive()
     */
    @Override
    public void raInactive() {
    	if(active) {
        	pools = null;
        	sbbInterface = null;
        	active = false;
        }   
    }
    
    /* (non-Javadoc)
     * @see javax.slee.resource.ResourceAdaptor#unsetResourceAdaptorContext()
     */
    @Override
    public void unsetResourceAdaptorContext() {
    	this.context = null;
    }

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#getResourceAdaptorInterface(java.lang.String)
	 */
	@Override
	public Object getResourceAdaptorInterface(String arg0) {
		return sbbInterface;
	}

	/**
	 * @return the context
	 */
	public ResourceAdaptorContext getContext() {
		return context;
	}
	
    // NOT USED
        
	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#activityEnded(javax.slee.resource.ActivityHandle)
	 */
	@Override
	public void activityEnded(ActivityHandle arg0) {
		
		
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#activityUnreferenced(javax.slee.resource.ActivityHandle)
	 */
	@Override
	public void activityUnreferenced(ActivityHandle arg0) {
		
		
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#administrativeRemove(javax.slee.resource.ActivityHandle)
	 */
	@Override
	public void administrativeRemove(ActivityHandle arg0) {
		
		
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#eventProcessingFailed(javax.slee.resource.ActivityHandle, javax.slee.resource.FireableEventType, java.lang.Object, javax.slee.Address, javax.slee.resource.ReceivableService, int, javax.slee.resource.FailureReason)
	 */
	@Override
	public void eventProcessingFailed(ActivityHandle arg0,
			FireableEventType arg1, Object arg2, Address arg3,
			ReceivableService arg4, int arg5, FailureReason arg6) {
		
		
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#eventProcessingSuccessful(javax.slee.resource.ActivityHandle, javax.slee.resource.FireableEventType, java.lang.Object, javax.slee.Address, javax.slee.resource.ReceivableService, int)
	 */
	@Override
	public void eventProcessingSuccessful(ActivityHandle arg0,
			FireableEventType arg1, Object arg2, Address arg3,
			ReceivableService arg4, int arg5) {
		
		
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#eventUnreferenced(javax.slee.resource.ActivityHandle, javax.slee.resource.FireableEventType, java.lang.Object, javax.slee.Address, javax.slee.resource.ReceivableService, int)
	 */
	@Override
	public void eventUnreferenced(ActivityHandle arg0, FireableEventType arg1,
			Object arg2, Address arg3, ReceivableService arg4, int arg5) {
		
		
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#getActivity(javax.slee.resource.ActivityHandle)
	 */
	@Override
	public Object getActivity(ActivityHandle arg0) {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#getActivityHandle(java.lang.Object)
	 */
	@Override
	public ActivityHandle getActivityHandle(Object arg0) {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#getMarshaler()
	 */
	@Override
	public Marshaler getMarshaler() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#queryLiveness(javax.slee.resource.ActivityHandle)
	 */
	@Override
	public void queryLiveness(ActivityHandle arg0) {
		
		
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#raConfigurationUpdate(javax.slee.resource.ConfigProperties)
	 */
	@Override
	public void raConfigurationUpdate(ConfigProperties arg0) {
		
		
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#raConfigure(javax.slee.resource.ConfigProperties)
	 */
	@Override
	public void raConfigure(ConfigProperties arg0) {
		
		
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#raStopping()
	 */
	@Override
	public void raStopping() {
		
		
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#raUnconfigure()
	 */
	@Override
	public void raUnconfigure() {
		
		
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#raVerifyConfiguration(javax.slee.resource.ConfigProperties)
	 */
	@Override
	public void raVerifyConfiguration(ConfigProperties arg0)
			throws InvalidConfigurationException {
		
		
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#serviceActive(javax.slee.resource.ReceivableService)
	 */
	@Override
	public void serviceActive(ReceivableService arg0) {
		
		
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#serviceInactive(javax.slee.resource.ReceivableService)
	 */
	@Override
	public void serviceInactive(ReceivableService arg0) {
		
		
	}

	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#serviceStopping(javax.slee.resource.ReceivableService)
	 */
	@Override
	public void serviceStopping(ReceivableService arg0) {
		
		
	}
    
}

