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

package org.openxdm.xcap.server.slee.resource.datasource;

import java.util.concurrent.ConcurrentHashMap;

import javax.slee.Address;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ActivityFlags;
import javax.slee.resource.EventFlags;
import javax.slee.resource.FailureReason;
import javax.slee.resource.FireableEventType;
import javax.slee.resource.Marshaler;
import javax.slee.resource.ReceivableService;
import javax.slee.resource.ResourceAdaptorContext;
import javax.slee.resource.SleeEndpoint;

import org.openxdm.xcap.common.datasource.DataSource;
import org.openxdm.xcap.common.uri.DocumentSelector;

public abstract class AbstractDataSourceResourceAdaptor implements DataSourceResourceAdaptor {
    	
    private ConcurrentHashMap<ActivityHandle,ActivityObject> activities = new ConcurrentHashMap<ActivityHandle,ActivityObject>();    
    private SleeEndpoint sleeEndpoint;
    private ResourceAdaptorContext context;
    private DataSourceSbbInterface sbbInterface = new DataSourceSbbInterface(this);
        
    private FireableEventType documentUpdatedEventId;
    private FireableEventType elementUpdatedEventId;
    private FireableEventType attributeUpdatedEventId;
    
    protected static int ACTIVITY_FLAGS = initActivityFlags();
    
    /**
	 * @return
	 */
	private static int initActivityFlags() {
		int activityFlags = ActivityFlags.REQUEST_ACTIVITY_UNREFERENCED_CALLBACK;
		return ActivityFlags.setRequestEndedCallback(activityFlags);
	}
	
    /* (non-Javadoc)
     * @see javax.slee.resource.ResourceAdaptor#activityEnded(javax.slee.resource.ActivityHandle)
     */
    @Override
    public void activityEnded(javax.slee.resource.ActivityHandle ah) {
    	final Tracer logger = getLogger();
		if (logger.isFineEnabled()) {
			logger.fine("activity "+ah+" ended");
		}
    	// just remove the handle
	    activities.remove(ah);
    }
	
	/*
	 * (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#activityUnreferenced(javax.slee.resource.ActivityHandle)
	 */
    @Override
	public void activityUnreferenced(javax.slee.resource.ActivityHandle ah) {
		final Tracer logger = getLogger();
		if (logger.isFineEnabled()) {
			logger.fine("activity "+ah+" unreferenced");
		}				
		// no need to keep activities that have no entities attached
		endActivity((ActivityHandle)ah);		
	}	
	
    /* (non-Javadoc)
     * @see javax.slee.resource.ResourceAdaptor#administrativeRemove(javax.slee.resource.ActivityHandle)
     */
    @Override
    public void administrativeRemove(javax.slee.resource.ActivityHandle arg0) {
    	// TODO Auto-generated method stub
    	
    }
    
    /* (non-Javadoc)
     * @see javax.slee.resource.ResourceAdaptor#eventUnreferenced(javax.slee.resource.ActivityHandle, javax.slee.resource.FireableEventType, java.lang.Object, javax.slee.Address, javax.slee.resource.ReceivableService, int)
     */
    @Override
    public void eventUnreferenced(javax.slee.resource.ActivityHandle arg0,
    		FireableEventType arg1, Object arg2, Address arg3,
    		ReceivableService arg4, int arg5) {
    	// TODO Auto-generated method stub
    	
    }
    
