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

/**
 * @author martins
 * 
 */
public interface PublicationClientControl {

	/**
	 * Creates a new publication for the specified Entity and SIP Event Package.
	 * 
	 * @param entity
	 * @param eventPackage
	 * @param document
	 * @param contentType
	 * @param contentSubType
	 * @param expires
	 *            the time in seconds, which the publication is valid
	 * @param callback
	 * @return
	 */
	public Result newPublication(String entity, String eventPackage,
			String document, String contentType, String contentSubType,
			int expires);

	/**
	 * Refreshes the publication identified by the specified Entity, SIP Event
	 * Package and ETag.
	 * 
	 * @param entity
	 * @param eventPackage
	 * @param eTag
	 * @param expires
	 *            the time in seconds, which the publication is valid
	 * @param callback
	 * @return
	 */
	public Result refreshPublication(String entity, String eventPackage,
			String eTag, int expires);

	/**
	 * Modifies the publication identified by the specified Entity, SIP Event
	 * Package and ETag.
	 * 
	 * @param entity
	 * @param eventPackage
	 * @param eTag
	 * @param document
	 * @param contentType
	 * @param contentSubType
	 * @param expires
	 *            the time in seconds, which the publication is valid
	 * @param callback
	 * @return
	 */
	public Result modifyPublication(String entity, String eventPackage,
			String eTag, String document, String contentType,
			String contentSubType, int expires);

	/**
	 * Removes the publication identified by the specified Entity, SIP Event
	 * Package and ETag.
	 * 
	 * @param entity
	 * @param eventPackage
	 * @param eTag
	 * @param callback
	 * @return status code for the response
	 */
	public int removePublication(String entity, String eventPackage, String eTag);

}
