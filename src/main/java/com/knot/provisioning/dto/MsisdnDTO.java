package com.knot.provisioning.dto;

import java.io.Serializable;

import com.knot.tring.procesprov.schema.ActiveFirstRequest;

public class MsisdnDTO implements Serializable{

	/**
	 * @author SAI D
	 */
	private static final long serialVersionUID = 1L;
	
	private String msisdn;

	private String source;
	
	private String flag;
	
	private String requestCompletionTime;
	
	private String reqDtlID;
	
	

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	

	public String getRequestCompletionTime() {
		return requestCompletionTime;
	}

	public void setRequestCompletionTime(String requestCompletionTime) {
		this.requestCompletionTime = requestCompletionTime;
	}
	
	public String getReqDtlID() {
		return reqDtlID;
	}

	public void setReqDtlID(String reqDtlID) {
		this.reqDtlID = reqDtlID;
	}

	

	public MsisdnDTO() {
		super();
	}
	
	public MsisdnDTO(ActiveFirstRequest request) {
		super();
		this.msisdn = request.getMsisdn();
		this.source = request.getSource();
		this.flag = "Y";
		this.requestCompletionTime = request.getRequestCompletionTime();
		this.reqDtlID = request.getReqDtlID();
		
	}
	
	
}
