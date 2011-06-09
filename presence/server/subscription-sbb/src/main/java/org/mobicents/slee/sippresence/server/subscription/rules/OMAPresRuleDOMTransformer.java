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

package org.mobicents.slee.sippresence.server.subscription.rules;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.mobicents.xdm.common.util.dom.DomUtils;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OMAPresRuleDOMTransformer implements
		OMAPresRuleTransformer<Document> {

	@Override
	public Document transform(Document content, OMAPresRule rule)
			throws InternalServerErrorException {

		if (!rule.hasTransformations()) {
			// nothing to transform
			return content;
		}

		// clone doc
		Document result = null;
		try {
			result = DomUtils.DOCUMENT_BUILDER_NS_AWARE_FACTORY
					.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			throw new InternalServerErrorException(
					"failed to setup DOM builder", e);
		}
		Node presence = result.appendChild(result.importNode(
				content.getDocumentElement(), true));
		NodeList presenceChilds = presence.getChildNodes();
		Node presenceChild = null;
		String nodeName = null;
		boolean keepChild;
		Set<Node> nodesToRemove = null;
		for (int i = 0; i < presenceChilds.getLength(); i++) {

			presenceChild = presenceChilds.item(i);
			keepChild = true;
			if (presenceChild.getNodeType() == Node.ELEMENT_NODE) {
				nodeName = DomUtils.getElementName(presenceChild);
				if ((!rule.isProvideAllDevices() || !rule
						.isProvideAllAttributes()) && nodeName.equals("device")) {
					keepChild = keepDevice(presenceChild, rule);
				} else if ((!rule.isProvideAllPersons() || !rule
						.isProvideAllAttributes()) && nodeName.equals("person")) {
					keepChild = keepPerson(presenceChild, rule);
				} else if ((!rule.isProvideAllServices() || !rule
						.isProvideAllAttributes()) && nodeName.equals("tuple")) {
					keepChild = keepService(presenceChild, rule);
				} else {
					if (!rule.isProvideAllAttributes()) {
						keepChild = keepUnknownAttribute(nodeName,
								presenceChild.getNamespaceURI(), presenceChild,
								rule);
					}
				}

			}
			// TODO consider removing unneeded text nodes and namespaces
			// declared
			if (!keepChild) {
				if (nodesToRemove == null) {
					nodesToRemove = new HashSet<Node>();
				}
				nodesToRemove.add(presenceChild);
			}
		}
		if (nodesToRemove != null) {
			for (Node node : nodesToRemove) {
				node.getParentNode().removeChild(node);
			}
		}

		return result;
	}

	private boolean keepService(Node presenceChild, OMAPresRule rule) {

		boolean keepNode = rule.isProvideAllServices();
		NodeList nodeList = presenceChild.getChildNodes();
		Node node = null;
		String nodeName = null;
		List<Node> otherNodes = null;
		for (int i = 0; i < nodeList.getLength(); i++) {
			node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				nodeName = DomUtils.getElementName(node);
				if (nodeName.equals("class")) {
					if (keepNode) {
						continue;
					}
					if (rule.getProvideServiceClasses() != null
							&& rule.getProvideServiceClasses().contains(
									node.getTextContent().trim())) {
						keepNode = true;
						if (rule.isProvideAllAttributes()) {
							return true;
						}
					}
				} else if (nodeName.equals("contact")) {
					if (keepNode) {
						continue;
					}
					if (rule.getProvideServiceServiceURIs() != null
							&& rule.getProvideServiceServiceURIs().contains(
									node.getTextContent().trim())) {
						keepNode = true;
						if (rule.isProvideAllAttributes()) {
							return true;
						}
						continue;
					} else if (rule.getProvideServiceServiceURISchemes() != null
							&& rule.getProvideServiceServiceURISchemes()
									.contains(
											URI.create(node.getTextContent().trim())
													.getScheme())) {
						keepNode = true;
						if (rule.isProvideAllAttributes()) {
							return true;
						}
					}
				} else if (nodeName.equals("id")) {
					if (keepNode) {
						continue;
					}
					if (rule.getProvideServiceOccurenceIds() != null
							&& rule.getProvideServiceOccurenceIds().contains(
									node.getTextContent().trim())) {
						keepNode = true;
						if (rule.isProvideAllAttributes()) {
							return true;
						}
					}
				} else {
					if (!rule.isProvideAllAttributes()) {
						if (otherNodes == null) {
							otherNodes = new ArrayList<Node>();
						}
						otherNodes.add(node);
					}
				}
			}
		}

		if (!keepNode) {
			return false;
		}

		// filter the other nodes
		if (otherNodes != null) {
			for (Node otherNode : otherNodes) {
				if (!keepServiceAttribute(otherNode, rule)) {
					presenceChild.removeChild(otherNode);
				}
			}
		}

		return true;
	}

	private boolean keepServiceAttribute(Node node, OMAPresRule rule) {

		String nodeName = DomUtils.getElementName(node);
		String nodeNamespaceURI = node.getNamespaceURI();

		/*
		 * The <contact>, <service-class> [9], <basic> status, and <timestamp>
		 * elements in all <tuple> elements, if present, are provided to
		 * watchers.
		 */

		if (nodeNamespaceURI.equals("urn:ietf:params:xml:ns:pidf")) {
			// pidf
			if (nodeName.equals("status")) {
				return true;
			} else if (nodeName.equals("timestamp")) {
				return true;
			}
		} else if (nodeNamespaceURI
				.equals("urn:ietf:params:xml:ns:pidf:data-model")) {
			// data model
			if (nodeName.equals("note")) {
				return rule.isProvideNote();
			}
			else if (nodeName.equals("deviceID")) {
				return rule.isProvideDeviceID();
			}
		} else if (nodeNamespaceURI.equals("urn:ietf:params:xml:ns:pidf:rpid")) {
			// rpid
			if (nodeName.equals("class")) {
				return rule.isProvideClass();			
			} else if (nodeName.equals("privacy")) {
				return rule.isProvidePrivacy();
			} else if (nodeName.equals("relationship")) {
				return rule.isProvideRelationship();
			} else if (nodeName.equals("service-class")) {
				return true;
			} else if (nodeName.equals("status-icon")) {
				return rule.isProvideStatusIcon();
			} else if (nodeName.equals("user-input")) {
				return keepUserInput(node, rule);
			}
		} else if (nodeNamespaceURI.equals("urn:oma:xml:prs:pidf:oma-pres")) {
			// oma
			if (nodeName.equals("willingness")) {
				return rule.isProvideWillingness();
			} else if (nodeName.equals("service-description")) {
				return true;
			} else if (nodeName.equals("barring-state")) {
				return rule.isProvideBarringState();
			} else if (nodeName.equals("registration-state")) {
				return rule.isProvideRegistrationState();
			} else if (nodeName.equals("session-participation")) {
				return rule.isProvideSessionParticipation();
			}
		} else if (nodeNamespaceURI
				.equals("urn:ietf:params:xml:ns:pidf:geopriv10")) {
			// geopriv
			if (nodeName.equals("geopriv")) {
				return rule.getProvideGeopriv() == GeoPrivTransformation.FULL;
			}
		}
		return keepUnknownAttribute(nodeName, nodeNamespaceURI, node, rule);
	}

	private boolean keepUserInput(Node node, OMAPresRule rule) {

		UserInputTransformation uti = rule.getProvideUserInput();

		if (uti == null) {
			return false;
		}

		/*
		 * This permission controls access to the <user-input> element defined
		 * in [9]. The name of the element providing this permission is
		 * <provide-user-input>, and it is an enumerated integer type. Its value
		 * defines what information is provided to watchers in person, device,
		 * or service data elements:
		 * 
		 * false: This value indicates that the <user-input> element is removed
		 * from the document. It is assigned the numeric value of 0.
		 * 
		 * bare: This value indicates that the <user-input> element is to be
		 * retained. However, any "idle-threshold" and "since" attributes are to
		 * be removed. This value is assigned the numeric value of 10.
		 * 
		 * thresholds: This value indicates that the <user-input> element is to
		 * be retained. However, only the "idle-threshold" attribute is to be
		 * retained. This value is assigned the numeric value of 20.
		 * 
		 * full: This value indicates that the <user-input> element is to be
		 * retained, including any attributes. This value is assigned the
		 * numeric value of 30.
		 */

		final Element element = (Element) node;

		switch (uti) {

		case BARE:
			element.removeAttribute("last-input");
			element.removeAttribute("idle-threshold");
			return true;
		case THRESHOLDS:
			element.removeAttribute("last-input");
			return true;
		case FULL:
			return true;
		case FALSE:
		default:
			return false;

		}

	}

	private boolean keepPerson(Node presenceChild, OMAPresRule rule) {

		boolean keepNode = rule.isProvideAllPersons();
		NodeList nodeList = presenceChild.getChildNodes();
		Node node = null;
		String nodeName = null;
		List<Node> otherNodes = null;
		for (int i = 0; i < nodeList.getLength(); i++) {
			node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				nodeName = DomUtils.getElementName(node);
				if (nodeName.equals("class")) {
					if (keepNode) {
						continue;
					}
					if (rule.getProvidePersonClasses() != null
							&& rule.getProvidePersonClasses().contains(
									node.getTextContent().trim())) {
						keepNode = true;
						if (rule.isProvideAllAttributes()) {
							return true;
						}
					}
				} else if (nodeName.equals("id")) {
					if (keepNode) {
						continue;
					}
					if (rule.getProvidePersonOccurenceIds() != null
							&& rule.getProvidePersonOccurenceIds().contains(
									node.getTextContent().trim())) {
						keepNode = true;
						if (rule.isProvideAllAttributes()) {
							return true;
						}
					}
				} else {
					if (!rule.isProvideAllAttributes()) {
						if (otherNodes == null) {
							otherNodes = new ArrayList<Node>();
						}
						otherNodes.add(node);
					}
				}
			}
		}

		if (!keepNode) {
			return false;
		}

		if (otherNodes != null) {
			for (Node otherNode : otherNodes) {
				if (!keepPersonAttribute(otherNode, rule)) {
					presenceChild.removeChild(otherNode);
				}
			}
		}

		return true;
	}

	private boolean keepPersonAttribute(Node node, OMAPresRule rule) {

		String nodeName = DomUtils.getElementName(node);
		String nodeNamespaceURI = node.getNamespaceURI();

		/*
		 * The <timestamp> element in all <person> elements, if present, is
		 * provided to watchers.
		 */

		if (nodeNamespaceURI
				.equals("urn:ietf:params:xml:ns:pidf:data-model")) {
			// data model
			if (nodeName.equals("timestamp")) {
				return true;
			}
			else if (nodeName.equals("note")) {
				return rule.isProvideNote();
			}
		} else if (nodeNamespaceURI.equals("urn:ietf:params:xml:ns:pidf:rpid")) {
			// rpid
			if (nodeName.equals("activities")) {
				return rule.isProvideActivities();
			} else if (nodeName.equals("class")) {
				return rule.isProvideClass();
			} else if (nodeName.equals("mood")) {
				return rule.isProvideMood();
			} else if (nodeName.equals("place-is")) {
				return rule.isProvidePlaceIs();
			} else if (nodeName.equals("place-type")) {
				return rule.isProvidePlaceType();
			} else if (nodeName.equals("privacy")) {
				return rule.isProvidePrivacy();
			} else if (nodeName.equals("sphere")) {
				return rule.isProvideSphere();
			} else if (nodeName.equals("status-icon")) {
				return rule.isProvideStatusIcon();
			} else if (nodeName.equals("time-offset")) {
				return rule.isProvideTimeOffset();
			} else if (nodeName.equals("user-input")) {
				return keepUserInput(node, rule);
			}
		} else if (nodeNamespaceURI.equals("urn:oma:xml:prs:pidf:oma-pres")) {
			// oma
			if (nodeName.equals("overriding-willingness")) {
				return rule.isProvideWillingness();
			}
		} else if (nodeNamespaceURI
				.equals("urn:ietf:params:xml:ns:pidf:geopriv10")) {
			// geopriv
			if (nodeName.equals("geopriv")) {
				return rule.getProvideGeopriv() == GeoPrivTransformation.FULL;
			}
		}
		return keepUnknownAttribute(nodeName, nodeNamespaceURI, node, rule);
	}

	private boolean keepDevice(Node presenceChild, OMAPresRule rule) {

		boolean keepNode = rule.isProvideAllDevices();
		NodeList nodeList = presenceChild.getChildNodes();
		Node node = null;
		String nodeName = null;
		List<Node> otherNodes = null;
		for (int i = 0; i < nodeList.getLength(); i++) {
			node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				nodeName = DomUtils.getElementName(node);
				if (nodeName.equals("class")) {
					if (keepNode) {
						continue;
					}
					if (rule.getProvideDeviceClasses() != null
							&& rule.getProvideDeviceClasses().contains(
									node.getTextContent().trim())) {
						keepNode = true;
						if (rule.isProvideAllAttributes()) {
							return true;
						}
					}
				} else if (nodeName.equals("deviceID")) {
					if (keepNode) {
						continue;
					}
					if (rule.getProvideDeviceDeviceIDs() != null
							&& rule.getProvideDeviceDeviceIDs().contains(
									node.getTextContent().trim())) {
						keepNode = true;
						if (rule.isProvideAllAttributes()) {
							return true;
						}
					}
				} else if (nodeName.equals("id")) {
					if (keepNode) {
						continue;
					}
					if (rule.getProvideDeviceOccurenceIds() != null
							&& rule.getProvideDeviceOccurenceIds().contains(
									node.getTextContent().trim())) {
						keepNode = true;
						if (rule.isProvideAllAttributes()) {
							return true;
						}
					}
				} else {
					if (!rule.isProvideAllAttributes()) {
						if (otherNodes == null) {
							otherNodes = new ArrayList<Node>();
						}
						otherNodes.add(node);
					}
				}
			}
		}

		if (!keepNode) {
			return false;
		}

		if (otherNodes != null) {
			for (Node otherNode : otherNodes) {
				if (!keepDeviceAttribute(otherNode, rule)) {
					presenceChild.removeChild(otherNode);
				}
			}
		}

		return true;
	}

	private boolean keepDeviceAttribute(Node node, OMAPresRule rule) {

		String nodeName = DomUtils.getElementName(node);
		String nodeNamespaceURI = node.getNamespaceURI();

		/*
		 * The <timestamp> and <deviceID> elements in all <device> elements, if
		 * present, are provided to all watchers.
		 */
		if (nodeNamespaceURI
				.equals("urn:ietf:params:xml:ns:pidf:data-model")) {
			// data model
			if (nodeName.equals("timestamp")) {
				return true;
			}
			else if (nodeName.equals("deviceID")) {
				return true;
			} else if (nodeName.equals("note")) {
				return rule.isProvideNote();
			}
		} else if (nodeNamespaceURI.equals("urn:ietf:params:xml:ns:pidf:rpid")) {
			// rpid
			if (nodeName.equals("class")) {
				return rule.isProvideClass();
			} else if (nodeName.equals("user-input")) {
				return keepUserInput(node, rule);
			}
		} else if (nodeNamespaceURI.equals("urn:oma:xml:prs:pidf:oma-pres")) {
			// oma
			if (nodeName.equals("network-availability")) {
				return rule.isProvideNetworkAvailability();
			}
		} else if (nodeNamespaceURI
				.equals("urn:ietf:params:xml:ns:pidf:geopriv10")) {
			// geopriv
			if (nodeName.equals("geopriv")) {
				return rule.getProvideGeopriv() == GeoPrivTransformation.FULL;
			}
		}
		return keepUnknownAttribute(nodeName, nodeNamespaceURI, node, rule);
	}

	private boolean keepUnknownAttribute(String nodeName,
			String nodeNamespaceURI, Node node, OMAPresRule rule) {
		// look for a name + namespace match in rule
		if (rule.getUnknownBooleanAttributes() != null) {
			for (UnknownBooleanAttributeTransformation ubat : rule
					.getUnknownBooleanAttributes()) {
				if (!ubat.getName().equals(nodeName)) {
					continue;
				}
				if (nodeNamespaceURI == null) {
					if (ubat.getNamespace() != null) {
						continue;
					}
				} else if (!nodeNamespaceURI.equals(ubat.getNamespace())) {
					continue;
				}
				// match, process childs
				// FIXME this sounds nonsense
				/*NodeList nodeList = node.getChildNodes();
				Node childNode = null;
				for (int i = 0; i < nodeList.getLength(); i++) {
					childNode = nodeList.item(i);
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						if (!keepUnknownAttribute(
								DomUtils.getElementName(childNode),
								childNode.getNamespaceURI(), childNode, rule)) {
							node.removeChild(childNode);
						}
					}
				}*/
				// provide node
				return true;
			}
		}
		// no match, don't provide node
		return false;
	}

}
