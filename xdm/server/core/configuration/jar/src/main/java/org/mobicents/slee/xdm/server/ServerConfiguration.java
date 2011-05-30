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

package org.mobicents.slee.xdm.server;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author martins
 *
 */
public class ServerConfiguration implements ServerConfigurationMBean {
	
	private String serverHost = System.getProperty("bind.address","127.0.0.1");
	
	private int serverPort = 8080;
	
	private String schemeAndAuthority = "http://"+serverHost+":"+serverPort;
	
	private String xcapRoot = "/mobicents/";
	
	private String fullXcapRoot;
	
	private String authenticationRealm;
	
	private boolean localXcapAuthentication;
	
	private boolean allowAssertedUserIDs;
	
	private Set<String> xcapDiffSuperUsers = new HashSet<String>(); 
		
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
		if (schemeAndAuthority != null && xcapRoot != null) {
			fullXcapRoot = new StringBuilder(schemeAndAuthority).append(xcapRoot).toString();
		}
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
		if (schemeAndAuthority != null && xcapRoot != null) {
			fullXcapRoot = new StringBuilder(schemeAndAuthority).append(xcapRoot).toString();
		}
	}
	
	@Override
	public String getFullXcapRoot() {
		if (fullXcapRoot == null && schemeAndAuthority != null && xcapRoot != null) {
			fullXcapRoot = new StringBuilder(schemeAndAuthority).append(xcapRoot).toString();
		}
		return fullXcapRoot;
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
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.ServerConfigurationMBean#getLocalXcapAuthentication()
	 */
	@Override
	public boolean getLocalXcapAuthentication() {
		return localXcapAuthentication;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.ServerConfigurationMBean#setLocalXcapAuthentication(boolean)
	 */
	@Override
	public void setLocalXcapAuthentication(boolean value) {
		this.localXcapAuthentication = value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.ServerConfigurationMBean#getAllowAssertedUserIDs()
	 */
	public boolean getAllowAssertedUserIDs() {
		return allowAssertedUserIDs;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.ServerConfigurationMBean#setAllowAssertedUserIDs(boolean)
	 */
	public void setAllowAssertedUserIDs(boolean allowAssertedUserIDs) {
		this.allowAssertedUserIDs = allowAssertedUserIDs;
	}
	
	@Override
	public Set<String> getXcapDiffSuperUsers() {
		return xcapDiffSuperUsers;
	}
	
	@Override
	public void setXcapDiffSuperUsers(Set<String> xcapDiffSuperUsers) {
		this.xcapDiffSuperUsers = xcapDiffSuperUsers;		
	}
}
