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

package org.mobicents.xdm.common.util.dom;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

public class DomUtils {

	public static boolean isElementNamed(Node node, String name) {
		if (node == null) {
			return false;
		}
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			return false;
		}
		return getElementName(node).equals(name);
	}
	
	public static String getElementName(Node node) {
		return node.getLocalName() == null ? node.getNodeName() : node
				.getLocalName();
	}
	
	public static final DocumentBuilderFactory DOCUMENT_BUILDER_NS_AWARE_FACTORY = initDocumentBuilderNsAwareFactory();

	private static DocumentBuilderFactory initDocumentBuilderNsAwareFactory() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		return factory;
	}
	
	public static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();
	
	public static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
	
}
