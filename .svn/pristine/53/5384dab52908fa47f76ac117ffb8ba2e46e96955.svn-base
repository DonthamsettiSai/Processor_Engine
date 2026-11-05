package com.knot.provisioning.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author Sameena
 *
 */

public class ResponseMode implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	private String statusCode;	
	
	private String statusMessage;
	
	@JsonInclude(Include.NON_NULL)
	private String totalDisplayCount;

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

	public String getTotalDisplayCount() {
		return totalDisplayCount;
	}

	public void setTotalDisplayCount(String totalDisplayCount) {
		this.totalDisplayCount = totalDisplayCount;
	}

	
}
