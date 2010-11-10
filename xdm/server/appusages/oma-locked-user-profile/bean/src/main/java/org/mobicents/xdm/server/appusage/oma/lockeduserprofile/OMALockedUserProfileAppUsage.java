package org.mobicents.xdm.server.appusage.oma.lockeduserprofile;

import javax.xml.validation.Validator;

import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.mobicents.xdm.server.appusage.oma.userprofile.OMAUserProfileAppUsage;
import org.openxdm.xcap.common.error.ConstraintFailureConflictException;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.error.UniquenessFailureConflictException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.w3c.dom.Document;
/**
 * OMA XDM 2.0 Locked User Profile XCAP App Usage.
 * @author martins
 *
 */
public class OMALockedUserProfileAppUsage extends OMAUserProfileAppUsage {

	public static final String ID = "org.openmobilealliance.locked-user-profile";
		
	/**
	 * 
	 * @param schemaValidator
	 */
	public OMALockedUserProfileAppUsage(Validator schemaValidator) {
		super(ID,schemaValidator,new OMALockedUserProfileAuthorizationPolicy());
	}
	
	@Override
	public void checkConstraintsOnPut(Document document, String xcapRoot,
			DocumentSelector documentSelector, AppUsageDataSource dataSource)
			throws UniquenessFailureConflictException,
			InternalServerErrorException, ConstraintFailureConflictException {
		// avoid constraints from super
	}
	
}
