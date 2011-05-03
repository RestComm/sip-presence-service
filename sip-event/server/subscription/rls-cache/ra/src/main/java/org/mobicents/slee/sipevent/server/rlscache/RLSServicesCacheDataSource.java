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

package org.mobicents.slee.sipevent.server.rlscache;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.slee.resource.ActivityHandle;

import org.openxdm.xcap.common.uri.DocumentSelector;

public class RLSServicesCacheDataSource {

	private ConcurrentHashMap<RLSServiceActivityHandle, RLSServiceActivityImpl> rlsServicesActivities = new ConcurrentHashMap<RLSServiceActivityHandle, RLSServiceActivityImpl>();

	private ConcurrentHashMap<ResourceListActivityHandle, ResourceListActivityImpl> resourceListsActivities = new ConcurrentHashMap<ResourceListActivityHandle, ResourceListActivityImpl>();

	private ConcurrentHashMap<String, RLSServiceImpl> rlsServices = new ConcurrentHashMap<String, RLSServiceImpl>();

	private ConcurrentHashMap<DocumentSelector, ReferencedResourceLists> resourceLists = new ConcurrentHashMap<DocumentSelector, ReferencedResourceLists>();

	private ConcurrentHashMap<DocumentSelector, Set<String>> rlsServicesDocs = new ConcurrentHashMap<DocumentSelector, Set<String>>();

	public void removeActivity(ActivityHandle handle) {
		if (handle.getClass() == ResourceListActivityHandle.TYPE) {
			removeResourceListActivity((ResourceListActivityHandle) handle);
		}
		else {
			removeRLSServiceActivity((RLSServiceActivityHandle) handle);
		}
	}
	
	public Object getActivity(ActivityHandle handle) {
		if (handle.getClass() == ResourceListActivityHandle.TYPE) {
			return getResourceListActivity((ResourceListActivityHandle) handle);
		}
		else {
			return getRLSServiceActivity((RLSServiceActivityHandle) handle);
		}
	}
	
	public ActivityHandle getActivityHandle(Object activity) {
		if (activity.getClass() == ResourceListActivityImpl.TYPE) {
			ResourceListActivityImpl activityImpl = (ResourceListActivityImpl) activity;
			ResourceListActivityHandle handle = new ResourceListActivityHandle(activityImpl.getDocumentSelector()); 
			return resourceListsActivities.containsKey(handle) ? handle : null;
		}
		else {
			RLSServiceActivityImpl activityImpl = (RLSServiceActivityImpl) activity;
			RLSServiceActivityHandle handle = new RLSServiceActivityHandle(activityImpl.getServiceURI()); 
			return rlsServicesActivities.containsKey(handle) ? handle : null;
		}
	}
	
	public RLSServiceActivityImpl putIfAbsentRLSServiceActivity(
			RLSServiceActivityHandle handle, RLSServiceActivityImpl activity) {
		return rlsServicesActivities.putIfAbsent(handle, activity);
	}

	public RLSServiceActivityImpl getRLSServiceActivity(
			RLSServiceActivityHandle handle) {
		return rlsServicesActivities.get(handle);
	}

	public RLSServiceActivityImpl removeRLSServiceActivity(
			RLSServiceActivityHandle handle) {
		return rlsServicesActivities.remove(handle);
	}

	public ResourceListActivityImpl putIfAbsentResourceListActivity(
			ResourceListActivityHandle handle, ResourceListActivityImpl activity) {
		return resourceListsActivities.putIfAbsent(handle, activity);
	}

	public ResourceListActivityImpl getResourceListActivity(
			ResourceListActivityHandle handle) {
		return resourceListsActivities.get(handle);

	}

	public ResourceListActivityImpl removeResourceListActivity(
			ResourceListActivityHandle handle) {
		return resourceListsActivities.remove(handle);
	}

	public RLSServiceImpl putRLSServiceIfAbsent(String serviceURI,
			RLSServiceImpl rlsService) {
		return rlsServices.putIfAbsent(serviceURI, rlsService);
	}

	public RLSServiceImpl getRLSService(String serviceURI) {
		return rlsServices.get(serviceURI);
	}

	public RLSServiceImpl removeRLSService(String serviceURI) {
		return rlsServices.remove(serviceURI);
	}

	public ReferencedResourceLists putResourceListIfAbsent(
			DocumentSelector documentSelector,
			ReferencedResourceLists resourceList) {
		return resourceLists.putIfAbsent(documentSelector, resourceList);
	}

	public ReferencedResourceLists getResourceList(
			DocumentSelector documentSelector) {
		return resourceLists.get(documentSelector);
	}

	public ReferencedResourceLists removeResourceList(
			DocumentSelector documentSelector) {
		return resourceLists.remove(documentSelector);
	}

	public void putRlsServicesDocs(
			DocumentSelector documentSelector,
			Set<String> rlsServicesDoc) {
		rlsServicesDocs.put(documentSelector, rlsServicesDoc);
	}

	public Set<String> getRlsServicesDocs(
			DocumentSelector documentSelector) {
		return rlsServicesDocs.get(documentSelector);
	}

	public Set<String> removeRlsServicesDocs(
			DocumentSelector documentSelector) {
		return rlsServicesDocs.remove(documentSelector);
	}
	
	public Set<String> getRLSServices() {
		return new HashSet<String>(rlsServices.keySet());
	}
	
	public Set<ActivityHandle> getAllHandles() {
		HashSet<ActivityHandle> result = new HashSet<ActivityHandle>(rlsServicesActivities.keySet());
		result.addAll(resourceListsActivities.keySet());
		return result;
	}
}
