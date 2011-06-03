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

package org.mobicents.slee.xdm.server.subscription;

import java.io.StringReader;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sip.ServerTransaction;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.EventHeader;
import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.SbbLocalObject;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.mobicents.protocols.xcap.diff.BuildPatchException;
import org.mobicents.protocols.xcap.diff.dom.DOMXcapDiffFactory;
import org.mobicents.protocols.xcap.diff.dom.DOMXcapDiffPatchBuilder;
import org.mobicents.slee.sipevent.server.subscription.NotifyContent;
import org.mobicents.slee.sipevent.server.subscription.data.Notifier;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.mobicents.slee.xdm.server.ServerConfiguration;
import org.mobicents.xdm.common.util.dom.DomUtils;
import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageManagement;
import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.NodeSelector;
import org.openxdm.xcap.common.uri.Parser;
import org.openxdm.xcap.common.uri.ResourceSelector;
import org.openxdm.xcap.common.xml.TextWriter;
import org.openxdm.xcap.server.slee.resource.datasource.CollectionActivity;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceActivityContextInterfaceFactory;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceSbbInterface;
import org.openxdm.xcap.server.slee.resource.datasource.DocumentActivity;
import org.openxdm.xcap.server.slee.resource.datasource.DocumentUpdatedEvent;
import org.openxdm.xcap.server.slee.resource.datasource.NodeSubscription;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Logic for {@link XcapDiffSubscriptionControlSbb}
 * 
 * @author martins
 * 
 */
public class XcapDiffSubscriptionControl {

	private static final String[] xcapDiffEventPackages = { "xcap-diff" };

	private static final AppUsageManagement APP_USAGE_MANAGEMENT = AppUsageManagement
			.getInstance();

	private static final ServerConfiguration XDM_SERVER_CONFIGURATION = ServerConfiguration
			.getInstance();

	private final Map<String, String> EVENT_HEADER_PATCHING_PARAMS = initEventHeaderPatchingParams();

