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
import org.openxdm.xcap.client.test.ServerConfiguration;
import org.openxdm.xcap.common.datasource.DataSource;
import org.openxdm.xcap.common.error.NoParentConflictException;
import org.openxdm.xcap.common.key.UserAttributeUriKey;
import org.openxdm.xcap.common.key.UserDocumentUriKey;
import org.openxdm.xcap.common.key.UserElementUriKey;
import org.openxdm.xcap.common.resource.AttributeResource;
import org.openxdm.xcap.common.resource.ElementResource;
import org.openxdm.xcap.common.uri.AttributeSelector;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.ElementSelectorStep;
import org.openxdm.xcap.common.uri.ElementSelectorStepByPos;

public class NoParentTest extends AbstractXDMJunitOldClientTest {
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(NoParentTest.class);
	}
		
	@Test
	public void test() throws HttpException, IOException, JAXBException, InterruptedException {
		
		// create content for tests		
		
		String documentContent =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<resource-lists xmlns=\"urn:ietf:params:xml:ns:resource-lists\">" +
				"<list/>" +
			"</resource-lists>";						
		
		String elementContent = "<list xmlns=\"urn:ietf:params:xml:ns:resource-lists\"/>";				

		// create uri keys
		
		UserDocumentUriKey documentKey = new UserDocumentUriKey(appUsage.getAUID(),user,documentName);		
		UserDocumentUriKey fakeAppUsageDocumentKey = new UserDocumentUriKey("eduardomartinsappusage",user,documentName);
		
		LinkedList<ElementSelectorStep> elementSelectorSteps = new LinkedList<ElementSelectorStep>();
		ElementSelectorStep step1 = new ElementSelectorStep("resource-lists");
		ElementSelectorStep step2 = new ElementSelectorStepByPos("list",2);								
		ElementSelectorStep step3 = new ElementSelectorStep("display-name");
		elementSelectorSteps.add(step1);
		elementSelectorSteps.addLast(step2);		
		elementSelectorSteps.addLast(step3);
		ElementSelector elementSelector = new ElementSelector(elementSelectorSteps);
		UserElementUriKey elementKey = new UserElementUriKey(appUsage.getAUID(),user,documentName,elementSelector,null);
		UserElementUriKey fakeAppUsageElementKey = new UserElementUriKey("eduardomartinsappusage",user,documentName,elementSelector,null);
		
		UserAttributeUriKey attrKey = new UserAttributeUriKey(appUsage.getAUID(),user,documentName,elementSelector,new AttributeSelector("name"),null);
		UserAttributeUriKey fakeAttrKey = new UserAttributeUriKey("eduardomartinsappusage",user,documentName,elementSelector,new AttributeSelector("name"),null);
	
		// DOCUMENT PARENT NOT FOUND

		NoParentConflictException exception = new NoParentConflictException(ServerConfiguration.SERVER_XCAP_ROOT+'/');				
		exception.setSchemeAndAuthorityURI("http://"+ServerConfiguration.SERVER_HOST+":"+ServerConfiguration.SERVER_PORT);
		
		// 1. put new document and document parent not found
		
		// send put request and get response
		Response response = client.put(fakeAppUsageDocumentKey,appUsage.getMimetype(),documentContent,null);		
		// check response
		assertTrue("Response must exists",response != null);
		assertTrue("Response content must be the expected one and response code should be "+exception.getResponseStatus(),response.getCode() == exception.getResponseStatus() && response.getContent().equals(exception.getResponseContent()));

		// 2. put new element and document parent not found
		
		// send put request and get response
		response = client.put(fakeAppUsageElementKey,ElementResource.MIMETYPE,elementContent,null);		
		// check response
		assertTrue("Response must exists",response != null);
		assertTrue("Response content must be the expected one and response code should be "+exception.getResponseStatus(),response.getCode() == exception.getResponseStatus() && response.getContent().equals(exception.getResponseContent()));

		// 3. put new attribute and document parent not found
		
		// send put request and get response
		response = client.put(fakeAttrKey,AttributeResource.MIMETYPE,"",null);		
		// check response
		assertTrue("Response must exists",response != null);
		assertTrue("Response content must be the expected one and response code should be "+exception.getResponseStatus(),response.getCode() == exception.getResponseStatus() && response.getContent().equals(exception.getResponseContent()));

		// DOCUMENT NOT FOUND
		
		exception = new NoParentConflictException(ServerConfiguration.SERVER_XCAP_ROOT+'/'+appUsage.getAUID()+"/users/"+user);				
		exception.setSchemeAndAuthorityURI("http://"+ServerConfiguration.SERVER_HOST+":"+ServerConfiguration.SERVER_PORT);
		
		// 4. put new element and document not found

		// send put request and get response
		response = client.put(elementKey,ElementResource.MIMETYPE,elementContent,null);		
		// check response
		assertTrue("Response must exists",response != null);
		System.out.println("Response Content: "+response.getContent());
		assertTrue("Response content must be the expected one and response code should be "+exception.getResponseStatus(),response.getCode() == exception.getResponseStatus() && response.getContent().equals(exception.getResponseContent()));
		
		// 5. put new attribute and document not found

		// send put request and get response
		response = client.put(attrKey,AttributeResource.MIMETYPE,"",null);		
		// check response
		assertTrue("Response must exists",response != null);
		assertTrue("Response content must be the expected one and response code should be "+exception.getResponseStatus(),response.getCode() == exception.getResponseStatus() && response.getContent().equals(exception.getResponseContent()));
		
		// ELEMENT PARENT NOT FOUND
		
		// send put request and get response
		response = client.put(documentKey,appUsage.getMimetype(),documentContent,null);		
		// check put response
		assertTrue("Put response must exists",response != null);
		assertTrue("Put response code should be 201",response.getCode() == 201);

		exception = new NoParentConflictException(ServerConfiguration.SERVER_XCAP_ROOT+'/'+appUsage.getAUID()+"/users/"+user+"/"+documentName+"/~~/resource-lists");				
		exception.setSchemeAndAuthorityURI("http://"+ServerConfiguration.SERVER_HOST+":"+ServerConfiguration.SERVER_PORT);
		
		// 6. put new element and element parent not found

		// send put request and get response
		response = client.put(elementKey,ElementResource.MIMETYPE,elementContent,null);		
		// check response
		assertTrue("Response must exists",response != null);
		assertTrue("Response content must be the expected one and response code should be "+exception.getResponseStatus(),response.getCode() == exception.getResponseStatus() && response.getContent().equals(exception.getResponseContent()));
		
		// 7. put new attribute and element parent not found

		// send put request and get response
		response = client.put(attrKey,AttributeResource.MIMETYPE,"",null);		
		// check response
		assertTrue("Response must exists",response != null);
		assertTrue("Response content must be the expected one and response code should be "+exception.getResponseStatus(),response.getCode() == exception.getResponseStatus() && response.getContent().equals(exception.getResponseContent()));
		
		// clean up
		client.delete(documentKey,null);
	}
	
}





