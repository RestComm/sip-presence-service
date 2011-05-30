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
import java.util.LinkedList;

import javax.xml.bind.JAXBException;

import junit.framework.JUnit4TestAdapter;

import org.apache.commons.httpclient.HttpException;
import org.junit.Test;
import org.openxdm.xcap.client.Response;
import org.openxdm.xcap.client.test.AbstractXDMJunitOldClientTest;
import org.openxdm.xcap.common.error.NotXMLAttributeValueConflictException;
import org.openxdm.xcap.common.key.UserAttributeUriKey;
import org.openxdm.xcap.common.key.UserDocumentUriKey;
import org.openxdm.xcap.common.resource.AttributeResource;
import org.openxdm.xcap.common.uri.AttributeSelector;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.ElementSelectorStep;

public class NotXMLAttributeValueTest extends AbstractXDMJunitOldClientTest {
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(NotXMLAttributeValueTest.class);
	}
	
	@Test
	public void test() throws HttpException, IOException, JAXBException, InterruptedException {
		
		// create uri		
		UserDocumentUriKey key = new UserDocumentUriKey(appUsage.getAUID(),user,documentName);
		
		String newContent =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<resource-lists xmlns=\"urn:ietf:params:xml:ns:resource-lists\">" +
				"<list/>" +
			"</resource-lists>";			
		
		// send put request and get response
		Response initialPutResponse = client.put(key,appUsage.getMimetype(),newContent,null);
		
		// check put response
		assertTrue("Put response must exists",initialPutResponse != null);
		assertTrue("Put response code should be 201",initialPutResponse.getCode() == 201);		
		
		// create uri
		LinkedList<ElementSelectorStep> elementSelectorSteps = new LinkedList<ElementSelectorStep>();
		ElementSelectorStep step1 = new ElementSelectorStep("resource-lists");
		ElementSelectorStep step2 = new ElementSelectorStep("list");								
		elementSelectorSteps.add(step1);
		elementSelectorSteps.addLast(step2);
		UserAttributeUriKey attrKey = new UserAttributeUriKey(appUsage.getAUID(),user,documentName,new ElementSelector(elementSelectorSteps),new AttributeSelector("name"),null);
		
		// create exception for return codes
		NotXMLAttributeValueConflictException exception = new NotXMLAttributeValueConflictException();
		
		// 2. put new attr
		
		// send put request and get response
		Response attrPutResponse = client.put(attrKey,AttributeResource.MIMETYPE,"<badattvalue",null);				
		// check put response
		assertTrue("Put response must exists",attrPutResponse != null);
		assertTrue("Put response content must be the expected and the response code should be "+exception.getResponseStatus(),attrPutResponse.getCode() == exception.getResponseStatus() && attrPutResponse.getContent().equals(exception.getResponseContent()));
		
		// send put request and get response
		attrPutResponse = client.put(attrKey,AttributeResource.MIMETYPE,"'badattvalue",null);				
		// check put response
		assertTrue("Put response must exists",attrPutResponse != null);
		assertTrue("Put response content must be the expected and the response code should be "+exception.getResponseStatus(),attrPutResponse.getCode() == exception.getResponseStatus() && attrPutResponse.getContent().equals(exception.getResponseContent()));
		
		// send put request and get response
		attrPutResponse = client.put(attrKey,AttributeResource.MIMETYPE,"\"badattvalue",null);				
		// check put response
		assertTrue("Put response must exists",attrPutResponse != null);
		assertTrue("Put response content must be the expected and the response code should be "+exception.getResponseStatus(),attrPutResponse.getCode() == exception.getResponseStatus() && attrPutResponse.getContent().equals(exception.getResponseContent()));
		
		// 2. replace attr
		
		// send put request and get response
		attrPutResponse = client.put(attrKey,AttributeResource.MIMETYPE,"enemies",null);				
		
		// check put response
		assertTrue("Put response must exists",attrPutResponse != null);
		assertTrue("Put response code should be 201, instead it was "+attrPutResponse.getCode()+", content = "+attrPutResponse.getContent(),attrPutResponse.getCode() == 201);
		
		// send put request and get response
		attrPutResponse = client.put(attrKey,AttributeResource.MIMETYPE,"<badattvalue",null);				
		// check put response
		assertTrue("Put response must exists",attrPutResponse != null);
		assertTrue("Put response content must be the expected and the response code should be "+exception.getResponseStatus(),attrPutResponse.getCode() == exception.getResponseStatus() && attrPutResponse.getContent().equals(exception.getResponseContent()));
		
		// send put request and get response
		attrPutResponse = client.put(attrKey,AttributeResource.MIMETYPE,"'badattvalue",null);				
		// check put response
		assertTrue("Put response must exists",attrPutResponse != null);
		assertTrue("Put response content must be the expected and the response code should be "+exception.getResponseStatus(),attrPutResponse.getCode() == exception.getResponseStatus() && attrPutResponse.getContent().equals(exception.getResponseContent()));
		
		// send put request and get response
		attrPutResponse = client.put(attrKey,AttributeResource.MIMETYPE,"\"badattvalue",null);				
		// check put response
		assertTrue("Put response must exists",attrPutResponse != null);
		assertTrue("Put response content must be the expected and the response code should be "+exception.getResponseStatus(),attrPutResponse.getCode() == exception.getResponseStatus() && attrPutResponse.getContent().equals(exception.getResponseContent()));
		
		//TODO bad char and entity refs puts
		
		// clean up
		client.delete(key,null);
	}
			
}
