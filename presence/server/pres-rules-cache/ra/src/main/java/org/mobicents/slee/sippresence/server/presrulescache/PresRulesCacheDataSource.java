/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
