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

package org.openxdm.xcap.client.test.error;

import java.util.Map;

import org.openxdm.xcap.common.key.UserDocumentUriKey;
import org.openxdm.xcap.common.key.UserNamespaceBindingsUriKey;
import org.openxdm.xcap.common.uri.ElementSelector;

public class MethodNotAllowedTestFakeUserDocumentUriKey extends UserDocumentUriKey {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UserNamespaceBindingsUriKey nsKey;
	
	public MethodNotAllowedTestFakeUserDocumentUriKey(String auid, String user, String documentName, ElementSelector elementSelector, Map<String,String> namespaces) {
		super(auid, user, documentName);
		nsKey = new UserNamespaceBindingsUriKey(auid,user,documentName,elementSelector,namespaces);
	}

	@Override
	public String toString() {
		return nsKey.toString();
	}
}
