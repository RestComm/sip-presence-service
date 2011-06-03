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

public class RLSServiceActivityImpl implements RLSServiceActivity {

	public final static Class<?> TYPE = RLSServiceActivityImpl.class;
	
	private final String serviceURI;
	
	private RLSServicesCacheResourceAdaptor ra;
	
	private boolean ending;
	
	public RLSServiceActivityImpl(String serviceURI,RLSServicesCacheResourceAdaptor ra) {
		if (serviceURI == null) {
			throw new NullPointerException("null serviceURI");
		}
		this.serviceURI = serviceURI;
		this.ra = ra;
	}
	
	@Override
	public String getServiceURI() {
		return serviceURI;
	}
	
	@Override
	public RLSService getRLSService() {
		return ra.getDataSource().getRLSService(serviceURI);
	}
		
	public boolean isEnding() {
		return ending;
	}
	
	public void ending() {
		this.ending = true;
	}
	
	@Override
	public String toString() {
		return new StringBuilder("RLSServicesActivityImpl[uri=").append(serviceURI).append("]").toString();
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
		RLSServiceActivityImpl other = (RLSServiceActivityImpl) obj;
		return this.serviceURI.equals(other.serviceURI);
	}
	
	
}
