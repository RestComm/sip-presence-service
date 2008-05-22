package org.openxdm.xcap.server.slee;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.slee.ActivityContextInterface;
import javax.slee.ChildRelation;
import javax.slee.RolledBackContext;
import javax.slee.SbbContext;

import net.java.slee.resource.http.events.HttpServletRequestEvent;

import org.apache.log4j.Logger;
import org.openxdm.xcap.common.error.BadRequestException;
import org.openxdm.xcap.common.error.ConflictException;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.error.MethodNotAllowedException;
import org.openxdm.xcap.common.error.NoParentConflictException;
import org.openxdm.xcap.common.error.NotFoundException;
import org.openxdm.xcap.common.error.PreconditionFailedException;
import org.openxdm.xcap.common.error.UnsupportedMediaTypeException;
import org.openxdm.xcap.common.http.HttpConstant;
import org.openxdm.xcap.common.resource.Resource;
import org.openxdm.xcap.common.uri.ParseException;
import org.openxdm.xcap.common.uri.Parser;
import org.openxdm.xcap.common.uri.ResourceSelector;
import org.openxdm.xcap.server.etag.ETagValidator;
import org.openxdm.xcap.server.etag.IfMatchETagValidator;
import org.openxdm.xcap.server.etag.IfNoneMatchETagValidator;
import org.openxdm.xcap.server.result.ReadResult;
import org.openxdm.xcap.server.result.WriteResult;

public abstract class AggregationProxySbb implements javax.slee.Sbb {

	private SbbContext sbbContext = null; // This SBB's context

	private static Logger logger = Logger.getLogger(AggregationProxySbb.class);

	/**
	 * Called when an sbb object is instantied and enters the pooled state.
	 */
	public void setSbbContext(SbbContext context) {
		if (logger.isDebugEnabled()) logger.debug("setSbbContext(context=" + context.toString() + ")");
		this.sbbContext = context;
	}

	public void unsetSbbContext() {
		if (logger.isDebugEnabled()) logger.debug("unsetSbbContext()");
		this.sbbContext = null;
	}

	public void sbbCreate() throws javax.slee.CreateException {
		if (logger.isDebugEnabled()) logger.debug("sbbCreate()");
	}

	public void sbbPostCreate() throws javax.slee.CreateException {
		if (logger.isDebugEnabled()) logger.debug("sbbPostCreate()");
	}

	public void sbbActivate() {
		if (logger.isDebugEnabled()) logger.debug("sbbActivate()");
	}

	public void sbbPassivate() {
		if (logger.isDebugEnabled()) logger.debug("sbbPassivate()");
	}

	public void sbbRemove() {
		if (logger.isDebugEnabled()) logger.debug("sbbRemove()");
	}

	public void sbbLoad() {
		if (logger.isDebugEnabled()) logger.debug("sbbLoad()");
	}

	public void sbbStore() {
		if (logger.isDebugEnabled()) logger.debug("sbbStore()");
	}

	public void sbbExceptionThrown(Exception exception, Object event,
			ActivityContextInterface activity) {
		if (logger.isDebugEnabled()) logger.debug("sbbExceptionThrown(exception=" + exception.toString()
				+ ",event=" + event.toString() + ",activity="
				+ activity.toString() + ")");
	}

	public void sbbRolledBack(RolledBackContext sbbRolledBack) {
		if (logger.isDebugEnabled()) logger.debug("sbbRolledBack(sbbRolledBack=" + sbbRolledBack.toString()
				+ ")");
	}

	protected SbbContext getSbbContext() {
		return sbbContext;
	}

	// CHILD RELATIONS & RA ABSTRACTIONS
	// ################################################################

	public abstract ChildRelation getRequestProcessorChildRelation();

	protected RequestProcessorSbbLocalObject getRequestProcessor()
			throws InternalServerErrorException {
		// get the child relation
		ChildRelation childRelation = getRequestProcessorChildRelation();
		// creates the child sbb if does not exist
		if (childRelation.isEmpty()) {
			try {
				return (RequestProcessorSbbLocalObject) childRelation.create();
			} catch (Exception e) {
				logger.error("unable to create the child sbb.", e);
				throw new InternalServerErrorException("");
			}
		}
		else {
			// return the child sbb
			return (RequestProcessorSbbLocalObject) childRelation.iterator().next();
		}
	}

