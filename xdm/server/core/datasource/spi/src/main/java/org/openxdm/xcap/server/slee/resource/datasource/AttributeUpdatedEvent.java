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

import java.util.Map;

import javax.slee.EventTypeID;

import org.mobicents.protocols.xcap.diff.BuildPatchException;
import org.mobicents.protocols.xcap.diff.dom.DOMDocumentPatchComponentBuilder;
import org.mobicents.protocols.xcap.diff.dom.DOMXcapDiffPatchBuilder;
import org.openxdm.xcap.common.uri.AttributeSelector;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.NodeSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class AttributeUpdatedEvent extends DocumentUpdatedEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * IMPORTANT: must sync with the event descriptor data!!!!
	 */
	public static final EventTypeID EVENT_TYPE_ID = new EventTypeID(
			"AttributeUpdatedEvent", "org.openxdm", "1.0");

	private final NodeSelector nodeSelector;
	private final AttributeSelector attributeSelector;
	private final String oldAttributeValue;
	private final String newAttributeValue;

	public AttributeUpdatedEvent(DocumentSelector documentSelector,
			String defaultDocNamespace,
			org.openxdm.xcap.common.datasource.Document oldDocument,
			Document newDocumentDOM, String newDocumentString, String newETag,
			NodeSelector nodeSelector, AttributeSelector attributeSelector,
			String oldAttributeValue, String newAttributeValue) {
		super(documentSelector, defaultDocNamespace, oldDocument,
				newDocumentDOM, newDocumentString, newETag);
		this.nodeSelector = nodeSelector;
		this.attributeSelector = attributeSelector;
		this.oldAttributeValue = oldAttributeValue;
		this.newAttributeValue = newAttributeValue;
	}

	public AttributeSelector getAttributeSelector() {
		return attributeSelector;
	}

	public String getNewAttributeValue() {
		return newAttributeValue;
	}

	public String getOldAttributeValue() {
		return oldAttributeValue;
	}

	public NodeSelector getNodeSelector() {
		return nodeSelector;
	}

	@Override
	protected Document createDocXcapDiff(boolean mayPatch) throws BuildPatchException {
		
		if (!mayPatch) {
			// if xml patch ops may not be included, then just reuse the parent logic
			return super.createDocXcapDiff(false);		
		}
		
		Map<String, String> namespaceBindings = nodeSelector
				.getNamespaceContext() != null ? nodeSelector
				.getNamespaceContext().getNamespaces() : null;
		DOMXcapDiffPatchBuilder patchBuilder = XCAP_DIFF_FACTORY
				.getPatchBuilder();
		DOMDocumentPatchComponentBuilder documentPatchComponentBuilder = patchBuilder
				.getDocumentPatchComponentBuilder();
		Element patchInstruction = null;
		if (oldAttributeValue == null) {
			patchInstruction = documentPatchComponentBuilder
					.getXmlPatchOperationsBuilder().addAttribute(
							nodeSelector.getElementSelector(),
							attributeSelector.getAttName(), newAttributeValue,
							namespaceBindings);
		} else {
			if (newAttributeValue == null) {
				patchInstruction = documentPatchComponentBuilder
						.getXmlPatchOperationsBuilder().removeAttribute(
								nodeSelector.toString(), namespaceBindings);
			} else {
				patchInstruction = documentPatchComponentBuilder
						.getXmlPatchOperationsBuilder().replaceAttribute(
								nodeSelector.toString(), newAttributeValue,
								namespaceBindings);
			}
		}
		Element[] patchInstructions = { patchInstruction };
		Element[] patchComponents = { documentPatchComponentBuilder
				.buildPatchComponent(documentSelector.toString(),
						getPreviousETag(), getNewETag(), patchInstructions) };
		return patchBuilder.buildPatch(
				XDM_SERVER_CONFIGURATION.getFullXcapRoot(), patchComponents);
	}

}
