package com.knot.provisioning.bo;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/**
 * 
 * @author Sameena
 *
 */

public class NetworkIdBOPK implements Serializable {

	private static final long serialVersionUID = 1L;

    private BigInteger lobID;	

	
	private BigInteger customerID;	
	
	private BigInteger customerAccountID;

	public BigInteger getLobID() {
		return lobID;
	}

	public void setLobID(BigInteger lobID) {
		this.lobID = lobID;
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

	@Override
	public int hashCode() {
		return Objects.hash(customerAccountID, customerID, lobID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NetworkIdBOPK other = (NetworkIdBOPK) obj;
		return Objects.equals(customerAccountID, other.customerAccountID)
				&& Objects.equals(customerID, other.customerID) && Objects.equals(lobID, other.lobID);
	}


}
