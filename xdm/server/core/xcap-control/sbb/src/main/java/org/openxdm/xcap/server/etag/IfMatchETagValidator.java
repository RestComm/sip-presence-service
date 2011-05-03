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

package org.openxdm.xcap.server.etag;

import org.openxdm.xcap.common.error.PreconditionFailedException;

public class IfMatchETagValidator implements ETagValidator {

	private String eTag;
	
	public IfMatchETagValidator(String eTag) {		
		this.eTag = eTag;
	}
	
	public void validate(String documentETag) throws PreconditionFailedException {
		if(eTag != null) {
			if(eTag.compareTo("*") == 0) {
				// matches anything except null
				if (documentETag == null) {
					throw new PreconditionFailedException();
				}
			}
			else {
				// etags must match
				if (documentETag == null || eTag.compareTo(documentETag) != 0) {
					throw new PreconditionFailedException();
				}
			}
		}
	}
	
}
