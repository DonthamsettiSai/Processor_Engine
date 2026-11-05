package com.knot.provisioning.dto;

import java.io.Serializable;

public class LifeCycleDto implements Serializable{

	
	private static final long serialVersionUID = 1L;
	
	private String statusCode;
	
	private String statusMessage;
	
	private String requestCompletionTime;
	
	private String reqDtlID;
	
	private String source;

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

	public String getRequestCompletionTime() {
		return requestCompletionTime;
	}

	public void setRequestCompletionTime(String requestCompletionTime) {
		this.requestCompletionTime = requestCompletionTime;
	}

	public String getReqDtlID() {
		return reqDtlID;
	}

	public void setReqDtlID(String reqDtlID) {
		this.reqDtlID = reqDtlID;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	

}
