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

package org.mobicents.slee.sippresence.server.subscription;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.sip.ServerTransaction;
import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.resource.StartActivityException;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.publication.data.ComposedPublication;
import org.mobicents.slee.sipevent.server.subscription.NotifyContent;
import org.mobicents.slee.sipevent.server.subscription.data.Notifier;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.mobicents.slee.sippresence.server.presrulescache.PresRulesActivity;
import org.mobicents.slee.sippresence.server.subscription.rules.OMAPresRule;
import org.mobicents.slee.sippresence.server.subscription.rules.OMAPresRuleDOMTransformer;
import org.mobicents.slee.sippresence.server.subscription.rules.PresRuleCMPKey;
import org.mobicents.slee.sippresence.server.subscription.rules.RulesetProcessor;
import org.mobicents.slee.sippresence.server.subscription.rules.SubHandlingAction;
import org.mobicents.xdm.common.util.dom.DomUtils;
import org.openxdm.xcap.client.appusage.presrules.jaxb.commonpolicy.Ruleset;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Logic for the sip presence subscription control, which complements the SIP
 * Event generic logic.
 * 
 * @author martins
 * 
 */
public class PresenceSubscriptionControl {

	private static Logger logger = Logger
			.getLogger(PresenceSubscriptionControl.class);

	private static final OMAPresRuleDOMTransformer PRES_RULE_TRANSFORMER = new OMAPresRuleDOMTransformer();

	private static final String[] eventPackages = { "presence" };

	public static String[] getEventPackages() {
		return eventPackages;
	}

