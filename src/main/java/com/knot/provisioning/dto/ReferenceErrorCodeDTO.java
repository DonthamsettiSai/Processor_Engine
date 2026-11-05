package com.knot.provisioning.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * 
 * @author Lakshman
 *
 */

public class ReferenceErrorCodeDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String statusCode;

	private String statusMessage;

	private Map<String, ErrorCodeDTO> result;

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public Map<String, ErrorCodeDTO> getResult() {
		return result;
	}

	public void setResult(Map<String, ErrorCodeDTO> result) {
		this.result = result;
	}
	
	public String getErrorDesc(String message) {
		return result.get(message) != null ? result.get(message).getErrorDescription() : "Error Code Not Found: "+ message ;
	}

	public String getErrorDesc(Long message) {
		return result.get(message.toString()) != null ? result.get(message.toString()).getErrorDescription() : "Error Code Not Found: "+ message ;
	}
}
