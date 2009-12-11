package org.openxdm.xcap.server.slee.resource.appusagecache;

import javax.slee.facilities.Tracer;

import org.openxdm.xcap.common.appusage.AppUsage;
import org.openxdm.xcap.common.appusage.AppUsageFactory;

public class AppUsageCacheResourceAdaptorSbbInterfaceImpl implements AppUsageCacheResourceAdaptorSbbInterface {
    
	private final AppUsageCacheResourceAdaptor ra;
	private final Tracer tracer;
	
	public AppUsageCacheResourceAdaptorSbbInterfaceImpl(AppUsageCacheResourceAdaptor ra) {
		this.ra = ra;
		tracer = ra.getContext().getTracer(getClass().getSimpleName());
	}

	public void put(AppUsageFactory appUsageFactory) {
		// create new pool
		AppUsagePool pool = new AppUsagePool(appUsageFactory,ra.getAppUsagePoolSize());
		// add to pools atomically, if not there
		ra.getPools().putIfAbsent(appUsageFactory.getAppUsageId(),pool);
		if(tracer.isFineEnabled()) {
			tracer.fine("created pool for app usage "+appUsageFactory.getAppUsageId());
		}
	}

	public AppUsage borrow(String auid) throws InterruptedException {
		if(tracer.isFineEnabled()) {
			tracer.fine("borrow(auid="+auid+")");
		}		
		AppUsagePool pool = ra.getPools().get(auid);
		if (pool != null) {
			return pool.borrow();
		}
		else {
			return null;
		}		
	}

	public void release(AppUsage appUsage) {
		if(tracer.isFineEnabled()) {
			tracer.fine("release(auid="+appUsage.getAUID()+")");
		}		
		AppUsagePool pool = ra.getPools().get(appUsage.getAUID());
		if (pool != null) {
			pool.release(appUsage);
		}				
	}

	public void remove(String auid) {
		if(tracer.isFineEnabled()) {
			tracer.fine("remove(auid="+auid+")");
		}		
		ra.getPools().remove(auid);		
	}	
	
}