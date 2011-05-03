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

package org.mobicents.slee.sipevent.server.subscription.eventlist;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple multipart/related string constructor. Doesn't validate references among body part contents.
 * @author martins
 *
 */
public class MultiPart {

	public static final String MULTIPART_CONTENT_TYPE = "multipart";
	public static final String MULTIPART_CONTENT_SUBTYPE = "related";
	
	private final String boundary;
	private final String type;
	private final List<BodyPart> bodyParts; 
	
	public MultiPart(String boundary, String type) {
		this.boundary = boundary;
		this.type = type;
		this.bodyParts = new ArrayList<BodyPart>();
	}
	
	public List<BodyPart> getBodyParts() {
		return bodyParts;
	}
	
	public String getBoundary() {
		return boundary;
	}
	
	public String getType() {
		return type;
	}
	
	public int hashCode() {
		return this.boundary.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			MultiPart other = (MultiPart) obj;
			return other.boundary.equals(this.boundary);
		}
		else {
			return false;
		}
	}
	
	public String toString() {
		
		if (!bodyParts.isEmpty()) {
			final String b = "--"+boundary;
			String result = b;
			for (BodyPart bodyPart : bodyParts) {
				result += "\n"+bodyPart+b;
			}
			result +="--";
			return result;
		}
		else {
			return null;
		}
	}
}
