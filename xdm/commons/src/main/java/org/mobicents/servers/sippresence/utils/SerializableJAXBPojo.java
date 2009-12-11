/**
 * 
 */
package org.mobicents.servers.sippresence.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * @author martins
 *
 */
public abstract class SerializableJAXBPojo<E> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private transient E pojo;
	
	/**
	 * 
	 */
	public SerializableJAXBPojo(E pojo) {
		this.pojo = pojo;
	}
	
	/**
	 * 
	 * @return
	 * @throws JAXBException 
	 */
	protected abstract JAXBContext getJAXBContext() throws JAXBException;
	
	/**
	 * 
	 * @return
	 */
	public E getPojo() {
		return pojo;
	}
	
	/**
	 * @param pojo the pojo to set
	 */
	public void setPojo(E pojo) {
		this.pojo = pojo;
	}
	
	// serialization
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		
		stream.defaultWriteObject();
		
		try {
			marshall(stream);
		} catch (JAXBException e) {
			throw new IOException(e.getMessage(),e);
		}
	}
	
	private void marshall(ObjectOutputStream stream) throws JAXBException {
		final JAXBContext context = getJAXBContext();
		final Marshaller marshaller = context.createMarshaller();
		marshaller.marshal(pojo, stream);	
	}
	
	private void readObject(ObjectInputStream stream)  throws IOException, ClassNotFoundException {
				
		stream.defaultReadObject();
		
		try {
			unmarshall(stream);
		} catch (JAXBException e) {
			throw new IOException(e.getMessage(),e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void unmarshall(ObjectInputStream stream) throws JAXBException {
		final JAXBContext context = getJAXBContext();
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		pojo = (E) unmarshaller.unmarshal(stream);
	}
	
}
