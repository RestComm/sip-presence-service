package org.openxdm.xcap.common.uri;

import java.io.Serializable;

/**
 * A document selector points to a document resource on
 * a XCAP server. It's built from a application usage id (auid), document selector string, a document parent selector string, and the document name.
 * 
 * Usage Example that creates a document selector pointing to 'resource-lists' document named
 * 'index', for user 'sip:eduardo@mobicents.org'
 * 
 * DocumentSelector documentSelector = new DocumentSelector(
 * "resource-lists", "users/user/sip%3Aeduardo%40mobicents.org",index");
 * 
 * DocumentSelector documentSelector = DocumentSelector.valueOf("resource-lists/users/user/sip%3Aeduardo%40mobicents.org/index");
 * 
 * Note that you need to take care of percent encoding chars that are not
 * allowed in a valid URI.
 * 
 * @author Eduardo Martins
 *
 */

public class DocumentSelector implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String auid;
	private final String documentParent;
	private final String documentName;

	private transient String completeDocumentParent = null;
	private transient String toString = null;
	
	/**
	 * Builds a {@link DocumentSelector} from a {@link String} value. 
	 * @param documentSelector the document selector string, it may start or not with a /
	 * @return
	 * @throws ParseException
	 */
	public static DocumentSelector valueOf(String documentSelector) throws ParseException {
		try {
			// get documentName & documentParent
			int documentNameSeparator = documentSelector.lastIndexOf("/");
			if (documentNameSeparator != -1) {				
				final String documentParent = documentSelector.substring(0,documentNameSeparator);
				final String documentName = documentSelector.substring(documentNameSeparator+1);				
				final int auidBeginIndex = documentParent.charAt(0) == '/' ? 1 : 0;
				final int auidEndIndex = documentParent.indexOf('/',auidBeginIndex);
				final String auid = documentParent.substring(auidBeginIndex,auidEndIndex);				
				return new DocumentSelector(auid,documentParent.substring(auidEndIndex+1),documentName);				
			} else {
				throw new ParseException(null);
			}			
		}
		catch (IndexOutOfBoundsException e) {
			throw new ParseException(null,e);
		}
	}

	/**
	 * Creates a new instance of a document selector, from the specified application usage id (auid), document parent and document name. 
	 * @param auid the application usage id of the document resource.
	 * @param documentParent the parent of the document.
	 * @param documentName the document name.
	 */
	public DocumentSelector(String auid, String documentParent,
			String documentName) {
		this.documentParent = documentParent;
		this.documentName = documentName;
		this.auid = auid;
	}

	/**
	 * Retreives the application usage id of the document resource.
	 * @return
	 */
	public String getAUID() {
		return auid;
	}

	/**
	 * Retreives the document's name of the document resource. 
	 * @return
	 */
	public String getDocumentName() {
		return documentName;
	}

	/**
	 * Retreives the document's parent of the document resource, relative to the auid 
	 * @return
	 */
	public String getDocumentParent() {
		return documentParent;
	}
	
	/**
	 * Retreives the document's parent of the document resource, including the auid 
	 * @return
	 */
	public String getCompleteDocumentParent() {
		if (completeDocumentParent == null) {
			completeDocumentParent = new StringBuilder(auid.length()+documentParent.length()+2).append('/').append(auid).append('/').append(documentParent).toString();
		}
		return completeDocumentParent;
	}
	
	/**
	 * Indicates if the document selector is pointing to a document in the users tree.
	 * @return
	 */
	public boolean isUserDocument() {
		// no need to check for whole 'users word, document parent's first char
		// can only be a 'g' or 'u'
		return documentParent.charAt(0) == 'u';
	}
	
	@Override
	public String toString() {
		if (toString == null) {
			toString = new StringBuilder(getCompleteDocumentParent()).append('/').append(documentName).toString(); 
		}
		return toString;
	}
	
	@Override
	public int hashCode() {
		return (auid.hashCode()*31+documentParent.hashCode())*31+documentName.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			final DocumentSelector other = (DocumentSelector) obj;
			return this.auid.equals(other.auid) && this.documentParent.equals(other.documentParent) && this.documentName.equals(other.documentName);
		}
		else {
			return false;
		}
	}
}
