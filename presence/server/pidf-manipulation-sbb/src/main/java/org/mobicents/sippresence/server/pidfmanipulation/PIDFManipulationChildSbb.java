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

package org.mobicents.sippresence.server.pidfmanipulation;

import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.facilities.Tracer;

import org.mobicents.slee.ChildRelationExt;
import org.mobicents.slee.SbbContextExt;
import org.mobicents.slee.sipevent.server.publication.PublicationClientControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.publication.Result;

public abstract class PIDFManipulationChildSbb implements Sbb, PIDFManipulationChild {

	private static final String EVENT_PACKAGE = "presence";
	private static final String CONTENT_TYPE = "application";
	private static final String CONTENT_SUBTYPE = "pidf+xml";
	private static final int EXPIRES = -1;

	private static Tracer tracer;
	private SbbContextExt sbbContextExt;

	// ------- sbb logic

	@Override
	public void modifyPublication(String content) {

		final String entity = getEntity();

		if (tracer.isFineEnabled()) {
			tracer.fine("Updating pidf manipulation presence state for entity "
					+ entity);
		}

		String eTag = getETag();
		PublicationClientControlSbbLocalObject childSbb = ((PublicationClientControlSbbLocalObject) getChildRelation()
				.get(ChildRelationExt.DEFAULT_CHILD_NAME));
		Result result = null;
		if (eTag != null) {
			result = childSbb.modifyPublication(entity, EVENT_PACKAGE, eTag,
					content, CONTENT_TYPE, CONTENT_SUBTYPE, EXPIRES);
		} else {
			result = childSbb.newPublication(entity, EVENT_PACKAGE, content,
					CONTENT_TYPE, CONTENT_SUBTYPE, EXPIRES);
		}
		setETag(result.getETag());

		if (tracer.isInfoEnabled()) {
			tracer.info("Updated pidf manipulation presence state for entity "
					+ entity + ". ETag = " + result.getETag());
		}
	}

	@Override
	public void newPublication(String entity, String content) {

		if (tracer.isFineEnabled()) {
			tracer.fine("Publishing pidf manipulation presence state for entity "
					+ entity);
		}

		PublicationClientControlSbbLocalObject childSbb = null;
		try {
			childSbb = (PublicationClientControlSbbLocalObject) getChildRelation()
					.create(ChildRelationExt.DEFAULT_CHILD_NAME);
		} catch (Throwable e) {
			tracer.severe("failed to created child sbb", e);
			return;
		}

		final Result result = childSbb.newPublication(entity, EVENT_PACKAGE,
				content, CONTENT_TYPE, CONTENT_SUBTYPE, EXPIRES);
		setETag(result.getETag());

		if (tracer.isInfoEnabled()) {
			tracer.info("Published pidf manipulation presence state for entity "
					+ entity + ". ETag = " + result.getETag());
		}
	}

	@Override
	public void removePublication() {

		final String entity = getEntity();

		if (tracer.isFineEnabled()) {
			tracer.fine("Removing pidf manipulation presence state for entity "
					+ entity);
		}

		String eTag = getETag();
		if (eTag != null) {
			PublicationClientControlSbbLocalObject childSbb = ((PublicationClientControlSbbLocalObject) getChildRelation()
					.get(ChildRelationExt.DEFAULT_CHILD_NAME));
			childSbb.removePublication(entity, EVENT_PACKAGE, eTag);
			childSbb.remove();
		}

		if (tracer.isInfoEnabled()) {
			tracer.info("Removed pidf manipulation presence state for entity "
					+ entity);
		}
	}

	private String getEntity() {
		// entity is first part of the sbb local object name
		return sbbContextExt.getSbbLocalObject().getName().split("/")[0];
	}

	// -------- cmp fields

	public abstract String getETag();

	public abstract void setETag(String etag);

	// -------- child relation

	public abstract ChildRelationExt getChildRelation();

	// -------- sbb interface

	@Override
	public void sbbActivate() {

	}

	@Override
	public void sbbCreate() throws CreateException {

	}

	@Override
	public void sbbExceptionThrown(Exception arg0, Object arg1,
			ActivityContextInterface arg2) {

	}

	@Override
	public void sbbLoad() {

	}

	@Override
	public void sbbPassivate() {

	}

	@Override
	public void sbbPostCreate() throws CreateException {

	}

	@Override
	public void sbbRemove() {

	}

	@Override
	public void sbbRolledBack(RolledBackContext arg0) {

	}

	@Override
	public void sbbStore() {

	}

	@Override
	public void setSbbContext(SbbContext sbbContext) {
		sbbContextExt = (SbbContextExt) sbbContext;
		if (tracer == null) {
			tracer = sbbContext.getTracer(getClass().getSimpleName());
		}
	}

	@Override
	public void unsetSbbContext() {
		sbbContextExt = null;
	}

}
