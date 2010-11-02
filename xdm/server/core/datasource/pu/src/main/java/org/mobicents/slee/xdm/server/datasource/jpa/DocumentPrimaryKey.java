package org.mobicents.slee.xdm.server.datasource.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * 
 * @author eduardomartins
 * 
 */

@Embeddable
public class DocumentPrimaryKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6638892043798746768L;

	@Column(name = "AUID", nullable = false)
	private String appUsage;
	
	@Column(name = "DOCUMENT_NAME", nullable = false)
	private String documentName;

	@Column(name = "DOCUMENT_PARENT", nullable = false)
	private String documentParent;

	public DocumentPrimaryKey() {
		// TODO Auto-generated constructor stub
	}
	
	public DocumentPrimaryKey(String auid, String documentParent, String documentName) {
		setDocumentName(documentName);
		setAppUsage(auid);
		setDocumentParent(documentParent);
	}

	// -- GETTERS AND SETTERS

	public String getAppUsage() {
		return appUsage;
	}
	
	public void setAppUsage(String appUsage) {
		this.appUsage = appUsage;
	}
	
	public String getDocumentName() {
		return documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public String getDocumentParent() {
		return documentParent;
	}
	
	public void setDocumentParent(String documentParent) {
		this.documentParent = documentParent;
	}

	@Override
	public int hashCode() {
		int result = appUsage.hashCode();
		result = 31 * result + documentParent.hashCode();
		result = 31 * result + documentName.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocumentPrimaryKey other = (DocumentPrimaryKey) obj;
		if (!appUsage.equals(other.appUsage)) 
			return false;
		if (!documentParent.equals(other.documentParent)) 
			return false;
		if (!documentName.equals(other.documentName)) 
			return false;
		return true;
	}

	public String toString() {
		return "DocumentPrimaryKey : appUsage = "+appUsage+", documentParent = "+documentParent+" , documentName = " + documentName;
	}

}