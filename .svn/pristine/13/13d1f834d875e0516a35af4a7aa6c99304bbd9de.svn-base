package com.knot.provisioning.bo;

import java.io.Serializable;
import java.math.BigInteger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ProvisioningRequestDetailBO implements Serializable {

	/**
	 *   `` bigint(11) NOT NULL,
  `REQUEST_ID` bigint(11) NOT NULL,
  `LOB_ID` bigint(11) NOT NULL,
  `SERVICE_ID` bigint(11) NOT NULL,
  `SERVICE_PACKAGE_ID` bigint(11) NOT NULL,
  `RATE_PLAN_ID` bigint(11) NOT NULL,
  `INTERFACE_TYPE` varchar(100) DEFAULT NULL,
  `SERVER_ID` bigint(11) NOT NULL,
  `SERVER_URL` varchar(100) DEFAULT NULL,
  `TOKEN_ID` varchar(255) DEFAULT NULL,
  `LOB_PRIORITY` int(4) NOT NULL,
  `SERVICE_PRIORITY` int(4) NOT NULL,
  `RATE_PLAN_PRIORITY` int(4) NOT NULL,
  `SEQUENCE_ID` int(4) NOT NULL,
  `SUB_SEQUENCE_ID` int(4) NOT NULL,
  `DEPENDENT_SEQUENCE_ID` int(4) DEFAULT NULL,
  `DEPENDENT_SUB_SEQUENCE_ID` int(4) DEFAULT NULL,
  `DEP_SUB_SEQ_PROCESSING_TYPE` char(1) DEFAULT NULL,
  `CAPTURE_DEP_SUB_SEQ_RESPONSE` char(1) NOT NULL DEFAULT 'N',
  `REQUEST_OBJECT` text,
  `MAX_RETRIES` tinyint(4) DEFAULT NULL,
  `TIME_GAP_BETWEEN_RETRY` int(11) DEFAULT NULL,
  `TIME_GAP_UNIT` tinyint(4) DEFAULT NULL,
  `CURRENT_RETRY_COUNT` tinyint(4) DEFAULT NULL,
  `REQUEST_CREATION_DATE` datetime DEFAULT NULL,
  `REQUEST_PICKED_DATE` datetime DEFAULT NULL,
  `REQUEST_COMPLETION_DATE` datetime DEFAULT NULL,
  `SLA_TIME` int(11) DEFAULT '0',
  `SLA_UNIT` tinyint(4) DEFAULT NULL,
  `ACTUAL_PROCESS_TIME` int(11) DEFAULT '0',
  `SLA_VIOLATION_STATUS` char(1) DEFAULT NULL,
  `STATUS` tinyint(4) NOT NULL DEFAULT '11',
  `VERSION_NO` decimal(2,1) DEFAULT NULL,
  `VERSION_DATE` datetime DEFAULT NULL,
  `ERROR_CODE` varchar(25) DEFAULT NULL,
  `ERROR_DESCRIPTION` text,
  PRIMARY KEY (`REQUEST_DETAIL_ID`)
	 */
	private static final long serialVersionUID = 1L;

	@Id
	
	
	@Column(name="REQUEST_DETAIL_ID")
	private BigInteger requestDetailID;
	@Column(name="REQUEST_ID")
	private BigInteger requestID;

	@Column(name="TENANT_ID")
	private BigInteger tenantID;	

	@Column(name="PARTNER_ID")
	private BigInteger partnerID;		

	@Column(name="CUSTOMER_ID")
	private BigInteger customerID;	

	@Column(name="NODE_ID")
	private BigInteger nodeID;	

	@Column(name="CUSTOMER_ACCOUNT_ID")
	private BigInteger customerAccountID;	

	@Column(name="WORK_ORDER_ID")
	private BigInteger workOrderID;	

	@Column(name="ACTION_ID")
	private Integer actionID;

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

	public Integer getActionID() {
		return actionID;
	}

	public void setActionID(Integer actionID) {
		this.actionID = actionID;
	}

}
