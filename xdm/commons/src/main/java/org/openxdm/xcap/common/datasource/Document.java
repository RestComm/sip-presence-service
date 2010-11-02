package org.openxdm.xcap.common.datasource;

import org.openxdm.xcap.common.error.InternalServerErrorException;

public interface Document {

	public org.w3c.dom.Document getAsDOMDocument() throws InternalServerErrorException;
	
	public String getAsString() throws InternalServerErrorException;
	
	public String getAUID();
	
	public String getDocumentParent();
	
	public String getDocumentName(); 
	
	public String getETag();
	
}
