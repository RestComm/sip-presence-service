package org.mobicents.slee.sipevent.server.rlscache;

import javax.slee.resource.StartActivityException;

import org.openxdm.xcap.common.uri.DocumentSelector;

public interface RLSServicesCacheSbbInterface {

	public void resourceListsUpdated(DocumentSelector documentSelector, String document);

	public void rlsServicesUpdated(DocumentSelector documentSelector, String document);
	
	public RLSServiceActivity getRLSServiceActivity(String serviceURI) throws StartActivityException;
	
	public RLSService getRLSService(String serviceURI);

}
