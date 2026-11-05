package com.knot.provisioning.dto;

import java.io.Serializable;

/**
 * 
 * @author Sameena
 *
 */

public class ServiceDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String serviceID;

	private String lobID;

	private String lobName;

	private String serviceCode;

	private String serviceName;

	private String serviceType;

	private String partnerID;

	private String multipleService;

	private String onetimeService;

	private String effectiveFrom;

	private String effectiveTo;

	private String status;

	private String tenantID;
	
	private String serviceProvisioningPriority;
	
	private String serviceBusinessPriority;

	public String getServiceID() {
		return serviceID;
	}

	public void setServiceID(String serviceID) {
		this.serviceID = serviceID;
	}

	public String getLobID() {
		return lobID;
	}

	public void setLobID(String lobID) {
		this.lobID = lobID;
	}

	public String getLobName() {
		return lobName;
	}

	public void setLobName(String lobName) {
		this.lobName = lobName;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getPartnerID() {
		return partnerID;
	}

	public void setPartnerID(String partnerID) {
		this.partnerID = partnerID;
	}

	public String getMultipleService() {
		return multipleService;
	}

	public void setMultipleService(String multipleService) {
		this.multipleService = multipleService;
	}

	public String getOnetimeService() {
		return onetimeService;
	}

	public void setOnetimeService(String onetimeService) {
		this.onetimeService = onetimeService;
	}

	public String getEffectiveFrom() {
		return effectiveFrom;
	}

	public void setEffectiveFrom(String effectiveFrom) {
		this.effectiveFrom = effectiveFrom;
	}

	public String getEffectiveTo() {
		return effectiveTo;
	}

	public void setEffectiveTo(String effectiveTo) {
		this.effectiveTo = effectiveTo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTenantID() {
		return tenantID;
	}

	public void setTenantID(String tenantID) {
		this.tenantID = tenantID;
	}

	public String getServiceProvisioningPriority() {
		return serviceProvisioningPriority;
	}

	public void setServiceProvisioningPriority(String serviceProvisioningPriority) {
		this.serviceProvisioningPriority = serviceProvisioningPriority;
	}

	public String getServiceBusinessPriority() {
		return serviceBusinessPriority;
	}

	public void setServiceBusinessPriority(String serviceBusinessPriority) {
		this.serviceBusinessPriority = serviceBusinessPriority;
	}
}
