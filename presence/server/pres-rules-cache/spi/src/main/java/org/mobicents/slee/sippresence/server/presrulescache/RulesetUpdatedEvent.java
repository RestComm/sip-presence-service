package org.mobicents.slee.sippresence.server.presrulescache;

import java.io.Serializable;

import javax.slee.EventTypeID;

import org.openxdm.xcap.client.appusage.presrules.jaxb.commonpolicy.Ruleset;
import org.openxdm.xcap.common.uri.DocumentSelector;

public class RulesetUpdatedEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * IMPORTANT: must sync with the event descriptor data!!!!
	 */
	public static final EventTypeID EVENT_TYPE_ID = new EventTypeID("RulesetUpdatedEvent","org.mobicents","1.0");
	
	private final String id;
		
	private final DocumentSelector documentSelector;
	private final Ruleset ruleset;
	private final String oldETag;
	private final String newETag;
	
	public RulesetUpdatedEvent(DocumentSelector documentSelector,
			String oldETag, String newETag,
			Ruleset ruleset) {		
		this.documentSelector = documentSelector;
		this.oldETag = oldETag;
		this.newETag = newETag;
		this.ruleset = ruleset;
		// if doc was deleted add a non hex char to the old etag as event id
		id = (newETag != null) ? newETag : oldETag + "g";
	}
	
	public Ruleset getRuleset() {
		return ruleset;
	}
	
	public DocumentSelector getDocumentSelector() {
		return documentSelector;
	}
	
	public String getNewETag() {
		return newETag;
	}
	
	public String getOldETag() {
		return oldETag;
	}
	
	public boolean equals(Object o) {
		if (o != null && o.getClass() == this.getClass()) {
			return ((RulesetUpdatedEvent)o).id.equals(id);
		}
		else {
			return false;
		}	
	}
	
	public int hashCode() {		
		return id.hashCode();
	}
}
