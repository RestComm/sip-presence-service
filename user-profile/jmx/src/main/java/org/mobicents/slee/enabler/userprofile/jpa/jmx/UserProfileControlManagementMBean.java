package org.mobicents.slee.enabler.userprofile.jpa.jmx;

/**
 * JMX Configuration of the User Profile Control.
 * 
 * @author martins
 *
 */
public interface UserProfileControlManagementMBean {

	
	public static final String MBEAN_NAME="org.mobicents.slee:userprofile=UserProfileControl";
	
	/**
	 * Adds a new user with the specified username and password.
	 * 
	 * @param username
	 * @param password
	 * @throws NullPointerException if the username is null
 	 * @throws IllegalStateException if the user already exists
	 */
	public void addUser(String username, String password) throws NullPointerException, IllegalStateException;
	
	/**
	 * 
	 * Removes the user with specified username.
	 * 
	 * @param username
	 * @return true if the user existed and was removed, false otherwise
	 * @throws NullPointerException if the username is null
	 */
	public boolean removeUser(String username) throws NullPointerException;
	
	/**
	 * Retrieves all users.
	 * 
	 * @return
	 * @throws ManagementException if an unexpected error occurred
	 */
	public String[] listUsers();

	/**
	 * Retrieves all users, separated by commas, in a single string.
	 * 
	 * @return
	 */
	public String listUsersAsString();
}
