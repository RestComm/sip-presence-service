package org.mobicents.xdm.server.appusage.oma.userprofile;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class OMAUserProfileAppUsageFactory implements AppUsageFactory {

	private final Schema schema;

	public OMAUserProfileAppUsageFactory(Schema schema) {
		this.schema = schema;
	}
	
	public AppUsage getAppUsageInstance() {
		return new OMAUserProfileAppUsage(schema.newValidator());
	}
	
	public String getAppUsageId() {
		return OMAUserProfileAppUsage.ID;
	}
	
	@Override
	public AppUsageDataSourceInterceptor getDataSourceInterceptor() {
		return null;
	}
}
