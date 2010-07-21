package org.mobicents.slee.sippresence.server.presrulescache;

import org.openxdm.xcap.client.appusage.presrules.jaxb.commonpolicy.Ruleset;
import org.openxdm.xcap.common.uri.DocumentSelector;

public interface PresRulesActivity {

	public DocumentSelector getDocumentSelector();
	
	public Ruleset getRuleset();
	
}
