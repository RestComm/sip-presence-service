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

package org.openxdm.xcap.server.slee;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.slee.ActivityContextInterface;
import javax.slee.RolledBackContext;
import javax.slee.SbbContext;
import javax.slee.facilities.Tracer;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.mobicents.slee.xdm.server.ServerConfiguration;
import org.mobicents.xdm.common.util.dom.DocumentCloner;
import org.mobicents.xdm.common.util.dom.DomUtils;
import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageManagement;
import org.mobicents.xdm.server.appusage.AuthorizationPolicy;
import org.openxdm.xcap.common.error.BadRequestException;
import org.openxdm.xcap.common.error.CannotDeleteConflictException;
import org.openxdm.xcap.common.error.CannotInsertConflictException;
import org.openxdm.xcap.common.error.ConflictException;
import org.openxdm.xcap.common.error.ConstraintFailureConflictException;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.error.MethodNotAllowedException;
import org.openxdm.xcap.common.error.NoParentConflictException;
import org.openxdm.xcap.common.error.NotAuthorizedRequestException;
import org.openxdm.xcap.common.error.NotFoundException;
import org.openxdm.xcap.common.error.NotUTF8ConflictException;
import org.openxdm.xcap.common.error.NotValidXMLFragmentConflictException;
import org.openxdm.xcap.common.error.NotXMLAttributeValueConflictException;
import org.openxdm.xcap.common.error.PreconditionFailedException;
import org.openxdm.xcap.common.error.SchemaValidationErrorConflictException;
import org.openxdm.xcap.common.error.UniquenessFailureConflictException;
import org.openxdm.xcap.common.error.UnsupportedMediaTypeException;
import org.openxdm.xcap.common.etag.ETagGenerator;
import org.openxdm.xcap.common.resource.AttributeResource;
import org.openxdm.xcap.common.resource.DocumentResource;
import org.openxdm.xcap.common.resource.ElementResource;
import org.openxdm.xcap.common.resource.NamespaceBindings;
import org.openxdm.xcap.common.uri.AttributeSelector;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.ElementSelectorStep;
import org.openxdm.xcap.common.uri.ElementSelectorStepByAttr;
import org.openxdm.xcap.common.uri.ElementSelectorStepByPos;
import org.openxdm.xcap.common.uri.ElementSelectorStepByPosAttr;
import org.openxdm.xcap.common.uri.NodeSelector;
import org.openxdm.xcap.common.uri.ParseException;
import org.openxdm.xcap.common.uri.Parser;
import org.openxdm.xcap.common.uri.ResourceSelector;
import org.openxdm.xcap.common.uri.TerminalSelector;
import org.openxdm.xcap.common.xml.NamespaceContext;
import org.openxdm.xcap.common.xml.TextWriter;
import org.openxdm.xcap.common.xml.XMLValidator;
import org.openxdm.xcap.server.etag.ETagValidator;
import org.openxdm.xcap.server.result.CreatedWriteResult;
import org.openxdm.xcap.server.result.OKWriteResult;
import org.openxdm.xcap.server.result.ReadResult;
import org.openxdm.xcap.server.result.WriteResult;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceSbbInterface;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class RequestProcessorSbb implements RequestProcessor,
		javax.slee.Sbb {

	private SbbContext sbbContext = null;

	private static Tracer logger;

	private DataSourceSbbInterface dataSourceSbbInterface;
	private AppUsageDataSourceImpl appUsageDataSource;

	private static final ServerConfiguration CONFIGURATION = ServerConfiguration
			.getInstance();

	private static final AppUsageManagement APPUSAGE_MANAGEMENT = AppUsageManagement
			.getInstance();

	/**
	 * Called when an sbb object is instantied and enters the pooled state.
	 */
	public void setSbbContext(SbbContext context) {
		this.sbbContext = context;
		if (logger == null) {
			logger = sbbContext.getTracer(this.getClass().getSimpleName());
		}
		try {
			Context myEnv = (Context) new InitialContext()
					.lookup("java:comp/env");
			dataSourceSbbInterface = (DataSourceSbbInterface) myEnv
					.lookup("slee/resources/openxdm/datasource/sbbrainterface");
			appUsageDataSource = new AppUsageDataSourceImpl(
					dataSourceSbbInterface);
		} catch (NamingException e) {
			logger.severe("Can't set sbb context.", e);
		}
	}

	public void unsetSbbContext() {
		this.sbbContext = null;
	}

	public void sbbCreate() throws javax.slee.CreateException {
	}

	public void sbbPostCreate() throws javax.slee.CreateException {
	}

	public void sbbActivate() {
	}

	public void sbbPassivate() {
	}

	public void sbbRemove() {
	}

	public void sbbLoad() {
	}

	public void sbbStore() {
	}

	public void sbbExceptionThrown(Exception exception, Object event,
			ActivityContextInterface activity) {
		if (logger.isFineEnabled())
			logger.fine("sbbExceptionThrown(exception=" + exception.toString()
					+ ",event=" + event.toString() + ",activity="
					+ activity.toString() + ")");
	}

	public void sbbRolledBack(RolledBackContext sbbRolledBack) {
		if (logger.isFineEnabled())
			logger.fine("sbbRolledBack(sbbRolledBack="
					+ sbbRolledBack.toString() + ")");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openxdm.xcap.server.slee.RequestProcessor#delete(org.openxdm.xcap
	 * .common.uri.ResourceSelector, org.openxdm.xcap.server.etag.ETagValidator,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public WriteResult delete(ResourceSelector resourceSelector,
			ETagValidator eTagValidator, String xcapRoot,
			String authenticatedUser) throws NotFoundException,
			InternalServerErrorException, BadRequestException,
			CannotDeleteConflictException, PreconditionFailedException,
			MethodNotAllowedException, SchemaValidationErrorConflictException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException, NotAuthorizedRequestException {

		if (logger.isFineEnabled())
			logger.fine("deleting " + resourceSelector);

		AppUsage appUsage = null;

		try {

			// parse document selector
			final DocumentSelector documentSelector = DocumentSelector
					.valueOf(resourceSelector.getDocumentSelector());

			// get app usage
			appUsage = APPUSAGE_MANAGEMENT.getAppUsage(documentSelector
					.getAUID());
			if (appUsage == null) {
				// throw exception
				if (logger.isFineEnabled())
					logger.fine("appusage " + documentSelector.getAUID()
							+ " not found");
				throw new NotFoundException();
			}

			// authorize user
			if (authenticatedUser != null
					&& !appUsage.getAuthorizationPolicy().isAuthorized(
							authenticatedUser,
							AuthorizationPolicy.Operation.DELETE,
							documentSelector, appUsageDataSource)) {
				throw new NotAuthorizedRequestException();
			}

			// get document
			org.openxdm.xcap.common.datasource.Document document = dataSourceSbbInterface
					.getDocument(documentSelector);
			if (document == null) {
				// throw exception
				if (logger.isFineEnabled())
					logger.fine("document " + documentSelector + " not found");
				throw new NotFoundException();
			}
			if (logger.isFineEnabled())
				logger.fine("document " + documentSelector + " found");

			// check document etag
			if (eTagValidator != null) {
				eTagValidator.validate(document.getETag());
				if (logger.isFineEnabled())
					logger.fine("document " + documentSelector
							+ " etag validated");
			} else {
				if (logger.isFineEnabled())
					logger.fine("document " + documentSelector
							+ " etag validation not required");
			}

			if (resourceSelector.getNodeSelector() != null) {
				// elem, attr or namespace bind
				// parse node selector
				final NodeSelector nodeSelector = Parser.parseNodeSelector(
						resourceSelector.getNodeSelector(),
						resourceSelector.getNamespaceContext());
				if (logger.isFineEnabled())
					logger.fine("node selector " + nodeSelector
							+ " found and parsed");
				// config namespace context
				final NamespaceContext namespaceContext = resourceSelector
						.getNamespaceContext();
				namespaceContext.setDefaultDocNamespace(appUsage
						.getDefaultDocumentNamespace());
				// clone doc
				final Document newDocumentDOM = DocumentCloner.clone(document
						.getAsDOMDocument());
				// get element
				final Element newElement = getElementForDeleteOrGet(
						newDocumentDOM, nodeSelector, false);
				// parse element selector
				final ElementSelector elementSelector = Parser
						.parseElementSelector(nodeSelector.getElementSelector());
				if (nodeSelector.getTerminalSelector() != null) {
					// delete attr or namespace bind
					// parse terminal selector
					TerminalSelector terminalSelector = Parser
							.parseTerminalSelector(nodeSelector
									.getTerminalSelector());
					if (logger.isFineEnabled())
						logger.fine("terminal selector " + terminalSelector
								+ " found and parsed");

					if (terminalSelector instanceof AttributeSelector) {
						return deleteAttribute(document, newDocumentDOM,
								newElement, documentSelector, nodeSelector,
								elementSelector,
								(AttributeSelector) terminalSelector, appUsage,
								true);
					} else {
						// namespace selector, only GET method is allowed
						if (logger.isFineEnabled())
							logger.fine("terminal selector "
									+ terminalSelector
									+ " is a namespace selector, not allowed on delete");
						Map<String, String> map = new HashMap<String, String>();
						map.put("Allow", "GET");
						throw new MethodNotAllowedException(map);
					}
				} else {
					// delete element
					return deleteElement(document, newDocumentDOM, newElement,
							documentSelector, nodeSelector, elementSelector,
							appUsage, true);
				}
			} else {
				return deleteDocument(document, documentSelector, appUsage,
						true);
			}

		} catch (ParseException e) {
			if (logger.isFineEnabled())
				logger.fine("error parsing uri, returning not found");
			throw new NotFoundException();

		}

	}

	private WriteResult deleteDocument(
			org.openxdm.xcap.common.datasource.Document document,
			DocumentSelector documentSelector, AppUsage appUsage,
			boolean processResourceInterdependencies)
			throws InternalServerErrorException,
			ConstraintFailureConflictException,
			UniquenessFailureConflictException,
			SchemaValidationErrorConflictException {

		if (logger.isFineEnabled())
			logger.fine("deleting document " + documentSelector);

		if (logger.isFineEnabled())
			logger.fine("processing app usage resource interdependencies for "
					+ documentSelector);

		// process resource interdependencies for the request app usage
		if (processResourceInterdependencies) {
			try {
				appUsage.processResourceInterdependenciesOnDeleteDocument(
						document.getAsDOMDocument(), documentSelector, this,
						appUsageDataSource);
			} catch (SchemaValidationErrorConflictException e) {
				// must rollback all changes in datasource
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (UniquenessFailureConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (ConstraintFailureConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (InternalServerErrorException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			}
		}
		if (logger.isFineEnabled())
			logger.fine("app usage resource interdependencies processed for "
					+ documentSelector);

		// delete document
		try {
			dataSourceSbbInterface.deleteDocument(documentSelector,
					appUsage.getDefaultDocumentNamespace(), document);
		} catch (InternalServerErrorException e) {
			if (!sbbContext.getRollbackOnly())
				sbbContext.setRollbackOnly();
			throw e;
		}

		if (logger.isFineEnabled())
			logger.fine(documentSelector.toString() + " deleted");

		return new OKWriteResult();
	}

	private WriteResult deleteElement(
			final org.openxdm.xcap.common.datasource.Document oldDocument,
			final Document newDocumentDOM, final Element newElement,
			final DocumentSelector documentSelector,
			final NodeSelector nodeSelector,
			final ElementSelector elementSelector, AppUsage appUsage,
			boolean processResourceInterdependencies)
			throws InternalServerErrorException, NotFoundException,
			CannotDeleteConflictException,
			SchemaValidationErrorConflictException, BadRequestException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException {

		if (logger.isFineEnabled())
			logger.fine("deleting element " + elementSelector + " in document "
					+ documentSelector);

		// check cannot delete
		ElementSelectorStep lastElementSelectorStep = elementSelector
				.getLastStep();
		if (lastElementSelectorStep instanceof ElementSelectorStepByPosAttr) {
			// need to check if it's the last sibring
			// with the same name and attr value
			ElementSelectorStepByPosAttr elementSelectorStepByPosAttr = (ElementSelectorStepByPosAttr) lastElementSelectorStep;
			if (elementSelectorStepByPosAttr.getName().equals("*")) {
				if (logger.isFineEnabled())
					logger.fine("element selector by attr and pos with wildcard name");
				// all elements wildcard
				Element siblingElement = newElement;
				while ((siblingElement = (Element) siblingElement
						.getNextSibling()) != null) {
					// get attribute with same name
					Attr siblingElementAttr = siblingElement
							.getAttributeNode(elementSelectorStepByPosAttr
									.getAttrName());
					// check if it has the same value
					if (siblingElementAttr != null
							&& siblingElementAttr.getValue()
									.equals(elementSelectorStepByPosAttr
											.getAttrValue())) {
						// we have a sibling with the
						// same attribute with the same
						// value, so when we delete the
						// element the uri points to
						// this one
						if (logger.isFineEnabled())
							logger.fine("sibling element with same attr name and value, cannot delete");
						throw new CannotDeleteConflictException();
					}
				}
			} else {
				if (logger.isFineEnabled())
					logger.fine("element selector by attr and pos without wildcard name");
				Element siblingElement = newElement;
				while ((siblingElement = (Element) siblingElement
						.getNextSibling()) != null) {
					if (newElement.getNodeName().compareTo(
							siblingElement.getNodeName()) == 0
							&& newElement.getNamespaceURI().compareTo(
									siblingElement.getNamespaceURI()) == 0) {
						// sibling with the same name
						// get attribute with same name
						Attr siblingElementAttr = siblingElement
								.getAttributeNode(elementSelectorStepByPosAttr
										.getAttrName());
						// check if it has the same
						// value
						if (siblingElementAttr != null
								&& siblingElementAttr.getValue().equals(
										elementSelectorStepByPosAttr
												.getAttrValue())) {
							// we have a sibling with
							// the same attribute with
							// the same value, so when
							// we delete the element the
							// uri points to this one
							if (logger.isFineEnabled())
								logger.fine("sibling element with same attr name and value, cannot delete");
							throw new CannotDeleteConflictException();
						}
					}
				}
			}
		} else if (lastElementSelectorStep instanceof ElementSelectorStepByPos) {

			ElementSelectorStepByPos elementSelectorStepByPos = (ElementSelectorStepByPos) lastElementSelectorStep;
			/*
			 * In particular, if a DELETE operation refers to an element by name
			 * and position alone (parent/elname[n]), this is permitted only
			 * when the element to be deleted is the last element amongst all
			 * its siblings with that name. Similarly, if a DELETE operation
			 * refers to an element by position alone (parent/*[n]), this is
			 * permitted only when the elemented to be deleted is the last
			 * amongst all sibling elements, regardless of name.
			 */
			// find out if it's the last sibling
			if (elementSelectorStepByPos.getName().equals("*")) {
				if (logger.isFineEnabled())
					logger.fine("element selector by pos with wildcard name");
				if (newElement.getNextSibling() != null) {
					// not the last * sibling
					if (logger.isFineEnabled())
						logger.fine("not the last * sibling, cannot delete");
					throw new CannotDeleteConflictException();
				}
			} else {
				if (logger.isFineEnabled())
					logger.fine("element selector by pos without wildcard name");
				// search a next sibling with the same
				// name
				Element siblingElement = newElement;
				while ((siblingElement = (Element) siblingElement
						.getNextSibling()) != null) {
					if (newElement.getNodeName().compareTo(
							siblingElement.getNodeName()) == 0
							&& newElement.getNamespaceURI().compareTo(
									siblingElement.getNamespaceURI()) == 0) {
						if (logger.isFineEnabled())
							logger.fine("sibling element with same name and ns after the selected element,cannot delete");
						throw new CannotDeleteConflictException();
					}
				}
			}
		}
		if (logger.isFineEnabled())
			logger.fine("element deleted");
		// the element can be deleted
		newElement.getParentNode().removeChild(newElement);

		if (logger.isFineEnabled())
			logger.fine("validating document after delete");
		// validate the updated document against it's schema
		appUsage.validateSchema(newDocumentDOM);

		if (logger.isFineEnabled())
			logger.fine("checking app usage constraints and resource interdependencies...");
		// verify app usage constraints
		appUsage.checkConstraintsOnDelete(newDocumentDOM,
				CONFIGURATION.getXcapRoot(), documentSelector,
				appUsageDataSource);

		// create new etag
		String newETag = ETagGenerator.generate(documentSelector.toString());

		// process resource interdependencies for the request app usage
		if (processResourceInterdependencies) {
			try {
				appUsage.processResourceInterdependenciesOnDeleteElement(
						newElement, documentSelector, newETag, nodeSelector,
						elementSelector, this, appUsageDataSource);
			} catch (SchemaValidationErrorConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (UniquenessFailureConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (ConstraintFailureConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (InternalServerErrorException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			}
		}
		// update data source with document
		try {
			String newDocumentString = TextWriter.toString(newDocumentDOM);
			dataSourceSbbInterface.updateElement(documentSelector,
					appUsage.getDefaultDocumentNamespace(), oldDocument,
					newDocumentDOM, newDocumentString, newETag, nodeSelector,
					newElement, null);

			if (logger.isFineEnabled())
				logger.fine("document updated in data source");
		} catch (Exception e) {
			if (!sbbContext.getRollbackOnly())
				sbbContext.setRollbackOnly();
			throw new InternalServerErrorException(
					"Failed to serialize resulting dom document to string");
		}

		if (logger.isFineEnabled())
			logger.fine(elementSelector.toString() + " element in "
					+ documentSelector + " deleted");

		return new OKWriteResult(newETag);

	}

	private WriteResult deleteAttribute(
			final org.openxdm.xcap.common.datasource.Document oldDocument,
			final Document newDocumentDOM, final Element newElement,
			final DocumentSelector documentSelector,
			final NodeSelector nodeSelector,
			final ElementSelector elementSelector,
			final AttributeSelector attributeSelector, final AppUsage appUsage,
			boolean processResourceInterdependencies)
			throws InternalServerErrorException, NotFoundException,
			SchemaValidationErrorConflictException, BadRequestException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException {

		if (logger.isFineEnabled())
			logger.fine("deleting attribute " + attributeSelector.getAttName()
					+ " from element " + elementSelector + " in document "
					+ documentSelector);

		final String attrName = attributeSelector.getAttName();
		String oldAttrValue = null;
		// note that getAttribute returns "" for a non existent attribute
		if (newElement.hasAttribute(attrName)) {
			oldAttrValue = newElement.getAttribute(attrName);
		}
		if (oldAttrValue != null) {
			// exists, delete it
			newElement.removeAttribute(attrName);
			if (logger.isFineEnabled())
				logger.fine("attribute found and deleted");
		} else {
			// does not exists
			if (logger.isFineEnabled())
				logger.fine("attribute to delete not found");
			throw new NotFoundException();
		}

		if (logger.isFineEnabled())
			logger.fine("validating document after delete");
		// validate the updated document against it's schema
		appUsage.validateSchema(newDocumentDOM);

		if (logger.isFineEnabled())
			logger.fine("checking app usage constraints and resource interdependencies...");
		// verify app usage constraints
		appUsage.checkConstraintsOnDelete(newDocumentDOM,
				CONFIGURATION.getXcapRoot(), documentSelector,
				appUsageDataSource);

		// create new etag
		String newETag = ETagGenerator.generate(documentSelector.toString());

		// process resource interdependencies
		if (processResourceInterdependencies) {
			try {
				appUsage.processResourceInterdependenciesOnDeleteAttribute(
						documentSelector, newETag, nodeSelector,
						elementSelector, attributeSelector, this,
						appUsageDataSource);
			} catch (SchemaValidationErrorConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (UniquenessFailureConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (ConstraintFailureConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (InternalServerErrorException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			}
		}
		if (logger.isFineEnabled())
			logger.fine("app usage resource interdependencies processed for "
					+ documentSelector);

		// update data source with document
		try {
			String newDocumentString = TextWriter.toString(newDocumentDOM);
			dataSourceSbbInterface.updateAttribute(documentSelector,
					appUsage.getDefaultDocumentNamespace(), oldDocument,
					newDocumentDOM, newDocumentString, newETag, nodeSelector,
					attributeSelector, oldAttrValue, null);
			if (logger.isFineEnabled())
				logger.fine("document updated in data source");
		} catch (Exception e) {
			if (!sbbContext.getRollbackOnly())
				sbbContext.setRollbackOnly();
			throw new InternalServerErrorException(
					"Failed to serialize resulting dom document to string");
		}

		return new OKWriteResult(newETag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openxdm.xcap.server.slee.RequestProcessor#get(org.openxdm.xcap.common
	 * .uri.ResourceSelector, java.lang.String)
	 */
	@Override
	public ReadResult get(ResourceSelector resourceSelector,
			String authenticatedUser) throws NotFoundException,
			InternalServerErrorException, BadRequestException,
			NotAuthorizedRequestException {

		AppUsage appUsage = null;

		try {
			// parse document parent String
			DocumentSelector documentSelector = DocumentSelector
					.valueOf(resourceSelector.getDocumentSelector());
			// get app usage from cache
			appUsage = APPUSAGE_MANAGEMENT.getAppUsage(documentSelector
					.getAUID());
			if (appUsage == null) {
				// throw exception
				if (logger.isFineEnabled())
					logger.fine("appusage not found");
				throw new NotFoundException();
			}
			// authorize user
			if (authenticatedUser != null
					&& !appUsage.getAuthorizationPolicy().isAuthorized(
							authenticatedUser,
							AuthorizationPolicy.Operation.GET,
							documentSelector, appUsageDataSource)) {
				throw new NotAuthorizedRequestException();
			}
			// get document
			org.openxdm.xcap.common.datasource.Document document = dataSourceSbbInterface
					.getDocument(documentSelector);
			if (document == null) {
				// throw exception
				if (logger.isFineEnabled())
					logger.fine("document not found");
				throw new NotFoundException();
			}
			if (logger.isFineEnabled())
				logger.fine("document found");
			// get document's etag
			String eTag = document.getETag();
			// check node selector string from resource selector
			if (resourceSelector.getNodeSelector() != null) {
				// elem, attrib or namespace bind
				// parse node selector
				NodeSelector nodeSelector = Parser.parseNodeSelector(
						resourceSelector.getNodeSelector(),
						resourceSelector.getNamespaceContext());
				if (logger.isFineEnabled())
					logger.fine("node selector found and parsed");
				// create xpath
				XPath xpath = DomUtils.XPATH_FACTORY.newXPath();
				// add a namespace context to xpath to resolve bindings
				// config namespace context
				final NamespaceContext nsContext = resourceSelector
						.getNamespaceContext();
				nsContext.setDefaultDocNamespace(appUsage
						.getDefaultDocumentNamespace());
				xpath.setNamespaceContext(nsContext);
				if (logger.isFineEnabled())
					logger.fine("xpath initiated with namespace context");
				// get document as dom
				org.w3c.dom.Document domDocument = document.getAsDOMDocument();
				final Element element = getElementForDeleteOrGet(domDocument,
						nodeSelector, false);
				if (nodeSelector.getTerminalSelector() != null) {
					// parse terminal selector
					TerminalSelector terminalSelector = Parser
							.parseTerminalSelector(nodeSelector
									.getTerminalSelector());
					if (logger.isFineEnabled())
						logger.fine("terminal selector found and parsed");
					if (terminalSelector instanceof AttributeSelector) {
						// attribute selector, get attribute
						if (logger.isFineEnabled())
							logger.fine("terminal selector is an attribute selector");
						Attr attr = element
								.getAttributeNode(((AttributeSelector) terminalSelector)
										.getAttName());
						if (attr != null) {
							// exists, return its value
							if (logger.isFineEnabled())
								logger.fine("attribute found, returning result");
							return new ReadResult(eTag, new AttributeResource(
									attr.getNodeValue()));
						} else {
							// does not exists
							if (logger.isFineEnabled())
								logger.fine("attribute to retreive not found");
							throw new NotFoundException();
						}
					} else {
						// namespace selector, get namespace bindings
						if (logger.isFineEnabled())
							logger.fine("terminal selector is a namespace selector");
						return new ReadResult(eTag, getNamespaceBindings(
								element, DomUtils.getElementName(element),
								nsContext.getNamespaces()));
					}
				} else {
					// element
					if (logger.isFineEnabled())
						logger.fine("terminal selector not found, returining result with the element found");
					return new ReadResult(eTag, new ElementResource(
							TextWriter.toString(element)));
				}

			} else {
				// no node selector, just get the document
				if (logger.isFineEnabled())
					logger.fine("node selector not found, returning the document");
				return new ReadResult(eTag, new DocumentResource(
						document.getAsString(), appUsage.getMimetype()));
			}
		} catch (ParseException e) {
			if (logger.isFineEnabled())
				logger.fine("error in parsing uri.");
			throw new NotFoundException();
		} catch (TransformerException e) {
			logger.severe("unable to transform dom element to text.", e);
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	private NamespaceBindings getNamespaceBindings(Node element,
			String elementName, Map<String, String> namespacesToGet)
			throws NotFoundException {

		boolean done = false;
		// init result namespaces map
		Map<String, String> result = new HashMap<String, String>();
		// remove empty prefix from "namespaces to get" map
		namespacesToGet.remove("");
		// create set of namespaces uri to get
		Collection<String> namespacesUris = namespacesToGet.values();

		while (done == false && element.getNodeType() == Node.ELEMENT_NODE) {
			// get element attributes
			NamedNodeMap elementAttributes = element.getAttributes();
			// process each one
			for (int i = 0; i < elementAttributes.getLength(); i++) {
				Node attributeNode = elementAttributes.item(i);
				if (attributeNode.getNodeName().compareTo("xmlns") == 0
						|| attributeNode.getPrefix().compareTo("xmlns") == 0) {
					// its a namespace
					if (namespacesUris.contains(attributeNode.getNodeValue())) {
						// it was requested, add it to the result map
						result.put(attributeNode.getNodeName(),
								attributeNode.getNodeValue());
						if (result.size() == namespacesUris.size()) {
							done = true;
							break;
						}
					}
				}
			}
			// move to parent
			element = element.getParentNode();
		}

		if (!done) {
			// at least one was not found
			if (logger.isFineEnabled())
				logger.fine("didn't found any namespace binding, returning not found");
			throw new NotFoundException();
		} else {
			// return namespace bindings
			if (logger.isFineEnabled())
				logger.fine("found namespace binding(s)");
			return new NamespaceBindings(elementName, result);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openxdm.xcap.server.slee.RequestProcessor#put(org.openxdm.xcap.common
	 * .uri.ResourceSelector, java.lang.String, java.io.InputStream,
	 * org.openxdm.xcap.server.etag.ETagValidator, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public WriteResult put(ResourceSelector resourceSelector, String mimetype,
			InputStream contentStream, ETagValidator eTagValidator,
			String xcapRoot, String authenticatedUser)
			throws ConflictException, MethodNotAllowedException,
			UnsupportedMediaTypeException, InternalServerErrorException,
			PreconditionFailedException, BadRequestException,
			NotAuthorizedRequestException {

		DocumentSelector documentSelector = null;
		try {
			// parse document parent String
			documentSelector = DocumentSelector.valueOf(resourceSelector
					.getDocumentSelector());
			if (logger.isFineEnabled())
				logger.fine("document selector found and parsed: "
						+ documentSelector);
		} catch (ParseException e) {
			// invalid document selector, throw no parent exception
			if (logger.isFineEnabled())
				logger.fine("failed to parse document selector, returning no parent conflict");
			throw new NoParentConflictException(xcapRoot);
		}

		// get app usage
		final AppUsage appUsage = APPUSAGE_MANAGEMENT
				.getAppUsage(documentSelector.getAUID());
		if (appUsage == null) {
			// throw exception
			if (logger.isFineEnabled())
				logger.fine("appusage " + documentSelector.getAUID()
						+ " not found");
			throw new NoParentConflictException(xcapRoot);
		}
		if (logger.isFineEnabled())
			logger.fine("appusage " + documentSelector.getAUID() + " found");

		// authorize user
		if (authenticatedUser != null
				&& !appUsage.getAuthorizationPolicy().isAuthorized(
						authenticatedUser, AuthorizationPolicy.Operation.PUT,
						documentSelector, appUsageDataSource)) {
			throw new NotAuthorizedRequestException();
		}

		// try to get document's resource
		org.openxdm.xcap.common.datasource.Document oldDocument = dataSourceSbbInterface
				.getDocument(documentSelector);
		if (oldDocument != null) {
			// validate etag if needed
			if (eTagValidator != null) {
				eTagValidator.validate(oldDocument.getETag());
				if (logger.isFineEnabled())
					logger.fine("document etag validated");
			} else {
				if (logger.isFineEnabled())
					logger.fine("document etag validation not required");
			}
		}

		if (resourceSelector.getNodeSelector() != null) {
			// put elem, attr, namespaces

			if (oldDocument == null) {
				// doc does not exists, throw exception
				throw new NoParentConflictException(xcapRoot
						+ documentSelector.getCollection());
			}

			NodeSelector nodeSelector = null;
			ElementSelector elementSelector = null;
			try {

				// parse node selector
				nodeSelector = Parser.parseNodeSelector(
						resourceSelector.getNodeSelector(),
						resourceSelector.getNamespaceContext());
				elementSelector = Parser.parseElementSelector(nodeSelector
						.getElementSelector());
				if (logger.isFineEnabled())
					logger.fine("node selector found and parsed: "
							+ nodeSelector);
			} catch (ParseException e) {
				// unable to parse the node selector, throw no parent
				// exception with the document as the existent ancestor
				if (logger.isFineEnabled())
					logger.fine("unable to parse the node selector or element selector, returning no parent conflict with the document as the existent ancestor");
				throw new NoParentConflictException(xcapRoot
						+ resourceSelector.getDocumentSelector());
			}
			// config namespace context
			final NamespaceContext namespaceContext = resourceSelector
					.getNamespaceContext();
			namespaceContext.setDefaultDocNamespace(appUsage
					.getDefaultDocumentNamespace());
			// clone doc
			final Document newDocumentDOM = DocumentCloner.clone(oldDocument
					.getAsDOMDocument());
			// get element
			final Element element = getElementForPut(documentSelector,
					newDocumentDOM, nodeSelector, false);
			if (nodeSelector.getTerminalSelector() != null) {
				// put attr or namespaces
				try {
					TerminalSelector terminalSelector = Parser
							.parseTerminalSelector(nodeSelector
									.getTerminalSelector());
					if (terminalSelector instanceof AttributeSelector) {
						// verify mimetype
						if (mimetype == null
								|| !mimetype.equals(AttributeResource.MIMETYPE)) {
							// mimetype is not correct
							throw new UnsupportedMediaTypeException();
						}
						// read attribute value (checking if is utf-8 too)
						final String newAttributeValue = XMLValidator
								.getUTF8String(contentStream);
						if (logger.isFineEnabled())
							logger.fine("attr content is utf-8");
						return putAttribute(oldDocument, newDocumentDOM,
								element, documentSelector, nodeSelector,
								elementSelector,
								(AttributeSelector) terminalSelector,
								newAttributeValue, appUsage, true);
					} else {
						if (logger.isFineEnabled())
							logger.fine("terminal selector is a namespace selector, not allowed on put");
						Map<String, String> map = new HashMap<String, String>();
						map.put("Allow", "GET");
						throw new MethodNotAllowedException(map);
					}
				} catch (ParseException e) {
					// unable to parse the node selector, throw no parent
					// exception with the document as the existent ancestor
					if (logger.isFineEnabled())
						logger.fine("unable to parse the node selector or element selector, returning no parent conflict with the document as the existent ancestor");
					throw new NoParentConflictException(xcapRoot
							+ resourceSelector.getDocumentSelector());
				}
			} else {
				// check mimetype
				if (mimetype == null
						|| !mimetype.equals(ElementResource.MIMETYPE)) {
					// mimetype is not correct
					throw new UnsupportedMediaTypeException();
				}
				// read and verify if content value is utf-8
				final String newElementAsString = XMLValidator
						.getUTF8String(contentStream);
				if (logger.isFineEnabled())
					logger.fine("content is utf-8");
				// create XML fragment node
				final Element newElement = XMLValidator
						.getWellFormedDocumentFragment(new StringReader(
								newElementAsString));
				if (logger.isFineEnabled())
					logger.fine("content is well formed document fragment");
				return putElement(oldDocument, newDocumentDOM, element,
						documentSelector, nodeSelector, elementSelector,
						newElement, appUsage, true);
			}
		} else {
			// put document

			// validate mimetype
			if (mimetype == null || !mimetype.equals(appUsage.getMimetype())) {
				// mimetype is not valid
				if (logger.isFineEnabled())
					logger.fine("invalid mimetype, does not matches the app usage");
				throw new UnsupportedMediaTypeException();
			}
			// verify if content is utf-8
			final Reader utf8reader = XMLValidator.getUTF8Reader(contentStream);
			if (logger.isFineEnabled())
				logger.fine("document content is utf-8");
			// build new document
			final Document newDomDocument = XMLValidator
					.getWellFormedDocument(utf8reader);
			if (logger.isFineEnabled())
				logger.fine("document content is well formed");
			return putDocument(documentSelector, oldDocument, newDomDocument,
					appUsage, true);
		}
	}

	// -- APP USAGE REQUEST PROCESSOR METHODS

	private WriteResult putDocument(DocumentSelector documentSelector,
			org.openxdm.xcap.common.datasource.Document oldDocument,
			Document newDocumentDOM, AppUsage appUsage,
			boolean processResourceInterdependencies) throws ConflictException,
			MethodNotAllowedException, UnsupportedMediaTypeException,
			InternalServerErrorException, PreconditionFailedException,
			BadRequestException, NotAuthorizedRequestException {

		Document oldDomDocument = null;
		if (oldDocument == null) { // DOCUMENTS DOES NOT EXIST
			if (logger.isFineEnabled())
				logger.fine("document not found");
		} else {
			// DOCUMENT EXISTS
			if (logger.isFineEnabled())
				logger.fine("document found");
			oldDomDocument = oldDocument.getAsDOMDocument();
		}

		// validate the updated document against it's schema
		appUsage.validateSchema(newDocumentDOM);
		if (logger.isFineEnabled())
			logger.fine("document validated by schema");

		// verify app usage constraints
		appUsage.checkConstraintsOnPut(newDocumentDOM,
				CONFIGURATION.getXcapRoot(), documentSelector,
				appUsageDataSource);
		if (logger.isFineEnabled())
			logger.fine("app usage constraints checked");

		// create new document etag
		String newETag = ETagGenerator.generate(documentSelector.toString());
		if (logger.isFineEnabled())
			logger.fine("new document etag generated");

		// process resource interdependencies for the request app usage
		if (processResourceInterdependencies) {
			try {
				appUsage.processResourceInterdependenciesOnPutDocument(
						oldDomDocument, newDocumentDOM, documentSelector,
						newETag, this, appUsageDataSource);
			} catch (SchemaValidationErrorConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (UniquenessFailureConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (InternalServerErrorException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (ConstraintFailureConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			}
		}
		if (logger.isFineEnabled())
			logger.fine("app usage resource interdependencies processed");

		// update data source with document
		try {
			String newDocumentString = TextWriter.toString(newDocumentDOM);
			if (oldDocument == null) {
				dataSourceSbbInterface.createDocument(documentSelector,
						appUsage.getDefaultDocumentNamespace(), newDocumentDOM,
						newDocumentString, newETag);
				if (logger.isFineEnabled())
					logger.fine("document created in data source");
			} else {
				dataSourceSbbInterface.updateDocument(documentSelector,
						appUsage.getDefaultDocumentNamespace(), oldDocument,
						newDocumentDOM, newDocumentString, newETag);
				if (logger.isFineEnabled())
					logger.fine("document updated in data source");
			}
		} catch (Exception e) {
			logger.severe(
					"Failed to serialize resulting dom document to string", e);
			throw new InternalServerErrorException(
					"Failed to serialize resulting dom document to string", e);
		}

		return oldDocument == null ? new CreatedWriteResult(newETag)
				: new OKWriteResult(newETag);

	}

	private WriteResult putElement(
			final org.openxdm.xcap.common.datasource.Document oldDocument,
			final Document newDocumentDOM, final Element oldElement,
			final DocumentSelector documentSelector,
			final NodeSelector nodeSelector,
			final ElementSelector elementSelector, Element newElement,
			final AppUsage appUsage, boolean processResourceInterdependencies)
			throws InternalServerErrorException, NoParentConflictException,
			SchemaValidationErrorConflictException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException, CannotInsertConflictException,
			NotValidXMLFragmentConflictException, NotUTF8ConflictException,
			BadRequestException, NotAuthorizedRequestException {

		if (logger.isFineEnabled())
			logger.fine("putting element " + elementSelector + " in "
					+ documentSelector);

		if (oldElement != null) {
			// replace element
			// verify if cannot insert
			ElementSelectorStep lastElementSelectorStep = elementSelector
					.getLastStep();
			// if element's tag name is not equal to
			// this step's name then cannot insert
			if (!newElement.getTagName().equals(
					lastElementSelectorStep.getName())) {
				if (logger.isFineEnabled())
					logger.fine("element's tag name is not equal to this step's name, cannot insert");
				throw new CannotInsertConflictException();
			}
			if (lastElementSelectorStep instanceof ElementSelectorStepByAttr) {
				ElementSelectorStepByAttr elementSelectorStepByAttr = (ElementSelectorStepByAttr) lastElementSelectorStep;
				// check attr value
				String elementAttrValue = newElement
						.getAttribute(elementSelectorStepByAttr.getAttrName());
				if (elementAttrValue == null
						|| !elementAttrValue.equals(elementSelectorStepByAttr
								.getAttrValue())) {
					if (logger.isFineEnabled())
						logger.fine("element selector's last step has an attr and it's new value changes this attr value, cannot insert");
					throw new CannotInsertConflictException();
				}
			}
			// import the element node
			newElement = (Element) newDocumentDOM.importNode(newElement, true);
			// replace node
			oldElement.getParentNode().replaceChild(newElement, oldElement);
			if (logger.isFineEnabled())
				logger.fine("element " + elementSelector + " replaced in "
						+ documentSelector);
		} else {
			// new element
			final Element elementParent = getElementForPut(documentSelector,
					newDocumentDOM, nodeSelector, true);
			if (elementParent == null) {
				if (logger.isFineEnabled())
					logger.fine("element parent not found, returning no parent conflict");
				XPath xpath = DomUtils.XPATH_FACTORY.newXPath();
				xpath.setNamespaceContext(nodeSelector.getNamespaceContext());
				throw new NoParentConflictException(getElementExistentAncestor(
						CONFIGURATION.getXcapRoot(),
						documentSelector.toString(),
						nodeSelector.getElementParentSelectorWithEmptyPrefix(),
						newDocumentDOM, xpath));
			} else {
				// put new element
				newElement = (Element) newDocumentDOM.importNode(newElement,
						true);

				// get element step
				ElementSelectorStep elementLastStep = elementSelector
						.getLastStep();

				// get element name & namespace
				String elementNamespace = null;
				String elementName = null;
				String elementNamePrefix = elementLastStep.getPrefix();
				if (elementNamePrefix != null) {
					// get element name without prefix
					elementName = elementLastStep.getNameWithoutPrefix();
					// and get namespace
					elementNamespace = nodeSelector.getNamespaceContext()
							.getNamespaceURI(elementNamePrefix);
				} else {
					// get element name without prefix
					elementName = elementLastStep.getName();
					// and get namespace
					elementNamespace = nodeSelector.getNamespaceContext()
							.getNamespaceURI("");
				}

				// if new element node name is not the same as in the uri then
				// cannot
				// insert
				if (!newElement.getNodeName().equals(elementName)) {
					if (logger.isFineEnabled())
						logger.fine("element node name is not the same as in the uri, cannot insert");
					throw new CannotInsertConflictException();
				}

				if (elementLastStep instanceof ElementSelectorStepByPos) {
					// position defined
					if (logger.isFineEnabled())
						logger.fine("element selector's last step with position defined");
					ElementSelectorStepByPos elementSelectorStepByPos = (ElementSelectorStepByPos) elementLastStep;
					if (elementSelectorStepByPos.getPos() == 1) {
						// POS = 1
						if (!(elementLastStep instanceof ElementSelectorStepByPosAttr)) {
							// NO ATTR TEST, *[1] e name[1], either way, just
							// append to
							// the parent
							if (logger.isFineEnabled())
								logger.fine("element selector's last step without attr test defined");
							elementParent.appendChild(newElement);
							if (logger.isFineEnabled())
								logger.fine("element appended to parent");
						} else {
							// ATTR TEST
							if (logger.isFineEnabled())
								logger.fine("element selector's last step with attr test defined");
							// verify that the element has this step atribute
							// with this
							// step attribute value, if not it cannot insert
							ElementSelectorStepByPosAttr elementSelectorStepByPosAttr = (ElementSelectorStepByPosAttr) elementLastStep;
							String elementAttrName = elementSelectorStepByPosAttr
									.getAttrName();
							String elementAttrValue = newElement
									.getAttribute(elementAttrName);
							if (elementAttrValue == null
									|| !elementAttrValue
											.equals(elementSelectorStepByPosAttr
													.getAttrValue())) {
								if (logger.isFineEnabled())
									logger.fine("element selector's last step has an atribute and the attribute value does not matches, cannot insert");
								throw new CannotInsertConflictException();
							}
							// *[1][attr-test], insert before the first element
							// name[1][attr-test], insert before the first
							// element with
							// same name
							NodeList elementParentChilds = elementParent
									.getChildNodes();
							boolean inserted = false;
							for (int i = 0; i < elementParentChilds.getLength(); i++) {
								if (elementParentChilds.item(i) instanceof Element
										&& ((elementName
												.equals(elementParentChilds
														.item(i).getNodeName()) && elementParentChilds
												.item(i).getNamespaceURI()
												.equals(elementNamespace)) || (elementName
												.equals("*")))) {
									elementParent.insertBefore(newElement,
											elementParentChilds.item(i));
									if (logger.isFineEnabled())
										logger.fine("element inserted at pos "
												+ i);
									inserted = true;
									break;
								}
							}
							if (!inserted) {
								// didn't found an element just append to parent
								elementParent.appendChild(newElement);
								if (logger.isFineEnabled())
									logger.fine("element appended to parent");
							}
						}
					}

					else {
						// POS > 1, must find the pos-1 element and insert after
						if (elementLastStep instanceof ElementSelectorStepByPosAttr) {
							// ATTR TEST
							if (logger.isFineEnabled())
								logger.fine("element selector's last step with attr test defined");
							// verify that the element has this step atribute
							// with this
							// step attribute value, if not it cannot insert
							ElementSelectorStepByPosAttr elementSelectorStepByPosAttr = (ElementSelectorStepByPosAttr) elementLastStep;
							String elementAttrName = elementSelectorStepByPosAttr
									.getAttrName();
							String elementAttrValue = newElement
									.getAttribute(elementAttrName);
							if (elementAttrValue == null
									|| !elementAttrValue
											.equals(elementSelectorStepByPosAttr
													.getAttrValue())) {
								if (logger.isFineEnabled())
									logger.fine("element selector's last step has an atribute and the attribute value does not matches, cannot insert");
								throw new CannotInsertConflictException();
							}
						}
						// *[pos>1], name[pos>1], *[pos>1][attr-test],
						// name[pos>1][attr-test], insert in the parent after
						// the pos-1
						// element
						NodeList elementParentChilds = elementParent
								.getChildNodes();
						boolean inserted = false;
						int elementsFound = 0;
						for (int i = 0; i < elementParentChilds.getLength(); i++) {
							if (elementParentChilds.item(i) instanceof Element
									&& ((elementName.equals(elementParentChilds
											.item(i).getNodeName()) && elementParentChilds
											.item(i).getNamespaceURI()
											.equals(elementNamespace)) || (elementName
											.equals("*")))) {
								elementsFound++;
								if (elementsFound == elementSelectorStepByPos
										.getPos() - 1) {
									// insert after
									if (i == elementParentChilds.getLength() - 1) {
										// no node after, use append
										elementParent.appendChild(newElement);
										if (logger.isFineEnabled())
											logger.fine("element appended to parent");
									} else {
										// node after exists, insert before
										elementParent
												.insertBefore(newElement,
														elementParentChilds
																.item(i + 1));
										if (logger.isFineEnabled())
											logger.fine("element inserted at pos "
													+ i + 1);
									}
									inserted = true;
									break;
								}
							}
						}
						if (!inserted) {
							// didn't found pos-1 element, cannot insert
							if (logger.isFineEnabled())
								logger.fine("didn't found "
										+ (elementSelectorStepByPos.getPos() - 1)
										+ " element, cannot insert");
							throw new CannotInsertConflictException();
						}
					}
				}

				else if (elementLastStep instanceof ElementSelectorStepByAttr) {
					// no position defined
					if (logger.isFineEnabled())
						logger.fine("element selector's last step with attr test defined only");
					// first verify element has this step atribute with this
					// step
					// attribute value, if not it cannot insert
					ElementSelectorStepByAttr elementSelectorStepByAttr = (ElementSelectorStepByAttr) elementLastStep;
					String elementAttrValue = newElement
							.getAttribute(elementSelectorStepByAttr
									.getAttrName());
					if (elementAttrValue == null
							|| !elementAttrValue
									.equals(elementSelectorStepByAttr
											.getAttrValue())) {
						if (logger.isFineEnabled())
							logger.fine("element selector's last step has an atribute and the attribute value does not matches, cannot insert");
						throw new CannotInsertConflictException();
					}
					// insert after the last with same name
					NodeList elementParentChilds = elementParent
							.getChildNodes();
					boolean inserted = false;
					for (int i = elementParentChilds.getLength() - 1; i > -1; i--) {
						if (elementParentChilds.item(i) instanceof Element) {
							if (elementParentChilds.item(i) instanceof Element
									&& ((elementName.equals(elementParentChilds
											.item(i).getNodeName()) && elementParentChilds
											.item(i).getNamespaceURI()
											.equals(elementNamespace)) || (elementName
											.equals("*")))) {
								// insert after this element
								if (i == elementParentChilds.getLength() - 1) {
									elementParent.appendChild(newElement);
									if (logger.isFineEnabled())
										logger.fine("element appended to parent");
								} else {
									elementParent.insertBefore(newElement,
											elementParentChilds.item(i + 1));
									if (logger.isFineEnabled())
										logger.fine("element inserted at pos "
												+ i + 1);
								}
								inserted = true;
								break;
							}
						}
					}
					if (!inserted) {
						// didn't found an element with same name and namespace,
						// just
						// append to parent
						elementParent.appendChild(newElement);
						if (logger.isFineEnabled())
							logger.fine("element appended to parent");
					}
				}

				else {
					// no position and attr defined, it's the first child or the
					// first
					// with this name so just append new element
					elementParent.appendChild(newElement);
					if (logger.isFineEnabled())
						logger.fine("element selector's last step without attr test or position defined, element appended to parent");
				}
				if (logger.isFineEnabled())
					logger.fine("element parent found, new element added");
			}

		}

		// validate the updated document against it's schema
		appUsage.validateSchema(newDocumentDOM);
		if (logger.isFineEnabled())
			logger.fine("document validated by schema");

		// verify app usage constraints
		appUsage.checkConstraintsOnPut(newDocumentDOM,
				CONFIGURATION.getXcapRoot(), documentSelector,
				appUsageDataSource);
		if (logger.isFineEnabled())
			logger.fine("app usage constraints checked");

		// create new document etag
		String newETag = ETagGenerator.generate(documentSelector.toString());
		if (logger.isFineEnabled())
			logger.fine("new document etag generated");

		// process resource interdependencies for the request app usage
		if (processResourceInterdependencies) {
			try {
				appUsage.processResourceInterdependenciesOnPutElement(
						oldElement, newElement, newDocumentDOM,
						documentSelector, newETag, nodeSelector,
						elementSelector, this, appUsageDataSource);
			} catch (SchemaValidationErrorConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (UniquenessFailureConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (InternalServerErrorException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (ConstraintFailureConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			}
		}
		if (logger.isFineEnabled())
			logger.fine("app usage resource interdependencies processed");

		// update data source with document
		try {
			String newDocumentString = TextWriter.toString(newDocumentDOM);
			dataSourceSbbInterface.updateElement(documentSelector,
					appUsage.getDefaultDocumentNamespace(), oldDocument,
					newDocumentDOM, newDocumentString, newETag, nodeSelector,
					oldElement, newElement);
			if (logger.isFineEnabled())
				logger.fine("document updated in data source");
		} catch (Exception e) {
			logger.severe(
					"Failed to serialize resulting dom document to string", e);
			throw new InternalServerErrorException(
					"Failed to serialize resulting dom document to string", e);
		}

		return oldElement == null ? new CreatedWriteResult(newETag)
				: new OKWriteResult(newETag);
	}

	private WriteResult putAttribute(
			final org.openxdm.xcap.common.datasource.Document oldDocument,
			final Document newDocumentDOM, final Element newElement,
			final DocumentSelector documentSelector,
			final NodeSelector nodeSelector,
			final ElementSelector elementSelector,
			final AttributeSelector attributeSelector,
			final String newAttributeValue, final AppUsage appUsage,
			boolean processResourceInterdependencies)
			throws InternalServerErrorException, NoParentConflictException,
			SchemaValidationErrorConflictException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException,
			NotXMLAttributeValueConflictException, BadRequestException,
			CannotInsertConflictException, NotAuthorizedRequestException {

		if (logger.isFineEnabled())
			logger.fine("putting attribute " + attributeSelector
					+ " in element " + elementSelector + ", in doc "
					+ documentSelector);

		String oldAttributeValue = null;

		if (newElement == null) {
			// throw no parent exception since there is
			// no element but we have a terminal
			// selector
			XPath xpath = DomUtils.XPATH_FACTORY.newXPath();
			xpath.setNamespaceContext(nodeSelector.getNamespaceContext());
			throw new NoParentConflictException(getElementExistentAncestor(
					CONFIGURATION.getXcapRoot(), documentSelector.toString(),
					nodeSelector.getElementSelectorWithEmptyPrefix(),
					newDocumentDOM, xpath));
		}

		// verify if attribute value is
		// valid AttValue
		XMLValidator.checkAttValue(newAttributeValue);
		if (logger.isFineEnabled())
			logger.fine("attr value is valid AttValue");

		// get attribute
		final Attr attribute = newElement.getAttributeNode(attributeSelector
				.getAttName());
		if (attribute != null) {
			// ATTR EXISTS
			if (logger.isFineEnabled())
				logger.fine("attr found in document");
			oldAttributeValue = attribute.getNodeValue();

			// verify if cannot insert,
			// e.g .../x[id1="1"]/@id1
			// and attValue = 2
			ElementSelectorStep lastElementSelectorStep = elementSelector
					.getLastStep();
			if (lastElementSelectorStep instanceof ElementSelectorStepByAttr) {
				ElementSelectorStepByAttr elementSelectorByAttr = (ElementSelectorStepByAttr) lastElementSelectorStep;
				// if this step attr
				// name is the specified
				// attrName and this
				// step attrValue is not
				// the same as the
				// specified attr value,
				// it cannot insert
				if (elementSelectorByAttr.getAttrName().equals(
						attributeSelector.getAttName())
						&& !elementSelectorByAttr.getAttrValue().equals(
								newAttributeValue)) {
					if (logger.isFineEnabled())
						logger.fine("element selector's last step attr name is the specified attrName and this step attrValue is not the same as the specified attr value, cannot insert");
					throw new CannotInsertConflictException();
				}
			}

		} else { // ATTR DOES NOT EXISTS
			if (logger.isFineEnabled())
				logger.fine("attr not found in document");
		}
		// set attribute
		newElement.setAttributeNS(null, attributeSelector.getAttName(),
				newAttributeValue);
		if (logger.isFineEnabled())
			logger.fine("attr set");

		// validate the updated document against it's schema
		appUsage.validateSchema(newDocumentDOM);
		if (logger.isFineEnabled())
			logger.fine("document validated by schema");

		// verify app usage constraints
		appUsage.checkConstraintsOnPut(newDocumentDOM,
				CONFIGURATION.getXcapRoot(), documentSelector,
				appUsageDataSource);
		if (logger.isFineEnabled())
			logger.fine("app usage constraints checked");

		// create new document etag
		String newETag = ETagGenerator.generate(documentSelector.toString());
		if (logger.isFineEnabled())
			logger.fine("new document etag generated");

		// process resource interdependencies for the request app usage
		if (processResourceInterdependencies) {
			try {
				appUsage.processResourceInterdependenciesOnPutAttribute(
						oldAttributeValue, newAttributeValue, documentSelector,
						newETag, nodeSelector, elementSelector,
						attributeSelector, this, appUsageDataSource);
			} catch (SchemaValidationErrorConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (UniquenessFailureConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (InternalServerErrorException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			} catch (ConstraintFailureConflictException e) {
				if (!sbbContext.getRollbackOnly())
					sbbContext.setRollbackOnly();
				throw e;
			}
		}
		if (logger.isFineEnabled())
			logger.fine("app usage resource interdependencies processed");

		// update data source with document
		try {
			String newDocumentString = TextWriter.toString(newDocumentDOM);
			dataSourceSbbInterface.updateAttribute(documentSelector,
					appUsage.getDefaultDocumentNamespace(), oldDocument,
					newDocumentDOM, newDocumentString, newETag, nodeSelector,
					attributeSelector, oldAttributeValue, newAttributeValue);
			if (logger.isFineEnabled())
				logger.fine("document updated in data source");
		} catch (Exception e) {
			logger.severe(
					"Failed to serialize resulting dom document to string", e);
			throw new InternalServerErrorException(
					"Failed to serialize resulting dom document to string", e);
		}

		return attribute != null ? new OKWriteResult(newETag)
				: new CreatedWriteResult(newETag);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AppUsageRequestProcessor#deleteDocument
	 * (org.openxdm.xcap.common.uri.DocumentSelector,
	 * org.mobicents.xdm.server.appusage.AppUsage)
	 */
	@Override
	public void deleteDocument(DocumentSelector documentSelector,
			AppUsage appUsage) throws InternalServerErrorException,
			NotFoundException, SchemaValidationErrorConflictException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException {

		// get document
		org.openxdm.xcap.common.datasource.Document document = dataSourceSbbInterface
				.getDocument(documentSelector);
		if (document == null) {
			// throw exception
			if (logger.isFineEnabled())
				logger.fine("document " + documentSelector + " not found");
			throw new NotFoundException();
		}
		;

		// document exists
		if (logger.isFineEnabled())
			logger.fine("document " + documentSelector + " found");

		deleteDocument(document, documentSelector, appUsage, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AppUsageRequestProcessor#deleteElement
	 * (org.openxdm.xcap.common.uri.DocumentSelector,
	 * org.openxdm.xcap.common.uri.NodeSelector,
	 * org.openxdm.xcap.common.uri.ElementSelector,
	 * org.openxdm.xcap.common.xml.NamespaceContext,
	 * org.mobicents.xdm.server.appusage.AppUsage)
	 */
	@Override
	public void deleteElement(final DocumentSelector documentSelector,
			final NodeSelector nodeSelector,
			final ElementSelector elementSelector, AppUsage appUsage)
			throws InternalServerErrorException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException, NotFoundException,
			CannotDeleteConflictException,
			SchemaValidationErrorConflictException, BadRequestException {

		// get document
		final org.openxdm.xcap.common.datasource.Document oldDocument = dataSourceSbbInterface
				.getDocument(documentSelector);
		if (oldDocument == null) {
			// throw exception
			if (logger.isFineEnabled())
				logger.fine("document not found");
			throw new NotFoundException();
		}

		// clone doc
		final Document newDocumentDOM = DocumentCloner.clone(oldDocument
				.getAsDOMDocument());

		// get element
		final Element newElement = getElementForDeleteOrGet(newDocumentDOM,
				nodeSelector, false);

		deleteElement(oldDocument, newDocumentDOM, newElement,
				documentSelector, nodeSelector, elementSelector, appUsage,
				false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AppUsageRequestProcessor#deleteAttribute
	 * (org.openxdm.xcap.common.uri.DocumentSelector,
	 * org.openxdm.xcap.common.uri.NodeSelector,
	 * org.openxdm.xcap.common.uri.ElementSelector,
	 * org.openxdm.xcap.common.uri.AttributeSelector,
	 * org.mobicents.xdm.server.appusage.AppUsage)
	 */
	@Override
	public void deleteAttribute(final DocumentSelector documentSelector,
			final NodeSelector nodeSelector,
			final ElementSelector elementSelector,
			final AttributeSelector attributeSelector, final AppUsage appUsage)
			throws InternalServerErrorException, BadRequestException,
			NotFoundException, SchemaValidationErrorConflictException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException {

		// get document
		final org.openxdm.xcap.common.datasource.Document oldDocument = dataSourceSbbInterface
				.getDocument(documentSelector);
		if (oldDocument == null) {
			// throw exception
			if (logger.isFineEnabled())
				logger.fine("document not found");
			throw new NotFoundException();
		}

		// clone doc
		final Document newDocumentDOM = DocumentCloner.clone(oldDocument
				.getAsDOMDocument());

		// get element
		final Element newElement = getElementForDeleteOrGet(newDocumentDOM,
				nodeSelector, false);
		deleteAttribute(oldDocument, newDocumentDOM, newElement,
				documentSelector, nodeSelector, elementSelector,
				attributeSelector, appUsage, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AppUsageRequestProcessor#putDocument
	 * (org.openxdm.xcap.common.uri.DocumentSelector, org.w3c.dom.Document,
	 * org.mobicents.xdm.server.appusage.AppUsage)
	 */
	@Override
	public boolean putDocument(DocumentSelector documentSelector,
			Document newDomDocument, AppUsage appUsage)
			throws InternalServerErrorException, NoParentConflictException,
			SchemaValidationErrorConflictException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException, ConflictException,
			MethodNotAllowedException, UnsupportedMediaTypeException,
			PreconditionFailedException, BadRequestException,
			NotAuthorizedRequestException {

		// try to get document's resource
		org.openxdm.xcap.common.datasource.Document oldDocument = dataSourceSbbInterface
				.getDocument(documentSelector);

		WriteResult result = putDocument(documentSelector, oldDocument,
				newDomDocument, appUsage, false);
		return result.getResponseStatus() == 201;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AppUsageRequestProcessor#putElement
	 * (org.openxdm.xcap.common.uri.DocumentSelector,
	 * org.openxdm.xcap.common.uri.NodeSelector,
	 * org.openxdm.xcap.common.uri.ElementSelector,
	 * org.openxdm.xcap.common.xml.NamespaceContext, org.w3c.dom.Element,
	 * org.mobicents.xdm.server.appusage.AppUsage)
	 */
	@Override
	public boolean putElement(final DocumentSelector documentSelector,
			final NodeSelector nodeSelector,
			final ElementSelector elementSelector, Element newElement,
			final AppUsage appUsage) throws InternalServerErrorException,
			NoParentConflictException, SchemaValidationErrorConflictException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException, CannotInsertConflictException,
			NotValidXMLFragmentConflictException, NotUTF8ConflictException,
			BadRequestException, NotAuthorizedRequestException {

		// get doc
		org.openxdm.xcap.common.datasource.Document oldDocument = dataSourceSbbInterface
				.getDocument(documentSelector);
		if (oldDocument == null) {
			// doc does not exists, throw exception
			throw new NoParentConflictException(CONFIGURATION.getXcapRoot()
					+ "/" + documentSelector.getCollection());
		}

		// clone doc
		final Document newDocumentDOM = DocumentCloner.clone(oldDocument
				.getAsDOMDocument());

		// get old element
		final Element oldElement = getElementForPut(documentSelector,
				newDocumentDOM, nodeSelector, false);
		// put element
		WriteResult result = putElement(oldDocument, newDocumentDOM,
				oldElement, documentSelector, nodeSelector, elementSelector,
				newElement, appUsage, false);
		return result.getResponseStatus() == 201;
	}

	@Override
	public boolean putAttribute(DocumentSelector documentSelector,
			NodeSelector nodeSelector, ElementSelector elementSelector,
			AttributeSelector attributeSelector, String attrValue,
			AppUsage appUsage) throws InternalServerErrorException,
			NoParentConflictException, SchemaValidationErrorConflictException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException,
			NotXMLAttributeValueConflictException, BadRequestException,
			CannotInsertConflictException, NotAuthorizedRequestException {

		// get doc
		org.openxdm.xcap.common.datasource.Document oldDocument = dataSourceSbbInterface
				.getDocument(documentSelector);
		if (oldDocument == null) {
			// doc does not exists, throw exception
			throw new NoParentConflictException(CONFIGURATION.getXcapRoot()
					+ "/" + documentSelector.getCollection());
		}

		// clone doc
		final Document newDocumentDOM = DocumentCloner.clone(oldDocument
				.getAsDOMDocument());

		// get old element
		final Element newElement = getElementForPut(documentSelector,
				newDocumentDOM, nodeSelector, false);
		// put element
		WriteResult result = putAttribute(oldDocument, newDocumentDOM,
				newElement, documentSelector, nodeSelector, elementSelector,
				attributeSelector, attrValue, appUsage, false);
		return result.getResponseStatus() == 201;

	}

	// AUX

	private String getElementExistentAncestor(String xcapRoot,
			String documentSelector, String elementSelectorWithEmptyPrefix,
			Document document, XPath xpath) {

		// first part is the xcap uri that points to the document
		StringBuilder sb = new StringBuilder(xcapRoot).append(documentSelector);

		// loop till we find an existing ancestor
		String elementAncestor = null;
		int index = -1;
		while ((index = elementSelectorWithEmptyPrefix.lastIndexOf('/')) > 0) {
			elementSelectorWithEmptyPrefix = elementSelectorWithEmptyPrefix
					.substring(0, index);
			try {
				Element element = (Element) xpath.evaluate(
						elementSelectorWithEmptyPrefix, document,
						XPathConstants.NODE);
				if (element != null) {
					elementAncestor = elementSelectorWithEmptyPrefix;
					break;
				}
			} catch (XPathExpressionException e) {
				// silently ignore an invalid xpath expression, specs requires
				// it
			}
		}

		if (elementAncestor != null) {
			// existing element ancestor found
			// remove empty prefixes if those exist
			elementAncestor = elementAncestor.replaceAll("/:", "/");
			// and add it to the ancestor
			sb.append("/~~").append(elementAncestor);
		}

		String ancestor = sb.toString();
		if (logger.isFineEnabled())
			logger.fine("existing ancestor is " + ancestor);
		return ancestor;
	}

	private Element getElement(Document domDocument,
			String elementSelectorWithEmptyPrefixes,
			NamespaceContext namespaceContext) throws IllegalArgumentException {

		if (logger.isFineEnabled())
			logger.fine("retrieving element "
					+ elementSelectorWithEmptyPrefixes);

		// lets use xpath
		final XPath xpath = DomUtils.XPATH_FACTORY.newXPath();
		// set context to resolve namespace bindings
		xpath.setNamespaceContext(namespaceContext);
		try {
			// exec query to get element
			final NodeList elementNodeList = (NodeList) xpath.evaluate(
					elementSelectorWithEmptyPrefixes, domDocument,
					XPathConstants.NODESET);
			if (elementNodeList.getLength() == 1) {
				if (logger.isFineEnabled())
					logger.fine("element " + elementSelectorWithEmptyPrefixes
							+ " found");
				return (Element) elementNodeList.item(0);
			} else if (elementNodeList.getLength() == 0) {
				if (logger.isFineEnabled()) {
					logger.fine("element " + elementSelectorWithEmptyPrefixes
							+ " not found");
				}
				return null;
			} else {
				if (logger.isFineEnabled()) {
					logger.fine("multiple elements match "
							+ elementSelectorWithEmptyPrefixes);
				}
				throw new IllegalArgumentException("multiple elements match "
						+ elementSelectorWithEmptyPrefixes);
			}
		} catch (XPathExpressionException e) {
			// error in xpath expression
			if (logger.isFineEnabled())
				logger.fine("unable to retrieve element "
						+ elementSelectorWithEmptyPrefixes
						+ " error in xpath expression", e);
			throw new IllegalArgumentException("unable to retrieve element "
					+ elementSelectorWithEmptyPrefixes
					+ " error in xpath expression", e);
		}
	}

	private Element getElementForDeleteOrGet(Document document,
			NodeSelector nodeSelector, boolean parent)
			throws BadRequestException, NotFoundException {
		String elementSelectorWithEmptyPrefix = parent ? nodeSelector
				.getElementParentSelectorWithEmptyPrefix() : nodeSelector
				.getElementSelectorWithEmptyPrefix();
		// get element
		Element element = null;
		try {
			element = getElement(document, elementSelectorWithEmptyPrefix,
					nodeSelector.getNamespaceContext());
		} catch (IllegalArgumentException e) {
			if (nodeSelector.elementSelectorHasUnbindedPrefixes()) {
				// element selector has unbinded prefixe(s)
				if (logger.isFineEnabled())
					logger.fine("element selector doesn't have prefixe(s) bound, bad request");
				throw new BadRequestException();
			} else {
				// nothing wrong with prefixes, return not found
				// exception
				if (logger.isFineEnabled())
					logger.fine("element not found");
				throw new NotFoundException();
			}
		}
		if (element == null) {
			if (logger.isFineEnabled())
				logger.fine("element not found");
			throw new NotFoundException();
		}
		return element;
	}

	private Element getElementForPut(DocumentSelector documentSelector,
			Document document, NodeSelector nodeSelector, boolean parent)
			throws BadRequestException, NoParentConflictException {
		String elementSelectorWithEmptyPrefix = parent ? nodeSelector
				.getElementParentSelectorWithEmptyPrefix() : nodeSelector
				.getElementSelectorWithEmptyPrefix();
		// get element
		try {
			return getElement(document, elementSelectorWithEmptyPrefix,
					nodeSelector.getNamespaceContext());
		} catch (IllegalArgumentException e) {
			if (nodeSelector.elementSelectorHasUnbindedPrefixes()) {
				if (logger.isFineEnabled())
					logger.fine("element selector doesn't have prefixe(s) bound, bad request");
				throw new BadRequestException();
			} else {
				XPath xpath = DomUtils.XPATH_FACTORY.newXPath();
				xpath.setNamespaceContext(nodeSelector.getNamespaceContext());
				throw new NoParentConflictException(getElementExistentAncestor(
						CONFIGURATION.getXcapRoot(),
						documentSelector.toString(),
						elementSelectorWithEmptyPrefix, document, xpath));
			}
		}
	}

}
