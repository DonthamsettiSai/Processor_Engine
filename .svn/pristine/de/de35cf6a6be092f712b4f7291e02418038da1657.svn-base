package com.knot.provioning.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "provisioning_requests")
public class ProvisioningRequest implements Serializable {

	/** 	  
	*/

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "REQUEST_ID")
	private BigInteger requestID;

	@Column(name = "TENANT_ID")
	private BigInteger tenantID;

	@Column(name = "PARTNER_ID")
	private BigInteger partnerID;

	@Column(name = "CUSTOMER_ID")
	private BigInteger customerID;

	@Column(name = "NODE_ID")
	private BigInteger nodeID;

	@Column(name = "CUSTOMER_ACCOUNT_ID")
	private BigInteger customerAccountID;

	@Column(name = "WORK_ORDER_ID")
	private BigInteger workOrderID;

	@Column(name = "ACTIVITY_ID")
	private String activityID;

	@Column(name = "ACTION_ID")
	private Integer actionID;

	@Column(name = "REQUEST_CREATION_DATE")
	private Date requestCreationDate;

	@Column(name = "REQUEST_PICKED_DATE")
	private Date requestPickedDate;

	@Column(name = "REQUEST_COMPLETION_DATE")
	private Date requestCompletionDate;

	@Column(name = "STATUS")
	private int status;

	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "CREATED_ON")
	private Date createdOn;

	@Column(name = "CREATED_IPADDR")
	private String createdIP;

	@Column(name = "MODIFIED_BY")
	private String modifiedBy;

	@Column(name = "MODIFIED_ON")
	private Date modifiedOn;

	@Column(name = "MODIFIED_IPADDR")
	private String modifiedIPAddr;
	
	@Column(name = "PRIORITY")
	private Integer priority;

	public Integer getPriority() {
	    return priority;
	}

	public void setPriority(Integer priority) {
	    this.priority = priority;
	}

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

	public String getActivityID() {
		return activityID;
	}

	public void setActivityID(String activityID) {
		this.activityID = activityID;
	}

	public Integer getActionID() {
		return actionID;
	}

	public void setActionID(Integer actionID) {
		this.actionID = actionID;
	}

	public Date getRequestCreationDate() {
		return requestCreationDate;
	}

	public void setRequestCreationDate(Date requestCreationDate) {
		this.requestCreationDate = requestCreationDate;
	}

	public Date getRequestPickedDate() {
		return requestPickedDate;
	}

	public void setRequestPickedDate(Date requestPickedDate) {
		this.requestPickedDate = requestPickedDate;
	}

	public Date getRequestCompletionDate() {
		return requestCompletionDate;
	}

	public void setRequestCompletionDate(Date requestCompletionDate) {
		this.requestCompletionDate = requestCompletionDate;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getCreatedIP() {
		return createdIP;
	}

	public void setCreatedIP(String createdIP) {
		this.createdIP = createdIP;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public String getModifiedIPAddr() {
		return modifiedIPAddr;
	}

	public void setModifiedIPAddr(String modifiedIPAddr) {
		this.modifiedIPAddr = modifiedIPAddr;
	}
}
