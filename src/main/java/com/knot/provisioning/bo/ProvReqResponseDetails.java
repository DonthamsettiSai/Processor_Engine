package com.knot.provisioning.bo;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
//@IdClass(ProvisioningReqResponseBOPK.class)
public class ProvReqResponseDetails implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
    @Id	
	@Column(name="RESPONSE_ID")
	private BigInteger responseID;
	
	@Column(name="REQUEST_DETAIL_ID")
	private BigInteger requestDetailID;	
	
	@Column(name="REQUEST_ID")
	private BigInteger requestID;
	
	@Column(name="VERSION_NO")
	private Double versionNo;
	
	@Id
	@Column(name="RESPONSE_SEQUENCE_NO")
	private Integer responseSequenceNo;
	
	@Column(name="RESPONSE_OBJECT_TYPE")
	private String responseObjectType;
	
	@Column(name="RESPONSE_OBJECT")
	private String responseobject;
	
	@Column(name="RETRY_SUBMIT_DATE")
	private LocalDate retrySubmitDate;
	
	@Column(name="RESPONSE_RECEIVED_DATE")
	private LocalDate responseReceivedDate;
	
	@Column(name="STATUS")
	private Integer status;
	
	@Column(name="ERROR_CODE")
	private String errorCode;
	
	@Column(name="ERROR_DESCRIPTION")
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

	public Double getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(Double versionNo) {
		this.versionNo = versionNo;
	}

	public Integer getResponseSequenceNo() {
		return responseSequenceNo;
	}

	public void setResponseSequenceNo(Integer responseSequenceNo) {
		this.responseSequenceNo = responseSequenceNo;
	}

	public String getResponseObjectType() {
		return responseObjectType;
	}

	public void setResponseObjectType(String responseObjectType) {
		this.responseObjectType = responseObjectType;
	}

	public String getResponseobject() {
		return responseobject;
	}

	public void setResponseobject(String responseobject) {
		this.responseobject = responseobject;
	}

	public LocalDate getRetrySubmitDate() {
		return retrySubmitDate;
	}

	public void setRetrySubmitDate(LocalDate retrySubmitDate) {
		this.retrySubmitDate = retrySubmitDate;
	}

	public LocalDate getResponseReceivedDate() {
		return responseReceivedDate;
	}

	public void setResponseReceivedDate(LocalDate responseReceivedDate) {
		this.responseReceivedDate = responseReceivedDate;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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
