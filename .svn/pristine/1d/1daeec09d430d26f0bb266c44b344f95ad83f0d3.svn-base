package com.knot.provisioning.bo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Transient;

/**
 * Sathish Ravula
 */

@Entity
@IdClass(ProvisioningReqDetailsBOPK.class)
public class ProvisioningReqDetailsBO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "REQUEST_DETAIL_ID")
	private String requestDetailID;

	@Column(name = "REQUEST_ID")
	private String requestID;

//	@JsonIgnore
	@Column(name = "SERVICE_ID")
	private String serviceID;

	@Column(name = "SERVER_URL")
	private String serverURL;

	@Column(name = "TOKEN_ID")
	private String tokenID;

	@Column(name = "REQUEST_OBJECT")
	private String requestObject;

	@Column(name = "MAX_RETRIES")
	private String maxRetries;

	@Column(name = "CURRENT_RETRY_COUNT")
	private String retryCount;

	@Column(name = "STATUS")
	private String status;
	
	@Column(name = "DEVICE_TYPE")
	private String deviceType;
	
	@Column(name = "DEP_SUB_SEQ_PROCESSING_TYPE")
	private String processingType;


	@Id
	@Column(name = "VERSION_NO")
	private String version;

	@Transient
	private String serviceName;
	
	@Transient
	private String serviceCode;

	@JsonIgnore
	@Column(name = "tenant_id")
	private String tenantId;
	
	@JsonIgnore
	@Column(name = "partner_id")
	private String partnerId;

	public String getRequestDetailID() {
		return requestDetailID;
	}

	public void setRequestDetailID(String requestDetailID) {
		this.requestDetailID = requestDetailID;
	}

	public String getRequestID() {
		return requestID;
	}

	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}

	public String getServerURL() {
		return serverURL;
	}

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public String getTokenID() {
		return tokenID;
	}

	public void setTokenID(String tokenID) {
		this.tokenID = tokenID;
	}

	public String getRequestObject() {
		return requestObject;
	}

	public void setRequestObject(String requestObject) {
		this.requestObject = requestObject;
	}

	public String getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(String maxRetries) {
		this.maxRetries = maxRetries;
	}

	public String getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(String retryCount) {
		this.retryCount = retryCount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getServiceID() {
		return serviceID;
	}

	public void setServiceID(String serviceID) {
		this.serviceID = serviceID;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getProcessingType() {
		return processingType;
	}

	public void setProcessingType(String processingType) {
		this.processingType = processingType;
	}
	
	
	
}
