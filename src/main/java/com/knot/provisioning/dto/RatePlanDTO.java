package com.knot.provisioning.dto;

import java.io.Serializable;

public class RatePlanDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String ratePlanID;
	
	private String ratePlanName;
	
	private String status;
	
	private String ratePlantype;
	
	public String getRatePlanID() {
		return ratePlanID;
	}
	public void setRatePlanID(String ratePlanID) {
		this.ratePlanID = ratePlanID;
	}
	public String getRatePlanName() {
		return ratePlanName;
	}
	public void setRatePlanName(String ratePlanName) {
		this.ratePlanName = ratePlanName;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRatePlantype() {
		return ratePlantype;
	}
	public void setRatePlantype(String ratePlantype) {
		this.ratePlantype = ratePlantype;
	}

	

}
