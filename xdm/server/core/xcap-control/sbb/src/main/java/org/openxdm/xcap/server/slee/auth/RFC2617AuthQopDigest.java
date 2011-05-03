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

package org.openxdm.xcap.server.slee.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.log4j.Logger;
import org.openxdm.xcap.common.error.InternalServerErrorException;

/**
 * 
 * @author martins
 *
 */
public class RFC2617AuthQopDigest {
	
	private static final Logger logger = Logger.getLogger(RFC2617AuthQopDigest.class);
	
	/**
	 * 
	 */
	private final String username;
	
	/**
	 * 
	 */
	private final String realm;
	
	/**
	 * 
	 */
	private final String password;
	
	/**
	 * 
	 */
	private final String nonce;
	
	/**
	 * 
	 */
	private final String nonceCount;
	
	/**
	 * 
	 */
	private final String cnonce;
	
	/**
	 * 
	 */
	private final String qop = "auth";
	
	/**
	 * 
	 */
	private final String method;
	
	/**
	 * 
	 */
	private final String digestUri;
	
	/**
	 * 
	 * @param username
	 * @param realm
	 * @param password
	 * @param nonce
	 * @param nonceCount
	 * @param cnonce
	 * @param qop
	 * @param method
	 * @param digestUri
	 */
	public RFC2617AuthQopDigest(String username, String realm,
			String password, String nonce, String nonceCount, String cnonce,
			String method, String digestUri) {
		this.username = username;
		this.realm = realm;
		this.password = password;
		this.nonce = nonce;
		this.nonceCount = nonceCount;
		this.cnonce = cnonce;
		this.method = method;
		this.digestUri = digestUri;
	}

	/**
	 * Calculates the encoded http digest according to RFC 2617 with "auth" qop.
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public String digest() throws InternalServerErrorException {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Calculating RFC 2617 qop=auth digest with params: username = "+username+" , realm = "+realm+" , password = "+password+" , nonce = "+nonce+" , nonceCount = "+nonceCount+" , cnonce = "+cnonce+" , method = "+method+" , digestUri = "+digestUri+" ;");
		}
		
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new InternalServerErrorException("failed to get instance of MD5 digest, used in "+this.getClass());
		}
		
		final String a1 = username + ":" + realm + ":" + password;
		final String ha1 = AsciiHexStringEncoder.encode(messageDigest.digest(EncodingUtil.getAsciiBytes(a1)));
		
		
		final String a2 = method + ":" + digestUri;
		final String ha2 = AsciiHexStringEncoder.encode(messageDigest.digest(EncodingUtil.getAsciiBytes(a2)));
		
		final String kd = ha1 + ":" + nonce + ":" + nonceCount + ":" + cnonce + ":" + qop + ":" + ha2;
		
		return AsciiHexStringEncoder.encode(messageDigest.digest(EncodingUtil.getAsciiBytes(kd)));
	}
	
}