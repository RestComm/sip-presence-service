package org.mobicents.slee.sipevent.server.subscription.eventlist;

import javax.xml.bind.JAXBElement;

import org.openxdm.xcap.client.appusage.rlsservices.jaxb.PackagesType;

public class ServiceTypePackageVerifier {

	public boolean hasPackage(PackagesType packagesType, String eventPackage) {		
		for (Object obj : packagesType.getPackageAndAny()) {
			JAXBElement<?> element = (JAXBElement<?>) obj;
			if (element.getName().getLocalPart().equals("package") && element.getValue().equals(eventPackage)) {
				return true;
			}
		}
		return false;
	}
	
}
