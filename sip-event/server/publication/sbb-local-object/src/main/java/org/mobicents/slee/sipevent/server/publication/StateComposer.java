/**
 * 
 */
package org.mobicents.slee.sipevent.server.publication;

/**
 * @author martins
 *
 */
public interface StateComposer {

	/**
	 * Combines 2 states, represent by unmarshalled JAXB element values.
	 * 
	 * @param state1
	 * @param state2
	 * @return
	 */
	public Object compose(Object state1, Object state2);
	
}
