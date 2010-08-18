package org.openxdm.xcap.server.slee.appusage.presrules;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsageDeployer;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class PresRulesAppUsageDeployer extends AppUsageDeployer {
	
	@Override
	public AppUsageFactory createAppUsageFactory(Schema schema) {
		return new PresRulesAppUsageFactory(schema);
	}

	@Override
	public String getSchemaRootNamespace() {
		return PresRulesAppUsage.DEFAULT_DOC_NAMESPACE;
	}

}
