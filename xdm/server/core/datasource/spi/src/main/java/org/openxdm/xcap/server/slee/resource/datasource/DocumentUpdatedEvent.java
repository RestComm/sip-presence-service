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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.slee.EventTypeID;
import javax.xml.XMLConstants;

import org.mobicents.protocols.xcap.diff.BuildPatchException;
import org.mobicents.protocols.xcap.diff.dom.DOMXcapDiffFactory;
import org.mobicents.protocols.xcap.diff.dom.utils.DOMNodeComparator;
import org.mobicents.slee.xdm.server.ServerConfiguration;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.NodeSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DocumentUpdatedEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * IMPORTANT: must sync with the event descriptor data!!!!
	 */
	public static final EventTypeID EVENT_TYPE_ID = new EventTypeID(
			"DocumentUpdatedEvent", "org.openxdm", "1.0");

	// factory to build xcap diff patches
	protected static final DOMXcapDiffFactory XCAP_DIFF_FACTORY = new DOMXcapDiffFactory();
	// the server config
	protected static final ServerConfiguration XDM_SERVER_CONFIGURATION = ServerConfiguration
			.getInstance();

	// xcap diff patches
	private transient Document docXcapDiffWithPatch;
	private transient Document docXcapDiffWithoutPatch;

	private transient Map<NodeSelector, Document> nodeXcapDiff;
	private transient Set<NodeSelector> nodesNotUpdated;
	// the default doc namespace is essential for node patch generation
	protected final String defaultDocNamespace;

	// resource update data
	protected final DocumentSelector documentSelector;
	protected final org.openxdm.xcap.common.datasource.Document oldDocument;
	protected final Document newDocumentDOM;
	protected final String newDocumentString;
	protected final String newETag;

	public DocumentUpdatedEvent(DocumentSelector documentSelector,
			String defaultDocNamespace,
			org.openxdm.xcap.common.datasource.Document oldDocument,
			Document newDocumentDOM, String newDocumentString, String newETag) {
		this.defaultDocNamespace = defaultDocNamespace;
		this.documentSelector = documentSelector;
		this.oldDocument = oldDocument;
		this.newDocumentDOM = newDocumentDOM;
		this.newDocumentString = newDocumentString;
		this.newETag = newETag;
	}

	public String getDefaultDocNamespace() {
		return defaultDocNamespace;
	}

	public DocumentSelector getDocumentSelector() {
		return documentSelector;
	}

	public Document getNewDocument() {
		return newDocumentDOM;
	}

	public String getNewDocumentString() {
		return newDocumentString;
	}

	public String getNewETag() {
		return newETag;
	}

	public org.openxdm.xcap.common.datasource.Document getOldDocument() {
		return oldDocument;
	}

	public String getPreviousETag() {
		return oldDocument != null ? oldDocument.getETag() : null;
	}

	/**
	 * Retrieves the doc xcap diff, possibly with a patch, regarding the
	 * resource update.
	 * 
	 * @param mayIncludePatch
	 * @return
	 * @throws BuildPatchException
	 */
	public Document getDocXcapDiff(boolean mayIncludePatch)
			throws BuildPatchException {
		if (mayIncludePatch) {
			// may include patch
			if (docXcapDiffWithPatch == null) {
				docXcapDiffWithPatch = createDocXcapDiff(true);
			}
			return docXcapDiffWithPatch;
		} else {
			// may not include patch
			if (docXcapDiffWithoutPatch == null) {
				docXcapDiffWithoutPatch = createDocXcapDiff(false);
			}
			return docXcapDiffWithoutPatch;
		}
	}

	/**
	 * Retrieves the xcap diff patch for the specified node in the document.
	 * 
	 * Beware that the subscription document selector is not validated against
	 * the one in the event.
	 * 
	 * @param nodeSubscription
	 * @return null if the node was not changed
	 * @throws BuildPatchException
	 */
	public Document getNodeXcapDiff(NodeSubscription nodeSubscription)
			throws BuildPatchException {
		// lazy init of node patches
		if (nodeXcapDiff == null) {
			nodeXcapDiff = new HashMap<NodeSelector, Document>();
		}
		NodeSelector nodeSelector = nodeSubscription.getNodeSelector();
		// check if patch was previously generated
		Document nodePatch = nodeXcapDiff.get(nodeSelector);
		if (nodePatch == null
				&& (nodesNotUpdated == null || !nodesNotUpdated
						.contains(nodeSelector))) {
			// patch not generated
			nodePatch = createNodeXcapDiff(nodeSubscription);
			if (nodePatch != null) {
				// got a patch, save it
				nodeXcapDiff.put(nodeSelector, nodePatch);
			} else {
				// node not updated, save that info, avoids recalculation of
				// patch
				if (nodesNotUpdated == null) {
					nodesNotUpdated = new HashSet<NodeSelector>();
				}
				nodesNotUpdated.add(nodeSelector);
			}
		}
		return nodePatch;
	}

	/**
	 * 
	 * 
	 * @param nodeSubscription
	 * @return
	 * @throws BuildPatchException
	 */
	private Document createNodeXcapDiff(NodeSubscription nodeSubscription)
			throws BuildPatchException {

		final Element[] patchComponents = new Element[1];

		Document oldDocumentDOM = null;
		try {
			if (oldDocument != null) {
				oldDocumentDOM = oldDocument.getAsDOMDocument();
			}
		} catch (InternalServerErrorException e) {
			throw new BuildPatchException(e.getMessage(), e);
		}

		NodeSelector nodeSelector = nodeSubscription.getNodeSelector();
		DOMNodeComparator nodeComparator = new DOMNodeComparator();
		DOMNodeComparator.Result result = nodeComparator.compare(
				oldDocumentDOM, newDocumentDOM,
				nodeSelector.toStringWithEmptyPrefix(),
				nodeSelector.getNamespaceContext());
		if (!result.isDifferent()) {
			return null;
		}
		// going to build patch, start by creating the sel patch component
		// attribute
		if (nodeSelector.getTerminalSelector() == null) {
			// elem
			if (result.isRemoved()) {
				patchComponents[0] = XCAP_DIFF_FACTORY
						.getPatchBuilder()
						.getElementPatchComponentBuilder()
						.buildPatchComponent(
								nodeSubscription.getSel(),
								false,
								nodeSelector.getNamespaceContext()
										.getNamespaces());
			} else {
				// created or updated
				Element element = (Element) result.getNewNode();
				// collect the element parent namespace bindings
				Map<String, String> namespaceBindings = new HashMap<String, String>(
						nodeSelector.getNamespaceContext().getNamespaces());
				collectParentNamespaceBindigs(element, namespaceBindings);
				// create patch component
				patchComponents[0] = XCAP_DIFF_FACTORY
						.getPatchBuilder()
						.getElementPatchComponentBuilder()
						.buildPatchComponent(nodeSubscription.getSel(),
								element, namespaceBindings);
			}
		} else {
			// attr
			if (result.isRemoved()) {
				patchComponents[0] = XCAP_DIFF_FACTORY
						.getPatchBuilder()
						.getAttributePatchComponentBuilder()
						.buildPatchComponent(
								nodeSubscription.getSel(),								
								nodeSelector.getNamespaceContext()
										.getNamespaces());
			} else {
				// created or updated
				String attributeValue = result.getNewNode().getNodeValue();
				// create patch component
				patchComponents[0] = XCAP_DIFF_FACTORY
						.getPatchBuilder()
						.getAttributePatchComponentBuilder()
						.buildPatchComponent(
								nodeSubscription.getSel(),
								attributeValue,
								nodeSelector.getNamespaceContext()
										.getNamespaces());
			}
		}

		return XCAP_DIFF_FACTORY.getPatchBuilder().buildPatch(
				XDM_SERVER_CONFIGURATION.getFullXcapRoot(), patchComponents);
	}

	private static final String XMLNAMESPACE_ATTR = "xmlns";

	/**
	 * Collects all namespace bindings from elem parent till document root.
	 * Since namespace bindings can be redefined, bindings already collected are
	 * not overwritten.
	 * 
	 * @param element
	 * @param namespaceBindings
	 * 
	 */
	private void collectParentNamespaceBindigs(Element element,
			Map<String, String> namespaceBindings) {

		Node parentNode = element.getParentNode();
		if (parentNode.getNodeType() != Node.ELEMENT_NODE) {
			return;
		}

		final NamedNodeMap atts = parentNode.getAttributes();
		Node attr = null;
		String attrName = null;
		String attrPrefix = null;
		for (int i = 0; i < atts.getLength(); i++) {
			attr = atts.item(i);
			attrName = attr.getLocalName();
			attrPrefix = attr.getPrefix();
			if (XMLNAMESPACE_ATTR.equals(attrPrefix)) {
				// xmlns:
				if (!namespaceBindings.containsKey(attrName)) {
					namespaceBindings.put(attrName, attr.getNodeValue());
				}
			} else if (XMLNAMESPACE_ATTR.equals(attrName)) {
				// xmlns
				if (!namespaceBindings
						.containsKey(XMLConstants.DEFAULT_NS_PREFIX)) {
					namespaceBindings.put(XMLConstants.DEFAULT_NS_PREFIX,
							attr.getNodeValue());
				}
			}
		}

		collectParentNamespaceBindigs((Element) parentNode, namespaceBindings);

	}

	protected Document createDocXcapDiff(boolean mayPatch)
			throws BuildPatchException {
		Element[] patchComponents = new Element[1];
		if (oldDocument != null) {
			if (newDocumentDOM != null) {
				// doc updated
				if (mayPatch) {
					Element[] patchInstructions = null;
					try {
						patchInstructions = XCAP_DIFF_FACTORY
								.getPatchBuilder()
								.getDocumentPatchComponentBuilder()
								.getXmlPatchOperationsBuilder()
								.buildPatchInstructions(
										oldDocument.getAsDOMDocument(),
										newDocumentDOM);
					} catch (InternalServerErrorException e) {
						throw new BuildPatchException(e.getMessage(), e);
					}
					patchComponents[0] = XCAP_DIFF_FACTORY
							.getPatchBuilder()
							.getDocumentPatchComponentBuilder()
							.buildPatchComponent(documentSelector.toString(),
									getPreviousETag(), newETag,
									patchInstructions);
				} else {
					patchComponents[0] = XCAP_DIFF_FACTORY
							.getPatchBuilder()
							.getDocumentPatchComponentBuilder()
							.buildPatchComponent(documentSelector.toString(),
									getPreviousETag(), newETag, null);
				}
			} else {
				// doc removed
				patchComponents[0] = XCAP_DIFF_FACTORY
						.getPatchBuilder()
						.getDocumentPatchComponentBuilder()
						.buildPatchComponent(documentSelector.toString(),
								getPreviousETag(), null, null);
			}
		} else {
			// doc created
			patchComponents[0] = XCAP_DIFF_FACTORY
					.getPatchBuilder()
					.getDocumentPatchComponentBuilder()
					.buildPatchComponent(documentSelector.toString(), null,
							newETag, null);

		}
		return XCAP_DIFF_FACTORY.getPatchBuilder().buildPatch(
				XDM_SERVER_CONFIGURATION.getFullXcapRoot(), patchComponents);
	}
}
