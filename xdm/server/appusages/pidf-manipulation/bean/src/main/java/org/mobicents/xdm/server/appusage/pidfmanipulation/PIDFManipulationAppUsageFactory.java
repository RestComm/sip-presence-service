package org.mobicents.xdm.server.appusage.pidfmanipulation;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class PIDFManipulationAppUsageFactory implements AppUsageFactory {

	private final Schema schema;
	private final String allowedDocumentName;
	
	public PIDFManipulationAppUsageFactory(Schema schema, String allowedDocumentName) {
		this.schema = schema;
		this.allowedDocumentName = allowedDocumentName;
	}
	
	public AppUsage getAppUsageInstance() {
		return new PIDFManipulationAppUsage(schema.newValidator(),allowedDocumentName);
	}
	
	public String getAppUsageId() {
		return PIDFManipulationAppUsage.ID;
	}
	
	@Override
	public AppUsageDataSourceInterceptor getDataSourceInterceptor() {
		return null;
	}
}
