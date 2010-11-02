package org.mobicents.xdm.server.appusage.oma.xcapdirectory;

import javax.xml.validation.Validator;

import org.mobicents.xdm.server.appusage.AppUsage;

public class XcapDirectoryAppUsage extends AppUsage {

	public static final String ID = "org.openmobilealliance.xcap-directory";
	public static final String DEFAULT_DOC_NAMESPACE = "urn:oma:xml:xdm:xcap-directory";
	public static final String MIMETYPE = "application/vnd.oma.xcap-directory+xml";
		
	public XcapDirectoryAppUsage(Validator schemaValidator) {
		super(ID,DEFAULT_DOC_NAMESPACE,MIMETYPE,schemaValidator,new XcapDirectoryAuthorizationPolicy());
	}
	
}
	
