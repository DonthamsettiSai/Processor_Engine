package com.knot.provioning.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "provisioning_response_details")
public class ProvRespDetails implements Serializable{
	
	private static final long serialVersionUID = 1L; 

	@Id
	@Column(name = "PROVISIONING_RESP_ID")
	private BigInteger serviceProvResID;

	@Column(name = "PROVISIONING_REQUEST_ID")
	private BigInteger serviceProvReqID;

	@Column(name = "SERVER_URL")
	private String serverUrl;

	@Column(name = "RESPONSE_CODES")
	private String responseCode;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "API_OBJECT")
	private String apiObject;

	@Id
	@Column(name = "EFFECTIVE_FROM")
	private LocalDate effectiveFrom;

	@Column(name = "EFFECTIVE_TO")
	private LocalDate effectiveTo;

	@Column(name = "STATUS")
	private Integer status;

	@Column(name = "RESPONSE_CODE_TYPE")
	private Integer responseCodeType;
	
	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "CREATED_IPADDR")
	private String createdIPAddr;

	@Column(name = "CREATED_ON")
	private LocalDateTime createdOn;
	
	@Column(name = "MODIFIED_BY")
	private String modifiedBy;
	
	@Column(name = "MODIFIED_ON")
	private String modifiedIPAddr;
	
	@Column(name = "MODIFIED_IPADDR")
	private LocalDateTime modifiedOn;

	
	public Integer getResponseCodeType() {
		return responseCodeType;
	}

	public void setResponseCodeType(Integer responseCodeType) {
		this.responseCodeType = responseCodeType;
	}
	
	public BigInteger getServiceProvResID() {
		return serviceProvResID;
	}

	public void setServiceProvResID(BigInteger serviceProvResID) {
		this.serviceProvResID = serviceProvResID;
	}

	public BigInteger getServiceProvReqID() {
		return serviceProvReqID;
	}

	public void setServiceProvReqID(BigInteger serviceProvReqID) {
		this.serviceProvReqID = serviceProvReqID;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getApiObject() {
		return apiObject;
	}

	public void setApiObject(String apiObject) {
		this.apiObject = apiObject;
	}

	public LocalDate getEffectiveFrom() {
		return effectiveFrom;
	}

	public void setEffectiveFrom(LocalDate effectiveFrom) {
		this.effectiveFrom = effectiveFrom;
	}

	public LocalDate getEffectiveTo() {
		return effectiveTo;
	}

	public void setEffectiveTo(LocalDate effectiveTo) {
		this.effectiveTo = effectiveTo;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedIPAddr() {
		return createdIPAddr;
	}

	public void setCreatedIPAddr(String createdIPAddr) {
		this.createdIPAddr = createdIPAddr;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getModifiedIPAddr() {
		return modifiedIPAddr;
	}

	public void setModifiedIPAddr(String modifiedIPAddr) {
		this.modifiedIPAddr = modifiedIPAddr;
	}

	public LocalDateTime getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(LocalDateTime modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	/**
	 * public ProvRespDetails(ProvResponseDTO provRespDto) { this.serviceProvResID =
	 * Integer.parseInt(provRespDto.getServiceProvResID()); this.serviceProvReqID =
	 * Integer.parseInt(provRespDto.getServiceProvReqID()); this.serverUrl =
	 * provRespDto.getServerUrl(); this.responseCode =
	 * provRespDto.getResponseCode(); this.description =
	 * provRespDto.getDescription(); this.apiObject = provRespDto.getApiObject();
	 * this.effectiveFrom = FileUtil.checkEmptyNull(provRespDto.getEffectiveFrom())
	 * ? DateUtill.parseDate(provRespDto.getEffectiveFrom(), Constants.DATE_FORMATE)
	 * : LocalDate.now(); this.effectiveTo =
	 * FileUtil.checkEmptyNull(provRespDto.getEffectiveTo()) ?
	 * DateUtill.parseDate(provRespDto.getEffectiveTo(), Constants.DATE_FORMATE) :
	 * DateUtill.getLocalDefaultEndDate(); this.status =
	 * Integer.parseInt(provRespDto.getStatus()); this.createdBy =
	 * provRespDto.getCreatedBy(); this.createdIPAddr =
	 * provRespDto.getCreatedIPAddr(); this.createdOn = LocalDateTime.now(); }
	 */

}
