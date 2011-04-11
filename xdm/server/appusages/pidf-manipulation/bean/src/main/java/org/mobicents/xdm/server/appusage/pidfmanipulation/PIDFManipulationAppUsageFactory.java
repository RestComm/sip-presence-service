package org.mobicents.xdm.server.appusage.pidfmanipulation;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class PIDFManipulationAppUsageFactory implements AppUsageFactory {

	private final Schema schema;
	
	public PIDFManipulationAppUsageFactory(Schema schema) {
		this.schema = schema;
	}
	
	public AppUsage getAppUsageInstance() {
		return new PIDFManipulationAppUsage(schema.newValidator());
	}
	
	public String getAppUsageId() {
		return PIDFManipulationAppUsage.ID;
	}
	
	@Override
	public AppUsageDataSourceInterceptor getDataSourceInterceptor() {
		return null;
	}
}
