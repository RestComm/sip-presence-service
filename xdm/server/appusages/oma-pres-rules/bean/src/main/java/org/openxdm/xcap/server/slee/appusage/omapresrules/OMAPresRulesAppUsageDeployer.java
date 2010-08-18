package org.openxdm.xcap.server.slee.appusage.omapresrules;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsageDeployer;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class OMAPresRulesAppUsageDeployer extends AppUsageDeployer {

	@Override
	public AppUsageFactory createAppUsageFactory(Schema schema) {
		return new OMAPresRulesAppUsageFactory(schema);
	}

	@Override
	public String getSchemaRootNamespace() {
		return OMAPresRulesAppUsage.SCHEMA_TARGETNAMESPACE;
	}

}
