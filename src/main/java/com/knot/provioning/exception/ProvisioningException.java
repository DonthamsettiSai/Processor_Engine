package com.knot.provioning.exception;

public class ProvisioningException extends RuntimeException{

	/**
	 * @author Sai D
	 */
	private static final long serialVersionUID = 1L;
	
	
	public ProvisioningException() {
		super();
	}
	
	public ProvisioningException(String message) {
		super(message);
	}

}