	public void onDelete(HttpServletRequestEvent event,
			ActivityContextInterface aci) {

		// detach from the activity
		aci.detach(sbbContext.getSbbLocalObject());

		HttpServletRequest request = event.getRequest();
		HttpServletResponse response = event.getResponse();
		
		try {

			PrintWriter responseWriter = response.getWriter();
			try {

				// get xcap root from config
				String xcapRoot = ServerConfiguration.XCAP_ROOT;

				// create jxcap resource selector from request's uri & query
				// string
				ResourceSelector resourceSelector = Parser
						.parseResourceSelector(xcapRoot, request
								.getRequestURI(), request.getQueryString());

				// check conditional request headers
				// get ifMatch eTag
				ETagValidator eTagValidator = null;
				String eTag = request.getHeader(HttpConstant.HEADER_IF_MATCH);
				if (eTag != null) {
					eTagValidator = new IfMatchETagValidator(eTag);
				} else {
					eTag = request.getHeader(HttpConstant.HEADER_IF_NONE_MATCH);
					if (eTag != null) {
						eTagValidator = new IfNoneMatchETagValidator(eTag);
					}
				}

				// delete in data source
				WriteResult result = getRequestProcessor().delete(
						resourceSelector, eTagValidator, xcapRoot);
				// set response status
				response.setStatus(result.getResponseStatus());
				// set response entity tag if provided
				if (result.getResponseEntityTag() != null) {
					response.setHeader(HttpConstant.HEADER_ETAG, result
							.getResponseEntityTag());
				}

			} catch (ParseException e) {
				NotFoundException ne = new NotFoundException();
				if (logger.isDebugEnabled()) logger.debug("invalid xcap uri, replying , replying "+ne.getResponseStatus());
				response.setStatus(ne.getResponseStatus());

			} catch (NotFoundException e) {
				if (logger.isDebugEnabled()) logger.debug("doc/elem/attrib not found, replying "+e.getResponseStatus());
				response.setStatus(e.getResponseStatus());

			} catch (ConflictException e) {
				if (logger.isDebugEnabled()) logger.debug("conflict exception, replying "+e.getResponseStatus());
				response.setStatus(e.getResponseStatus());
				responseWriter.print(e.getResponseContent());

			} catch (MethodNotAllowedException e) {
				if (logger.isDebugEnabled()) logger.debug("method not allowed, replying "+e.getResponseStatus());
				response.setStatus(e.getResponseStatus());
				// add all exception headers
				Map<String, String> exceptionHeaders = e.getResponseHeaders();
				for (Iterator<String> i = exceptionHeaders.keySet().iterator(); i
						.hasNext();) {
					String headerName = i.next();
					String headerValue = exceptionHeaders.get(headerName);
					response.setHeader(headerName, headerValue);
				}

			} catch (PreconditionFailedException e) {
				if (logger.isDebugEnabled()) logger.debug("precondition failed on etags, replying "+e.getResponseStatus());
				response.setStatus(e.getResponseStatus());

			} catch (InternalServerErrorException e) {
				logger.warn("internal server error: "+e.getMessage()+", replying "+e.getResponseStatus());
				response.setStatus(e.getResponseStatus());

			} catch (BadRequestException e) {
				if (logger.isDebugEnabled()) logger.debug("bad request, replying "+e.getResponseStatus());
				response.setStatus(e.getResponseStatus());
			}
			// send to client
			responseWriter.close();
			
		} catch (Exception e) {
			logger.error("Error processing onDelete()", e);
		}

	}

