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

package org.mobicents.xdm.server.appusage.oma.groupusagelist;

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
import org.mobicents.slee.enabler.userprofile.jpa.jmx.UserProfileControlManagementMBean;

public class AbstractT {

	private ObjectName userProfileMBeanObjectName;
	private RMIAdaptor rmiAdaptor;
	
	protected void initRmiAdaptor() throws NamingException, MalformedObjectNameException, NullPointerException {
		// Set Some JNDI Properties
		Hashtable env = new Hashtable();
		env.put(Context.PROVIDER_URL, "jnp://localhost:1099");
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
	
}
