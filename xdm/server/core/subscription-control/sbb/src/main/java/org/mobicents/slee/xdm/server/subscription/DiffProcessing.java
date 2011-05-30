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

package org.mobicents.slee.xdm.server.subscription;

/**
 * @author baranowb
 * 
 */
public enum DiffProcessing {

	Aggregate("aggregate"), NoPatching("no-patching"), XcapPatching(
			"xcap-patching");

	/**
	 * string representing EventPackage header parameter with one of above
	 * values.
	 */
	public static final String PARAM = "diff-processing";

	private DiffProcessing(String t) {
		this.type = t;
	}

	private String type;

	public String toString() {
		return this.type;
	}

	public static DiffProcessing fromString(String type) {

		if (type == null) {
			return NoPatching; // default
		}

		if (type.equals(Aggregate.type)) {
			return Aggregate;
		}
		if (type.equals(NoPatching.type)) {
			return NoPatching;
		}
		if (type.equals(XcapPatching.type)) {
			return XcapPatching;
		}

		return NoPatching; // default or should it be null?

	}

}