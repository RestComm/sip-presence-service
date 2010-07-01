/**
 * 
 */
package org.mobicents.slee.sipevent.server.publication;

/**
 * @author martins
 *
 */
public interface PublicationClientControl {

	/**
	 * Creates a new publication for the specified Entity and SIP Event Package.
	 * 
	 * @param entity
	 * @param eventPackage
	 * @param document
	 * @param contentType
	 * @param contentSubType
	 * @param expires
	 *            the time in seconds, which the publication is valid
	 * @param callback
	 * @return             
	 */
	public Result newPublication(String entity,
			String eventPackage, String document, String contentType,
			String contentSubType, int expires);

	/**
	 * Refreshes the publication identified by the specified Entity, SIP Event
	 * Package and ETag.
	 * 
	 * @param entity
	 * @param eventPackage
	 * @param eTag
	 * @param expires
	 *            the time in seconds, which the publication is valid
	 * @param callback
	 * @return
	 */
	public Result refreshPublication(String entity,
			String eventPackage, String eTag, int expires);

	/**
	 * Modifies the publication identified by the specified Entity, SIP Event
	 * Package and ETag.
	 *
	 * @param entity
	 * @param eventPackage
	 * @param eTag
	 * @param document
	 * @param contentType
	 * @param contentSubType
	 * @param expires
	 *            the time in seconds, which the publication is valid
	 * @param callback
	 * @return
	 */
	public Result modifyPublication(String entity,
			String eventPackage, String eTag, String document,
			String contentType, String contentSubType, int expires);

	/**
	 * Removes the publication identified by the specified Entity, SIP Event
	 * Package and ETag.
	 * 
	 * @param entity
	 * @param eventPackage
	 * @param eTag
	 * @param callback
	 * @return status code for the response
	 */
	public int removePublication(String entity,
			String eventPackage, String eTag);

}
