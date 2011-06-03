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

package org.openxdm.xcap.common.xml;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.mobicents.xdm.common.util.dom.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SchemaContext {

	/**
	 * Map with schemas as DOM documents, indexed by schema's target namespace
	 */
	private Map<String, Document> documentMap = new HashMap<String, Document>();
	private SchemaFactory factory = SchemaFactory
			.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	/**
	 * Retrieves an instance from all schema files in a dir. The schema files
	 * must have the xsd file extension
	 * 
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static SchemaContext fromDir(URI dirURI) throws SAXException,
			IOException, ParserConfigurationException {
		// init dom resources
		List<Document> schemaDocuments = new ArrayList<Document>();
		DocumentBuilder documentBuilder = DomUtils.DOCUMENT_BUILDER_NS_AWARE_FACTORY
				.newDocumentBuilder();
		// read files and parse to dom resources
		File schemaDir = new File(dirURI);
		if (schemaDir.isDirectory()) {
			// create filter to select only files with name that ends with .xsd
			FileFilter fileFilter = new FileFilter() {
				public boolean accept(File f) {
					if (f.isDirectory()) {
						return false;
					} else {
						if (f.getName().endsWith(".xsd")) {
							return true;
						} else {
							return false;
						}
					}
				}
			};
			// get schema files from schema dir
			File[] schemaFiles = schemaDir.listFiles(fileFilter);
			for (File schemaFile : schemaFiles) {
				// parse each one to dom document and add to schema docs list
				schemaDocuments.add(documentBuilder.parse(schemaFile));
			}
		} else {
			return null;
		}
		// create and return new schema context
		SchemaContext schemaContext = new SchemaContext(schemaDocuments);
		schemaContext.getFactory().setResourceResolver(
				new LocalLSResourceResolver(dirURI));
		return schemaContext;
	}

	/**
	 * Creates a new instance to provide schemas that can combine the ones
	 * specified as DOM documents.
	 * 
	 * @param documents
	 *            the list os schemas that can be combined by the provider
	 */
	public SchemaContext(List<Document> documents) {
		for (Iterator<Document> i = documents.iterator(); i.hasNext();) {
			Document document = i.next();
			String targetNamespace = (document.getDocumentElement())
					.getAttribute("targetNamespace");
			if (targetNamespace != null) {
				documentMap.put(targetNamespace, document);
			}
		}
	}

	public SchemaFactory getFactory() {
		return factory;
	}

	/**
	 * Retrieves a schema which combines all schemas in the directory referenced
	 * from the specified target namespace.
	 * 
	 * @param rootTargetNamespace
	 * @return
	 */
	public Schema getCombinedSchema(String rootTargetNamespace) {

		// create temp list that will hold all schema docs sources to combine
		LinkedList<DOMSource> sourcesToCombine = new LinkedList<DOMSource>();
		// create temp list that will hold all schema docs to process
		LinkedList<String> documentsToProcessByTargetNamepsace = new LinkedList<String>();
		// get root document and kick off the process to find other needed
		// schemas
		documentsToProcessByTargetNamepsace.addLast(rootTargetNamespace);
		// add others by looking at each document for others needed to import
		while (!documentsToProcessByTargetNamepsace.isEmpty()) {
			// get head target namespace
			String targetNamespace = documentsToProcessByTargetNamepsace
					.removeFirst();
			// get the related schema document
			Document document = documentMap.get(targetNamespace);
			if (document != null) {
				// document exists, add source to combination list
				sourcesToCombine.addFirst(new DOMSource(document));
				// and find other docs to import
				NodeList nl = document.getElementsByTagNameNS(
						"http://www.w3.org/2001/XMLSchema", "import");
				for (int i = 0; i < nl.getLength(); i++) {
					Element elem = (Element) nl.item(i);
					// found one, add target namespace to process list
					documentsToProcessByTargetNamepsace.addLast(elem
							.getAttribute("namespace"));
				}
			} else {
				// document needed is not here, abort
				return null;
			}
		}
		// create a schema by combining all selected
		try {
			return factory.newSchema(sourcesToCombine
					.toArray(new DOMSource[sourcesToCombine.size()]));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Retrieves a schema which combines all schemas in the directory used to
	 * create the context.
	 * 
	 * @return
	 */
	public Schema getCombinedSchema() {
		// create temp list that will hold all schema docs sources to combine
		LinkedList<DOMSource> sourcesToCombine = new LinkedList<DOMSource>();
		for (Document document : documentMap.values()) {
			sourcesToCombine.addFirst(new DOMSource(document));
		}
		// create a schema by combining all selected
		try {
			return factory.newSchema(sourcesToCombine
					.toArray(new DOMSource[sourcesToCombine.size()]));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
