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

package org.mobicents.slee.sipevent.server.subscription.winfo;

import java.text.ParseException;

import javax.sip.header.ContentTypeHeader;
import javax.slee.ActivityContextInterface;
import javax.slee.facilities.Tracer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionControlSbb;
import org.mobicents.slee.sipevent.server.subscription.WInfoNotifyEvent;
import org.mobicents.slee.sipevent.server.subscription.WInfoNotifyEvent.Watcher;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionControlDataSource;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.mobicents.xdm.common.util.dom.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Service logic regarding winfo subscriptions
 * 
 * @author martins
 * 
 */
public class WInfoSubscriptionHandler {

	private static Tracer tracer;

	private SubscriptionControlSbb sbb;

	public WInfoSubscriptionHandler(SubscriptionControlSbb sbb) {
		this.sbb = sbb;
		if (tracer == null) {
			tracer = sbb.getSbbContext().getTracer(getClass().getSimpleName());
		}
	}

	public void notifyWinfoSubscriptions(
			SubscriptionControlDataSource dataSource,
			Subscription subscription,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {

		if (!subscription.getKey().isWInfoSubscription()) {

			for (Subscription winfoSubscription : dataSource
					.getSubscriptionsByNotifierAndEventPackage(subscription
							.getNotifier().getUri(), subscription.getKey()
							.getEventPackage() + ".winfo")) {

				if (winfoSubscription.getStatus() == Subscription.Status.active) {

					try {
						// get subscription aci
						ActivityContextInterface winfoAci = sbb
								.getActivityContextNamingfacility().lookup(
										winfoSubscription.getKey().toString());
						if (winfoAci != null) {
							// fire winfo notify event
							sbb.fireWInfoNotifyEvent(
									new WInfoNotifyEvent(winfoSubscription
											.getKey(), subscription.getKey(),
											createWInfoWatcher(subscription)),
									winfoAci, null);
						} else {
							// aci is gone, cleanup subscription
							tracer.warning("Unable to find subscription aci to notify subscription "
									+ winfoSubscription.getKey()
									+ ". Removing subscription data");
							sbb.removeSubscriptionData(dataSource,
									winfoSubscription, null, null, childSbb);
						}
					} catch (Exception e) {
						tracer.severe("failed to notify winfo subscriber", e);
					}
				}
			}
		}
	}

	/*
	 * creates watcher object for a subscription
	 */
	private Watcher createWInfoWatcher(Subscription subscription) {
		// create watcher
		Watcher watcher = new Watcher();
		watcher.setId(String.valueOf(subscription.hashCode()));
		watcher.setStatus(subscription.getStatus().toString());
		watcher.setDurationSubscribed(subscription.getSubscriptionDuration());
		if (subscription.getLastEvent() != null) {
			watcher.setEvent(subscription.getLastEvent().toString());
		}
		if (subscription.getSubscriberDisplayName() != null) {
			watcher.setDisplayName(subscription.getSubscriberDisplayName());
		}
		if (!subscription.getStatus().equals(Subscription.Status.terminated)) {
			watcher.setExpiration(subscription.getRemainingExpires());
		}
		watcher.setValue(subscription.getSubscriber());
		return watcher;
	}

	/*
	 * creates partial watcher info doc
	 */
	public Document getPartialWatcherInfoContent(
			Subscription winfoSubscription,
			SubscriptionKey watcherSubscriptionKey, Watcher watcher) {
		// create watcher info
		Document document = createWatcherInfoDocument(winfoSubscription,
				"partial");
		Element watcherList = addWatcherInfoDocumentWatcherList(
				winfoSubscription, document.getDocumentElement());
		addWatcherInfoDocumentWatcher(watcherList, watcher);
		return document;
	}

	/*
	 * generates full watcher info doc
	 */
	public Document getFullWatcherInfoContent(
			SubscriptionControlDataSource dataSource,
			Subscription winfoSubscription) {
		// create watcher info
		Document document = createWatcherInfoDocument(winfoSubscription, "full");
		Element watcherList = addWatcherInfoDocumentWatcherList(
				winfoSubscription, document.getDocumentElement());
		// get watcher subscriptions
		// and add a watcher element for each
		Watcher watcher = null;
		for (Subscription subscription : dataSource
				.getSubscriptionsByNotifierAndEventPackage(winfoSubscription
						.getNotifier().getUri(), watcherList
						.getAttribute("package"))) {
			// create and add watcher to watcher info list
			watcher = createWInfoWatcher(subscription);
			addWatcherInfoDocumentWatcher(watcherList, watcher);
		}
		return document;
	}

	private Document createWatcherInfoDocument(Subscription winfoSubscription,
			String state) {
		DocumentBuilder builder = null;
		try {
			builder = DomUtils.DOCUMENT_BUILDER_NS_AWARE_FACTORY
					.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			tracer.severe("failed to create dom doc builder", e);
			return null;
		}
		Document document = builder.newDocument();
		Element watcherInfo = document.createElementNS(
				"urn:ietf:params:xml:ns:watcherinfo", "watcherinfo");
		winfoSubscription.incrementVersion();
		winfoSubscription.store();
		watcherInfo.setAttribute("version",
				Integer.toString(winfoSubscription.getVersion()));
		watcherInfo.setAttribute("state", state);
		document.appendChild(watcherInfo);
		return document;
	}

	private void addWatcherInfoDocumentWatcher(Element watcherList,
			Watcher watcher) {
		Element watcherElement = watcherList.getOwnerDocument().createElement(
				"watcher");
		if (watcher.getDisplayName() != null) {
			watcherElement.setAttribute("display-name",
					watcher.getDisplayName());
		}
		if (watcher.getDurationSubscribed() != null) {
			watcherElement.setAttribute("duration-subscribed", watcher
					.getDurationSubscribed().toString());
		}
		watcherElement.setAttribute("event", watcher.getEvent());
		if (watcher.getExpiration() != null) {
			watcherElement.setAttribute("expiration", watcher.getExpiration()
					.toString());
		}
		watcherElement.setAttribute("id", watcher.getId());
		if (watcher.getLang() != null) {
			watcherElement.setAttribute("lang", watcher.getLang());
		}
		watcherElement.setAttribute("status", watcher.getStatus());
		watcherElement.setTextContent(watcher.getValue());
		watcherList.appendChild(watcherElement);
	}

	private Element addWatcherInfoDocumentWatcherList(
			Subscription winfoSubscription, Element watcherInfo) {
		Element watcherList = watcherInfo.getOwnerDocument().createElement(
				"watcherinfo");
		watcherList.setAttribute("resource", winfoSubscription.getNotifier()
				.getUri());
		String winfoEventPackage = winfoSubscription.getKey().getEventPackage();
		String eventPackage = winfoEventPackage.substring(0,
				winfoEventPackage.indexOf(".winfo"));
		watcherList.setAttribute("package", eventPackage);
		watcherInfo.appendChild(watcherList);
		return watcherList;
	}

	public ContentTypeHeader getWatcherInfoContentHeader() {
		try {
			return sbb.getHeaderFactory().createContentTypeHeader(
					"application", "watcherinfo+xml");
		} catch (ParseException e) {
			tracer.severe("failure creating content type header", e);
			return null;
		}
	}
}
