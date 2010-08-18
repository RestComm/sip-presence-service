package org.openxdm.xcap.server.slee.appusage.rlsservices;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsageDeployer;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class RLSServicesAppUsageDeployer extends AppUsageDeployer {

	@Override
	public AppUsageFactory createAppUsageFactory(Schema schema) {
		return new RLSServicesAppUsageFactory(schema);
	}

	@Override
	public String getSchemaRootNamespace() {
		return RLSServicesAppUsage.DEFAULT_DOC_NAMESPACE;
	}

}
