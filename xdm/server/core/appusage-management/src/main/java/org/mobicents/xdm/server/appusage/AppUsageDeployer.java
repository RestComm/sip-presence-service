package org.mobicents.xdm.server.appusage;

import java.net.URI;

import javax.xml.validation.Schema;

import org.apache.log4j.Logger;
import org.openxdm.xcap.common.xml.SchemaContext;

public abstract class AppUsageDeployer {

	private static final Logger logger = Logger.getLogger(AppUsageDeployer.class);
	
	private AppUsageFactory appUsageFactory;
	
	/**
	 * Creates the app usage factory.
	 * @return
	 */
	public abstract AppUsageFactory createAppUsageFactory(Schema schema);
	
	/**
	 * Retrieves the schema's root namespace for the app usage.
	 * @return
	 */
	public abstract String getSchemaRootNamespace();
	
	/**
	 * Retrieves the {@link URI} pointing to the directory containing the XML Schemas used to validate app usage documents.
	 * @return
	 */
	public URI getSchemaDirectoryURI() {
		return AppUsageManagement.getInstance().getDefaultSchemaDir();
	}
	
	/**
	 * This will be invoked by JBoss Microcontainer when the bean is deployed.
	 */
	public void start() {
		
		String schemaRootNamespace = getSchemaRootNamespace();
		URI schemaDirURI = getSchemaDirectoryURI();
		/*		
		if (schemaDirURI..get.getProtocol().equals("vfszip")) {
			try {
				schemaDirURL = new URL("file:"+schemaDirURL.getPath());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}*/
		
		if (logger.isDebugEnabled()) {
			logger.debug("Loading "+schemaRootNamespace+" combined schema from URI "+schemaDirURI);
		}
		
		try {
			// create schema context
			SchemaContext schemaContext = SchemaContext.fromDir(schemaDirURI);
			// get schema from context
			Schema schema = schemaContext
					.getCombinedSchema(schemaRootNamespace);
			if (logger.isInfoEnabled()) {
				logger.info("Loaded "+schemaRootNamespace+" combined schema from URI "+schemaDirURI);
			}	
			appUsageFactory = createAppUsageFactory(schema);
			AppUsageManagement.getInstance().put(appUsageFactory);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load "+schemaRootNamespace+" combined schema from URI "+schemaDirURI,e);
		}		
	}
	
	/**
	 * This will be invoked by JBoss Microcontainer when the bean is undeployed.
	 */
	public void stop() {
		if (appUsageFactory != null) 
			AppUsageManagement.getInstance().remove(appUsageFactory.getAppUsageId());
	}
	
}
