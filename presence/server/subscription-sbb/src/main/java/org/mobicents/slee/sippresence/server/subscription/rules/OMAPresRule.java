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

package org.mobicents.slee.sippresence.server.subscription.rules;

import java.util.HashSet;
import java.util.Set;

import org.openxdm.xcap.common.uri.DocumentSelector;

public class OMAPresRule extends PresRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = 865547623453835201L;
	
	private boolean provideBarringState;
	private GeoPrivTransformation provideGeopriv = GeoPrivTransformation.FALSE;
	private boolean provideNetworkAvailability;
	private boolean provideRegistrationState;
	private Set<String> serviceIDs;
	private boolean provideSessionParticipation;
	private boolean provideWillingness;
	
	public OMAPresRule(DocumentSelector documentSelector) {
		super(documentSelector);
	}
	
	/**
	 * combines this OMA pres rule with another.
	 * @param other
	 */
	public void combine(OMAPresRule other) {
		super.combine(other);
		this.provideBarringState = this.provideBarringState || other.provideBarringState;
		if (this.provideGeopriv.getValue() < other.provideGeopriv.getValue()) {
			this.provideGeopriv = other.provideGeopriv;
		}
		this.provideNetworkAvailability = this.provideNetworkAvailability || other.provideNetworkAvailability;
		this.provideRegistrationState = this.provideRegistrationState || other.provideRegistrationState;
		if (other.serviceIDs != null) {
			if (this.serviceIDs == null) {
				this.serviceIDs = new HashSet<String>();
			}
			serviceIDs.addAll(other.serviceIDs);
		}
		this.provideSessionParticipation = this.provideSessionParticipation || other.provideSessionParticipation;
		this.provideWillingness = this.provideWillingness || other.provideWillingness;
	}

	public boolean isProvideBarringState() {
		return provideBarringState;
	}

	public void setProvideBarringState(boolean provideBarringState) {
		this.provideBarringState = provideBarringState;
	}

	public GeoPrivTransformation getProvideGeopriv() {
		return provideGeopriv;
	}

	public void setProvideGeopriv(GeoPrivTransformation provideGeopriv) {
		this.provideGeopriv = provideGeopriv;
	}

	public boolean isProvideNetworkAvailability() {
		return provideNetworkAvailability;
	}

	public void setProvideNetworkAvailability(boolean provideNetworkAvailability) {
		this.provideNetworkAvailability = provideNetworkAvailability;
	}

	public boolean isProvideRegistrationState() {
		return provideRegistrationState;
	}

	public void setProvideRegistrationState(boolean provideRegistrationState) {
		this.provideRegistrationState = provideRegistrationState;
	}

	public boolean isProvideSessionParticipation() {
		return provideSessionParticipation;
	}

	public void setProvideSessionParticipation(boolean provideSessionParticipation) {
		this.provideSessionParticipation = provideSessionParticipation;
	}

	public boolean isProvideWillingness() {
		return provideWillingness;
	}

	public void setProvideWillingness(boolean provideWillingness) {
		this.provideWillingness = provideWillingness;
	}

	public Set<String> getServiceIDs() {		
		return serviceIDs;
	}
	
	public void setServiceIDs(Set<String> serviceIDs) {
		this.serviceIDs = serviceIDs;
	}
	
}
