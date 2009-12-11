/**
 * 
 */
package org.mobicents.slee.sippresence.server.jmx;

/**
 * Management of the SIP Presence Server configuration.
 * 
 * @author martins
 *
 */
public interface SipPresenceServerManagementMBean {

	public static final String MBEAN_NAME = "org.mobicents.slee:sippresence=SipPresenceServerManagement";
	
	/**
	 * Retrieves the id of the XCAP App Usage to be used to retrieve notifier's pres-rules docs, from the XDMS.
	 * @return
	 */
	public String getPresRulesAUID();
	
	/**
	 * Sets the id of the XCAP App Usage to be used to retrieve notifier's pres-rules docs, from the XDMS.
	 * @param auid
	 */
	public void setPresRulesAUID(String auid);
	
	/**
	 * Retrieves the name of the document to be used to retrieve the notifier's pres rules doc, from the XDMS. 
	 * @return
	 */
	public String getPresRulesDocumentName();
	
	/**
	 * Sets the name of the document to be used to retrieve the notifier's pres rules doc, from the XDMS.
	 * @param documentName
	 */
	public void setPresRulesDocumentName(String documentName);
	
}
