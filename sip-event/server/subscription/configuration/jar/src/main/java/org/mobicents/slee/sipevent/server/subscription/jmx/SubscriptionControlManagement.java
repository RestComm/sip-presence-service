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

package org.mobicents.slee.sipevent.server.subscription.jmx;

import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionControlDataSource;

public class SubscriptionControlManagement implements
		SubscriptionControlManagementMBean {

	private int defaultExpires = 3600;
	private int maxExpires = defaultExpires;
	private int minExpires = 60;
	private int defaultWaitingExpires = (24 * 60 * 60);
	private int maxForwards = 70;
	private String contactAddressDisplayName = "Mobicents SIP Event Server";
	private boolean eventListSupportOn = true;
	private String pChargingVectorHeaderTerminatingIOI = "mobicents.org";
	private SubscriptionControlDataSource dataSource;
	
	private static final SubscriptionControlManagement INSTANCE = new SubscriptionControlManagement();
	
	public static SubscriptionControlManagement getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 
	 */
	private SubscriptionControlManagement() {
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean#getDefaultExpires()
	 */
	public int getDefaultExpires() {
		return defaultExpires;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean#setDefaultExpires(int)
	 */
	public void setDefaultExpires(int defaultExpires) {
		this.defaultExpires = defaultExpires;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean#getMaxExpires()
	 */
	public int getMaxExpires() {
		return maxExpires;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean#setMaxExpires(int)
	 */
	public void setMaxExpires(int maxExpires) {
		this.maxExpires = maxExpires;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean#getMinExpires()
	 */
	public int getMinExpires() {
		return minExpires;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean#setMinExpires(int)
	 */
	public void setMinExpires(int minExpires) {
		this.minExpires = minExpires;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean#getDefaultWaitingExpires()
	 */
	public int getDefaultWaitingExpires() {
		return defaultWaitingExpires;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean#setDefaultWaitingExpires(int)
	 */
	public void setDefaultWaitingExpires(int defaultWaitingExpires) {
		this.defaultWaitingExpires = defaultWaitingExpires;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean#getMaxForwards()
	 */
	public int getMaxForwards() {
		return maxForwards;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean#setMaxForwards(int)
	 */
	public void setMaxForwards(int maxForwards) {
		this.maxForwards = maxForwards;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean#getContactAddressDisplayName()
	 */
	public String getContactAddressDisplayName() {
		return contactAddressDisplayName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean
	 * #setContactAddressDisplayName(java.lang.String)
	 */
	public void setContactAddressDisplayName(String contactAddressDisplayName) {
		this.contactAddressDisplayName = contactAddressDisplayName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean#getEventListSupportOn()
	 */
	public boolean getEventListSupportOn() {
		return eventListSupportOn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean#setEventListSupportOn(boolean)
	 */
	public void setEventListSupportOn(boolean eventListSupportOn) {
		this.eventListSupportOn = eventListSupportOn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean
	 * #getPChargingVectorHeaderTerminatingIOI()
	 */
	public String getPChargingVectorHeaderTerminatingIOI() {
		return pChargingVectorHeaderTerminatingIOI;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.subscription.jmx.
	 * SubscriptionControlManagementMBean
	 * #setPChargingVectorHeaderTerminatingIOI(java.lang.String)
	 */
	public void setPChargingVectorHeaderTerminatingIOI(
			String pChargingVectorHeaderTerminatingIOI) {
		this.pChargingVectorHeaderTerminatingIOI = pChargingVectorHeaderTerminatingIOI;
	}
	
	public void setDataSource(SubscriptionControlDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public SubscriptionControlDataSource getDataSource() {
		return dataSource;
	}
}
