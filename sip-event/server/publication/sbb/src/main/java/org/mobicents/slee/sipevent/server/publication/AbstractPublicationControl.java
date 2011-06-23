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

import java.io.Serializable;
import java.io.StringReader;
import java.util.List;

import javax.sip.address.URI;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;
import javax.sip.message.Response;
import javax.xml.transform.dom.DOMSource;

import org.mobicents.slee.sipevent.server.publication.data.ComposedPublication;
import org.mobicents.slee.sipevent.server.publication.data.ComposedPublicationKey;
import org.mobicents.slee.sipevent.server.publication.data.Publication;
import org.mobicents.slee.sipevent.server.publication.data.PublicationControlDataSource;
import org.mobicents.slee.sipevent.server.publication.data.PublicationKey;
import org.mobicents.slee.sipevent.server.publication.jmx.PublicationControlManagement;
import org.mobicents.xdm.common.util.dom.DomUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * 
 * @author martins
 * 
 */
public abstract class AbstractPublicationControl implements PublicationControl {

	private static final Result UNSUPPORTED_MEDIA_TYPE = new Result(
			Response.UNSUPPORTED_MEDIA_TYPE);
	private static final Result FORBIDDEN = new Result(Response.FORBIDDEN);
	private static final Result SERVER_INTERNAL_ERROR = new Result(
			Response.SERVER_INTERNAL_ERROR);
	private static final Result CONDITIONAL_REQUEST_FAILED = new Result(
			Response.CONDITIONAL_REQUEST_FAILED);

	protected abstract PublicationControlLogger getLogger();

	protected abstract ImplementedPublicationControl getImplementedPublicationControl();
	
	private static final PublicationControlManagement management = PublicationControlManagement
			.getInstance();
	private static final PublicationControlDataSource dataSource = management.getDataSource();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.sipevent.server.publication.PublicationClientControl
	 * #newPublication(java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, int)
	 */
	public Result newPublication(String entity, String eventPackage,
			String document, String contentType, String contentSubType,
			int expires) {

		final PublicationControlLogger logger = getLogger();
		if (logger.isDebugEnabled()) {
			logger.debug("new publication request: entity=" + entity
					+ ",eventPackage=" + eventPackage);
		}

		Publication publication = null;
		Result result = null;

		// get child sbb
		final ImplementedPublicationControl impl = getImplementedPublicationControl();

		try {

			Document domDocument = unmarshallDocument(document, eventPackage, impl);
			
			if (domDocument == null) {
				// If the content type of the request does
				// not match the event package, or is not understood by the ESC,
				// the
				// ESC MUST reject the request with an appropriate response,
				// such as
				// 415 (Unsupported Media Type)
				if (logger.isInfoEnabled()) {
					logger.info("publication for resource " + entity
							+ " on event package " + eventPackage
							+ " has unsupported media type");
				}
				return UNSUPPORTED_MEDIA_TYPE;
			}

			
			// authorize publication
			if (!impl.authorizePublication(entity, eventPackage, domDocument)) {
				if (logger.isInfoEnabled()) {
					logger.info("publication for resource " + entity
							+ " on event package " + eventPackage
							+ " not authorized");
				}
				result = FORBIDDEN;
			} else {
				// create SIP-ETag
				final String eTag = ETagGenerator
						.generate(entity, eventPackage);
				// create publication pojo
				final PublicationKey publicationKey = new PublicationKey(eTag,
						entity, eventPackage);
				publication = new Publication(publicationKey, document,
						contentType, contentSubType);
				publication.setDocumentAsDOM(domDocument);
				// set timer
				setTimer(publication, expires);
				// update or create composed publication
				ComposedPublication composedPublication = getComposedPublication(
						entity, eventPackage);
				if (composedPublication == null) {
					// single publication, composed publication is the new
					// publication
					composedPublication = ComposedPublication
							.fromPublication(publication);
					dataSource.add(composedPublication);
				} else {
					// composed publication exists
					composedPublication = updateComposedPublication(
							publication, getPublications(entity, eventPackage),
							null,impl);
				}
				// persist data
				dataSource.add(publication);
				// notify subscribers
				impl.notifySubscribers(composedPublication);
				if (logger.isInfoEnabled()) {
					logger.info("New " + publication + " for " + expires
							+ " seconds");
				}
				result = new Result(200, eTag, expires);

			}
		} catch (Exception e) {
			logger.error("failed to create publication", e);
			if (publication != null) {
				// timer may be set, cancel it
				cancelTimer(publication);
			}
			result = SERVER_INTERNAL_ERROR;
		}
		return result;
	}

