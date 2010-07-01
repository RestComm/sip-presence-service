/**
 * 
 */
package org.mobicents.slee.sipevent.server.publication.data;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

/**
 * @author martins
 *
 */
public class JAXBContentHandler {

	private final JAXBContext jaxbContext;
	
	/**
	 * 
	 */
	public JAXBContentHandler(JAXBContext jaxbContext) {
		this.jaxbContext = jaxbContext;
	}
	
	public String marshallToString(JAXBElement<?> unmarshalledContent) {
		final StringWriter stringWriter = new StringWriter();
		try {
			jaxbContext.createMarshaller().marshal(unmarshalledContent, stringWriter);
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}
		return stringWriter.toString();
		// no need to close string writer, does nothing
	}
	
	public JAXBElement<?> unmarshallFromString(String marshalledContent) {
		try {
			return (JAXBElement<?>) jaxbContext.createUnmarshaller().unmarshal(new StringReader(marshalledContent));
		} catch (JAXBException e) {
			e.printStackTrace();
			return null; 
		}
		// no need to close string reader, does nothing
	}
	
}
