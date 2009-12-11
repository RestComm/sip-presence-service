package org.openxdm.xcap.common.error;

public class SchemaValidationErrorConflictException extends ConflictException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	public SchemaValidationErrorConflictException() {
		super();
	}
	
	public SchemaValidationErrorConflictException(String msg) {
		super(msg);
	}
	
	public SchemaValidationErrorConflictException(String msg,Throwable t) {
		super(msg,t);
	}

	protected String getConflictError() {
		return "<schema-validation-error />";
	}

}