	@SuppressWarnings("unchecked")
	public void isSubscriberAuthorized(String subscriber,
			String subscriberDisplayName, Notifier notifier,
			SubscriptionKey key, int expires, String content,
			String contentType, String contentSubtype, boolean eventList,
			String presRulesAUID, String presRulesDocumentName,
			ServerTransaction serverTransaction,
			PresenceSubscriptionControlSbbInterface sbb) {

		DocumentSelector documentSelector = getDocumentSelector(
				notifier.getUri(), presRulesAUID, presRulesDocumentName);
		if (documentSelector == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to create document selector for notifier "
						+ notifier.getUri()
						+ ", can't proceed with subscription.");
			}
			sbb.getParentSbb().newSubscriptionAuthorization(subscriber,
					subscriberDisplayName, notifier, key, expires,
					Response.FORBIDDEN, eventList, serverTransaction);
			return;
		}

		// get pres rules activity and attach sbb to receive updates
		PresRulesActivity activity = null;
		try {
			activity = sbb.getPresRulesSbbInterface().getActivity(
					documentSelector);
		} catch (StartActivityException e) {
			logger.error(
					"Failed to start activity, can't proceed, refusing subscription",
					e);
			sbb.getParentSbb().newSubscriptionAuthorization(subscriber,
					subscriberDisplayName, notifier, key, expires,
					Response.FORBIDDEN, eventList, serverTransaction);
			return;
		}

		sbb.getPresRulesACIF().getActivityContextInterface(activity)
				.attach(sbb.getSbbLocalObject());

		// get ruleset
		Ruleset ruleset = activity.getRuleset();
		if (ruleset == null) {
			if (logger.isInfoEnabled()) {
				logger.info("Notifier " + notifier.getUri()
						+ " has no ruleset, allowing subscription " + key);
			}
			OMAPresRule combinedRule = new OMAPresRule(documentSelector);
			combinedRule.setProvideAllDevices(true);
			combinedRule.setProvideAllAttributes(true);
			combinedRule.setProvideAllPersons(true);
			combinedRule.setProvideAllServices(true);
			combinedRule.setSubHandling(SubHandlingAction.allow);
			HashMap<PresRuleCMPKey, OMAPresRule> combinedRules = sbb
					.getCombinedRules();
			if (combinedRules == null) {
				combinedRules = new HashMap<PresRuleCMPKey, OMAPresRule>();
			}
			combinedRules.put(new PresRuleCMPKey(subscriber, notifier, key),
					combinedRule);
			sbb.setCombinedRules(combinedRules);
			sbb.getParentSbb().newSubscriptionAuthorization(subscriber,
					subscriberDisplayName, notifier, key, expires, Response.OK,
					eventList, serverTransaction);
			return;
		}

		// process ruleset
		RulesetProcessor rulesetProcessor = new RulesetProcessor(subscriber,
				notifier.getUri(), ruleset, documentSelector, sbb);
		OMAPresRule combinedRule = rulesetProcessor.getCombinedRule();
		int responseCode = combinedRule.getSubHandling().getResponseCode();
		if (responseCode < 300) {
			// save combined rule, the subscription will be created
			HashMap<PresRuleCMPKey, OMAPresRule> combinedRules = sbb
					.getCombinedRules();
			if (combinedRules == null) {
				combinedRules = new HashMap<PresRuleCMPKey, OMAPresRule>();
			}
			combinedRules.put(new PresRuleCMPKey(subscriber, notifier, key),
					combinedRule);
			sbb.setCombinedRules(combinedRules);
		}
		sbb.getParentSbb().newSubscriptionAuthorization(subscriber,
				subscriberDisplayName, notifier, key, expires, responseCode,
				eventList, serverTransaction);
	}

	@SuppressWarnings("unchecked")
	public void removingSubscription(Subscription subscription,
			String presRulesAUID, String presRulesDocumentName,
			PresenceSubscriptionControlSbbInterface sbb) {

		if (logger.isDebugEnabled()) {
			logger.debug("removingSubscription(" + subscription + ")");
		}
		// get combined rules cmp
		HashMap<PresRuleCMPKey, OMAPresRule> combinedRules = sbb
				.getCombinedRules();
		if (combinedRules != null) {
			// remove subscription from map
			if (logger.isDebugEnabled()) {
				logger.debug("combined rules: " + combinedRules.keySet());
			}
			OMAPresRule ruleRemoved = combinedRules.remove(new PresRuleCMPKey(
					subscription.getSubscriber(), subscription.getNotifier(),
					subscription.getKey()));
			if (ruleRemoved != null) {

				boolean detachFromPresRulesActivity = true;
				for (OMAPresRule rule : combinedRules.values()) {
					if (rule.getDocumentSelector().equals(
							ruleRemoved.getDocumentSelector())) {
						// there is at least another subscription using same
						// pres rules activity, can't unsubscribe
						detachFromPresRulesActivity = false;
						break;
					}
				}

				if (detachFromPresRulesActivity) {
					PresRulesActivity activity = null;
					try {
						activity = sbb.getPresRulesSbbInterface().getActivity(
								ruleRemoved.getDocumentSelector());
					} catch (StartActivityException e) {
						logger.error(e.getMessage(), e);
						// ignore
						return;
					}
					ActivityContextInterface aci = sbb.getPresRulesACIF()
							.getActivityContextInterface(activity);
					aci.detach(sbb.getSbbLocalObject());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void rulesetUpdated(DocumentSelector documentSelector,
			Ruleset ruleset, PresenceSubscriptionControlSbbInterface sbb) {

		// get current combined rules from cmp
		HashMap<PresRuleCMPKey, OMAPresRule> combinedRules = sbb
				.getCombinedRules();
		if (combinedRules == null) {
			combinedRules = new HashMap<PresRuleCMPKey, OMAPresRule>();
		}

		// for each combined rules that has the user that updated the doc as
		// notifier reprocess the rules
		OMAPresRule oldCombinedRule = null;
		OMAPresRule newCombinedRule = null;
		PresRuleCMPKey presRuleCMPKey = null;
		SubscriptionKey subscriptionKey = null;
		for (Entry<PresRuleCMPKey, OMAPresRule> entry : combinedRules
				.entrySet()) {
			oldCombinedRule = entry.getValue();
			if (documentSelector.equals(oldCombinedRule.getDocumentSelector())) {
				presRuleCMPKey = entry.getKey();
				subscriptionKey = presRuleCMPKey.getSubscriptionKey();
				newCombinedRule = new RulesetProcessor(
						presRuleCMPKey.getSubscriber(), presRuleCMPKey
								.getNotifier().getUri(), ruleset,
						documentSelector, sbb).getCombinedRule();
				combinedRules.put(entry.getKey(), newCombinedRule);
				// check permission changed
				if (oldCombinedRule.getSubHandling().getResponseCode() != newCombinedRule
						.getSubHandling().getResponseCode()) {
					sbb.getParentSbb().authorizationChanged(
							presRuleCMPKey.getSubscriber(),
							presRuleCMPKey.getNotifier(),
							subscriptionKey.getEventPackage(),
							subscriptionKey.getEventId(),
							newCombinedRule.getSubHandling().getResponseCode());
				}
				// TODO check if transformations changed need any further action
			}
		}
	}

	/**
	 * interface used by rules processor to get sphere for a notifier
	 */
	public String getSphere(String notifier,
			PresenceSubscriptionControlSbbInterface sbb) {

		// get ridden of notifier uri params, if any
		String notifierWithoutParams = notifier.split(";")[0];

		ComposedPublication composedPublication = sbb.getPublicationChildSbb()
				.getComposedPublication(notifierWithoutParams, "presence");
		if (composedPublication == null
				|| composedPublication.getDocumentAsDOM() == null) {
			return null;
		}

		String sphere = null;
		Element presence = composedPublication.getDocumentAsDOM()
				.getDocumentElement();
		NodeList presenceChilds = presence.getChildNodes();
		Node presenceChild = null;
		for (int i = 0; i < presenceChilds.getLength(); i++) {
			presenceChild = presenceChilds.item(i);
			if (DomUtils.isElementNamed(presenceChild, "person")) {
				// presence/person element
				NodeList personChilds = presenceChild.getChildNodes();
				Node personChild = null;
				for (int j = 0; j < personChilds.getLength(); j++) {
					personChild = personChilds.item(j);
					if (DomUtils.isElementNamed(personChild, "sphere")) {
						// presence/person/sphere element
						String tmpSphere = null;
						NodeList sphereChilds = personChild.getChildNodes();
						Node sphereChild = null;
						for (int l = 0; l < sphereChilds.getLength(); l++) {
							sphereChild = sphereChilds.item(l);
							if (sphereChild.getNodeType() == Node.ELEMENT_NODE) {
								if (tmpSphere == null) {
									tmpSphere = DomUtils
											.getElementName(sphereChild);
								} else {
									tmpSphere += " "
											+ DomUtils
													.getElementName(sphereChild);
								}
							}
						}
						if (sphere == null) {
							sphere = tmpSphere;
						} else {
							if (!sphere.equals(tmpSphere)) {
								// inconsistent sphere values
								return null;
							}
						}
					}
				}
			}
		}
		return sphere;
	}

	public NotifyContent getNotifyContent(Subscription subscription,
			PresenceSubscriptionControlSbbInterface sbb) {
		try {
			ComposedPublication composedPublication = sbb
					.getPublicationChildSbb().getComposedPublication(
							subscription.getNotifier().getUri(),
							subscription.getKey().getEventPackage());
			if (composedPublication != null
					&& composedPublication.getDocumentAsDOM() != null) {
				Object filteredContent = filterContentPerSubscriber(
						subscription, composedPublication.getDocumentAsDOM(),
						false, sbb);
				if (filteredContent != null) {
					return new NotifyContent(filteredContent, sbb
							.getHeaderFactory().createContentTypeHeader(
									composedPublication.getContentType(),
									composedPublication.getContentSubType()),
							null);
				}
			}
		} catch (Exception e) {
			logger.error("failed to get notify content", e);
		}
		return null;
	}

	public Object filterContentPerSubscriber(Subscription subscription,
			Object unmarshalledContent, 
			PresenceSubscriptionControlSbbInterface sbb) {
		return filterContentPerSubscriber(subscription, unmarshalledContent, true, sbb);
	}
	
	@SuppressWarnings("unchecked")
	private Object filterContentPerSubscriber(Subscription subscription,
			Object unmarshalledContent, boolean stateChange,
			PresenceSubscriptionControlSbbInterface sbb) {

		if (unmarshalledContent == null) {
			return null;
		}

		if (!(unmarshalledContent instanceof Document)) {
			logger.warn("request to filter notify content with an unexpected content of type: "
					+ unmarshalledContent.getClass());
			return unmarshalledContent;
		}

		// get rules for subscriptions on this sbb entity (sip dialog)
		HashMap<PresRuleCMPKey, OMAPresRule> combinedRules = sbb
				.getCombinedRules();
		if (combinedRules == null) {
			// no rules, return full content
			return unmarshalledContent;
		}
		// get rule for this specific subscription
		final OMAPresRule rule = combinedRules.get(new PresRuleCMPKey(
				subscription.getSubscriber(), subscription.getNotifier(),
				subscription.getKey()));
		if (rule == null) {
			// no rule for the subscription, return full content
			return unmarshalledContent;
		}

		if (rule.getSubHandling() == SubHandlingAction.allow) {
			// apply transformations
			try {
				return PRES_RULE_TRANSFORMER.transform(
						(Document) unmarshalledContent, rule);
			} catch (Exception e) {
				logger.error("failed to apply rule in content filtering for "
						+ subscription, e);
				return null;
			}
		} else {
			if (rule.getSubHandling() != SubHandlingAction.politeblock) {
				logger.warn("request to filter notify content with an unexpected sub-handling value: "
						+ rule.getSubHandling());
			}
			// TODO consider to create polite block notify content instead of no
			// content, as OMA specs say, for no state change notifications
			// emmartins: to me polite blocking is accepting subscription but
			// never show any state, thus no notify content
			return null;
		}

	}

	// --------- AUX

	/**
	 * from a user return the document selector pointing to it's pres-rules
	 * 
	 * @param user
	 * @return
	 */
	private DocumentSelector getDocumentSelector(String user,
			String presRulesAUID, String presRulesDocumentName) {
		return new DocumentSelector(new StringBuilder(presRulesAUID)
				.append("/users/").append(user).toString(),
				presRulesDocumentName);
	}

}