	private Map<String, String> initEventHeaderPatchingParams() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(DiffProcessing.PARAM, DiffProcessing.XcapPatching.toString());
		return Collections.unmodifiableMap(map);
	}

	public static String[] getEventPackages() {
		return xcapDiffEventPackages;
	}

	private ContentTypeHeader xcapDiffContentTypeHeader = null;

	public ContentTypeHeader getXcapDiffContentTypeHeader(
			XcapDiffSubscriptionControlSbbInterface sbb) {
		if (xcapDiffContentTypeHeader == null) {
			try {
				xcapDiffContentTypeHeader = sbb
						.getHeaderFactory()
						.createContentTypeHeader("application", "xcap-diff+xml");
			} catch (ParseException e) {
				// ignore
				e.printStackTrace();
			}
		}
		return xcapDiffContentTypeHeader;
	}

	public void isSubscriberAuthorized(String subscriber,
			String subscriberDisplayName, Notifier notifier,
			SubscriptionKey key, int expires, String content,
			String contentType, String contentSubtype, boolean eventList,
			ServerTransaction serverTransaction,
			XcapDiffSubscriptionControlSbbInterface sbb) {

		DiffProcessing diffProcessing = DiffProcessing.NoPatching;
		if (serverTransaction != null) {
			// sip subscription
			EventHeader eventHeader = (EventHeader) serverTransaction
					.getRequest().getHeader(EventHeader.NAME);
			if (eventHeader != null) {
				diffProcessing = DiffProcessing.fromString(eventHeader
						.getParameter(DiffProcessing.PARAM));
			}
		}

		if (content == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("xcap diff subscription request includes no content, replying forbidden");
			}
			sbb.getParentSbb().newSubscriptionAuthorization(subscriber,
					subscriberDisplayName, notifier, key, expires,
					Response.FORBIDDEN, eventList, serverTransaction);
			return;
		}

		try {
			org.w3c.dom.Document document = DomUtils.DOCUMENT_BUILDER_NS_AWARE_FACTORY
					.newDocumentBuilder().parse(
							new InputSource(new StringReader(content)));
			Element resourceLists = document.getDocumentElement();

			// ok, resource-lists parsed, let's process it's lists elements
			HashSet<String> collectionsToSubscribe = null;
			HashSet<DocumentSelector> documentsToSubscribe = new HashSet<DocumentSelector>();
			HashSet<NodeSubscription> nodeSubscriptions = new HashSet<NodeSubscription>();
			NodeList resourceListsChilds = resourceLists.getChildNodes();
			for (int i = 0; i < resourceListsChilds.getLength(); i++) {
				Node resourceListsChild = resourceListsChilds.item(i);
				if (DomUtils.isElementNamed(resourceListsChild, "list")) {
					// resource-lists/list
					NodeList listChilds = resourceListsChild.getChildNodes();
					Node listChild = null;
					String uri = null;
					for (int j = 0; j < listChilds.getLength(); j++) {
						listChild = listChilds.item(j);
						if (DomUtils.isElementNamed(listChild, "entry")) {
							// resource-lists/list/entry
							uri = ((Element) listChild).getAttribute("uri");
							// process it
							ResourceSelector resourceSelector = null;
							try {
								int queryComponentSeparator = uri.indexOf('?');
								// note: in xcap uris the root start with /, in
								// xcap diff it doesn't, due to separation of
								// full xcap root from doc collection, so we
								// must remove the leading / from config
								if (queryComponentSeparator > 0) {
									resourceSelector = Parser
											.parseResourceSelector(
													ServerConfiguration
															.getInstance()
															.getXcapRoot()
															.substring(1),
													uri.substring(0,
															queryComponentSeparator),
													uri.substring(queryComponentSeparator + 1));
								} else {
									resourceSelector = Parser
											.parseResourceSelector(
													ServerConfiguration
															.getInstance()
															.getXcapRoot()
															.substring(1), uri,
													null);
								}

								// authorize
								if (subscriber != null
										&& !XDM_SERVER_CONFIGURATION
												.getXcapDiffSuperUsers()
												.contains(subscriber)) {
									// not a super user, may only subscribe its
									// own collections
									String[] docSelectorParts = resourceSelector
											.getDocumentSelector().split("/");
									if (docSelectorParts.length < 3
											&& !docSelectorParts[1]
													.equals("users")
											&& !docSelectorParts[2]
													.equals(subscriber)) {
										sbb.getParentSbb()
												.newSubscriptionAuthorization(
														subscriber,
														subscriberDisplayName,
														notifier, key, expires,
														Response.FORBIDDEN,
														eventList,
														serverTransaction);
										return;
									}
								}

								int docSelectorLength = resourceSelector
										.getDocumentSelector().length();
								if (resourceSelector.getDocumentSelector()
										.charAt(docSelectorLength - 1) == '/') {
									// trying to subscribe collection
									String collection = resourceSelector
											.getDocumentSelector().substring(0,
													docSelectorLength - 1);
									if (logger.isInfoEnabled()) {
										logger.info("subscribing collection "
												+ collection);
									}
									if (collectionsToSubscribe == null) {
										collectionsToSubscribe = new HashSet<String>();
									}
									collectionsToSubscribe.add(collection);
								} else {
									// trying to subscribe a document or part of
									// it
									final DocumentSelector documentSelector = DocumentSelector
											.valueOf(resourceSelector
													.getDocumentSelector());
									// parse node selector if exists
									NodeSelector nodeSelector = null;
									if (resourceSelector.getNodeSelector() != null) {
										nodeSelector = Parser
												.parseNodeSelector(
														resourceSelector
																.getNodeSelector(),
														resourceSelector
																.getNamespaceContext());
										if (nodeSelector.getTerminalSelector() != null) {
											// parse terminal selector to
											// validate
											Parser.parseTerminalSelector(nodeSelector
													.getTerminalSelector());
										}
										NodeSubscription nodeSubscription = new NodeSubscription()
												.setDocumentSelector(
														documentSelector)
												.setNodeSelector(nodeSelector)
												.setSel(uri);
										if (logger.isInfoEnabled()) {
											logger.info("subscribing node "
													+ nodeSelector
													+ "of document "
													+ documentSelector);
										}
										if (nodeSubscriptions == null) {
											nodeSubscriptions = new HashSet<NodeSubscription>();
										}
										nodeSubscriptions.add(nodeSubscription);
									} else {
										if (logger.isInfoEnabled()) {
											logger.info("subscribing document "
													+ documentSelector);
										}
										if (documentsToSubscribe == null) {
											documentsToSubscribe = new HashSet<DocumentSelector>();
										}
										documentsToSubscribe
												.add(documentSelector);
									}
								}
							} catch (Exception e) {
								logger.error(
										"failed to parse entry uri to subscribe, ignoring "
												+ uri, e);
							}
						}
					}
				}
			}

			// create subscriptions object
			Subscriptions subscriptions = new Subscriptions(key, subscriber,
					collectionsToSubscribe, documentsToSubscribe,
					nodeSubscriptions, diffProcessing);
			// get subscriptions map cmp
			SubscriptionsMap subscriptionsMap = sbb.getSubscriptionsMap();
			if (subscriptionsMap == null) {
				subscriptionsMap = new SubscriptionsMap();
			}
			// build set of other documents and collections already subscribed
			// by
			// this entity
			HashSet<DocumentSelector> documentSelectorsAlreadySubscribed = null;
			HashSet<String> collectionsAlreadySubscribed = null;
			for (Subscriptions s : subscriptionsMap.getSubscriptions()) {
				if (!s.getCollectionSubscriptions().isEmpty()) {
					if (collectionsAlreadySubscribed == null) {
						collectionsAlreadySubscribed = new HashSet<String>();
					}
					collectionsAlreadySubscribed.addAll(s
							.getCollectionSubscriptions());
				}
				Set<DocumentSelector> subscribedDocuments = s
						.getAllDocumentsToSubscribe();
				if (!subscribedDocuments.isEmpty()) {
					if (documentSelectorsAlreadySubscribed == null) {
						documentSelectorsAlreadySubscribed = new HashSet<DocumentSelector>();
					}
					documentSelectorsAlreadySubscribed
							.addAll(subscribedDocuments);
				}
			}
			// save subscriptions object on cmp
			subscriptionsMap.put(subscriptions);
			sbb.setSubscriptionsMap(subscriptionsMap);
			// let's subscribe all documents and/or collections
			DataSourceActivityContextInterfaceFactory dataSourceActivityContextInterfaceFactory = sbb
					.getDataSourceActivityContextInterfaceFactory();
			DataSourceSbbInterface dataSourceSbbInterface = sbb
					.getDataSourceSbbInterface();
			SbbLocalObject sbbLocalObject = sbb.getSbbContext()
					.getSbbLocalObject();
			for (DocumentSelector documentSelector : subscriptions
					.getAllDocumentsToSubscribe()) {
				if (documentSelectorsAlreadySubscribed == null
						|| !documentSelectorsAlreadySubscribed
								.contains(documentSelector)) {
					// this document selector is not subscribed already due
					// to another
					// subscription in the same entity, so subscribe the doc
					DocumentActivity activity = dataSourceSbbInterface
							.createDocumentActivity(documentSelector);
					ActivityContextInterface aci = dataSourceActivityContextInterfaceFactory
							.getActivityContextInterface(activity);
					aci.attach(sbbLocalObject);
				}
			}
			for (String collection : subscriptions.getCollectionSubscriptions()) {
				if (collectionsAlreadySubscribed == null
						|| !collectionsAlreadySubscribed.contains(collection)) {
					// collections already subscribed does not match this
					// collection,
					// so subscribe it
					CollectionActivity activity = dataSourceSbbInterface
							.createCollectionActivity(collection);
					ActivityContextInterface aci = dataSourceActivityContextInterfaceFactory
							.getActivityContextInterface(activity);
					aci.attach(sbbLocalObject);
				}
			}

			// continue new subscription process
			sbb.getParentSbb().newSubscriptionAuthorization(subscriber,
					subscriberDisplayName, notifier, key, expires, Response.OK,
					eventList, serverTransaction);
		} catch (Exception e) {
			logger.error("failed to parse resource-lists in initial subscribe",
					e);
			sbb.getParentSbb().newSubscriptionAuthorization(subscriber,
					subscriberDisplayName, notifier, key, expires,
					Response.FORBIDDEN, eventList, serverTransaction);
		}

	}

	public void removingSubscription(Subscription subscription,
			XcapDiffSubscriptionControlSbbInterface sbb) {

		// get subscriptions map and remove subscription terminating
		SubscriptionsMap subscriptionsMap = sbb.getSubscriptionsMap();
		if (subscriptionsMap != null) {
			Subscriptions subscriptions = subscriptionsMap.remove(subscription
					.getKey());

			// build set of other documents and collections already subscribed
			// by
			// this entity
			HashSet<DocumentSelector> documentSelectorsSubscribedByOthers = null;
			HashSet<String> collectionsSubscribedByOthers = null;
			for (Subscriptions s : subscriptionsMap.getSubscriptions()) {
				for (DocumentSelector ds : s.getAllDocumentsToSubscribe()) {
					if (documentSelectorsSubscribedByOthers == null) {
						documentSelectorsSubscribedByOthers = new HashSet<DocumentSelector>();
					}
					documentSelectorsSubscribedByOthers.add(ds);
				}
				for (String collection : s.getCollectionSubscriptions()) {
					if (collectionsSubscribedByOthers == null) {
						collectionsSubscribedByOthers = new HashSet<String>();
					}
					collectionsSubscribedByOthers.add(collection);
				}
			}

			// now unsubscribe each that was subscribed only by the subscription
			// terminating
			SbbLocalObject sbbLocalObject = sbb.getSbbContext()
					.getSbbLocalObject();
			for (ActivityContextInterface aci : sbb.getSbbContext()
					.getActivities()) {
				Object activity = aci.getActivity();
				if (activity instanceof DocumentActivity) {
					String aciDS = ((DocumentActivity) activity)
							.getDocumentSelector();
					for (DocumentSelector ds : subscriptions
							.getDocumentSubscriptions()) {
						if (documentSelectorsSubscribedByOthers == null
								|| (ds.toString().equals(aciDS) && !documentSelectorsSubscribedByOthers
										.contains(ds))) {
							// safe to unsubscribe this document
							aci.detach(sbbLocalObject);
						}
					}
				} else if (activity instanceof CollectionActivity) {
					String aciCollection = ((CollectionActivity) activity)
							.getCollection();
					for (String collection : subscriptions
							.getCollectionSubscriptions()) {
						if (collectionsSubscribedByOthers == null
								|| (collection.equals(aciCollection) && !collectionsSubscribedByOthers
										.contains(collection))) {
							// safe to unsubscribe this collection
							aci.detach(sbbLocalObject);
						}
					}
				}
			}
		} else {
			logger.warn("Removing subscription but map of subscriptions is null");
		}
	}

	private static final DOMXcapDiffPatchBuilder XCAP_DIFF_PATCH_BUILDER = new DOMXcapDiffFactory()
			.getPatchBuilder();

	public NotifyContent getNotifyContent(Subscription subscription,
			XcapDiffSubscriptionControlSbbInterface sbb) {

		// let's gather all content for this subscription
		SubscriptionsMap subscriptionsMap = sbb.getSubscriptionsMap();
		if (subscriptionsMap == null) {
			logger.error("failed to get notify content for subscription "
					+ subscription
					+ ", there are no xcap diff subscriptions map");
			return null;
		}
		Subscriptions subscriptions = subscriptionsMap.get(subscription
				.getKey());
		if (subscriptions == null) {
			logger.error("failed to get notify content for subscription "
					+ subscription
					+ ", there are no xcap diff subscriptions for such subscription in map");
			return null;
		}

		Set<Element> patchComponents = new HashSet<Element>();
		// let's process first collections
		for (String collection : subscriptions.getCollectionSubscriptions()) {
			// get documents that exist in this collection
			if (logger.isDebugEnabled()) {
				logger.debug("building patch component for collection "
						+ collection);
			}
			try {
				for (Document document : sbb.getDataSourceSbbInterface()
						.getDocuments(collection, true)) {
					DocumentSelector documentSelector = new DocumentSelector(
							document.getCollection(),
							document.getDocumentName());
					try {
						patchComponents.add(XCAP_DIFF_PATCH_BUILDER
								.getDocumentPatchComponentBuilder()
								.buildPatchComponent(
										documentSelector.toString(), null,
										document.getETag(), null));
					} catch (BuildPatchException e) {
						logger.error(e.getMessage(), e);
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		// now individual docs
		for (DocumentSelector documentSelector : subscriptions
				.getDocumentSubscriptions()) {
			if (logger.isDebugEnabled()) {
				logger.debug("building patch component for document subscription "
						+ documentSelector);
			}
			Document document = null;
			try {
				document = sbb.getDataSourceSbbInterface().getDocument(
						documentSelector);
			} catch (InternalServerErrorException e) {
				logger.error(e);
			}
			if (document != null) {
				try {
					patchComponents.add(XCAP_DIFF_PATCH_BUILDER
							.getDocumentPatchComponentBuilder()
							.buildPatchComponent(documentSelector.toString(),
									null, document.getETag(), null));
				} catch (BuildPatchException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		// now document nodes
		Document document = null;
		NodeSelector nodeSelector = null;
		for (NodeSubscription nodeSubscription : subscriptions
				.getNodeSubscriptions()) {
			if (logger.isDebugEnabled()) {
				logger.debug("building patch component for node subscription "
						+ nodeSubscription);
			}
			try {
				document = sbb.getDataSourceSbbInterface().getDocument(
						nodeSubscription.getDocumentSelector());
			} catch (InternalServerErrorException e) {
				logger.error(e);
			}
			boolean exists = document != null;
			if (exists) {
				// find out if the node exists
				nodeSelector = nodeSubscription.getNodeSelector();
				org.w3c.dom.Document documentDOM = null;
				try {
					documentDOM = document.getAsDOMDocument();
				} catch (InternalServerErrorException e) {
					logger.error(e.getMessage(), e);
					continue;
				}
				AppUsage appUsage = APP_USAGE_MANAGEMENT
						.getAppUsage(nodeSubscription.getDocumentSelector()
								.getAUID());
				if (appUsage == null) {
					logger.error("app usage not available, unable to process node subscription "
							+ nodeSubscription);
					continue;
				}
				nodeSelector.getNamespaceContext().setDefaultDocNamespace(
						appUsage.getDefaultDocumentNamespace());
				final XPath xpath = DomUtils.XPATH_FACTORY.newXPath();
				xpath.setNamespaceContext(nodeSelector.getNamespaceContext());
				Node node = null;
				try {
					// exec query to get element
					final NodeList nodeList = (NodeList) xpath.evaluate(
							nodeSelector.toStringWithEmptyPrefix(),
							documentDOM, XPathConstants.NODESET);
					if (nodeList.getLength() == 0) {
						// node does not exists
						if (logger.isDebugEnabled()) {
							try {
								logger.debug("no node matches expression "
										+ nodeSelector
												.toStringWithEmptyPrefix()
										+ " in doc: \n"
										+ TextWriter.toString(documentDOM));
							} catch (TransformerException e) {
								logger.error(e);
							}
						}
						exists = false;
					} else {
						node = nodeList.item(0);
					}
				} catch (XPathExpressionException e) {
					logger.error(
							"failed to find node " + nodeSelector + "in doc "
									+ nodeSubscription.getDocumentSelector(), e);
					continue;
				}
				try {
					if (nodeSubscription.getNodeSelector()
							.getTerminalSelector() != null) {
						if (exists) {
							patchComponents.add(XCAP_DIFF_PATCH_BUILDER
									.getAttributePatchComponentBuilder()
									.buildPatchComponent(
											nodeSubscription.getSel(),
											((Attr) node).getValue(),
											nodeSelector.getNamespaceContext()
													.getNamespaces()));
						} else {
							patchComponents.add(XCAP_DIFF_PATCH_BUILDER
									.getAttributePatchComponentBuilder()
									.buildPatchComponent(
											nodeSubscription.getSel(),
											nodeSelector.getNamespaceContext()
													.getNamespaces()));
						}
					} else {
						if (exists) {
							patchComponents.add(XCAP_DIFF_PATCH_BUILDER
									.getElementPatchComponentBuilder()
									.buildPatchComponent(
											nodeSubscription.getSel(),
											node,
											nodeSelector.getNamespaceContext()
													.getNamespaces()));
						} else {
							patchComponents.add(XCAP_DIFF_PATCH_BUILDER
									.getElementPatchComponentBuilder()
									.buildPatchComponent(
											nodeSubscription.getSel(),
											false,
											nodeSelector.getNamespaceContext()
													.getNamespaces()));
						}

					}
				} catch (BuildPatchException e) {
					logger.error(e.getMessage(), e);
					continue;
				}
			}
		}

		// build notify content
		org.w3c.dom.Document xcapDiff = null;
		try {
			xcapDiff = XCAP_DIFF_PATCH_BUILDER.buildPatch(
					XDM_SERVER_CONFIGURATION.getFullXcapRoot(), patchComponents
							.toArray(new Element[patchComponents.size()]));
			if (logger.isInfoEnabled()) {
				try {
					logger.info("xcap diff notify content for subscription "
							+ subscription + ":\n"
							+ TextWriter.toString(xcapDiff));
				} catch (TransformerException e) {
					logger.error(e.getMessage(), e);
				}
			}
		} catch (BuildPatchException e) {
			logger.error("failed to build xcap diff patch", e);
			return null;
		}
		return new NotifyContent(xcapDiff, getXcapDiffContentTypeHeader(sbb),
				null);

	}

	public Object filterContentPerSubscriber(Subscription subscription,
			Object unmarshalledContent,
			XcapDiffSubscriptionControlSbbInterface sbb) {
		return unmarshalledContent;
	}

	/**
	 * 
	 * @param event
	 * @param aci
	 * @param sbb
	 */
	public void documentUpdated(DocumentUpdatedEvent event,
			ActivityContextInterface aci,
			XcapDiffSubscriptionControlSbbInterface sbb) {

		if (logger.isDebugEnabled()) {
			logger.debug("document " + event.getDocumentSelector()
					+ " updated.");
		}

		SubscriptionsMap subscriptionsMap = sbb.getSubscriptionsMap();
		if (subscriptionsMap == null) {
			return;
		}

		String eventCollection = null;
		Object activity = aci.getActivity();
		if (activity instanceof CollectionActivity) {
			eventCollection = ((CollectionActivity) activity).getCollection();
			if (logger.isDebugEnabled()) {
				logger.debug("document update in a collection activity -> "
						+ eventCollection);
			}
		}

		for (Subscriptions s : subscriptionsMap.getSubscriptions()) {

			if (logger.isDebugEnabled()) {
				logger.debug("processing subscription " + s.getKey());
			}

			if (eventCollection != null) {
				// event was fired in a collection, look for subscriptions with
				// such collection
				for (String collection : s.getCollectionSubscriptions()) {
					if (eventCollection.equals(collection)) {
						if (logger.isDebugEnabled()) {
							logger.debug("subscription " + s.getKey()
									+ " is subscribed to collection "
									+ collection
									+ ", requesting notification...");
						}
						notifySubscriber(s, event, sbb);
					}
				}

			} else {
				// event was fired in the document activity, no need to check
				// collections
				for (DocumentSelector ds : s.getDocumentSubscriptions()) {
					if (ds.equals(event.getDocumentSelector())) {
						if (logger.isDebugEnabled()) {
							logger.debug("subscription " + s.getKey()
									+ " is subscribed to document " + ds
									+ ", requesting notification...");
						}
						notifySubscriber(s, event, sbb);
					}
				}

				for (NodeSubscription ns : s.getNodeSubscriptions()) {
					if (ns.getDocumentSelector().equals(
							event.getDocumentSelector())) {
						if (logger.isDebugEnabled()) {
							logger.debug("subscription " + s.getKey()
									+ " is subscribed to " + ns
									+ ", requesting notification...");
						}
						// tell underlying sip event framework to notify
						// subscriber
						try {
							sbb.getParentSbb().notifySubscriber(
									s.getKey(),
									new NotifyContent(
											event.getNodeXcapDiff(ns),
											getXcapDiffContentTypeHeader(sbb),
											null));
						} catch (BuildPatchException e) {
							logger.error(
									"Failed to build and notify xcap diff for subscription "
											+ s + " and node "
											+ ns.getNodeSelector()
											+ " in document "
											+ event.getDocumentSelector(), e);
						}
					}
				}
			}
		}
	}

	private void notifySubscriber(Subscriptions s, DocumentUpdatedEvent event,
			XcapDiffSubscriptionControlSbbInterface sbb) {
		// tell underlying sip event framework to notify
		// subscriber
		try {
			if (s.getDiffProcessing() == null
					|| s.getDiffProcessing() == DiffProcessing.NoPatching) {
				sbb.getParentSbb().notifySubscriber(
						s.getKey(),
						new NotifyContent(event.getDocXcapDiff(false),
								getXcapDiffContentTypeHeader(sbb), null));
			} else {
				sbb.getParentSbb().notifySubscriber(
						s.getKey(),
						new NotifyContent(event.getDocXcapDiff(true),
								getXcapDiffContentTypeHeader(sbb),
								EVENT_HEADER_PATCHING_PARAMS));
			}

		} catch (BuildPatchException e) {
			logger.error(
					"Failed to build and notify xcap diff for subscription "
							+ s + " and document "
							+ event.getDocumentSelector(), e);
		}
	}

	private static Logger logger = Logger
			.getLogger(XcapDiffSubscriptionControl.class);

}
