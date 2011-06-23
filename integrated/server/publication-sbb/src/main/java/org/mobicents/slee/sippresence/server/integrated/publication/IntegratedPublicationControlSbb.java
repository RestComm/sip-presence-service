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

package org.mobicents.slee.sippresence.server.integrated.publication;

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
import org.mobicents.slee.sipevent.server.publication.StateComposer;
import org.mobicents.slee.sipevent.server.publication.data.ComposedPublication;
import org.mobicents.slee.sipevent.server.publication.data.Publication;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionControl;
import org.mobicents.slee.sippresence.server.publication.PresencePublicationControl;
import org.w3c.dom.Document;

/**
 * Implemented Publication control child sbb for an integrated server, that is,
 * accepting publications of several event packages. At this moment this service
 * includes only the presence event package.
 * 
 * @author eduardomartins
 * 
 */
public abstract class IntegratedPublicationControlSbb implements Sbb,
		IntegratedPublicationControlSbbInterface {

	private static Logger logger = Logger
			.getLogger(IntegratedPublicationControlSbb.class);

	private final static PresencePublicationControl PRESENCE_PUBLICATION_CONTROL = new PresencePublicationControl();

	/**
	 * SbbObject's context setting
	 */
	public void setSbbContext(SbbContext sbbContext) {
	}

	private HeaderFactory headerFactory;

	public HeaderFactory getHeaderFactory() throws NamingException {
		if (headerFactory == null) {
			headerFactory = ((SleeSipProvider) new InitialContext()
					.lookup("java:comp/env/slee/resources/jainsip/1.2/provider"))
					.getHeaderFactory();
		}
		return headerFactory;
	}

	public abstract ChildRelationExt getPresenceSubscriptionControlChildRelation();

	@Override
	public SubscriptionControl getPresenceSubscriptionControl() {
		ChildRelationExt childRelationExt = getPresenceSubscriptionControlChildRelation();
		SubscriptionControl childSbb = (SubscriptionControl) childRelationExt
				.get(ChildRelationExt.DEFAULT_CHILD_NAME);
		if (childSbb == null) {
			try {
				childSbb = (SubscriptionControl) childRelationExt
						.create(ChildRelationExt.DEFAULT_CHILD_NAME);
			} catch (Exception e) {
				logger.error("Failed to create child sbb", e);
				return null;
			}
		}
		return childSbb;
	}

	@Override
	public String[] getEventPackages() {
		return PRESENCE_PUBLICATION_CONTROL.getEventPackages();
	}

	@Override
	public void notifySubscribers(ComposedPublication composedPublication) {
		PRESENCE_PUBLICATION_CONTROL.notifySubscribers(composedPublication,
				this);
	}

	@Override
	public boolean authorizePublication(String entity, String eventPackage,
			Document content) {
		return PRESENCE_PUBLICATION_CONTROL.authorizePublication(entity,
				content);
	}

	@Override
	public boolean acceptsContentType(String eventPackage,
			ContentTypeHeader contentTypeHeader) {
		return PRESENCE_PUBLICATION_CONTROL.acceptsContentType(eventPackage,
				contentTypeHeader);
	}

	@Override
	public Header getAcceptsHeader(String eventPackage) {
		return PRESENCE_PUBLICATION_CONTROL
				.getAcceptsHeader(eventPackage, this);
	}

	@Override
	public Schema getSchema(String eventPackage) {
		return PRESENCE_PUBLICATION_CONTROL.getSchema();
	}

	@Override
	public StateComposer getStateComposer(String eventPackage) {
		return PRESENCE_PUBLICATION_CONTROL.getStateComposer();
	}

	@Override
	public Publication getAlternativeValueForExpiredPublication(
			Publication publication) {
		return PRESENCE_PUBLICATION_CONTROL
				.getAlternativeValueForExpiredPublication(publication);
	}

	@Override
	public boolean isResponsibleForResource(URI uri, String eventPackage) {
		return PRESENCE_PUBLICATION_CONTROL.isResponsibleForResource(uri);
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