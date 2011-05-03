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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *     
 * @author eduardomartins
 *
 */
@Entity
@Table(name = "MOBICENTS_SLEE_ENABLER_USERPROFILES")
@NamedQueries({
	@NamedQuery(name=UserProfile.JPA_NAMED_QUERY_SELECT_ALL_USERPROFILES,query="SELECT p FROM UserProfile p")
	})
public class UserProfile implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3697052553779974529L;

	private static final String JPA_NAMED_QUERY_PREFIX = "MSPS_UP_NQUERY_";
	public static final String JPA_NAMED_QUERY_SELECT_ALL_USERPROFILES = JPA_NAMED_QUERY_PREFIX + "selectAllUserProfiles";
	
	@Id
	@Column(name = "USERNAME", nullable = false)
	private String username;
	
	/**
	 * the user password
	 */
	@Column(name = "PASSWORD", nullable = true)
	private String password;
	
	public UserProfile() {
		// TODO Auto-generated constructor stub
	}
	
	public UserProfile(String username) {
		setUsername(username);
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Override
	public int hashCode() {
		return username.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			UserProfile other = (UserProfile) obj;
			return other.username.equals(this.username);
		}
		else {
			return false;
		}
	}

	// -- GETTERS AND SETTERS
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}