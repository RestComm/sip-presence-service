package org.mobicents.sippresence.server.pidfmanipulation;

public interface PIDFManipulationChild {

	public void newPublication(String entity, String content);
	
	public void modifyPublication(String content);
	
	public void removePublication();
	
}
