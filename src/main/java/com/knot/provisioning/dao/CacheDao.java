package com.knot.provisioning.dao;

import java.util.List;

import com.knot.provioning.model.ProvRespDetails;

public interface CacheDao {

	String strConfigVal(String code, String tenantID);

	Integer intConfigVal(String code, String tenantID);

	Long longConfigVal(String code, String tenantID);

	List<ProvRespDetails> getProvResponseDetails();
	
	public String configValByCodeForGraceFullStop(String code, String tenantID);

}
