/**
 * 
 */
package org.mobicents.slee.sipevent.server.publication;

import javax.sip.address.URI;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.mobicents.slee.sipevent.server.publication.data.ComposedPublication;
import org.mobicents.slee.sipevent.server.publication.data.Publication;

/**
 * @author martins
 *
 */
public interface ImplementedPublicationControl {

	/**
	 * the impl class SIP event packages supported
	 * 
	 * @return
	 */
	public String[] getEventPackages();

	/**
	 * Verifies if the specified content type header can be accepted for the
	 * specified event package.
	 * 
	 * @param eventPackage
	 * @param contentTypeHeader
	 * @return
	 */
	public boolean acceptsContentType(String eventPackage,
			ContentTypeHeader contentTypeHeader);

	/**
	 * Retrieves the accepted content types for the specified event package.
	 * 
	 * @param eventPackage
	 * @return
	 */
	public Header getAcceptsHeader(String eventPackage);

	/**
	 * Notifies subscribers about a publication update for the specified entity
	 * regarding the specified event package.
	 * 
	 * @param composedPublication
	 */
	public void notifySubscribers(ComposedPublication composedPublication);

	/**
	 * Retrieves the associated JAXB Context, needed to work with publication content.
	 * 
	 * @return
	 */
	public JAXBContext getJaxbContext();

	/**
	 * Retrieves the {@link StateComposer} concrete impl, used to combine publications.
	 * 
	 * @return 
	 */
	public StateComposer getStateComposer();

	/**
	 * Checks if this server is responsible for the resource publishing state.
	 * 
	 */
	public boolean isResponsibleForResource(URI uri);

	/**
	 * verifies if entity is authorized to publish the content
	 * 
	 * @param entity
	 * @param unmarshalledContent
	 * @return
	 */
	public boolean authorizePublication(String entity,
			JAXBElement<?> unmarshalledContent);

	/**
	 * 
	 * Through this method the event package implementation sbb has a chance to
	 * define an alternative publication value for the one expired, this can
	 * allow a behavior such as defining offline status in a presence resource.
	 * 
	 * @param publication
	 * @return
	 */
	public Publication getAlternativeValueForExpiredPublication(
			Publication publication);
	
}
