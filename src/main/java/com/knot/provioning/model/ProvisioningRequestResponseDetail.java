package com.knot.provioning.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "provisioning_request_response_details")
@IdClass(ProvisioningRequestResponseDetailPK.class)

public class ProvisioningRequestResponseDetail implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "RESPONSE_ID")
	private BigInteger responseID;

	@Id
	@Column(name = "RESPONSE_SEQUENCE_NO")
	private Integer responseSequenceNo;

	@Column(name = "REQUEST_DETAIL_ID")
	private BigInteger requestDetailID;

	@Column(name = "REQUEST_ID")
	private BigInteger requestID;

	@Column(name = "STATUS")
	private int status;

	@Column(name = "VERSION_NO")
	private float version;

	@Column(name = "RESPONSE_OBJECT_TYPE")
	private String responseObjectType;

	@Column(name = "RESPONSE_OBJECT")
	private String responseObject;

	@Column(name = "RETRY_SUBMIT_DATE")
	private Date retrySubmitDate;

	@Column(name = "RESPONSE_RECEIVED_DATE")
	private Date responseReceivedDate;

	@Column(name = "ERROR_CODE")
	private String errorCode;

	@Column(name = "ERROR_DESCRIPTION")
	private String errorDescription;

	public BigInteger getResponseID() {
		return responseID;
	}

	public void setResponseID(BigInteger responseID) {
		this.responseID = responseID;
	}

	public BigInteger getRequestDetailID() {
		return requestDetailID;
	}

	public void setRequestDetailID(BigInteger requestDetailID) {
		this.requestDetailID = requestDetailID;
	}

	public BigInteger getRequestID() {
		return requestID;
	}

	public void setRequestID(BigInteger requestID) {
		this.requestID = requestID;
	}

	public Integer getResponseSequenceNo() {
		return responseSequenceNo;
	}

	public void setResponseSequenceNo(Integer responseSequenceNo) {
		this.responseSequenceNo = responseSequenceNo;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public float getVersion() {
		return version;
	}

	public void setVersion(float version) {
		this.version = version;
	}

	public String getResponseObjectType() {
		return responseObjectType;
	}

	public void setResponseObjectType(String responseObjectType) {
		this.responseObjectType = responseObjectType;
	}

	public String getResponseObject() {
		return responseObject;
	}

	public void setResponseObject(String responseObject) {
		this.responseObject = responseObject;
	}

	public Date getRetrySubmitDate() {
		return retrySubmitDate;
	}

	public void setRetrySubmitDate(Date retrySubmitDate) {
		this.retrySubmitDate = retrySubmitDate;
	}

	public Date getResponseReceivedDate() {
		return responseReceivedDate;
	}

	public void setResponseReceivedDate(Date responseReceivedDate) {
		this.responseReceivedDate = responseReceivedDate;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
}
