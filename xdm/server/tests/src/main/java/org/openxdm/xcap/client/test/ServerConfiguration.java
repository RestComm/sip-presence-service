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

package org.openxdm.xcap.client.test;

import java.util.Properties;

import org.openxdm.xcap.client.XCAPClient;
import org.openxdm.xcap.client.XCAPClientImpl;

public class ServerConfiguration {

	private static Properties properties = new Properties();
	
	public static String SERVER_HOST;
	public static int SERVER_PORT;
	public static String SERVER_XCAP_ROOT;
	
	static {
		ServerConfiguration serverConfiguration = new ServerConfiguration();
		try {
			properties.load(serverConfiguration.getClass().getResourceAsStream("configuration.properties"));
			SERVER_HOST = getServerHost();
			SERVER_PORT = getServerPort();
			SERVER_XCAP_ROOT = getServerXcapRoot();
		} catch (Exception e) {
			SERVER_HOST = "127.0.0.1";
			SERVER_PORT = 8080;
			SERVER_XCAP_ROOT = "/mobicents";
		}
		
	}
	
	private static String getServerHost() {
		return properties.getProperty("SERVER_HOST", "127.0.0.1");
	}
	
	private static int getServerPort() {
		int serverPort = 8080;
		String serverPortProperty = properties.getProperty("SERVER_PORT");
		if (serverPortProperty != null) {
			try {
				serverPort = Integer.parseInt(serverPortProperty);
			}
			catch (Exception e) {
				// ignore
			}
		}
		return serverPort;
	}

	private static String getServerXcapRoot() {
		return properties.getProperty("SERVER_XCAP_ROOT", "/mobicents");
	}

	public static XCAPClient getXCAPClientInstance() throws InterruptedException {
		return new XCAPClientImpl(SERVER_HOST,SERVER_PORT,SERVER_XCAP_ROOT);
	}
	
}
