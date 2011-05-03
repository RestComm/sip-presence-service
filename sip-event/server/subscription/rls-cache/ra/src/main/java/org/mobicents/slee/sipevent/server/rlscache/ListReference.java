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

public class ListReference {

	public enum Status { RESOLVING, BAD_GATEWAY, OK }
	
	private final ListReferenceFrom from;
	
	private final ListReferenceFrom to;
	
	public ListReference(ListReferenceFrom from, ListReferenceFrom to) {
		this.from = from;
		this.to = to;
	}
	
	public ListReferenceFrom getFrom() {
		return from;
	}
	
	public ListReferenceFrom getTo() {
		return to;
	}
	
	@Override
	public int hashCode() {
		return from.hashCode() * 31 + to.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListReference other = (ListReference) obj;
		if (!from.equals(other.from))
			return false;
		if (!to.equals(other.to))
			return false; 
		return true;
	}
		
}
