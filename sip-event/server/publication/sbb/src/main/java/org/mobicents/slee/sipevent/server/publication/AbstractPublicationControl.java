package org.mobicents.slee.sipevent.server.publication;

import java.io.Serializable;
import java.util.List;

import javax.sip.address.URI;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;
import javax.sip.message.Response;
import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.publication.data.ComposedPublication;
import org.mobicents.slee.sipevent.server.publication.data.ComposedPublicationKey;
import org.mobicents.slee.sipevent.server.publication.data.JAXBContentHandler;
import org.mobicents.slee.sipevent.server.publication.data.Publication;
import org.mobicents.slee.sipevent.server.publication.data.PublicationControlDataSource;
import org.mobicents.slee.sipevent.server.publication.data.PublicationKey;
import org.mobicents.slee.sipevent.server.publication.jmx.PublicationControlManagement;

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

	protected abstract Logger getLogger();

	protected abstract ImplementedPublicationControl getImplementedPublicationControl();

	private static JAXBContentHandler jaxbContentHandler;
	private static StateComposer stateComposer;

	private static final PublicationControlManagement management = PublicationControlManagement
			.getInstance();
	private static final PublicationControlDataSource dataSource = management.getDataSource();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.sipevent.server.publication.PublicationControl#init()
	 */
	public void init() {
		ImplementedPublicationControl impl = getImplementedPublicationControl();
		jaxbContentHandler = new JAXBContentHandler(impl.getJaxbContext());
		stateComposer = impl.getStateComposer();
	}

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

		final Logger logger = getLogger();
		if (logger.isDebugEnabled()) {
			logger.debug("new publication request: entity=" + entity
					+ ",eventPackage=" + eventPackage);
		}

		Publication publication = null;
		Result result = null;

		try {

			// unmarshall document
			final JAXBElement<?> unmarshalledContent = jaxbContentHandler
					.unmarshallFromString(document);
			if (unmarshalledContent == null) {
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

			// get child sbb
			final ImplementedPublicationControl impl = getImplementedPublicationControl();

			// authorize publication
			if (!impl.authorizePublication(entity, unmarshalledContent)) {
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
						contentType, contentSubType, jaxbContentHandler);
				publication.setUnmarshalledContent(unmarshalledContent);
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
							null);
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

		final Logger logger = getLogger();
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
						newPublicationKey, publication.getDocument(),
						publication.getContentType(), publication
								.getContentSubType(), jaxbContentHandler);
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

		final Logger logger = getLogger();
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
						publication, publications, true);
				if (composedPublication.getDocument() == null
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
						composedPublication.setDocument(alternativePublication
								.getDocument());
						composedPublication
								.setUnmarshalledContent(alternativePublication
										.getUnmarshalledContent());
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

		final Logger logger = getLogger();
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
				final JAXBElement<?> unmarshalledContent = jaxbContentHandler
						.unmarshallFromString(document);
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
				if (!impl.authorizePublication(entity, unmarshalledContent)) {
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
							contentSubType, jaxbContentHandler);
					newPublication.setUnmarshalledContent(unmarshalledContent);
					// get composed publication and rebuild it
					final ComposedPublication composedPublication = updateComposedPublication(
							newPublication, publications, oldETag);
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
	@SuppressWarnings("unchecked")
	private ComposedPublication updateComposedPublication(
			Publication publication, Publication[] publications,
			String exceptETag) {
		final ComposedPublication composedPublication = ComposedPublication
				.fromPublication(publication);
		final JAXBElement unmarshalledContent = publication
				.getUnmarshalledContent();
		Object composedPublicationUnmarshalledContentValue = unmarshalledContent
				.getValue();
		for (Publication otherPublication : publications) {
			if (exceptETag == null
					|| !otherPublication.getPublicationKey().getETag().equals(
							exceptETag)) {
				composedPublicationUnmarshalledContentValue = stateComposer
						.compose(composedPublicationUnmarshalledContentValue,
								otherPublication.getUnmarshalledContent()
										.getValue());
			}
		}
		composedPublication.setUnmarshalledContent(new JAXBElement(
				unmarshalledContent.getName(), unmarshalledContent
						.getDeclaredType(), unmarshalledContent.getScope(),
				composedPublicationUnmarshalledContentValue));
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

		final Logger logger = getLogger();

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
								.getPublicationKey().getEventPackage()), false);
				if (composedPublication.getDocument() == null
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
						composedPublication.setDocument(alternativePublication
								.getDocument());
						composedPublication
								.setUnmarshalledContent(alternativePublication
										.getUnmarshalledContent());
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
		final ComposedPublication cp = dataSource
				.get(new ComposedPublicationKey(entity, eventPackage));
		if (cp != null) {
			cp.setJaxbContentHandler(jaxbContentHandler);
		}
		return cp;
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
	public boolean isResponsibleForResource(URI uri) {
		return getImplementedPublicationControl().isResponsibleForResource(uri);
	}

	// ----------- AUX METHODS

	@SuppressWarnings("unchecked")
	private ComposedPublication removeFromComposedPublication(
			Publication publication, Publication[] publications,
			boolean publicationIsInPublications) {

		final String entity = publication.getPublicationKey().getEntity();
		final String eventPackage = publication.getPublicationKey()
				.getEventPackage();
		final ComposedPublicationKey composedPublicationKey = new ComposedPublicationKey(
				entity, eventPackage);

		final ComposedPublication composedPublication = new ComposedPublication(composedPublicationKey);
		
		// rebuild composed content with all publications (except the one
		// removed)
		Object composedPublicationUnmarshalledContentValue = null;
		for (Publication otherPublication : publications) {
			if (!publicationIsInPublications
					|| !otherPublication.getPublicationKey().getETag().equals(
							publication.getPublicationKey().getETag())) {
				composedPublicationUnmarshalledContentValue = stateComposer
						.compose(composedPublicationUnmarshalledContentValue,
								otherPublication.getUnmarshalledContent()
										.getValue());
			}
		}

		if (composedPublicationUnmarshalledContentValue == null) {
			// just remove composed state
			dataSource.delete(composedPublicationKey);
		} else {
			JAXBElement unmarshalledContent = publication
					.getUnmarshalledContent();
			composedPublication.setJaxbContentHandler(jaxbContentHandler);
			composedPublication.setUnmarshalledContent(new JAXBElement(
					unmarshalledContent.getName(), unmarshalledContent
							.getDeclaredType(), unmarshalledContent.getScope(),
					composedPublicationUnmarshalledContentValue));
			composedPublication.setContentSubType(publication.getContentSubType());
			composedPublication.setContentType(publication.getContentType());
			composedPublication.forceDocumentUpdate();
			// update
			dataSource.update(composedPublication);

		}
		return composedPublication;
	}

	private Publication getPublication(String eTag, String entity,
			String eventPackage) {
		final Publication p = dataSource.get(new PublicationKey(eTag, entity,
				eventPackage));
		if (p != null) {
			p.setJaxbContentHandler(jaxbContentHandler);
		}
		return p;
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
			final Publication[] resultArray = resultList
					.toArray(new Publication[resultList.size()]);
			// need to set the jaxb handler
			for (Publication p : resultArray) {
				p.setJaxbContentHandler(jaxbContentHandler);
			}
			return resultArray;
		}
	}

}