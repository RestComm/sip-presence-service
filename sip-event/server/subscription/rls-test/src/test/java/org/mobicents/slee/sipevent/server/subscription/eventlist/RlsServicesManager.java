package org.mobicents.slee.sipevent.server.subscription.eventlist;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Hashtable;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.mobicents.slee.enabler.userprofile.jpa.jmx.UserProfileControlManagementMBean;
import org.openxdm.xcap.client.XCAPClient;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ListType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType.DisplayName;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.ObjectFactory;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.PackagesType;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.RlsServices;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.ServiceType;
import org.openxdm.xcap.common.key.UserDocumentUriKey;
import org.openxdm.xcap.server.slee.appusage.rlsservices.RLSServicesAppUsage;

public class RlsServicesManager {

	private XCAPClient xCAPClient;
	private final ResourceListServerSipTest test;
	private final String serviceUri;
	private final String[] entryURIs;
	
	protected String password = "password";
	
	private ObjectName userProfileMBeanObjectName;
	private RMIAdaptor rmiAdaptor;
		
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
	
	private void createUser(String user, String password) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		String sigs[] = { String.class.getName(), String.class.getName() };
		Object[] args = { user, password };
		rmiAdaptor.invoke(userProfileMBeanObjectName, "addUser", args, sigs);	
	}
	
	private void removeUser(String user) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		String sigs[] = { String.class.getName()};
		Object[] args = { user};
		rmiAdaptor.invoke(userProfileMBeanObjectName, "removeUser", args, sigs);	
	}
	
	public void runBefore() throws IOException, InterruptedException, MalformedObjectNameException, NullPointerException, NamingException, InstanceNotFoundException, MBeanException, ReflectionException {
		xCAPClient = ServerConfiguration.getXCAPClientInstance();	
		initRmiAdaptor();
		createUser(serviceUri, password);
		xCAPClient.setAuthenticationCredentials(serviceUri, password);
		xCAPClient.setDoAuthentication(true);
	}
	
	public void runAfter() throws IOException, InstanceNotFoundException, MBeanException, ReflectionException {
		if (xCAPClient != null) {
			xCAPClient.shutdown();
			xCAPClient = null;
		}		
		removeUser(serviceUri);
	}
	
	public RlsServicesManager(String serviceUri, String[] entryURIs, ResourceListServerSipTest test) {
		this.serviceUri = serviceUri;
		this.entryURIs = entryURIs;
		this.test = test;		
	}
	
	public void putRlsServices() {
		try {
			runBefore();
		} catch (Throwable e) {
			e.printStackTrace();
			test.failTest(e.getMessage());
		}
		try {
			xCAPClient.put(new UserDocumentUriKey(RLSServicesAppUsage.ID,serviceUri,"index"), RLSServicesAppUsage.MIMETYPE, getRlsServices(entryURIs).getBytes("UTF-8"),null);
		} catch (Exception e) {
			e.printStackTrace();
			test.failTest(e.getMessage());
		}
	}
	
	public void deleteRlsServices() {
		try {
			xCAPClient.delete(new UserDocumentUriKey(RLSServicesAppUsage.ID,serviceUri,"index"),null);
		} catch (Exception e) {
			e.printStackTrace();
			test.failTest(e.getMessage());
		}
		try {
			runAfter();
		} catch (Throwable e) {
			e.printStackTrace();
			test.failTest(e.getMessage());
		}
	}
	
	private EntryType createEntryType(String uri) {
		EntryType entryType = new EntryType();
		entryType.setUri(uri);
		DisplayName displayName = new EntryType.DisplayName();
		displayName.setValue(uri);
		entryType.setDisplayName(displayName);
		return entryType;
	}
	
	private String getRlsServices(String[] entryURIs) {
		StringWriter stringWriter = new StringWriter();
		try {			
			JAXBContext context = JAXBContext.newInstance("org.openxdm.xcap.client.appusage.rlsservices.jaxb");
			ListType listType = new ListType();
			for (String entryURI : entryURIs) {
				listType.getListOrExternalOrEntry().add(createEntryType(entryURI));
			}
			ServiceType serviceType = new ServiceType();
			serviceType.setList(listType);
			PackagesType packagesType = new PackagesType();
			packagesType.getPackageAndAny().add(new ObjectFactory().createPackagesTypePackage("presence"));
			serviceType.setPackages(packagesType);
			serviceType.setUri(serviceUri);
			RlsServices rlsServices = new RlsServices();
			rlsServices.getService().add(serviceType);
			context.createMarshaller().marshal(rlsServices, stringWriter);
			return stringWriter.toString();			
		} catch (Exception e) {
			e.printStackTrace();
			test.failTest(e.getMessage());
		}
		finally {		
			try {
				stringWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
				test.failTest(e.getMessage());
			}
		}
		return null;
	}
}
