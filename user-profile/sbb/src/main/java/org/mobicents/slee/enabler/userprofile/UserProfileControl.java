/**
 * 
 */
package org.mobicents.slee.enabler.userprofile;

/**
 * @author martins
 *
 */
public interface UserProfileControl {

	/**
	 * 
	 * @param username
	 * @return
	 */
	public UserProfile find(String username);
	
}
