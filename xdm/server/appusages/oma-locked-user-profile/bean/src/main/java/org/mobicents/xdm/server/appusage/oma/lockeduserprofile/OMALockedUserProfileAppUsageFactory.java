package org.mobicents.xdm.server.appusage.oma.lockeduserprofile;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class OMALockedUserProfileAppUsageFactory implements AppUsageFactory {

	private final Schema schema;

	public OMALockedUserProfileAppUsageFactory(Schema schema) {
		this.schema = schema;
	}
	
	public AppUsage getAppUsageInstance() {
		return new OMALockedUserProfileAppUsage(schema.newValidator());
	}
	
	public String getAppUsageId() {
		return OMALockedUserProfileAppUsage.ID;
	}
	
	@Override
	public AppUsageDataSourceInterceptor getDataSourceInterceptor() {
		return null;
	}
}
