package com.knot.provioning.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Sathish Ravula
 */
public class ProvisioningReqDetailsPK implements Serializable {

	private static final long serialVersionUID = 1L;

	private BigInteger requesDetailID;

	private Float versionNO;

//	private Float requestID;

//	public Float getRequestID() {
//		return requestID;
//	}
//
//	public void setRequestID(Float requestID) {
//		this.requestID = requestID;
//	}

	public ProvisioningReqDetailsPK() {
	}

	public BigInteger getRequesDetailID() {
		return requesDetailID;
	}

	public void setRequesDetailID(BigInteger requesDetailID) {
		this.requesDetailID = requesDetailID;
	}

	/**
	 * @return the versionNO
	 */
	public Float getVersionNO() {
		return versionNO;
	}

	/**
	 * @param versionNO the versionNO to set
	 */
	public void setVersionNO(Float versionNO) {
		this.versionNO = versionNO;
	}

	@Override
	public int hashCode() {
		return Objects.hash(requesDetailID, versionNO);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProvisioningReqDetailsPK other = (ProvisioningReqDetailsPK) obj;
		return Objects.equals(requesDetailID, other.requesDetailID) && Objects.equals(versionNO, other.versionNO);
	}

}
