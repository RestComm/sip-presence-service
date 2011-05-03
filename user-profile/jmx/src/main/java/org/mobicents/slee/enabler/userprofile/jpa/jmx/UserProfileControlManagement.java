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

import java.util.ArrayList;
import java.util.Arrays;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.mobicents.slee.enabler.userprofile.jpa.UserProfile;

/**
 * Management interface for user profiles stored on JPA.
 * 
 * @author martins
 * 
 */
public class UserProfileControlManagement implements
		UserProfileControlManagementMBean {

	private static final Logger logger = Logger
			.getLogger(UserProfileControlManagement.class);

	private TransactionManager txMgr;
	
	/**
	 * the jpa entity manager factory to manage user profiles
	 */
	private EntityManagerFactory entityManagerFactory;

	private static final UserProfileControlManagement INSTANCE = new UserProfileControlManagement();
	
	public static UserProfileControlManagement getInstance() {
		return INSTANCE;
	}
	
	private UserProfileControlManagement() {}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public void start() {

		try {
			txMgr = (TransactionManager) new InitialContext().lookup("java:/TransactionManager");
		} catch (NamingException e) {
			throw new RuntimeException(e.getMessage(),e);
		}

		entityManagerFactory = Persistence
			.createEntityManagerFactory("mobicents-slee-enabler-userprofile-pu");

		logger.info("Service started.");
	}

	/**
	 * 
	 */
	public void stop() {
		
		entityManagerFactory.close();
		
		logger.info("Service stopped.");
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.xdm.server.userprofile.jmx.
	 * UserProfileControlManagementMBean#addUser(java.lang.String,
	 * java.lang.String)
	 */
	public void addUser(String username, String password)
			throws NullPointerException, IllegalStateException {

		if (logger.isDebugEnabled()) {
			logger.debug("addUser( username = "+username+")");
		}
		
		if (username == null)
			throw new NullPointerException("null username");

		if (getUser(username) != null) {
			throw new IllegalStateException("user "+username+" already exists");
		}
		
		EntityManager entityManager = null;
		
		Transaction tx= null;
		
		try {
			
			if (txMgr.getTransaction() != null) {
				tx = txMgr.suspend();
			}
			
			txMgr.begin();
			
			entityManager = entityManagerFactory.createEntityManager();		
			
			UserProfile userProfile = new UserProfile(username);
			userProfile.setPassword(password);

			entityManager.persist(userProfile);
			
			txMgr.commit();
						
			if (logger.isInfoEnabled()) {
				logger.info("Added user "+username);
			}

		} catch (Throwable e) {
			try {
				txMgr.rollback();							
			} catch (Throwable f) {
				logger.error(f.getMessage(),f);
			}		
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (tx != null) {
				try {
					txMgr.resume(tx);			
				} catch (Throwable f) {
					logger.error(f.getMessage(),f);
				}
			}
			if (entityManager != null) {
				entityManager.close();
			}
				
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.xdm.server.userprofile.jmx.
	 * UserProfileControlManagementMBean#getUsers()
	 */
	public String[] listUsers() {

		if (logger.isDebugEnabled()) {
			logger.debug("listUsers()");
		}
		
		EntityManager entityManager = null;
		try {
			entityManager = entityManagerFactory.createEntityManager();

			ArrayList<String> resultList = new ArrayList<String>();
			for (Object result : entityManager.createNamedQuery(
					UserProfile.JPA_NAMED_QUERY_SELECT_ALL_USERPROFILES)
					.getResultList()) {
				UserProfile userProfile = (UserProfile) result;
				resultList.add(userProfile.getUsername());
			}

			String[] resultArray = new String[resultList.size()];
			return resultList.toArray(resultArray);
		
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.enabler.userprofile.jpa.jmx.UserProfileControlManagementMBean#listUsersAsString()
	 */
	public String listUsersAsString() {
		return Arrays.asList(listUsers()).toString();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.xdm.server.userprofile.jmx.
	 * UserProfileControlManagementMBean#removeUser(java.lang.String)
	 */
	public boolean removeUser(String username)
			throws NullPointerException {

		if (logger.isDebugEnabled()) {
			logger.debug("removeUser( username = "+username+" )");
		}
		
		if (username == null)
			throw new NullPointerException("null username");

		EntityManager entityManager = null;
		
		Transaction tx= null;
		
		try {
			
			if (txMgr.getTransaction() != null) {
				tx = txMgr.suspend();
			}
			
			txMgr.begin();
			
			entityManager = entityManagerFactory.createEntityManager();

			UserProfile userProfile = entityManager.find(UserProfile.class,username);

			boolean exists = userProfile != null;
			if (exists) {
				entityManager.remove(userProfile);
				if (logger.isInfoEnabled()) {
					logger.info("Removed user "+username);
				}
			}

			txMgr.commit();
			
			return exists;
		} catch (Throwable e) {
			try {
				txMgr.rollback();			
			} catch (Throwable f) {
				logger.error(f.getMessage(),f);
			}
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (tx != null) {
				try {
					txMgr.resume(tx);			
				} catch (Throwable f) {
					logger.error(f.getMessage(),f);
				}
			}
			if (entityManager != null) {
				entityManager.close();
			}
		}
	}

	/**
	 * Retrieves the user profile for the specified username.
	 * 
	 * @param username
	 * @return
	 * @throws NullPointerException
	 */
	public UserProfile getUser(String username)
			throws NullPointerException {

		if (logger.isDebugEnabled()) {
			logger.debug("getUser( username = "+username+" )");
		}
		
		if (username == null)
			throw new NullPointerException("null username");
		
		EntityManager entityManager = entityManagerFactory
				.createEntityManager();

		UserProfile userProfile = entityManager.find(UserProfile.class,username);

		entityManager.close();

		return userProfile;
	}

}
