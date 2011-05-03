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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.junit.Test;
import org.mobicents.slee.enabler.userprofile.jpa.jmx.UserProfileControlManagementMBean;
import org.mobicents.xcap.client.XcapClient;
import org.mobicents.xcap.client.auth.Credentials;
import org.mobicents.xcap.client.impl.XcapClientImpl;
import org.mobicents.xcap.client.uri.DocumentSelectorBuilder;
import org.mobicents.xcap.client.uri.ElementSelectorBuilder;
import org.mobicents.xcap.client.uri.UriBuilder;

public class LoadTestUserProvisioningTest {
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(LoadTestUserProvisioningTest.class);
	}
	
	private Properties properties = new Properties();
		
	private XcapClient client = null;
	
	private ObjectName userProfileMBeanObjectName;
	private RMIAdaptor rmiAdaptor;
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initRmiAdaptor() throws NamingException, MalformedObjectNameException, NullPointerException {
		// Set Some JNDI Properties
		Hashtable env = new Hashtable();
		env.put(Context.PROVIDER_URL, "jnp://"+properties.getProperty("SERVER_HOST")+":1099");
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		env.put(Context.URL_PKG_PREFIXES, "org.jnp.interfaces");

		InitialContext ctx = new InitialContext(env);
		rmiAdaptor = (RMIAdaptor) ctx.lookup("jmx/rmi/RMIAdaptor");
		userProfileMBeanObjectName = new ObjectName(UserProfileControlManagementMBean.MBEAN_NAME);
	}
	
	private void createUser(String user, String password) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		String sigs[] = { String.class.getName(), String.class.getName() };
		Object[] args = { user, password };
		rmiAdaptor.invoke(userProfileMBeanObjectName, "addUser", args, sigs);	
	}
	
	private void provisionUser(String user, int userNumber) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException, URISyntaxException {
		
		System.out.println("Provisioning user "+user+" in the XDMS:");
		
		try {
			createUser(user,"password");
		}
		catch (RuntimeMBeanException e) {
			if (!(e.getCause() instanceof IllegalStateException)) {
				e.printStackTrace();
			}
		}
		
		Credentials credentials = client.getCredentialsFactory().getHttpDigestCredentials(user, "password");
		
		String schemeAndAuth = "http://"+properties.getProperty("SERVER_HOST")+":"+properties.getProperty("SERVER_PORT");
		String xcapRoot = properties.getProperty("SERVER_XCAP_ROOT");
		
		// create resource lists doc uri		
		String resourcesListDocumentSelector = DocumentSelectorBuilder.getUserDocumentSelectorBuilder("resource-lists",user,"index").toPercentEncodedString();
		UriBuilder uriBuilder = new UriBuilder()
			.setSchemeAndAuthority(schemeAndAuth)
			.setXcapRoot(xcapRoot)
			.setDocumentSelector(resourcesListDocumentSelector);
		URI resourceListDocumentURI = uriBuilder.toURI();
		
		// create resource-list content
		String resourceList =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"\n<resource-lists xmlns=\"urn:ietf:params:xml:ns:resource-lists\">" +
				"\n\t<list name=\"Default\">";
		int users_size = Integer.valueOf(properties.getProperty("USERS_SIZE"));
		//int users_size_length = properties.getProperty("USERS_SIZE").length(); 
		for (int i=1;i<Integer.valueOf(properties.getProperty("USERS_RESOURCE_LIST_SIZE"));i++) {
			int entryUserNumber = (userNumber+i)%users_size;
			String entryUserNumberString = Integer.toString(entryUserNumber);
			//while(entryUserNumberString.length() < users_size_length) {
				// sipp adds 0s to usernames to make them of same length
				//entryUserNumberString = "0"+entryUserNumberString;				
			//}
			String entryUser = "sip:"+properties.getProperty("USERS_NAME_PREFIX")+entryUserNumberString+"@"+properties.getProperty("SERVER_HOST");
			resourceList += "\n\t\t<entry uri=\""+entryUser+"\"><display-name>"+entryUser+"</display-name></entry>";
		}
		resourceList += "\n\t</list>" +
			"\n</resource-lists>";	
		
		// put the doc in the xdms
		int putResult = client.put(resourceListDocumentURI,"application/resource-lists+xml",resourceList,null,credentials).getCode();
		System.out.println("Put result for resource lists doc: "+putResult+". Doc:\n"+resourceList);
		Assert.assertTrue(putResult < 300);
		
		// create pres-rules doc uri
		String presRulesDocumentSelector = DocumentSelectorBuilder.getUserDocumentSelectorBuilder("org.openmobilealliance.pres-rules",user,"pres-rules").toPercentEncodedString();
		uriBuilder.setDocumentSelector(presRulesDocumentSelector);
		URI presRulesDocumentURI = uriBuilder.toURI();
		
		// create pres-rules content
		String presRules = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
			"<cr:ruleset xmlns=\"urn:ietf:params:xml:ns:pres-rules\" xmlns:cr=\"urn:ietf:params:xml:ns:common-policy\">"+
			"<cr:rule id=\"a\">"+
			"<cr:conditions>"+
			"<cr:identity><cr:many domain=\""+properties.getProperty("SERVER_HOST")+"\"/></cr:identity>"+
			"</cr:conditions>"+
			"<cr:actions><sub-handling>allow</sub-handling></cr:actions>"+
			"<cr:transformations>"+
			"<provide-devices><all-devices/></provide-devices>"+
			"<provide-services><all-services/></provide-services>"+
			"<provide-persons><all-persons/></provide-persons>"+
			"<provide-all-attributes/>"+
			"</cr:transformations>"+
			"</cr:rule>"+
			"</cr:ruleset>";

		// put pres-rules in the xdms
		putResult = client.put(presRulesDocumentURI,"application/auth-policy+xml",presRules,null,credentials).getCode();
		System.out.println("Put result for pres rules doc:"+putResult);
		Assert.assertTrue(putResult < 300);
				
		// create rls services doc uri
		String rlsServicesDocumentSelector = DocumentSelectorBuilder.getUserDocumentSelectorBuilder("rls-services",user,"index").toPercentEncodedString();
		uriBuilder.setDocumentSelector(rlsServicesDocumentSelector);
		URI rlsServicesDocumentURI = uriBuilder.toURI();
		
		// create rls-services content
		uriBuilder.setDocumentSelector(resourcesListDocumentSelector);
		uriBuilder.setElementSelector(new ElementSelectorBuilder().appendStepByName("resource-lists").appendStepByAttr("list", "name", "Default").toPercentEncodedString());
		String rlsServices = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"\n<rls-services xmlns=\"urn:ietf:params:xml:ns:rls-services\">" +
				"\n\t<service uri=\""+user+";pres-list=Default"+"\">" +
					"\n\t\t<resource-list>"+uriBuilder.toURI().toString()+"</resource-list>" +
					"\n\t\t<packages><package>presence</package></packages>" +
			"	\n\t</service>" +
			"\n</rls-services>";
		
		// put rls services in the xdms
		putResult = client.put(rlsServicesDocumentURI,"application/rls-services+xml",rlsServices,null,credentials).getCode();
		System.out.println("Put result for rls services doc:"+putResult);
		Assert.assertTrue(putResult < 300);
		
	}
	
	@Test
	public void doProvisioning() throws IOException, MalformedObjectNameException, NullPointerException, NamingException, InstanceNotFoundException, MBeanException, ReflectionException, URISyntaxException {
		
		properties.load(this.getClass().getResourceAsStream("provisioning.properties"));
		client = new XcapClientImpl();
		initRmiAdaptor();		
		
		//int users_size_length = properties.getProperty("USERS_SIZE").length(); 
		for (int i=1;i<=Integer.valueOf(properties.getProperty("USERS_SIZE"));i++) {
			String userNumber = Integer.toString(i);
			//while(userNumber.length() < users_size_length) {
				// sipp adds 0s to usernames to make them of same length
				//userNumber = "0"+userNumber;				
			//}
			provisionUser("sip:"+properties.getProperty("USERS_NAME_PREFIX")+userNumber+"@"+properties.getProperty("SERVER_HOST"),i);
		}
		
		client.shutdown();
	}

}
