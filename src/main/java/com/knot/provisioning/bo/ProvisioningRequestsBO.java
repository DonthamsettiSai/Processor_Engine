package com.knot.provisioning.bo;

import java.io.Serializable;
import java.math.BigInteger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * @author sai D
 */
@Entity
public class ProvisioningRequestsBO implements Serializable{


	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="REQUEST_ID")
	private BigInteger requestID;
	
	@Column(name = "CUSTOMER_ID")
	private String customerID;
	
	@Column(name = "NODE_ID")
	private String nodeID;
	
	@Column(name = "CUSTOMER_ACCOUNT_ID")
	private String accountID;
	
	@Column(name = "TENANT_ID")
	private String tenantID;
	
	@Column(name = "REQUEST_CREATION_DATE")
	private String requestCreationDate;
	
	@Column(name = "STATUS")
	private String status;
	
	@Column(name = "activity_id")
	private String activityID;
	
	@Column(name = "WORK_ORDER_ID")
	private String workOrderId;
	
	public String getWorkOrderId() {
		return workOrderId;
	}

	public void setWorkOrderId(String workOrderId) {
		this.workOrderId = workOrderId;
	}

	public BigInteger getRequestID() {
		return requestID;
	}

	public void setRequestID(BigInteger requestID) {
		this.requestID = requestID;
	}

	public String getCustomerID() {
		return customerID;
	}

	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}

	public String getAccountID() {
		return accountID;
	}

	public void setAccountID(String accountID) {
		this.accountID = accountID;
	}

	public String getRequestCreationDate() {
		return requestCreationDate;
	}

	public void setRequestCreationDate(String requestCreationDate) {
		this.requestCreationDate = requestCreationDate;
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
	
	/**
	 * public String getCustomerName() { return customerName; }
	 * 
	 * public void setCustomerName(String customerName) { this.customerName =
	 * customerName; }
	 * 
	 * public String getAccountName() { return accountName; }
	 * 
	 * public void setAccountName(String accountName) { this.accountName =
	 * accountName; }
	 */

	public String getActivityID() {
		return activityID;
	}

	public void setActivityID(String activityID) {
		this.activityID = activityID;
	}
	
	
}
