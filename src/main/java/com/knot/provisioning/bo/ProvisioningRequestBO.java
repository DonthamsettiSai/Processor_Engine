package com.knot.provisioning.bo;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ProvisioningRequestBO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="REQUEST_ID")
	private BigInteger requestID;

	@Column(name="TENANT_ID")
	private BigInteger tenantID;	

	@Column(name="PARTNER_ID")
	private BigInteger partnerID;
	
	@Column(name ="TNT_REQUEST_ID")
	private String tntRequestID;

	@Column(name="CUSTOMER_ID")
	private BigInteger customerID;	

	@Column(name="NODE_ID")
	private BigInteger nodeID;	

	@Column(name="CUSTOMER_ACCOUNT_ID")
	private BigInteger customerAccountID;

	
	@Column(name="WORK_ORDER_ID")
	private BigInteger workOrderID;
	
	@Column(name="ACTIVITY_ID")
	private Integer actvityId;

	@Column(name="ACTION_ID")
	private Integer actionId;
	
	@Column(name="ASSIGNED_QUEUE_ID")
	private Integer assignedQueueId;
	
	@Column(name="REQUEST_CREATION_DATE")
	private LocalDateTime requestCreationDate;
	
	@Column(name="REQUEST_PICKED_DATE")
	private LocalDateTime requestPickedDate;
	
	@Column(name="REQUEST_COMPLETION_DATE")
	private LocalDateTime requestCompletionDate;
	
	@Column(name="SLA_APPLICABLE")
	private Character slaApplicable;
	
	@Column(name="SLA_MET")
	private Character slaMet;
	
	@Column(name="REMARKS")
	private String remarks;
	
	@Column(name="RESOURCE_DTL_ID")
	private Integer resourceDtlId;
	
	@Column(name="STATUS")
	private Integer status;
	
	
	@Column(name="CREATED_BY")
	private String createdBy;
	
	@Column(name="CREATED_ON")
	private LocalDateTime createdOn;
	
	
	@Column(name="CREATED_IPADDR")
	private String createdIpaddr;
	
	@Column(name="MODIFIED_BY")
	private String modifiedBy;
	
	@Column(name="MODIFIED_ON")
	private LocalDateTime modifiedOn;
	
	@Column(name="MODIFIED_IPADDR")
	private String modifiedIpaddr;

	public BigInteger getRequestID() {
		return requestID;
	}

	public void setRequestID(BigInteger requestID) {
		this.requestID = requestID;
	}

	public BigInteger getTenantID() {
		return tenantID;
	}

	public void setTenantID(BigInteger tenantID) {
		this.tenantID = tenantID;
	}

	public BigInteger getPartnerID() {
		return partnerID;
	}

	public void setPartnerID(BigInteger partnerID) {
		this.partnerID = partnerID;
	}

	public String getTntRequestID() {
		return tntRequestID;
	}

	public void setTntRequestID(String tntRequestID) {
		this.tntRequestID = tntRequestID;
	}

	public BigInteger getCustomerID() {
		return customerID;
	}

	public void setCustomerID(BigInteger customerID) {
		this.customerID = customerID;
	}

	public BigInteger getNodeID() {
		return nodeID;
	}

	public void setNodeID(BigInteger nodeID) {
		this.nodeID = nodeID;
	}

	public BigInteger getCustomerAccountID() {
		return customerAccountID;
	}

	public void setCustomerAccountID(BigInteger customerAccountID) {
		this.customerAccountID = customerAccountID;
	}

	public BigInteger getWorkOrderID() {
		return workOrderID;
	}

	public void setWorkOrderID(BigInteger workOrderID) {
		this.workOrderID = workOrderID;
	}

	public Integer getActvityId() {
		return actvityId;
	}

	public void setActvityId(Integer actvityId) {
		this.actvityId = actvityId;
	}

	public Integer getActionId() {
		return actionId;
	}

	public void setActionId(Integer actionId) {
		this.actionId = actionId;
	}

	public Integer getAssignedQueueId() {
		return assignedQueueId;
	}

	public void setAssignedQueueId(Integer assignedQueueId) {
		this.assignedQueueId = assignedQueueId;
	}

	public LocalDateTime getRequestCreationDate() {
		return requestCreationDate;
	}

	public void setRequestCreationDate(LocalDateTime requestCreationDate) {
		this.requestCreationDate = requestCreationDate;
	}

	public LocalDateTime getRequestPickedDate() {
		return requestPickedDate;
	}

	public void setRequestPickedDate(LocalDateTime requestPickedDate) {
		this.requestPickedDate = requestPickedDate;
	}

	public LocalDateTime getRequestCompletionDate() {
		return requestCompletionDate;
	}

	public void setRequestCompletionDate(LocalDateTime requestCompletionDate) {
		this.requestCompletionDate = requestCompletionDate;
	}

	public Character getSlaApplicable() {
		return slaApplicable;
	}

	public void setSlaApplicable(Character slaApplicable) {
		this.slaApplicable = slaApplicable;
	}

	public Character getSlaMet() {
		return slaMet;
	}

	public void setSlaMet(Character slaMet) {
		this.slaMet = slaMet;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Integer getResourceDtlId() {
		return resourceDtlId;
	}

	public void setResourceDtlId(Integer resourceDtlId) {
		this.resourceDtlId = resourceDtlId;
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

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

	public String getCreatedIpaddr() {
		return createdIpaddr;
	}

	public void setCreatedIpaddr(String createdIpaddr) {
		this.createdIpaddr = createdIpaddr;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public LocalDateTime getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(LocalDateTime modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public String getModifiedIpaddr() {
		return modifiedIpaddr;
	}

	public void setModifiedIpaddr(String modifiedIpaddr) {
		this.modifiedIpaddr = modifiedIpaddr;
	}
	
	
}
