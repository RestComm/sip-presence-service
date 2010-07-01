package org.mobicents.slee.sippresence.client;

import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.ChildRelation;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.facilities.Tracer;

import org.mobicents.slee.sipevent.server.publication.PublicationClientControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.publication.Result;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParentSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Event;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Status;

public abstract class InternalPresenceClientControlSbb implements Sbb,InternalPresenceClientControl {

	private static Tracer logger;

	private SbbContext sbbContext = null; // This SBB's context

	/**
	 * Called when an sbb object is created and enters the pooled state.
	 */
	public void setSbbContext(SbbContext sbbContext) {
		this.sbbContext = sbbContext;
		logger = sbbContext.getTracer(getClass().getSimpleName());
	}

	// Implementation of PresenceClientControlSbbLocalObject :
	// all we need to do is forward the requests to the publication or
	// subscription childs, setting the event package to "presence" when needed

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControl#setParentSbb(org.mobicents.slee.sippresence.client.PresenceClientControlParentSbbLocalObject)
	 */
	public void setParentSbb(PresenceClientControlParentSbbLocalObject parentSbb) {
		// store in cmp
		setParentSbbCMP(parentSbb);
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControl#newPublication(java.lang.Object, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public void newPublication(Object requestId, String entity,
			String document, String contentType, String contentSubType,
			int expires) {

		final PublicationClientControlSbbLocalObject childSbb = getPublicationClientControlSbbLocalObject();
		if (childSbb != null) {
			final Result result = childSbb.newPublication(entity, "presence", document,
					contentType, contentSubType, expires);
			if (result.getStatusCode() < 300) {
				getParentSbbCMP().newPublicationOk(requestId, result.getETag(), result.getExpires());
			}
			else {
				getParentSbbCMP().newPublicationError(requestId, result.getStatusCode());
			}
		} else {
			getParentSbbCMP().newPublicationError(requestId,
					Response.SERVER_INTERNAL_ERROR);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControl#refreshPublication(java.lang.Object, java.lang.String, java.lang.String, int)
	 */
	public void refreshPublication(Object requestId, String entity,
			String eTag, int expires) {

		final PublicationClientControlSbbLocalObject childSbb = getPublicationClientControlSbbLocalObject();
		if (childSbb != null) {
			final Result result = childSbb.refreshPublication(entity, "presence", eTag,
					expires);
			if (result.getStatusCode() < 300) {
				getParentSbbCMP().refreshPublicationOk(requestId, result.getETag(), result.getExpires());
			}
			else {
				getParentSbbCMP().refreshPublicationError(requestId, result.getStatusCode());
			}
		} else {
			getParentSbbCMP().refreshPublicationError(requestId,
					Response.SERVER_INTERNAL_ERROR);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControl#modifyPublication(java.lang.Object, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public void modifyPublication(Object requestId, String entity, String eTag,
			String document, String contentType, String contentSubType,
			int expires) {

		final PublicationClientControlSbbLocalObject childSbb = getPublicationClientControlSbbLocalObject();
		if (childSbb != null) {
			final Result result = childSbb.modifyPublication(entity, "presence", eTag,
					document, contentType, contentSubType, expires);
			if (result.getStatusCode() < 300) {
				getParentSbbCMP().modifyPublicationOk(requestId, result.getETag(), result.getExpires());
			}
			else {
				getParentSbbCMP().modifyPublicationError(requestId, result.getStatusCode());
			}
		} else {
			getParentSbbCMP().modifyPublicationError(requestId,
					Response.SERVER_INTERNAL_ERROR);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControl#removePublication(java.lang.Object, java.lang.String, java.lang.String)
	 */
	public void removePublication(Object requestId, String entity, String eTag) {

		PublicationClientControlSbbLocalObject childSbb = getPublicationClientControlSbbLocalObject();
		if (childSbb != null) {
			
			final int result = childSbb.removePublication(entity, "presence", eTag);
			if (result < 300) {
				getParentSbbCMP().removePublicationOk(requestId);
			}
			else {
				getParentSbbCMP().removePublicationError(requestId, result);
			}
		} else {
			getParentSbbCMP().removePublicationError(requestId,
					Response.SERVER_INTERNAL_ERROR);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControl#newSubscription(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public void newSubscription(String subscriber,
			String subscriberdisplayName, String notifier, String eventPackage,
			String subscriptionId, int expires) {

		SubscriptionClientControlSbbLocalObject childSbb = getSubscriptionClientControlSbbLocalObject();
		if (childSbb != null) {
			// presence subscribes doesn't have content
			childSbb.subscribe(subscriber, subscriberdisplayName, notifier,
					eventPackage, subscriptionId, expires, null, null, null);
		} else {
			getParentSbbCMP().newSubscriptionError(subscriber, notifier,
					eventPackage, subscriptionId,
					Response.SERVER_INTERNAL_ERROR);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControl#refreshSubscription(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public void refreshSubscription(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires) {

		SubscriptionClientControlSbbLocalObject childSbb = getSubscriptionClientControlSbbLocalObject();
		if (childSbb != null) {
			childSbb.resubscribe(subscriber, notifier, eventPackage,
					subscriptionId, expires);
		} else {
			getParentSbbCMP().refreshSubscriptionError(subscriber, notifier,
					eventPackage, subscriptionId,
					Response.SERVER_INTERNAL_ERROR);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControl#removeSubscription(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void removeSubscription(String subscriber, String notifier,
			String eventPackage, String subscriptionId) {

		SubscriptionClientControlSbbLocalObject childSbb = getSubscriptionClientControlSbbLocalObject();
		if (childSbb != null) {
			childSbb.unsubscribe(subscriber, notifier, eventPackage,
					subscriptionId);
		} else {
			getParentSbbCMP().refreshSubscriptionError(subscriber, notifier,
					eventPackage, subscriptionId,
					Response.SERVER_INTERNAL_ERROR);
		}

	}
	
	// Implementation of SubscriptionClientControlParentSbbLocalObject :
	// all we need to do is forward the requests to the parent sbb

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParent#subscribeOk(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	public void subscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires,
			int responseCode) {

		getParentSbbCMP().newSubscriptionOk(subscriber, notifier, eventPackage,
				subscriptionId, expires, responseCode);
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParent#subscribeError(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public void subscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {

		getParentSbbCMP().newSubscriptionError(subscriber, notifier,
				eventPackage, subscriptionId, error);
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParent#resubscribeOk(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public void resubscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires) {

		getParentSbbCMP().refreshSubscriptionOk(subscriber, notifier,
				eventPackage, subscriptionId, expires);
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParent#resubscribeError(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public void resubscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {

		getParentSbbCMP().refreshSubscriptionError(subscriber, notifier,
				eventPackage, subscriptionId, error);
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParent#unsubscribeOk(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void unsubscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId) {

		getParentSbbCMP().removeSubscriptionOk(subscriber, notifier,
				eventPackage, subscriptionId);
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParent#unsubscribeError(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public void unsubscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {

		getParentSbbCMP().removeSubscriptionError(subscriber, notifier,
				eventPackage, subscriptionId, error);
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParent#notifyEvent(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.mobicents.slee.sipevent.server.subscription.pojo.Subscription.Event, org.mobicents.slee.sipevent.server.subscription.pojo.Subscription.Status, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void notifyEvent(String subscriber, String notifier,
			String eventPackage, String subscriptionId,
			Event terminationReason, Status status, String content,
			String contentType, String contentSubtype) {
		
		getParentSbbCMP().notifyEvent(subscriber, notifier, eventPackage,
				subscriptionId, terminationReason, status, content,
				contentType, contentSubtype);
	}
	
	// CHILD RELATIONS AND CMP FIELDs

	public abstract void setParentSbbCMP(
			PresenceClientControlParentSbbLocalObject parentSbb);

	public abstract PresenceClientControlParentSbbLocalObject getParentSbbCMP();

	public abstract PublicationClientControlSbbLocalObject getPublicationClientControlChildSbbCMP();

	public abstract void setPublicationClientControlChildSbbCMP(
			PublicationClientControlSbbLocalObject value);

	public abstract ChildRelation getPublicationClientControlChildRelation();

	private PublicationClientControlSbbLocalObject getPublicationClientControlSbbLocalObject() {
		PublicationClientControlSbbLocalObject childSbb = getPublicationClientControlChildSbbCMP();
		if (childSbb == null) {
			try {
				childSbb = (PublicationClientControlSbbLocalObject) getPublicationClientControlChildRelation()
						.create();
			} catch (Exception e) {
				logger.severe("Failed to create child sbb", e);
				return null;
			}
			setPublicationClientControlChildSbbCMP(childSbb);
		}
		return childSbb;
	}

	public abstract SubscriptionClientControlSbbLocalObject getSubscriptionClientControlChildSbbCMP();

	public abstract void setSubscriptionClientControlChildSbbCMP(
			SubscriptionClientControlSbbLocalObject value);

	public abstract ChildRelation getSubscriptionClientControlChildRelation();

	private SubscriptionClientControlSbbLocalObject getSubscriptionClientControlSbbLocalObject() {
		SubscriptionClientControlSbbLocalObject childSbb = getSubscriptionClientControlChildSbbCMP();
		if (childSbb == null) {
			try {
				childSbb = (SubscriptionClientControlSbbLocalObject) getSubscriptionClientControlChildRelation()
						.create();
			} catch (Exception e) {
				logger.severe("Failed to create child sbb", e);
				return null;
			}
			setSubscriptionClientControlChildSbbCMP(childSbb);
			childSbb
					.setParentSbb((SubscriptionClientControlParentSbbLocalObject) this.sbbContext
							.getSbbLocalObject());
		}
		return childSbb;
	}

	// SBB OBJECT LIFECYCLE METHODS

	public void sbbActivate() {
	}

	public void sbbCreate() throws CreateException {
	}

	public void sbbExceptionThrown(Exception arg0, Object arg1,
			ActivityContextInterface arg2) {
	}

	public void sbbLoad() {
	}

	public void sbbPassivate() {
	}

	public void sbbPostCreate() throws CreateException {
	}

	public void sbbRemove() {
	}

	public void sbbRolledBack(RolledBackContext arg0) {
	}

	public void sbbStore() {
	}

	public void unsetSbbContext() {
		this.sbbContext = null;
	}

}
