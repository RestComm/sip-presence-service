/**
 * 
 */
package org.mobicents.slee.sippresence.server.jmx;

/**
 * @author martins
 *
 */
public class SipPresenceServerManagement implements SipPresenceServerManagementMBean {

	private String presRulesAUID;
	private String presRulesDocumentName;
	private String jaxbPackageNames;
	
	private static SipPresenceServerManagement INSTANCE = new SipPresenceServerManagement();
	
	/**
	 * Retrieves the singleton.
	 * @return
	 */
	public static SipPresenceServerManagement getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 
	 */
	private SipPresenceServerManagement() {}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.server.jmx.SipPresenceServerManagementMBean#getPresRulesAUID()
	 */
	@Override
	public String getPresRulesAUID() {
		return presRulesAUID;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.server.jmx.SipPresenceServerManagementMBean#getPresRulesDocumentName()
	 */
	@Override
	public String getPresRulesDocumentName() {
		return presRulesDocumentName;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.server.jmx.SipPresenceServerManagementMBean#setPresRulesAUID(java.lang.String)
	 */
	@Override
	public void setPresRulesAUID(String auid) {
		this.presRulesAUID = auid;		
	}

	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.server.jmx.SipPresenceServerManagementMBean#setPresRulesDocumentName(java.lang.String)
	 */
	@Override
	public void setPresRulesDocumentName(String documentName) {
		this.presRulesDocumentName = documentName;
	}

	/**
	 * Retrieves the package names for jaxb pojos, to be used when (un)marshalling presence content.
	 * @return
	 */
	public String getJaxbPackageNames() {
		return jaxbPackageNames;
	}

	/**
	 * Sets the package names (separated by ':' char) for jaxb pojos, to be used when (un)marshalling
	 * presence content. All whitespaces will be removed.
	 * 
	 * @param packageNames
	 */
	public void setJaxbPackageNames(String packageNames) {
		this.jaxbPackageNames = packageNames.replaceAll("\\s","");
	}
}
