package com.knot.provisioning.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NSOServiceDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String message;

	private String status;

	private Object additionalInfo;

	@JsonProperty("service_order_id")
	private String serviceOrderId;

	private String unique_id;
	@JsonProperty("status_code")

	private String statusCode;

	private String success;

	private FailDto fail;

	private String reqDtlID;

	private String reqID;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Object getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(Object additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public String getServiceOrderId() {
		return serviceOrderId;
	}

	public void setServiceOrderId(String serviceOrderId) {
		this.serviceOrderId = serviceOrderId;
	}

	public String getUnique_id() {
		return unique_id;
	}

	public void setUnique_id(String unique_id) {
		this.unique_id = unique_id;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	public FailDto getFail() {
		return fail;
	}

	public void setFail(FailDto fail) {
		this.fail = fail;
	}

	public String getReqDtlID() {
		return reqDtlID;
	}

	public void setReqDtlID(String reqDtlID) {
		this.reqDtlID = reqDtlID;
	}

	public String getReqID() {
		return reqID;
	}

	public void setReqID(String reqID) {
		this.reqID = reqID;
	}

	
	public NSOServiceDto() {
		super();
		
	}
	
	public NSOServiceDto(NSOServiceDto dto) {
		this.message = dto.getMessage();
		this.status = dto.getStatus();
		this.additionalInfo = dto.getAdditionalInfo();
		this.serviceOrderId = dto.getServiceOrderId();
		this.unique_id = dto.getUnique_id();
		this.statusCode = dto.getStatusCode();
		this.success = dto.getSuccess();
		this.fail = dto.getFail();
		this.reqDtlID = dto.getReqDtlID();
		this.reqID = dto.getReqID();
	}
}
