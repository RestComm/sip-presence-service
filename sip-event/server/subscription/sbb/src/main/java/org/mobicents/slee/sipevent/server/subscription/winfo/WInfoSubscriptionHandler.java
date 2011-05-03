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

import java.io.StringWriter;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.List;

import javax.sip.header.ContentTypeHeader;
import javax.slee.ActivityContextInterface;
import javax.slee.facilities.Tracer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionControlSbb;
import org.mobicents.slee.sipevent.server.subscription.WInfoNotifyEvent;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionControlDataSource;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.mobicents.slee.sipevent.server.subscription.winfo.pojo.Watcher;
import org.mobicents.slee.sipevent.server.subscription.winfo.pojo.WatcherList;
import org.mobicents.slee.sipevent.server.subscription.winfo.pojo.Watcherinfo;

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

	public void notifyWinfoSubscriptions(SubscriptionControlDataSource dataSource,Subscription subscription,
			ImplementedSubscriptionControlSbbLocalObject childSbb) {

		if (!subscription.getKey().isWInfoSubscription()) {
			
			for (Subscription winfoSubscription : dataSource
					.getSubscriptionsByNotifierAndEventPackage(subscription
							.getNotifier().getUri(), subscription.getKey()
							.getEventPackage()
							+ ".winfo")) {

				if (winfoSubscription.getStatus() == Subscription.Status.active) {

					try {
						// get subscription aci
						ActivityContextInterface winfoAci = sbb
								.getActivityContextNamingfacility().lookup(
										winfoSubscription.getKey().toString());
						if (winfoAci != null) {
							// fire winfo notify event
							sbb.fireWInfoNotifyEvent(new WInfoNotifyEvent(winfoSubscription.getKey(), subscription.getKey(), createWInfoWatcher(subscription)), winfoAci, null);							
						} else {
							// aci is gone, cleanup subscription
							tracer
									.warning("Unable to find subscription aci to notify subscription "
											+ winfoSubscription.getKey()
											+ ". Removing subscription data");
							sbb.removeSubscriptionData(dataSource,winfoSubscription, null,
									null, childSbb);
						}
					} catch (Exception e) {
						tracer.severe("failed to notify winfo subscriber", e);
					}
				}
			}
		}
	}

	private static final JAXBContext winfoJAXBContext = initWInfoJAXBContext();

	private static JAXBContext initWInfoJAXBContext() {
		try {
			return JAXBContext
					.newInstance("org.mobicents.slee.sipevent.server.subscription.winfo.pojo");
		} catch (JAXBException e) {
			tracer.severe("failed to create winfo jaxb context");
			return null;
		}
	}

	private Marshaller getWInfoMarshaller() {
		try {
			return winfoJAXBContext.createMarshaller();
		} catch (JAXBException e) {
			tracer.severe("failed to create winfo unmarshaller", e);
			return null;
		}
	}

	/*
	 * creates watcher jaxb object for a subscription
	 */
	private Watcher createWInfoWatcher(Subscription subscription) {
		// create watcher
		Watcher watcher = new Watcher();
		watcher.setId(String.valueOf(subscription.hashCode()));
		watcher.setStatus(subscription.getStatus().toString());
		watcher.setDurationSubscribed(BigInteger.valueOf(subscription
				.getSubscriptionDuration()));
		if (subscription.getLastEvent() != null) {
			watcher.setEvent(subscription.getLastEvent().toString());
		}
		if (subscription.getSubscriberDisplayName() != null) {
			watcher.setDisplayName(subscription.getSubscriberDisplayName());
		}
		if (!subscription.getStatus().equals(Subscription.Status.terminated)) {
			watcher.setExpiration(BigInteger.valueOf(subscription
					.getRemainingExpires()));
		}
		watcher.setValue(subscription.getSubscriber());
		return watcher;
	}

	/*
	 * marshals a jaxb watcherinfo object to string
	 */
	private String marshallWInfo(Watcherinfo watcherinfo) {
		// marshall to string
		String result = null;
		StringWriter stringWriter = new StringWriter();
		try {
			Marshaller marshaller = getWInfoMarshaller();
			marshaller.marshal(watcherinfo, stringWriter);
			result = stringWriter.toString();
			stringWriter.close();
		} catch (Exception e) {
			tracer.severe("failed to marshall winfo", e);
			try {
				stringWriter.close();
			} catch (Exception f) {
				tracer.severe("failed to close winfo string writer", f);
			}
		}

		return result;
	}

	/*
	 * creates partial watcher info doc
	 */
	public String getPartialWatcherInfoContent(Subscription winfoSubscription,
			SubscriptionKey watcherSubscriptionKey, Watcher watcher) {
		// create watcher info
		Watcherinfo watcherinfo = new Watcherinfo();
		watcherinfo.setVersion(BigInteger.valueOf(winfoSubscription
				.getVersion()));
		watcherinfo.setState("partial");
		// create watcher list
		WatcherList watcherList = new WatcherList();
		watcherList.setResource(winfoSubscription.getNotifier().getUri());
		watcherList.setPackage(watcherSubscriptionKey.getEventPackage());
		// create and add watcher to watcher info list
		watcherList.getWatcher().add(watcher);
		// add watcher list to watcher info
		watcherinfo.getWatcherList().add(watcherList);
		// marshall and return
		return marshallWInfo(watcherinfo);
	}

	/*
	 * generates full watcher info doc
	 */
	public String getFullWatcherInfoContent(SubscriptionControlDataSource dataSource,Subscription winfoSubscription) {

		// create watcher info
		Watcherinfo watcherinfo = new Watcherinfo();
		watcherinfo.setVersion(BigInteger.valueOf(winfoSubscription
				.getVersion()));
		watcherinfo.setState("full");
		// create watcher list
		WatcherList watcherList = new WatcherList();
		watcherList.setResource(winfoSubscription.getNotifier().getUri());
		String winfoEventPackage = winfoSubscription.getKey().getEventPackage();
		String eventPackage = winfoEventPackage.substring(0, winfoEventPackage
				.indexOf(".winfo"));
		watcherList.setPackage(eventPackage);
		// get watcher subscriptions
		// add a watcher element for each
		List<Watcher> watchers = watcherList.getWatcher();
		for(Subscription subscription : dataSource.getSubscriptionsByNotifierAndEventPackage(winfoSubscription.getNotifier().getUri(), eventPackage)) {
			// create and add watcher to watcher info list
			watchers.add(createWInfoWatcher(subscription));
		}
		// add watcher list to watcher info
		watcherinfo.getWatcherList().add(watcherList);
		// marshall and return
		return marshallWInfo(watcherinfo);
	}

	public ContentTypeHeader getWatcherInfoContentHeader() {
		try {
			return sbb.getHeaderFactory().createContentTypeHeader("application",
					"watcherinfo+xml");
		} catch (ParseException e) {
			tracer.severe("failure creating content type header", e);
			return null;
		}
	}
}
