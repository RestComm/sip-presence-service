package org.mobicents.xdm.server.appusage.oma.prescontent;

import java.util.Set;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsageDeployer;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class OMAPresContentAppUsageDeployer extends AppUsageDeployer {
	
	private Set<String> encodingsAllowed;
	private Integer maxDataSize;
	private Set<String> mimetypesAllowed;
	private String presRulesAUID;
	private String presRulesDocumentName;
	
	@Override
	public AppUsageFactory createAppUsageFactory(Schema schema) {
		return new OMAPresContentAppUsageFactory(schema,encodingsAllowed,(maxDataSize != null ? maxDataSize : 0),mimetypesAllowed,presRulesAUID,presRulesDocumentName);
	}

	public Set<String> getEncodingsAllowed() {
		return encodingsAllowed;
	}
	
	public void setEncodingsAllowed(Set<String> encodingsAllowed) {
		this.encodingsAllowed = encodingsAllowed;
	}
	
	public Integer getMaxDataSize() {
		return maxDataSize;
	}
	
	public void setMaxDataSize(Integer maxDataSize) {
		this.maxDataSize = maxDataSize;
	}
	
	public Set<String> getMimetypesAllowed() {
		return mimetypesAllowed;
	}
	
	public void setMimetypesAllowed(Set<String> mimetypesAllowed) {
		this.mimetypesAllowed = mimetypesAllowed;
	}
	
	public String getPresRulesAUID() {
		return presRulesAUID;
	}
	
	public String getPresRulesDocumentName() {
		return presRulesDocumentName;
	}

	@Override
	public String getSchemaRootNamespace() {
		return OMAPresContentAppUsage.DEFAULT_DOC_NAMESPACE;
	}

	public void setPresRulesAUID(String auid) {
		this.presRulesAUID = auid;		
	}

	public void setPresRulesDocumentName(String documentName) {
		this.presRulesDocumentName = documentName;
	}

}
