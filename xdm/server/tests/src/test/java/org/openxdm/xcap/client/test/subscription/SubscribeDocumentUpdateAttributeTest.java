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

package org.openxdm.xcap.client.test.subscription;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.xml.bind.JAXBException;

import junit.framework.JUnit4TestAdapter;

import org.mobicents.xcap.client.XcapResponse;
import org.mobicents.xcap.client.uri.AttributeSelectorBuilder;
import org.mobicents.xcap.client.uri.ElementSelectorBuilder;
import org.mobicents.xcap.client.uri.UriBuilder;
import org.openxdm.xcap.client.test.ServerConfiguration;
import org.openxdm.xcap.common.xcapdiff.DocumentType;
import org.openxdm.xcap.common.xcapdiff.XcapDiff;

/**
 * Subscribes changes in xml element.
 * 
 * @author martins
 * 
 */
public class SubscribeDocumentUpdateAttributeTest extends SubscribeDocumentTest {

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(SubscribeDocumentUpdateAttributeTest.class);
	}
	
	protected String getAttributeSelector() {
		return new AttributeSelectorBuilder("name").toPercentEncodedString();
	}
	
	protected String getElementSelector() {
		return new ElementSelectorBuilder().appendStepByName("resource-lists").appendStepByPos("list", 1).toPercentEncodedString();
	}
	
	protected URI getAttributeXcapURI() throws URISyntaxException {
		// create uri to put doc		
		UriBuilder uriBuilder = new UriBuilder()
			.setSchemeAndAuthority("http://"+ServerConfiguration.SERVER_HOST+":"+ServerConfiguration.SERVER_PORT)
			.setXcapRoot(ServerConfiguration.SERVER_XCAP_ROOT+"/")
			.setDocumentSelector(getDocumentSelector())
			.setElementSelector(getElementSelector())
			.setTerminalSelector(getAttributeSelector());
		return uriBuilder.toURI();	
	}
	
	@Override
	protected void sendTest2XcapRequest() throws IOException,
			URISyntaxException {
		// replace attribute
		String attr = "sip:alice@example.com";
		XcapResponse putResponse = client.put(getAttributeXcapURI(),"application/xcap-att+xml",attr,getAssertedUserIdHeaders(client.getHeaderFactory(), user),null);
		assertTrue("Put response must exists",putResponse != null);
		System.out.print(putResponse.toString());
		assertTrue("Put response code should be 201, instead it is "+putResponse.getCode(),putResponse.getCode() == 201);
		// set etags
		previousEtag = newEtag;
		newEtag = putResponse.getETag();
	}

	@Override
	protected void sendTest3XcapRequest() throws IOException,
			URISyntaxException {
		// delete attribute
		XcapResponse deleteResponse = client.delete(getAttributeXcapURI(),getAssertedUserIdHeaders(client.getHeaderFactory(), user),null);
		assertTrue("Delete response must exists",deleteResponse != null);
		System.out.print(deleteResponse.toString());
		assertTrue("Delete response code should be 200",deleteResponse.getCode() == 200);
		// set previous etag
		previousEtag = newEtag;
		newEtag = deleteResponse.getETag();
	}
	
	@Override
	protected void processTest3Notify(RequestEvent requestEvent)
			throws ParseException, SipException, InvalidArgumentException,
			JAXBException {
		XcapDiff xcapDiff = processNotify(requestEvent);
		assertTrue("not a single document element inside xcap diff document received", xcapDiff.getDocumentOrElementOrAttribute().size() == 1 && xcapDiff.getDocumentOrElementOrAttribute().get(0) instanceof DocumentType);
		DocumentType documentType = (DocumentType) xcapDiff.getDocumentOrElementOrAttribute().get(0);
		assertTrue("doc selector is not "+getDocumentSelector(), documentType.getSel() != null && documentType.getSel().equals(getDocumentSelector()));
		assertTrue("previous etag doesn't match one received in second XCAP PUT response", documentType.getPreviousEtag() != null && documentType.getPreviousEtag().equals(previousEtag));	
		assertTrue("new etag ("+documentType.getNewEtag()+") doesn't match one received in XCAP PUT response ("+newEtag+")", documentType.getNewEtag() != null && documentType.getNewEtag().equals(newEtag));	
	}
	
	@Override
	protected void processTest4Notify(RequestEvent requestEvent)
			throws ParseException, SipException, InvalidArgumentException,
			JAXBException {
		super.processTest4Notify(requestEvent);
		// delete doc in xcdm
		try {
			client.delete(getDocumentXcapURI(),getAssertedUserIdHeaders(client.getHeaderFactory(), user),null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
}
