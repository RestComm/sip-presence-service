package org.mobicents.xdm.server.appusage;

import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;

/**
 * Data Source interface for app usages.
 * @author martins
 *
 */
public interface AppUsageDataSource {
	
	/**
	 * 
	 * @param documentSelector
	 * @return
	 * @throws InternalServerErrorException
	 */
	public Document getDocument(DocumentSelector documentSelector) throws InternalServerErrorException;
	
	/**
	 * 
	 * @param auid
	 * @param documentParent
	 * @return
	 * @throws InternalServerErrorException
	 */
	public Document[] getDocuments(String auid, String documentParent) throws InternalServerErrorException;
	
	/**
	 * 
	 * @param auid
	 * @return
	 * @throws InternalServerErrorException
	 */
	public Document[] getDocuments(String auid) throws InternalServerErrorException;
	
}
