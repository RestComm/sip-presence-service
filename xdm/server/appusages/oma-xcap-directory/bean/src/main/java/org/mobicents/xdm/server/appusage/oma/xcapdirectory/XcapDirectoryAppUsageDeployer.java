package org.mobicents.xdm.server.appusage.oma.xcapdirectory;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsageDeployer;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class XcapDirectoryAppUsageDeployer extends AppUsageDeployer {

	@Override
	public AppUsageFactory createAppUsageFactory(Schema schema) {
		return new XcapDirectoryAppUsageFactory(schema);
	}

	@Override
	public String getSchemaRootNamespace() {
		return XcapDirectoryAppUsage.DEFAULT_DOC_NAMESPACE;
	}

}
