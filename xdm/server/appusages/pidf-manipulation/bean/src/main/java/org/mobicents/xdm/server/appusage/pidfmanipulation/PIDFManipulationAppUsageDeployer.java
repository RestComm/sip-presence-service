package org.mobicents.xdm.server.appusage.pidfmanipulation;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsageDeployer;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class PIDFManipulationAppUsageDeployer extends AppUsageDeployer {
	
	private String allowedDocumentName;
	
	@Override
	public AppUsageFactory createAppUsageFactory(Schema schema) {
		return new PIDFManipulationAppUsageFactory(schema,allowedDocumentName);
	}

	@Override
	public String getSchemaRootNamespace() {
		return PIDFManipulationAppUsage.DEFAULT_DOC_NAMESPACE;
	}

	public String getAllowedDocumentName() {
		return allowedDocumentName;
	}
	
	public void setAllowedDocumentName(String allowedDocumentName) {
		this.allowedDocumentName = allowedDocumentName;
	}
}
