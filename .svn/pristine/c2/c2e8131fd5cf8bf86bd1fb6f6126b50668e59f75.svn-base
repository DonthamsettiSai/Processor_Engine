package com.knot.provisioning.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;

public class TmfAysncRequestDto implements Serializable {

	/**
	 *@author SAI D
	 */
	private static final long serialVersionUID = 1L;

	@NotBlank(message = "statusCode is mandatory")
	private String statusCode;

	private String statusMessage;

	@NotBlank(message = "serviceOrderId is mandatory")
	private String serviceOrderId;

	/** private String externalID; */
	
	private Object response;


	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

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

	public String getServiceOrderId() {
		return serviceOrderId;
	}

	public void setServiceOrderId(String serviceOrderId) {
		this.serviceOrderId = serviceOrderId;
	}

	/**
	 * public String getExternalID() { return externalID; }
	 * 
	 * public void setExternalID(String externalID) { this.externalID = externalID;
	 * }
	 */
}
