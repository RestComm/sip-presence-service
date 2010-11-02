package org.mobicents.xdm.server.appusage.oma.xcapdirectory;

import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.mobicents.slee.xdm.server.ServerConfiguration;
import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor;
import org.mobicents.xdm.server.appusage.AppUsageManagement;
import org.mobicents.xdm.server.appusage.InterceptedDocument;
import org.mobicents.xdm.server.appusage.oma.xcapdirectory.uri.UriBuilder;
import org.mobicents.xdm.server.appusage.oma.xcapdirectory.uri.UriComponentEncoder;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author martins
 * 
 */
public class XcapDirectoryAppUsageDataSourceInterceptor implements
		AppUsageDataSourceInterceptor {

	private static final Logger logger = Logger
			.getLogger(XcapDirectoryAppUsage.class);

	public static final String DIRECTORY_DOCUMENT_NAME = "directory.xml";
	private static final String ETAG_ATTRIBUTE_NAME = "etag";
	private static final String ROOT_DIRECTORY_ELEMENT_NAME = "xcap-directory";
	private static final String FOLDER_ELEMENT_NAME = "folder";
	private static final String AUID_ATTR_NAME = "auid";
	private static final String ENTRY_ELEMENT_NAME = "entry";
	private static final String URI_ATTR_NAME = "uri";

	private static final ServerConfiguration XDM_SERVER_CONFIGURATION = ServerConfiguration
			.getInstance();

	private static final DocumentBuilder DOCUMENT_BUILDER = createDocumentBuilder();

	private static DocumentBuilder createDocumentBuilder() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor#getDocument
	 * (org.openxdm.xcap.common.uri.DocumentSelector,
	 * org.mobicents.xdm.server.appusage.AppUsageDataSource)
	 */
	@Override
	public InterceptedDocument getDocument(DocumentSelector documentSelector,
			AppUsageDataSource dataSource) throws InternalServerErrorException {
		if (logger.isDebugEnabled()) {
			logger.debug("building xcap directory doc " + documentSelector
					+ " on request");
		}

		Document document = DOCUMENT_BUILDER.newDocument();
		Element rootElement = document.createElementNS(
				XcapDirectoryAppUsage.DEFAULT_DOC_NAMESPACE,
				ROOT_DIRECTORY_ELEMENT_NAME);
		document.appendChild(rootElement);
		for (String appUsageId : AppUsageManagement.getInstance()
				.getAppUsages()) {
			if (!appUsageId.equals(XcapDirectoryAppUsage.ID)) {
				Element folderElement = document.createElementNS(
						XcapDirectoryAppUsage.DEFAULT_DOC_NAMESPACE,
						FOLDER_ELEMENT_NAME);
				rootElement.appendChild(folderElement);
				folderElement.setAttributeNS(null, AUID_ATTR_NAME, appUsageId);
				for (org.openxdm.xcap.common.datasource.Document storedDoc : dataSource
						.getDocuments(appUsageId, documentSelector
								.getDocumentParent())) {
					Element entryElement = document.createElementNS(
							XcapDirectoryAppUsage.DEFAULT_DOC_NAMESPACE,
							ENTRY_ELEMENT_NAME);
					folderElement.appendChild(entryElement);
					entryElement.setAttributeNS(null, URI_ATTR_NAME,
							getDocumentURI(new DocumentSelector(appUsageId,
									storedDoc.getDocumentParent(), storedDoc
											.getDocumentName())));
					entryElement.setAttributeNS(null, ETAG_ATTRIBUTE_NAME,
							storedDoc.getETag());
				}
			}
		}
		return new InterceptedDocument(documentSelector, document);
	}

	private String getDocumentURI(DocumentSelector documentSelector)
			throws InternalServerErrorException {
		UriBuilder uriBuilder = new UriBuilder();
		uriBuilder.setDocumentSelector(UriComponentEncoder.encodePath(documentSelector.toString()));
		uriBuilder.setXcapRoot(XDM_SERVER_CONFIGURATION.getXcapRoot());
		uriBuilder.setSchemeAndAuthority(XDM_SERVER_CONFIGURATION
				.getSchemeAndAuthority());
		try {
			return uriBuilder.toURI().toString();
		} catch (URISyntaxException e) {
			throw new InternalServerErrorException(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor#getDocuments
	 * (java.lang.String, org.mobicents.xdm.server.appusage.AppUsageDataSource)
	 */
	@Override
	public InterceptedDocument[] getDocuments(String documentParent,
			AppUsageDataSource dataSource) throws InternalServerErrorException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AppUsageDataSourceInterceptor#getDocuments
	 * (org.mobicents.xdm.server.appusage.AppUsageDataSource)
	 */
	@Override
	public InterceptedDocument[] getDocuments(AppUsageDataSource dataSource)
			throws InternalServerErrorException {
		throw new UnsupportedOperationException();
	}

}
