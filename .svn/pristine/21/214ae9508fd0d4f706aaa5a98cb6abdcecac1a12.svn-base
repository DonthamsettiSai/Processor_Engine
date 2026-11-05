package com.knot.provisioning.bo;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * 
 * @author 
 *
 *
 *   
 */

@Entity
@Table(name = "customer_accounts_network_ids")
@IdClass(NetworkIdBOPK.class)
public class CustomerAccountNetworkIDBO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "LOB_ID")
	private BigInteger lobID;	

	@Column(name = "NETWORK_ID")
	private String networkID;

	@Id
	@Column(name = "CUSTOMER_ID")
	private BigInteger customerID;	

	@Id
	@Column(name = "CUSTOMER_ACCOUNT_ID")
	private BigInteger customerAccountID;

	@Column(name = "TENANT_ID")
	private BigInteger tenantID;
	
	@Column(name = "PARTNER_ID")
	private BigInteger partnerID;
	
	@Column(name = "DEVICE_SERIAL_NO")
	private String  deviceSerialNo;
	
	@Column(name = "CREATED_ON")
	private Date  createdOn;
	
	public BigInteger getLobID() {
		return lobID;
	}

	public void setLobID(BigInteger lobID) {
		this.lobID = lobID;
	}

	public String getNetworkID() {
		return networkID;
	}

	public void setNetworkID(String networkID) {
		this.networkID = networkID;
	}

	public BigInteger getCustomerID() {
		return customerID;
	}

	public void setCustomerID(BigInteger customerID) {
		this.customerID = customerID;
	}

	public BigInteger getCustomerAccountID() {
		return customerAccountID;
	}

	public void setCustomerAccountID(BigInteger customerAccountID) {
		this.customerAccountID = customerAccountID;
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

	public String getDeviceSerialNo() {
		return deviceSerialNo;
	}

	public void setDeviceSerialNo(String deviceSerialNo) {
		this.deviceSerialNo = deviceSerialNo;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

}
