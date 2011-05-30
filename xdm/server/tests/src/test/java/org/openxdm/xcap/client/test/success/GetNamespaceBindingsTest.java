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

package org.openxdm.xcap.client.test.success;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.bind.JAXBException;

import junit.framework.JUnit4TestAdapter;

import org.apache.commons.httpclient.HttpException;
import org.junit.Test;
import org.openxdm.xcap.client.Response;
import org.openxdm.xcap.client.test.AbstractXDMJunitOldClientTest;
import org.openxdm.xcap.common.key.UserDocumentUriKey;
import org.openxdm.xcap.common.key.UserNamespaceBindingsUriKey;
import org.openxdm.xcap.common.key.XcapUriKey;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.ElementSelectorStep;
import org.openxdm.xcap.common.xml.XMLValidator;

public class GetNamespaceBindingsTest extends AbstractXDMJunitOldClientTest {
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(GetNamespaceBindingsTest.class);
	}
	
	@Test
	public void test() throws HttpException, IOException, JAXBException, InterruptedException {
				
		// create uri		
		UserDocumentUriKey key = new UserDocumentUriKey(appUsage.getAUID(),user,documentName);
		
		String documentContent =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<resource-lists xmlns=\"urn:ietf:params:xml:ns:resource-lists\">" +
				"<list/>" +
			"</resource-lists>";			
		
		// send put request and get response
		Response initialPutResponse = client.put(key,appUsage.getMimetype(),documentContent,null);
		
		// check put response
		assertTrue("Put response must exists",initialPutResponse != null);
		assertTrue("Put response code should be 201",initialPutResponse.getCode() == 201);		
		
		// create uri
		LinkedList<ElementSelectorStep> elementSelectorSteps = new LinkedList<ElementSelectorStep>();
		ElementSelectorStep step1 = new ElementSelectorStep("resource-lists");
		ElementSelectorStep step2 = new ElementSelectorStep("pre:list");								
		elementSelectorSteps.add(step1);
		elementSelectorSteps.addLast(step2);
		Map<String,String> namespaces = new HashMap<String,String>();
		namespaces.put("pre",appUsage.getDefaultDocumentNamespace());		
		XcapUriKey namespacesKey = new UserNamespaceBindingsUriKey(appUsage.getAUID(),user,documentName,new ElementSelector(elementSelectorSteps),namespaces);
		
		// send get request and get response
		Response namespacesGetResponse = client.get(namespacesKey,null);
		
		String expectedResponseContent = "<list xmlns=\"urn:ietf:params:xml:ns:resource-lists\"/>";
		
		// check get response
		assertTrue("Get response must exists",namespacesGetResponse != null);
		assertTrue("Get response code should be 200 and the content must be the expected",namespacesGetResponse.getCode() == 200 && XMLValidator.weaklyEquals((String)namespacesGetResponse.getContent(),expectedResponseContent));
		
		// clean up
		client.delete(key,null);
	}
			
}
