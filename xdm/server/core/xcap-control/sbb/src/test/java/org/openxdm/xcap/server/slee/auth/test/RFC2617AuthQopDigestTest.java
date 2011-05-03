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

package org.openxdm.xcap.server.slee.auth.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.server.slee.auth.RFC2617AuthQopDigest;

public class RFC2617AuthQopDigestTest {

	/**
	 * Tests if {@link RFC2617AuthQopDigest} outputs same result as the code in
	 * RFC 2617 Section 5.
	 * 
	 * @throws InternalServerErrorException
	 */
	@Test
	public void test() throws InternalServerErrorException {

		String username = "Mufasa";
		String realm = "testrealm@host.com";
		String password = "Circle Of Life";

		String nonce = "dcd98b7102dd2f0e8b11d0f600bfb0c093";
		String nonceCount = "00000001";
		String cnonce = "0a4f113b";

		String method = "GET";
		String digestUri = "/dir/index.html";

		RFC2617AuthQopDigest digestProcessor = new RFC2617AuthQopDigest(
				username, realm, password, nonce, nonceCount, cnonce,
				method, digestUri);

		String digest = digestProcessor.digest();

		assertTrue(
				"Digest must not be nul and must have same value as the Response param in section 3.5 of RFC 2617, 6629fae49393a05397450978507c4ef1",
				digest != null
						&& digest.equals("6629fae49393a05397450978507c4ef1"));

	}

}
