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

/**
 * 
 * An element selector step is part of the element selector. This step extends
 * ElementSelectorStep by containing positional information, that is used to
 * select an element from a xml document, and it's defined in XCAP specs by the
 * regular expression:
 * 
 * by-pos = NameorAny "[" position "]" NameorAny = QName / "*" position =
 * 1*DIGIT
 * 
 * @author Eduardo Martins
 * 
 */
public class ElementSelectorStepByPos extends ElementSelectorStep {

	private int pos;

	/**
	 * Creates a new step from the specified element name and position. Besides
	 * possible limitations defined by ElementSelectorStep, this constructor
	 * throws IllegalArgumentException if the provided position is not > 0.
	 * 
	 * @param name
	 * @param pos
	 */
	public ElementSelectorStepByPos(String name, int pos) {
		super(name);

		if (pos < 0) {
			throw new IllegalArgumentException(
					"pos must be non negative number.");
		} else {
			this.pos = pos;
		}
	}

	/**
	 * Retreives the element position of this step.
	 * 
	 * @return
	 */
	public int getPos() {
		return pos;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName()).append('[').append(getPos()).append(']');
		return sb.toString();
	}
}
