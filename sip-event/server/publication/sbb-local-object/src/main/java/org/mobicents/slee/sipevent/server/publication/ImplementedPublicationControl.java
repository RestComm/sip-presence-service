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

/**
 * 
 */
package org.mobicents.slee.sipevent.server.publication;

import javax.sip.address.URI;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;
import javax.xml.validation.Schema;

import org.mobicents.slee.sipevent.server.publication.data.ComposedPublication;
import org.mobicents.slee.sipevent.server.publication.data.Publication;
import org.w3c.dom.Document;

/**
 * @author martins
 *
 */
public interface ImplementedPublicationControl {

	/**
	 * the impl class SIP event packages supported
	 * 
	 * @return
	 */
	public String[] getEventPackages();

	/**
	 * Verifies if the specified content type header can be accepted for the
	 * specified event package.
	 * 
	 * @param eventPackage
	 * @param contentTypeHeader
	 * @return
	 */
	public boolean acceptsContentType(String eventPackage,
			ContentTypeHeader contentTypeHeader);

	/**
	 * Retrieves the accepted content types for the specified event package.
	 * 
	 * @param eventPackage
	 * @return
	 */
	public Header getAcceptsHeader(String eventPackage);

	/**
	 * Notifies subscribers about a publication update for the specified entity
	 * regarding the specified event package.
	 * 
	 * @param composedPublication
	 */
	public void notifySubscribers(ComposedPublication composedPublication);

	/**
	 * Retrieves the schema needed to validate a publication content.
	 * 
	 * @return
	 */
	public Schema getSchema(String eventPackage);

	/**
	 * Retrieves the {@link StateComposer} concrete impl, used to combine publications.
	 * 
	 * @return 
	 */
	public StateComposer getStateComposer(String eventPackage);

	/**
	 * Checks if this server is responsible for the resource publishing state.
	 * 
	 */
	public boolean isResponsibleForResource(URI uri,String eventPackage);

	/**
	 * verifies if entity is authorized to publish the content
	 * 
	 * @param entity
	 * @param content
	 * @return
	 */
	public boolean authorizePublication(String entity,String eventPackage,
			Document content);

	/**
	 * 
	 * Through this method the event package implementation sbb has a chance to
	 * define an alternative publication value for the one expired, this can
	 * allow a behavior such as defining offline status in a presence resource.
	 * 
	 * @param publication
	 * @return
	 */
	public Publication getAlternativeValueForExpiredPublication(
			Publication publication);
	
}
