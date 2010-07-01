/**
 * 
 */
package org.mobicents.slee.sipevent.server.publication;

/**
 * The result for a publication related operation.
 * @author martins
 *
 */
public class Result {

	private final int statusCode;
	private final String eTag;
	private final int expires;
	
	/**
	 * @param statusCode
	 * @param eTag
	 */
	public Result(int statusCode, String eTag, int expires) {
		this.statusCode = statusCode;
		this.eTag = eTag;
		this.expires = expires;
	}
	
	/**
	 * @param statusCode
	 */
	public Result(int statusCode) {
		this.statusCode = statusCode;
		this.eTag = null;
		this.expires = -1;
	}
	
	/**
	 * @return the eTag
	 */
	public String getETag() {
		return eTag;
	}
	
	/**
	 * @return the expires
	 */
	public int getExpires() {
		return expires;
	}
	
	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}		
	
}
