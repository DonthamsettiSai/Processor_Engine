package com.knot.provioning.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public class ProvisioningRequestDetailPK implements Serializable {

	/**
	 * 
	 */
	
	
	private static final long serialVersionUID = 1L;

	private BigInteger requestDetailID;  	

	private float version;

	public BigInteger getRequestDetailID() {
		return requestDetailID;
	}

	public void setRequestDetailID(BigInteger requestDetailID) {
		this.requestDetailID = requestDetailID;
	}

	public float getVersion() {
		return version;
	}

	public void setVersion(float version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		return Objects.hash(requestDetailID, version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProvisioningRequestDetailPK other = (ProvisioningRequestDetailPK) obj;
		return Objects.equals(requestDetailID, other.requestDetailID)
				&& Float.floatToIntBits(version) == Float.floatToIntBits(other.version);
	}

	
	
	

}/**
package com.knot.provioning.model;

import java.io.Serializable;

public class ProvisioningRequestDetailPK implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer requestDetailID;  	

	private float version;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((requestDetailID == null) ? 0 : requestDetailID.hashCode());
		result = prime * result + Float.floatToIntBits(version);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProvisioningRequestDetailPK other = (ProvisioningRequestDetailPK) obj;
		if (requestDetailID == null) {
			if (other.requestDetailID != null)
				return false;
		} else if (!requestDetailID.equals(other.requestDetailID))
			return false;
		if (Float.floatToIntBits(version) != Float.floatToIntBits(other.version))
			return false;
		return true;
	}

	

}*/
