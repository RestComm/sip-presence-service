package org.openxdm.xcap.client.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;

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

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.junit.Test;
import org.mobicents.slee.enabler.userprofile.jpa.jmx.UserProfileControlManagementMBean;
import org.mobicents.xcap.client.XcapClient;
import org.mobicents.xcap.client.impl.XcapClientImpl;
import org.mobicents.xcap.client.uri.DocumentSelectorBuilder;
import org.mobicents.xcap.client.uri.ElementSelectorBuilder;
import org.mobicents.xcap.client.uri.UriBuilder;
import org.mobicents.xdm.server.appusage.AppUsage;
import org.openxdm.xcap.server.slee.appusage.omapresrules.OMAPresRulesAppUsage;
import org.openxdm.xcap.server.slee.appusage.resourcelists.ResourceListsAppUsage;
import org.openxdm.xcap.server.slee.appusage.rlsservices.RLSServicesAppUsage;

public class LoadTestUserProvisioningTest {
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(LoadTestUserProvisioningTest.class);
	}
	
	private Properties properties = new Properties();
		
	private XcapClient client = null;
	private AppUsage resourceListAppUsage = new ResourceListsAppUsage(null);
	private AppUsage rlsServicesAppUsage = new RLSServicesAppUsage(null);
	private AppUsage presRulesAppUsage = new OMAPresRulesAppUsage(null);
	
	private ObjectName userProfileMBeanObjectName;
	private RMIAdaptor rmiAdaptor;
	
	
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
	
	private void provisionUser(String user) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException, URISyntaxException {
		
		try {
			createUser(user,"password");
		}
		catch (RuntimeMBeanException e) {
			if (!(e.getCause() instanceof IllegalStateException)) {
				e.printStackTrace();
			}
		}
		
		Credentials credentials = new UsernamePasswordCredentials(user, "password");
		
		String schemeAndAuth = "http://"+properties.getProperty("SERVER_HOST")+":"+properties.getProperty("SERVER_PORT");
		String xcapRoot = properties.getProperty("SERVER_XCAP_ROOT");
		
		// create resource lists doc uri		
		String resourcesListDocumentSelector = DocumentSelectorBuilder.getUserDocumentSelectorBuilder(resourceListAppUsage.getAUID(),user,"index").toPercentEncodedString();
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
		HashSet<Integer> entries = new HashSet<Integer>();
		Random random= new Random();
		int entryUserNumber = -1;
		int users_size_length = properties.getProperty("USERS_SIZE").length(); 
		for (int i=1;i<Integer.valueOf(properties.getProperty("USERS_RESOURCE_LIST_SIZE"));i++) {
			while(true) {
				entryUserNumber = random.nextInt(Integer.valueOf(properties.getProperty("USERS_SIZE"))-1)+1;
				// ensures no repeated entries
				if (entries.add(entryUserNumber)) {
					break;
				}
			}
			String entryUserNumberString = Integer.toString(entryUserNumber);
			while(entryUserNumberString.length() < users_size_length) {
				// sipp adds 0s to usernames to make them of same length
				entryUserNumberString = "0"+entryUserNumberString;				
			}
			String entryUser = "sip:"+properties.getProperty("USERS_NAME_PREFIX")+entryUserNumberString+"@"+properties.getProperty("SERVER_HOST");
			resourceList += "\n\t\t<entry uri=\""+entryUser+"\"><display-name>"+entryUser+"</display-name></entry>";
		}
		resourceList += "\n\t</list>" +
			"\n</resource-lists>";	
		
		// put the doc in the xdms
		System.out.println("Resource Lists doc generated for "+user+":\n"+resourceList);
		int putResult = client.put(resourceListDocumentURI,resourceListAppUsage.getMimetype(),resourceList,null,credentials).getCode();
		System.out.println("Put result for resource lists doc:"+putResult);
		Assert.assertTrue(putResult < 300);
		
		// create pres-rules doc uri
		String presRulesDocumentSelector = DocumentSelectorBuilder.getUserDocumentSelectorBuilder(presRulesAppUsage.getAUID(),user,"pres-rules").toPercentEncodedString();
		uriBuilder.setDocumentSelector(presRulesDocumentSelector);
		URI presRulesDocumentURI = uriBuilder.toURI();
		
		// create pres-rules content
		String presRules = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
			"\n<cr:ruleset xmlns=\"urn:ietf:params:xml:ns:pres-rules\" xmlns:cr=\"urn:ietf:params:xml:ns:common-policy\">"+
			"\n\t<cr:rule id=\"a\">"+
			"\n\t\t<cr:conditions>"+
			"\n\t\t\t<cr:identity><cr:many domain=\""+properties.getProperty("SERVER_HOST")+"\"/></cr:identity>"+
			"\n\t\t</cr:conditions>"+
			"\n\t\t<cr:actions><sub-handling>allow</sub-handling></cr:actions>"+
			"\n\t\t<cr:transformations>"+
			"\n\t\t\t<provide-devices><all-devices/></provide-devices>"+
			"\n\t\t\t<provide-services><all-services/></provide-services>"+
			"\n\t\t\t<provide-persons><all-persons/></provide-persons>"+
			"\n\t\t\t<provide-all-attributes/>"+
			"\n\t\t</cr:transformations>"+
			"\n\t</cr:rule>"+
			"\n</cr:ruleset>";

		// put pres-rules in the xdms
		System.out.println("Pres Rules doc generated for "+user+":\n"+presRules);
		putResult = client.put(presRulesDocumentURI,presRulesAppUsage.getMimetype(),presRules,null,credentials).getCode();
		System.out.println("Put result for pres rules doc:"+putResult);
		Assert.assertTrue(putResult < 300);
				
		// create rls services doc uri
		String rlsServicesDocumentSelector = DocumentSelectorBuilder.getUserDocumentSelectorBuilder(rlsServicesAppUsage.getAUID(),user,"index").toPercentEncodedString();
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
		System.out.println("RLS Services doc generated for "+user+":\n"+rlsServices);
		putResult = client.put(rlsServicesDocumentURI,rlsServicesAppUsage.getMimetype(),rlsServices,null,credentials).getCode();
		System.out.println("Put result for rls services doc:"+putResult);
		Assert.assertTrue(putResult < 300);
		
	}
	
	@Test
	public void doProvisioning() throws IOException, MalformedObjectNameException, NullPointerException, NamingException, InstanceNotFoundException, MBeanException, ReflectionException, URISyntaxException {
		
		properties.load(this.getClass().getResourceAsStream("provisioning.properties"));
		client = new XcapClientImpl();
		initRmiAdaptor();		
		
		int users_size_length = properties.getProperty("USERS_SIZE").length(); 
		for (int i=0;i<Integer.valueOf(properties.getProperty("USERS_SIZE"));i++) {
			String userNumber = Integer.toString(i+1);
			while(userNumber.length() < users_size_length) {
				// sipp adds 0s to usernames to make them of same length
				userNumber = "0"+userNumber;				
			}
			provisionUser("sip:"+properties.getProperty("USERS_NAME_PREFIX")+userNumber+"@"+properties.getProperty("SERVER_HOST"));
		}
		
		client.shutdown();
	}

}
