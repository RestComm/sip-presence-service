package org.mobicents.xdm.server.appusage.oma.groupusagelist;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsageDeployer;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class OMAGroupUsageListAppUsageDeployer extends AppUsageDeployer {
	
	@Override
	public AppUsageFactory createAppUsageFactory(Schema schema) {
		return new OMAGroupUsageListAppUsageFactory(schema);
	}

	@Override
	public String getSchemaRootNamespace() {
		return OMAGroupUsageListAppUsage.DEFAULT_DOC_NAMESPACE;
	}

}
