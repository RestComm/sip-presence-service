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

package org.mobicents.slee.sippresence.server.publication;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.mobicents.xdm.common.util.dom.DomUtils;
import org.openxdm.xcap.common.xml.TextWriter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PresenceCompositionPolicyTest {
	
	@Test
	public void test() throws IOException, ParserConfigurationException, TransformerException, SAXException {
		String pidf1 =   
			"<?xml version='1.0' encoding='UTF-8'?>" +
			"<presence xmlns='urn:ietf:params:xml:ns:pidf' xmlns:dm='urn:ietf:params:xml:ns:pidf:data-model' xmlns:rpid='urn:ietf:params:xml:ns:pidf:rpid' xmlns:c='urn:ietf:params:xml:ns:pidf:cipid' xmlns:op='urn:oma:xml:prs:pidf:oma-pres' entity='sip:user@example.com'>" +
				"<tuple id='t54bb0569'>" +
					"<status>" +
						"<basic>open</basic>" +
					"</status>" +
					"<contact>sip:my_name@example.com</contact>" +
					"<timestamp>2005-02-22T20:07:07Z</timestamp>" +
					"<op:service-description>" + 
						"<op:service-id>org.openmobilealliance:PoC-session</op:service-id>" + 
						"<op:version> 1.0 </op:version>" + 
						"<op:description>This is 1</op:description>"+
					"</op:service-description>"+
				"</tuple>" +
				"<dm:person id='p65f3307a'>" +
					"<rpid:activities><rpid:busy/></rpid:activities>" +
					"<dm:note>Busy</dm:note>" +
				"</dm:person>" +
				"<dm:device id='p65f3307b'>" +
					"<dm:deviceID>urn:uuid:d27459b7-8213-4395-aa77-ed859a3e5b3a</dm:deviceID>" +
					"<dm:timestamp>2005-02-22T20:07:07Z</dm:timestamp>" +
					"<dm:note>BlahBlah</dm:note>" +
				"</dm:device>" +
				"<dm:device id='p65f3307c'>" +
					"<dm:deviceID>urn:uuid:d27459b7-8213-4395-aa77-ed859a3e5b3b</dm:deviceID>" +
					"<dm:timestamp>2005-02-22T20:07:07Z</dm:timestamp>" +
					"<dm:note>BlahBlahBlah</dm:note>" +
					"<op:network-availability>" + 
					"<op:network id='IMS'>" + 
						"<op:active/>" + 
					"</op:network>" + 
				 "</op:network-availability>" +
				"</dm:device>" +
			"</presence>";
		
		String pidf2 = "<?xml version='1.0' encoding='UTF-8'?>" +
		"<presence xmlns='urn:ietf:params:xml:ns:pidf' xmlns:dm='urn:ietf:params:xml:ns:pidf:data-model' xmlns:rpid='urn:ietf:params:xml:ns:pidf:rpid' xmlns:c='urn:ietf:params:xml:ns:pidf:cipid' xmlns:op='urn:oma:xml:prs:pidf:oma-pres' entity='sip:user@example.com'>" +
			"<tuple id='t54bb05690'>" +
				"<status>" +
					"<basic>open</basic>" +
				"</status>" +
				"<contact>sip:my_name@example.com</contact>" +
				"<timestamp>2005-02-23T20:07:07Z</timestamp>" +
				"<op:service-description>" + 
					"<op:service-id>org.openmobilealliance:PoC-session</op:service-id>" + 
					"<op:version> 1.0 </op:version>" + 
					"<op:description>This is 2</op:description>"+
				"</op:service-description>"+
			"</tuple>" +
			"<dm:person id='p65f3307a'>" +
				"<rpid:activities><rpid:busy/></rpid:activities>" +
				"<dm:note>Busy</dm:note>" +
			"</dm:person>" +
			"<dm:device id='p65f3307b'>" +
				"<dm:deviceID>urn:uuid:d27459b7-8213-4395-aa77-ed859a3e5b3b</dm:deviceID>" +
				"<dm:timestamp>2005-02-23T20:07:07Z</dm:timestamp>" +
				"<dm:note>Zzz</dm:note>" +
				"<op:network-availability>" + 
					"<op:network id='IMS'>" + 
						"<op:terminated/>" + 
					"</op:network>" + 
				 "</op:network-availability>" +
			"</dm:device>" +
		"</presence>";
		
		StringReader sr = new StringReader(pidf1);
		DocumentBuilder documentBuilder = DomUtils.DOCUMENT_BUILDER_NS_AWARE_FACTORY.newDocumentBuilder();
		Document presence = documentBuilder.parse(new InputSource(sr));
		sr.close();
		sr = new StringReader(pidf2);
		Document otherPresence =  documentBuilder.parse(new InputSource(sr));
		sr.close();
		Document compositionPresence = new PresenceCompositionPolicy().compose(presence, otherPresence);
		System.out.println("Composed pidf:\n"+TextWriter.toString(compositionPresence,true));
		
	}
}
