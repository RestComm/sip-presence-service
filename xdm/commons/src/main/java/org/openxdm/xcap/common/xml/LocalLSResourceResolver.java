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

package org.openxdm.xcap.common.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Scanner;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class LocalLSResourceResolver implements LSResourceResolver {

	private URI sourceDir;

	public LocalLSResourceResolver(URI sourceDir) {
		this.sourceDir = sourceDir;
	}

	@Override
	public LSInput resolveResource(String type, String namespaceURI,
			String publicId, String systemId, String baseURI) {

		if (type != null && type.equals("http://www.w3.org/2001/XMLSchema")) {
			int i = systemId.lastIndexOf('/');
			String resourceFileName = null;
			if (i < systemId.length() - 1) {
				resourceFileName = systemId.substring(i + 1);
			} else {
				resourceFileName = systemId;
			}
			File file = new File(new File(sourceDir), resourceFileName);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
			String stringData = new Scanner(fis).useDelimiter("\\A").next();
			LSInputImpl lsImpl = new LSInputImpl();
			lsImpl.baseURI = baseURI;
			lsImpl.publicId = publicId;
			lsImpl.stringData = stringData;
			lsImpl.systemId = systemId;
			return lsImpl;
		}
		return null;
	}

	private static class LSInputImpl implements LSInput {

		private String publicId, systemId, baseURI, stringData;

		@Override
		public String getBaseURI() {
			return baseURI;
		}

		@Override
		public InputStream getByteStream() {
			return null;
		}

		@Override
		public boolean getCertifiedText() {
			return false;
		}

		@Override
		public Reader getCharacterStream() {
			return null;
		}

		@Override
		public String getEncoding() {
			return null;
		}

		@Override
		public String getPublicId() {
			return publicId;
		}

		@Override
		public String getStringData() {
			return stringData;
		}

		@Override
		public String getSystemId() {
			return systemId;
		}

		@Override
		public void setBaseURI(String arg0) {
			this.baseURI = arg0;
		}

		@Override
		public void setByteStream(InputStream arg0) {

		}

		@Override
		public void setCertifiedText(boolean arg0) {

		}

		@Override
		public void setCharacterStream(Reader arg0) {

		}

		@Override
		public void setEncoding(String arg0) {

		}

		@Override
		public void setPublicId(String arg0) {
			this.publicId = arg0;
		}

		@Override
		public void setStringData(String arg0) {
			this.stringData = arg0;
		}

		@Override
		public void setSystemId(String arg0) {
			this.systemId = arg0;
		}

	}

}