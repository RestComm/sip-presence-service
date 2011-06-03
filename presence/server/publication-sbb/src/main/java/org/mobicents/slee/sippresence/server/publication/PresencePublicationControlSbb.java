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

package org.mobicents.slee.sippresence.server.publication;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sip.address.URI;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.xml.validation.Schema;

import net.java.slee.resource.sip.SleeSipProvider;

import org.apache.log4j.Logger;
import org.mobicents.slee.ChildRelationExt;
import org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl;
import org.mobicents.slee.sipevent.server.publication.StateComposer;
import org.mobicents.slee.sipevent.server.publication.data.ComposedPublication;
import org.mobicents.slee.sipevent.server.publication.data.Publication;
import org.mobicents.slee.sipevent.server.subscription.NotifyContent;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionControlSbbLocalObject;
import org.mobicents.slee.sippresence.server.jmx.SipPresenceServerManagement;
import org.mobicents.xdm.common.util.dom.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Publication control implementation child sbb that transforms the sip event
 * framework in the PUBLISH interface of a SIP Presence Server.
 * 
 * @author eduardomartins
 * 
 */
public abstract class PresencePublicationControlSbb implements Sbb,
		ImplementedPublicationControl {

	private static Logger logger = Logger
			.getLogger(PresencePublicationControlSbb.class);
	private final static String[] eventPackages = { "presence" };
	
	/**
	 * SbbObject's context setting
	 */
	public void setSbbContext(SbbContext sbbContext) {
	}

	private HeaderFactory headerFactory;

	private HeaderFactory getHeaderFactory() throws NamingException {
		if (headerFactory == null) {
			headerFactory = ((SleeSipProvider) new InitialContext()
					.lookup("java:comp/env/slee/resources/jainsip/1.2/provider"))
					.getHeaderFactory();
		}
		return headerFactory;
	}

	public abstract ChildRelationExt getChildRelation();

	private SubscriptionControlSbbLocalObject getChildSbb() {
		ChildRelationExt childRelationExt = getChildRelation();
		SubscriptionControlSbbLocalObject childSbb = (SubscriptionControlSbbLocalObject) childRelationExt.get(ChildRelationExt.DEFAULT_CHILD_NAME);
		if (childSbb == null) {
			try {
				childSbb = (SubscriptionControlSbbLocalObject) childRelationExt
						.create(ChildRelationExt.DEFAULT_CHILD_NAME);
			} catch (Exception e) {
				logger.error("Failed to create child sbb", e);
				return null;
			}			
		}
		return childSbb;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl#getEventPackages()
	 */
	public String[] getEventPackages() {
		return eventPackages;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl#notifySubscribers(org.mobicents.slee.sipevent.server.publication.pojo.ComposedPublication)
	 */
	public void notifySubscribers(ComposedPublication composedPublication) {
		try {
			SubscriptionControlSbbLocalObject childSbb = getChildSbb();
			ContentTypeHeader contentTypeHeader = (composedPublication
					.getContentType() == null || composedPublication
					.getContentSubType() == null) ? null : getHeaderFactory()
					.createContentTypeHeader(
							composedPublication.getContentType(),
							composedPublication.getContentSubType());
			childSbb.notifySubscribers(composedPublication
					.getComposedPublicationKey().getEntity(),
					composedPublication.getComposedPublicationKey()
							.getEventPackage(), new NotifyContent(composedPublication
							.getDocumentAsDOM(), contentTypeHeader, null));
		} catch (Exception e) {
			logger.error("failed to notify subscribers for "
					+ composedPublication.getComposedPublicationKey(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl#authorizePublication(java.lang.String, org.w3c.dom.Document)
	 */
	public boolean authorizePublication(String requestEntity,
			Document unmarshalledContent) {
		// returns true if request uri matches entity (stripped from pres:
		// prefix if found) inside pidf doc
		Element pidfElement = unmarshalledContent.getDocumentElement();
		String entity = pidfElement.getAttribute("entity");
		if (entity != null) {
			if (entity.startsWith("pres:") && entity.length() > 5) {
				entity = entity.substring(5);
			}
			return entity.equals(requestEntity);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl#acceptsContentType(java.lang.String, javax.sip.header.ContentTypeHeader)
	 */
	public boolean acceptsContentType(String eventPackage,
			ContentTypeHeader contentTypeHeader) {
		// FIXME
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl#getAcceptsHeader(java.lang.String)
	 */
	public Header getAcceptsHeader(String eventPackage) {
		// FIXME
		if (eventPackage.equals("presence")) {
			try {
				return getHeaderFactory().createAcceptHeader("application",
						"pidf+xml");
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return null;
	}

	@Override
	public Schema getSchema() {
		return SipPresenceServerManagement.getInstance().getCombinedSchema();
	}
	
	private static final PresenceCompositionPolicy presenceCompositionPolicy = new PresenceCompositionPolicy();
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl#getStateComposer()
	 */
	@Override
	public StateComposer getStateComposer() {
		return presenceCompositionPolicy;
	}

	public Publication getAlternativeValueForExpiredPublication(
			Publication publication) {
		Document document = publication.getDocumentAsDOM();
		Element presence = document.getDocumentElement();
		NodeList presenceChilds = presence.getChildNodes();
		Node presenceChild = null;
		for(int i=0;i<presenceChilds.getLength();i++) {
			presenceChild = presenceChilds.item(i);
			if (DomUtils.isElementNamed(presenceChild, "tuple")) {
				// remove all child element nodes
				NodeList tupleChilds = presenceChild.getChildNodes();
				Node tupleChild = null;
				for (int j=0; j<tupleChilds.getLength(); j++) {
					tupleChild = tupleChilds.item(j);
					if (tupleChild.getNodeType() == Node.ELEMENT_NODE) {
						presenceChild.removeChild(tupleChild);
					}
				}
				// add a closed status element
				Element status = document.createElement("status");
				tupleChild.appendChild(status);
				Element basic = document.createElement("basic");
				basic.setTextContent("closed");
				status.appendChild(basic);				
			}
			else if (presenceChild.getNodeType() == Node.ELEMENT_NODE) {
				// remove any other child element
				presence.removeChild(presenceChild);
			}
		}
		publication.setDocumentAsString(null);
		return publication;
	}

	public boolean isResponsibleForResource(URI uri) {
		return true;
	}

	// ----------- SBB OBJECT's LIFE CYCLE

	public void sbbActivate() {
	}

	public void sbbCreate() throws CreateException {
	}

	public void sbbExceptionThrown(Exception arg0, Object arg1,
			ActivityContextInterface arg2) {
	}

	public void sbbLoad() {
	}

	public void sbbPassivate() {
	}

	public void sbbPostCreate() throws CreateException {
	}

	public void sbbRemove() {
	}

	public void sbbRolledBack(RolledBackContext arg0) {
	}

	public void sbbStore() {
	}

	public void unsetSbbContext() {
	}

}
