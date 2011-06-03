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

package org.mobicents.xdm.server.appusage.oma.xcapdirectory;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
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
import org.mobicents.xcap.client.XcapResponse;
import org.mobicents.xcap.client.auth.Credentials;
import org.mobicents.xcap.client.impl.XcapClientImpl;
import org.mobicents.xcap.client.uri.DocumentSelectorBuilder;
import org.mobicents.xcap.client.uri.UriBuilder;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.error.NotUTF8ConflictException;
import org.openxdm.xcap.common.error.NotWellFormedConflictException;
import org.openxdm.xcap.common.xml.TextWriter;
import org.openxdm.xcap.common.xml.XMLValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PutAndDeleteDocumentTest extends AbstractT {
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(PutAndDeleteDocumentTest.class);
	}
	
	private String user = "sip:joe@example.com";
	private String documentName = "index";
	
	@Test
	public void test() throws IOException, JAXBException, InterruptedException, TransformerException, NotWellFormedConflictException, NotUTF8ConflictException, InternalServerErrorException, InstanceNotFoundException, MBeanException, ReflectionException, URISyntaxException, MalformedObjectNameException, NullPointerException, NamingException {
		
		initRmiAdaptor();

		try {
			createUser(user,"password");
		}
		catch (RuntimeMBeanException e) {
			if (!(e.getCause() instanceof IllegalStateException)) {
				e.printStackTrace();
			}
		}
		
		XcapClient client = new XcapClientImpl();
		
		Credentials credentials = client.getCredentialsFactory().getHttpDigestCredentials(user, "password");
		
		// create uri to put rls-services doc		
		String documentSelector = DocumentSelectorBuilder.getUserDocumentSelectorBuilder("rls-services",user,documentName).toPercentEncodedString();
		UriBuilder uriBuilder = new UriBuilder()
			.setSchemeAndAuthority("http://localhost:8080")
			.setXcapRoot("/mobicents/")
			.setDocumentSelector(documentSelector);
		URI documentURI = uriBuilder.toURI();
		
		// read documents xml
		InputStream is = this.getClass().getResourceAsStream("rls-services-doc.xml");
        String rlsContent = TextWriter.toString(XMLValidator.getWellFormedDocument(XMLValidator.getUTF8Reader(is)));
		is.close();
		
		// send put request and get response
		XcapResponse putResponse = client.put(documentURI,"application/rls-services+xml",rlsContent,null,credentials);
		String docEtag = putResponse.getETag(); 

		// check put response
		System.out.println("Response got:\n"+putResponse);
		assertTrue("Put response must exists",putResponse != null);
		assertTrue("Put response code should be 200 or 201",putResponse.getCode() == 201 || putResponse.getCode() == 200);
				
		// get user xcap directory doc
		String xdDocumentSelector = DocumentSelectorBuilder.getUserDocumentSelectorBuilder(XcapDirectoryAppUsage.ID,user,XcapDirectoryAppUsageDataSourceInterceptor.DIRECTORY_DOCUMENT_NAME).toPercentEncodedString();
		UriBuilder xdUriBuilder = new UriBuilder()
		.setSchemeAndAuthority("http://localhost:8080")
		.setXcapRoot("/mobicents/")
		.setDocumentSelector(xdDocumentSelector);
		URI xdDocumentURI = xdUriBuilder.toURI();
		XcapResponse getResponse = client.get(xdDocumentURI,null,credentials);
				
		// check get response
		assertTrue("Get response must exists",getResponse != null);
		assertTrue("Get response code should be 200",getResponse.getCode() == 200); 
		System.out.println("Response got:\n"+getResponse);
		ByteArrayInputStream bais = new ByteArrayInputStream(getResponse.getEntity().getRawContent());
		Document directoryDocAfterPut = XMLValidator.getWellFormedDocument(XMLValidator.getUTF8Reader(bais));
		bais.close();
		
		// ensure node folder[rls-services]/entry[etag] exists  
		boolean foundElement = false;
		NodeList rootChildNodeList = directoryDocAfterPut.getDocumentElement().getChildNodes();
		for (int i = 0; i<rootChildNodeList.getLength();i++) {
			Node rootChildNode = rootChildNodeList.item(i);
			if (rootChildNode instanceof Element) {
				if (((Element)rootChildNode).getAttribute("auid").equals("rls-services")) {
					NodeList rlsServicesFolderChildNodeList = rootChildNode.getChildNodes();
					for (int j = 0; j<rlsServicesFolderChildNodeList.getLength();j++) {
						Node rlsServicesFolderChildNode = rlsServicesFolderChildNodeList.item(j);
						if (rlsServicesFolderChildNode instanceof Element) {
							if (((Element)rlsServicesFolderChildNode).getAttribute("etag").equals(docEtag)) {
								foundElement = true;
								break;
							}
						}
					}
				}
			}
			if(foundElement) {
				break;
			}
		}
		assertTrue("Get response content must be the expected one",foundElement);
		
		// delete rls doc
		client.delete(documentURI,null,credentials);
		
		// get xcap directory doc again and check content
		getResponse = client.get(xdDocumentURI,null,credentials);
		
		// check get response
		assertTrue("Get response must exists",getResponse != null);
		assertTrue("Get response code should be 200",getResponse.getCode() == 200); 
		System.out.println("Response got:\n"+getResponse);
		bais = new ByteArrayInputStream(getResponse.getEntity().getRawContent());
		Document directoryDocAfterDelete = XMLValidator.getWellFormedDocument(XMLValidator.getUTF8Reader(bais));
		bais.close();
		
		// ensure node folder[rls-services]/entry[etag] exists  
		foundElement = false;
		rootChildNodeList = directoryDocAfterDelete.getDocumentElement().getChildNodes();
		for (int i = 0; i<rootChildNodeList.getLength();i++) {
			Node rootChildNode = rootChildNodeList.item(i);
			if (rootChildNode instanceof Element) {
				if (((Element)rootChildNode).getAttribute("auid").equals("rls-services")) {
					NodeList rlsServicesFolderChildNodeList = rootChildNode.getChildNodes();
					for (int j = 0; j<rlsServicesFolderChildNodeList.getLength();j++) {
						Node rlsServicesFolderChildNode = rlsServicesFolderChildNodeList.item(j);
						if (rlsServicesFolderChildNode instanceof Element) {
							if (((Element)rlsServicesFolderChildNode).getAttribute("etag").equals(docEtag)) {
								foundElement = true;
								break;
							}
						}
					}
				}
			}
			if(foundElement) {
				break;
			}
		}
		assertTrue("Get response content must be the expected one",!foundElement);
				
		client.shutdown();
		
		removeUser(user);
		
	}
		
}
