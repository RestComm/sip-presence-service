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

package org.openxdm.xcap.server.slee.resource.datasource;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.mobicents.xdm.common.util.uri.UriComponentEncoder;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.NodeSelector;

public class NodeSubscription implements Externalizable {

	private transient String sel;
	
	private DocumentSelector documentSelector;
	private NodeSelector nodeSelector;
	
	public NodeSubscription() {
		// needed by externalizable
	}
	
	public DocumentSelector getDocumentSelector() {
		return documentSelector;
	}
	
	public NodeSubscription setDocumentSelector(DocumentSelector documentSelector) {
		this.documentSelector = documentSelector;
		return this;
	}
	
	public NodeSelector getNodeSelector() {
		return nodeSelector;
	}
	
	public NodeSubscription setNodeSelector(NodeSelector nodeSelector) {
		this.nodeSelector = nodeSelector;
		return this;
	}
	
	public String getSel() {
		if (sel == null) {
			sel = new StringBuilder(UriComponentEncoder.encodePath(documentSelector.toString())).append("/~~/").append(UriComponentEncoder.encodePath(nodeSelector.toString())).toString();
		}
		return sel;
	}
	
	public NodeSubscription setSel(String sel) {
		this.sel = sel;
		return this;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(documentSelector);
		out.writeObject(nodeSelector);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		documentSelector = (DocumentSelector) in.readObject();
		nodeSelector = (NodeSelector) in.readObject();		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((documentSelector == null) ? 0 : documentSelector.hashCode());
		result = prime * result
				+ ((nodeSelector == null) ? 0 : nodeSelector.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeSubscription other = (NodeSubscription) obj;
		if (documentSelector == null) {
			if (other.documentSelector != null)
				return false;
		} else if (!documentSelector.equals(other.documentSelector))
			return false;
		if (nodeSelector == null) {
			if (other.nodeSelector != null)
				return false;
		} else if (!nodeSelector.equals(other.nodeSelector))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new StringBuilder("NodeSubscription[ds=").append(documentSelector.toString()).append(",ns=").append(nodeSelector).append("]").toString();
	}
	
}
