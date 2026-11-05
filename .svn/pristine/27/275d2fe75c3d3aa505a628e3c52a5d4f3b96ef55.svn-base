package com.knot.provioning.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public class ProvisioningRequestResponseDetailPK implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private BigInteger responseID; 
	
	private Integer responseSequenceNo;

	public BigInteger getResponseID() {
		return responseID;
	}

	public void setResponseID(BigInteger responseID) {
		this.responseID = responseID;
	}

	public Integer getResponseSequenceNo() {
		return responseSequenceNo;
	}

	public void setResponseSequenceNo(Integer responseSequenceNo) {
		this.responseSequenceNo = responseSequenceNo;
	}

	@Override
	public int hashCode() {
		return Objects.hash(responseID, responseSequenceNo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProvisioningRequestResponseDetailPK other = (ProvisioningRequestResponseDetailPK) obj;
		return Objects.equals(responseID, other.responseID)
				&& Objects.equals(responseSequenceNo, other.responseSequenceNo);
	}

	
}
