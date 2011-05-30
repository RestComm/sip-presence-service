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

import java.io.IOException;

import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;

import org.openxdm.xcap.common.error.ConstraintFailureConflictException;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.error.NotAuthorizedRequestException;
import org.openxdm.xcap.common.error.SchemaValidationErrorConflictException;
import org.openxdm.xcap.common.error.UniquenessFailureConflictException;
import org.openxdm.xcap.common.uri.AttributeSelector;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.NodeSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Each XCAP resource on a server is associated with an application. In order
 * for an application to use those resources, application specific conventions
 * must be specified. Those conventions include the XML schema that defines the
 * structure and constraints of the data, well known URIs to bootstrap access to
 * the data, and so on. All of those application specific conventions are
 * defined by the application usage.
 * 
 * @author Eduardo Martins
 * 
 */
public abstract class AppUsage {

	/**
	 * Each application usage is associated with a name, called an Application
	 * Unique ID (AUID). This name uniquely identifies the application usage
	 * within the namespace of application usages, and is different from AUIDs
	 * used by other applications.
	 */
	private String auid = null;

	/**
	 * All application usages MUST define a namespace URI that represents the
	 * default document namespace to be used when evaluating URIs. The default
	 * document namespace does not apply to elements or attributes within the
	 * documents themselves - it applies only to the evaluation of URIs within
	 * that application usage. Indeed, the term 'default document namespace' is
	 * distinct from the term 'default namespace'. The latter has the standard
	 * meaning within XML documents, and the former refers to the default used
	 * in evaluation of XCAP URIs. XCAP does not change in any way the
	 * mechanisms for determining the default namespace within XML documents.
	 * However, if a document contains a URI representing an XCAP resource, the
	 * default document namespace defined by the application usage applies to
	 * that URI as well.
	 */
	private String defaultDocumentNamespace = null;

	/**
	 * The application usage MUST also identify the MIME type for documents
	 * compliant to that schema.
	 */
	private String mimetype = null;

	private Validator uniquenessSchemaValidator = null;

	/**
	 * All application usages MUST describe their document contents using XML
	 * schema. Here we have an appropriate validator.
	 */
	private Validator schemaValidator = null;

	/**
	 * By default, each user is able to access (read, modify, and delete) all of
	 * the documents below their home directory, and any user is able to read
	 * documents within the global directory. However, only trusted users,
	 * explicitly provisioned into the server, can modify global documents. The
	 * application usage can specify a different authorization policy that
	 * applies to all documents associated with that application usage.
	 */
	private AuthorizationPolicy authorizationPolicy;

	public AppUsage(String auid, String defaultDocumentNamespace,
			String mimetype, Validator schemaValidator,
			String authorizedUserDocumentName) {
		this.auid = auid;
		this.defaultDocumentNamespace = defaultDocumentNamespace;
		this.mimetype = mimetype;
		this.schemaValidator = schemaValidator;
		authorizationPolicy = new DefaultAuthorizationPolicy(
				authorizedUserDocumentName);
	}

	public AppUsage(String auid, String defaultDocumentNamespace,
			String mimetype, Validator schemaValidator,
			AuthorizationPolicy authorizationPolicy) {
		this.auid = auid;
		this.defaultDocumentNamespace = defaultDocumentNamespace;
		this.mimetype = mimetype;
		this.schemaValidator = schemaValidator;
		this.authorizationPolicy = authorizationPolicy;
	}

	public AppUsage(String auid, String defaultDocumentNamespace,
			String mimetype, Validator schemaValidator,
			Validator uniquenessSchemaValidator,
			String authorizedUserDocumentName) {
		this.auid = auid;
		this.defaultDocumentNamespace = defaultDocumentNamespace;
		this.mimetype = mimetype;
		this.schemaValidator = schemaValidator;
		this.uniquenessSchemaValidator = uniquenessSchemaValidator;
		authorizationPolicy = new DefaultAuthorizationPolicy(
				authorizedUserDocumentName);
	}

	public AppUsage(String auid, String defaultDocumentNamespace,
			String mimetype, Validator schemaValidator,
			Validator uniquenessSchemaValidator,
			AuthorizationPolicy authorizationPolicy) {
		this.auid = auid;
		this.defaultDocumentNamespace = defaultDocumentNamespace;
		this.mimetype = mimetype;
		this.schemaValidator = schemaValidator;
		this.uniquenessSchemaValidator = uniquenessSchemaValidator;
		this.authorizationPolicy = authorizationPolicy;
	}

	public String getDefaultDocumentNamespace() {
		return defaultDocumentNamespace;
	}

