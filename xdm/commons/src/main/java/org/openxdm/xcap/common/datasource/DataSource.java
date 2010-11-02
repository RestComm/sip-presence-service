package org.openxdm.xcap.common.datasource;

import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;

/**
 * TODO
 * Important: Implementations must be thread safe!
 * @author Eduardo Martins
 *
 */
public interface DataSource {

	/**
	 * Open the datasource, enables all required resources. 
	 * @throws InternalServerErrorException
	 */
	public void open() throws InternalServerErrorException;
	
	/**
	 * Closes the datasource, disables all required resources. 
	 * @throws InternalServerErrorException
	 */
	public void close() throws InternalServerErrorException;
	
	/**
	 * 
	 * @param documentSelector
	 * @return
	 * @throws InternalServerErrorException
	 */
	public Document getDocument(DocumentSelector documentSelector) throws InternalServerErrorException;

	/**
	 * 
	 * @param documentSelector
	 * @param eTag
	 * @param xml
	 * @param document
	 * @throws InternalServerErrorException
	 */
	public void createDocument(DocumentSelector documentSelector,String eTag, String xml, org.w3c.dom.Document document) throws InternalServerErrorException;

	/**
	 * 
	 * @param documentSelector
	 * @param oldETag
	 * @param newETag
	 * @param documentAsString
	 * @param document
	 * @throws InternalServerErrorException
	 */
	public void updateDocument(DocumentSelector documentSelector,
			String oldETag, String newETag, String documentAsString,
			org.w3c.dom.Document document) throws InternalServerErrorException;
	
	/**
	 * 
	 * @param documentSelector
	 * @param oldETag
	 * @throws InternalServerErrorException
	 */
	public void deleteDocument(DocumentSelector documentSelector, String oldETag) throws InternalServerErrorException;
	
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
