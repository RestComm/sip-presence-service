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

package org.mobicents.slee.sipevent.server.publication;

import javax.slee.facilities.Tracer;

public class SLEEPublicationControlLogger implements PublicationControlLogger {

	private final Tracer tracer;
	
	public SLEEPublicationControlLogger(Tracer tracer) {
		this.tracer = tracer;
	}
	
	@Override
	public boolean isDebugEnabled() {
		return tracer.isFineEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return tracer.isInfoEnabled();
	}

	@Override
	public void debug(String msg) {
		tracer.fine(msg);
	}

	@Override
	public void info(String msg) {
		tracer.info(msg);
	}

	@Override
	public void warn(String msg) {
		tracer.warning(msg);
	}

	@Override
	public void error(String msg, Throwable t) {
		tracer.severe(msg,t);
	}

}
