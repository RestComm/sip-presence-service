package org.mobicents.slee.xdm.server;

/**
 * 
 * @author martins
 *
 */
public class ServerConfiguration implements ServerConfigurationMBean {
	
	private String serverHost = System.getProperty("bind.address","127.0.0.1");
	
	private int serverPort = 8080;
	
	private String schemeAndAuthority = "http://"+serverHost+":"+serverPort;
	
	private String xcapRoot = "/mobicents";
	
	private boolean dynamicUserProvision;
	
	private String authenticationRealm;
	
	private static final ServerConfiguration INSTANCE = new ServerConfiguration();
	
	public static ServerConfiguration getInstance() {
		return INSTANCE;
	}
	
	private ServerConfiguration() {
		
	}
	
	/**
	 * @return the serverHost
	 */
	public String getServerHost() {
		return serverHost;
	}

	/**
	 * @param serverHost the serverHost to set
	 */
	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	/**
	 * @return the serverPort
	 */
	public int getServerPort() {
		return serverPort;
	}

	/**
	 * @param serverPort the serverPort to set
	 */
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	/**
	 * @return the schemeAndAuthority
	 */
	public String getSchemeAndAuthority() {
		return schemeAndAuthority;
	}

	/**
	 * 
	 * @param schemeAndAuthority
	 */
	public void setSchemeAndAuthority(String schemeAndAuthority) {
		this.schemeAndAuthority = schemeAndAuthority;
	}

	/**
	 * @return the xcapRoot
	 */
	public String getXcapRoot() {
		return xcapRoot;
	}

	/**
	 * @param xcapRoot the xcapRoot to set
	 */
	public void setXcapRoot(String xcapRoot) {
		this.xcapRoot = xcapRoot;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.ServerConfigurationMBean#getDynamicUserProvision()
	 */
	public boolean getDynamicUserProvision() {
		return dynamicUserProvision;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.ServerConfigurationMBean#setDynamicUserProvision(boolean)
	 */
	public void setDynamicUserProvision(boolean value) {
		this.dynamicUserProvision = value;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.ServerConfigurationMBean#getAuthenticationRealm()
	 */
	public String getAuthenticationRealm() {
		if (authenticationRealm == null) {
			authenticationRealm = getServerHost();
		}
		return authenticationRealm;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.ServerConfigurationMBean#setAuthenticationRealm(java.lang.String)
	 */
	public void setAuthenticationRealm(String realm) {
		authenticationRealm = realm;
		if (authenticationRealm == null) {
			authenticationRealm = getServerHost();
		}
	}
}
