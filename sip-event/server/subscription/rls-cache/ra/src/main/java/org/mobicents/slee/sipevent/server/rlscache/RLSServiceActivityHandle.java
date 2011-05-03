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

package org.mobicents.slee.sipevent.server.rlscache;

import javax.slee.resource.ActivityHandle;

public class RLSServiceActivityHandle implements ActivityHandle {

	public final static Class<?> TYPE = RLSServiceActivityHandle.class;
	
	private final String serviceURI;
	
	public RLSServiceActivityHandle(String serviceURI) {
		if (serviceURI == null) {
			throw new NullPointerException("null serviceURI");
		}
		this.serviceURI = serviceURI;
	}
	
	public String getServiceURI() {
		return serviceURI;
	}
	

	@Override
	public int hashCode() {
		return serviceURI.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RLSServiceActivityHandle other = (RLSServiceActivityHandle) obj;
		return this.serviceURI.equals(other.serviceURI);
	}
		
	@Override
	public String toString() {
		return new StringBuilder("RLSServicesActivityHandle[uri=").append(serviceURI).append("]").toString();
	}
}
