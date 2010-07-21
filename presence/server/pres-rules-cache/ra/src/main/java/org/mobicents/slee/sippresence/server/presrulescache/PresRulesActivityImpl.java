package org.mobicents.slee.sippresence.server.presrulescache;

import org.openxdm.xcap.client.appusage.presrules.jaxb.commonpolicy.Ruleset;
import org.openxdm.xcap.common.uri.DocumentSelector;

public class PresRulesActivityImpl implements PresRulesActivity {

	private final DocumentSelector documentSelector;
	
	private final PresRulesCacheResourceAdaptor ra;
	
	public PresRulesActivityImpl(DocumentSelector documentSelector,PresRulesCacheResourceAdaptor ra) {
		if (documentSelector == null) {
			throw new NullPointerException("null documentSelector");
		}
		this.documentSelector = documentSelector;
		this.ra = ra;
	}
	
	@Override
	public DocumentSelector getDocumentSelector() {
		return documentSelector;
	}
	
	@Override
	public Ruleset getRuleset() {
		return ra.getDataSource().getRuleset(documentSelector);
	}

	@Override
	public String toString() {
		return new StringBuilder("PresRulesActivityImpl[ds=").append(documentSelector).append("]").toString();
	}

	@Override
	public int hashCode() {
		return documentSelector.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PresRulesActivityImpl other = (PresRulesActivityImpl) obj;
		return this.documentSelector.equals(other.documentSelector);
	}
	
	
}
