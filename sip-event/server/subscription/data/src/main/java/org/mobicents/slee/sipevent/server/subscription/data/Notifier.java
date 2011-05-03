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

package org.mobicents.slee.sipevent.server.subscription.data;

import java.io.Serializable;

public class Notifier implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String uri;
	
	private final String param; 
	
	private final boolean presList;
	
	private final boolean pocGroup;
	
	private transient String uriWithParam = null;
	
	private final static String COLON = ";";
		 
	private final static String PRES_LIST = "pres-list=";
	private final static String POC_GROUP = "poc-group=";
		
	public Notifier(String notifier) {
		String[] notifierParts = notifier.split(COLON);
		if (notifierParts.length > 1) {
			this.uri = notifierParts[0];
			for (int i=1;i<notifierParts.length;i++) {
				if (notifierParts[i].startsWith(PRES_LIST)) {
					param = notifierParts[i];
					presList = true;
					pocGroup = false;
					return;
				}
				if (notifierParts[i].startsWith(POC_GROUP)) {
					param = notifierParts[i];
					presList = false;
					pocGroup = true;
					return;
				}					
			}
			this.param = null;
		}
		else {
			this.uri = notifier;
			this.param = null;
		}
		presList = false;
		pocGroup = false;
	}
	
	public String getUri() {
		return uri;
	}
	
	public String getParam() {
		return param;
	}
	
	public String getUriWithParam() {
		if (uriWithParam == null) {
			if (param == null) {
				uriWithParam = uri;
			}
			else {
				uriWithParam = new StringBuilder(uri).append(COLON).append(param).toString();
			}
		}
		return uriWithParam;
	}
	
	public boolean isPocGroup() {
		return pocGroup;
	}
	
	public boolean isPresList() {
		return presList;
	}
	
	@Override
	public String toString() {
		return getUriWithParam();
	}
}
