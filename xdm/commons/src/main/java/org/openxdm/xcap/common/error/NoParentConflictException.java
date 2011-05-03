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

package org.openxdm.xcap.common.error;

public class NoParentConflictException extends ConflictException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String conflictError = null;
	private String existingAncestor = null;
	private String queryComponent = null;
	private String schemeAndAuthorityURI = null;
			
	public NoParentConflictException(String existingAncestor) {
		if (existingAncestor == null) {
			throw new IllegalArgumentException("existing ancestor must not be null");
		}
		this.existingAncestor = existingAncestor;
	}
	
	public void setQueryComponent(String queryComponent) {
		this.queryComponent = queryComponent;
	}
	
	public void setSchemeAndAuthorityURI(String schemeAndAuthorityURI) {		
		this.schemeAndAuthorityURI = schemeAndAuthorityURI;
	}
	
	protected String getConflictError() {
		if (conflictError == null) {
			if (schemeAndAuthorityURI != null) {
				StringBuilder sb = new StringBuilder("<no-parent><ancestor>").append(schemeAndAuthorityURI);
				if (existingAncestor != "") {
					sb.append(existingAncestor);
				}
				if (queryComponent != null) {
					sb.append('?').append(queryComponent);
				}
				sb.append("</ancestor></no-parent>");
				conflictError = sb.toString();				
			}
			else{
				return "<parent />";
			}			
		}
		return conflictError;
	}

}
