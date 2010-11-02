package org.mobicents.xdm.server.appusage.oma.xcapdirectory;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class XcapDirectoryAppUsageFactory implements AppUsageFactory {

	private final Schema schema;
	
	public XcapDirectoryAppUsageFactory(Schema schema) {
		this.schema = schema;		
	}
	
	public AppUsage getAppUsageInstance() {
		return new XcapDirectoryAppUsage(schema.newValidator());
	}
	
	public String getAppUsageId() {
		return XcapDirectoryAppUsage.ID;
	}
	
	@Override
	public AppUsageDataSourceInterceptor getDataSourceInterceptor() {
		return new XcapDirectoryAppUsageDataSourceInterceptor();
	}
}