	public String getAUID() {
		return auid;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void validateSchema(Document document)
			throws SchemaValidationErrorConflictException,
			InternalServerErrorException {
		// check arg
		if (document == null) {
			throw new IllegalArgumentException("document can't be null");
		}
		// validate
		try {
			if (schemaValidator != null) {
				schemaValidator.validate(new DOMSource(document));
			}
		} catch (SAXException e) {
			throw new SchemaValidationErrorConflictException(e.getMessage(), e);
		} catch (IOException e) {
			throw new InternalServerErrorException(e.getMessage(), e);
		}
	}

	public AuthorizationPolicy getAuthorizationPolicy() {
		return authorizationPolicy;
	}

	/**
	 * The application usage can specify additional constraints that are not
	 * possible through XML Schema, e.g. a collection of documents need to have
	 * the root element with a unique value for attribute "id". In this method
	 * the app usage implements the checks on constraints for PUT operations. It
	 * is recommended that the main XML Schema of the app usage doesn't
	 * implement any of these checks, because in a error use case, the server
	 * will return a schema validation error, thus this API supports the usage
	 * of a additional XML Schema just for uniqueness contraints.
	 * 
	 * Note that this method also throws Not Authorized Exception, this is
	 * required by some XCAP App usages, which are badly design, and "authorize"
	 * bad content, instead of the standard Constraint Failure.
	 * 
	 * @param document
	 * @param xcapRoot
	 * @param documentSelector
	 * @param dataSource
	 * @throws UniquenessFailureConflictException
	 * @throws InternalServerErrorException
	 * @throws ConstraintFailureConflictException
	 * @throws {@link NotAuthorizedRequestException}
	 */
	public void checkConstraintsOnPut(Document document, String xcapRoot,
			DocumentSelector documentSelector, AppUsageDataSource dataSource)
			throws UniquenessFailureConflictException,
			InternalServerErrorException, ConstraintFailureConflictException,
			NotAuthorizedRequestException {

		// validate uniqueness schema if exists
		if (uniquenessSchemaValidator != null) {
			try {
				uniquenessSchemaValidator.validate(new DOMSource(document));
			} catch (SAXException e) {
				throw new UniquenessFailureConflictException();
			} catch (IOException e) {
				throw new InternalServerErrorException(e.getMessage());
			}
		}

		// default is nothing else to check
	}

	/**
	 * The application usage may specify resource interdependencies, like one
	 * global document being a composition of all user documents, this method is
	 * where the application usage defines this dependency logic on PUT
	 * requests.
	 * 
	 * @param oldDocument
	 * @param newDocument
	 * @param documentSelector
	 * @param requestProcessor
	 * @throws SchemaValidationErrorConflictException
	 * @throws UniquenessFailureConflictException
	 * @throws InternalServerErrorException
	 * @throws ConstraintFailureConflictException
	 */
	public void processResourceInterdependenciesOnPutDocument(
			Document oldDocument, Document newDocument,
			DocumentSelector documentSelector, String newETag,
			AppUsageRequestProcessor requestProcessor,
			AppUsageDataSource dataSource)
			throws SchemaValidationErrorConflictException,
			UniquenessFailureConflictException, InternalServerErrorException,
			ConstraintFailureConflictException {
		// default is no resource interdependencies
	}

	/**
	 * The application usage may specify resource interdependencies, like one
	 * global document being a composition of all user documents, this method is
	 * where the application usage defines this dependency logic on PUT requests
	 * for an element.
	 * 
	 * @param oldElement
	 * @param newElement
	 * @param document
	 * @param documentSelector
	 * @param newETag
	 * @param nodeSelector
	 * @param elementSelector
	 * @param requestProcessor
	 * @param dataSource
	 * @throws SchemaValidationErrorConflictException
	 * @throws UniquenessFailureConflictException
	 * @throws InternalServerErrorException
	 * @throws ConstraintFailureConflictException
	 */
	public void processResourceInterdependenciesOnPutElement(
			Element oldElement, Element newElement, Document document,
			DocumentSelector documentSelector, String newETag,
			NodeSelector nodeSelector, ElementSelector elementSelector,
			AppUsageRequestProcessor requestProcessor,
			AppUsageDataSource dataSource)
			throws SchemaValidationErrorConflictException,
			UniquenessFailureConflictException, InternalServerErrorException,
			ConstraintFailureConflictException {
		// default is no resource interdependencies
	}

	/**
	 * The application usage may specify resource interdependencies, like one
	 * global document being a composition of all user documents, this method is
	 * where the application usage defines this dependency logic on PUT requests
	 * for an attribute.
	 * 
	 * @param oldAttrValue
	 * @param newAttrValue
	 * @param documentSelector
	 * @param newETag
	 * @param nodeSelector
	 * @param elementSelector
	 * @param attributeSelector
	 * @param requestProcessor
	 * @param dataSource
	 * @throws SchemaValidationErrorConflictException
	 * @throws UniquenessFailureConflictException
	 * @throws InternalServerErrorException
	 * @throws ConstraintFailureConflictException
	 */
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
		// default is no resource interdependencies
	}

