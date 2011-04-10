package org.mobicents.xdm.server.appusage.oma.prescontent;

import java.util.Set;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class OMAPresContentAppUsageFactory implements AppUsageFactory {

	private final Schema schema;
	private final Set<String> encodingsAllowed;
	private final int maxDataSize;
	private final Set<String> mimetypesAllowed;
	private final String presRulesAUID;
	private final String presRulesDocumentName;
	
	public OMAPresContentAppUsageFactory(Schema schema, Set<String> encodingsAllowed, int maxDataSize, Set<String> mimetypesAllowed, String presRulesAUID, String presRulesDocumentName) {
		this.schema = schema;
		this.encodingsAllowed = encodingsAllowed;
		this.maxDataSize = maxDataSize;
		this.mimetypesAllowed = mimetypesAllowed;
		this.presRulesAUID = presRulesAUID;
		this.presRulesDocumentName = presRulesDocumentName;
	}
	
	public AppUsage getAppUsageInstance() {
		return new OMAPresContentAppUsage(schema.newValidator(), encodingsAllowed, maxDataSize, mimetypesAllowed, presRulesAUID, presRulesDocumentName);
	}
	
	public String getAppUsageId() {
		return OMAPresContentAppUsage.ID;
	}
	
	@Override
	public AppUsageDataSourceInterceptor getDataSourceInterceptor() {
		return null;
	}
}
