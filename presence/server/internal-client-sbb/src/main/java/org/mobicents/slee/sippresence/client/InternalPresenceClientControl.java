/**
 * 
 */
package org.mobicents.slee.sippresence.client;

import org.mobicents.slee.sipevent.server.publication.PublicationClientControlParent;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParent;

/**
 * @author martins
 *
 */
public interface InternalPresenceClientControl extends PresenceClientControl, PublicationClientControlParent, SubscriptionClientControlParent {

}
