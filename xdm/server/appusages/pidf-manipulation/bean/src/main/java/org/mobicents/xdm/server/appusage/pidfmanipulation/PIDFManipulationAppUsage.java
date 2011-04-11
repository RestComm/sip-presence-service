package org.mobicents.xdm.server.appusage.pidfmanipulation;

import javax.xml.validation.Validator;

import org.mobicents.xdm.server.appusage.AppUsage;

/**
 * IETF PIDF Manipulation XCAP App Usage.
 * 
 * @author martins
 * 
 */
public class PIDFManipulationAppUsage extends AppUsage {

	public static final String ID = "pidf-manipulation";
	public static final String DEFAULT_DOC_NAMESPACE = "urn:ietf:params:xml:ns:pidf";
	public static final String MIMETYPE = "application/pidf+xml";

	/**
	 * 
	 * @param schemaValidator
	 */
	public PIDFManipulationAppUsage(Validator schemaValidator,String allowedDocumentName) {
		super(ID,DEFAULT_DOC_NAMESPACE,MIMETYPE,schemaValidator,allowedDocumentName);
	}

}
