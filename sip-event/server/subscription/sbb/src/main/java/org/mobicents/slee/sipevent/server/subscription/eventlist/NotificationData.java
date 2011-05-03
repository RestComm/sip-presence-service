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

package org.mobicents.slee.sipevent.server.subscription.eventlist;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.rlscache.RLSService;
import org.mobicents.slee.sipevent.server.subscription.eventlist.rlmi.List;
import org.mobicents.slee.sipevent.server.subscription.eventlist.rlmi.Name;
import org.mobicents.slee.sipevent.server.subscription.eventlist.rlmi.Resource;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType.DisplayName;

public class NotificationData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/*
	 * JAXB context is thread safe
	 */
	protected static final JAXBContext rlmiJaxbContext = initJAXBContext();

	private static final Logger logger = Logger.getLogger(NotificationData.class);
	
	private static JAXBContext initJAXBContext() {
		try {
			return JAXBContext
					.newInstance("org.mobicents.slee.sipevent.server.subscription.eventlist.rlmi");
		} catch (JAXBException e) {
			logger.error("failed to create jaxb context");
			return null;
		}
	}
	
	private transient Set<String> urisLeft;
	private final EntryType[] entries;
	private final int version;
	private final String notifier;
	private transient Map<String,BodyPart> bodyParts = new HashMap<String, BodyPart>();
	private transient Map<String,Instance> instances = new HashMap<String, Instance>();
	private final boolean fullState;
	private final String rlmiCid;
	private final String multiPartBoundary;
	private final AtomicBoolean locked = new AtomicBoolean(false);
	
	private static final EntryType[] EMPTY_ENTRYTYPE_ARRAY = {};
	
	private static class Instance implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		String uri;
		String id;
		String cid;
		String state;
		String reason;		
		
	}
	
	public NotificationData(String notifier, int version, RLSService rlsService, String rlmiCid, String multiPartBoundary) {
		this.version = version;
		this.notifier = notifier;
		this.fullState = true;
		this.rlmiCid = rlmiCid;
		this.multiPartBoundary = multiPartBoundary;
		entries = rlsService.getEntries().toArray(EMPTY_ENTRYTYPE_ARRAY);
		urisLeft = new HashSet<String>(entries.length);
		for (EntryType entryType : entries) {
			urisLeft.add(entryType.getUri());
		}
	}
	
	public NotificationData(String notifier, int version, EntryType entryType, String rlmiCid, String multiPartBoundary) {
		this.version = version;
		this.notifier = notifier;
		this.fullState = false;
		this.rlmiCid = rlmiCid;
		this.multiPartBoundary = multiPartBoundary;
		entries = new EntryType[]{entryType};
		urisLeft = new HashSet<String>(1);
		urisLeft.add(entryType.getUri());		
	}
	
	/**
	 * Adds notification data for a resource
	 * @return if all required data is added a {@link MultiPart} will be returned, otherwise null
	 */
	public MultiPart addNotificationData(String uri, String cid, String instanceId, String content, String contentType, String contentSubType, String status, String reason) throws IllegalStateException {		
		if (cid != null) {
			bodyParts.put(uri, new BodyPart(uri,"binary",cid,contentType,contentSubType,"UTF-8",content));
		}
		Instance instance = new Instance();
		instance.uri = uri;
		instance.id = instanceId;
		instance.cid = cid;
		instance.state = status;
		instance.reason = reason;
		instances.put(uri, instance);
		return notificationDataNotNeeded(uri);
	}
	
	/**
	 * Indicates no notification data for a resource, possibly due to an error, from this invocation this resource won't count on the verification for complete notification data & multipart building 
	 * @return if all required data is added a {@link MultiPart} will be returned, otherwise null
	 */
	public MultiPart notificationDataNotNeeded(String uri) throws IllegalStateException {
		if(urisLeft.remove(uri) && urisLeft.isEmpty()) {
			return buildMultipart();
		}
		else {
			return null;
		}
	}
	
	private MultiPart buildMultipart() throws IllegalStateException {
		
		// check lock
		if (!locked.compareAndSet(false, true)) {
			throw new IllegalStateException();
		}
		// create rlmi
		List rlmiList = new List();
		rlmiList.setFullState(fullState);
		rlmiList.setVersion(version);
		rlmiList.setUri(notifier);
		for (EntryType entryType : entries) {
			final Resource resource = new Resource();
			resource.setUri(entryType.getUri());
			final DisplayName displayName = entryType.getDisplayName();
			if (displayName != null) {
				Name name = new Name();
				name.setLang(displayName.getLang());
				name.setValue(displayName.getValue());
				resource.getName().add(name);
			}
			final Instance instance = instances.get(entryType.getUri());
			if (instance != null) {
				org.mobicents.slee.sipevent.server.subscription.eventlist.rlmi.Instance jaxbInstance = new org.mobicents.slee.sipevent.server.subscription.eventlist.rlmi.Instance();
				jaxbInstance.setId(instance.id);
				jaxbInstance.setCid(instance.cid);
				jaxbInstance.setState(instance.state);
				jaxbInstance.setReason(instance.reason);
				resource.getInstance().add(jaxbInstance);
			}
			rlmiList.getResource().add(resource);
		}
		// marshall it
		String rlmiString = marshallRlmi(rlmiList);
		// create multipart
		MultiPart multiPart = new MultiPart(multiPartBoundary,"application/rlmi+xml");
		// add rlmi body part
		multiPart.getBodyParts().add(new BodyPart(null,"binary",rlmiCid,"application","rlmi+xml","UTF-8",rlmiString));
		// add remaining body parts
		for (BodyPart bodyPart : bodyParts.values()) {
			multiPart.getBodyParts().add(bodyPart);
		}
		return multiPart;
	}
	
	private String marshallRlmi(List rlmiList) {
		StringWriter stringWriter = new StringWriter();
		String result = null;
		try {
			rlmiJaxbContext.createMarshaller().marshal(rlmiList, stringWriter);
			result = stringWriter.toString();
		}
		catch (Exception e) {
			logger.error("failed to marshall rlmi content",e);
		}		
		try {
			stringWriter.close();
		} catch (Exception e) {
			logger.error("failed to close stringwriter used to marshall rlmi content",e);
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		return notifier.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			return ((NotificationData)obj).notifier.equals(this.notifier);
		}
		else {
			return false;
		}
	}
	
	// serialization
	
	private static final String[] EMPTY_STRING_ARRAY = {};
	private static final BodyPart[] EMPTY_BODYPART_ARRAY = {};
	private static final Instance[] EMPTY_INSTANCE_ARRAY = {};
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		
		stream.defaultWriteObject();
		
		int arraySize = urisLeft.size();
		String[] stringArray;
		if (arraySize == 0) {
			stringArray = EMPTY_STRING_ARRAY;
		}
		else {
			stringArray = urisLeft.toArray(new String[arraySize]);
		}
		stream.writeObject(stringArray);
		
		final Collection<BodyPart> bodyPartCollection = bodyParts.values();
		arraySize = bodyPartCollection.size();
		BodyPart[] bodyPartArray;
		if (arraySize == 0) {
			bodyPartArray = EMPTY_BODYPART_ARRAY;
		}
		else {
			bodyPartArray = bodyPartCollection.toArray(new BodyPart[arraySize]);
		}
		stream.writeObject(bodyPartArray);
		
		final Collection<Instance> instanceCollection = instances.values();
		arraySize = instanceCollection.size();
		Instance[] instanceArray;
		if (arraySize == 0) {
			instanceArray = EMPTY_INSTANCE_ARRAY;
		}
		else {
			instanceArray = instanceCollection.toArray(new Instance[arraySize]);
		}
		stream.writeObject(instanceArray);
		
	}
	
	private void readObject(ObjectInputStream stream)  throws IOException, ClassNotFoundException {
				
		stream.defaultReadObject();

		String[] stringArray = (String[]) stream.readObject();
		if (stringArray.length == 0) {
			urisLeft = Collections.emptySet();
		}
		else {
			urisLeft = new HashSet<String>();			
		}
		for (String s : stringArray) {
			urisLeft.add(s);
		}		
		
		BodyPart[] bodyPartArray = (BodyPart[]) stream.readObject();
		bodyParts = new HashMap<String, BodyPart>();
		for (BodyPart bodyPart : bodyPartArray) {
			bodyParts.put(bodyPart.getUri(), bodyPart);
		}
		
		Instance[] instanceArray = (Instance[]) stream.readObject();
		instances = new HashMap<String, Instance>();
		for (Instance instance : instanceArray) {
			instances.put(instance.uri, instance);
		}
	}
	
}