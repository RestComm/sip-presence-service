package org.mobicents.slee.sippresence.server.presrulescache;

import javax.slee.resource.StartActivityException;

import org.openxdm.xcap.common.uri.DocumentSelector;

public interface PresRulesSbbInterface {

	public void rulesetUpdated(DocumentSelector documentSelector,
			String oldETag, String newETag, String ruleset);

	public PresRulesActivity getActivity(DocumentSelector documentSelector) throws StartActivityException;

}
