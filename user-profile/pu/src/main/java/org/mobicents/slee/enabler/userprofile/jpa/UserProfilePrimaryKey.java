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

package org.mobicents.slee.enabler.userprofile.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * 
 * @author eduardomartins
 * 
 */

@Embeddable
public class UserProfilePrimaryKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6638892043798746768L;

	@Column(name = "USERNAME", nullable = false)
	private String username;

	@Column(name = "REALM", nullable = false)
	private String realm;
	
	public UserProfilePrimaryKey() {
		// TODO Auto-generated constructor stub
	}
	
	public UserProfilePrimaryKey(String username, String domain) {
		setUsername(username);
		setRealm(domain);
	}

	// -- GETTERS AND SETTERS

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			UserProfilePrimaryKey other = (UserProfilePrimaryKey) obj;
			return this.username.equals(other.username)
					&& this.realm.equals(other.realm);
		} else {
			return false;
		}
	}

	public int hashCode() {
		int result;
		result = username.hashCode();
		result = 31 * result + realm.hashCode();
		return result;
	}

	public String toString() {
		return "UserProfilePrimaryKey: username = " + username
				+ " , realm = " + realm;
	}

}