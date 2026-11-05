package com.knot.provisioning.dto;

import java.io.Serializable;

public class ServiceActDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private String workOrderId;


	public String getWorkOrderId() {
		return workOrderId;
	}


	public void setWorkOrderId(String workOrderId) {
		this.workOrderId = workOrderId;
	}
	
	

}
