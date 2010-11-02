package org.openxdm.xcap.server.slee;

import org.mobicents.xdm.server.appusage.AppUsageDataSource;
import org.openxdm.xcap.common.datasource.DataSource;
import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;

/**
 * 
 * @author martins
 * 
 */
public class AppUsageDataSourceImpl implements AppUsageDataSource {

	/**
	 * 
	 */
	private final DataSource dataSource;

	/**
	 * 
	 * @param dataSource
	 */
	public AppUsageDataSourceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AppUsageDataSource#getDocument(org.
	 * openxdm.xcap.common.uri.DocumentSelector)
	 */
	@Override
	public Document getDocument(DocumentSelector documentSelector)
			throws InternalServerErrorException {
		return dataSource.getDocument(documentSelector);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AppUsageDataSource#getDocuments(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	public Document[] getDocuments(String auid, String documentParent)
			throws InternalServerErrorException {
		return dataSource.getDocuments(auid, documentParent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.xdm.server.appusage.AppUsageDataSource#getDocuments(java
	 * .lang.String)
	 */
	@Override
	public Document[] getDocuments(String auid)
			throws InternalServerErrorException {
		return dataSource.getDocuments(auid);
	}

}
