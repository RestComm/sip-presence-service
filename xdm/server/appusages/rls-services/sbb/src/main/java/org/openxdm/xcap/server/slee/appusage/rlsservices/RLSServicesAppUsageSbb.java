package org.openxdm.xcap.server.slee.appusage.rlsservices;

import java.net.URL;

import javax.slee.facilities.Tracer;
import javax.xml.validation.Schema;

import org.openxdm.xcap.common.appusage.AppUsageFactory;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.xml.SchemaContext;
import org.openxdm.xcap.server.slee.AbstractAppUsageSbb;

/**
 * JAIN SLEE Root Sbb for rls-services Xcap application usage.  
 * @author Eduardo Martins
 *
 */
public abstract class RLSServicesAppUsageSbb extends AbstractAppUsageSbb {

	// MANDATORY ABSTRACT METHODS IMPL FOR A APP USAGE ROOT SBB, AbstractAppUsageSbb will invoke them

	public AppUsageFactory getAppUsageFactory() throws InternalServerErrorException {

		final Tracer tracer = super.getSbbContext().getTracer(getClass().getSimpleName());
		
		if(tracer.isFineEnabled()) {
			tracer.fine("getAppUsageFactory()");
		}
		
		AppUsageFactory appUsageFactory = null;

		try {
			// load schema files to dom documents
			tracer.info("Loading schemas from file system...");
			URL schemaDirURL = this.getClass().getResource("xsd");
			if (schemaDirURL != null) {
				// create schema context
				SchemaContext schemaContext = SchemaContext
						.fromDir(schemaDirURL.toURI());
				// get schema from context
				Schema schema = schemaContext
						.getCombinedSchema(RLSServicesAppUsage.DEFAULT_DOC_NAMESPACE);
				tracer.info("Schemas loaded.");
				// create and return factory
				appUsageFactory = new RLSServicesAppUsageFactory(schema,super.getSbbContext().getTracer(RLSServicesAppUsage.class.getSimpleName()));
				
			} else {
				tracer.warning("Schemas dir resource not found!");
			}
		} catch (Exception e) {
			tracer.severe("Unable to load app usage schemas from file system", e);
		}

		if (appUsageFactory == null) {
			throw new InternalServerErrorException(
					"Unable to get app usage factory");
		} else {
			return appUsageFactory;
		}
	}	

	public String getAUID() {
		return RLSServicesAppUsage.ID;
	}

}