	/**
	 * The application usage can specify additional constraints that are not
	 * possible through XML Schema, e.g. a collection of documents need to have
	 * the root element with a unique value for attribute "id". In this method
	 * the app usage implements the checks on constraints for DELETE operations.
	 * It is recommended that the main XML Schema of the app usage doesn't
	 * implement any of these checks, because in a error use case, the server
	 * will return a schema validation error, thus this API supports the usage
	 * of a additional XML Schema just for uniqueness contraints.
	 * 
	 * @param document
	 * @param xcapRoot
	 * @param documentSelector
	 * @param dataSource
	 * @throws UniquenessFailureConflictException
	 * @throws InternalServerErrorException
	 * @throws ConstraintFailureConflictException
	 */
	public void checkConstraintsOnDelete(Document document, String xcapRoot,
			DocumentSelector documentSelector, AppUsageDataSource dataSource)
			throws UniquenessFailureConflictException,
			InternalServerErrorException, ConstraintFailureConflictException {
		// default is nothing else to check
	}

	/**
	 * The application usage may specify resource interdependencies, like one
	 * global document being a composition of all user documents, this method is
	 * where the application usage defines this dependency logic on DELETE
	 * requests.
	 * 
	 * @param document
	 * @param documentSelector
	 * @param requestProcessor
	 * @throws SchemaValidationErrorConflictException
	 * @throws UniquenessFailureConflictException
	 * @throws InternalServerErrorException
	 * @throws ConstraintFailureConflictException
	 */
	public void processResourceInterdependenciesOnDeleteDocument(
			Document deletedDocument, DocumentSelector documentSelector,
			AppUsageRequestProcessor requestProcessor,
			AppUsageDataSource dataSource)
			throws SchemaValidationErrorConflictException,
			UniquenessFailureConflictException, InternalServerErrorException,
			ConstraintFailureConflictException {
		// default is no resource interdependencies
	}

	/**
	 * The application usage may specify resource interdependencies, like one
	 * global document being a composition of all user documents, this method is
	 * where the application usage defines this dependency logic on DELETE
	 * requests.
	 * 
	 * @param deletedElement
	 * @param documentSelector
	 * @param newETag
	 * @param nodeSelector
	 * @param elementSelector
	 * @param requestProcessor
	 * @param dataSource
	 * @throws SchemaValidationErrorConflictException
	 * @throws UniquenessFailureConflictException
	 * @throws InternalServerErrorException
	 * @throws ConstraintFailureConflictException
	 */
	public void processResourceInterdependenciesOnDeleteElement(
			Node deletedElement, DocumentSelector documentSelector,
			String newETag, NodeSelector nodeSelector,
			ElementSelector elementSelector, 
			AppUsageRequestProcessor requestProcessor,
			AppUsageDataSource dataSource)
			throws SchemaValidationErrorConflictException,
			UniquenessFailureConflictException, InternalServerErrorException,
			ConstraintFailureConflictException {
		// default is no resource interdependencies
	}

	/**
	 * The application usage may specify resource interdependencies, like one
	 * global document being a composition of all user documents, this method is
	 * where the application usage defines this dependency logic on DELETE
	 * requests.
	 * 
	 * @param documentSelector
	 * @param newETag
	 * @param nodeSelector
	 * @param elementSelector
	 * @param attributeSelector
	 * @param requestProcessor
	 * @param dataSource
	 * @throws SchemaValidationErrorConflictException
	 * @throws UniquenessFailureConflictException
	 * @throws InternalServerErrorException
	 * @throws ConstraintFailureConflictException
	 */
	public void processResourceInterdependenciesOnDeleteAttribute(
			DocumentSelector documentSelector, String newETag,
			NodeSelector nodeSelector, ElementSelector elementSelector,
			AttributeSelector attributeSelector,
			AppUsageRequestProcessor requestProcessor,
			AppUsageDataSource dataSource)
			throws SchemaValidationErrorConflictException,
			UniquenessFailureConflictException, InternalServerErrorException,
			ConstraintFailureConflictException {
		// default is no resource interdependencies
	}

}
