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

package org.mobicents.slee.sipevent.server.publication.jmx;

import org.mobicents.slee.sipevent.server.publication.data.PublicationControlDataSource;

public class PublicationControlManagement implements PublicationControlManagementMBean {
	
	private int defaultExpires = 3600;
	private int maxExpires = defaultExpires;
	private int minExpires = 60;
	private String contactAddressDisplayName = "Mobicents SIP Event Server";
	private String pChargingVectorHeaderTerminatingIOI = "mobicents.org"; 
	private boolean useAlternativeValueForExpiredPublication;
	private PublicationControlDataSource dataSource;
	
	private static final PublicationControlManagement INSTANCE = new PublicationControlManagement();
	
	public static PublicationControlManagement getInstance() {
		return INSTANCE;
	}
	
	private PublicationControlManagement() {		
	}
	
	public int getDefaultExpires() {
		return defaultExpires;
	}

	public void setDefaultExpires(int defaultExpires) {
		this.defaultExpires = defaultExpires;
	}

	public int getMaxExpires() {
		return maxExpires;
	}

	public void setMaxExpires(int maxExpires) {
		this.maxExpires = maxExpires;
	}

	public int getMinExpires() {
		return minExpires;
	}

	public void setMinExpires(int minExpires) {
		this.minExpires = minExpires;
	}

	public String getContactAddressDisplayName() {
		return contactAddressDisplayName;
	}

	public void setContactAddressDisplayName(String contactAddressDisplayName) {
		this.contactAddressDisplayName = contactAddressDisplayName;
	}
	
	public String getPChargingVectorHeaderTerminatingIOI() {
		return pChargingVectorHeaderTerminatingIOI;
	}
	
	public void setPChargingVectorHeaderTerminatingIOI(
			String pChargingVectorHeaderTerminatingIOI) {
		this.pChargingVectorHeaderTerminatingIOI = pChargingVectorHeaderTerminatingIOI;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.jmx.PublicationControlManagementMBean#setUseAlternativeValueForExpiredPublication(boolean)
	 */
	@Override
	public void setUseAlternativeValueForExpiredPublication(
			boolean value) {
		this.useAlternativeValueForExpiredPublication = value;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.jmx.PublicationControlManagementMBean#isUseAlternativeValueForExpiredPublication()
	 */
	@Override
	public boolean isUseAlternativeValueForExpiredPublication() {
		return useAlternativeValueForExpiredPublication;
	}

	public void setDataSource(PublicationControlDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public PublicationControlDataSource getDataSource() {
		return dataSource;
	}
	
}
