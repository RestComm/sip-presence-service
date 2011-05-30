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
import org.openxdm.xcap.common.error.BadRequestException;
import org.openxdm.xcap.common.key.UserAttributeUriKey;
import org.openxdm.xcap.common.key.UserDocumentUriKey;
import org.openxdm.xcap.common.resource.AttributeResource;
import org.openxdm.xcap.common.uri.AttributeSelector;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.ElementSelectorStep;
import org.openxdm.xcap.common.uri.ElementSelectorStepByPos;

public class BadRequestTest extends AbstractXDMJunitOldClientTest {
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(BadRequestTest.class);
	}
	
	@Test
	public void test() throws HttpException, IOException, JAXBException, InterruptedException {
			
		// create uri		
		UserDocumentUriKey key = new UserDocumentUriKey(appUsage.getAUID(),user,documentName);
		
		String content =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<resource-lists xmlns=\"urn:ietf:params:xml:ns:resource-lists\">" +
				"<list/>" +
				"<list name=\"enemies\"/>" +
			"</resource-lists>";
		
		// send put request and get response
		Response response = client.put(key,appUsage.getMimetype(),content,null);
		
		// check put response
		assertTrue("Put response must exists",response != null);
		assertTrue("Put response code should be 201",response.getCode() == 201);
				
		// create element selector		
		LinkedList<ElementSelectorStep> elementSelectorSteps = new LinkedList<ElementSelectorStep>();
		ElementSelectorStep step1 = new ElementSelectorStep("pre1:resource-lists");
		ElementSelectorStep step2 = new ElementSelectorStepByPos("pre2:list",1);
		elementSelectorSteps.add(step1);
		elementSelectorSteps.addLast(step2);
		// set namespace bindings 
		Map<String,String> namespaces = new HashMap<String,String>();
		namespaces.put("pre1",appUsage.getDefaultDocumentNamespace());
		
		// create attr put uri
		UserAttributeUriKey attrKey = new UserAttributeUriKey(appUsage.getAUID(),user,documentName,new ElementSelector(elementSelectorSteps),new AttributeSelector("name"),namespaces);
		// send put request and get response
		Response putResponse = client.put(attrKey,AttributeResource.MIMETYPE,"friends",null);
		// check put response, must be bad request
		assertTrue("Put response must exists",putResponse != null);
		assertTrue("Put response code should be "+BadRequestException.RESPONSE_STATUS,putResponse.getCode() == BadRequestException.RESPONSE_STATUS);

		// create attr delete & get uri
		elementSelectorSteps.removeLast();
		step2 = new ElementSelectorStepByPos("pre2:list",2);
		elementSelectorSteps.addLast(step2);
		UserAttributeUriKey anotherAttrKey = new UserAttributeUriKey(appUsage.getAUID(),user,documentName,new ElementSelector(elementSelectorSteps),new AttributeSelector("name"),null);
				
		// send get request and get response
		Response getResponse = client.get(anotherAttrKey,null);
		// check get response, must be bad request
		assertTrue("Delete response must exists",getResponse != null);
		assertTrue("Delete response code should be "+BadRequestException.RESPONSE_STATUS,getResponse.getCode() == BadRequestException.RESPONSE_STATUS);
		
		// send delete request and get response
		Response deleteResponse = client.delete(anotherAttrKey,null);
		// check put response, must be bad request
		assertTrue("Delete response must exists",deleteResponse != null);
		assertTrue("Delete response code should be "+BadRequestException.RESPONSE_STATUS,deleteResponse.getCode() == BadRequestException.RESPONSE_STATUS);
		
		// cleanup
		client.delete(key,null);
	}	
	
}
