package com.knot.provioning.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "provisioning_request_processing_errors")

public class ProvisioningRequestProcessingError implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ERROR_RECORD_ID")
	private BigInteger errorRecordID;

	@Column(name = "REQUEST_ID")
	private BigInteger requestID;

	@Column(name = "REQUEST_DETAIL_ID")
	private BigInteger requestDetailID;

	@Column(name = "STATUS")
	private Integer status;

	@Column(name = "REMARKS")
	private String remarks;

	/**
	 * @Column(name = "VERSION_NO") private Float versionNo;
	 */

	/**
	 * @Column(name = "SERVICE_ID") private Integer serviceID;
	 */

	/**
	 * @Column(name = "SUB_SEQUENCE_ID") private Integer subSequenceID;
	 */

	@Column(name = "ERROR_DESCRIPTION")
	private String errorDescription;

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	@Column(name = "ERROR_CODE")
	private String errorCode;

	@Column(name = "ERROR_FOUND_DATE")
	private Date errorFoundDate;

	public BigInteger getErrorRecordID() {
		return errorRecordID;
	}

	public void setErrorRecordID(BigInteger errorRecordID) {
		this.errorRecordID = errorRecordID;
	}

	public BigInteger getRequestID() {
		return requestID;
	}

	public void setRequestID(BigInteger requestID) {
		this.requestID = requestID;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public BigInteger getRequestDetailID() {
		return requestDetailID;
	}

	public void setRequestDetailID(BigInteger requestDetailID) {
		this.requestDetailID = requestDetailID;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public Date getErrorFoundDate() {
		return errorFoundDate;
	}

	public void setErrorFoundDate(Date errorFoundDate) {
		this.errorFoundDate = errorFoundDate;
	}

}
