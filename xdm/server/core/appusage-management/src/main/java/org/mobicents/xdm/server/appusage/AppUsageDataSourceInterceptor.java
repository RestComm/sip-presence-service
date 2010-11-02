package org.mobicents.xdm.server.appusage;

import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;

/**
 * The data source interceptor allows retrieval of dom docs that are built on
 * request.
 * 
 * @author martins
 * 
 */
public interface AppUsageDataSourceInterceptor {

	/**
	 * 
	 * @param documentSelector
	 * @param dataSource
	 * @return
	 * @throws InternalServerErrorException
	 */
	public InterceptedDocument getDocument(DocumentSelector documentSelector,
			AppUsageDataSource dataSource) throws InternalServerErrorException;

	/**
	 * 
	 * @param documentParent
	 * @param dataSource
	 * @return
	 * @throws InternalServerErrorException
	 */
	public InterceptedDocument[] getDocuments(String documentParent,
			AppUsageDataSource dataSource) throws InternalServerErrorException;

	/**
	 * 
	 * @param dataSource
	 * @return
	 * @throws InternalServerErrorException
	 */
	public InterceptedDocument[] getDocuments(AppUsageDataSource dataSource)
			throws InternalServerErrorException;

}
