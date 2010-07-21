package org.mobicents.slee.sipevent.server.rlscache;

public class ListReference {

	public enum Status { RESOLVING, BAD_GATEWAY, OK }
	
	private final ListReferenceFrom from;
	
	private final ListReferenceFrom to;
	
	public ListReference(ListReferenceFrom from, ListReferenceFrom to) {
		this.from = from;
		this.to = to;
	}
	
	public ListReferenceFrom getFrom() {
		return from;
	}
	
	public ListReferenceFrom getTo() {
		return to;
	}
	
	@Override
	public int hashCode() {
		return from.hashCode() * 31 + to.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListReference other = (ListReference) obj;
		if (!from.equals(other.from))
			return false;
		if (!to.equals(other.to))
			return false; 
		return true;
	}
		
}
