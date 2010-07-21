package org.mobicents.slee.sippresence.server.presrulescache;

import java.util.concurrent.ConcurrentHashMap;

import org.openxdm.xcap.client.appusage.presrules.jaxb.commonpolicy.Ruleset;
import org.openxdm.xcap.common.uri.DocumentSelector;

public class PresRulesCacheDataSource {

	private final ConcurrentHashMap<PresRulesActivityHandle, PresRulesActivityImpl> activities = new ConcurrentHashMap<PresRulesActivityHandle, PresRulesActivityImpl>();
	
	private final ConcurrentHashMap<DocumentSelector, Ruleset> rulesets = new ConcurrentHashMap<DocumentSelector, Ruleset>();

	public PresRulesActivityImpl putActivity(PresRulesActivityHandle handle, PresRulesActivityImpl activity) {
		return activities.putIfAbsent(handle, activity);
	}
	
	public PresRulesActivityImpl getActivity(PresRulesActivityHandle handle) {
		return activities.get(handle);
	}
	
	public boolean removeActivity(PresRulesActivityHandle handle) {
		return activities.remove(handle) != null;
	}
	
	public void putRuleset(DocumentSelector documentSelector, Ruleset ruleset) {
		rulesets.put(documentSelector, ruleset);
	}
	
	public Ruleset getRuleset(DocumentSelector documentSelector) {
		return rulesets.get(documentSelector);
	}
	
	public boolean removeRuleset(DocumentSelector documentSelector) {
		return rulesets.remove(documentSelector) != null;
	}
	
}