	private Document unmarshallDocument(String document, String eventPackage, ImplementedPublicationControl implementedPublicationControl) {
		// unmarshall document
		StringReader reader = new StringReader(document);
		try {
			Document domDocument = DomUtils.DOCUMENT_BUILDER_NS_AWARE_FACTORY.newDocumentBuilder().parse(new InputSource(reader));
			implementedPublicationControl.getSchema(eventPackage).newValidator().validate(new DOMSource(domDocument));
			return domDocument;
		}
		catch (Exception e) {
			PublicationControlLogger logger = getLogger();
			if (logger.isDebugEnabled()) {
				logger.error("failed to parse publication content",e);
			}
			return null;
		}
		finally {
			reader.close();
		}
	}

	/**
	 * @param publication
	 * @param expires
	 * @throws Exception
	 */
	protected abstract void setTimer(Publication publication, int expires)
			throws Exception;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.sipevent.server.publication.PublicationClientControl
	 * #refreshPublication(java.lang.String, java.lang.String, java.lang.String,
	 * int)
	 */
	public Result refreshPublication(String entity, String eventPackage,
			String oldETag, int expires) {

		final PublicationControlLogger logger = getLogger();
		if (logger.isDebugEnabled()) {
			logger.debug("refresh Publication: entity=" + entity
					+ ",eventPackage=" + eventPackage + ",eTag=" + oldETag
					+ ",expires=" + expires);
		}

		Result result = null;

		try {
			// get publication
			final Publication publication = getPublication(oldETag, entity,
					eventPackage);
			if (publication == null) {
				if (logger.isInfoEnabled()) {
					logger.info("can't refresh publication for resource "
							+ entity + " on event package " + eventPackage
							+ " with eTag " + oldETag + ", it does not exist");
				}
				result = CONDITIONAL_REQUEST_FAILED;
			} else {
				// create new SIP-ETag
				final String eTag = ETagGenerator
						.generate(entity, eventPackage);
				// create new publication pojo
				final PublicationKey newPublicationKey = new PublicationKey(
						eTag, entity, eventPackage);
				final Publication newPublication = new Publication(
						newPublicationKey, publication.getDocumentAsString(),
						publication.getContentType(), publication
								.getContentSubType());
				// reset timer
				resetTimer(publication, newPublication, expires);
				// replace publication
				dataSource.replace(publication, newPublication);
				// inform parent publication is valid
				result = new Result(Response.OK, eTag, expires);
				if (logger.isInfoEnabled()) {
					logger.info("Refreshed " + publication + " for " + expires
							+ " seconds");
				}
			}
		} catch (Exception e) {
			logger.error("failed to refresh publication", e);
			result = SERVER_INTERNAL_ERROR;
		}

		return result;
	}

	/**
	 * @param publication
	 * @param newPublication
	 * @param expires
	 * @throws Exception
	 */
	protected abstract void resetTimer(Publication publication,
			Publication newPublication, int expires) throws Exception;

	/**
	 * @param timerID
	 */
	protected abstract void cancelTimer(Publication publication);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.sipevent.server.publication.PublicationClientControl
	 * #removePublication(java.lang.String, java.lang.String, java.lang.String)
	 */
	public int removePublication(String entity, String eventPackage, String eTag) {

		final PublicationControlLogger logger = getLogger();
		if (logger.isDebugEnabled()) {
			logger.debug("removePublication: entity=" + entity
					+ ",eventPackage=" + eventPackage + ",eTag=" + eTag);
		}

		int result = -1;

		try {
			// get publications for the entity and event package and look for
			// the one related with the request
			Publication publication = null;
			final Publication[] publications = getPublications(entity,
					eventPackage);
			for (Publication otherPublication : publications) {
				if (otherPublication.getPublicationKey().getETag().equals(eTag)) {
					publication = otherPublication;
					break;
				}
			}
			if (publication == null) {
				if (logger.isInfoEnabled()) {
					logger.info("can't remove publication for resource "
							+ entity + " on event package " + eventPackage
							+ " with eTag " + eTag + ", it does not exist");
				}
				result = Response.CONDITIONAL_REQUEST_FAILED;
			} else {
				// cancel timer
				cancelTimer(publication);
				// remove old publication
				dataSource.delete(publication);
				// get child sbb
				final ImplementedPublicationControl impl = getImplementedPublicationControl();
				// we need to re-compose all publications except the one being
				// removed
				final ComposedPublication composedPublication = removeFromComposedPublication(
						publication, publications, true, impl);
				if (composedPublication.getDocumentAsString() == null
						&& management
								.isUseAlternativeValueForExpiredPublication()) {
					// give the event package implementation sbb a chance to
					// define
					// an alternative publication value for the one expired,
					// this can allow a behavior such as defining offline status
					// in a presence resource
					final Publication alternativePublication = impl
							.getAlternativeValueForExpiredPublication(publication);
					if (alternativePublication != null) {
						composedPublication
								.setContentSubType(alternativePublication
										.getContentSubType());
						composedPublication
								.setContentType(alternativePublication
										.getContentType());
						composedPublication.setDocumentAsString(alternativePublication
								.getDocumentAsString());
						composedPublication
								.setDocumentAsDOM(alternativePublication
										.getDocumentAsDOM());
					}

				}
				// inform parent publication is removed
				result = Response.OK;
				if (logger.isInfoEnabled()) {
					logger.info("Removed " + publication);
				}
				// notify subscribers
				impl.notifySubscribers(composedPublication);
			}
		} catch (Exception e) {
			logger.error("failed to remove publication", e);
			result = Response.SERVER_INTERNAL_ERROR;
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.sipevent.server.publication.PublicationClientControl
	 * #modifyPublication(java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public Result modifyPublication(String entity, String eventPackage,
			String oldETag, String document, String contentType,
			String contentSubType, int expires) {

		final PublicationControlLogger logger = getLogger();
		if (logger.isDebugEnabled()) {
			getLogger().debug(
					"modifyPublication: entity=" + entity + ",eventPackage="
							+ eventPackage + ",eTag=" + oldETag);
		}

		Result result = null;
		try {
			// get child sbb
			final ImplementedPublicationControl impl = getImplementedPublicationControl();
			// get publications for the entity and event package and look for
			// the one related with the request
			Publication publication = null;
			final Publication[] publications = getPublications(entity,
					eventPackage);
			for (Publication otherPublication : publications) {
				if (otherPublication.getPublicationKey().getETag().equals(
						oldETag)) {
					publication = otherPublication;
					break;
				}
			}
			if (publication == null) {
				if (logger.isInfoEnabled()) {
					logger.info("can't modify publication for resource "
							+ entity + " on event package " + eventPackage
							+ " with eTag " + oldETag + ", it does not exist");
				}
				result = CONDITIONAL_REQUEST_FAILED;
			} else {
				// unmarshall document
				final Document unmarshalledContent = unmarshallDocument(document,eventPackage,impl);
				if (unmarshalledContent == null) {
					// If the content type of the request does
					// not match the event package, or is not understood by the
					// ESC,
					// the
					// ESC MUST reject the request with an appropriate response,
					// such as
					// 415 (Unsupported Media Type)
					if (logger.isInfoEnabled()) {
						logger.info("publication for resource " + entity
								+ " on event package " + eventPackage
								+ " has unsupported media type");
					}
					return UNSUPPORTED_MEDIA_TYPE;
				}

				// authorize publication
				if (!impl.authorizePublication(entity, eventPackage, unmarshalledContent)) {
					result = FORBIDDEN;
					if (logger.isInfoEnabled()) {
						logger.info("publication for resource " + entity
								+ " on event package " + eventPackage
								+ " not authorized");
					}
				} else {
					// create new SIP-ETag
					final String eTag = ETagGenerator.generate(entity,
							eventPackage);
					// create new publication pojo with new key and document
					final PublicationKey newPublicationKey = new PublicationKey(
							eTag, entity, eventPackage);
					final Publication newPublication = new Publication(
							newPublicationKey, document, contentType,
							contentSubType);
					newPublication.setDocumentAsDOM(unmarshalledContent);
					// get composed publication and rebuild it
					final ComposedPublication composedPublication = updateComposedPublication(
							newPublication, publications, oldETag, impl);
					// reset timer
					resetTimer(publication, newPublication, expires);
					// replace data
					dataSource.replace(publication, newPublication);
					// notify subscribers
					impl.notifySubscribers(composedPublication);
					// inform parent publication is valid
					result = new Result(Response.OK, eTag, expires);
					if (logger.isInfoEnabled()) {
						logger.info(publication + " modified.");
					}
				}
			}
		} catch (Exception e) {
			logger.error("failed to refresh publication", e);
			result = SERVER_INTERNAL_ERROR;
		}

		return result;
	}

	/**
	 * @param newPublication
	 * @param composedPublication
	 */
	private ComposedPublication updateComposedPublication(
			Publication publication, Publication[] publications,
			String exceptETag, ImplementedPublicationControl impl) {
		final ComposedPublication composedPublication = ComposedPublication
				.fromPublication(publication);
		Document unmarshalledContent = publication
				.getDocumentAsDOM();
		for (Publication otherPublication : publications) {
			if (exceptETag == null
					|| !otherPublication.getPublicationKey().getETag().equals(
							exceptETag)) {
				unmarshalledContent = impl.getStateComposer(publication.getPublicationKey().getEventPackage())
						.compose(unmarshalledContent,
								otherPublication.getDocumentAsDOM());
			}
		}
		composedPublication.setDocumentAsDOM(unmarshalledContent);
		composedPublication.forceDocumentUpdate();
		dataSource.update(composedPublication);
		return composedPublication;
	}

	/**
	 * a timer has occurred in a dialog regarding a publication
	 * 
	 * @param event
	 * @param aci
	 */
	public void timerExpired(Serializable timerID) {

		final PublicationControlLogger logger = getLogger();

		// get publication
		final Publication publication = dataSource.getFromTimerID(timerID);
		if (publication != null) {

			try {
				// get child sbb
				final ImplementedPublicationControl impl = getImplementedPublicationControl();
				// remove publication
				dataSource.delete(publication);
				if (logger.isInfoEnabled()) {
					logger.info(publication + " removed. Timer expired.");
				}
				// we need to re-compose all publications except the one being
				// removed
				final ComposedPublication composedPublication = removeFromComposedPublication(
						publication, getPublications(publication
								.getPublicationKey().getEntity(), publication
								.getPublicationKey().getEventPackage()), false,impl);
				if (composedPublication.getDocumentAsString() == null
						&& management
								.isUseAlternativeValueForExpiredPublication()) {
					// give the event package implementation sbb a chance to
					// define
					// an alternative publication value for the one expired,
					// this can allow a behavior such as defining offline status
					// in a presence resource
					final Publication alternativePublication = impl
							.getAlternativeValueForExpiredPublication(publication);
					if (alternativePublication != null) {
						composedPublication
								.setContentSubType(alternativePublication
										.getContentSubType());
						composedPublication
								.setContentType(alternativePublication
										.getContentType());
						composedPublication.setDocumentAsString(alternativePublication
								.getDocumentAsString());
						composedPublication
								.setDocumentAsDOM(alternativePublication
										.getDocumentAsDOM());
					}
				}
				// notify subscribers
				impl.notifySubscribers(composedPublication);
			} catch (Exception e) {
				logger.error("failed to remove publication that expired", e);
			}
		}

	}

	// ----------- SBB LOCAL OBJECT

	public void shutdown() {
		// nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.publication.PublicationControl#
	 * getComposedPublication(java.lang.String, java.lang.String)
	 */
	public ComposedPublication getComposedPublication(String entity,
			String eventPackage) {
		return dataSource
				.get(new ComposedPublicationKey(entity, eventPackage));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.publication.PublicationControl#
	 * acceptsContentType(java.lang.String, javax.sip.header.ContentTypeHeader)
	 */
	public boolean acceptsContentType(String eventPackage,
			ContentTypeHeader contentTypeHeader) {
		return getImplementedPublicationControl().acceptsContentType(
				eventPackage, contentTypeHeader);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.publication.PublicationControl#
	 * getAcceptsHeader(java.lang.String)
	 */
	public Header getAcceptsHeader(String eventPackage) {
		return getImplementedPublicationControl()
				.getAcceptsHeader(eventPackage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.publication.PublicationControl#
	 * getEventPackages()
	 */
	public String[] getEventPackages() {
		return getImplementedPublicationControl().getEventPackages();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.sipevent.server.publication.PublicationControl#
	 * isResponsibleForResource(javax.sip.address.URI)
	 */
	public boolean isResponsibleForResource(URI uri, String eventPackage) {
		return getImplementedPublicationControl().isResponsibleForResource(uri,eventPackage);
	}

	// ----------- AUX METHODS

	private ComposedPublication removeFromComposedPublication(
			Publication publication, Publication[] publications,
			boolean publicationIsInPublications, ImplementedPublicationControl impl) {

		final String entity = publication.getPublicationKey().getEntity();
		final String eventPackage = publication.getPublicationKey()
				.getEventPackage();
		final ComposedPublicationKey composedPublicationKey = new ComposedPublicationKey(
				entity, eventPackage);

		final ComposedPublication composedPublication = new ComposedPublication(composedPublicationKey);
		
		// rebuild composed content with all publications (except the one
		// removed)
		Document composedPublicationUnmarshalledContent = null;
		for (Publication otherPublication : publications) {
			if (!publicationIsInPublications
					|| !otherPublication.getPublicationKey().getETag().equals(
							publication.getPublicationKey().getETag())) {
				composedPublicationUnmarshalledContent = impl.getStateComposer(eventPackage)
						.compose(composedPublicationUnmarshalledContent,
								otherPublication.getDocumentAsDOM());
			}
		}

		if (composedPublicationUnmarshalledContent == null) {
			// just remove composed state
			dataSource.delete(composedPublicationKey);
		} else {
			composedPublication.setDocumentAsDOM(
					composedPublicationUnmarshalledContent);
			composedPublication.setContentType(publication.getContentType());
			composedPublication.setContentSubType(publication.getContentSubType());
			composedPublication.forceDocumentUpdate();
			// update
			dataSource.update(composedPublication);

		}
		return composedPublication;
	}

	private Publication getPublication(String eTag, String entity,
			String eventPackage) {
		return dataSource.get(new PublicationKey(eTag, entity,
				eventPackage));
	}

	private static final Publication[] EMPTY_ARRAY_PUBLICATIONS = {};

	/**
	 * 
	 * @param entity
	 * @param eventPackage
	 * @return null if there are no publications
	 */
	private Publication[] getPublications(String entity, String eventPackage) {

		final List<?> resultList = dataSource.getPublications(eventPackage,
				entity);

		if (resultList.size() == 0) {
			return EMPTY_ARRAY_PUBLICATIONS;
		} else {
			return resultList
					.toArray(new Publication[resultList.size()]);
		}
	}

}