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
package org.openxdm.xcap.server.slee;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openxdm.xcap.common.error.InternalServerErrorException;

/**
 * 
 * @author aayush.bhatnagar
 * @author martins
 * 
 *         This SBB is used to handle all the authentication related logic for
 *         incoming XCAP requests and validate the challenge responses.
 * 
 */
public interface AuthenticationProxy {

	/**
	 * Handles authentication of the XCAP request.
	 * 
	 * @param request
	 * @param response
	 * @return the authenticated user, null if authentication failed or is not
	 *         complete, the request processing should be canceled since a
	 *         response was already sent
	 * @throws InternalServerErrorException
	 */
	public String authenticate(HttpServletRequest request,
			HttpServletResponse response) throws InternalServerErrorException;
	
}
