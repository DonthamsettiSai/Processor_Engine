package com.knot.provisioning.dto;

import java.io.Serializable;

public class PageCount implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String PLD;
	private String RPD;
	public String getPLD() {
		return PLD;
	}
	public void setPLD(String pLD) {
		PLD = pLD;
	}
	public String getRPD() {
		return RPD;
	}
	public void setRPD(String rPD) {
		RPD = rPD;
	}

}