	public void onGet(HttpServletRequestEvent event,
			ActivityContextInterface aci) {

		// detach from the activity
		aci.detach(sbbContext.getSbbLocalObject());

		HttpServletRequest request = event.getRequest();
		HttpServletResponse response = event.getResponse();

		try {

			PrintWriter responseWriter = response.getWriter();

			try {

				// create jxcap resource selector from request's uri & query
				// string
				ResourceSelector resourceSelector = Parser
						.parseResourceSelector(ServerConfiguration.XCAP_ROOT, request.getRequestURI(),
								request.getQueryString());
				// read result from data source
				ReadResult result = getRequestProcessor().get(resourceSelector);
				// get data object from result
				Resource dataObject = result.getResponseDataObject();
				// set response content type
				response.setContentType(dataObject.getMimetype());
				// set response entity tag
				response.setHeader(HttpConstant.HEADER_ETAG, result
						.getResponseEntityTag());
				// add response content
				responseWriter.println(dataObject.toXML());

			} catch (ParseException e) {
				NotFoundException ne = new NotFoundException();
				logger.warn("invalid xcap uri, replying "+ne.getResponseStatus());
				response.setStatus(ne.getResponseStatus());

			} catch (NotFoundException e) {
			
				if (logger.isDebugEnabled()) logger.debug("doc/elem/attrib not found, replying "+e.getResponseStatus());
				response.setStatus(e.getResponseStatus());

			} catch (InternalServerErrorException e) {
				logger.warn("internal server error: ");
				response.setStatus(e.getResponseStatus());

			} catch (BadRequestException e) {
				if (logger.isDebugEnabled()) logger.debug("bad request, replying "+e.getResponseStatus());
				response.setStatus(e.getResponseStatus());
			}
			// send to client
			responseWriter.close();

		} catch (Exception e) {
			logger.error("Error processing onGet()", e);
		}

	}

	public void onPut(HttpServletRequestEvent event,
			ActivityContextInterface aci) {

		// detach from the activity
		aci.detach(sbbContext.getSbbLocalObject());

		HttpServletRequest request = event.getRequest();
		HttpServletResponse response = event.getResponse();
		
		try {

			PrintWriter responseWriter = response.getWriter();

			try {

				// create resource selector from request's uri & query
				// string
				ResourceSelector resourceSelector = Parser
						.parseResourceSelector(ServerConfiguration.XCAP_ROOT, request
								.getRequestURI(), request.getQueryString());

				// check conditional request headers
				// get ifMatch eTag
				ETagValidator eTagValidator = null;
				String eTag = request.getHeader(HttpConstant.HEADER_IF_MATCH);
				if (eTag != null) {
					eTagValidator = new IfMatchETagValidator(eTag);
				} else {
					eTag = request.getHeader(HttpConstant.HEADER_IF_NONE_MATCH);
					if (eTag != null) {
						eTagValidator = new IfNoneMatchETagValidator(eTag);
					}
				}
				// get content mimetype
				String mimetype = request.getContentType();
				// put object in data source
				WriteResult result = getRequestProcessor().put(
						resourceSelector, mimetype, request.getInputStream(),
						eTagValidator, ServerConfiguration.XCAP_ROOT);
				// set response status
				response.setStatus(result.getResponseStatus());
				// set response entity tag with new one on result
				response.setHeader(HttpConstant.HEADER_ETAG, result
						.getResponseEntityTag());

			} catch (ParseException e) {
				// invalid resource selector
				BadRequestException bre = new BadRequestException();
				if (logger.isDebugEnabled()) logger.debug("invalid xcap uri, replying "+bre.getResponseStatus());
				response.setStatus(bre.getResponseStatus());

			} catch (IOException e) {
				InternalServerErrorException ie = new InternalServerErrorException(
						e.getMessage());
				logger.warn("internal server error: "+e.getMessage()+", replying "+ie.getResponseStatus());
				response.setStatus(ie.getResponseStatus());

			} catch (NoParentConflictException e) {
					// add base uri
					e.setSchemeAndAuthorityURI(ServerConfiguration.SCHEME_AND_AUTHORITY_URI);
					// add query string if exists
					if (request.getQueryString() != null) {
						e.setQueryComponent(request.getQueryString());
					}
					if (logger.isDebugEnabled()) logger.debug("no parent conflict exception, replying "+e.getResponseStatus());
					response.setStatus(e.getResponseStatus());
					responseWriter.print(e.getResponseContent());
					
			} catch (ConflictException e) {
				if (logger.isDebugEnabled()) logger.debug("conflict exception, replying "+e.getResponseStatus());
				response.setStatus(e.getResponseStatus());
				responseWriter.print(e.getResponseContent());

			} catch (MethodNotAllowedException e) {
				if (logger.isDebugEnabled()) logger.debug("method not allowed, replying "+e.getResponseStatus());
				response.setStatus(e.getResponseStatus());
				// add all exception headers
				Map<String, String> exceptionHeaders = e.getResponseHeaders();
				for (Iterator<String> i = exceptionHeaders.keySet().iterator(); i
						.hasNext();) {
					String headerName = i.next();
					String headerValue = exceptionHeaders.get(headerName);
					response.setHeader(headerName, headerValue);
				}

			} catch (UnsupportedMediaTypeException e) {
				if (logger.isDebugEnabled()) logger.debug("unsupported media exception, replying "+e.getResponseStatus());
				response.setStatus(e.getResponseStatus());

			} catch (InternalServerErrorException e) {
				logger.warn("internal server error: "+e.getMessage()+", replying "+e.getResponseStatus());
				response.setStatus(e.getResponseStatus());

			} catch (PreconditionFailedException e) {
				if (logger.isDebugEnabled()) logger.debug("precondition failed on etags, replying "+e.getResponseStatus());
				response.setStatus(e.getResponseStatus());

			} catch (BadRequestException e) {
				if (logger.isDebugEnabled()) logger.debug("invalid xcap uri, replying "+e.getResponseStatus());
				response.setStatus(e.getResponseStatus());
			}			
			// send to client
			responseWriter.close();

		} catch (Exception e) {
			logger.error("Error processing onPut()", e);
		}

	}

