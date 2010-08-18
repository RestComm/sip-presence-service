package org.openxdm.xcap.server.slee.appusage.xcapcaps;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsageDeployer;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class XCAPCapsAppUsageDeployer extends AppUsageDeployer {
	
	@Override
	public AppUsageFactory createAppUsageFactory(Schema schema) {
		return new XCAPCapsAppUsageFactory(schema);
	}

	@Override
	public String getSchemaRootNamespace() {
		return XCAPCapsAppUsage.DEFAULT_DOC_NAMESPACE;
	}

}
