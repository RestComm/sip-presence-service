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

package org.mobicents.slee.sipevent.server.subscription.eventlist;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBContext;

import org.mobicents.xcap.client.XcapClient;
import org.mobicents.xcap.client.XcapConstant;
import org.mobicents.xcap.client.header.Header;
import org.mobicents.xcap.client.header.HeaderFactory;
import org.mobicents.xcap.client.impl.XcapClientImpl;
import org.mobicents.xcap.client.uri.DocumentSelectorBuilder;
import org.mobicents.xcap.client.uri.UriBuilder;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType.DisplayName;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ListType;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.ObjectFactory;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.PackagesType;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.RlsServices;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.ServiceType;
import org.openxdm.xcap.server.slee.appusage.rlsservices.RLSServicesAppUsage;

public class RlsServicesManager {

	private XcapClient client;
	private final ResourceListServerSipTest test;
	private final String serviceUri;
	private final String[] entryURIs;
	
	protected String password = "password";
	
	public RlsServicesManager(String serviceUri, String[] entryURIs, ResourceListServerSipTest test) {
		this.serviceUri = serviceUri;
		this.entryURIs = entryURIs;
		this.test = test;		
	}
	
	private URI getXcapUri() throws URISyntaxException {
		String documentSelector = DocumentSelectorBuilder.getUserDocumentSelectorBuilder(RLSServicesAppUsage.ID,serviceUri,"index").toPercentEncodedString();
		// create uri to put doc		
		UriBuilder uriBuilder = new UriBuilder()
			.setSchemeAndAuthority("http://127.0.0.1:8080")
			.setXcapRoot("/mobicents/")
			.setDocumentSelector(documentSelector);
		return uriBuilder.toURI();
	}
	
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
	
	public void putRlsServices() {
		client = new XcapClientImpl();
		try {
			client.put(getXcapUri(), RLSServicesAppUsage.MIMETYPE, getRlsServices(entryURIs).getBytes("UTF-8"),getAssertedUserIdHeaders(client.getHeaderFactory(), serviceUri),null);
		} catch (Exception e) {
			e.printStackTrace();
			test.failTest(e.getMessage());
		}
	}
	
	public void deleteRlsServices() {
		if (client != null) {
			try {				
				client.delete(getXcapUri(), getAssertedUserIdHeaders(client.getHeaderFactory(), serviceUri),null);
			} catch (Exception e) {
				e.printStackTrace();
				test.failTest(e.getMessage());
			}
			
			try {
				client.shutdown();
				client = null;
			} catch (Throwable e) {
				e.printStackTrace();
				test.failTest(e.getMessage());
			}
		}
	}
	
	private EntryType createEntryType(String uri) {
		EntryType entryType = new EntryType();
		entryType.setUri(uri);
		DisplayName displayName = new EntryType.DisplayName();
		displayName.setValue(uri);
		entryType.setDisplayName(displayName);
		return entryType;
	}
	
	private String getRlsServices(String[] entryURIs) {
		StringWriter stringWriter = new StringWriter();
		try {			
			JAXBContext context = JAXBContext.newInstance("org.openxdm.xcap.client.appusage.rlsservices.jaxb");
			ListType listType = new ListType();
			for (String entryURI : entryURIs) {
				listType.getListOrExternalOrEntry().add(createEntryType(entryURI));
			}
			ServiceType serviceType = new ServiceType();
			serviceType.setList(listType);
			PackagesType packagesType = new PackagesType();
			packagesType.getPackageAndAny().add(new ObjectFactory().createPackagesTypePackage("presence"));
			serviceType.setPackages(packagesType);
			serviceType.setUri(serviceUri);
			RlsServices rlsServices = new RlsServices();
			rlsServices.getService().add(serviceType);
			context.createMarshaller().marshal(rlsServices, stringWriter);
			return stringWriter.toString();			
		} catch (Exception e) {
			e.printStackTrace();
			test.failTest(e.getMessage());
		}
		finally {		
			try {
				stringWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
				test.failTest(e.getMessage());
			}
		}
		return null;
	}
}
