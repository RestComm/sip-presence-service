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

package org.mobicents.xdm.server.appusage;

import javax.xml.transform.TransformerException;

import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.xml.TextWriter;

public class InterceptedDocument implements Document {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final DocumentSelector documentSelector;
	private final org.w3c.dom.Document domDocument;
	private final String eTag;
	private String documentAsString;

	public InterceptedDocument(DocumentSelector documentSelector,
			org.w3c.dom.Document domDocument) {
		this(documentSelector, domDocument, "null");
	}

	public InterceptedDocument(DocumentSelector documentSelector,
			org.w3c.dom.Document domDocument, String eTag) {
		this.documentSelector = documentSelector;
		this.domDocument = domDocument;
		this.eTag = eTag;
	}

	public InterceptedDocument(Document document)
			throws InternalServerErrorException {
		this(new DocumentSelector(document.getCollection(),
				document.getDocumentName()), document.getAsDOMDocument(),
				document.getETag());
	}

	@Override
	public String getCollection() {
		return documentSelector.getCollection();
	}

	@Override
	public org.w3c.dom.Document getAsDOMDocument()
			throws InternalServerErrorException {
		return domDocument;
	}

	@Override
	public String getAsString() throws InternalServerErrorException {
		if (documentAsString == null) {
			try {
				documentAsString = TextWriter.toString(domDocument);
			} catch (TransformerException e) {
				throw new InternalServerErrorException(e.getMessage(), e);
			}
		}
		return documentAsString;
	}

	@Override
	public String getDocumentName() {
		return documentSelector.getDocumentName();
	}

	@Override
	public String getETag() {
		return eTag;
	}

}