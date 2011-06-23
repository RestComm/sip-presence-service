package org.mobicents.slee.sippresence.server.publication;

import javax.sip.address.URI;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;
import javax.xml.validation.Schema;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.publication.StateComposer;
import org.mobicents.slee.sipevent.server.publication.data.ComposedPublication;
import org.mobicents.slee.sipevent.server.publication.data.Publication;
import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlParent;
import org.mobicents.slee.sipevent.server.subscription.NotifyContent;
import org.mobicents.slee.sippresence.server.jmx.SipPresenceServerManagement;
import org.mobicents.xdm.common.util.dom.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PresencePublicationControl {

	private static Logger logger = Logger
	.getLogger(PresencePublicationControl.class);
	
	private final static String[] eventPackages = { "presence" };

	public String[] getEventPackages() {
		return eventPackages;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl#notifySubscribers(org.mobicents.slee.sipevent.server.publication.pojo.ComposedPublication)
	 */
	public void notifySubscribers(ComposedPublication composedPublication, PresencePublicationControlSbbInterface sbb) {
		try {
			ImplementedSubscriptionControlParent subscriptionControl = sbb.getPresenceSubscriptionControl();
			ContentTypeHeader contentTypeHeader = (composedPublication
					.getContentType() == null || composedPublication
					.getContentSubType() == null) ? null : sbb.getHeaderFactory()
					.createContentTypeHeader(
							composedPublication.getContentType(),
							composedPublication.getContentSubType());
			subscriptionControl.notifySubscribers(composedPublication
					.getComposedPublicationKey().getEntity(),
					composedPublication.getComposedPublicationKey()
							.getEventPackage(), new NotifyContent(composedPublication
							.getDocumentAsDOM(), contentTypeHeader, null));
		} catch (Exception e) {
			logger.error("failed to notify subscribers for "
					+ composedPublication.getComposedPublicationKey(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl#authorizePublication(java.lang.String, org.w3c.dom.Document)
	 */
	public boolean authorizePublication(String requestEntity,
			Document unmarshalledContent) {
		// returns true if request uri matches entity (stripped from pres:
		// prefix if found) inside pidf doc
		Element pidfElement = unmarshalledContent.getDocumentElement();
		String entity = pidfElement.getAttribute("entity");
		if (entity != null) {
			if (entity.startsWith("pres:") && entity.length() > 5) {
				entity = entity.substring(5);
			}
			return entity.equals(requestEntity);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl#acceptsContentType(java.lang.String, javax.sip.header.ContentTypeHeader)
	 */
	public boolean acceptsContentType(String eventPackage,
			ContentTypeHeader contentTypeHeader) {
		// FIXME
		return true;
	}

	public Header getAcceptsHeader(String eventPackage, PresencePublicationControlSbbInterface sbb) {
		// FIXME
		if (eventPackage.equals("presence")) {
			try {
				return sbb.getHeaderFactory().createAcceptHeader("application",
						"pidf+xml");
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return null;
	}

	public Schema getSchema() {
		return SipPresenceServerManagement.getInstance().getCombinedSchema();
	}
	
	private static final PresenceCompositionPolicy presenceCompositionPolicy = new PresenceCompositionPolicy();
	
	public StateComposer getStateComposer() {
		return presenceCompositionPolicy;
	}

	public Publication getAlternativeValueForExpiredPublication(
			Publication publication) {
		Document document = publication.getDocumentAsDOM();
		Element presence = document.getDocumentElement();
		NodeList presenceChilds = presence.getChildNodes();
		Node presenceChild = null;
		for(int i=0;i<presenceChilds.getLength();i++) {
			presenceChild = presenceChilds.item(i);
			if (DomUtils.isElementNamed(presenceChild, "tuple")) {
				// remove all child element nodes
				NodeList tupleChilds = presenceChild.getChildNodes();
				Node tupleChild = null;
				for (int j=0; j<tupleChilds.getLength(); j++) {
					tupleChild = tupleChilds.item(j);
					if (tupleChild.getNodeType() == Node.ELEMENT_NODE) {
						presenceChild.removeChild(tupleChild);
					}
				}
				// add a closed status element
				Element status = document.createElement("status");
				tupleChild.appendChild(status);
				Element basic = document.createElement("basic");
				basic.setTextContent("closed");
				status.appendChild(basic);				
			}
			else if (presenceChild.getNodeType() == Node.ELEMENT_NODE) {
				// remove any other child element
				presence.removeChild(presenceChild);
			}
		}
		publication.setDocumentAsString(null);
		return publication;
	}

	public boolean isResponsibleForResource(URI uri) {
		return true;
	}
}
