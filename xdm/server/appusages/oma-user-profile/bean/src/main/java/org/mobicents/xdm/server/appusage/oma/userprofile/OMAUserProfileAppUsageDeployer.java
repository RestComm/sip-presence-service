package org.mobicents.xdm.server.appusage.oma.userprofile;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsageDeployer;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class OMAUserProfileAppUsageDeployer extends AppUsageDeployer {
	
	@Override
	public AppUsageFactory createAppUsageFactory(Schema schema) {
		return new OMAUserProfileAppUsageFactory(schema);
	}

	@Override
	public String getSchemaRootNamespace() {
		return OMAUserProfileAppUsage.DEFAULT_DOC_NAMESPACE;
	}

}
