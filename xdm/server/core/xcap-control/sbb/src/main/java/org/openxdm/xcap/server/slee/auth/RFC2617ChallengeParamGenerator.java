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
import java.security.SecureRandom;

import org.apache.commons.httpclient.util.EncodingUtil;
import org.openxdm.xcap.common.error.InternalServerErrorException;

public class RFC2617ChallengeParamGenerator {

	public String getNonce(String seed) throws InternalServerErrorException {
		if (nonceDigestSecret == null) {
			nonceDigestSecret = generateOpaque();
		}
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new InternalServerErrorException("failed to get instance of MD5 digest, used in "+RFC2617AuthQopDigest.class.getName());
		}
		return AsciiHexStringEncoder.encode(messageDigest.digest(EncodingUtil.getAsciiBytes(seed+":"+nonceDigestSecret)));
	}

	private String nonceDigestSecret;
	
	private final SecureRandom opaqueGenerator = new SecureRandom();

	public String generateOpaque() {
		synchronized (opaqueGenerator) {
			return Integer.toHexString(opaqueGenerator.nextInt());
		}
	}
}
