package org.mobicents.xdm.server.appusage.oma.groupusagelist;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class OMAGroupUsageListAppUsageFactory implements AppUsageFactory {

	private final Schema schema;

	public OMAGroupUsageListAppUsageFactory(Schema schema) {
		this.schema = schema;
	}
	
	public AppUsage getAppUsageInstance() {
		return new OMAGroupUsageListAppUsage(schema.newValidator());
	}
	
	public String getAppUsageId() {
		return OMAGroupUsageListAppUsage.ID;
	}
	
	@Override
	public AppUsageDataSourceInterceptor getDataSourceInterceptor() {
		return null;
	}
}
