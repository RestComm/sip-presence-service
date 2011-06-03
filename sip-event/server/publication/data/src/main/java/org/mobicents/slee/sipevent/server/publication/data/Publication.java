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

package org.mobicents.slee.sipevent.server.publication.data;

import java.io.Serializable;

/**
 *  
 *     This class is JPA pojo for a publication of sip events.
 *     
 * @author eduardomartins
 *
 */
public class Publication extends PublicationContent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8020033417766370446L;

	
	/**
	 * the publication key
	 */   
	private final PublicationKey publicationKey;
	
	/**
	 * the id of the SLEE timer associated with this subscription
	 */
	private Serializable timerID;
	
	public Publication(PublicationKey publicationKey, String document, String contentType, String contentSubType) {
		super(document,contentType,contentSubType);
		this.publicationKey = publicationKey;
	}

	// -- GETTERS AND SETTERS
	
	public PublicationKey getPublicationKey() {
		return publicationKey;
	}

	public Serializable getTimerID() {
		return timerID;
	}

	public void setTimerID(Serializable timerID) {
		this.timerID = timerID;
	}
	
	@Override
	public String toString() {
		return new StringBuilder("publication: key=").append(publicationKey).toString();
	}
	
}
