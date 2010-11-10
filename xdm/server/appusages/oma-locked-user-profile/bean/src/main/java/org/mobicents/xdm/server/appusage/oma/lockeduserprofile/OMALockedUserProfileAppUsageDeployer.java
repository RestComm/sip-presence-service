package org.mobicents.xdm.server.appusage.oma.lockeduserprofile;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsageFactory;
import org.mobicents.xdm.server.appusage.oma.userprofile.OMAUserProfileAppUsageDeployer;

public class OMALockedUserProfileAppUsageDeployer extends OMAUserProfileAppUsageDeployer {
	
	@Override
	public AppUsageFactory createAppUsageFactory(Schema schema) {
		return new OMALockedUserProfileAppUsageFactory(schema);
	}

}
