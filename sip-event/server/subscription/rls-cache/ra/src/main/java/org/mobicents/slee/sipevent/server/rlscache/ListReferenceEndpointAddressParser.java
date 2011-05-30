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

import javax.slee.facilities.Tracer;
import javax.slee.resource.ResourceAdaptorContext;

import org.mobicents.slee.xdm.server.ServerConfiguration;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.ElementSelectorStepByAttr;
import org.openxdm.xcap.common.uri.NodeSelector;
import org.openxdm.xcap.common.uri.Parser;
import org.openxdm.xcap.common.uri.ResourceSelector;

public class ListReferenceEndpointAddressParser {

	private final Tracer tracer;
	
	public ListReferenceEndpointAddressParser(ResourceAdaptorContext context) {
		tracer = context.getTracer(getClass().getSimpleName());
	}

	public String getLocalXcapRoot() {
		return ServerConfiguration.getInstance().getXcapRoot();
	}
	
	public String getSchemeAndAuthorityURI() {
		return ServerConfiguration.getInstance().getSchemeAndAuthority();
	}
	
	public ListReferenceEndpointAddress getAddress(String uri, boolean absoluteURI) {
		
		if (tracer.isFineEnabled()) {
			tracer.fine("Making address from resource list uri "+uri);
		}
		
		try {
			if (absoluteURI) {
				String shemeAndAuthorityURI = getSchemeAndAuthorityURI();
				if (uri.startsWith(shemeAndAuthorityURI)) {
					uri = uri.substring(shemeAndAuthorityURI.length());
				}
				else {
					if (tracer.isFineEnabled()) {
						tracer.fine("The resource list (to dereference) uri "+uri+" does not starts with server scheme and authority uri "+shemeAndAuthorityURI);
					}
					return null;
				}
			}
			else {
				uri = "/" + uri;
			}
			ResourceSelector resourceSelector = null;
			int queryComponentSeparator = uri.indexOf('?');
			if (queryComponentSeparator > 0) {
				resourceSelector = Parser
						.parseResourceSelector(
								getLocalXcapRoot(),
								uri
										.substring(0,
												queryComponentSeparator),
								uri
										.substring(
												queryComponentSeparator + 1));
			} else {
				resourceSelector = Parser
						.parseResourceSelector(
								getLocalXcapRoot(),
								uri, null);
			}
			
			DocumentSelector documentSelector = DocumentSelector.valueOf(resourceSelector.getDocumentSelector());
			if (!documentSelector.getAUID().equals("resource-lists")) {
				tracer.severe("Unable to make address, invalid or not supported resource list uri: "+uri);
				return null;
			}
			ElementSelector elementSelector = null;
			if (resourceSelector.getNodeSelector() != null) {
				NodeSelector nodeSelector = Parser.parseNodeSelector(resourceSelector.getNodeSelector(),null);
				elementSelector = Parser.parseElementSelector(nodeSelector.getElementSelector());
				if (elementSelector.getStepsSize() < 2) {
					tracer.warning("List reference element selector selects resource-list root element. Uri: "+uri);
					return null;
				}
				// only support element selectors with steps by attribute value (except root one), otherwise due to doc changes the ref may become invalid or point to a different list
				for (int i=1;i<elementSelector.getStepsSize();i++) {
					if (!(elementSelector.getStep(i) instanceof ElementSelectorStepByAttr)) {
						tracer.warning("List reference element selector includes steps not selected by attribute value, not supported, all references will become with BAD GATEWAY 502 state. Uri: "+uri);
						return null;
					}
				}
			}
			
			return new ListReferenceEndpointAddress(documentSelector, elementSelector);
			
		}
		catch (Exception e) {
			tracer.severe("Failed to parse resource list (to dereference) "+uri,e);
			return null;
		}
	}
	
}
