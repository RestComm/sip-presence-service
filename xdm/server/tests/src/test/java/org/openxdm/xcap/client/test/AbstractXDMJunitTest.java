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

import java.io.IOException;
import java.util.Hashtable;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.junit.After;
import org.junit.Before;
import org.mobicents.slee.enabler.userprofile.jpa.jmx.UserProfileControlManagementMBean;
import org.mobicents.xcap.client.XcapClient;
import org.mobicents.xcap.client.XcapConstant;
import org.mobicents.xcap.client.header.Header;
import org.mobicents.xcap.client.header.HeaderFactory;
import org.mobicents.xcap.client.impl.XcapClientImpl;
import org.mobicents.xdm.server.appusage.AppUsage;
import org.openxdm.xcap.server.slee.appusage.resourcelists.ResourceListsAppUsage;

public abstract class AbstractXDMJunitTest {

	protected XcapClient client;
	protected AppUsage appUsage = new ResourceListsAppUsage(null,false);
	protected String user = "sip:user@mobicents.org";
	protected String documentName = "index";
	
	private ObjectName userProfileMBeanObjectName;
	private RMIAdaptor rmiAdaptor;
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initRmiAdaptor() throws NamingException, MalformedObjectNameException, NullPointerException {
		// Set Some JNDI Properties
		Hashtable env = new Hashtable();
		env.put(Context.PROVIDER_URL, "jnp://"+ServerConfiguration.SERVER_HOST+":1099");
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		env.put(Context.URL_PKG_PREFIXES, "org.jnp.interfaces");

		InitialContext ctx = new InitialContext(env);
		rmiAdaptor = (RMIAdaptor) ctx.lookup("jmx/rmi/RMIAdaptor");
		userProfileMBeanObjectName = new ObjectName(UserProfileControlManagementMBean.MBEAN_NAME);
	}
	
	protected void createUser(String user, String password) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		String sigs[] = { String.class.getName(), String.class.getName() };
		Object[] args = { user, password };
		rmiAdaptor.invoke(userProfileMBeanObjectName, "addUser", args, sigs);	
	}
	
	protected void removeUser(String user) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		String sigs[] = { String.class.getName()};
		Object[] args = { user};
		rmiAdaptor.invoke(userProfileMBeanObjectName, "removeUser", args, sigs);	
	}
	
	@Before
	public void runBefore() throws IOException, InterruptedException, MalformedObjectNameException, NullPointerException, NamingException, InstanceNotFoundException, MBeanException, ReflectionException {
		client = new XcapClientImpl();
		initRmiAdaptor();
		createUser(user, user);
	}
	
	@After
	public void runAfter() throws IOException, InstanceNotFoundException, MBeanException, ReflectionException {
		if (client != null) {
			client.shutdown();
			client = null;
		}		
		removeUser(user);
		appUsage = null;
		user = null;
		documentName = null;
	}
	
	protected Header[] getAssertedUserIdHeaders(HeaderFactory headerFactory, String assertedUserId) {
		Header[] headers = null;
		if (assertedUserId != null) {
			headers = new Header[1];
			headers[0] = headerFactory
					.getBasicHeader(
							XcapConstant.HEADER_X_3GPP_Asserted_Identity,
							assertedUserId);
		}
		return headers;
	}
}
