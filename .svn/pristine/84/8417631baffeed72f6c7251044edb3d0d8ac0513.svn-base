package com.knot.provisioning.serviceimpl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.knot.provioning.model.ProvRespDetails;
import com.knot.provisioning.dao.CacheDao;
import com.knot.provisioning.dto.ErrorCodeDTO;
import com.knot.provisioning.network.CatNetwork;
import com.knot.provisioning.network.RefMasterNetwork;
import com.knot.provisioning.service.CacheService;
import com.knot.redis.dto.OcsRPMapData;

@Service
public class CacheServiceImpl implements CacheService {

	public static final Logger log = LogManager.getLogger(CacheServiceImpl.class);

	private CacheDao dao;
	private CatNetwork catNet;
	private RefMasterNetwork refNet;

	@Autowired
	public void setRefNet(RefMasterNetwork refNet) {
		this.refNet = refNet;
	}

	@Autowired
	public void setCatNet(CatNetwork catNet) {
		this.catNet = catNet;
	}

	@Autowired
	public void setConfig(CacheDao dao) {
		this.dao = dao;
	}

	@Override
	public String strConfigVal(String code, String tenantID) {
		return dao.strConfigVal(code, tenantID);
	}

	@Override
	public Integer intConfigVal(String code, String tenantID) {
		return dao.intConfigVal(code, tenantID);
	}

	@Override
	public Long longConfigVal(String code, String tenantID) {
		return dao.longConfigVal(code, tenantID);
	}


	@Override
	public ProvRespDetails getResponseDetailsMap(Integer provObjId) {
		Function<ProvRespDetails, BigInteger> keyMapper = resp -> resp.getServiceProvReqID();
		Function<ProvRespDetails, List<ProvRespDetails>> valueMapper = resp -> Arrays.asList(resp);
		BinaryOperator<List<ProvRespDetails>> merger = (oldvalue, newvalue) -> {
			ArrayList<ProvRespDetails> list1 = new ArrayList<>();
			list1.addAll(oldvalue);
			list1.addAll(newvalue);
			return list1;
		};
		List<ProvRespDetails> list = dao.getProvResponseDetails();
		Map<BigInteger, List<ProvRespDetails>> responseDetailsMap = list.stream()
				.collect(Collectors.toMap(keyMapper, valueMapper, merger));
		List<ProvRespDetails> responseList = responseDetailsMap.get(BigInteger.valueOf(provObjId));
		if (responseList != null && !responseList.isEmpty()) {
			ProvRespDetails respDtl = responseList.stream().findFirst().orElse(null);
			if (respDtl != null) {
				return respDtl;
			}
			log.error("ResponseObjects not found for id: {}", provObjId);
		} else {
			log.error("ResponseObjects not found in map for id: {}", provObjId);
		}
		return null;
	}

	@Override
	public String getOcsPlanData(String id) {
		List<OcsRPMapData> ocsData = catNet.loadOCSData();
		Map<String, String> ocsMap = new HashMap<>();
		ocsData.stream().forEach(o -> ocsMap.put(o.getOcsRatePlanID(), o.getOobRatePlanID()));
		return ocsMap.getOrDefault(id, "-1");
	}
	
	@Override
	public String getErrorDescription(String errorCode) {
		Map<String, ErrorCodeDTO> errorMap = refNet.getAllErrorCode();
		
		String errorDescription = "";
		ErrorCodeDTO errorCodeDTO = errorMap.get(errorCode);
		if (errorCodeDTO != null) {
			errorDescription = errorCodeDTO.getErrorDescription();
		}
		log.error("Error Code : {} Not Configured.", errorCode);
		return errorDescription;
	}
	
	@Override
	public String configValByCodeForGraceFullStop(String code, String tenantID) {
		return dao.configValByCodeForGraceFullStop(code, tenantID);
	}


}
