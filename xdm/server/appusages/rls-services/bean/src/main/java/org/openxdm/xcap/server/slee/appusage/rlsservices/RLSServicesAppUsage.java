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

package org.openxdm.xcap.server.slee.appusage.rlsservices;

import java.net.URI;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.mobicents.xdm.common.util.dom.DomUtils;
import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.mobicents.xdm.server.appusage.AppUsageRequestProcessor;
import org.openxdm.xcap.common.error.ConstraintFailureConflictException;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.error.NotAuthorizedRequestException;
import org.openxdm.xcap.common.error.SchemaValidationErrorConflictException;
import org.openxdm.xcap.common.error.UniquenessFailureConflictException;
import org.openxdm.xcap.common.uri.AttributeSelector;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.NodeSelector;
import org.openxdm.xcap.common.xml.NamespaceContext;
import org.openxdm.xcap.server.slee.appusage.resourcelists.ResourceListsAppUsage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RLSServicesAppUsage extends AppUsage {

	public static final String ID = "rls-services";
	public static final String DEFAULT_DOC_NAMESPACE = "urn:ietf:params:xml:ns:rls-services";
	public static final String MIMETYPE = "application/rls-services+xml";

	private static final Logger logger = Logger
			.getLogger(RLSServicesAppUsage.class);

	private final NamespaceContext namespaceContext;
	private final boolean validateUniquenessContraints;

	public RLSServicesAppUsage(Validator schemaValidator, boolean validateUniquenessContraints) {
		super(ID, DEFAULT_DOC_NAMESPACE, MIMETYPE, schemaValidator,
				new RLSServicesAuthorizationPolicy());
		namespaceContext = new NamespaceContext();
		namespaceContext.setDefaultDocNamespace(DEFAULT_DOC_NAMESPACE);
		this.validateUniquenessContraints = validateUniquenessContraints;
	}

	private static final String SERVICE_ELEMENT_NAME = "service";
	private static final String URI_ATTRIBUTE_NAME = "uri";

	private Set<String> getServiceURIs(Document document)
			throws UniquenessFailureConflictException {
		Set<String> serviceURIs = new HashSet<String>();
		NodeList nodeList = document.getDocumentElement().getChildNodes();
		Node node = null;
		for (int i = 0; i < nodeList.getLength(); i++) {
			node = nodeList.item(i);
			if (DomUtils.isElementNamed(node, SERVICE_ELEMENT_NAME)) {
				Element element = (Element) node;
				if (!serviceURIs.add(element.getAttribute(URI_ATTRIBUTE_NAME))) {
					throw new UniquenessFailureConflictException();
				}
			}
		}
		return serviceURIs;
	}

	static boolean isUserIndexDoc(DocumentSelector documentSelector) {
		if (!documentSelector.getDocumentName().equals("index")) {
			return false;
		}
		return documentSelector.getCollection().equals(
				new StringBuilder("rls-services/users/").append(
						documentSelector.getUser()).toString());
	}

	private void checkServiceExists(String uri, AppUsageDataSource dataSource)
			throws UniquenessFailureConflictException,
			InternalServerErrorException {
		// fetch all user docs
		DocumentSelector ds = null;
		NodeList nodeList = null;
		Node node = null;
		for (org.openxdm.xcap.common.datasource.Document document : dataSource
				.getDocuments("rls-services/users", true)) {
			ds = new DocumentSelector(document.getCollection(),
					document.getDocumentName());
			if (RLSServicesAppUsage.isUserIndexDoc(ds)) {
				// only process index docs at the user dir collection
				// get all 2nd level elements
				nodeList = document.getAsDOMDocument().getDocumentElement()
						.getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					node = nodeList.item(i);
					if (DomUtils.isElementNamed(node, SERVICE_ELEMENT_NAME)) {
						// rls-services/service element
						if (((Element) node).getAttribute(URI_ATTRIBUTE_NAME)
								.equals(uri)) {
							// uri attribute matches, thus new service not
							// unique
							throw new UniquenessFailureConflictException();
						}
					}
				}
			}
		}
	}

	private void checkServicesExists(Set<String> uris,
			AppUsageDataSource dataSource)
			throws UniquenessFailureConflictException,
			InternalServerErrorException {
		// fetch all user docs
		DocumentSelector ds = null;
		NodeList nodeList = null;
		Node node = null;
		for (org.openxdm.xcap.common.datasource.Document document : dataSource
				.getDocuments("rls-services/users", true)) {
			ds = new DocumentSelector(document.getCollection(),
					document.getDocumentName());
			if (RLSServicesAppUsage.isUserIndexDoc(ds)) {
				// only process index docs at the user dir collection
				// get all 2nd level elements
				nodeList = document.getAsDOMDocument().getDocumentElement()
						.getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					node = nodeList.item(i);
					if (DomUtils.isElementNamed(node, SERVICE_ELEMENT_NAME)) {
						// rls-services/service element
						if (uris.contains(((Element) node)
								.getAttribute(URI_ATTRIBUTE_NAME))) {
							// uri attribute matches, thus new service not
							// unique
							throw new UniquenessFailureConflictException();
						}
					}
				}
			}
		}
	}

	@Override
	public void processResourceInterdependenciesOnPutAttribute(
			String oldAttrValue, String newAttrValue,
			DocumentSelector documentSelector, String newETag,
			NodeSelector nodeSelector, ElementSelector elementSelector,
			AttributeSelector attributeSelector,
			AppUsageRequestProcessor requestProcessor,
			AppUsageDataSource dataSource)
			throws SchemaValidationErrorConflictException,
			UniquenessFailureConflictException, InternalServerErrorException,
			ConstraintFailureConflictException {

		if (logger.isDebugEnabled()) {
			logger.debug("processResourceInterdependenciesOnPutAttribute( oldAttrValue = "
					+ oldAttrValue
					+ ", newAttrValue = "
					+ newAttrValue
					+ ", documentSelector = "
					+ documentSelector
					+ ", elementSelector = "
					+ elementSelector
					+ ", attributeSelector = " + attributeSelector + " )");
		}

		if (!validateUniquenessContraints) {
			return;
		}
		
		if (!isUserIndexDoc(documentSelector)) {
			return;
		}

		if (elementSelector.getStepsSize() != 2
				|| !attributeSelector.getAttName().equals("uri")) {
			// only the change of a service uri is relevant
			return;
		}

		// the new attr value is a new service uri, check it doesn't exists
		checkServiceExists(newAttrValue, dataSource);

	}

	@Override
	public void processResourceInterdependenciesOnPutElement(
			Element oldElement, Element newElement, Document document,
			DocumentSelector documentSelector, String newETag,
			NodeSelector nodeSelector, ElementSelector elementSelector,
			AppUsageRequestProcessor requestProcessor,
			AppUsageDataSource dataSource)
			throws SchemaValidationErrorConflictException,
			UniquenessFailureConflictException, InternalServerErrorException,
			ConstraintFailureConflictException {

		if (logger.isDebugEnabled()) {
			logger.debug("processResourceInterdependenciesOnPutElement( oldElement = "
					+ oldElement
					+ ", newElement = "
					+ newElement
					+ ", documentSelector = "
					+ documentSelector
					+ ", elementSelector = " + elementSelector + " )");
		}

		if (!validateUniquenessContraints) {
			return;
		}
		
		if (!isUserIndexDoc(documentSelector)) {
			return;
		}

		if (elementSelector.getStepsSize() > 2) {
			// only the change of a service uri is relevant
			return;
		} else if (elementSelector.getStepsSize() == 2) {
			// put of 1 service
			if (oldElement != null) {
				// update
				if (oldElement.getAttribute("uri").equals(
						newElement.getAttribute("uri"))) {
					// uri didn't change
					return;
				} else {
					// check if the new uri exists
					checkServiceExists(newElement.getAttribute("uri"),
							dataSource);
				}
			} else {
				// creation
				// check if the new uri exists
				checkServiceExists(newElement.getAttribute("uri"), dataSource);
			}
		} else {
			// put of all services, same as put of doc
			processResourceInterdependenciesOnPutDocument(dataSource
					.getDocument(documentSelector).getAsDOMDocument(),
					document, documentSelector, newETag, requestProcessor,
					dataSource);
		}
	}

	@Override
	public void processResourceInterdependenciesOnPutDocument(
			Document oldDocument, Document newDocument,
			DocumentSelector documentSelector, String newEtag,
			AppUsageRequestProcessor requestProcessor,
			AppUsageDataSource dataSource)
			throws SchemaValidationErrorConflictException,
			UniquenessFailureConflictException, InternalServerErrorException,
			ConstraintFailureConflictException {

		if (logger.isDebugEnabled()) {
			logger.debug("processResourceInterdependenciesOnPutDocument( oldDoc = "
					+ oldDocument
					+ ", newDoc = "
					+ newDocument
					+ ", documentSelector = " + documentSelector + " )");
		}

		if (!validateUniquenessContraints) {
			return;
		}
		
		if (!isUserIndexDoc(documentSelector)) {
			return;
		}

		if (oldDocument == null) {
			// insert
			checkServicesExists(getServiceURIs(newDocument), dataSource);
		} else {
			// update
			Set<String> newURIs = getServiceURIs(newDocument);
			newURIs.removeAll(getServiceURIs(oldDocument));
			checkServicesExists(newURIs, dataSource);
		}
	}

	@Override
	public void checkConstraintsOnPut(Document document, String xcapRoot,
			DocumentSelector documentSelector, AppUsageDataSource dataSource)
			throws UniquenessFailureConflictException,
			InternalServerErrorException, ConstraintFailureConflictException,
			NotAuthorizedRequestException {

		if (!documentSelector.isUserDocument()) {
			return;
		}

		super.checkConstraintsOnPut(document, xcapRoot, documentSelector,
				dataSource);

		/*
		 * NOTE: the contraint below is ensured when (re)building the global doc
		 * 
		 * "The URI in the "uri" attribute of the <service> element MUST be
		 * unique amongst all other URIs in "uri" elements in any <service>
		 * element in any document on a particular server. This uniqueness
		 * constraint spans across XCAP roots."
		 */

		/*
		 * TODO ensure the uri is not a network resource, such as the uri of a
		 * sip user
		 * 
		 * "Furthermore, the URI MUST NOT correspond to an existing resource
		 * within the domain of the URI. If a server is asked to set the URI to
		 * something that already exists, the server MUST reject the request
		 * with a 409, and use the mechanisms defined in [10] to suggest
		 * alternate URIs that have not yet been allocated."
		 */

		// get document's element childs
		NodeList childNodes = document.getDocumentElement().getChildNodes();
		// process each one
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (DomUtils.isElementNamed(childNode, "service")) {
				// service element
				// get childs
				NodeList serviceChildNodes = childNode.getChildNodes();
				// process each one
				for (int j = 0; j < serviceChildNodes.getLength(); j++) {
					Node serviceChildNode = serviceChildNodes.item(j);
					if (DomUtils.isElementNamed(serviceChildNode, "list")) {
						// list element
						/*
						 * o In addition, an RLS services document can contain a
						 * <list> element, which in turn can contain <entry>,
						 * <entry-ref> and <external> elements. The constraints
						 * defined for these elements in Section 3.4.7 MUST be
						 * enforced.
						 */
						ResourceListsAppUsage.checkNodeResourceListConstraints(
								serviceChildNode, false);
					} else if (DomUtils.isElementNamed(serviceChildNode,
							"resource-list")) {
						// resource-list element

						// flag setup
						boolean throwException = true;
						// node value is the uri to evaluate
						String resourceListUri = serviceChildNode
								.getTextContent().trim();

						try {
							// build uri
							URI uri = new URI(resourceListUri);
							String uriScheme = uri.getScheme();
							/*
							 * The URI in a <resource-list> element MUST be an
							 * absolute URI.
							 */
							if (uriScheme != null
									&& (uriScheme.equalsIgnoreCase("http") || uriScheme
											.equalsIgnoreCase("https"))) {
								// split string after "scheme://" to find path
								// segments
								String[] resourceListUriPaths = resourceListUri
										.substring(uriScheme.length() + 3)
										.split("/");
								for (int k = 0; k < resourceListUriPaths.length; k++) {
									/*
									 * The server MUST verify that the URI path
									 * contains "resource-lists" in the path
									 * segment corresponding to the AUID.
									 */
									if (resourceListUriPaths[k]
											.equals(ResourceListsAppUsage.ID)) {
										// found auid
										if (!resourceListUriPaths[k + 1]
												.equals("global")) {
											// not global
											/*
											 * If the RLS services document is
											 * within the XCAP user tree (as
											 * opposed to the global tree), the
											 * server MUST verify that the XUI
											 * in the path is the same as the
											 * XUI in the URI of to the
											 * resource-list document.
											 */
											// decode the candidate xui first
											String resourceListXUIDecoded = URLDecoder
													.decode(resourceListUriPaths[k + 2],
															"UTf-8");
											String requestXUI = documentSelector
													.getUser();
											if (resourceListXUIDecoded
													.equals(requestXUI)) {
												throwException = false;
											} else {
												logger.error("not the same xcap user id in request ("
														+ requestXUI
														+ ") and resource list ("
														+ resourceListXUIDecoded
														+ ") URIs");
											}
											break;
										} else {
											throwException = false;
											break;
										}
									}
								}
							}
						} catch (Exception e) {
							// ignore
							logger.error(e.getMessage(), e);
						}
						// throw exception if needed
						if (throwException) {
							throw new ConstraintFailureConflictException(
									"Bad URI in resource-list element >> "
											+ resourceListUri);
						}
					}
				}
			}
		}
	}
}