    /* (non-Javadoc)
     * @see javax.slee.resource.ResourceAdaptor#queryLiveness(javax.slee.resource.ActivityHandle)
     */
    @Override
    public void queryLiveness(javax.slee.resource.ActivityHandle arg0) {
    	// TODO Auto-generated method stub
    	
    }
        
	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#setResourceAdaptorContext(javax.slee.resource.ResourceAdaptorContext)
	 */
	@Override
	public void setResourceAdaptorContext(ResourceAdaptorContext arg0) {
		this.context = arg0;
        this.sleeEndpoint = context.getSleeEndpoint();
        try {
        	documentUpdatedEventId = context.getEventLookupFacility().getFireableEventType(DocumentUpdatedEvent.EVENT_TYPE_ID);
        	elementUpdatedEventId = context.getEventLookupFacility().getFireableEventType(ElementUpdatedEvent.EVENT_TYPE_ID);
        	attributeUpdatedEventId = context.getEventLookupFacility().getFireableEventType(AttributeUpdatedEvent.EVENT_TYPE_ID);
        } catch (Exception e) {
        	throw new RuntimeException(e.getMessage());
        }  
	}
	
	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#unsetResourceAdaptorContext()
	 */
	@Override
	public void unsetResourceAdaptorContext() {
		this.context = null;
		this.sleeEndpoint = null;
		this.documentUpdatedEventId = null;
		this.elementUpdatedEventId = null;
		this.attributeUpdatedEventId = null;		
	}
	
	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#raActive()
	 */
	@Override
	public void raActive() {
	}
	
	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#raStopping()
	 */
	@Override
	public void raStopping() {
		final Tracer logger = getLogger();
		if (logger.isFineEnabled()) {
			logger.fine("ra object for entity "+context.getEntityName()+" is stopping");
		}
		// end all activities
		synchronized(activities) {
			for(ActivityHandle activityHandle: activities.keySet()) {
				endActivity(activityHandle);
			}				
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#raInactive()
	 */
	@Override
	public void raInactive() {
		final Tracer logger = getLogger();
		if (logger.isFineEnabled()) {
			logger.fine("ra object for entity "+context.getEntityName()+" is inactive");
		}			
	}
	
	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#eventProcessingFailed(javax.slee.resource.ActivityHandle, javax.slee.resource.FireableEventType, java.lang.Object, javax.slee.Address, javax.slee.resource.ReceivableService, int, javax.slee.resource.FailureReason)
	 */
	@Override
	public void eventProcessingFailed(javax.slee.resource.ActivityHandle arg0,
			FireableEventType arg1, Object arg2, Address arg3,
			ReceivableService arg4, int arg5, FailureReason arg6) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#eventProcessingSuccessful(javax.slee.resource.ActivityHandle, javax.slee.resource.FireableEventType, java.lang.Object, javax.slee.Address, javax.slee.resource.ReceivableService, int)
	 */
	@Override
	public void eventProcessingSuccessful(
			javax.slee.resource.ActivityHandle arg0, FireableEventType arg1,
			Object arg2, Address arg3, ReceivableService arg4, int arg5) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#getActivity(javax.slee.resource.ActivityHandle)
	 */
	@Override
	public Object getActivity(javax.slee.resource.ActivityHandle handle) {
		return activities.get((ActivityHandle)handle);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#getActivityHandle(java.lang.Object)
	 */
	@Override
	public javax.slee.resource.ActivityHandle getActivityHandle(Object activity) {
		final ActivityObject activityObject = (ActivityObject) activity;
		final ActivityHandle activityHandle = new ActivityHandle(activityObject.id);
		if (activities.containsKey(activityHandle)) {
			return activityHandle;
		}
		else {
			return null;
		}	    
	}
		
	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#getMarshaler()
	 */
	@Override
	public Marshaler getMarshaler() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#getResourceAdaptorInterface(java.lang.String)
	 */
	@Override
	public Object getResourceAdaptorInterface(String arg0) {
		return this.sbbInterface;		
	}	
	
	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#serviceActive(javax.slee.resource.ReceivableService)
	 */
	@Override
	public void serviceActive(ReceivableService arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#serviceStopping(javax.slee.resource.ReceivableService)
	 */
	@Override
	public void serviceStopping(ReceivableService arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see javax.slee.resource.ResourceAdaptor#serviceInactive(javax.slee.resource.ReceivableService)
	 */
	@Override
	public void serviceInactive(ReceivableService arg0) {
		// TODO Auto-generated method stub
		
	}
	
	// ABSTRACT METHODS
	
	public abstract DataSource getDataSource();
	
	public abstract Tracer getLogger();
	
	// THIS RA LOGIC
	
	public void postDocumentUpdatedEvent(DocumentUpdatedEvent event) {
		postEvent(event, documentUpdatedEventId,event.getDocumentSelector());
	}
	public void postElementUpdatedEvent(ElementUpdatedEvent event) {
		postEvent(event, elementUpdatedEventId,event.getDocumentSelector());
	}
	public void postAttributeUpdatedEvent(AttributeUpdatedEvent event) {
		postEvent(event, attributeUpdatedEventId,event.getDocumentSelector());
	}
	
	private void postEvent(Object event, FireableEventType eventId, DocumentSelector documentSelector) {
		if (getLogger().isFineEnabled()) {
			getLogger().fine("postEvent(documentSelector="+documentSelector.toString()+")");
		}
		// try to fire event on document selector and all parent collection activities
		fireEvent(event, eventId, new ActivityHandle(documentSelector.toString()));
		for (String collection : documentSelector.getParentCollections()) {
			fireEvent(event, eventId, new ActivityHandle(collection));
		}
	}
	
	private void fireEvent(Object event, FireableEventType eventId, ActivityHandle handle) {
		if (getLogger().isFineEnabled()) {
			getLogger().fine("fireEvent(eventId="+eventId+",handleId="+handle.getId()+")");
		}
		if (getActivity(handle) != null) {
			// handle exists, fire event
			try {
				this.sleeEndpoint.fireEvent(handle, eventId, event, null, null,EventFlags.NO_FLAGS);
			} catch (Exception e) {
				getLogger().severe("failed to post event for "+handle.toString(), e);
			}
		}
	}
	
	public void endActivity(ActivityHandle handle) {
		// check it has activity
		if(activities.containsKey(handle)) {
			// tell slee to end the activity context
			try {
				this.sleeEndpoint.endActivity(handle);
			} catch (Exception e) {
				getLogger().severe("unable to end activity: ",e);
			}					
		}
	}
	
	/**
	 * creates a new activity, if does not exists
	 */
	public CollectionActivity createCollectionActivity(String collection) {
		final ActivityHandle activityHandle = new ActivityHandle(collection);
		CollectionActivity activity = (CollectionActivity) activities.get(activityHandle);
		if (activity == null) {
			activity = new CollectionActivity(collection);
			final CollectionActivity anotherActivity = (CollectionActivity) activities.putIfAbsent(activityHandle, activity);
			if (anotherActivity != null) {
				activity = anotherActivity;
			}
			else {
				// created, add to slee
				try {
					sleeEndpoint.startActivityTransacted(activityHandle, activity,ACTIVITY_FLAGS);
				} catch (Throwable e) {
					getLogger().severe("failed to start activity for collection "+collection,e);
				}
			}
		}
		
		return activity;
	}
	
	/**
	 * creates a new activity, if does not exists
	 */
	public DocumentActivity createDocumentActivity(
			DocumentSelector documentSelector) {
		ActivityHandle activityHandle = new ActivityHandle(documentSelector.toString());
		DocumentActivity activity = (DocumentActivity) activities.get(activityHandle);
		if (activity == null) {
			activity = new DocumentActivity(documentSelector);
			DocumentActivity anotherActivity = (DocumentActivity) activities.putIfAbsent(activityHandle, activity);
			if (anotherActivity != null) {
				activity = anotherActivity;
			}
			else {
				// created, add to slee
				try {
					sleeEndpoint.startActivityTransacted(activityHandle, activity,ACTIVITY_FLAGS);
				} catch (Throwable e) {
					getLogger().severe("failed to start activity for document selector "+documentSelector,e);
				}
			}
		}
		return activity;
	}
	
}
