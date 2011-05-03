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

package org.openxdm.xcap.common.uri;

import org.openxdm.xcap.common.xml.XMLValidator;

/**
 * 
 * An element selector step is part of the element selector. The simplest form of a step doesn't have element position or attribute information, and it's defined in XCAP specs by the regular expression:
 * 
 * step = by-name/by-pos/by-attr/by-pos-attr/extension-selector
 * by-name = NameorAny
 * NameorAny = QName / "*"
 * 
 * 
 * @author Eduardo Martins
 *
 */
public class ElementSelectorStep {

	private String name;

	/**
	 * Creates a new step by the element's name. If the provided name is not '*' or a valid QName, an IllegalArgumentException is thrown.
	 * @param name the element's name.
	 */
	public ElementSelectorStep(String name) {
		if (name.equals("*") || XMLValidator.isQName(name) ) {
			this.name = name;
		} 
		else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Retrieves the element's name of this step.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves the prefix of this step element's name, if any.
	 * @return the prefix of this step element's name; null if it doesn't have a prefix.
	 */
	public String getPrefix() {
		// get index of :
		int i = name.indexOf(':');
		if (i < 0) {
			return null;
		} else {
			return name.substring(0, i);
		}
	}

	public String getNameWithoutPrefix() {
		// get index of :
		int i = name.indexOf(':');
		if (i < 0) {
			return name;
		} else {
			return name.substring(i+1);
		}
	}
	
	public String toString() {
		return name;
	}
}
