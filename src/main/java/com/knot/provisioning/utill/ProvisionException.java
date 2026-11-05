package com.knot.provisioning.utill;

/**
 * @author M.Siva Sankar
 *
 */
public class ProvisionException extends Exception {

	private static final long serialVersionUID = 1L;

	private String message;
	
	private String errorCode;

	public ProvisionException() {

	}

	public ProvisionException(Exception e) {
		this.message = e != null ? e.getMessage() : "Exception is null";
	}

	public ProvisionException(String message) {
		this.message = message;
	}

	 public ProvisionException(String message, String errorCode) {
		  super(message);
		   this.errorCode = errorCode;
		   this.message = message;
	    }
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

}
