package org.mobicents.slee.sipevent.server.subscription.data;

import java.io.Serializable;

import javax.slee.facilities.TimerID;

/**
 * 	Subscription: A subscription is a set of application state associated
 *     with a dialog.  This application state includes a pointer to the
 *     associated dialog, the event package name, and possibly an
 *     identification token.  Event packages will define additional
 *     subscription state information.  By definition, subscriptions
 *     exist in both a subscriber and a notifier.
 *  
 *     This class is JPA pojo for a subscription.
 *     
 * @author eduardomartins
 *
 */
public class Subscription implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8020033417766370446L;
	
	/**
	 * the subscription key
	 */   
	private final SubscriptionKey key;
		
	/**
	 * the subscriber
	 */
	private final String subscriber;
	
	/**
	 * the notifier
	 */
	private final Notifier notifier;
	
	/**
	 * the current status of the subscription
	 */
	public enum Status {
		active, 
		pending, 
		waiting, 
		terminated;
	}
	private Status status;
	
	/**
	 * the date when this subscription was created
	 */
	private final long creationDate; // subscription's date of creation

	/**
	 * the last time this subscription was refreshed
	 */
	private long lastRefreshDate; // last time this subscription was refreshed

	/**
	 * seconds to expire the subscription, after creation/last refresh
	 */
	private int expires; // seconds to expire

	/**
	 * display name of the subscriber
	 */
	private final String subscriberDisplayName; // the display name of the subscriber in a sip header
	
	/**
	 * last event that occurred in the subscription
	 */
	public enum Event {
		noresource,
		rejected,
		deactivated,
		probation,
		timeout,
		approved,
		giveup,
		subscribe
	}
	
	private Event lastEvent;
	
	/**
	 * the id of the SLEE timer associated with this subscription
	 */
	private TimerID timerID;
	
	/**
	 * the version of the last content, this only applies to WInfo or RLS subscriptions
	 */
	private int version;
	
	/**
	 * if true the subscription if for a resource list
	 */
	private boolean resourceList;
	
	private transient SubscriptionControlDataSource dataSource;
	
	private transient boolean created = false;
	
	/**
	 * 
	 * @param key
	 * @param subscriber
	 * @param notifier
	 * @param status
	 * @param subscriberDisplayName
	 * @param expires
	 * @param resourceList
	 */
	public Subscription(SubscriptionKey key, String subscriber, Notifier notifier, Status status, String subscriberDisplayName, int expires, boolean resourceList, SubscriptionControlDataSource dataSource) {
		this.key = key;
		this.subscriber = subscriber;
		this.notifier = notifier;
		this.status = status;
		if (status.equals(Status.active)){
			this.lastEvent = Event.approved;
		}
		else if (status.equals(Status.pending)) {
			this.lastEvent = Event.subscribe;
		}
		this.creationDate = System.currentTimeMillis();
		this.created = true;
		this.lastRefreshDate = creationDate;		
		this.expires = expires;
		this.subscriberDisplayName = subscriberDisplayName;
		this.version = 0;
		this.resourceList = resourceList;
		this.dataSource = dataSource;
	}
	
	public int getRemainingExpires() {
		long remainingExpires = expires - (System.currentTimeMillis()-lastRefreshDate) / 1000;		
		if (remainingExpires < 0) {
			return 0;
		}
		else {
			return (int) remainingExpires;
		}
	}

	public int getSubscriptionDuration() {
		return (int) ((System.currentTimeMillis() - creationDate) / 1000);
	}
	
	public void refresh(int expires) {
		lastRefreshDate = System.currentTimeMillis();
		this.expires = expires;
	}
	
	public boolean changeStatus(Event event) {

		// implements subscription state machine
		Status oldStatus = status;
		if (status == Status.active) {
			if (event == Event.noresource || event == Event.rejected || event == Event.deactivated || event == Event.probation || event == Event.timeout) {
				status = Status.terminated;
			}
		}

		else if (status == Status.pending) {
			if (event == Event.approved) {
				status = Status.active;
			}
			else if (event == Event.timeout) {
				status = Status.waiting;
			}
			else if (event == Event.noresource || event == Event.rejected || event == Event.deactivated || event == Event.probation || event == Event.giveup) {
				status = Status.terminated;
			}
		}		

		else if (status == Status.waiting) {
			if (event == Event.noresource || event == Event.rejected || event == Event.giveup || event == Event.approved) {
				status = Status.terminated;
			}
		}

		if (status != oldStatus) {
			lastEvent = event;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			return ((Subscription)obj).key.equals(this.key);
		}
		else {
			return false;
		}
	}

	// -- GETTERS AND SETTERS
	
	public SubscriptionKey getKey() {
		return key;
	}

	public String getSubscriber() {
		return subscriber;
	}

	public Notifier getNotifier() {
		return notifier;
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public long getCreationDate() {
		return creationDate;
	}

	public long getLastRefreshDate() {
		return lastRefreshDate;
	}

	public void setLastRefreshDate(long lastRefreshDate) {
		this.lastRefreshDate = lastRefreshDate;
	}

	public int getExpires() {
		return expires;
	}

	public void setExpires(int expires) {
		this.expires = expires;
	}

	public String getSubscriberDisplayName() {
		return subscriberDisplayName;
	}

	public Event getLastEvent() {
		return lastEvent;
	}

	public void setLastEvent(Event lastEvent) {
		this.lastEvent = lastEvent;
	}

	public TimerID getTimerID() {
		return timerID;
	}

	public void setTimerID(TimerID timerID) {
		if (this.timerID != null) {
			dataSource.removeTimerReference(this.timerID);
		}
		this.timerID = timerID;
		dataSource.addTimerReference(key, this.timerID);		
	}
	
	public int getVersion() {
		return version;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	public void incrementVersion() {
		this.version++;
	}
	
	public boolean getResourceList() {
		return resourceList;
	}
	
	public void setResourceList(boolean resourceList) {
		this.resourceList = resourceList;
	}
	
	@Override
	public String toString() {
		return "subscription: subscriber="+subscriber+",notifier="+notifier+",resourceList="+resourceList+",eventPackage="+key.getEventPackage()+",eventId="+key.getEventId()+",status="+status;
	}

	public SubscriptionControlDataSource getDataSource() {
		return dataSource;
	}
	
	void setDataSource(SubscriptionControlDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void store() {
		if (created) {
			dataSource.add(this);
			created = false;
		}
		else {
			dataSource.update(this);
		}
	}
	
	public void remove() {
		dataSource.remove(this);
	}
	
}