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

package org.mobicents.xdm.server.appusage.oma.prescontent;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;
import org.mobicents.xcap.client.XcapClient;
import org.mobicents.xcap.client.XcapConstant;
import org.mobicents.xcap.client.XcapResponse;
import org.mobicents.xcap.client.header.Header;
import org.mobicents.xcap.client.header.HeaderFactory;
import org.mobicents.xcap.client.impl.XcapClientImpl;
import org.mobicents.xcap.client.uri.DocumentSelectorBuilder;
import org.mobicents.xcap.client.uri.UriBuilder;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.error.NotUTF8ConflictException;
import org.openxdm.xcap.common.error.NotWellFormedConflictException;
import org.openxdm.xcap.common.xml.TextWriter;
import org.openxdm.xcap.common.xml.XMLValidator;

public class PutDocumentTest extends AbstractT {
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(PutDocumentTest.class);
	}
	
	private String user = "sip:alice@example.com";
	private String anotherUser = "sip:bob@example.com";
	private String documentName = "index";
	
	private Header[] getAssertedUserIdHeaders(HeaderFactory headerFactory, String assertedUserId) {
		Header[] headers = null;
		if (assertedUserId != null) {
			headers = new Header[1];
			headers[0] = headerFactory
					.getBasicHeader(
							XcapConstant.HEADER_X_3GPP_Asserted_Identity,
							assertedUserId);
		}
		return headers;
	}
	
	@Test
	public void test() throws IOException, JAXBException, InterruptedException, TransformerException, NotWellFormedConflictException, NotUTF8ConflictException, InternalServerErrorException, InstanceNotFoundException, MBeanException, ReflectionException, URISyntaxException, MalformedObjectNameException, NullPointerException, NamingException {
		
		initRmiAdaptor();

		try {
			createUser(user,user);
			createUser(anotherUser,anotherUser);
		}
		catch (RuntimeMBeanException e) {
			if (!(e.getCause() instanceof IllegalStateException)) {
				e.printStackTrace();
			}
		}
		
		XcapClient client = new XcapClientImpl();
				
		// create uri to put rls-services doc		
		String documentSelector = DocumentSelectorBuilder.getUserDocumentSelectorBuilder(OMAPresContentAppUsage.ID,user,documentName).toPercentEncodedString();
		UriBuilder uriBuilder = new UriBuilder()
			.setSchemeAndAuthority("http://localhost:8080")
			.setXcapRoot("/mobicents/")
			.setDocumentSelector(documentSelector);
		URI documentURI = uriBuilder.toURI();
		
		// read documents xml
		InputStream is = this.getClass().getResourceAsStream("good-document.xml");
        String content = TextWriter.toString(XMLValidator.getWellFormedDocument(XMLValidator.getUTF8Reader(is)));
		is.close();
		
		// send put request and get response
		XcapResponse putResponse = client.put(documentURI,OMAPresContentAppUsage.MIMETYPE,content,getAssertedUserIdHeaders(client.getHeaderFactory(), user),null);
		System.out.println("Response got:\n"+putResponse);
		assertTrue("Put response must exists",putResponse != null);
		assertTrue("Put response code should be 200 or 201",putResponse.getCode() == 201 || putResponse.getCode() == 200);
		
		// ensure other user can get doc
		XcapResponse getResponse = client.get(documentURI,getAssertedUserIdHeaders(client.getHeaderFactory(), anotherUser),null);
		assertTrue("Get response must exists",getResponse != null);
		assertTrue("Get response code should be 200",getResponse.getCode() == 200); 
		System.out.println("Response got:\n"+getResponse);
		
		// ensure another user can't put
		putResponse = client.put(documentURI,OMAPresContentAppUsage.MIMETYPE,content,getAssertedUserIdHeaders(client.getHeaderFactory(), anotherUser),null);
		System.out.println("Response got:\n"+putResponse);
		assertTrue("Put response must exists",putResponse != null);
		assertTrue("Put response code should be 403",putResponse.getCode() == 403);
		
		// ensure another user can't delete
		XcapResponse deleteResponse = client.delete(documentURI,getAssertedUserIdHeaders(client.getHeaderFactory(), anotherUser),null);
		assertTrue("Unauthorized delete response must exists",deleteResponse != null);
		assertTrue("Unauthorized delete response code should be 403",deleteResponse.getCode() == 403); 
		System.out.println("Response got:\n"+deleteResponse);
		
		// delete doc
		deleteResponse = client.delete(documentURI,getAssertedUserIdHeaders(client.getHeaderFactory(), user),null);
		assertTrue("Authorized delete response must exists",deleteResponse != null);
		assertTrue("Authorized delete response code should be 200",deleteResponse.getCode() == 200); 
		System.out.println("Response got:\n"+deleteResponse);		
		
		client.shutdown();
		
		removeUser(user);
		removeUser(anotherUser);
		
	}
		
}
