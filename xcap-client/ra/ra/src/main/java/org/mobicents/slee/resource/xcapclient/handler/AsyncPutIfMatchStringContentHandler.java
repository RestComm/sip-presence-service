package org.mobicents.slee.resource.xcapclient.handler;

import org.mobicents.slee.resource.xcapclient.XCAPClientResourceAdaptor;
import org.mobicents.slee.resource.xcapclient.XCAPResourceAdaptorActivityHandle;
import org.openxdm.xcap.client.Response;
import org.openxdm.xcap.common.key.XcapUriKey;

/**
 * Handles an async put if ETag match request, using String content.
 * 
 * @author emmartins
 * 
 */
public class AsyncPutIfMatchStringContentHandler extends AbstractAsyncHandler {

	protected String mimetype;
	protected String content;
	protected String eTag;

	public AsyncPutIfMatchStringContentHandler(XCAPClientResourceAdaptor ra,
			XCAPResourceAdaptorActivityHandle handle, XcapUriKey key,
			String eTag, String mimetype, String content) {
		super(ra, handle, key);
		this.mimetype = mimetype;
		this.content = content;
		this.eTag = eTag;
	}

	@Override
	protected Response doRequest() throws Exception {
		return ra.getClient().putIfMatch(key, eTag, mimetype, content);
	}

}