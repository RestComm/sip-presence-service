package org.openxdm.xcap.server.slee.appusage.rlsservices;

import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.mobicents.xdm.server.appusage.AppUsageRequestProcessor;
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
import org.openxdm.xcap.common.uri.AttributeSelector;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.ElementSelector;
import org.openxdm.xcap.common.uri.ElementSelectorStep;
import org.openxdm.xcap.common.uri.ElementSelectorStepByAttr;
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
	
	private static final Logger logger = Logger.getLogger(RLSServicesAppUsage.class);
	
	public RLSServicesAppUsage(Validator schemaValidator) {
		super(ID,DEFAULT_DOC_NAMESPACE,MIMETYPE,schemaValidator,new RLSServicesAuthorizationPolicy());
	}

	private final static DocumentSelector GLOBAL_DOCUMENT_SELECTOR = new DocumentSelector(ID,"global","index");
	private static final ElementSelectorStep RLS_SERVICES_ELEMENT_SELECTOR_STEP = new ElementSelectorStep("rls-services");
	private static final String SERVICE_ELEMENT_NAME = "service";
	private static final String URI_ATTRIBUTE_NAME = "uri";
	
	private Map<String,Element> getServices(Document document) {
		Map<String,Element> serviceURIs = new HashMap<String,Element>();
		NodeList documentChildNodes = document.getDocumentElement().getChildNodes();
		for(int i=0;i<documentChildNodes.getLength();i++) {
			Node documentChildNode = documentChildNodes.item(i);			
			if (documentChildNode.getNodeType() == Node.ELEMENT_NODE && documentChildNode.getLocalName().equals(SERVICE_ELEMENT_NAME)) {
				Element element = (Element) documentChildNode; 
				serviceURIs.put(element.getAttributeNode(URI_ATTRIBUTE_NAME).getNodeValue(),element);
			}					
		}
		return serviceURIs;
	}
	
	@Override
	public void processResourceInterdependenciesOnPutAttribute(
			String oldAttrValue, String newAttrValue,
			DocumentSelector documentSelector, String newEtag, NodeSelector nodeSelector,
			ElementSelector elementSelector,
			AttributeSelector attributeSelector,
			NamespaceContext namespaceContext,
			AppUsageRequestProcessor requestProcessor,
			AppUsageDataSource dataSource)
			throws SchemaValidationErrorConflictException,
			UniquenessFailureConflictException, InternalServerErrorException,
			ConstraintFailureConflictException {
	
		if (logger.isDebugEnabled()) {
			logger.debug("processResourceInterdependenciesOnPutAttribute( oldAttrValue = "+oldAttrValue+", newAttrValue = "+newAttrValue+", documentSelector = "+documentSelector+", elementSelector = "+elementSelector+", attributeSelector = "+attributeSelector+" )");
		}
		
		if (documentSelector.isUserDocument()) {
			try {
				requestProcessor.putAttribute(documentSelector, nodeSelector, elementSelector, attributeSelector, namespaceContext,newAttrValue,this);
			} catch (NoParentConflictException e) {
				throw new InternalServerErrorException("Update of service in rls global doc thrown exception",e);
			} catch (NotXMLAttributeValueConflictException e) {
				throw new InternalServerErrorException("Update of service in rls global doc thrown exception",e);
			} catch (CannotInsertConflictException e) {
				throw new InternalServerErrorException("Update of service in rls global doc thrown exception",e);
			} catch (BadRequestException e) {
				throw new InternalServerErrorException("Update of service in rls global doc thrown exception",e);
			}
		}
	}
	
	private boolean putElement(NodeSelector nodeSelector, ElementSelector elementSelector, NamespaceContext namespaceContext, Element newElement, AppUsageRequestProcessor requestProcessor) throws InternalServerErrorException {
		try {
			return requestProcessor.putElement(GLOBAL_DOCUMENT_SELECTOR, nodeSelector, elementSelector, namespaceContext, newElement,this);
		} catch (NoParentConflictException e) {
			throw new InternalServerErrorException("Put of service in rls global doc thrown exception",e);
		} catch (NotUTF8ConflictException e) {
			throw new InternalServerErrorException("Put of service in rls global doc thrown exception",e);
		} catch (CannotInsertConflictException e) {
			throw new InternalServerErrorException("Put of service in rls global doc thrown exception",e);
		} catch (BadRequestException e) {
			throw new InternalServerErrorException("Put of service in rls global doc thrown exception",e);
		} catch (SchemaValidationErrorConflictException e) {
			throw new InternalServerErrorException("Put of service in rls global doc thrown exception",e);
		} catch (ConstraintFailureConflictException e) {
			throw new InternalServerErrorException("Put of service in rls global doc thrown exception",e);
		} catch (NotValidXMLFragmentConflictException e) {
			throw new InternalServerErrorException("Put of service in rls global doc thrown exception",e);
		} catch (UniquenessFailureConflictException e) {
			throw new InternalServerErrorException("Put of service in rls global doc thrown exception",e);
		}
	}
	
	@Override
	public void processResourceInterdependenciesOnPutElement(
			Element oldElement, Element newElement, Document document,
			DocumentSelector documentSelector, String newEtag, NodeSelector nodeSelector,
			ElementSelector elementSelector, NamespaceContext namespaceContext,
			AppUsageRequestProcessor requestProcessor,
			AppUsageDataSource dataSource)
			throws SchemaValidationErrorConflictException,
			UniquenessFailureConflictException, InternalServerErrorException,
			ConstraintFailureConflictException {

		if (logger.isDebugEnabled()) {
			logger.debug("processResourceInterdependenciesOnPutElement( oldElement = "+oldElement+", newElement = "+newElement+", documentSelector = "+documentSelector+", elementSelector = "+elementSelector+" )");
		}
		
		if (documentSelector.isUserDocument()) {
			if (elementSelector.getStepsSize()>2) {
				// update of service
				if (logger.isDebugEnabled()) {
					logger.debug("Updating "+elementSelector+" in rls services global doc");
				}
				putElement(nodeSelector, elementSelector, namespaceContext, newElement, requestProcessor);
				if (logger.isInfoEnabled()) {
					logger.info("Updated "+elementSelector+" in rls services global doc");
				}
			}
			else if (elementSelector.getStepsSize() == 2) {
				// put of service
				if (oldElement != null) {
					// update of service
					if (logger.isDebugEnabled()) {
						logger.debug("Updating "+elementSelector+" in rls services global doc");
					}
					putElement(nodeSelector, elementSelector, namespaceContext, newElement, requestProcessor);
					if (logger.isInfoEnabled()) {
						logger.info("Updated "+elementSelector+" in rls services global doc");
					}
				}
				else {
					// new service
					if (logger.isDebugEnabled()) {
						logger.debug("Adding "+elementSelector+" to rls services global doc");
					}
					if(!putElement(nodeSelector, elementSelector, namespaceContext, newElement, requestProcessor)) {
						throw new UniquenessFailureConflictException();
					}
					if (logger.isInfoEnabled()) {
						logger.info("Added "+elementSelector+" to rls services global doc");
					}
				}
			}
			else {
				// put of all services, same as put of doc
				processResourceInterdependenciesOnPutDocument(dataSource.getDocument(documentSelector).getAsDOMDocument(), document, documentSelector, newEtag, requestProcessor,dataSource);
			}
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
			logger.debug("processResourceInterdependenciesOnPutDocument( oldDoc = "+oldDocument+", newDoc = "+newDocument+", documentSelector = "+documentSelector+" )");
		}
				
		if (documentSelector.isUserDocument()) {
			if (oldDocument == null) {
				
				// add all services in new doc
				
				for(Entry<String, Element> entry : getServices(newDocument).entrySet()) {
					LinkedList<ElementSelectorStep> elementSelectorSteps = new LinkedList<ElementSelectorStep>();
					elementSelectorSteps.add(RLS_SERVICES_ELEMENT_SELECTOR_STEP);
					elementSelectorSteps.add(new ElementSelectorStepByAttr(SERVICE_ELEMENT_NAME,URI_ATTRIBUTE_NAME,entry.getKey()));
					ElementSelector elementSelector = new ElementSelector(elementSelectorSteps);
					try {
						if (logger.isDebugEnabled()) {
							logger.debug("Adding "+entry.getKey()+" to rls services global doc");
						}
						if(!putElement(new NodeSelector(elementSelector.toString()), elementSelector, EMPTY_NAMESPACE_CONTEXT, entry.getValue(), requestProcessor)) {
							throw new UniquenessFailureConflictException();
						}
						if (logger.isInfoEnabled()) {
							logger.info("Added "+entry.getKey()+" to rls services global doc");
						}
					}
					catch (InternalServerErrorException e) {
						// global doc does not exists, put user doc as global
						if (logger.isDebugEnabled()) {
							logger.debug("Rls services global doc does not exists yet");
						}
						try {
							if (!requestProcessor.putDocument(GLOBAL_DOCUMENT_SELECTOR, newDocument,this)) {
								throw new UniquenessFailureConflictException();
							}
							if (logger.isDebugEnabled()) {
								logger.debug("Rls services global doc created");
							}
							return;
						}
						catch (NoParentConflictException f) {
							throw new InternalServerErrorException("Creation of rls global doc thrown exception",f);
						}
						catch (UniquenessFailureConflictException f) {
							// concurrent put? restart process
							if (logger.isDebugEnabled()) {
								logger.debug("Failed to create Rls services global doc, concurrent creation? Restarting update due to user doc put.");
							}
							processResourceInterdependenciesOnPutDocument(oldDocument, newDocument, documentSelector, newEtag, requestProcessor,dataSource);							
						} catch (ConflictException f) {
							throw new InternalServerErrorException("Creation of rls global doc thrown exception",f);
						} catch (MethodNotAllowedException f) {
							throw new InternalServerErrorException("Creation of rls global doc thrown exception",f);
						} catch (UnsupportedMediaTypeException f) {
							throw new InternalServerErrorException("Creation of rls global doc thrown exception",f);
						} catch (PreconditionFailedException f) {
							throw new InternalServerErrorException("Creation of rls global doc thrown exception",f);
						} catch (BadRequestException f) {
							throw new InternalServerErrorException("Creation of rls global doc thrown exception",f);
						} catch (NotAuthorizedRequestException f) {
							throw new InternalServerErrorException("Creation of rls global doc thrown exception",f);
						}
					}
				}
			}
			else {
				// doc update
				// gather services to add, update and remove in global doc
				Map<String,Element> newServiceURIs = getServices(newDocument);
				Map<String,Element> toAddServiceURIs = getServices(newDocument);
				Map<String,Element> toUpdateServiceURIs = getServices(newDocument);
				Map<String,Element> toDeleteServiceURIs = getServices(oldDocument);
				// setup services to add
				for(String serviceURI : toDeleteServiceURIs.keySet()) {
					toAddServiceURIs.remove(serviceURI);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Services to add to global rls service doc: "+toAddServiceURIs);
				}
				// setup services to update
				for(String serviceURI : toAddServiceURIs.keySet()) {
					toUpdateServiceURIs.remove(serviceURI);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Services to update in global rls service doc: "+toUpdateServiceURIs);
				}
				// setup services to delete
				for(String serviceURI : newServiceURIs.keySet()) {
					toDeleteServiceURIs.remove(serviceURI);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Services to delete from global rls service doc: "+toDeleteServiceURIs);
				}
				
				// add new ones
				for(Entry<String, Element> entry : toAddServiceURIs.entrySet()) {
					LinkedList<ElementSelectorStep> elementSelectorSteps = new LinkedList<ElementSelectorStep>();
					elementSelectorSteps.add(RLS_SERVICES_ELEMENT_SELECTOR_STEP);
					elementSelectorSteps.add(new ElementSelectorStepByAttr(SERVICE_ELEMENT_NAME,URI_ATTRIBUTE_NAME,entry.getKey()));
					ElementSelector elementSelector = new ElementSelector(elementSelectorSteps);
					if (logger.isDebugEnabled()) {
						logger.debug("Adding "+entry.getKey()+" to rls services global doc");
					}
					if(!putElement(new NodeSelector(elementSelector.toString()), elementSelector, EMPTY_NAMESPACE_CONTEXT, entry.getValue(), requestProcessor)) {
						throw new UniquenessFailureConflictException();
					}
					if (logger.isInfoEnabled()) {
						logger.info("Added "+entry.getKey()+" to rls services global doc");
					}
				}
				// now the updates
				for(Entry<String, Element> entry : toUpdateServiceURIs.entrySet()) {
					LinkedList<ElementSelectorStep> elementSelectorSteps = new LinkedList<ElementSelectorStep>();
					elementSelectorSteps.add(RLS_SERVICES_ELEMENT_SELECTOR_STEP);
					elementSelectorSteps.add(new ElementSelectorStepByAttr(SERVICE_ELEMENT_NAME,URI_ATTRIBUTE_NAME,entry.getKey()));
					ElementSelector elementSelector = new ElementSelector(elementSelectorSteps);
					if (logger.isDebugEnabled()) {
						logger.debug("Updating "+entry.getKey()+" in rls services global doc");
					}
					putElement(new NodeSelector(elementSelector.toString()), elementSelector, EMPTY_NAMESPACE_CONTEXT, entry.getValue(), requestProcessor);						
					if (logger.isInfoEnabled()) {
						logger.info("Updated "+entry.getKey()+" in rls services global doc");
					}
				}

				// finally the deletes
				for(Entry<String, Element> entry : toDeleteServiceURIs.entrySet()) {
					LinkedList<ElementSelectorStep> elementSelectorSteps = new LinkedList<ElementSelectorStep>();
					elementSelectorSteps.add(RLS_SERVICES_ELEMENT_SELECTOR_STEP);
					elementSelectorSteps.add(new ElementSelectorStepByAttr(SERVICE_ELEMENT_NAME,URI_ATTRIBUTE_NAME,entry.getKey()));
					ElementSelector elementSelector = new ElementSelector(elementSelectorSteps);
					if (logger.isDebugEnabled()) {
						logger.debug("Deleting "+entry.getKey()+" from rls services global doc");
					}
					deleteElement(new NodeSelector(elementSelector.toString()),elementSelector,EMPTY_NAMESPACE_CONTEXT,requestProcessor);						
					if (logger.isInfoEnabled()) {
						logger.info("Deleted "+entry.getKey()+" from rls services global doc");
					}
				}
			}
		}	
	}
		
	private void deleteElement(NodeSelector nodeSelector, ElementSelector elementSelector, NamespaceContext namespaceContext, AppUsageRequestProcessor requestProcessor) throws InternalServerErrorException {
		try {
			requestProcessor.deleteElement(GLOBAL_DOCUMENT_SELECTOR, nodeSelector,elementSelector,namespaceContext,this);
		} catch (CannotDeleteConflictException e) {
			throw new InternalServerErrorException(e.getMessage(),e);
		} catch (NotFoundException e) {
			throw new InternalServerErrorException(e.getMessage(),e);
		} catch (BadRequestException e) {
			throw new InternalServerErrorException(e.getMessage(),e);
		} catch (UniquenessFailureConflictException e) {
			throw new InternalServerErrorException(e.getMessage(),e);
		} catch (SchemaValidationErrorConflictException e) {
			throw new InternalServerErrorException(e.getMessage(),e);
		} catch (ConstraintFailureConflictException e) {
			throw new InternalServerErrorException(e.getMessage(),e);
		}
	}

	private static final NamespaceContext EMPTY_NAMESPACE_CONTEXT = initEmptyNamespaceContext();

	private static NamespaceContext initEmptyNamespaceContext() {
		final Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put(XMLConstants.DEFAULT_NS_PREFIX, RLSServicesAppUsage.DEFAULT_DOC_NAMESPACE);
		return new NamespaceContext(namespaces);
	}

	@Override
	public void processResourceInterdependenciesOnDeleteDocument(
			Document deletedDocument, DocumentSelector documentSelector,
			AppUsageRequestProcessor requestProcessor,
			AppUsageDataSource dataSource)
			throws SchemaValidationErrorConflictException,
			UniquenessFailureConflictException, InternalServerErrorException,
			ConstraintFailureConflictException {
		
		if (logger.isDebugEnabled()) {
			logger.debug("processResourceInterdependenciesOnDeleteDocument( documentSelector = "+documentSelector+" )");
		}
		
		if (documentSelector.isUserDocument()) {
			// delete all services from global doc
			NodeList documentChildNodes = deletedDocument.getDocumentElement().getChildNodes();
			for(int i=0;i<documentChildNodes.getLength();i++) {
				Node documentChildNode = documentChildNodes.item(i);			
				if (documentChildNode.getNodeType() == Node.ELEMENT_NODE && documentChildNode.getLocalName().equals(SERVICE_ELEMENT_NAME)) {
					String serviceURI = ((Element) documentChildNode).getAttributeNode(URI_ATTRIBUTE_NAME).getNodeValue();
					LinkedList<ElementSelectorStep> elementSelectorSteps = new LinkedList<ElementSelectorStep>();
					elementSelectorSteps.add(RLS_SERVICES_ELEMENT_SELECTOR_STEP);
					elementSelectorSteps.add(new ElementSelectorStepByAttr(SERVICE_ELEMENT_NAME,URI_ATTRIBUTE_NAME,serviceURI));
					ElementSelector elementSelector = new ElementSelector(elementSelectorSteps);
					if (logger.isDebugEnabled()) {
						logger.debug("Deleting "+serviceURI+" from rls services global doc");
					}
					deleteElement(new NodeSelector(elementSelector.toString()),elementSelector,EMPTY_NAMESPACE_CONTEXT,requestProcessor);
					if (logger.isInfoEnabled()) {
						logger.info("Deleted "+serviceURI+" from rls services global doc");
					}																												
				}
			}
		}
	}
	
	@Override
	public void processResourceInterdependenciesOnDeleteElement(
			Node deletedElement, DocumentSelector documentSelector, String newEtag, 
			NodeSelector nodeSelector, ElementSelector elementSelector,
			NamespaceContext namespaceContext,
			AppUsageRequestProcessor requestProcessor,
			AppUsageDataSource dataSource)
			throws SchemaValidationErrorConflictException,
			UniquenessFailureConflictException, InternalServerErrorException,
			ConstraintFailureConflictException {

		if (logger.isDebugEnabled()) {
			logger.debug("processResourceInterdependenciesOnDeleteElement( documentSelector = "+documentSelector+", elementSelector = "+elementSelector+" )");
		}

		if (documentSelector.isUserDocument()) {
			// delete element from global doc
			deleteElement(nodeSelector,elementSelector,namespaceContext,requestProcessor);																							
		}
	}
	
	@Override
	public void processResourceInterdependenciesOnDeleteAttribute(
			DocumentSelector documentSelector, String newEtag, NodeSelector nodeSelector,
			ElementSelector elementSelector,
			AttributeSelector attributeSelector,
			NamespaceContext namespaceContext,
			AppUsageRequestProcessor requestProcessor,
			AppUsageDataSource dataSource)
			throws SchemaValidationErrorConflictException,
			UniquenessFailureConflictException, InternalServerErrorException,
			ConstraintFailureConflictException {
		
		if (logger.isDebugEnabled()) {
			logger.debug("processResourceInterdependenciesOnDeleteAttribute( documentSelector = "+documentSelector+", elementSelector = "+elementSelector+", attributeSelector = "+attributeSelector+" )");
		}
		
		if (documentSelector.isUserDocument()) {
			// delete attribute from global doc
			try {
				requestProcessor.deleteAttribute(GLOBAL_DOCUMENT_SELECTOR, nodeSelector, elementSelector, attributeSelector, namespaceContext,this);
			} catch (NotFoundException e) {
				throw new InternalServerErrorException(e.getMessage(),e);
			} catch (BadRequestException e) {
				throw new InternalServerErrorException(e.getMessage(),e);
			}																						
		}
	}
	
	@Override
	public void checkConstraintsOnPut(Document document, String xcapRoot,
			DocumentSelector documentSelector, AppUsageDataSource dataSource)
			throws UniquenessFailureConflictException,
			InternalServerErrorException, ConstraintFailureConflictException {
	
		if (!documentSelector.isUserDocument()) {
			return;
		}
		
		super.checkConstraintsOnPut(document, xcapRoot, documentSelector, dataSource);
		
		/*
		 	NOTE: the contraint below is ensured when (re)building the global doc
		 	
		    "The URI in the "uri" attribute of the <service> element MUST be
      		unique amongst all other URIs in "uri" elements in any <service>
      		element in any document on a particular server.  This uniqueness
      		constraint spans across XCAP roots."
      	*/	
		
        /*  
            TODO ensure the uri is not a network resource, such as the uri of a sip user
            
            "Furthermore, the URI MUST NOT correspond to an existing resource
            within the domain of the URI.
      		If a server is asked to set the URI to something that already
      		exists, the server MUST reject the request with a 409, and use the
      		mechanisms defined in [10] to suggest alternate URIs that have not
      		yet been allocated."		
		 */
					
		// get document's element childs
		NodeList childNodes = document.getDocumentElement().getChildNodes();
		// process each one
		for(int i=0;i<childNodes.getLength();i++) {
			Node childNode = childNodes.item(i);			
			if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getLocalName().equals("service")) {
				// service element
				// get childs
				NodeList serviceChildNodes = childNode.getChildNodes();
				// process each one
				for(int j=0;j<serviceChildNodes.getLength();j++) {
					Node serviceChildNode = serviceChildNodes.item(j);
					if (serviceChildNode.getNodeType() == Node.ELEMENT_NODE && serviceChildNode.getLocalName().equals("list")) {
						// list element
						/*
							 o  In addition, an RLS services document can contain a <list>
							 element, which in turn can contain <entry>, <entry-ref> and
							 <external> elements.  The constraints defined for these elements
							 in Section 3.4.7 MUST be enforced.				 
						 */		
						ResourceListsAppUsage.checkNodeResourceListConstraints(serviceChildNode,false);
					}			 
					else if (serviceChildNode.getNodeType() == Node.ELEMENT_NODE && serviceChildNode.getLocalName().equals("resource-list")) {
						// resource-list element

						// flag setup
						boolean throwException = true;
						// node value is the uri to evaluate
						String resourceListUri = serviceChildNode.getTextContent().trim();

						try {																	
							// build uri
							URI uri = new URI(resourceListUri);
							String uriScheme = uri.getScheme();
							/*
								 The URI in a <resource-list> element MUST be an absolute URI.
							 */
							if(uriScheme != null && (uriScheme.equalsIgnoreCase("http") || uriScheme.equalsIgnoreCase("https"))) {
								// split string after "scheme://" to find path segments
								String[] resourceListUriPaths = resourceListUri.substring(uriScheme.length()+3).split("/");									
								for(int k=0;k<resourceListUriPaths.length;k++) {
									/*
										  The server MUST verify that the URI
										  path contains "resource-lists" in the
										  path segment corresponding to the
										  AUID.
									 */
									if (resourceListUriPaths[k].equals(ResourceListsAppUsage.ID)) {
										// found auid
										if (!resourceListUriPaths[k+1].equals("global")) {
											// not global
											/*
  													If the RLS services
					      							document is within the XCAP user tree (as opposed to the global
					      							tree), the server MUST verify that the XUI in the path is the same
					      							as the XUI in the URI of to the resource-list document.
											 */
											// decode the candidate xui first
											String resourceListXUIDecoded = URLDecoder.decode(resourceListUriPaths[k+2],"UTf-8");
											String requestXUI = documentSelector.getDocumentParent().split("/")[1];
											if (resourceListXUIDecoded.equals(requestXUI)) {
												throwException = false;												
											}
											else {
												logger.error("not the same xcap user id in request ("+requestXUI+") and resource list ("+resourceListXUIDecoded+") URIs");
											}
											break;
										}
										else {
											throwException = false;
											break;
										}
									}
								}									
							}								
						}
						catch (Exception e) {
							// ignore
							logger.error(e.getMessage(),e);
						}							
						// throw exception if needed
						if (throwException) {
							throw new ConstraintFailureConflictException("Bad URI in resource-list element >> "+resourceListUri);
						}
					}												
				}
			}
		}						
	}
}
	
