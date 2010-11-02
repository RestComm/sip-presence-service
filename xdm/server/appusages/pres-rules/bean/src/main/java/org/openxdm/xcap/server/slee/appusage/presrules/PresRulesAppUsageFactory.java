package org.openxdm.xcap.server.slee.appusage.presrules;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class PresRulesAppUsageFactory implements AppUsageFactory {

	private Schema schema = null;
	
	public PresRulesAppUsageFactory(Schema schema) {
		this.schema = schema;
	}
	
	public AppUsage getAppUsageInstance() {
		return new PresRulesAppUsage(schema.newValidator());
	}
	
	public String getAppUsageId() {
		return PresRulesAppUsage.ID;
	}
	
	@Override
	public AppUsageDataSourceInterceptor getDataSourceInterceptor() {
		return null;
	}
}