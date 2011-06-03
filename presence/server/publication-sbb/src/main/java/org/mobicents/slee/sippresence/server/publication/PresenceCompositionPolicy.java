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

package org.mobicents.slee.sippresence.server.publication;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;

import org.mobicents.slee.sipevent.server.publication.StateComposer;
import org.mobicents.xdm.common.util.dom.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PresenceCompositionPolicy implements StateComposer {
	
	private static final Random RANDOM = new Random();

	private static String generateNCName() {
		// note: any hex string is a valid NCName if does not starts with a number
		return new StringBuilder("a").append(Integer.toHexString(RANDOM.nextInt())).toString();
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.StateComposer#compose(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Document compose(Document document, Document otherDocument) {
		if (document == null) {
			return otherDocument;
		}
		if (otherDocument == null) {
			return document;
		}
		
		final PresenceElementData presenceData = new PresenceElementData(document.getDocumentElement());
		final PresenceElementData otherPresenceData = new PresenceElementData(otherDocument.getDocumentElement());
		
		Document newDocument = null;
		try {
			newDocument = DomUtils.DOCUMENT_BUILDER_NS_AWARE_FACTORY.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		Element newPresence = newDocument.createElementNS("urn:ietf:params:xml:ns:pidf", "presence");
		newDocument.appendChild(newPresence);
		newPresence.setAttribute("entity", presenceData.entity);
		
		for (Node n : composeTuples(presenceData.tuples, otherPresenceData.tuples, newDocument)) {
			newPresence.appendChild(n);
		}
		for (Node n : composeDevices(presenceData.devices, otherPresenceData.devices, newDocument)) {
			newPresence.appendChild(n);
		}
		for (Node n : composePersons(presenceData.persons, otherPresenceData.persons, newDocument)) {
			newPresence.appendChild(n);
		}
		for (Node n : composeAny(presenceData.any, otherPresenceData.any, true,false,false, newDocument)) {
			newPresence.appendChild(n);
		}
		for (Node n : composeNotes(presenceData.notes, otherPresenceData.notes, newDocument)) {
			newPresence.appendChild(n);
		}
		
		return newDocument;
	}

	private List<Element> composeTuples(List<Element> tuples, List<Element> otherTuples, Document newDocument) {
		
		List<Element> result = new ArrayList<Element>();
		
		// process all from tuples trying to match each with other tuples
		for (Iterator<Element> tuplesIt = tuples.iterator(); tuplesIt.hasNext(); ) {
			Element tuple = tuplesIt.next();
			for (Iterator<Element> otherTuplesIt = otherTuples.iterator(); otherTuplesIt.hasNext(); ) {
				Element otherTuple = otherTuplesIt.next();
				Element compositionTuple = composeTuple(tuple, otherTuple, newDocument);
				if (compositionTuple != null) {
					// the composition has a result
					result.add(compositionTuple);
					// remove the tuples to not be iterated again
					tuplesIt.remove();
					otherTuplesIt.remove();
					break;
				}
			}
		}
		
		// now add the ones left but replacing the ids
		Element e = null;
		for (Element tuple : tuples) {
			e = (Element) newDocument.importNode(tuple, true);
			e.setAttribute("id", generateNCName());
			result.add(e);
		}
		for (Element tuple : otherTuples) {
			e = (Element) newDocument.importNode(tuple, true);
			e.setAttribute("id", generateNCName());
			result.add(e);
		}		
		
		return result;
	}
	
	private Element composeTuple(Element tuple, Element otherTuple, Document newDocument) {
		
		Element result = newDocument.createElement("tuple");
		
		final TupleElementData tupleElementData = new TupleElementData(tuple);
		final TupleElementData otherTupleElementData = new TupleElementData(otherTuple);
		
		/*
		 * 
		 * Service elements (defined in section 10.1.2) If the following
		 * conditions all apply:
		 * 
		 * a. If one <tuple> element includes a <contact> element, as
		 * defined in [RFC3863], other <tuple> elements include an
		 * identical <contact> element; and
		 */
		if (tupleElementData.contact == null) {
			if (otherTupleElementData.contact != null) {
				// different contacts
				return null;
			}
			// else ignore no contacts
		}
		else {
			if (otherTupleElementData.contact == null) {
				// different contacts
				return null;
			}
			else {
				Element composedContact = this.composeContact(tupleElementData.contact, otherTupleElementData.contact,newDocument);
				if (composedContact != null) {
					result.appendChild(composedContact);
				}
				else {
					return null;
				}
			}
		}
		
		/* b. If one <tuple> element includes a <service-description>
		 * element, as defined in section 10.5.1, other <tuple> elements
		 * include an identical <service-description> element. Two
		 * <service-description> elements are identical if they contain
		 * identical <service-id> and <version> elements; and
		 */
		if (tupleElementData.serviceDescription == null) {
			if (otherTupleElementData.serviceDescription != null) {
				// different serviceDescription
				return null;
			}
			// else ignore no serviceDescription
		}
		else {
			if (otherTupleElementData.serviceDescription == null) {
				// different serviceDescription
				return null;
			}
			else {
				Node composedServiceDescription = this.composeServiceDescription(tupleElementData.serviceDescription, otherTupleElementData.serviceDescription,newDocument);
				if (composedServiceDescription != null) {
					result.appendChild(composedServiceDescription);
				}
				else {
					return null;
				}
			}
		}		
		
		/* c. If one <tuple> element includes a <class> element, as
		 * defined in section 10.5.1, other <tuple> elements include an
		 * identical <class> element; and
		 * 
		 * d. If there are no conflicting elements (i.e. same elements
		 * with different values) or attributes under the <tuple>
		 * elements. Different <timestamp> values are not considered as
		 * a conflict.
		 *
		 * then the PS SHALL:
		 * 
		 * a. Aggregate elements within a <tuple> element that are
		 * published from different Presence Sources into one <tuple>
		 * element. Identical elements with the same value and
		 * attributes SHALL not be duplicated; and
		 * 
		 * b. Set the <priority> attribute of the <contact> element in
		 * the aggregated <tuple> element to the highest one among those
		 * in the input <tuple> elements, if any <priority> attribute is
		 * present; and
		 * 
		 * c. Set the <timestamp> of the aggregated <tuple> to the most
		 * recent one among the ones that contribute to the aggregation;
		 * and
		 * 
		 * d. Keep no more than one <description> element from the
		 * <service-description> elements of the aggregated <tuple>
		 * element when there are different values of the <description>
		 * elements. 
		 */

		// process status
		if (tupleElementData.status == null) {
			if (otherTupleElementData.status != null) {
				// different status
				return null;
			}
			// else ignore no status
		}
		else {
			if (otherTupleElementData.status == null) {
				// different status
				return null;
			}
			else {
				Element status = this.composeStatus(tupleElementData.status, otherTupleElementData.status, newDocument);
				if (status != null) {
					result.appendChild(status);
				}
				else {
					return null;
				}
			}
		}

		// process anys
		List<Node> anys = composeAny(tupleElementData.any, otherTupleElementData.any, false,false,false, newDocument);
		if (anys != null) {
			for (Node any : anys) {
				result.appendChild(any);
			}			
		}
		else {
			return null;
		}

		// process notes
		List<Node> notes = composeNotes(tupleElementData.notes, otherTupleElementData.notes, newDocument);
		if (notes != null) {
			for (Node note : notes) {
				result.appendChild(note);
			}			
		}
				
		// process timestamp
		if (tupleElementData.timestamp == null) {
			if (otherTupleElementData.timestamp != null) {
				result.appendChild(newDocument.importNode(otherTupleElementData.timestamp, true));
			}			
		}
		else {
			if (otherTupleElementData.timestamp == null) {
				result.appendChild(newDocument.importNode(tupleElementData.timestamp, true));
			}
			else {
				XMLGregorianCalendar timestamp = null;
				XMLGregorianCalendar otherTimestamp = null;
				try {
					timestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(tupleElementData.timestamp.getTextContent());
					otherTimestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(otherTupleElementData.timestamp.getTextContent());
				} catch (Exception e) {
					e.printStackTrace();
				}				
				// need to compare
				if (timestamp.compare(otherTimestamp) > 0) {
					result.appendChild(newDocument.importNode(tupleElementData.timestamp, true));
				}
				else {
					result.appendChild(newDocument.importNode(otherTupleElementData.timestamp, true));
				}
			}
		}
 
		result.setAttribute("id", generateNCName());
		return result;
	}
	
	private Node composeServiceDescription(Element serviceDescription,
			Element otherServiceDescription, Document newDocument) {
		
		String elementServiceId = null;
		String elementVersion = null; 
		NodeList nodeList = serviceDescription.getChildNodes();
		Node node = null;
		for (int i=0; i<nodeList.getLength();i++) {
			node = nodeList.item(i);
			if (DomUtils.isElementNamed(node, "service-id")) {
				elementServiceId = ((Element)node).getTextContent();
			}
			else if (DomUtils.isElementNamed(node, "version")) {
				elementVersion = ((Element)node).getTextContent();
			}
		}
		String otherElementServiceId = null;
		String otherElementVersion = null;
		nodeList = otherServiceDescription.getChildNodes();
		for (int i=0; i<nodeList.getLength();i++) {
			node = nodeList.item(i);
			if (DomUtils.isElementNamed(node, "service-id")) {
				otherElementServiceId = ((Element)node).getTextContent();
			}
			else if (DomUtils.isElementNamed(node, "version")) {
				otherElementVersion = ((Element)node).getTextContent();
			}
		} 
		
		if (elementServiceId == null) {
			if (otherElementServiceId != null)
				return null;
		} else if (!elementServiceId.equals(otherElementServiceId))
			return null;
		
		if (elementVersion == null) {
			if (otherElementVersion != null)
				return null;
		} else if (!elementVersion.equals(otherElementVersion))
			return null;
		
		// service id and version matches, use one of the nodes 
		return newDocument.importNode(serviceDescription, true);
		
	}

	/**
	 * Compose a contact element from two non null ones.
	 * @param contact
	 * @param otherContact
	 * @return
	 */
	private Element composeContact(Element contact, Element otherContact, Document newDocument) {
		
		Element result = newDocument.createElement("contact");
		
		// compare values
		if (contact.getTextContent().equals(otherContact.getTextContent())) {
			result.setTextContent(contact.getTextContent());			
		}
		else {
			return null;
		}
		
		// process priority
		BigDecimal contactPriority = null;
		String s = contact.getAttribute("priority");
		if (s != null) {
			s = s.trim();
			if (!s.equals("")) {
				contactPriority = new BigDecimal(s);
			}
		}
		BigDecimal otherContactPriority = null;
		s = otherContact.getAttribute("priority");
		if (s != null) {
			s = s.trim();
			if (!s.equals("")) {
				otherContactPriority = new BigDecimal(s);
			}
		}
		if (contactPriority != null) {
			if (otherContactPriority == null || otherContactPriority.compareTo(contactPriority) < 0) {
				result.appendChild(newDocument.importNode(contact, true));				
			}			
			else {
				result.appendChild(newDocument.importNode(otherContact, true));
			}
		}
		else {
			if (otherContactPriority != null) {
				result.appendChild(newDocument.importNode(otherContact, true));
			}
		}		
		return result;
	}
	
	private Element composeStatus(Element status, Element otherStatus, Document newDocument) {
		
		Element result =  newDocument.createElement("status");
		
		final StatusElementData statusElementData = new StatusElementData(status);
		final StatusElementData otherStatusElementData = new StatusElementData(otherStatus);
		
		// check basic
		if (statusElementData.basic != null) {
			if (otherStatusElementData.basic != null) {
				if (statusElementData.basic.getTextContent().equals(otherStatusElementData.basic.getTextContent())) {
					result.appendChild(newDocument.importNode(statusElementData.basic,true));
				}
				else {
					// no match
					return null;
				}
			}
			else {
				// no match
				return null;
			}
		}
		else {
			if (otherStatusElementData.basic != null) {
				// no match
				return null;
			}
			else {
				// ignore
			}
		}
			
		// lets process the anys
		List<Node> anys = composeAny(statusElementData.any, otherStatusElementData.any, false,false,false, newDocument);
		if (anys != null) {
			if (result == null) {
				result = newDocument.createElement("status");
			}
			for (Node any : anys) {
				result.appendChild(any);
			}
		}
		else {
			return null;
		}

		return result;
	}
	
	private List<Node> composeAny(List<Element> anys, List<Element> otherAnys, boolean allowConflicts, boolean keepRecentInConflict, boolean anysIsOlder, Document newDocument) {

		ArrayList<Node> result = new ArrayList<Node>();
		for (Iterator<Element> anysIt = anys.iterator(); anysIt.hasNext(); ) {
			Element anysElement = anysIt.next();
			for (Iterator<Element> otherAnysIt = otherAnys.iterator(); otherAnysIt.hasNext(); ) {
				Element otherAnysElement = otherAnysIt.next();
				if (DomUtils.getElementName(anysElement).equals(DomUtils.getElementName(otherAnysElement))) {
					// same element name
					if (anysElement.getNamespaceURI() != null) {
						if (!anysElement.getNamespaceURI().equals(otherAnysElement.getNamespaceURI())) {
							continue;
						}
					}
					else {
						if (otherAnysElement.getNamespaceURI() != null) {
							continue;
						}
					}
					// same namespace
					// check for conflict
					if (isConflict(anysElement, otherAnysElement)) {
						if (allowConflicts) {
							if (keepRecentInConflict) {
								if (anysIsOlder) {
									result.add(newDocument.importNode(otherAnysElement,true));											
								}
								else {
									result.add(newDocument.importNode(anysElement,true));											
								}
								anysIt.remove();
								otherAnysIt.remove();
								break;
							}									
						}
						else {
							return null;
						}
					}
					else {
						if (anysIsOlder) {
							result.add(newDocument.importNode(otherAnysElement,true));											
						}
						else {
							result.add(newDocument.importNode(anysElement,true));											
						}
						anysIt.remove();
						otherAnysIt.remove();
						break;
					}
				}
			}
		}

		// now add the ones left 
		for (Element e : anys) {
			result.add(newDocument.importNode(e,true));											
		}
		for (Element e : otherAnys) {
			result.add(newDocument.importNode(e,true));											
		}
		
		return result;
	}

	private boolean isConflict(Element element, Element otherElement) {
		return !element.isEqualNode(otherElement);
	}
	
	private List<Node> composeNotes(List<Element> notes,List<Element> otherNotes, Document newDocument) {
		ArrayList<Node> result = new ArrayList<Node>();
		// process all from notes trying to match each with otherNote
		for (Iterator<Element> notesIt = notes.iterator(); notesIt.hasNext(); ) {
			Element note = notesIt.next();
			for (Iterator<Element> otherNotesIt = otherNotes.iterator(); otherNotesIt.hasNext(); ) {
				Element otherNote = otherNotesIt.next();
				if (note.isEqualNode(otherNote)) {
					result.add(newDocument.importNode(note, true));
					notesIt.remove();
					otherNotesIt.remove();
					break;
				}
			}
		}
		// now add the ones left
		for (Element note : notes) {
			result.add(newDocument.importNode(note, true));
		}
		for (Element note : otherNotes) {
			result.add(newDocument.importNode(note, true));
		}
		return result;
	}
	
	private List<Element> composeDevices(
			List<Element> devices,
			List<Element> otherDevices, Document newDocument) {
				
		ArrayList<Element> result = new ArrayList<Element>();

		for (Iterator<Element> it = devices.iterator(); it.hasNext(); ) {
			Element device = it.next();
			for (Iterator<Element> otherIt = otherDevices.iterator(); otherIt.hasNext(); ) {
				Element otherDevice = otherIt.next();
				Element compositionDevice = composeDevice(device, otherDevice, newDocument);
				if (compositionDevice != null) {
					// the composition has a result
					result.add(compositionDevice);
					// remove both to not be iterated again
					it.remove();
					otherIt.remove();
					break;
				}
			}
		}
		// now add the ones left but replacing the ids
		Element e = null;
		for (Element device : devices) {
			e = (Element) newDocument.importNode(device, true);
			e.setAttribute("id",generateNCName());
			result.add(e);
		}
		for (Element device : otherDevices) {
			e = (Element) newDocument.importNode(device, true);
			e.setAttribute("id",generateNCName());
			result.add(e);
		}
		return result;
		
	}

	private Element composeDevice(Element device,
			Element otherDevice, Document newDocument) {
		
		/*
		 * If the <deviceID> of the <device> elements that are published from
		 * different Presence Sources match
		 * 
		 * then the PS SHALL
		 * 
		 * a. Aggregate the non-conflicting elements within one <device>
		 * element; and
		 * 
		 * b. Set the <timestamp> of the aggregated <device> element to the most
		 * recent one among the ones that contribute to the aggregation; and
		 * 
		 * c. Use the element from the most recent publication for conflicting
		 * elements.
		 * 
		 */
		final DeviceElementData deviceElementData = new DeviceElementData(device);
		final DeviceElementData otherDeviceElementData = new DeviceElementData(otherDevice);
		
		if (deviceElementData.deviceID.getTextContent().equals(otherDeviceElementData.deviceID.getTextContent())) {
			
			Element result = newDocument.createElement("device");
			result.appendChild(newDocument.importNode(deviceElementData.deviceID, true));
			
			// process timestamp
			boolean deviceIsOlder = false;
			if (deviceElementData.timestamp == null) {
				if (otherDeviceElementData.timestamp != null) {
					result.appendChild(newDocument.importNode(otherDeviceElementData.timestamp, true));
					deviceIsOlder = true;
				}				
			}
			else {
				if (otherDeviceElementData.timestamp == null) {
					result.appendChild(newDocument.importNode(deviceElementData.timestamp, true));
				}
				else {
					XMLGregorianCalendar timestamp = null;
					XMLGregorianCalendar otherTimestamp = null;
					try {
						timestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(deviceElementData.timestamp.getTextContent());
						otherTimestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(otherDeviceElementData.timestamp.getTextContent());
					} catch (Exception e) {
						e.printStackTrace();
					}				
					// need to compare
					if (timestamp.compare(otherTimestamp) > 0) {
						result.appendChild(newDocument.importNode(deviceElementData.timestamp, true));
					}
					else {
						result.appendChild(newDocument.importNode(otherDeviceElementData.timestamp, true));
						deviceIsOlder = true;
					}
				}
			}
			
			// process anys
			List<Node> anys = composeAny(deviceElementData.any, otherDeviceElementData.any, true,true,deviceIsOlder, newDocument);
			if (anys != null) {
				for (Node any : anys) {
					result.appendChild(any);
				}			
			}

			// process notes
			List<Node> notes = composeNotes(deviceElementData.notes, otherDeviceElementData.notes, newDocument);
			if (notes != null) {
				for (Node note : notes) {
					result.appendChild(note);
				}			
			}
					
			result.setAttribute("id", generateNCName());
			return result;			
		}
		else {
			return null;
		}
	}
	
	private List<Element> composePersons(
			List<Element> persons,
			List<Element> otherPersons, Document newDocument) {
		
		ArrayList<Element> result = new ArrayList<Element>();

		for (Iterator<Element> it = persons.iterator(); it.hasNext(); ) {
			Element person = it.next();
			for (Iterator<Element> otherIt = otherPersons.iterator(); otherIt.hasNext(); ) {
				Element otherPerson = otherIt.next();
				Element compositionPerson = composePerson(person, otherPerson, newDocument);
				if (compositionPerson != null) {
					// the composition has a result
					result.add(compositionPerson);
					// remove both to not be iterated again
					it.remove();
					otherIt.remove();
					break;
				}
			}
		}
		// now add the ones left but replacing the ids
		Element e = null;
		for (Element person : persons) {
			e = (Element) newDocument.importNode(person, true);
			e.setAttribute("id",generateNCName());
			result.add(e);
		}
		for (Element person : otherPersons) {
			e = (Element) newDocument.importNode(person, true);
			e.setAttribute("id",generateNCName());
			result.add(e);
		}
		return result;
	}
	
	private Element composePerson(Element person,
			Element otherPerson, Document newDocument) {
		
		/*
		 * If the following conditions all apply:
		 * 
		 * a. If one <person> element includes a <class> element, as defined in
		 * section 10.5.1, other <person> elements include an identical <class>
		 * element; and
		 * 
		 * b. If there are no conflicting elements (same elements with different
		 * values or attributes) under the <person> elements. Different
		 * <timestamp> values are not considered as a conflict.
		 * 
		 * then the PS SHALL:
		 * 
		 * a. Aggregate elements within a <person> element that are published
		 * from different Presence Sources into one <person> element. Identical
		 * elements with the same value SHALL not be duplicated.
		 * 
		 * b. Set the <timestamp> of the aggregated <person> element to the most
		 * recent one among the ones that contribute to the aggregation. In any
		 * other case, the PS SHALL keep <person> elements from different
		 * Presence Sources separate.
		 */
		Element result = newDocument.createElement("person");
		
		final PersonElementData personElementData = new PersonElementData(person);
		final PersonElementData otherPersonElementData = new PersonElementData(otherPerson);
		
		// process anys
		List<Node> anys = composeAny(personElementData.any, otherPersonElementData.any, false,false,false, newDocument);
		if (anys != null) {
			for (Node any : anys) {
				result.appendChild(any);
			}			
		}
		else {
			return null;
		}

		// process notes
		List<Node> notes = composeNotes(personElementData.notes, otherPersonElementData.notes, newDocument);
		if (notes != null) {
			for (Node note : notes) {
				result.appendChild(note);
			}			
		}
				
		// process timestamp
		if (personElementData.timestamp == null) {
			if (otherPersonElementData.timestamp != null) {
				result.appendChild(newDocument.importNode(otherPersonElementData.timestamp, true));
			}			
		}
		else {
			if (otherPersonElementData.timestamp == null) {
				result.appendChild(newDocument.importNode(personElementData.timestamp, true));
			}
			else {
				XMLGregorianCalendar timestamp = null;
				XMLGregorianCalendar otherTimestamp = null;
				try {
					timestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(personElementData.timestamp.getTextContent());
					otherTimestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(otherPersonElementData.timestamp.getTextContent());
				} catch (Exception e) {
					e.printStackTrace();
				}				
				// need to compare
				if (timestamp.compare(otherTimestamp) > 0) {
					result.appendChild(newDocument.importNode(personElementData.timestamp, true));
				}
				else {
					result.appendChild(newDocument.importNode(otherPersonElementData.timestamp, true));
				}
			}
		}
 
		result.setAttribute("id", generateNCName());
		return result;
	}

	// --- temp element data structures
	
	private static final List<Element> EMPTY_LIST = Collections.emptyList();
	
	private static class PresenceElementData {	
		
		List<Element> tuples;
		List<Element> devices;
		List<Element> persons;
		List<Element> any;
		List<Element> notes;
		String entity;
		
		PresenceElementData(Element element) {
			entity = element.getAttribute("entity");
			NodeList childs = element.getChildNodes();
			Node child = null;
			for(int i=0;i<childs.getLength();i++) {
				child = childs.item(i);
				if(DomUtils.isElementNamed(child, "tuple")) {
					if(tuples == null) {
						tuples = new ArrayList<Element>();
					}
					tuples.add((Element) child);
				}
				else if(DomUtils.isElementNamed(child, "device")) {
					if(devices == null) {
						devices = new ArrayList<Element>();
					}
					devices.add((Element) child);
				}
				else if(DomUtils.isElementNamed(child, "person")) {
					if(persons == null) {
						persons = new ArrayList<Element>();
					}
					persons.add((Element) child);
				}
				else if(DomUtils.isElementNamed(child, "note")) {
					if(notes == null) {
						notes = new ArrayList<Element>();
					}
					notes.add((Element) child);
				}
				else if (child.getNodeType() == Node.ELEMENT_NODE) {
					if(any == null) {
						any = new ArrayList<Element>();
					}
					any.add((Element) child);
				}
			}
			if (any == null) {
				any = EMPTY_LIST;
			}
			if (devices == null) {
				devices = EMPTY_LIST;
			}
			if (notes == null) {
				notes = EMPTY_LIST;
			}
			if (persons == null) {
				persons = EMPTY_LIST;
			}
			if (tuples == null) {
				tuples = EMPTY_LIST;
			}			
		}		
	}
	
	private static class TupleElementData {	
		
		Element serviceDescription;
		Element contact;
		Element status;
		Element timestamp;
		List<Element> any;
		List<Element> notes;
		
		TupleElementData(Element element) {

			NodeList childs = element.getChildNodes();
			Node child = null;
			for(int i=0;i<childs.getLength();i++) {
				child = childs.item(i);
				if(DomUtils.isElementNamed(child, "contact")) {
					contact = (Element) child;
				}
				else if(DomUtils.isElementNamed(child, "service-description")) {
					serviceDescription = (Element) child;
				}
				else if(DomUtils.isElementNamed(child, "status")) {
					status = (Element) child;
				}
				else if(DomUtils.isElementNamed(child, "timestamp")) {
					timestamp = (Element) child;
				}
				else if(DomUtils.isElementNamed(child, "note")) {
					if(notes == null) {
						notes = new ArrayList<Element>();
					}
					notes.add((Element) child);
				}
				else if (child.getNodeType() == Node.ELEMENT_NODE) {
					if(any == null) {
						any = new ArrayList<Element>();
					}
					any.add((Element) child);
				}
			}
			if (any == null) {
				any = EMPTY_LIST;
			}			
			if (notes == null) {
				notes = EMPTY_LIST;
			}						
		}		
	}
	
	private static class StatusElementData {	
		
		Element basic;
		List<Element> any;
		
		StatusElementData(Element element) {
			NodeList childs = element.getChildNodes();
			Node child = null;
			for(int i=0;i<childs.getLength();i++) {
				child = childs.item(i);
				if(DomUtils.isElementNamed(child, "basic")) {
					basic = (Element) child;
				}				
				else if (child.getNodeType() == Node.ELEMENT_NODE) {
					if(any == null) {
						any = new ArrayList<Element>();
					}
					any.add((Element) child);
				}
			}
			if (any == null) {
				any = EMPTY_LIST;
			}											
		}		
	}
	
	private static class DeviceElementData {	
		
		
		Element deviceID;
		Element timestamp;
		List<Element> any;
		List<Element> notes;
		
		DeviceElementData(Element element) {

			NodeList childs = element.getChildNodes();
			Node child = null;
			for(int i=0;i<childs.getLength();i++) {
				child = childs.item(i);
				if(DomUtils.isElementNamed(child, "deviceID")) {
					deviceID = (Element) child;
				}
				else if(DomUtils.isElementNamed(child, "timestamp")) {
					timestamp = (Element) child;
				}
				else if(DomUtils.isElementNamed(child, "note")) {
					if(notes == null) {
						notes = new ArrayList<Element>();
					}
					notes.add((Element) child);
				}
				else if (child.getNodeType() == Node.ELEMENT_NODE) {
					if(any == null) {
						any = new ArrayList<Element>();
					}
					any.add((Element) child);
				}
			}
			if (any == null) {
				any = EMPTY_LIST;
			}			
			if (notes == null) {
				notes = EMPTY_LIST;
			}						
		}		
	}

	private static class PersonElementData {	
			
		Element timestamp;
		List<Element> any;
		List<Element> notes;
		
		PersonElementData(Element element) {
			
			NodeList childs = element.getChildNodes();
			Node child = null;
			for(int i=0;i<childs.getLength();i++) {
				child = childs.item(i);
				if(DomUtils.isElementNamed(child, "timestamp")) {
					timestamp = (Element) child;
				}
				else if(DomUtils.isElementNamed(child, "note")) {
					if(notes == null) {
						notes = new ArrayList<Element>();
					}
					notes.add((Element) child);
				}
				else if (child.getNodeType() == Node.ELEMENT_NODE) {
					if(any == null) {
						any = new ArrayList<Element>();
					}
					any.add((Element) child);
				}
			}
			if (any == null) {
				any = EMPTY_LIST;
			}			
			if (notes == null) {
				notes = EMPTY_LIST;
			}						
		}		
	}

}