	// ######################################## NOT SUPPORTED METHODS

	public void onPost(HttpServletRequestEvent event,
			ActivityContextInterface aci) {

		// detach from the activity
		aci.detach(sbbContext.getSbbLocalObject());

		// method not allowed, set right sc and allow header then send response
		try {
			HttpServletResponse response = event.getResponse();
			response.setStatus(MethodNotAllowedException.RESPONSE_STATUS);
			response.setHeader(HttpConstant.HEADER_ALLOW, "GET, PUT, DELETE");
			response.flushBuffer();
		} catch (Exception e) {
			logger.error("unable to send response", e);
		}

	}

	public void onHead(HttpServletRequestEvent event,
			ActivityContextInterface aci) {

		// detach from the activity
		aci.detach(sbbContext.getSbbLocalObject());

		// method not allowed, set right sc and allow header then send response
		try {
			HttpServletResponse response = event.getResponse();
			response.setStatus(MethodNotAllowedException.RESPONSE_STATUS);
			response.setHeader(HttpConstant.HEADER_ALLOW, "GET, PUT, DELETE");
			response.flushBuffer();
		} catch (Exception e) {
			logger.error("unable to send response", e);
		}

	}

	public void onOptions(HttpServletRequestEvent event,
			ActivityContextInterface aci) {

		// detach from the activity
		aci.detach(sbbContext.getSbbLocalObject());

		// method not allowed, set right sc and allow header then send response
		try {
			HttpServletResponse response = event.getResponse();
			response.setStatus(MethodNotAllowedException.RESPONSE_STATUS);
			response.setHeader(HttpConstant.HEADER_ALLOW, "GET, PUT, DELETE");
			response.flushBuffer();
		} catch (Exception e) {
			logger.error("unable to send response", e);
		}

	}

	public void onTrace(HttpServletRequestEvent event,
			ActivityContextInterface aci) {

		// detach from the activity
		aci.detach(sbbContext.getSbbLocalObject());

		// method not allowed, set right sc and allow header then send response
		try {
			HttpServletResponse response = event.getResponse();
			response.setStatus(MethodNotAllowedException.RESPONSE_STATUS);
			response.setHeader(HttpConstant.HEADER_ALLOW, "GET, PUT, DELETE");
			response.flushBuffer();
		} catch (Exception e) {
			logger.error("unable to send response", e);
		}

	}

}