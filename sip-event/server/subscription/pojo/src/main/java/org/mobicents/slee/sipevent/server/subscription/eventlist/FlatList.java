package org.mobicents.slee.sipevent.server.subscription.eventlist;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openxdm.xcap.client.appusage.rlsservices.jaxb.ServiceType;
import org.openxdm.xcap.common.uri.DocumentSelector;

public class FlatList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient Map<String, SerializableEntryType> entries = new HashMap<String, SerializableEntryType>();
	
	private final SerializableServiceType serviceType;
	
	private transient Set<DocumentSelector> resourceLists = new HashSet<DocumentSelector>(); 
	
	private int status = 200;  
	
	public FlatList(SerializableServiceType serviceType) {
		this.serviceType = serviceType;
	}
	
	public Map<String, SerializableEntryType> getEntries() {
		return entries;
	}
	
	public Set<DocumentSelector> getResourceLists() {
		return resourceLists;
	}
	
	public ServiceType getServiceType() {
		return serviceType.getPojo();
	}
	
	public void putEntry(SerializableEntryType entryType) {
		entries.put(entryType.getPojo().getUri(), entryType);
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	@Override
	public int hashCode() { 
		return serviceType.getPojo().getUri().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			return ((FlatList)obj).serviceType.getPojo().getUri().equals(this.serviceType.getPojo().getUri());
		}
		else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "FlatList(uri="+getServiceType().getUri()+") = "+getEntries().keySet();		
	}
	
	// serialization
	
	private static final SerializableEntryType[] EMPTY_SEE_ARRAY = {};
	private static final DocumentSelector[] EMPTY_DS_ARRAY = {};
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		
		stream.defaultWriteObject();
		
		final Collection<SerializableEntryType> entriesValues = entries.values();
		final int entriesValuesSize = entriesValues.size();
		SerializableEntryType[] entriesArray;
		if (entriesValuesSize == 0) {
			entriesArray = EMPTY_SEE_ARRAY;
		}
		else {
			entriesArray = entriesValues.toArray(new SerializableEntryType[entriesValuesSize]);
		}
		stream.writeObject(entriesArray);
		
		final int resourceListsSize = resourceLists.size();
		DocumentSelector[] resourceListsArray;
		if (resourceListsSize == 0) {
			resourceListsArray = EMPTY_DS_ARRAY;
		}
		else {
			resourceListsArray = resourceLists.toArray(new DocumentSelector[resourceListsSize]);
		}
		stream.writeObject(resourceListsArray);
		
	}
	
	private void readObject(ObjectInputStream stream)  throws IOException, ClassNotFoundException {
				
		stream.defaultReadObject();

		SerializableEntryType[] sArray = (SerializableEntryType[]) stream.readObject();
		entries = new HashMap<String,SerializableEntryType>();
		for (SerializableEntryType s : sArray) {
			entries.put(s.getPojo().getUri(), s);
		}
				
		DocumentSelector[] dsArray = (DocumentSelector[]) stream.readObject();
		resourceLists = new HashSet<DocumentSelector>();
		for (DocumentSelector ds : dsArray) {
			resourceLists.add(ds);
		}
	}
}
