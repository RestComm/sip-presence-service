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

package org.openxdm.xcap.client.test.error;

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
import org.openxdm.xcap.common.error.MethodNotAllowedException;
import org.openxdm.xcap.common.key.UserDocumentUriKey;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.ElementSelectorStep;

public class MethodNotAllowedTest extends AbstractXDMJunitOldClientTest {
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(MethodNotAllowedTest.class);
	}
	
	@Test
	public void test() throws HttpException, IOException, JAXBException, InterruptedException {

		// create uri		
		UserDocumentUriKey key = new UserDocumentUriKey(appUsage.getAUID(),user,documentName);
		
		String content =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<resource-lists xmlns=\"urn:ietf:params:xml:ns:resource-lists\">" +
				"<list/>" +
			"</resource-lists>";
		
		// send put request and get response
		Response putResponse = client.put(key,appUsage.getMimetype(),content,null);
		
		// check put response
		assertTrue("Put response must exists",putResponse != null);
		assertTrue("Put response code should be 201",putResponse.getCode() == 201);
				
		// create element selector		
		LinkedList<ElementSelectorStep> elementSelectorSteps = new LinkedList<ElementSelectorStep>();
		ElementSelectorStep step1 = new ElementSelectorStep("pre:resource-lists");
		ElementSelectorStep step2 = new ElementSelectorStep("pre:list");
		elementSelectorSteps.add(step1);
		elementSelectorSteps.addLast(step2);
		// set namespace bindings to ask 
		Map<String,String> namespaces = new HashMap<String,String>();
		namespaces.put("pre",appUsage.getDefaultDocumentNamespace());
		// create namespace bindings uri in a fake document uri key
		MethodNotAllowedTestFakeUserDocumentUriKey nsKey = new MethodNotAllowedTestFakeUserDocumentUriKey(appUsage.getAUID(),user,documentName,new ElementSelector(elementSelectorSteps),namespaces);
				
		// send put request and get response
		Response nsPutResponse = client.put(nsKey,appUsage.getMimetype(),content,null);
		
		// check put response, must be method not allowed
		assertTrue("Put response must exists",nsPutResponse != null);
		assertTrue("Put response code should be "+MethodNotAllowedException.RESPONSE_STATUS,nsPutResponse.getCode() == MethodNotAllowedException.RESPONSE_STATUS);

		// send delete request and get response
		Response nsDeleteResponse = client.delete(nsKey,null);
		
		// check delete response, must be method not allowed
		assertTrue("Put response must exists",nsDeleteResponse != null);
		assertTrue("Put response code should be "+MethodNotAllowedException.RESPONSE_STATUS,nsDeleteResponse.getCode() == MethodNotAllowedException.RESPONSE_STATUS);
		
		// clean up
		client.delete(key,null);
	}
		
}
