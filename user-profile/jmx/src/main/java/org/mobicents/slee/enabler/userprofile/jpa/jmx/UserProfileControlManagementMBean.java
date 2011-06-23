/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.slee.enabler.userprofile.jpa.jmx;

/**
 * JMX Configuration of the User Profile Control.
 * 
 * @author martins
 *
 */
public interface UserProfileControlManagementMBean {

	
	public static final String MBEAN_NAME="org.mobicents.sippresence:name=UserProfileControl";
	
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
