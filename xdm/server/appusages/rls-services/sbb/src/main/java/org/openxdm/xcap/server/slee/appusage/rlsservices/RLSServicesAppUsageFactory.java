package org.openxdm.xcap.server.slee.appusage.rlsservices;

import javax.slee.facilities.Tracer;
import javax.xml.validation.Schema;

import org.openxdm.xcap.common.appusage.AppUsage;
import org.openxdm.xcap.common.appusage.AppUsageFactory;

public class RLSServicesAppUsageFactory implements AppUsageFactory {

	private final Schema schema;
	private final Tracer tracer;
	
	public RLSServicesAppUsageFactory(Schema schema,Tracer tracer) {
		this.schema = schema;
		this.tracer = tracer;
	}
	
	public AppUsage getAppUsageInstance() {
		return new RLSServicesAppUsage(schema.newValidator(),tracer);
	}
	
	public String getAppUsageId() {
		return RLSServicesAppUsage.ID;
	}
}
