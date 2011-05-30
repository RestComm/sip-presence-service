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

package org.mobicents.xdm.server.appusage;

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
import org.openxdm.xcap.common.uri.NodeSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Allows app usages to make requests that affects other documents stored in the
 * XDM Server.
 * 
 * @author martins
 * 
 */
public interface AppUsageRequestProcessor {

	public boolean putDocument(DocumentSelector documentSelector,
			Document document, AppUsage appUsage)
			throws InternalServerErrorException, NoParentConflictException,
			SchemaValidationErrorConflictException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException, ConflictException,
			MethodNotAllowedException, UnsupportedMediaTypeException,
			PreconditionFailedException, BadRequestException,
			NotAuthorizedRequestException;

	public boolean putElement(DocumentSelector documentSelector,
			NodeSelector nodeSelector, ElementSelector elementSelector,
			Element element, AppUsage appUsage)
			throws InternalServerErrorException, NoParentConflictException,
			SchemaValidationErrorConflictException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException, CannotInsertConflictException,
			NotValidXMLFragmentConflictException, NotUTF8ConflictException,
			BadRequestException, NotAuthorizedRequestException;

	public boolean putAttribute(DocumentSelector documentSelector,
			NodeSelector nodeSelector, ElementSelector elementSelector,
			AttributeSelector attributeSelector, String attrValue,
			AppUsage appUsage) throws InternalServerErrorException,
			NoParentConflictException, SchemaValidationErrorConflictException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException,
			NotXMLAttributeValueConflictException, BadRequestException,
			CannotInsertConflictException, NotAuthorizedRequestException;

	public void deleteDocument(DocumentSelector documentSelector,
			AppUsage appUsage) throws InternalServerErrorException,
			NotFoundException, SchemaValidationErrorConflictException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException;

	public void deleteElement(DocumentSelector documentSelector,
			NodeSelector nodeSelector, ElementSelector elementSelector,
			AppUsage appUsage) throws InternalServerErrorException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException, NotFoundException,
			CannotDeleteConflictException,
			SchemaValidationErrorConflictException, BadRequestException;

	public void deleteAttribute(DocumentSelector documentSelector,
			NodeSelector nodeSelector, ElementSelector elementSelector,
			AttributeSelector attributeSelector, AppUsage appUsage)
			throws InternalServerErrorException, BadRequestException,
			NotFoundException, SchemaValidationErrorConflictException,
			UniquenessFailureConflictException,
			ConstraintFailureConflictException;

}
