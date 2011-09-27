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

package org.mobicents.slee.xdm.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

import javax.slee.CreateException;
import javax.slee.SbbContext;
import javax.slee.facilities.Tracer;

import org.mobicents.slee.ChildRelationExt;
import org.mobicents.slee.enabler.xdmc.XDMClientChildSbb;
import org.mobicents.xcap.client.auth.Credentials;
import org.openxdm.xcap.common.error.ConflictException;
import org.openxdm.xcap.common.error.NoParentConflictException;
import org.openxdm.xcap.common.error.RequestException;
import org.openxdm.xcap.common.uri.ParseException;
import org.openxdm.xcap.common.uri.ResourceSelector;
import org.openxdm.xcap.server.etag.ETagValidator;
import org.openxdm.xcap.server.etag.IfMatchETagValidator;
import org.openxdm.xcap.server.etag.IfNoneMatchETagValidator;
import org.openxdm.xcap.server.result.ReadResult;
import org.openxdm.xcap.server.result.WriteResult;
import org.openxdm.xcap.server.slee.RequestProcessorSbbLocalObject;

public abstract class InternalXDMClientControlSbb extends XDMClientChildSbb {

	private static Tracer tracer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#delete(java.net.URI,
	 * org.mobicents.xcap.client.auth.Credentials)
	 */
	@Override
	public void delete(URI uri, Credentials credentials) throws IOException {
		if (isLocalRequest(uri)) {
			localDelete(uri, null, credentials, null);
		} else {
			super.delete(uri, credentials);
		}
	}

	private boolean isLocalRequest(URI uri) {
		if (!uri.isAbsolute()) {
			return true;
		}
		final ServerConfiguration serverConfiguration = ServerConfiguration
				.getInstance();
		return uri.getPort() == serverConfiguration.getServerPort()
				&& uri.getHost().equals(serverConfiguration.getServerHost());
	}

	private void localDelete(URI uri, String assertedUserId,
			Credentials credentials, ETagValidator eTagValidator)
			throws IOException {

		if (tracer.isFineEnabled()) {
			tracer.fine("Local delete " + uri);
		}

		int responseCode = -1;
		String eTag = null;
		String responseContent = null;
		try {
			String user = assertedUserId;
			if (user == null && credentials != null) {
				user = credentials.getUserPrincipal().getName();
			}
			WriteResult writeResult = getRequestProcessor().delete(
					getResourceSelector(uri), eTagValidator,
					ServerConfiguration.getInstance().getXcapRoot(), user);
			responseCode = writeResult.getResponseStatus();
			eTag = writeResult.getResponseEntityTag();
		} catch (ConflictException e) {
			if (tracer.isFineEnabled()) {
				tracer.finer("Failed in local delete " + uri, e);
			}
			responseCode = e.getResponseStatus();
			responseContent = e.getResponseContent();
		} catch (RequestException e) {
			if (tracer.isFineEnabled()) {
				tracer.finer("Failed in local delete " + uri, e);
			}
			responseCode = e.getResponseStatus();
		}
		if (tracer.isInfoEnabled()) {
			if (responseCode == 200) {
				tracer.info("Local delete " + uri + ". ETag:" + eTag);
			} else {
				tracer.info("Failed in local delete " + uri + ". Response status: "
						+ responseCode + ", Response Content: "
						+ responseContent);
			}
		}
		getParent().deleteResponse(uri, responseCode, responseContent,
				eTag);
	}

	private ResourceSelector getResourceSelector(URI uri) throws IOException {
		try {
			return org.openxdm.xcap.common.uri.Parser.parseResourceSelector(
					ServerConfiguration.getInstance().getXcapRoot(), uri
							.getPath(), uri.getQuery());
		} catch (ParseException e) {
			throw new IOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#delete(java.net.URI,
	 * java.lang.String)
	 */
	@Override
	public void delete(URI uri, String assertedUserId) throws IOException {
		if (isLocalRequest(uri)) {
			localDelete(uri, assertedUserId, null, null);
		} else {
			super.delete(uri, assertedUserId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#deleteIfMatch(java.
	 * net.URI, java.lang.String, org.mobicents.xcap.client.auth.Credentials)
	 */
	@Override
	public void deleteIfMatch(URI uri, String eTag, Credentials credentials)
			throws IOException {
		if (isLocalRequest(uri)) {
			localDelete(uri, null, credentials, new IfMatchETagValidator(eTag));
		} else {
			super.deleteIfMatch(uri, eTag, credentials);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#deleteIfMatch(java.
	 * net.URI, java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteIfMatch(URI uri, String eTag, String assertedUserId)
			throws IOException {
		if (isLocalRequest(uri)) {
			localDelete(uri, assertedUserId, null, new IfMatchETagValidator(
					eTag));
		} else {
			super.deleteIfMatch(uri, eTag, assertedUserId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#deleteIfNoneMatch(java
	 * .net.URI, java.lang.String, org.mobicents.xcap.client.auth.Credentials)
	 */
	@Override
	public void deleteIfNoneMatch(URI uri, String eTag, Credentials credentials)
			throws IOException {
		if (isLocalRequest(uri)) {
			localDelete(uri, null, credentials, new IfNoneMatchETagValidator(
					eTag));
		} else {
			super.deleteIfNoneMatch(uri, eTag, credentials);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#deleteIfNoneMatch(java
	 * .net.URI, java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteIfNoneMatch(URI uri, String eTag, String assertedUserId)
			throws IOException {
		if (isLocalRequest(uri)) {
			localDelete(uri, assertedUserId, null,
					new IfNoneMatchETagValidator(eTag));
		} else {
			super.deleteIfNoneMatch(uri, eTag, assertedUserId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#get(java.net.URI,
	 * org.mobicents.xcap.client.auth.Credentials)
	 */
	@Override
	public void get(URI uri, Credentials credentials) throws IOException {
		if (isLocalRequest(uri)) {
			localGet(uri, null, credentials);
		} else {
			super.get(uri, credentials);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#get(java.net.URI,
	 * java.lang.String)
	 */
	@Override
	public void get(URI uri, String assertedUserId) throws IOException {
		if (isLocalRequest(uri)) {
			localGet(uri, assertedUserId, null);
		} else {
			super.get(uri, assertedUserId);
		}
	}

	private void localGet(URI uri, String assertedUserId,
			Credentials credentials) throws IOException {

		if (tracer.isFineEnabled()) {
			tracer.fine("Local get " + uri);
		}

		int responseCode = -1;
		String mimetype = null;
		String content = null;
		String eTag = null;
		try {
			String user = assertedUserId;
			if (user == null && credentials != null) {
				user = credentials.getUserPrincipal().getName();
			}
			ReadResult readResult = getRequestProcessor().get(
					getResourceSelector(uri), user);
			responseCode = 200;
			mimetype = readResult.getResponseDataObject().getMimetype();
			content = readResult.getResponseDataObject().toXML();
			eTag = readResult.getResponseEntityTag();
		} catch (RequestException e) {
			if (tracer.isFineEnabled()) {
				tracer.fine("Failed in local get " + uri, e);
			}
			responseCode = e.getResponseStatus();
		}
		if (tracer.isInfoEnabled()) {
			if (responseCode == 200) {
				tracer.info("Local get " + uri + ". ETag:" + eTag);
			} else {
				tracer.info("Failed in local get " + uri + ". Response status: "
						+ responseCode);
			}
		}
		getParent().getResponse(uri, responseCode, mimetype, content,
				eTag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#put(java.net.URI,
	 * java.lang.String, byte[], org.mobicents.xcap.client.auth.Credentials)
	 */
	@Override
	public void put(URI uri, String mimetype, byte[] content,
			Credentials credentials) throws IOException {
		if (isLocalRequest(uri)) {
			localPut(uri, mimetype, content, null, credentials, null);
		} else {
			super.put(uri, mimetype, content, credentials);
		}
	}

	private void localPut(URI uri, String mimetype, byte[] content,
			String assertedUserId, Credentials credentials,
			ETagValidator eTagValidator) throws IOException {

		if (tracer.isFineEnabled()) {
			tracer.fine("Local put " + uri);
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(content);
		int responseCode = -1;
		String eTag = null;
		String responseContent = null;
		try {
			String user = assertedUserId;
			if (user == null && credentials != null) {
				user = credentials.getUserPrincipal().getName();
			}
			WriteResult writeResult = getRequestProcessor().put(
					getResourceSelector(uri), mimetype, bais, eTagValidator,
					ServerConfiguration.getInstance().getXcapRoot(), user);
			responseCode = writeResult.getResponseStatus();
			eTag = writeResult.getResponseEntityTag();

		} catch (NoParentConflictException e) {
			if (tracer.isFineEnabled()) {
				tracer.fine("Failed in local put " + uri, e);
			}
			// add base uri
			e.setSchemeAndAuthorityURI(ServerConfiguration.getInstance()
					.getSchemeAndAuthority());
			responseCode = e.getResponseStatus();
			responseContent = e.getResponseContent();
		} catch (ConflictException e) {
			if (tracer.isFineEnabled()) {
				tracer.fine("Failed in local put " + uri, e);
			}
			responseCode = e.getResponseStatus();
			responseContent = e.getResponseContent();
		} catch (RequestException e) {
			if (tracer.isFineEnabled()) {
				tracer.fine("Failed in local put " + uri, e);
			}
			responseCode = e.getResponseStatus();
		} finally {
			try {
				bais.close();
			} catch (IOException e) {
				// ignore
				tracer.severe(e.getMessage(), e);
			}
		}
		if (tracer.isInfoEnabled()) {
			if (responseCode == 200 || responseCode == 201) {
				tracer.info("Local put " + uri + ". ETag:" + eTag);
			} else {
				tracer.info("Failed in local put " + uri + ". Response status: "
						+ responseCode + ", Response Content: "
						+ responseContent);
			}
		}
		getParent().putResponse(uri, responseCode, responseContent, eTag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#put(java.net.URI,
	 * java.lang.String, byte[], java.lang.String)
	 */
	@Override
	public void put(URI uri, String mimetype, byte[] content,
			String assertedUserId) throws IOException {
		if (isLocalRequest(uri)) {
			localPut(uri, mimetype, content, assertedUserId, null, null);
		} else {
			super.put(uri, mimetype, content, assertedUserId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#putIfMatch(java.net
	 * .URI, java.lang.String, java.lang.String, byte[],
	 * org.mobicents.xcap.client.auth.Credentials)
	 */
	@Override
	public void putIfMatch(URI uri, String eTag, String mimetype,
			byte[] content, Credentials credentials) throws IOException {
		if (isLocalRequest(uri)) {
			localPut(uri, mimetype, content, null, credentials,
					new IfMatchETagValidator(eTag));
		} else {
			super.putIfMatch(uri, eTag, mimetype, content, credentials);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#putIfMatch(java.net
	 * .URI, java.lang.String, java.lang.String, byte[], java.lang.String)
	 */
	@Override
	public void putIfMatch(URI uri, String eTag, String mimetype,
			byte[] content, String assertedUserId) throws IOException {
		if (isLocalRequest(uri)) {
			localPut(uri, mimetype, content, assertedUserId, null,
					new IfMatchETagValidator(eTag));
		} else {
			super.putIfMatch(uri, eTag, mimetype, content, assertedUserId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#putIfNoneMatch(java
	 * .net.URI, java.lang.String, java.lang.String, byte[],
	 * org.mobicents.xcap.client.auth.Credentials)
	 */
	@Override
	public void putIfNoneMatch(URI uri, String eTag, String mimetype,
			byte[] content, Credentials credentials) throws IOException {
		if (isLocalRequest(uri)) {
			localPut(uri, mimetype, content, null, credentials,
					new IfNoneMatchETagValidator(eTag));
		} else {
			super.putIfNoneMatch(uri, eTag, mimetype, content, credentials);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#putIfNoneMatch(java
	 * .net.URI, java.lang.String, java.lang.String, byte[], java.lang.String)
	 */
	@Override
	public void putIfNoneMatch(URI uri, String eTag, String mimetype,
			byte[] content, String assertedUserId) throws IOException {
		if (isLocalRequest(uri)) {
			localPut(uri, mimetype, content, assertedUserId, null,
					new IfNoneMatchETagValidator(eTag));
		} else {
			super.putIfNoneMatch(uri, eTag, mimetype, content, assertedUserId);
		}
	}

	public abstract ChildRelationExt getRequestProcessorChildRelation();

	private RequestProcessorSbbLocalObject getRequestProcessor()
			throws IOException {
		ChildRelationExt childRelation = getRequestProcessorChildRelation();
		RequestProcessorSbbLocalObject child = (RequestProcessorSbbLocalObject) childRelation.get(ChildRelationExt.DEFAULT_CHILD_NAME);
		if (child == null) {
			try {
				child  = (RequestProcessorSbbLocalObject) childRelation.create(ChildRelationExt.DEFAULT_CHILD_NAME);
			} catch (CreateException e) {
				throw new IOException(e);
			}
		} 
		return child;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.enabler.xdmc.XDMClientChildSbb#setSbbContext(javax
	 * .slee.SbbContext)
	 */
	@Override
	public void setSbbContext(SbbContext arg0) {
		super.setSbbContext(arg0);
		if (tracer == null) {
			tracer = arg0.getTracer(this.getClass().getSimpleName());
		}
	}
}
