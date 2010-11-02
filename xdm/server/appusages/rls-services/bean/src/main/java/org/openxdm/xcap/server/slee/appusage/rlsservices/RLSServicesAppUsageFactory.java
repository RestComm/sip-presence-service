package org.openxdm.xcap.server.slee.appusage.rlsservices;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class RLSServicesAppUsageFactory implements AppUsageFactory {

	private final Schema schema;
	
	public RLSServicesAppUsageFactory(Schema schema) {
		this.schema = schema;		
	}
	
	public AppUsage getAppUsageInstance() {
		return new RLSServicesAppUsage(schema.newValidator());
	}
	
	public String getAppUsageId() {
		return RLSServicesAppUsage.ID;
	}
	
	@Override
	public AppUsageDataSourceInterceptor getDataSourceInterceptor() {
		return null;
	}
}
