package org.openxdm.xcap.server.slee.appusage.resourcelists;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsageDeployer;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class ResourceListsAppUsageDeployer extends AppUsageDeployer {
	
	private final boolean omaCompliant;

	public ResourceListsAppUsageDeployer(boolean omaCompliant) {
		super();
		this.omaCompliant = omaCompliant;
	}
	
	@Override
	public AppUsageFactory createAppUsageFactory(Schema schema) {
		return new ResourceListsAppUsageFactory(schema,omaCompliant);
	}

	@Override
	public String getSchemaRootNamespace() {
		return ResourceListsAppUsage.DEFAULT_DOC_NAMESPACE;
	}

}
