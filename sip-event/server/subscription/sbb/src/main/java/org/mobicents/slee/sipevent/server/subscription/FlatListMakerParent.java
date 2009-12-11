/**
 * 
 */
package org.mobicents.slee.sipevent.server.subscription;

import org.mobicents.slee.sipevent.server.subscription.eventlist.FlatList;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ListType;

/**
 * @author martins
 *
 */
public interface FlatListMakerParent {

	/**
	 * provides the {@link FlatList} of {@link EntryType}s, which is the result of flattening a {@link ListType}
	 * @param flatList
	 */
	public void flatListMade(FlatList flatList);
	
}
