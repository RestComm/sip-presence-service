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

package org.mobicents.sippresence.presence.server.subscription;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilder;

import org.junit.Test;
import org.mobicents.slee.sippresence.server.subscription.rules.OMAPresRule;
import org.mobicents.slee.sippresence.server.subscription.rules.OMAPresRuleDOMTransformer;
import org.mobicents.slee.sippresence.server.subscription.rules.RulesetProcessor;
import org.mobicents.xdm.common.util.dom.DomUtils;
import org.openxdm.xcap.client.appusage.presrules.jaxb.commonpolicy.Ruleset;
import org.openxdm.xcap.common.xml.TextWriter;
import org.openxdm.xcap.common.xml.XMLValidator;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public abstract class AbstractPresRulesTest {

	public OMAPresRule getRule() throws Exception {
		JAXBContext jaxbContext = JAXBContext
		.newInstance("org.openxdm.xcap.client.appusage.presrules.jaxb.commonpolicy:org.openxdm.xcap.client.appusage.presrules.jaxb:org.openxdm.xcap.client.appusage.omapresrules.jaxb");
		InputStream is = AbstractPresRulesTest.class
		.getResourceAsStream(getClass().getSimpleName()
				+ "-rule.xml");
		Ruleset ruleset = (Ruleset) jaxbContext.createUnmarshaller().unmarshal(is);
		return RulesetProcessor.processTransformations(ruleset.getRule().get(0),null);
	}

	public Document getUnfilteredContent() throws Exception {
		InputStream is = AbstractPresRulesTest.class
				.getResourceAsStream("original-doc.xml");
		DocumentBuilder builder = DomUtils.DOCUMENT_BUILDER_NS_AWARE_FACTORY
				.newDocumentBuilder();
		return builder.parse(new InputSource(is));
	}

	public Document getFilteredContent() throws Exception {
		InputStream is = AbstractPresRulesTest.class
				.getResourceAsStream(getClass().getSimpleName()
						+ "-filtered-doc.xml");
		DocumentBuilder builder = DomUtils.DOCUMENT_BUILDER_NS_AWARE_FACTORY
				.newDocumentBuilder();
		return builder.parse(new InputSource(is));
	}
	
	@Test
	public void testRule() throws Exception {

		OMAPresRule rule = getRule();
		Document unfilteredDocument = getUnfilteredContent();
		Document filteredDocument = new OMAPresRuleDOMTransformer().transform(
				unfilteredDocument, rule);
		Document filteredDocumentToValidate = getFilteredContent();
		if (filteredDocument == null) {
			if (filteredDocumentToValidate != null) {
				throw new Exception("filtered doc is null but it should be:\n"
						+ TextWriter.toString(filteredDocumentToValidate, true));
			} else {
				System.out.println("both docs are null, test passed");
			}
		} else {
			if (filteredDocumentToValidate == null) {
				throw new Exception(
						"filtered doc should be null but instead it is:\n"
								+ TextWriter.toString(filteredDocument, true));
			} else {
				if (!XMLValidator.weaklyEquals(TextWriter.toString(filteredDocument), TextWriter.toString(filteredDocumentToValidate))){
					System.out.println("filtered doc content should be the expected. It is:\n"
							+ TextWriter.toString(filteredDocument,
									true)
							+ "\nIt should be:\n"
							+ TextWriter.toString(
									filteredDocumentToValidate, true));
					throw new Exception();
				} else {
					System.out
							.println("test passed, both docs content match:\n"
									+ TextWriter.toString(filteredDocument,
											true));
				}
			}
		}
	}
}
