package org.mobicents.slee.sipevent.server.publication.jmx;

public class PublicationControlManagement implements PublicationControlManagementMBean {
	
	private int defaultExpires = 3600;
	private int maxExpires = defaultExpires;
	private int minExpires = 60;
	private String contactAddressDisplayName = "Mobicents SIP Event Server";
	private String pChargingVectorHeaderTerminatingIOI = "mobicents.org"; 

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
}
