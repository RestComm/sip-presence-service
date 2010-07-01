/**
 * 
 */
package org.mobicents.slee.sipevent.server.subscription.eventlist;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.mobicents.servers.sippresence.utils.SerializableJAXBPojo;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.ServiceType;

/**
 * @author martins
 *
 */
public class SerializableServiceType extends SerializableJAXBPojo<ServiceType>{

	public static JAXBContext context;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servers.sippresence.utils.SerializableJAXBPojo#getJAXBContext()
	 */
	@Override
	protected JAXBContext getJAXBContext() throws JAXBException {
		if (context == null) {
			context  = JAXBContext.newInstance(ServiceType.class);
		}
		return context;
	}

	/**
	 * 
	 */
	public SerializableServiceType(ServiceType pojo) {
		super(pojo);
	}
	
}
